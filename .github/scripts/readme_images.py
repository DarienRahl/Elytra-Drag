#!/usr/bin/env python3
"""The README's pictures from the game's screenshots the client game test took (screenshots workflow).

    python3 .github/scripts/readme_images.py <screenshots folder> <docs/images/screenshots>

Needs Pillow. The screenshots are glide-back, glide-front, drag-back and drag-front (1280 x 720, PNG): Steve gliding
normally and holding Sneak, seen from behind and from the front.
"""

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

SHOTS = Path(sys.argv[1])
OUT = Path(sys.argv[2])
OUT.mkdir(parents=True, exist_ok=True)

FONT_FILES = [
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf",
]


def font(size):
    for file in FONT_FILES:
        if Path(file).exists():
            return ImageFont.truetype(file, size)
    return ImageFont.load_default(size)


LABEL = font(30)


def label(image, text, x, y, anchor="left"):
    """A white label with a shadow on a translucent black box at (x, y); anchor "right" puts its right edge at x."""
    draw = ImageDraw.Draw(image, "RGBA")
    width = round(draw.textlength(text, font=LABEL))
    pad = 10
    if anchor == "right":
        x -= width + 2 * pad
    draw.rectangle((x, y, x + width + 2 * pad, y + LABEL.size + 2 * pad), fill=(0, 0, 0, 150))
    draw.text((x + pad + 3, y + pad + 3), text, font=LABEL, fill=(63, 63, 63, 255))
    draw.text((x + pad, y + pad), text, font=LABEL, fill=(255, 255, 255, 255))


def load(name):
    return Image.open(SHOTS / f"{name}.png").convert("RGB")


def save(image, name):
    image.save(OUT / name, quality=88, optimize=True, progressive=True)
    print("wrote", name, image.size)


def side_by_side(left, right, left_text, right_text):
    """The middle halves of two pictures next to each other (the player is in the middle), with a white seam."""
    w, h = left.size
    quarter = w // 4
    both = Image.new("RGB", (w, h))
    both.paste(left.crop((quarter, 0, quarter + w // 2, h)), (0, 0))
    both.paste(right.crop((quarter, 0, quarter + w // 2, h)), (w // 2, 0))
    ImageDraw.Draw(both).rectangle((w // 2 - 2, 0, w // 2 + 1, h), fill=(255, 255, 255))
    label(both, left_text, w // 2 - 24, h - 80, anchor="right")
    label(both, right_text, w // 2 + 24, h - 80)
    return both


glide_back, glide_front = load("glide-back"), load("glide-front")
drag_back, drag_front = load("drag-back"), load("drag-front")

save(drag_front, "hero.jpg")
save(drag_back, "drag-back.jpg")
save(side_by_side(glide_back, drag_back, "Gliding", "Holding Sneak"), "glide-vs-drag-back.jpg")
save(side_by_side(glide_front, drag_front, "Gliding", "Holding Sneak"), "glide-vs-drag-front.jpg")
