#!/usr/bin/env python3

import subprocess
import sys
from pathlib import Path


if len(sys.argv) != 8:
    print(f"Usage: {sys.argv[0]} <api_url> <key1> <key2> <key3> <key4> <key5> <key6>")
    sys.exit(1)

api_url = sys.argv[1]
api_keys = sys.argv[2:]

generator = Path(__file__).with_name("sensor_generator.py")

configs = [
    ("-20", "30", "0", "gauss"),
    ("-20", "30", "0", "gauss"),
    ("0", "100", "50", "gauss"),
    ("0", "100", "50", "gauss"),
    ("900", "1100", "1000", "gauss"),
    ("900", "1100", "1000", "gauss"),
]

for api_key, (min_value, max_value, starting_value, rng ) in zip(api_keys, configs):
    subprocess.run([
        sys.executable,
        str(generator),
        "--count", "60",
        "--rng", rng,
        "--min-value", min_value,
        "--max-value", max_value,
        "--starting_pos", starting_value,
        api_url,
        api_key,
    ], check=True)