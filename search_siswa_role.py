import json
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

log_path = r"C:\Users\User\.gemini\antigravity\brain\f8e59dc1-477d-499a-abc6-cbce7d189461\.system_generated\logs\transcript.jsonl"

steps = []
with open(log_path, "r", encoding="utf-8") as f:
    for line in f:
        try:
            steps.append(json.loads(line))
        except Exception:
            pass

keywords = ["murid", "siswa"]
action_keywords = ["login", "dashboard", "menu", "halaman", "fitur", "tampilan", "aplikasi", "akses", "jawab", "kerjakan", "tugas", "kuis"]

matches = []
for idx, s in enumerate(steps):
    source = s.get("source")
    content = s.get("content", "")
    if source == "USER_EXPLICIT" and content:
        content_lower = content.lower()
        # Find user inputs where "murid" or "siswa" occurs together with other action keywords related to student app
        if any(kw in content_lower for kw in keywords) and any(akw in content_lower for akw in action_keywords):
            # Check if this isn't just about teacher or admin plotting
            if "ploting" not in content_lower and "kelola" not in content_lower:
                matches.append((s.get("step_index"), content))

print(f"Found {len(matches)} potential student role discussions:\n")
for index, content in matches[-10:]: # print the last 10 matches (recent ones)
    print(f"--- STEP {index} ---")
    print(content)
    print("===================================\n")
