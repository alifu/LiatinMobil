#!/usr/bin/env bash
# Regenerates the offline voice clips and the horn in app/src/main/res/raw.
# Needs macOS (uses the built-in Indonesian voice "Damayanti", `say` and `afconvert`) and python3.
# Usage: tools/generate_sounds.sh
set -euo pipefail

VOICE="Damayanti"
RATE=160                      # words per minute, a little slower so kids can follow
SAMPLE_RATE=22050
OUT_DIR="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/res/raw"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

if ! say -v '?' | grep -q "^$VOICE "; then
  echo "Voice '$VOICE' not found. Install it in System Settings > Accessibility > Spoken Content." >&2
  exit 1
fi

mkdir -p "$OUT_DIR"

speak() { # speak <file name without extension> <text>
  say -v "$VOICE" -r "$RATE" -o "$WORK_DIR/$1.aiff" "$2"
  afconvert -f WAVE -d "LEI16@$SAMPLE_RATE" -c 1 "$WORK_DIR/$1.aiff" "$OUT_DIR/$1.wav"
}

speak voice_turn_left      "Belok kiri!"
speak voice_turn_right     "Belok kanan!"
speak voice_driver_arrived "Driver sudah sampai. Ayo naik!"
speak voice_red_light      "Lampu merah. Berhenti dulu ya."
speak voice_arrived        "Hore! Kita sudah sampai!"

# Horn: two short two-tone beeps, "tin tin".
python3 - "$OUT_DIR/sfx_horn.wav" "$SAMPLE_RATE" <<'PY'
import math, struct, sys, wave

path, rate = sys.argv[1], int(sys.argv[2])
NOTES = (440.0, 554.0)        # two notes played together sound like a car horn
BEEP, GAP, FADE = 0.16, 0.07, 0.015

def beep():
    count = int(BEEP * rate)
    for i in range(count):
        t = i / rate
        envelope = min(1.0, t / FADE, (BEEP - t) / FADE)
        wave_sum = sum(math.sin(2 * math.pi * f * t) + 0.4 * math.sin(4 * math.pi * f * t) for f in NOTES)
        yield 0.65 * envelope * wave_sum / len(NOTES)

samples = list(beep()) + [0.0] * int(GAP * rate) + list(beep())
with wave.open(path, "wb") as out:
    out.setnchannels(1)
    out.setsampwidth(2)
    out.setframerate(rate)
    out.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, s)) * 32767)) for s in samples))
PY

echo "Generated:"; ls -l "$OUT_DIR"
