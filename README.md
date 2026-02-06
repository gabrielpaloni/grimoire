# 🧙‍♂️ Grimoire: Personal Knowledge Base

![Language](https://img.shields.io/badge/Language-Java_17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Linux%20%7C%20Windows-0078D7?style=for-the-badge&logo=linux&logoColor=white)
![Build](https://img.shields.io/badge/Build-Manual%20CLI-success?style=for-the-badge)

<br>
<p align="center">
  <img src="https://github.com/user-attachments/assets/5691e71c-bf94-4e24-98bf-c49384664dc5" alt="Grimoire Demo" width="100%" style="border-radius: 6px;">
</p>
<br>

**Grimoire** is a lightweight, local-first **Markdown Note-Taking Application** developed to create a distraction-free writing environment. It represents a deep dive into **Desktop GUI Development** and **Text Processing** using pure Java Swing, bridging the gap between raw text editing and rich HTML visualization.

---

## 📘 Project Philosophy: Second Brain

**Objective:**
To build a "Second Brain" utility that captures thoughts in Markdown syntax and instantly renders them. The focus was on **performance**, **independence** (zero external libraries), and **stability**.

### 🎯 Key Features
* **Hybrid Engine:** Combines a raw text editor with a real-time **HTML Preview**, powered by a custom-built Regex Parser (`MarkdownParser.java`).
* **Safe-Render System:** Implements a robust ASCII/Text-based fallback system for lists and icons, ensuring visual consistency across different Linux distributions and Java versions without encoding glitches.
* **Modern GUI (Swing):** A custom-built **borderless window** featuring a dark theme ("Dracula" inspired), custom scrollbars, and responsive layout.
* **Developer Friendly:**
    * Syntax Highlighting for Java code blocks.
    * One-click integration to open notes in **VS Code** or **IntelliJ**.
* **Smart Persistence:**
    * Auto-saves notes to a local folder (`meusEstudos`).
    * Remembers window position, size, and "Always on Top" state via `grimoire.properties`.

---

## 🛠️ Technical Evolution

This project was pivotal in mastering advanced Java Desktop development without reliance on Maven/Gradle magic:
* **Manual Build Pipeline:** Mastered the use of `javac` and `jar` CLI tools to manage classpaths, packages, and artifact generation manually.
* **Regex Parser:** Implemented a **Lexer/Parser** from scratch using Regular Expressions to convert Markdown symbols into HTML tags compatible with Java's `JEditorPane`.
* **Swing Customization:** Transitioned from standard Java interfaces to fully custom components (`paintComponent`), managing Layout Managers and Event Listeners manually.

### Project Structure
```text
Grimoire/
├── grimoire.jar       # Compiled Executable
├── meusEstudos/       # Your Notes Storage
├── src/
│   ├── core/          # Parsers, I/O, Config
│   ├── model/         # Data Classes
│   ├── view/          # GUI Components
│   └── Main.java
└── README.md
```

---

## 🚀 How to Run
Since this project uses Pure Java without dependency managers, you can compile it directly from the terminal.

**Prerequisites**
* Java JDK 11 or higher.

### 1. Build from Source (CLI)
```
# 1. Clone the repository
git clone https://github.com/gabrielpaloni/grimoire.git
cd grimoire

# 2. Compile the source code
# We map the source files to the 'out' directory
javac -d out src/core/*.java src/model/*.java src/view/*.java

# 3. Create the JAR executable
# We specify the entry point (view.Main) and include the compiled classes
jar cfe grimoire.jar view.Main -C out .
```

### 2. Run
```
java -jar grimoire.jar
```

### 3. Linux Shortcut (Optional)
To run Grimoire from anywhere in your terminal, run this inside the project folder:
```
echo -e '#!/bin/bash\ncd "'$(pwd)'"\njava -jar grimoire.jar > /dev/null 2>&1 &' | sudo tee /usr/local/bin/grimoire > /dev/null && sudo chmod +x /usr/local/bin/grimoire
```
Now just type grimoire!
---

##👥 Author
**Gabriel Paloni** - Computer Science Student Campinas, Brazil

While my academic focus is on **AI** and **Cybersecurity**, this project serves as a practical application of data structures, software architecture, and the importance of understanding how code works "under the hood" before using frameworks.

---

<p align="center"> <i>"Simplicity is the ultimate sophistication."</i> </p>
