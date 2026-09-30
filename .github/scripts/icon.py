#!/usr/bin/env python3
"""The mod icon: original pixel art of an elytra spread wide as an air brake, with wind streaks, on a sky tile.
No game textures. Needs Pillow.

    python3 .github/scripts/icon.py

Writes src/main/resources/assets/elytradrag/icon.png (128 x 128, a 32 x 32 grid scaled 4 times).
"""

from pathlib import Path

from PIL import Image, ImageDraw

GRID = 32
SCALE = 4
OUT = Path(__file__).resolve().parents[2] / "src/main/resources/assets/elytradrag/icon.png"

SKY_TOP = (84, 150, 230)
SKY_BOTTOM = (178, 218, 250)
FRAME = (28, 44, 70)
CLOUD = (250, 252, 255)
CLOUD_SHADE = (214, 228, 245)
STREAK = (255, 255, 255)

OUTLINE = (44, 38, 72)
MEMBRANE_LIGHT = (196, 186, 236)
MEMBRANE = (150, 138, 204)
MEMBRANE_DARK = (108, 96, 168)
VEIN = (80, 70, 132)
EDGE_HIGHLIGHT = (232, 226, 252)

# the left wing seen from behind, rolled outwards like the mod's air brake: from the shoulder at the top of the
# tile down to the tip at the bottom left, with a scalloped trailing edge (x, y in grid pixels)
LEFT_WING = [
    (15, 6), (12, 6), (9, 8), (6, 11), (4, 14), (2, 18), (1, 22), (2, 25),
    (4, 24), (5, 25), (7, 23), (8, 23), (9, 21), (10, 20), (11, 18), (12, 17), (13, 15), (14, 13), (15, 12),
]
# veins from the shoulder towards the trailing edge
VEINS = [((14, 7), (3, 21)), ((14, 8), (6, 22)), ((14, 9), (9, 19)), ((15, 10), (12, 15))]
LEADING_EDGE = ((13, 6), (1, 22))
TRAILING_EDGE = ((15, 12), (4, 25))
# the stiff leading edge catches the light
HIGHLIGHT = [(13, 7), (10, 8), (7, 11), (5, 14), (3, 18)]


def distance(point, line):
    """Distance of a point from the straight line through two points."""
    (x1, y1), (x2, y2) = line
    x, y = point
    return abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1) / ((y2 - y1) ** 2 + (x2 - x1) ** 2) ** 0.5


def lerp(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def rounded_mask(size, radius):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)
    return mask


def sky():
    img = Image.new("RGBA", (GRID, GRID), (0, 0, 0, 0))
    px = img.load()
    for y in range(GRID):
        colour = lerp(SKY_TOP, SKY_BOTTOM, y / (GRID - 1))
        for x in range(GRID):
            px[x, y] = colour + (255,)
    # clouds low on the tile
    for cx, cy, w in ((3, 27, 9), (21, 25, 8), (13, 29, 7)):
        for x in range(cx, min(GRID, cx + w)):
            px[x, cy] = CLOUD + (255,)
            px[x, cy + 1] = CLOUD_SHADE + (255,)
        for x in range(cx + 2, min(GRID, cx + w - 2)):
            px[x, cy - 1] = CLOUD + (255,)
    return img


def left_wing():
    """The left wing on its own transparent layer; the right one is its mirror image."""
    layer = Image.new("RGBA", (GRID, GRID), (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)
    draw.polygon(LEFT_WING, fill=MEMBRANE + (255,))
    px = layer.load()
    # light along the leading edge, darker only along the trailing edge
    for y in range(GRID):
        for x in range(GRID):
            if px[x, y][3]:
                lead = distance((x, y), LEADING_EDGE)
                trail = distance((x, y), TRAILING_EDGE)
                t = lead / (lead + trail) if lead + trail else 0
                if t < 0.3:
                    px[x, y] = MEMBRANE_LIGHT + (255,)
                elif t > 0.72:
                    px[x, y] = MEMBRANE_DARK + (255,)
    for start, end in VEINS:
        draw.line((start, end), fill=VEIN + (255,))
    draw.line(LEFT_WING + [LEFT_WING[0]], fill=OUTLINE + (255,))
    # the stiff leading edge catches the light
    draw.line(HIGHLIGHT, fill=EDGE_HIGHLIGHT + (255,))
    return layer


def streaks(img):
    draw = ImageDraw.Draw(img)
    # air rushing up past the braking wings
    for x, y1, y2 in ((15, 15, 20), (16, 18, 23), (13, 20, 23), (18, 20, 22)):
        draw.line(((x, y1), (x, y2)), fill=STREAK + (255,))


def main():
    img = sky()
    streaks(img)
    wing = left_wing()
    img.alpha_composite(wing)
    img.alpha_composite(wing.transpose(Image.Transpose.FLIP_LEFT_RIGHT))

    # a dark frame and rounded corners
    framed = Image.new("RGBA", (GRID, GRID), (0, 0, 0, 0))
    framed.paste(FRAME + (255,), (0, 0, GRID, GRID), rounded_mask(GRID, 5))
    framed.paste(img.crop((1, 1, GRID - 1, GRID - 1)), (1, 1), rounded_mask(GRID - 2, 4))

    big = framed.resize((GRID * SCALE, GRID * SCALE), Image.Resampling.NEAREST)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    big.save(OUT, optimize=True)
    print("wrote", OUT, big.size)


if __name__ == "__main__":
    main()
