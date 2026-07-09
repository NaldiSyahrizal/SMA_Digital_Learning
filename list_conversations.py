import os
import sys

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

brain_dir = r"C:\Users\User\.gemini\antigravity\brain"
for item in os.listdir(brain_dir):
    item_path = os.path.join(brain_dir, item)
    if os.path.isdir(item_path) and not item.startswith("."):
        print(f"Conversation: {item}")
        # check if it has a transcript
        log_file = os.path.join(item_path, ".system_generated", "logs", "transcript.jsonl")
        if os.path.exists(log_file):
            size_kb = os.path.getsize(log_file) / 1024
            print(f"  Transcript exists: {size_kb:.2f} KB")
