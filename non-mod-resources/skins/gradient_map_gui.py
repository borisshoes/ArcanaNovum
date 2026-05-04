from __future__ import annotations

import colorsys
import json
import tkinter as tk
from functools import lru_cache
from pathlib import Path
from tkinter import filedialog, messagebox, ttk

from PIL import Image, ImageColor, ImageTk

from gradient_map_png import colorize_loaded_image, parse_colors

DEFAULT_COLORS = ["#101820", "#3465A4", "#F4D35E", "#EAE2B7"]
CHECKER_LIGHT = (240, 238, 232, 255)
CHECKER_DARK = (220, 216, 206, 255)
GRADIENT_PREVIEW_WIDTH = 28
GRADIENT_PREVIEW_MIN_HEIGHT = 220
MIN_ZOOM = 0.25
MAX_ZOOM = 64.0
ZOOM_STEP = 1.25
ZOOM_RENDER_DELAY_MS = 30
GRADIENT_RENDER_DELAY_MS = 180
COLOR_PICKER_SIZE = 220
HUE_STRIP_HEIGHT = 18


def clamp(value: float, minimum: float, maximum: float) -> float:
    return max(minimum, min(value, maximum))


def rgb_to_hex(color: tuple[int, int, int]) -> str:
    return f"#{color[0]:02X}{color[1]:02X}{color[2]:02X}"


def parse_single_color(color_value: str) -> tuple[int, int, int]:
    return ImageColor.getrgb(color_value.strip())


def midpoint_color(first: str, second: str | None) -> str:
    try:
        start = parse_single_color(first)
        end = parse_single_color(second) if second else start
    except ValueError:
        return "#FFFFFF"
    return rgb_to_hex(tuple(round((start[channel] + end[channel]) / 2) for channel in range(3)))


