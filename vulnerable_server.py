#!/usr/bin/env python3
"""
Vulnerable EXIF Server - FOR EDUCATIONAL PURPOSES ONLY

This is a deliberately vulnerable Flask application that demonstrates
the EXIF command injection vulnerability. Use this to test exploits
locally before attempting the real challenge.

WARNING: DO NOT deploy this to production or expose to the internet!
This server contains intentional security vulnerabilities.

Usage:
    python3 vulnerable_server.py
    
Then upload images via:
    curl -F "file=@exploit.jpg" http://localhost:5000/upload
    Or open http://localhost:5000 in a browser
"""

import os
import sys
import tempfile

# Check if Flask is installed
try:
    from flask import Flask, request, render_template_string, jsonify
except ImportError:
    print("Flask is required. Install with: pip install Flask")
    sys.exit(1)

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max upload
app.config['UPLOAD_FOLDER'] = tempfile.mkdtemp()

# HTML template for the upload page
HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <title>EXIF Metadata Viewer</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        .container {
            background: rgba(255, 255, 255, 0.1);
            padding: 30px;
            border-radius: 10px;
            backdrop-filter: blur(10px);
        }
        h1 {
            text-align: center;
            margin-bottom: 30px;
        }
        .upload-form {
            background: rgba(255, 255, 255, 0.2);
            padding: 20px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        input[type="file"] {
            margin: 10px 0;
            padding: 10px;
            background: white;
            color: #333;
            border-radius: 5px;
            width: 100%;
        }
        button {
            background: #4CAF50;
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
        }
        button:hover {
            background: #45a049;
        }
        .result {
            background: rgba(0, 0, 0, 0.3);
            padding: 20px;
            border-radius: 5px;
            margin-top: 20px;
            white-space: pre-wrap;
            font-family: 'Courier New', monospace;
        }
        .warning {
            background: rgba(255, 0, 0, 0.3);
            border: 2px solid #ff6b6b;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📷 EXIF Metadata Viewer</h1>
        
        <div class="warning">
            <strong>⚠️ WARNING:</strong> This is a deliberately vulnerable application for educational purposes.
            DO NOT use in production!
        </div>
        
        <div class="upload-form">
            <h2>Upload an Image</h2>
            <form method="POST" enctype="multipart/form-data" action="/upload">
                <input type="file" name="file" accept="image/*" required>
                <button type="submit">Upload & View EXIF Data</button>
            </form>
        </div>
        
        {% if result %}
        <div class="result">
            <h3>EXIF Metadata:</h3>
            {{ result }}
        </div>
        {% endif %}
        
        <div style="margin-top: 30px; font-size: 12px; text-align: center; opacity: 0.8;">
            <p>This server intentionally contains security vulnerabilities for educational purposes.</p>
            <p>Try exploiting the EXIF processing with command injection!</p>
        </div>
    </div>
</body>
</html>
"""

@app.route('/')
def index():
    """Main page with upload form."""
    return render_template_string(HTML_TEMPLATE)

@app.route('/upload', methods=['POST'])
def upload_file():
    """
    Handle file upload and process EXIF data.
    
    VULNERABLE: This function intentionally uses os.popen() which is
    susceptible to command injection attacks.
    """
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400
    
    # Save uploaded file
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
    file.save(filepath)
    
    print(f"[*] File uploaded: {filepath}")
    
    try:
        # VULNERABLE CODE - DO NOT USE IN PRODUCTION!
        # This uses os.popen() which executes commands in a shell
        # allowing command injection through filename or EXIF data
        # 
        # NOTE: Even single quotes don't fully protect against injection!
        # If the filepath or EXIF data contains: ' followed by shell metacharacters
        # Example: filename like: image.jpg' ; cat flag.txt ; echo '
        # The command becomes: exiftool -Artist 'image.jpg' ; cat flag.txt ; echo '' 2>/dev/null
        # This executes the injected command!
        
        # Method 1: Extract Artist field (VULNERABLE to command injection)
        cmd = f"exiftool -Artist '{filepath}' 2>/dev/null || echo 'exiftool not installed'"
        print(f"[!] Executing vulnerable command: {cmd}")
        artist_output = os.popen(cmd).read()
        
        # Method 2: Extract all EXIF data (also VULNERABLE)
        cmd2 = f"exiftool '{filepath}' 2>/dev/null || echo 'exiftool not installed'"
        exif_output = os.popen(cmd2).read()
        
        result = f"Artist Field:\n{artist_output}\n\nFull EXIF Data:\n{exif_output}"
        
        # Clean up
        try:
            os.remove(filepath)
        except:
            pass
        
        return render_template_string(HTML_TEMPLATE, result=result)
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/upload', methods=['POST'])
def api_upload():
    """
    API endpoint for programmatic uploads.
    Returns JSON response with EXIF data.
    """
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400
    
    # Save uploaded file
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
    file.save(filepath)
    
    try:
        # VULNERABLE CODE - Command injection possible!
        cmd = f"exiftool '{filepath}' 2>/dev/null || echo 'exiftool not installed'"
        exif_output = os.popen(cmd).read()
        
        # Clean up
        try:
            os.remove(filepath)
        except:
            pass
        
        return jsonify({
            'success': True,
            'exif_data': exif_output
        })
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

def create_flag_file():
    """Create a test flag.txt file for local testing."""
    flag_content = "flag{test_exif_command_injection_vuln_12345}"
    flag_path = os.path.join(app.config['UPLOAD_FOLDER'], 'flag.txt')
    
    with open(flag_path, 'w') as f:
        f.write(flag_content)
    
    print(f"[*] Created test flag file at: {flag_path}")
    print(f"[*] Flag content: {flag_content}")
    
    # Also create it in the current directory for easier access
    with open('flag.txt', 'w') as f:
        f.write(flag_content)
    
    return flag_path

if __name__ == '__main__':
    print("""
    ╔═══════════════════════════════════════════════════════════╗
    ║     Vulnerable EXIF Server - FOR EDUCATION ONLY           ║
    ╚═══════════════════════════════════════════════════════════╝
    
    This server intentionally contains security vulnerabilities!
    
    - Upload endpoint: http://localhost:5000/upload
    - API endpoint: http://localhost:5000/api/upload
    - Web interface: http://localhost:5000
    
    The server processes EXIF metadata using os.popen() which is
    vulnerable to command injection attacks.
    
    Test your exploits against this server before attempting the
    real challenge!
    
    WARNING: DO NOT expose this server to the internet!
             DO NOT use this code in production!
    
    Press Ctrl+C to stop the server.
    ═══════════════════════════════════════════════════════════
    """)
    
    # Create test flag file
    flag_path = create_flag_file()
    print(f"\n[*] Server starting on http://localhost:5000")
    print(f"[*] Upload folder: {app.config['UPLOAD_FOLDER']}")
    print(f"[*] Test flag location: {flag_path}\n")
    
    # Run the server
    app.run(debug=True, host='0.0.0.0', port=5000)
