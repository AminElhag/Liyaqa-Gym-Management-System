#!/usr/bin/env python3
"""
Create web app icons and favicon
"""
import os
import struct
import zlib

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

def create_ico(size, color_rgb):
    """Create a simple ICO file for favicon"""
    # Create PNG data
    png_data = create_simple_png(size, size, color_rgb)

    # ICO header
    ico_header = struct.pack('<HHH', 0, 1, 1)  # Reserved, Type (1=ICO), Count

    # ICONDIRENTRY
    bpp = 24
    png_size = len(png_data)
    ico_entry = struct.pack('<BBBBHHII',
                            size if size < 256 else 0,  # Width
                            size if size < 256 else 0,  # Height
                            0,  # Color palette
                            0,  # Reserved
                            1,  # Color planes
                            bpp,  # Bits per pixel
                            png_size,  # Size of image data
                            22)  # Offset to image data

    return ico_header + ico_entry + png_data

def main():
    base_path = os.path.dirname(os.path.abspath(__file__))
    public_path = os.path.join(base_path, 'public')
    os.makedirs(public_path, exist_ok=True)

    # Orange color for gym theme
    color = (255, 107, 53)

    # Web icon sizes
    web_icons = [
        {'name': 'favicon.ico', 'size': 32, 'is_ico': True},
        {'name': 'favicon-16x16.png', 'size': 16, 'is_ico': False},
        {'name': 'favicon-32x32.png', 'size': 32, 'is_ico': False},
        {'name': 'apple-touch-icon.png', 'size': 180, 'is_ico': False},
        {'name': 'android-chrome-192x192.png', 'size': 192, 'is_ico': False},
        {'name': 'android-chrome-512x512.png', 'size': 512, 'is_ico': False},
    ]

    print("Generating web app icons and favicon...")

    for icon in web_icons:
        filepath = os.path.join(public_path, icon['name'])

        if icon['is_ico']:
            # Create ICO file
            ico_data = create_ico(icon['size'], color)
            with open(filepath, 'wb') as f:
                f.write(ico_data)
        else:
            # Create PNG file
            png_data = create_simple_png(icon['size'], icon['size'], color)
            with open(filepath, 'wb') as f:
                f.write(png_data)

        print(f"✓ Created {icon['name']} ({icon['size']}x{icon['size']})")

    # Create web manifest
    manifest = {
        "name": "Liyaqa Gym Manager",
        "short_name": "Liyaqa",
        "description": "Comprehensive gym management system",
        "icons": [
            {
                "src": "/android-chrome-192x192.png",
                "sizes": "192x192",
                "type": "image/png"
            },
            {
                "src": "/android-chrome-512x512.png",
                "sizes": "512x512",
                "type": "image/png"
            }
        ],
        "theme_color": "#FF6B35",
        "background_color": "#FFFFFF",
        "display": "standalone",
        "start_url": "/"
    }

    import json
    manifest_file = os.path.join(public_path, 'site.webmanifest')
    with open(manifest_file, 'w') as f:
        json.dump(manifest, f, indent=2)
    print(f"✓ Created site.webmanifest")

    print("\n✅ All web icons generated successfully!")
    print("\nAdd these to your HTML <head>:")
    print('<link rel="icon" type="image/x-icon" href="/favicon.ico">')
    print('<link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png">')
    print('<link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png">')
    print('<link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">')
    print('<link rel="manifest" href="/site.webmanifest">')
    return 0

if __name__ == '__main__':
    import sys
    sys.exit(main())