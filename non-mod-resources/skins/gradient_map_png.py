from __future__ import annotations

import argparse
from pathlib import Path
from typing import Sequence

from PIL import Image, ImageColor, ImageOps


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Apply a multi-stop color gradient to a grayscale PNG. "
            "The first color maps to the darkest visible pixel in the image, "
            "and the last color maps to the lightest visible pixel."
        )
    )
    parser.add_argument("input_image", type=Path, help="Path to the source grayscale PNG")
    parser.add_argument("output_image", type=Path, help="Path for the recolored PNG")
    parser.add_argument(
        "colors",
        nargs="+",
        help="Gradient colors in order, such as #101820 #3465a4 #f4d35e or named colors",
    )
    return parser.parse_args()


def parse_colors(color_values: Sequence[str]) -> list[tuple[int, int, int]]:
    if len(color_values) < 2:
        raise ValueError("Provide at least two colors to build a gradient.")
    return [ImageColor.getrgb(color_value) for color_value in color_values]


def lerp_channel(start: int, end: int, amount: float) -> int:
    return round(start + (end - start) * amount)


def sample_gradient(colors: Sequence[tuple[int, int, int]], amount: float) -> tuple[int, int, int]:
    if amount <= 0:
        return colors[0]
    if amount >= 1:
        return colors[-1]

    scaled = amount * (len(colors) - 1)
    segment = min(int(scaled), len(colors) - 2)
    local_amount = scaled - segment
    start = colors[segment]
    end = colors[segment + 1]
    return tuple(lerp_channel(start[channel], end[channel], local_amount) for channel in range(3))


def visible_intensity_bounds(grayscale: Image.Image, alpha: Image.Image) -> tuple[int, int]:
    grayscale_data = grayscale.getdata()
    alpha_data = alpha.getdata()
    visible_values = [value for value, alpha_value in zip(grayscale_data, alpha_data) if alpha_value != 0]
    if not visible_values:
        raise ValueError("The input image has no visible pixels to recolor.")
    return min(visible_values), max(visible_values)


def colorize_loaded_image(source: Image.Image, colors: Sequence[tuple[int, int, int]]) -> Image.Image:
    rgba = source.convert("RGBA")
    grayscale = ImageOps.grayscale(rgba)
    alpha = rgba.getchannel("A")
    darkest, lightest = visible_intensity_bounds(grayscale, alpha)

    recolored = Image.new("RGBA", rgba.size)
    grayscale_data = list(grayscale.getdata())
    alpha_data = list(alpha.getdata())
    output_pixels: list[tuple[int, int, int, int]] = []

    for intensity, alpha_value in zip(grayscale_data, alpha_data):
        if alpha_value == 0:
            output_pixels.append((0, 0, 0, 0))
            continue

        if darkest == lightest:
            mapped = colors[0]
        else:
            amount = (intensity - darkest) / (lightest - darkest)
            mapped = sample_gradient(colors, amount)

        output_pixels.append((*mapped, alpha_value))

    recolored.putdata(output_pixels)
    return recolored


def colorize_image(input_path: Path, output_path: Path, colors: Sequence[tuple[int, int, int]]) -> None:
    with Image.open(input_path) as source:
        recolored = colorize_loaded_image(source, colors)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        recolored.save(output_path, format="PNG")


def main() -> int:
    args = parse_args()
    try:
        colors = parse_colors(args.colors)
        colorize_image(args.input_image, args.output_image, colors)
    except Exception as exc:  # noqa: BLE001 - CLI should surface the concrete failure.
        raise SystemExit(f"Error: {exc}") from exc

    print(f"Saved recolored image to {args.output_image}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())