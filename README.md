# JADX-AI (v0.0.1-beta)

> Fork of [JADX](https://github.com/skylot/jadx) with Model Context Protocol (MCP) integration for AI-powered static code analysis and real-time code review using Claude.

![jadx-ai-banner](docs/assets/jadx-ai-banner.png)

---

## 🚀 What is JADX-AI?

**JADX-AI** is a modified version of the [original JADX decompiler](https://github.com/skylot/jadx) that integrates directly with [Model Context Protocol (MCP)](https://github.com/anthropic/mcp) to provide **live reverse engineering support with LLMs like Claude**.

Think: "Decompile → Context-Aware Code Review → AI Recommendations" — all in real time.

---

## 🔥 Why a Fork Instead of a Plugin?

While the plugin system in JADX is useful, it has limitations:

| Feature                           | Plugin | Fork (jadx-ai) ✅ |
|----------------------------------|--------|-------------------|
| GUI manipulation                 | ❌     | ✅                |
| Export live context to LLM       | ❌     | ✅                |
| Auto-save project for analysis   | ❌     | ✅                |
| Integrate MCP HTTP server inside | ❌     | ✅                |

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

### 1. Clone and Build

```bash
git clone https://github.com/<your-username>/jadx-ai.git
cd jadx-ai
./gradlew dist

Output can be found at:

jadx/build/jadx/bin/

You can run it with:

./jadx-gui

2. Claude Desktop Setup

Make sure Claude Desktop is running with MCP enabled.

Then, navigate code and interact via real-time code review prompts using the built-in integration.
🧠 Future Roadmap

AI-assisted vulnerability scanning (auto-SAST)

Claude plugin suggestions pane in GUI

Save + diff decompiled classes

Annotate and summarize methods

Export Smali-to-LLM context

    Integrate with Browser Bruter + other MCP clients

🙏 Credits

This project is a fork of the original JADX, an amazing open-source Android decompiler created and maintained by @skylot. All core decompilation logic belongs to them. We extend it with AI capabilities.

If you're looking for pure decompilation, please consider using the original repo.
📎 Original README (JADX)

The original README.md from jadx is included here in this repository for reference and credit.
🧪 License

This fork inherits the Apache 2.0 License from the original JADX repository.

    Built with ❤️ for the reverse engineering and AI communities.
