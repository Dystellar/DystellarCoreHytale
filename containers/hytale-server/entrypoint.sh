#!/bin/sh

cd "$BASEDIR"

latest=$(ls ../hytale-release/*.zip | sort -t'-' -k1 | tail -1)

unzip -o "../hytale-release/$latest" -d .