@lru_cache(maxsize=64)
def _checkerboard_base(size: tuple[int, int], square_size: int = 16) -> Image.Image:
    board = Image.new("RGBA", size, CHECKER_LIGHT)
    pixels = board.load()
    width, height = size
    for y in range(height):
        for x in range(width):
            if ((x // square_size) + (y // square_size)) % 2:
                pixels[x, y] = CHECKER_DARK
    return board


def build_checkerboard(size: tuple[int, int], square_size: int = 16) -> Image.Image:
    return _checkerboard_base(size, square_size).copy()


def build_zoomed_preview(image: Image.Image, zoom: float) -> Image.Image:
    rgba = image.convert("RGBA")
    target_size = (
        max(1, round(rgba.width * zoom)),
        max(1, round(rgba.height * zoom)),
    )

    if target_size == rgba.size:
        scaled = rgba
    else:
        resample = Image.Resampling.NEAREST if zoom >= 1 else Image.Resampling.LANCZOS
        scaled = rgba.resize(target_size, resample)

    background = build_checkerboard(target_size)
    background.paste(scaled, (0, 0), scaled)
    return background


def build_gradient_preview(
    colors: list[tuple[int, int, int]],
    size: tuple[int, int],
    *,
    vertical: bool = False,
) -> Image.Image:
    width, height = size
    preview = Image.new("RGBA", size)
    segment_count = len(colors) - 1

    if vertical:
        gradient_pixels: list[tuple[int, int, int, int]] = []
        last_index = max(height - 1, 1)
        for y in range(height):
            amount = y / last_index
            scaled = amount * segment_count
            segment = min(int(scaled), segment_count - 1)
            local_amount = scaled - segment
            start = colors[segment]
            end = colors[segment + 1]
            mapped = tuple(round(start[channel] + (end[channel] - start[channel]) * local_amount) for channel in range(3))
            gradient_pixels.extend([(*mapped, 255)] * width)
        preview.putdata(gradient_pixels)
        return preview

    gradient_row: list[tuple[int, int, int, int]] = []
    last_index = max(width - 1, 1)
    for x in range(width):
        amount = x / last_index
        scaled = amount * segment_count
        segment = min(int(scaled), segment_count - 1)
        local_amount = scaled - segment
        start = colors[segment]
        end = colors[segment + 1]
        mapped = tuple(round(start[channel] + (end[channel] - start[channel]) * local_amount) for channel in range(3))
        gradient_row.append((*mapped, 255))

    preview.putdata(gradient_row * height)
    return preview


class ColorPickerDialog(tk.Toplevel):
    def __init__(self, parent: tk.Misc, initial_color: str) -> None:
        super().__init__(parent)
        self.title("Choose Color")
        self.resizable(False, False)
        self.transient(parent)
        self.result: str | None = None
        self.sv_photo: ImageTk.PhotoImage | None = None
        self.hue_photo: ImageTk.PhotoImage | None = None
        self.updating_entry = False

        try:
            red, green, blue = parse_single_color(initial_color)
        except ValueError:
            red, green, blue = (255, 255, 255)
        self.hue, self.saturation, self.value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
        if self.saturation == 0:
            self.hue = 0.0

        self.hex_var = tk.StringVar()
        self._build_ui()
        self._refresh_all()
        self.grab_set()
        self.wait_visibility()

    def _build_ui(self) -> None:
        frame = ttk.Frame(self, padding=14)
        frame.grid(sticky="nsew")

        self.sv_canvas = tk.Canvas(frame, width=COLOR_PICKER_SIZE, height=COLOR_PICKER_SIZE, highlightthickness=1)
        self.sv_canvas.grid(row=0, column=0, columnspan=3, sticky="ew")
        self.sv_canvas.bind("<Button-1>", self._pick_sv)
        self.sv_canvas.bind("<B1-Motion>", self._pick_sv)

        self.hue_canvas = tk.Canvas(frame, width=COLOR_PICKER_SIZE, height=HUE_STRIP_HEIGHT, highlightthickness=1)
        self.hue_canvas.grid(row=1, column=0, columnspan=3, sticky="ew", pady=(10, 12))
        self.hue_canvas.bind("<Button-1>", self._pick_hue)
        self.hue_canvas.bind("<B1-Motion>", self._pick_hue)

        self.preview = tk.Label(frame, width=4, relief="sunken")
        self.preview.grid(row=2, column=0, sticky="w", padx=(0, 8))

        entry = ttk.Entry(frame, textvariable=self.hex_var, width=12)
        entry.grid(row=2, column=1, sticky="ew")
        entry.bind("<Return>", self._apply_hex_entry)
        entry.bind("<FocusOut>", self._apply_hex_entry)

        ttk.Button(frame, text="OK", command=self._accept).grid(row=3, column=1, sticky="e", pady=(14, 0), padx=(0, 8))
        ttk.Button(frame, text="Cancel", command=self._cancel).grid(row=3, column=2, sticky="e", pady=(14, 0))

    def _build_sv_image(self) -> Image.Image:
        image = Image.new("RGBA", (COLOR_PICKER_SIZE, COLOR_PICKER_SIZE))
        pixels: list[tuple[int, int, int, int]] = []
        last_index = COLOR_PICKER_SIZE - 1
        for y in range(COLOR_PICKER_SIZE):
            value = 1 - (y / last_index)
            for x in range(COLOR_PICKER_SIZE):
                saturation = x / last_index
                red, green, blue = colorsys.hsv_to_rgb(self.hue, saturation, value)
                pixels.append((round(red * 255), round(green * 255), round(blue * 255), 255))
        image.putdata(pixels)
        return image

    def _build_hue_image(self) -> Image.Image:
        image = Image.new("RGBA", (COLOR_PICKER_SIZE, HUE_STRIP_HEIGHT))
        pixels: list[tuple[int, int, int, int]] = []
        last_index = COLOR_PICKER_SIZE - 1
        for _y in range(HUE_STRIP_HEIGHT):
            for x in range(COLOR_PICKER_SIZE):
                red, green, blue = colorsys.hsv_to_rgb(x / last_index, 1, 1)
                pixels.append((round(red * 255), round(green * 255), round(blue * 255), 255))
        image.putdata(pixels)
        return image

    def _current_hex(self) -> str:
        red, green, blue = colorsys.hsv_to_rgb(self.hue, self.saturation, self.value)
        return rgb_to_hex((round(red * 255), round(green * 255), round(blue * 255)))

    def _refresh_all(self) -> None:
        self.sv_photo = ImageTk.PhotoImage(self._build_sv_image())
        self.sv_canvas.delete("all")
        self.sv_canvas.create_image(0, 0, image=self.sv_photo, anchor="nw")
        marker_x = self.saturation * (COLOR_PICKER_SIZE - 1)
        marker_y = (1 - self.value) * (COLOR_PICKER_SIZE - 1)
        self.sv_canvas.create_oval(marker_x - 5, marker_y - 5, marker_x + 5, marker_y + 5, outline="white", width=2)
        self.sv_canvas.create_oval(marker_x - 6, marker_y - 6, marker_x + 6, marker_y + 6, outline="black", width=1)

        self.hue_photo = ImageTk.PhotoImage(self._build_hue_image())
        self.hue_canvas.delete("all")
        self.hue_canvas.create_image(0, 0, image=self.hue_photo, anchor="nw")
        hue_x = self.hue * (COLOR_PICKER_SIZE - 1)
        self.hue_canvas.create_line(hue_x, 0, hue_x, HUE_STRIP_HEIGHT, fill="white", width=3)
        self.hue_canvas.create_line(hue_x, 0, hue_x, HUE_STRIP_HEIGHT, fill="black", width=1)

        hex_color = self._current_hex()
        self.preview.configure(bg=hex_color)
        self.updating_entry = True
        self.hex_var.set(hex_color)
        self.updating_entry = False

    def _pick_sv(self, event: tk.Event) -> str:
        last_index = COLOR_PICKER_SIZE - 1
        self.saturation = clamp(event.x / last_index, 0.0, 1.0)
        self.value = clamp(1 - (event.y / last_index), 0.0, 1.0)
        self._refresh_all()
        return "break"

    def _pick_hue(self, event: tk.Event) -> str:
        self.hue = clamp(event.x / (COLOR_PICKER_SIZE - 1), 0.0, 1.0)
        self._refresh_all()
        return "break"

    def _apply_hex_entry(self, _event: tk.Event | None = None) -> str:
        if self.updating_entry:
            return "break"
        try:
            red, green, blue = parse_single_color(self.hex_var.get())
        except ValueError:
            return "break"
        self.hue, self.saturation, self.value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
        self._refresh_all()
        return "break"

    def _accept(self) -> None:
        self._apply_hex_entry()
        self.result = self._current_hex()
        self.destroy()

    def _cancel(self) -> None:
        self.result = None
        self.destroy()


class GradientMapGui(tk.Tk):
    def __init__(self) -> None:
        super().__init__()
        self.title("Gradient PNG Mapper")
        self.geometry("1320x860")
        self.minsize(1080, 760)

        self.input_path: Path | None = None
        self.source_image: Image.Image | None = None
        self.output_image: Image.Image | None = None
        self.preview_job: str | None = None
        self.canvas_job: str | None = None
        self.zoom_job: str | None = None
        self.color_vars: list[tk.StringVar] = []
        self.color_entries: list[ttk.Entry] = []
        self.swatch_buttons: list[tk.Button] = []
        self.image_refs: dict[str, ImageTk.PhotoImage] = {}
        self.preview_cache: dict[tuple[str, int], ImageTk.PhotoImage] = {}
        self.last_valid_colors = parse_colors(DEFAULT_COLORS)
        self.zoom_value = 1.0
        self.fit_mode = True
        self.pan_anchor: tuple[int, int] | None = None
        self.pan_start_center: tuple[float, float] | None = None

        self.status_var = tk.StringVar(value="Load a grayscale PNG to start.")
        self.path_var = tk.StringVar(value="No image loaded")
        self.zoom_var = tk.StringVar(value="Zoom 100%")

        self._build_ui()
        for color in DEFAULT_COLORS:
            self._create_color_var(color)
        self._rebuild_color_editor()
        self._update_gradient_preview(self.last_valid_colors)
        self._render_previews(keep_center=False)

    def _build_ui(self) -> None:
        self.columnconfigure(0, weight=1)
        self.rowconfigure(0, weight=1)

        root = ttk.Frame(self, padding=16)
        root.grid(sticky="nsew")
        root.columnconfigure(1, weight=1)
        root.rowconfigure(1, weight=1)

        toolbar = ttk.Frame(root)
        toolbar.grid(row=0, column=0, columnspan=2, sticky="ew", pady=(0, 12))
        toolbar.columnconfigure(3, weight=1)

        ttk.Button(toolbar, text="Open PNG", command=self.open_image).grid(row=0, column=0, padx=(0, 8))
        ttk.Button(toolbar, text="Save Output", command=self.save_output).grid(row=0, column=1, padx=(0, 8))
        ttk.Button(toolbar, text="Refresh Preview", command=self.refresh_preview).grid(row=0, column=2)
        ttk.Label(toolbar, textvariable=self.path_var).grid(row=0, column=3, sticky="ew", padx=(16, 0))

        controls = ttk.LabelFrame(root, text="Gradient Colors", padding=12)
        controls.grid(row=1, column=0, sticky="ns", padx=(0, 12))
        controls.columnconfigure(0, weight=1)
        controls.rowconfigure(1, weight=1)

        ttk.Label(
            controls,
            text="Edit stops on the right. Use the swatch to pick a color.",
            wraplength=250,
            justify="left",
        ).grid(row=0, column=0, sticky="ew")

        editor = ttk.Frame(controls)
        editor.grid(row=1, column=0, sticky="nsew", pady=(10, 0))
        editor.columnconfigure(1, weight=1)

        gradient_frame = ttk.Frame(editor)
        gradient_frame.grid(row=0, column=0, sticky="ns", padx=(0, 10))
        ttk.Label(gradient_frame, text="Gradient").grid(row=0, column=0, sticky="w", pady=(0, 6))
        self.gradient_preview_label = ttk.Label(gradient_frame)
        self.gradient_preview_label.grid(row=1, column=0, sticky="ns")

        selector_frame = ttk.Frame(editor)
        selector_frame.grid(row=0, column=1, sticky="nsew")
        selector_frame.columnconfigure(0, weight=1)

        self.color_rows = ttk.Frame(selector_frame)
        self.color_rows.grid(row=0, column=0, sticky="nsew")
        self.color_rows.columnconfigure(1, weight=1)

        action_row = ttk.Frame(selector_frame)
        action_row.grid(row=1, column=0, sticky="ew", pady=(8, 0))
        ttk.Button(action_row, text="+ End", command=self.add_color_stop).grid(row=0, column=0, padx=(0, 6))
        ttk.Button(action_row, text="Import", command=self.import_gradient).grid(row=0, column=1, padx=(0, 6))
        ttk.Button(action_row, text="Export", command=self.export_gradient).grid(row=0, column=2)

        preview_panel = ttk.Frame(root)
        preview_panel.grid(row=1, column=1, sticky="nsew")
        preview_panel.columnconfigure(0, weight=1, uniform="preview")
        preview_panel.columnconfigure(1, weight=1, uniform="preview")
        preview_panel.rowconfigure(1, weight=1)

        zoom_toolbar = ttk.Frame(preview_panel)
        zoom_toolbar.grid(row=0, column=0, columnspan=3, sticky="ew", pady=(0, 8))
        zoom_toolbar.columnconfigure(4, weight=1)

        ttk.Button(zoom_toolbar, text="-", width=3, command=self.zoom_out).grid(row=0, column=0, padx=(0, 4))
        ttk.Button(zoom_toolbar, text="Fit", width=4, command=self.fit_to_window).grid(row=0, column=1, padx=4)
        ttk.Button(zoom_toolbar, text="+", width=3, command=self.zoom_in).grid(row=0, column=2, padx=4)
        ttk.Label(zoom_toolbar, textvariable=self.zoom_var).grid(row=0, column=3, padx=(8, 16))
        ttk.Label(zoom_toolbar, text="Mouse wheel zooms both previews. Drag either preview to pan.").grid(
            row=0,
            column=4,
            sticky="w",
        )

        template_frame = ttk.LabelFrame(preview_panel, text="Template Image", padding=8)
        template_frame.grid(row=1, column=0, sticky="nsew", padx=(0, 8))
        template_frame.columnconfigure(0, weight=1)
        template_frame.rowconfigure(0, weight=1)
        self.template_canvas = self._create_preview_canvas(template_frame)
        self.template_canvas.grid(row=0, column=0, sticky="nsew")

        output_frame = ttk.LabelFrame(preview_panel, text="Output Image", padding=8)
        output_frame.grid(row=1, column=1, sticky="nsew", padx=(8, 0))
        output_frame.columnconfigure(0, weight=1)
        output_frame.rowconfigure(0, weight=1)
        self.output_canvas = self._create_preview_canvas(output_frame)
        self.output_canvas.grid(row=0, column=0, sticky="nsew")

        self.vertical_scrollbar = ttk.Scrollbar(preview_panel, orient="vertical", command=self._scroll_vertical)
        self.vertical_scrollbar.grid(row=1, column=2, sticky="ns", padx=(8, 0))
        self.horizontal_scrollbar = ttk.Scrollbar(preview_panel, orient="horizontal", command=self._scroll_horizontal)
        self.horizontal_scrollbar.grid(row=2, column=0, columnspan=2, sticky="ew", pady=(8, 0))

        status = ttk.Label(root, textvariable=self.status_var, anchor="w")
        status.grid(row=2, column=0, columnspan=2, sticky="ew", pady=(12, 0))

    def _create_preview_canvas(self, parent: ttk.LabelFrame) -> tk.Canvas:
        canvas = tk.Canvas(
            parent,
            background="#ece8df",
            highlightthickness=0,
            cursor="fleur",
            xscrollcommand=self._set_horizontal_scroll,
            yscrollcommand=self._set_vertical_scroll,
        )
        canvas.bind("<Configure>", self._handle_canvas_resize)
        canvas.bind("<MouseWheel>", self._handle_mousewheel_zoom)
        canvas.bind("<ButtonPress-1>", self._start_pan)
        canvas.bind("<B1-Motion>", self._pan_previews)
        canvas.bind("<ButtonRelease-1>", self._end_pan)
        canvas.bind("<Double-Button-1>", self._handle_fit_shortcut)
        return canvas

    def _make_color_var(self, value: str) -> tk.StringVar:
        color_var = tk.StringVar(value=value)
        color_var.trace_add("write", self._handle_color_change)
        return color_var

    def _create_color_var(self, value: str) -> None:
        self.color_vars.append(self._make_color_var(value))

    def _handle_color_change(self, *_args: object) -> None:
        self._sync_swatch_buttons()
        self.schedule_preview_refresh()

    def _rebuild_color_editor(self) -> None:
        for child in self.color_rows.winfo_children():
            child.destroy()

        self.swatch_buttons = []
        self.color_entries = []
        for index, color_var in enumerate(self.color_vars):
            row = ttk.Frame(self.color_rows)
            row.grid(row=index, column=0, sticky="ew", pady=2)
            row.columnconfigure(1, weight=1)

            swatch = tk.Button(row, width=2, relief="raised", command=lambda idx=index: self.pick_color(idx))
            swatch.grid(row=0, column=0, padx=(0, 6))
            self.swatch_buttons.append(swatch)

            entry = ttk.Entry(row, textvariable=color_var, width=12)
            entry.grid(row=0, column=1, sticky="ew")
            self.color_entries.append(entry)

            ttk.Button(row, text="+", width=2, command=lambda idx=index: self.insert_color_stop_after(idx)).grid(
                row=0,
                column=2,
                padx=(6, 2),
            )

            ttk.Button(row, text="↑", width=2, command=lambda idx=index: self.move_stop(idx, -1)).grid(
                row=0,
                column=3,
                padx=2,
            )
            ttk.Button(row, text="↓", width=2, command=lambda idx=index: self.move_stop(idx, 1)).grid(
                row=0,
                column=4,
                padx=2,
            )
            ttk.Button(row, text="X", width=2, command=lambda idx=index: self.remove_stop(idx)).grid(
                row=0,
                column=5,
                padx=(2, 0),
            )

        self._sync_swatch_buttons()

    def _sync_swatch_buttons(self) -> None:
        for button, entry, color_var in zip(self.swatch_buttons, self.color_entries, self.color_vars):
            color_value = color_var.get().strip()
            try:
                normalized = rgb_to_hex(parse_single_color(color_value))
                button.configure(bg=normalized, activebackground=normalized, highlightbackground=normalized)
                try:
                    entry.configure(foreground="#111111")
                except tk.TclError:
                    pass
            except tk.TclError:
                button.configure(bg="#c7c7c7", activebackground="#c7c7c7", highlightbackground="#c7c7c7")
            except ValueError:
                button.configure(bg="#d9b4b4", activebackground="#d9b4b4", highlightbackground="#d9b4b4")
                try:
                    entry.configure(foreground="#9c1c1c")
                except tk.TclError:
                    pass

    def current_color_strings(self) -> list[str]:
        return [color_var.get().strip() for color_var in self.color_vars]

    def current_colors(self) -> list[tuple[int, int, int]]:
        return parse_colors(self.current_color_strings())

    def validated_current_colors(self) -> list[tuple[int, int, int]] | None:
        try:
            colors = self.current_colors()
        except Exception as exc:  # noqa: BLE001 - keep partial color edits non-disruptive.
            self._sync_swatch_buttons()
            self.status_var.set(f"Invalid gradient: {exc}")
            return None
        self.last_valid_colors = colors
        return colors

    def add_color_stop(self) -> None:
        seed = self.color_vars[-1].get() if self.color_vars else "#ffffff"
        self._create_color_var(seed)
        self._rebuild_color_editor()
        self.schedule_preview_refresh()

    def insert_color_stop_after(self, index: int) -> None:
        next_color = self.color_vars[index + 1].get() if index + 1 < len(self.color_vars) else None
        seed = midpoint_color(self.color_vars[index].get(), next_color)
        self.color_vars.insert(index + 1, self._make_color_var(seed))
        self._rebuild_color_editor()
        self.schedule_preview_refresh()

    def remove_stop(self, index: int) -> None:
        if len(self.color_vars) <= 2:
            messagebox.showerror("Gradient Colors", "Keep at least two color stops in the gradient.")
            return
        self.color_vars.pop(index)
        self._rebuild_color_editor()
        self.schedule_preview_refresh()

    def move_stop(self, index: int, direction: int) -> None:
        target = index + direction
        if not 0 <= target < len(self.color_vars):
            return
        self.color_vars[index], self.color_vars[target] = self.color_vars[target], self.color_vars[index]
        self._rebuild_color_editor()
        self.schedule_preview_refresh()

    def pick_color(self, index: int) -> None:
        current = self.color_vars[index].get().strip() or "#ffffff"
        dialog = ColorPickerDialog(self, current)
        self.wait_window(dialog)
        if dialog.result is None:
            return
        self.color_vars[index].set(dialog.result)

    def _load_image_from_path(self, path: Path) -> bool:
        try:
            with Image.open(path) as source:
                loaded = source.convert("RGBA")
        except Exception as exc:  # noqa: BLE001 - GUI should report the concrete failure.
            messagebox.showerror("Open PNG", f"Could not open image.\n\n{exc}")
            return False

        self.source_image = loaded
        self.output_image = None
        self.input_path = path
        self.path_var.set(str(path))
        self.fit_mode = True
        self._clear_preview_cache()
        self._cancel_pending_zoom_render()
        self.status_var.set(f"Loaded {path.name}. Generating preview...")
        self.refresh_preview()
        self.after_idle(self.fit_to_window)
        return True

    def open_image(self) -> None:
        selected = filedialog.askopenfilename(
            parent=self,
            title="Select grayscale PNG",
            filetypes=[("PNG images", "*.png"), ("All files", "*.*")],
        )
        if not selected:
            return
        self._load_image_from_path(Path(selected))

    def save_output(self) -> None:
        if self.output_image is None:
            messagebox.showerror("Save Output", "Generate an output preview before saving.")
            return

        default_name = "output.png"
        if self.input_path is not None:
            default_name = f"{self.input_path.stem}_gradient.png"

        target = filedialog.asksaveasfilename(
            parent=self,
            title="Save recolored PNG",
            defaultextension=".png",
            initialfile=default_name,
            filetypes=[("PNG images", "*.png")],
        )
        if not target:
            return

        try:
            self.output_image.save(target, format="PNG")
        except Exception as exc:  # noqa: BLE001 - GUI should report the concrete failure.
            messagebox.showerror("Save Output", f"Could not save output image.\n\n{exc}")
            return

        self.status_var.set(f"Saved recolored image to {target}")

    def export_gradient(self) -> None:
        colors = self.validated_current_colors()
        if colors is None:
            messagebox.showerror("Export Gradient", "Fix the gradient colors before exporting.")
            return

        target = filedialog.asksaveasfilename(
            parent=self,
            title="Export gradient",
            defaultextension=".json",
            initialfile="gradient.json",
            filetypes=[("Gradient JSON", "*.json"), ("All files", "*.*")],
        )
        if not target:
            return

        payload = {"colors": [rgb_to_hex(color) for color in colors]}
        try:
            Path(target).write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
        except OSError as exc:
            messagebox.showerror("Export Gradient", f"Could not export gradient.\n\n{exc}")
            return
        self.status_var.set(f"Exported gradient to {target}")

    def import_gradient(self) -> None:
        selected = filedialog.askopenfilename(
            parent=self,
            title="Import gradient",
            filetypes=[("Gradient files", "*.json *.txt"), ("All files", "*.*")],
        )
        if not selected:
            return

        path = Path(selected)
        try:
            color_values = self._read_gradient_file(path)
            self._replace_gradient_colors(color_values)
        except Exception as exc:  # noqa: BLE001 - GUI should report the concrete failure.
            messagebox.showerror("Import Gradient", f"Could not import gradient.\n\n{exc}")
            return
        self.status_var.set(f"Imported gradient from {path.name}")

    def _read_gradient_file(self, path: Path) -> list[str]:
        text = path.read_text(encoding="utf-8")
        try:
            data = json.loads(text)
        except json.JSONDecodeError:
            color_values = [part.strip() for line in text.splitlines() for part in line.split(",")]
        else:
            if isinstance(data, dict):
                data = data.get("colors")
            if not isinstance(data, list):
                raise ValueError("Gradient JSON must be a list of colors or an object with a colors list.")
            color_values = [str(value).strip() for value in data]

        color_values = [value for value in color_values if value]
        if len(color_values) < 2:
            raise ValueError("A gradient needs at least two colors.")
        parse_colors(color_values)
        return color_values

    def _replace_gradient_colors(self, color_values: list[str]) -> None:
        normalized = [rgb_to_hex(parse_single_color(value)) for value in color_values]
        self.color_vars = [self._make_color_var(value) for value in normalized]
        self.last_valid_colors = parse_colors(normalized)
        self._rebuild_color_editor()
        self._update_gradient_preview(self.last_valid_colors)
        self.schedule_preview_refresh()

    def schedule_preview_refresh(self) -> None:
        if self.preview_job is not None:
            self.after_cancel(self.preview_job)
        self.preview_job = self.after(GRADIENT_RENDER_DELAY_MS, self.refresh_preview)

    def refresh_preview(self) -> None:
        self.preview_job = None
        colors = self.validated_current_colors()
        if colors is None:
            return
        self._update_gradient_preview(colors)

        if self.source_image is None:
            self.output_image = None
            self._clear_preview_cache()
            self._render_previews(keep_center=False)
            self.status_var.set("Load a grayscale PNG to preview the output.")
            return

        try:
            self.output_image = colorize_loaded_image(self.source_image, colors)
            self._clear_preview_cache()
        except Exception as exc:  # noqa: BLE001 - GUI should report the concrete failure.
            self.output_image = None
            self._clear_preview_cache()
            self._render_previews(keep_center=True)
            self.status_var.set(f"Preview error: {exc}")
            return

        if self.fit_mode:
            self.fit_to_window()
        else:
            self._render_previews(keep_center=True)

        self.status_var.set(
            f"Preview updated for {self.source_image.width}x{self.source_image.height} image with {len(colors)} color stops."
        )

    def fit_to_window(self) -> None:
        self.canvas_job = None
        if self.source_image is None:
            return

        fit_zoom = self._calculate_fit_zoom()
        if fit_zoom is None:
            return

        self.fit_mode = True
        self.zoom_value = fit_zoom
        self._cancel_pending_zoom_render()
        self._render_previews(keep_center=False)

    def zoom_in(self) -> None:
        if self.source_image is None:
            return
        self._set_zoom(self.zoom_value * ZOOM_STEP)

    def zoom_out(self) -> None:
        if self.source_image is None:
            return
        self._set_zoom(self.zoom_value / ZOOM_STEP)

    def _set_zoom(self, new_zoom: float) -> None:
        self.fit_mode = False
        clamped_zoom = clamp(new_zoom, MIN_ZOOM, MAX_ZOOM)
        if abs(clamped_zoom - self.zoom_value) < 1e-9:
            self._update_zoom_label()
            return
        self.zoom_value = clamped_zoom
        self._update_zoom_label()
        self._schedule_zoom_render()

    def _schedule_zoom_render(self) -> None:
        if self.zoom_job is not None:
            self.after_cancel(self.zoom_job)
        self.zoom_job = self.after(ZOOM_RENDER_DELAY_MS, self._apply_zoom_render)

    def _apply_zoom_render(self) -> None:
        self.zoom_job = None
        self._render_previews(keep_center=True)

    def _cancel_pending_zoom_render(self) -> None:
        if self.zoom_job is not None:
            self.after_cancel(self.zoom_job)
            self.zoom_job = None

    def _calculate_fit_zoom(self) -> float | None:
        if self.source_image is None:
            return None

        canvas_width = min(self.template_canvas.winfo_width(), self.output_canvas.winfo_width())
        canvas_height = min(self.template_canvas.winfo_height(), self.output_canvas.winfo_height())
        if canvas_width < 2 or canvas_height < 2:
            return None

        width_ratio = canvas_width / self.source_image.width
        height_ratio = canvas_height / self.source_image.height
        return clamp(min(width_ratio, height_ratio), MIN_ZOOM, MAX_ZOOM)

    def _handle_canvas_resize(self, _event: tk.Event) -> None:
        if self.canvas_job is not None:
            self.after_cancel(self.canvas_job)
        action = self.fit_to_window if self.fit_mode else self._refresh_canvas_only
        self.canvas_job = self.after(80, action)

    def _refresh_canvas_only(self) -> None:
        self.canvas_job = None
        self._render_previews(keep_center=True)

    def _handle_mousewheel_zoom(self, event: tk.Event) -> str:
        if self.source_image is None or event.delta == 0:
            return "break"

        steps = max(1, abs(event.delta) // 120)
        factor = ZOOM_STEP ** steps
        if event.delta < 0:
            factor = 1 / factor
        self._set_zoom(self.zoom_value * factor)
        return "break"

    def _handle_fit_shortcut(self, _event: tk.Event) -> str:
        self.fit_to_window()
        return "break"

    def _start_pan(self, event: tk.Event) -> str:
        if self.source_image is None:
            return "break"
        self.pan_anchor = (event.x_root, event.y_root)
        self.pan_start_center = self._current_view_center()
        return "break"

    def _pan_previews(self, event: tk.Event) -> str:
        if self.source_image is None or self.pan_anchor is None or self.pan_start_center is None:
            return "break"
        scroll_width, scroll_height = self._shared_scrollregion_size()
        if scroll_width <= 0 or scroll_height <= 0:
            return "break"

        delta_x = event.x_root - self.pan_anchor[0]
        delta_y = event.y_root - self.pan_anchor[1]
        center_x = self.pan_start_center[0] - (delta_x / scroll_width)
        center_y = self.pan_start_center[1] - (delta_y / scroll_height)
        self._move_to_center(center_x, center_y)
        return "break"

    def _end_pan(self, _event: tk.Event) -> str:
        self.pan_anchor = None
        self.pan_start_center = None
        return "break"

    def _scroll_horizontal(self, *args: str) -> None:
        for canvas in (self.template_canvas, self.output_canvas):
            canvas.xview(*args)
        center_x, center_y = self._current_view_center()
        self._move_to_center(center_x, center_y)

    def _scroll_vertical(self, *args: str) -> None:
        for canvas in (self.template_canvas, self.output_canvas):
            canvas.yview(*args)
        center_x, center_y = self._current_view_center()
        self._move_to_center(center_x, center_y)

    def _set_horizontal_scroll(self, first: str, last: str) -> None:
        self.horizontal_scrollbar.set(first, last)

    def _set_vertical_scroll(self, first: str, last: str) -> None:
        self.vertical_scrollbar.set(first, last)

    def _clear_preview_cache(self) -> None:
        self.preview_cache.clear()
        self.image_refs.pop("template", None)
        self.image_refs.pop("output", None)

    def _shared_scrollregion_size(self) -> tuple[float, float]:
        scroll_width = 0.0
        scroll_height = 0.0
        for canvas in (self.template_canvas, self.output_canvas):
            scrollregion = canvas.cget("scrollregion")
            if not scrollregion:
                continue
            _, _, canvas_scroll_width, canvas_scroll_height = [float(value) for value in scrollregion.split()]
            scroll_width = max(scroll_width, canvas_scroll_width)
            scroll_height = max(scroll_height, canvas_scroll_height)
        return scroll_width, scroll_height

    def _render_previews(self, *, keep_center: bool) -> None:
        center_x, center_y = self._current_view_center() if keep_center else (0.5, 0.5)

        self._render_canvas_image(
            self.template_canvas,
            self.source_image,
            "template",
            empty_text="Open a PNG to preview the template.",
        )
        self._render_canvas_image(
            self.output_canvas,
            self.output_image,
            "output",
            empty_text="Output preview appears here.",
        )
        self._restore_view_center(center_x, center_y)
        self._update_zoom_label()

    def _render_canvas_image(
        self,
        canvas: tk.Canvas,
        image: Image.Image | None,
        key: str,
        *,
        empty_text: str,
    ) -> None:
        canvas.delete("all")
        canvas_width = max(canvas.winfo_width(), 1)
        canvas_height = max(canvas.winfo_height(), 1)

        if image is None:
            canvas.configure(scrollregion=(0, 0, canvas_width, canvas_height))
            canvas.create_text(
                canvas_width / 2,
                canvas_height / 2,
                text=empty_text,
                fill="#6a665e",
                font=("Segoe UI", 11),
                justify="center",
            )
            return

        cache_key = (key, round(self.zoom_value * 1000))
        photo = self.preview_cache.get(cache_key)
        if photo is None:
            preview = build_zoomed_preview(image, self.zoom_value)
            photo = ImageTk.PhotoImage(preview)
            self.preview_cache[cache_key] = photo
        preview_width = photo.width()
        preview_height = photo.height()
        scroll_width = max(preview_width, canvas_width)
        scroll_height = max(preview_height, canvas_height)
        offset_x = (scroll_width - preview_width) / 2
        offset_y = (scroll_height - preview_height) / 2

        self.image_refs[key] = photo
        canvas.create_image(offset_x, offset_y, image=photo, anchor="nw")
        canvas.configure(scrollregion=(0, 0, scroll_width, scroll_height))

    def _current_view_center(self) -> tuple[float, float]:
        try:
            x_first, x_last = self.template_canvas.xview()
            y_first, y_last = self.template_canvas.yview()
        except tk.TclError:
            return 0.5, 0.5
        return (x_first + x_last) / 2, (y_first + y_last) / 2

    def _restore_view_center(self, center_x: float, center_y: float) -> None:
        self._move_to_center(center_x, center_y)

    def _move_to_center(self, center_x: float, center_y: float) -> None:
        scrollregion = self.template_canvas.cget("scrollregion")
        if not scrollregion:
            return

        for canvas in (self.template_canvas, self.output_canvas):
            canvas_width = max(canvas.winfo_width(), 1)
            canvas_height = max(canvas.winfo_height(), 1)
            _, _, scroll_width, scroll_height = [float(value) for value in canvas.cget("scrollregion").split()]
            visible_x = 1.0 if scroll_width <= 0 else min(1.0, canvas_width / scroll_width)
            visible_y = 1.0 if scroll_height <= 0 else min(1.0, canvas_height / scroll_height)
            left = 0.0 if visible_x >= 1 else clamp(center_x - visible_x / 2, 0.0, 1.0 - visible_x)
            top = 0.0 if visible_y >= 1 else clamp(center_y - visible_y / 2, 0.0, 1.0 - visible_y)
            canvas.xview_moveto(left)
            canvas.yview_moveto(top)

    def _update_zoom_label(self) -> None:
        mode = "Fit" if self.fit_mode else "Zoom"
        self.zoom_var.set(f"{mode} {self.zoom_value * 100:.0f}%")

    def _update_gradient_preview(self, colors: list[tuple[int, int, int]] | None = None) -> None:
        if colors is None:
            colors = self.last_valid_colors
        preview_height = max(GRADIENT_PREVIEW_MIN_HEIGHT, len(self.color_vars) * 34)
        preview = build_gradient_preview(colors, (GRADIENT_PREVIEW_WIDTH, preview_height), vertical=True)
        photo = ImageTk.PhotoImage(preview)
        self.image_refs["gradient"] = photo
        self.gradient_preview_label.configure(image=photo, text="")


def main() -> int:
    app = GradientMapGui()
    app.mainloop()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())