```md
# InfinoVault

InfinoVault is a fully on-device AI-powered document assistant for Android.  
It allows users to import PDFs, process them locally, and query their content using a lightweight LLM — without any cloud or API dependency.

---

## 🚀 Features

- 📄 Import and manage PDF documents
- 🔍 Automatic text extraction and chunking
- 🧠 Local embedding and semantic search
- 🤖 On-device LLM (SmolLM) for answering queries
- 🔒 100% offline — no data leaves the device

---

## 🧠 Architecture

```

PDF → Text Extraction → Chunking → Embedding → Retrieval → LLM → Answer

````

- **Extraction**: Parses PDF content
- **Chunking**: Splits text into manageable segments
- **Embedding**: Converts text into vector representations
- **Retrieval**: Finds relevant chunks based on query
- **LLM**: Generates final answer using context

---

## 📱 Tech Stack

- Kotlin (Android)
- Jetpack Compose
- Room Database
- On-device LLM (SmolLM)
- Custom lightweight embedding + retrieval pipeline

---

## ⚙️ Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/kfl15/InfinoVaultAi.git
````

2. Open in Android Studio

3. Build and run on emulator or device

---

## 🧪 Usage

1. Open the app
2. Tap the 📄 icon
3. Import a PDF
4. Go back to chat
5. Ask questions about the document

---

## ⚠️ Limitations

* Uses lightweight embedding (basic semantic capability)
* Small LLM → limited context window
* PDF extraction may vary depending on document structure

---

## 🔮 Future Improvements

* Better embedding models (MiniLM / OpenL3)
* Persistent vector database
* Multi-document querying
* UI for document selection
* Improved PDF parsing (OCR support)

---

## 👨‍💻 Author

Developed by extending an open-source Android LLM app and building a complete on-device RAG system on top.

---

## 📄 License

This project builds upon open-source components. Refer to original repositories for respective licenses.

```
```
