#!/usr/bin/env python3
"""Verify that a built XNagram APK really contains the Nagram strings.

Mirrors what the app does at runtime (see Localization.java):
  R.string.X -> assets/string_resource_ids.bin (resId -> name hash)
             -> assets/localization_<lang>.bin (name hash -> value)
A name missing from either table shows up in the UI as LOC_ERR.

Usage: verify_localization.py <apk> <res-dir> [LocalizationUtils.java]
  res-dir = TMessagesProj/src/main/res. With the generated LocalizationUtils.java the
  locale -> asset mapping is simulated too (Locale("tr") must find the Turkish asset).
Exit code 0 = all good, 1 = problems found.
"""
import glob
import os
import re
import struct
import sys
import xml.etree.ElementTree as ET
import zipfile


def java_hash(name):
    h = 0
    for ch in name.encode("utf-16-le").decode("utf-16-le"):
        h = (31 * h + ord(ch)) & 0xFFFFFFFF
    return h


def string_names(path):
    root = ET.parse(path).getroot()
    return {s.get("name"): (s.text or "") for s in root.findall("string")}


def read_localization(data):
    """int32 count, then (int32 hash, string) pairs. String = len byte(s) + utf8 + pad to 4."""
    pos = 0
    (count,) = struct.unpack_from("<i", data, pos)
    pos += 4
    result = {}
    for _ in range(count):
        (h,) = struct.unpack_from("<I", data, pos)
        pos += 4
        start = pos
        length = data[pos]
        pos += 1
        if length >= 254:
            length = data[pos] | (data[pos + 1] << 8) | (data[pos + 2] << 16)
            pos += 3
        value = data[pos:pos + length].decode("utf-8", "replace")
        pos += length
        total = pos - start
        pos += (-total) % 4
        result[h] = value
    return result


def read_res_bindings(data):
    (count,) = struct.unpack_from("<i", data, 0)
    return {struct.unpack_from("<I", data, 8 + 8 * i)[0] for i in range(count)}, count


def tag_of(dirname):
    q = dirname[len("values-"):]
    if q.startswith("b+"):
        return q[2:].replace("+", "-")
    if "-r" in q:
        lang, region = q.split("-r", 1)
        return lang + "-" + region
    return q


def asset_name(tag):
    return "localization_" + re.sub(r"[^a-z0-9_]", "_", tag.lower()) + ".bin"


def main():
    apk, resdir = sys.argv[1], sys.argv[2]
    problems = []
    zf = zipfile.ZipFile(apk)
    names_in_apk = set(zf.namelist())

    def asset(n):
        return zf.read("assets/" + n) if "assets/" + n in names_in_apk else None

    bind_raw = asset("string_resource_ids.bin")
    if bind_raw is None:
        print("FAIL: assets/string_resource_ids.bin missing")
        return 1
    bound_hashes, bound_count = read_res_bindings(bind_raw)
    print(f"string_resource_ids.bin: {bound_count} entries")

    # default (en) strings: all strings*.xml in values/
    en_expected = {}
    for f in sorted(glob.glob(os.path.join(resdir, "values", "strings*.xml"))):
        en_expected.update(string_names(f))
    en_raw = asset("localization_en.bin")
    if en_raw is None:
        print("FAIL: localization_en.bin missing")
        return 1
    en = read_localization(en_raw)
    print(f"localization_en.bin: {len(en)} entries, expected {len(en_expected)} source strings")

    missing_en = [n for n in en_expected if java_hash(n) not in en]
    missing_bind = [n for n in en_expected if java_hash(n) not in bound_hashes]
    if missing_en:
        problems.append(f"{len(missing_en)} strings missing from localization_en.bin, e.g. {missing_en[:8]}")
    if missing_bind:
        problems.append(f"{len(missing_bind)} strings missing from string_resource_ids.bin, e.g. {missing_bind[:8]}")

    # spot checks: one string from each Nagram file and the app name
    for key in ("NagramX", "TelegramUIAutoTranslate", "TranslateButtonMode"):
        h = java_hash(key)
        if h not in en or en[h].startswith("LOC_ERR") or not en[h]:
            problems.append(f"spot check failed (en): {key} -> {en.get(h)!r}")
        else:
            print(f"spot check ok (en): {key} = {en[h]!r}")

    # per-language assets
    tags = {}
    for d in sorted(glob.glob(os.path.join(resdir, "values-*"))):
        files = sorted(glob.glob(os.path.join(d, "strings*.xml")))
        if not files:
            continue
        tags.setdefault(tag_of(os.path.basename(d)), []).extend(files)

    for tag, files in sorted(tags.items()):
        expected = {}
        for f in files:
            expected.update(string_names(f))
        raw = asset(asset_name(tag))
        if raw is None:
            problems.append(f"language asset missing for {tag}: {asset_name(tag)}")
            continue
        loc = read_localization(raw)
        miss = [n for n in expected if java_hash(n) not in loc]
        if miss:
            problems.append(f"{tag}: {len(miss)}/{len(expected)} strings missing, e.g. {miss[:6]}")
        else:
            print(f"language {tag}: {len(expected)} strings ok")

    # locale -> asset resolution (mirrors the generated LocalizationUtils.getLocalizationAsset)
    if len(sys.argv) > 3:
        src = open(sys.argv[3], encoding="utf-8").read()
        head, _, tail = src.partition("final String language")
        exact = dict(re.findall(r'case "([^"]+)": return "([^"]+)";', head))
        by_lang = dict(re.findall(r'case "([^"]+)": return "([^"]+)";', tail))

        def resolve(tag, language):
            return exact.get(tag) or by_lang.get(language)

        names_by_lang = {}
        for tag, files in tags.items():
            for f in files:
                names_by_lang.setdefault(tag.split("-")[0], set()).update(string_names(f))
        for lang, expected in sorted(names_by_lang.items()):
            asset_file = resolve(lang, lang)
            if asset_file is None:
                problems.append(f"Locale('{lang}') finds NO asset (would show English / LOC_ERR)")
                continue
            raw = asset(asset_file)
            loc = read_localization(raw) if raw is not None else {}
            miss = [n for n in expected if java_hash(n) not in loc]
            if miss:
                problems.append(f"Locale('{lang}') -> {asset_file}: {len(miss)} strings missing, e.g. {miss[:5]}")
            else:
                print(f"locale '{lang}' -> {asset_file}: all {len(expected)} strings reachable")

    # Turkish must really be translated (not just the English text)
    tr = asset(asset_name("tr-TR"))
    if tr is not None:
        trl = read_localization(tr)
        h = java_hash("TelegramUIAutoTranslate")
        if h in trl and h in en and trl[h] == en[h]:
            problems.append("tr-TR: TelegramUIAutoTranslate is identical to the English text")
        elif h in trl:
            print(f"spot check ok (tr-TR): TelegramUIAutoTranslate = {trl[h]!r}")

    if problems:
        print("\nVERIFY LOCALIZATION: FAILED")
        for p in problems:
            print(" -", p)
        return 1
    print("\nVERIFY LOCALIZATION: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
