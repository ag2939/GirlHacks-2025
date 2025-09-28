import os
import re
import json
from dotenv import load_dotenv
from glob import glob
from mistralai import Mistral

load_dotenv()

# Keep the conversation history in a list
messages = [
    {
        "role": "user",
        "content": """You are a professional software security analyst. Given a unit of code, identify all potential vulnerabilities, security risks, or bad practices present. For each issue, provide:
                1. The type of vulnerability or risk (e.g., SQL injection, XSS, insecure API usage, buffer overflow, etc.).
                2. A brief explanation of why it is a risk.
                3. The exact location in the code (line number or code snippet).
                If you do not find any vulnerabilities in the unit, explicitly write:
                    "No vulnerabilities in this unit."
                Respond in a clear, structured format that can be easily read and referenced."""
    }
]


def load_model(model_name="mistral-large-2411"):
    api_key = os.environ["MISTRAL_API_KEY"]
    client = Mistral(api_key=api_key)
    response = client.chat.complete(model=model_name, messages=messages)
    answer = response.choices[0].message.content
    messages.append({"role": "assistant", "content": answer})
    return model_name, client


def do_chat(client, model, content):
    messages.append({"role": "user", "content": f"Find vulnerabilities in the code unit below:\n{content}"})
    response = client.chat.complete(model=model, messages=messages)
    answer = response.choices[0].message.content
    messages.append({"role": "assistant", "content": answer})
    return answer


def gen_test(client, model, vulnerability):
    prompt = f"""
    You are a security test-case generator for Android apps. Produce a JSON array of defensive test-cases
    for the target code described with vulnerabilities below. Each test-case must include: id, title, objective, preconditions,
    non_exploit_steps (numbered list), expected_results, severity, and recommended_mitigation. 

    IMPORTANT: Do NOT produce exploit payloads, do not provide steps that instruct bypassing authentication or
    performing unauthorized access on production systems. All tests must be safe to run in a controlled
    test environment (emulators, CI, or test projects) and focus on detection, validation, or configuration checks.

    Target code/context (short): imports include android.util.Log, Environment, BroadcastReceiver, DownloadManager,
    ExoPlayer, FirebaseDatabase, FirebaseStorage. The app may write to external storage and uses Firebase and ExoPlayer.

    Produce at least 8 test-cases in JSON. Vulnerabilities are:
    {vulnerability}
    """
    response = client.chat.complete(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        temperature=0.0,
    )
    json_string = response.choices[0].message.content.split("```json")[1].split("```")[0].strip()
    print(json_string)
    test_cases = json.loads(json_string)
    return test_cases


def split_java_sections(code_lines):
    sections = []
    buffer = []
    brace_count = 0
    in_block = False

    # Combine lines to preserve formatting
    lines = code_lines[:]

    for line in lines:
        stripped = line.strip()

        # Always include package/import lines as separate units
        if re.match(r"^package\s+[\w.]+;|^import(\s+static)?\s+[\w.]+(\.[\w*]+)?;", stripped):
            sections.append(stripped)
            continue

        # Detect start of a class or method block
        if re.match(r"^public\s+class\s+\w+.*\{", stripped) or \
           re.match(r"^(public|private|protected)?\s*[\w<>\[\]]+\s+\w+\s*\([^)]*\)\s*\{", stripped) or \
           re.match(r"^\s*@\w+", stripped):  # annotation
            buffer.append(line)
            brace_count += line.count("{") - line.count("}")
            in_block = True
            continue

        # If inside a block, accumulate lines
        if in_block:
            buffer.append(line)
            brace_count += line.count("{") - line.count("}")
            if brace_count == 0:
                sections.append("\n".join(buffer).strip())
                buffer = []
                in_block = False
            continue

        # Otherwise, treat as standalone (fields, etc.)
        if stripped:
            sections.append(stripped)

    idx = 0
    while idx < len(sections):
        section = sections[idx]
        statement_count = sum(1 for line in section.splitlines() if line.strip() and not line.strip().startswith('//'))
        if statement_count < 3 and idx != 0:
            sections[idx - 1] += "\n" + sections.pop(idx)
        else:
            idx += 1

    return sections


def find_vulnerabilities(source_dir):
    for path in glob(source_dir):
        with open(path, mode="r") as finput:
            print("Reading file:", path)
            content = finput.read()
            lines = content.splitlines()
            lines = [line for line in lines if line != '']
            units = split_java_sections(lines)

            with open(os.path.join('outputs', f"{os.path.basename(path).split('.')[0]}.txt"), mode='w+', encoding='utf-8') as foutput:
                for unit in units:
                    # print(unit)
                    answer = do_chat(client, model, unit)
                    foutput.write(unit + "\n" + answer)
                    foutput.write("\n=====================================================================================\n")


def make_test_cases(source_dir):
    for path in glob(source_dir):
        with open(path, mode="r") as finput:
            print("Reading file:", path)
            content = finput.read()
            sections = content.split("=====================================================================================")
            for section in sections:
                answer = gen_test(client, model, section)
                output_path = os.path.join('tests', f"{os.path.basename(path).split('.')[0]}.json")
                if not os.path.exists(output_path):
                    with open(output_path, "w", encoding="utf-8") as f:
                        json.dump([answer], f, ensure_ascii=False, indent=2)
                else:
                    with open(output_path, "r", encoding="utf-8") as f:
                        if f.read().strip() != '':
                            data = json.load(f)
                            data.append(answer)

                    with open(output_path, "w", encoding="utf-8") as f:
                        json.dump(data, f, ensure_ascii=False, indent=2)


if __name__ == '__main__':
    model, client = load_model()
    find_vulnerabilities(source_dir="./ShikshaReMastered/app/src/main/java/com/example/shiksharemastered/*.java")
    make_test_cases(source_dir="./outputs/*.txt")