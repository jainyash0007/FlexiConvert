# FlexiConvert - Offline File Converter

**FlexiConvert** is a powerful, modular, and fully offline file conversion utility built in Java. It supports over 40 popular document, archive, code, and image formats — all with a user-friendly desktop interface that runs without any internet connection.

---

## 🚀 Features

- ✅ **100% Offline Conversion** – All operations run locally with zero cloud dependency
- 🔄 **40+ Format Conversions** – Convert documents, images, archives, spreadsheets, and code files
- 📂 **Batch Support** – Convert multiple files in one go
- 🖱️ **Drag & Drop UI** – Simple, intuitive interface built using Swing
- 💾 **Custom Output Handling** – Rename safely, choose save location, or use default directories
- 🌓 **Theme Toggle** – Light/dark mode support for all users
- 🔍 **Robust Format Detection** – Smart “From → To” selection based on file type

---

## 🧾 Supported Conversions

### 📄 Documents
- **DOCX →** PDF, TXT, HTML  
- **RTF →** PDF, TXT  
- **Markdown →** HTML, PDF  
- **TXT →** PDF, HTML  

### 📊 Spreadsheets
- **XLSX →** CSV, PDF  
- **CSV →** JSON, XML  

### 📁 Archives
- **ZIP, TAR, GZ →** Folder extraction  

### 🖼️ Images
- **PDF →** Images  
- **PNG ↔ JPG, WEBP**  
- **JPG → PNG**  
- **WEBP → PNG**

### 🖥️ Presentations
- **PPTX →** PDF, Images  

### 📜 Code & Markup
- **HTML, XML, JAVA, PY →** PDF, TXT  
- **XML →** JSON, TXT, PDF  
- **JSON →** CSV, XML  

### 🎞️ Embedded Media
- Extract images and media from **DOCX, PPTX, PDF**

---

## 🖥️ System Requirements

- Java 11 or higher  
- Windows, macOS, or Linux  
- Minimum 4GB RAM (8GB recommended for large files)

---

## 📦 Installation

### 🪟 Windows
- [Download the latest `.exe` installer](https://github.com/your-username/your-repo/releases)
- Run the installer and follow the on-screen instructions
- Launch FlexiConvert from the Start Menu or desktop shortcut

### 🐧 macOS / Linux
- [Download the latest `.jar` file](https://github.com/your-username/your-repo/releases)
- Run using:
  ```bash
  java -jar offline-file-converter-1.0.0-shaded.jar
  ```

---

## 📋 Usage

1. **Select Input File:** Browse or drag a file into the app  
2. **Choose Conversion Type:** From → To dropdowns appear dynamically  
3. **Configure Output:** Use default folder or choose a location  
4. **Convert:** Click the “Convert” button  
5. **Save:** Use “Download…” to rename and store the file  

---

## 💡 Output Options

- **Default Output Folder:** Auto-save all files to a selected location  
- **Manual Selection:** Choose a location individually per file  
- **Open Folder Button:** One-click access to output location  
- **Rename Prompt:** Prevents overwriting with smart name suggestions

---

## 🌗 Theme Options

Toggle between light and dark mode using the theme icon in the bottom-right corner of the app.

---

## 🛠️ Building from Source

```bash
git clone https://github.com/your-username/your-repo.git
cd offline-file-converter
mvn clean package
java -jar target/offline-file-converter-1.0.0-shaded.jar
```

---

## 🧱 Dependencies

- [Apache PDFBox](https://pdfbox.apache.org/)
- [Apache POI](https://poi.apache.org/)
- [Jackson](https://github.com/FasterXML/jackson)
- [Commons Compress](https://commons.apache.org/proper/commons-compress/)
- [CommonMark](https://commonmark.org/)
- [Swing (Java UI Toolkit)](https://docs.oracle.com/javase/8/docs/technotes/guides/swing/)

---

## 📄 License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.

---

## 🙌 Acknowledgments

Built with ❤️ by Yash Jain using 100% open-source Java libraries.
