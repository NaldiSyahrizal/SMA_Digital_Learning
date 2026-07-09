import os
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

artifacts_dir = r"C:\Users\User\.gemini\antigravity\brain\f8e59dc1-477d-499a-abc6-cbce7d189461"
keywords = ["siswa", "murid", "student"]

for file in os.listdir(artifacts_dir):
    if file.endswith(".md"):
        file_path = os.path.join(artifacts_dir, file)
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
                content_lower = content.lower()
                if any(kw in content_lower for kw in keywords):
                    print(f"Found keyword in: {file}")
                    # Find and print headings or lists with keywords
                    lines = content.split("\n")
                    for line in lines:
                        if line.strip().startswith("#") and any(kw in line.lower() for kw in keywords):
                            print(f"  Heading: {line}")
                        elif ("murid" in line.lower() or "siswa" in line.lower()) and ("plan" in line.lower() or "fitur" in line.lower() or "halaman" in line.lower()):
                            print(f"  Content snippet: {line}")
        except Exception as e:
            print(f"Error reading {file}: {e}")
