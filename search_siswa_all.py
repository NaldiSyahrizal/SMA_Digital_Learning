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

keywords = ["siswa", "murid", "student"]
matches = []

for step in steps:
    source = step.get("source")
    content = step.get("content", "")
    if source == "USER_EXPLICIT" and content:
        content_lower = content.lower()
        if any(kw in content_lower for kw in keywords):
            matches.append((step.get("step_index"), content))

print(f"Total historical matches: {len(matches)}\n")
for index, content in matches:
    print(f"--- STEP {index} ---")
    print(content)
    print("===================================\n")
