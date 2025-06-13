#!/bin/bash
# push_gemma.sh – copy Gemma model into the app’s private files folder.

set -e                          # stop on first error

PKG="com.example.aimoodjournal"             # <-- your package name
SRC="$HOME/src/models/gemma-3n-E4B-it-int4.task" # <-- local model path
TMP="/data/local/tmp/$(basename "$SRC")"    # temp path on device
DST="files/models/$(basename "$SRC")"       # final path inside app

echo "Pushing model to device temp..."
adb push "$SRC" "$TMP"

echo "Copying into app sandbox..."
adb shell "run-as $PKG mkdir -p files/models &&
           run-as $PKG cp $TMP $DST &&
           run-as $PKG ls -lh $DST"

echo "Cleaning up temp file..."
adb shell rm "$TMP"

echo "Done – model lives at /data/data/$PKG/$DST"
