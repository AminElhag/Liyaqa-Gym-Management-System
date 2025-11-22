#!/usr/bin/env python3
"""
Generate launcher icons for Liyaqa Gym Management System
Creates simple gym-themed icons using available system tools
"""
import os
import subprocess
import sys

def create_svg_icon():
    """Create an SVG icon for the Liyaqa Gym app"""
    svg_content = '''<?xml version="1.0" encoding="UTF-8"?>
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <!-- Background -->
  <rect width="512" height="512" fill="#FF6B35" rx="80"/>

  <!-- Dumbbell Icon (Gym Theme) -->
  <g transform="translate(128, 200)">
    <!-- Left weight plate -->
    <rect x="0" y="20" width="40" height="80" fill="#FFFFFF" rx="8"/>
    <rect x="8" y="28" width="24" height="64" fill="#E8E8E8" rx="4"/>

    <!-- Bar -->
    <rect x="40" y="50" width="176" height="20" fill="#FFFFFF" rx="4"/>

    <!-- Center grip -->
    <rect x="100" y="45" width="56" height="30" fill="#E8E8E8" rx="6"/>
    <line x1="120" y1="45" x2="120" y2="75" stroke="#CCCCCC" stroke-width="2"/>
    <line x1="136" y1="45" x2="136" y2="75" stroke="#CCCCCC" stroke-width="2"/>

    <!-- Right weight plate -->
    <rect x="216" y="20" width="40" height="80" fill="#FFFFFF" rx="8"/>
    <rect x="224" y="28" width="24" height="64" fill="#E8E8E8" rx="4"/>
  </g>

  <!-- App Name -->
  <text x="256" y="390" font-family="Arial, sans-serif" font-size="56" font-weight="bold" fill="#FFFFFF" text-anchor="middle">LIYAQA</text>
  <text x="256" y="440" font-family="Arial, sans-serif" font-size="28" fill="#FFE8E0" text-anchor="middle">Gym Manager</text>
</svg>'''
    return svg_content

def create_svg_icon_round():
    """Create a round SVG icon variant"""
    svg_content = '''<?xml version="1.0" encoding="UTF-8"?>
<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <!-- Background circle -->
  <circle cx="256" cy="256" r="256" fill="#FF6B35"/>

  <!-- Dumbbell Icon (Gym Theme) -->
  <g transform="translate(128, 200)">
    <!-- Left weight plate -->
    <rect x="0" y="20" width="40" height="80" fill="#FFFFFF" rx="8"/>
    <rect x="8" y="28" width="24" height="64" fill="#E8E8E8" rx="4"/>

    <!-- Bar -->
    <rect x="40" y="50" width="176" height="20" fill="#FFFFFF" rx="4"/>

    <!-- Center grip -->
    <rect x="100" y="45" width="56" height="30" fill="#E8E8E8" rx="6"/>
    <line x1="120" y1="45" x2="120" y2="75" stroke="#CCCCCC" stroke-width="2"/>
    <line x1="136" y1="45" x2="136" y2="75" stroke="#CCCCCC" stroke-width="2"/>

    <!-- Right weight plate -->
    <rect x="216" y="20" width="40" height="80" fill="#FFFFFF" rx="8"/>
    <rect x="224" y="28" width="24" height="64" fill="#E8E8E8" rx="4"/>
  </g>

  <!-- App Name -->
  <text x="256" y="390" font-family="Arial, sans-serif" font-size="56" font-weight="bold" fill="#FFFFFF" text-anchor="middle">LIYAQA</text>
  <text x="256" y="440" font-family="Arial, sans-serif" font-size="28" fill="#FFE8E0" text-anchor="middle">Gym Manager</text>
</svg>'''
    return svg_content

def convert_svg_to_png(svg_file, png_file, size):
    """Convert SVG to PNG using system tools"""
    try:
        # Try using qlmanage (macOS)
        result = subprocess.run(
            ['qlmanage', '-t', '-s', str(size), '-o', os.path.dirname(png_file), svg_file],
            capture_output=True,
            text=True
        )
        if result.returncode == 0:
            # qlmanage creates filename.svg.png, need to rename
            temp_png = svg_file + '.png'
            if os.path.exists(temp_png):
                os.rename(temp_png, png_file)
                return True
    except FileNotFoundError:
        pass

    try:
        # Try using rsvg-convert (if installed via brew)
        subprocess.run(
            ['rsvg-convert', '-w', str(size), '-h', str(size), svg_file, '-o', png_file],
            check=True,
            capture_output=True
        )
        return True
    except (FileNotFoundError, subprocess.CalledProcessError):
        pass

    try:
        # Try using ImageMagick convert
        subprocess.run(
            ['convert', '-resize', f'{size}x{size}', '-background', 'none', svg_file, png_file],
            check=True,
            capture_output=True
        )
        return True
    except (FileNotFoundError, subprocess.CalledProcessError):
        pass

    return False

def main():
    base_path = os.path.dirname(os.path.abspath(__file__))
    res_path = os.path.join(base_path, 'src', 'main', 'res')

    # Create temporary SVG files
    svg_normal = os.path.join(base_path, 'ic_launcher.svg')
    svg_round = os.path.join(base_path, 'ic_launcher_round.svg')

    with open(svg_normal, 'w') as f:
        f.write(create_svg_icon())

    with open(svg_round, 'w') as f:
        f.write(create_svg_icon_round())

    # Android icon sizes
    densities = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }

    print("Generating Android launcher icons...")
    success = True

    for density, size in densities.items():
        mipmap_dir = os.path.join(res_path, f'mipmap-{density}')
        os.makedirs(mipmap_dir, exist_ok=True)

        normal_png = os.path.join(mipmap_dir, 'ic_launcher.png')
        round_png = os.path.join(mipmap_dir, 'ic_launcher_round.png')

        if not convert_svg_to_png(svg_normal, normal_png, size):
            print(f"Warning: Could not convert {density} normal icon")
            success = False
        else:
            print(f"✓ Created {density}/ic_launcher.png ({size}x{size})")

        if not convert_svg_to_png(svg_round, round_png, size):
            print(f"Warning: Could not convert {density} round icon")
            success = False
        else:
            print(f"✓ Created {density}/ic_launcher_round.png ({size}x{size})")

    if not success:
        print("\nWARNING: Some icons could not be generated.")
        print("Please install one of the following:")
        print("  - rsvg-convert: brew install librsvg")
        print("  - ImageMagick: brew install imagemagick")
        print("\nOr use an online tool to convert the SVG files to PNG.")
        return 1

    # Cleanup SVG files
    os.remove(svg_normal)
    os.remove(svg_round)

    print("\n✅ All Android launcher icons generated successfully!")
    return 0

if __name__ == '__main__':
    sys.exit(main())