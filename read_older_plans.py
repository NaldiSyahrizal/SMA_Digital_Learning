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

found_plans = []
for idx, s in enumerate(steps):
    tool_calls = s.get("tool_calls", [])
    for tc in tool_calls:
        func = tc.get("function", {})
        name = func.get("name")
        args = func.get("arguments", {})
        
        target_file = args.get("TargetFile", "")
        if "implementation_plan.md" in target_file:
            content = args.get("CodeContent", "")
            if not content:
                # check ReplacementChunks or other fields
                content = str(args.get("ReplacementChunks", ""))
            
            if "murid" in content.lower() or "siswa" in content.lower() or "student" in content.lower():
                found_plans.append((s.get("step_index"), content))

print(f"Found {len(found_plans)} plan modifications referencing student/murid.\n")
# Print the oldest one or list them
for step_idx, content in found_plans:
    print(f"==================================================")
    print(f"STEP {step_idx}:")
    print(content[:2500]) # print first 2500 chars
    print(f"==================================================\n")
