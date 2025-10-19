# Resume Analyzer Application with ATS Score Checker

## Overview

This is a standalone Java application designed to analyze and process resume documents (e.g., .pdf, .doc, .docx). It leverages **Apache Tika** for robust text extraction and parsing from various file formats.

## Getting Started

These instructions cover the prerequisites and steps needed to run the application.

### Prerequisites

You must have the **Java Runtime Environment (JRE)** installed on your system.
tika-app-3.2.3.jar is not provided with the source code. so make sure to download it.
(Note: No need to download tika-app-3.2.3.jar if you are downloading the release zip file. it is included in it) 

1.  **Check for Java:** Open your terminal or Command Prompt and run `java -version`.
2.  If Java is not installed, please download and install the latest JRE or JDK.

---

### Setup and Running the Application

Since this is a bundled application, no formal installation is required.

1.  **File Location:** Ensure all four files are located in the same directory:
    * `ResumeAnalyzerApp.jar`
    * `tika-app-3.2.3.jar`
    * `run_app.bat`
    * `run_app.sh`

2.  **Run the Application:** Use the script appropriate for your operating system.

| Operating System | How to Run |
| :--- | :--- |
| **Windows** | **Double-click** the `run_app.bat` file. |
| **macOS / Linux** | Open **Terminal**, Navigate to this folder, Run: `./run_app.sh` |

## 🛠 Included Files

* **`ResumeAnalyzerApp.jar`**: The main Java application file.
* **`tika-app-3.2.3.jar`**: The dependency file for Apache Tika, required for document parsing.
* **`run_app.bat`**: Script for launching the application on **Windows**.
* **`run_app.sh`**: Script for launching the application on **Linux/macOS**.
