#!/bin/bash

IN_FILE="duom.txt"
IN_DIR=/tts_input
FORMAT_DIR=/formatter
OUT_DIR=/tts_output

echo "-- Clean-up"
rm "$FORMAT_DIR/$IN_FILE"
rm "$OUT/*"
echo "-- Importing input"
#cp --verbose /tts_input/duom.txt /formatter/duom.txt
cp --verbose "$IN_DIR/$IN_FILE" "$FORMAT_DIR/$IN_FILE"
LAST_CMD_STATUS=$?
if [[ $LAST_CMD_STATUS -ne 0 ]] ; then
    echo "Error: 'Copy input' cmd status: ${LAST_CMD_STATUS}"
    exit $LAST_CMD_STATUS
fi

echo "-- Running translation program"
wine --version
cd formatter
wine "LithUSStest.exe" || true

echo "-- Exporting wavs"
cp --verbose *.wav "$OUT_DIR"
