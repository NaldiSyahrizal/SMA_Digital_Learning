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

target_index = 7037
found = False

for idx, step in enumerate(steps):
    step_idx = step.get("step_index")
    if step_idx == target_index:
        found = True
        start_pos = max(0, idx - 1)
        end_pos = min(len(steps), idx + 8)
        
        for k in range(start_pos, end_pos):
            s = steps[k]
            print(f"\n=== STEP {s.get('step_index')} ({s.get('source')} - {s.get('type')}) ===")
            content = s.get("content", "")
            print(content[:1500] + "..." if len(content) > 1500 else content)
            print("===================================\n")
        break

if not found:
    print(f"Step {target_index} not found.")
