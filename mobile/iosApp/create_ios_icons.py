#!/usr/bin/env python3
"""
Create iOS app icons
"""
import os
import struct
import zlib
import json

def create_simple_png(width, height, color_rgb):
    """Create a simple solid color PNG file"""
    png_signature = b'\x89PNG\r\n\x1a\n'

    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr_chunk = b'IHDR' + ihdr_data
    ihdr_crc = struct.pack('>I', zlib.crc32(ihdr_chunk))
    ihdr = struct.pack('>I', len(ihdr_data)) + ihdr_chunk + ihdr_crc

    raw_data = b''
    r, g, b = color_rgb
    for y in range(height):
        raw_data += b'\x00'
        raw_data += (bytes([r, g, b]) * width)

    compressed_data = zlib.compress(raw_data, 9)
    idat_chunk = b'IDAT' + compressed_data
    idat_crc = struct.pack('>I', zlib.crc32(idat_chunk))
    idat = struct.pack('>I', len(compressed_data)) + idat_chunk + idat_crc

    iend = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', zlib.crc32(b'IEND'))

    return png_signature + ihdr + idat + iend

def main():
    base_path = os.path.dirname(os.path.abspath(__file__))
    assets_path = os.path.join(base_path, 'Liyaqa', 'Resources', 'Assets.xcassets', 'AppIcon.appiconset')

    # Orange color for gym theme
    color = (255, 107, 53)

    # iOS icon specifications
    ios_icons = [
        {"size": "20x20", "scale": "2x", "idiom": "iphone", "pixels": 40},
        {"size": "20x20", "scale": "3x", "idiom": "iphone", "pixels": 60},
        {"size": "29x29", "scale": "2x", "idiom": "iphone", "pixels": 58},
        {"size": "29x29", "scale": "3x", "idiom": "iphone", "pixels": 87},
        {"size": "40x40", "scale": "2x", "idiom": "iphone", "pixels": 80},
        {"size": "40x40", "scale": "3x", "idiom": "iphone", "pixels": 120},
        {"size": "60x60", "scale": "2x", "idiom": "iphone", "pixels": 120},
        {"size": "60x60", "scale": "3x", "idiom": "iphone", "pixels": 180},
        {"size": "20x20", "scale": "1x", "idiom": "ipad", "pixels": 20},
        {"size": "20x20", "scale": "2x", "idiom": "ipad", "pixels": 40},
        {"size": "29x29", "scale": "1x", "idiom": "ipad", "pixels": 29},
        {"size": "29x29", "scale": "2x", "idiom": "ipad", "pixels": 58},
        {"size": "40x40", "scale": "1x", "idiom": "ipad", "pixels": 40},
        {"size": "40x40", "scale": "2x", "idiom": "ipad", "pixels": 80},
        {"size": "76x76", "scale": "1x", "idiom": "ipad", "pixels": 76},
        {"size": "76x76", "scale": "2x", "idiom": "ipad", "pixels": 152},
        {"size": "83.5x83.5", "scale": "2x", "idiom": "ipad", "pixels": 167},
        {"size": "1024x1024", "scale": "1x", "idiom": "ios-marketing", "pixels": 1024},
    ]

    print("Generating iOS app icons...")

    contents = {
        "images": [],
        "info": {
            "author": "xcode",
            "version": 1
        }
    }

    for icon in ios_icons:
        filename = f"AppIcon-{icon['idiom']}-{icon['size']}-{icon['scale']}.png"
        filepath = os.path.join(assets_path, filename)

        # Create PNG
        size = icon['pixels']
        png_data = create_simple_png(size, size, color)
        with open(filepath, 'wb') as f:
            f.write(png_data)

        # Add to Contents.json
        entry = {
            "idiom": icon['idiom'],
            "scale": icon['scale'],
            "size": icon['size'],
            "filename": filename
        }
        contents["images"].append(entry)

        print(f"✓ Created {filename} ({size}x{size})")

    # Write Contents.json
    contents_file = os.path.join(assets_path, 'Contents.json')
    with open(contents_file, 'w') as f:
        json.dump(contents, f, indent=2)

    print("\n✅ All iOS app icons generated successfully!")
    print("\nNOTE: These are simple placeholder icons.")
    print("For better icons, use the SVG file and convert using design tools.")
    return 0

if __name__ == '__main__':
    import sys
    sys.exit(main())