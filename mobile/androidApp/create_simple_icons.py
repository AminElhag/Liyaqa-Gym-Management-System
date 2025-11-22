#!/usr/bin/env python3
"""
Create simple placeholder PNG icons without external dependencies
"""
import os
import struct
import zlib

def create_simple_png(width, height, color_rgb):
    """
    Create a simple solid color PNG file
    color_rgb: tuple of (r, g, b)
    """
    # PNG file signature
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr_chunk = b'IHDR' + ihdr_data
    ihdr_crc = struct.pack('>I', zlib.crc32(ihdr_chunk))
    ihdr = struct.pack('>I', len(ihdr_data)) + ihdr_chunk + ihdr_crc

    # IDAT chunk (image data)
    raw_data = b''
    r, g, b = color_rgb
    for y in range(height):
        raw_data += b'\x00'  # Filter type
        raw_data += (bytes([r, g, b]) * width)

    compressed_data = zlib.compress(raw_data, 9)
    idat_chunk = b'IDAT' + compressed_data
    idat_crc = struct.pack('>I', zlib.crc32(idat_chunk))
    idat = struct.pack('>I', len(compressed_data)) + idat_chunk + idat_crc

    # IEND chunk
    iend = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', zlib.crc32(b'IEND'))

    return png_signature + ihdr + idat + iend

def main():
    base_path = os.path.dirname(os.path.abspath(__file__))
    res_path = os.path.join(base_path, 'src', 'main', 'res')

    # Orange color for gym theme (FF6B35)
    color = (255, 107, 53)

    # Android icon sizes
    densities = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }

    print("Generating Android launcher icons (simple placeholders)...")

    for density, size in densities.items():
        mipmap_dir = os.path.join(res_path, f'mipmap-{density}')
        os.makedirs(mipmap_dir, exist_ok=True)

        # Create normal icon
        normal_png = os.path.join(mipmap_dir, 'ic_launcher.png')
        png_data = create_simple_png(size, size, color)
        with open(normal_png, 'wb') as f:
            f.write(png_data)
        print(f"✓ Created {density}/ic_launcher.png ({size}x{size})")

        # Create round icon
        round_png = os.path.join(mipmap_dir, 'ic_launcher_round.png')
        with open(round_png, 'wb') as f:
            f.write(png_data)
        print(f"✓ Created {density}/ic_launcher_round.png ({size}x{size})")

    print("\n✅ All Android launcher icons generated successfully!")
    print("\nNOTE: These are simple placeholder icons.")
    print("For better icons, convert the SVG files in src/main/res/ using:")
    print("  brew install librsvg")
    print("  rsvg-convert -w SIZE -h SIZE ic_launcher.svg -o output.png")
    return 0

if __name__ == '__main__':
    import sys
    sys.exit(main())