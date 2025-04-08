# JADX-AI (v0.0.1-beta)

> It is a still in beta version, so expects bugs, crashes and logical erros.

> Fork of [JADX](https://github.com/skylot/jadx) with Model Context Protocol (MCP) integration for AI-powered static code analysis and real-time code review and reverse engineering tasks using Claude.

![jadx-ai-banner.png](img.png) generated using AI tools.

---

## 🤖 What is JADX-AI?

**JADX-AI** is a modified version of the [original JADX decompiler](https://github.com/skylot/jadx) that integrates directly with [Model Context Protocol (MCP)](https://github.com/anthropic/mcp) to provide **live reverse engineering support with LLMs like Claude**.

Think: "Decompile → Context-Aware Code Review → AI Recommendations" — all in real time.

[![Watch the video](https://img.youtube.com/vi/Od86IOkkaHg/0.jpg)](https://www.youtube.com/watch?v=Od86IOkkaHg&autoplay=1)

## Current MCP Tools

The following MCP tools are available:

- `fetch_current_class()` — Get the class name and full source of selected class
- `get_selected_text()` — Get currently selected text
- `get_all_classes()` — List all classes in the project
- `get_class_source(class_name)` — Get full source of a given class
- `get_method_by_name(class_name, method_name)` — Fetch a method’s source
- `search_method_by_name(method_name)` — Search method across classes
- `get_methods_of_class(class_name)` — List methods in a class
- `get_fields_of_class(class_name)` — List fields in a class
- `get_method_code(class_name, method_name)` — Alias for `get_method_by_name` //to be removed
- `get_smali_of_class(class_name)` — Fetch smali of class

---

## 🗒️ Sample Prompts

🔍 Basic Code Understanding

    "Explain what this class does in one paragraph."

    "Summarize the responsibilities of this method."

    "Is there any obfuscation in this class?"

    "List all Android permissions this class might require."

🛡️ Vulnerability Detection

    "Are there any insecure API usages in this method?"

    "Check this class for hardcoded secrets or credentials."

    "Does this method sanitize user input before using it?"

    "What security vulnerabilities might be introduced by this code?"

🛠️ Reverse Engineering Helpers

    "Deobfuscate and rename the classes and methods to something readable."

    "Can you infer the original purpose of this smali method?"

    "What libraries or SDKs does this class appear to be part of?"

📦 Static Analysis

    "List all network-related API calls in this class."

    "Identify file I/O operations and their potential risks."

    "Does this method leak device info or PII?"

🤖 AI Code Modification

    "Refactor this method to improve readability."

    "Add comments to this code explaining each step."

    "Rewrite this Java method in Python for analysis."

📄 Documentation & Metadata

    "Generate Javadoc-style comments for all methods."

    "What package or app component does this class likely belong to?"

    "Can you identify the Android component type (Activity, Service, etc.)?"

## 🔥 Why a Fork Instead of a Plugin?

While the plugin system in JADX is useful, it has limitations:

| Feature                                       | Plugin | Fork (jadx-ai) ✅ |
|-----------------------------------------------|--------|-------------------|
| GUI manipulation                              | ❌     | ✅                |
| Export live GUI context to LLM                | ❌     | ✅                |
| Auto-save project for analysis                | ❌     | ✅                |
| Integrate MCP HTTP server inside JADX it self | ❌     | ✅                |

This fork allows total control over the GUI and internal project model to support deeper LLM integration, including:

- Exporting selected class to MCP
- Running automated Claude analysis
- Receiving back suggestions inline

---

## 📦 Features (v0.0.1-beta)

- ✅ MCP server baked into JADX-GUI
- ✅ Exposes currently selected class via HTTP
- ✅ Built-in Claude Desktop integration
- ✅ Beta support for real-time code review
- ✅ MCP client interoperability via local loopback

This is a **developer beta** — designed for reverse engineers, AI researchers, and LLM tool builders.

---

## 🛠️ Getting Started

### 1. Downlaod from Releases: https://github.com/zinja-coder/jadx-ai/releases

```bash
# 1. Unzip the jadx-ai-beta-v.0.0.1.zip
unzip jadx-ai-<version>.zip

# 2. cd to jadx-ai directory
cd jadx-ai-<version>

# 3. Make it executable
sudo chmod +x ./bin/jadx-gui
sudo chmod +x ./bin/jadx

# 3. Execute the jadg-gui
./bin/jadx-gui

# Report any error and issue at this repo
```

### 2. Clone and Build

```bash
# 1. Clone this repo
git clone https://github.com/zinja-coder/jadx-ai.git
cd jadx-ai
./gradlew dist

# Output can be found at:

jadx/build/jadx/bin/

# 2. You can run it with:

./jadx-gui
```

## 🤖 Claude Desktop Setup

Make sure Claude Desktop is running with MCP enabled.

For instance, I have used following for Kali Linux: https://github.com/aaddrick/claude-desktop-debian

Then, navigate code and interact via real-time code review prompts using the built-in integration.

# 🧠 Future Roadmap

 - AI-assisted vulnerability scanning (auto-SAST)

 - Claude plugin suggestions pane in GUI

 - Save + diff decompiled classes

 - Annotate and summarize methods

 - Perform reverse engieering tasks using LLM

# 🙏 Credits

This project is a fork of the original JADX, an amazing open-source Android decompiler created and maintained by [@skylot](https://github.com/skylot). All core decompilation logic belongs to them. I have only extended it to support my MCP server with AI capabilities.

[📎 Original README (JADX)](https://github.com/skylot/jadx)

The original README.md from jadx is included here in this repository for reference and credit.

# 🧪 License

This fork inherits the Apache 2.0 License from the original JADX repository.

Built with ❤️ for the reverse engineering and AI communities.
