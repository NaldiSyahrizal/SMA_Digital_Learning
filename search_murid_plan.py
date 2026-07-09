import json
import sys

# Reconfigure stdout to use UTF-8 to prevent any encoding issues
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

keywords = ["siswa", "murid", "student", "plan", "rencana"]
found_count = 0

for i, step in enumerate(steps):
    source = step.get("source")
    content = step.get("content", "")
    
    if source == "USER_EXPLICIT" and content:
        content_lower = content.lower()
        # Look for messages where "murid" or "siswa" occurs together with "plan", "rencana", or details
        if ("murid" in content_lower or "siswa" in content_lower) and ("plan" in content_lower or "rencana" in content_lower or "buat" in content_lower or "alur" in content_lower):
            print(f"\n=========================================")
            print(f"STEP {step.get('step_index')} (USER INPUT):")
            print(content)
            print("-----------------------------------------")
            found_count += 1
            # Print subsequent model steps to see the response/plan
            for j in range(i + 1, min(i + 4, len(steps))):
                next_step = steps[j]
                next_source = next_step.get("source")
                next_content = next_step.get("content", "")
                next_type = next_step.get("type", "")
                if next_source == "MODEL" and next_content:
                    print(f"RESPONSE (Step {next_step.get('step_index')} - {next_type}):")
                    print(next_content[:1200] + "..." if len(next_content) > 1200 else next_content)
                    print(".........................................")

print(f"\nTotal matches found: {found_count}")
