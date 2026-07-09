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

matches = []
for idx, s in enumerate(steps):
    source = s.get("source")
    content = s.get("content", "")
    if source == "USER_EXPLICIT" and content:
        content_lower = content.lower()
        if "murid" in content_lower or "siswa" in content_lower:
            # Check for words describing student tasks
            if any(w in content_lower for w in ["kuis", "tugas", "materi", "lihat", "baca", "kerjakan", "kirim", "nilai", "grading", "submit"]):
                # exclude admin plotting
                if "ploting" not in content_lower or "guru" in content_lower:
                    matches.append((s.get("step_index"), content))

print(f"Total matching student task/feature discussions: {len(matches)}\n")
for index, content in matches:
    print(f"--- STEP {index} ---")
    print(content)
    print("===================================\n")
