import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

log_path = r"C:\Users\User\.gemini\antigravity\brain\f8e59dc1-477d-499a-abc6-cbce7d189461\.system_generated\logs\transcript.jsonl"

with open(log_path, "r", encoding="utf-8") as f:
    for i, line in enumerate(f):
        if "implementation_plan.md" in line:
            line_lower = line.lower()
            if "siswa" in line_lower or "murid" in line_lower or "student" in line_lower:
                print(f"Match found on line {i+1}:")
                # print first 2000 chars of the line
                print(line[:2000] + "...")
                print("==================================================\n")
