# StudyMind AI — Java-Powered Academic Study Assistant 🧠

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/)
[![Google Gemini](https://img.shields.io/badge/Google_Gemini-3.5%20Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://aistudio.google.com/)
[![Database](https://img.shields.io/badge/Database-Embedded%20H2-blue?style=for-the-badge)](http://localhost:8080/h2-console)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

An interactive, full-stack AI study assistant and revision platform built in **Java 21**, **Spring Boot**, and **Apache PDFBox**. Designed for students to converse with academic course materials, retain multi-turn context, generate revision notes, and perform **Retrieval-Augmented Generation (RAG)** grounded with exact page-level citations.

---

## 🌟 Key Features

1. **🤖 AI Academic Tutor (Multi-Turn Chat):**
   - Natural academic mentor dialogue with encouraging tone.
   - Preserves conversation history across sessions in a local persistent H2 database.
   - Multi-turn context preservation (follow-up doubts retain prior conversational memory).

2. **📄 Document Extraction & Processing:**
   - Supports **PDF** and **TXT** lecture notes and textbooks up to **5 MB**.
   - Page-by-page text extraction powered by **Apache PDFBox 3.x**.
   - Validates file formats and file sizes with clear error handling.

3. **🔍 Context-Grounded RAG (Retrieval-Augmented Generation):**
   - Pure Java **Sliding-Window Chunking** (~500 words with 50-word overlap).
   - In-memory **TF-IDF & Keyword Similarity Search** (zero heavy external vector DB required).
   - Strict source citations: `[Source: <filename>, Page <pageNumber>]`.
   - **Anti-Hallucination Guardrail:** If an answer cannot be grounded in the student's file, it politely responds:
     > *"I couldn't find enough information in your uploaded sources."*

4. **⚡ Active Recall Revision Tools:**
   - **📝 Structured Summary:** Core concepts, key definitions, and exam takeaways.
   - **🎯 5 MCQs:** Multiple-choice quiz questions with option distractors and explanations.
   - **🗂️ 5 Flashcards:** Concept / Question and concise answers for rapid self-testing.

5. **📺 YouTube Lecture Summarizer:**
   - Paste any educational YouTube URL to extract structured lecture notes, main topics, and bulleted takeaways.

6. **💾 Note Export (PDF & Excel):**
   - Export generated study notes and revision summaries into downloadable **PDF** files.
   - Export structured topics directly into **Excel (.xlsx)** sheets.

7. **🎨 Modern Dark Theme Interface:**
   - Responsive slate & indigo dark UI.
   - Session history sidebar, live typing indicators, and smooth transitions.

---

## 🏗️ Architecture & OOP Design

The project strictly follows Clean Architecture and Object-Oriented Design principles:

```
com.studymind
├── ai
│   ├── AIService.java                   # Interface: Abstraction & DIP
│   └── GeminiAIService.java             # Implementation: pure java.net.http.HttpClient
├── controller
│   ├── HomeController.java              # MVC view routing
│   ├── ChatApiController.java          # REST: Conversation & messaging
│   ├── DocumentApiController.java      # REST: File upload & status
│   ├── ModernFeaturesController.java   # REST: YouTube notes & PDF/Excel export
│   ├── SearchApiController.java        # REST: Diagnostic chunk search
│   └── StudyToolsApiController.java    # REST: Summary, MCQs, Flashcards
├── dto
│   ├── ChatRequest.java                 # Request payload encapsulation
│   ├── ChatResponse.java                # Response carrier with factory methods
│   └── DocumentUploadResponse.java      # Upload metadata carrier
├── exception
│   └── GlobalExceptionHandler.java      # AOP: Centralized exception handler
├── model
│   ├── Conversation.java                # JPA Entity: One-to-Many association
│   ├── ChatMessage.java                 # JPA Entity: Many-to-One association
│   ├── ExtractedDocument.java           # In-memory document model
│   └── ExtractedPage.java               # Page tracking for citations
├── rag
│   ├── TextChunk.java                   # Chunk data carrier
│   ├── TextChunkingService.java         # Overlapping sliding window segmentation
│   └── KeywordSimilaritySearchService.java # Pure Java TF-IDF ranking algorithm
├── repository
│   ├── ChatMessageRepository.java       # Spring Data JPA Repository
│   └── ConversationRepository.java      # Spring Data JPA Repository
└── service
    ├── ChatService.java                 # Conversational workflow orchestrator
    ├── DocumentExportService.java       # Dynamic PDF & Excel document generator
    ├── DocumentIngestionService.java    # Apache PDFBox text extraction
    ├── PromptService.java               # Centralized prompt templates
    ├── StudyToolsService.java           # Revision tools (MCQs, Flashcards, Summary)
    └── YouTubeSummaryService.java       # YouTube lecture notes parser
```

### Applied OOP & Design Principles:
- **Abstraction & DIP:** `AIService` interface decouples controllers from AI network logic.
- **Encapsulation:** Entities protect internal state with validated getters and setters.
- **Composition & Association:** `Conversation` maintains an encapsulated collection of `ChatMessage` objects (`@OneToMany`).
- **Single Responsibility Principle (SRP):** Dedicated services for ingestion, chunking, retrieval, prompt generation, and export.
- **Defensive Programming:** Comprehensive validation and `@ControllerAdvice` prevent raw stack traces from reaching clients.

---

## 🔒 Privacy & Enterprise Security Hardening

- **Zero API Key Exposure:** API keys are never committed or exposed. Injected dynamically via environment variable `${GEMINI_API_KEY}`.
- **Spring Security & Role-Based Access Control:** Administrative routes and the embedded H2 Console are guarded behind `ROLE_ADMIN` authentication.
- **BCrypt Password Hashing:** User passwords are encrypted with BCrypt (12 rounds) — zero plaintext storage.
- **DoS & AI Quota Rate Limiting:** `RateLimitingFilter` enforces a token bucket per client IP (120 req/min general, 30 req/min for AI endpoints), preventing abuse and DoS.
- **XSS & Prompt Injection Defense:** All user inputs and chat prompts pass through `InputSanitizer` (HTML stripping via Jsoup `Safelist.none()`).
- **CSV / Formula Injection Neutralization:** Notes exported to Excel beginning with `=`, `+`, `-`, or `@` are safely escaped.
- **Path Traversal & SSRF Prevention:** Filenames are strictly sanitized, and YouTube video summarization enforces validated YouTube domain whitelisting.
- **Hardened Security Headers:** Strict CSP, `X-Frame-Options: SAMEORIGIN`, `X-Content-Type-Options: nosniff`, and Referrer-Policy applied across all responses.
- **Production Error Masking:** Internal stack traces and binding exceptions are strictly hidden from users.
- **Local-Only Database:** Uploaded lecture materials and conversation histories remain strictly on the host machine in `./data/studymind_db` and are excluded from Git via `.gitignore`.

---

## 💻 Tech Stack

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.4 (Spring Web, Spring Data JPA, Thymeleaf)
- **Build Tool:** Apache Maven 3.9+
- **Database:** Embedded H2 Database (File-backed: `./data/studymind_db`)
- **PDF Extraction:** Apache PDFBox 3.0.3
- **Excel Generation:** Apache POI 5.3.0
- **AI Integration:** Google Gemini API via `java.net.http.HttpClient`
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (AJAX)

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java JDK 21+** installed (`java -version`).
- **Apache Maven 3.9+** installed (`mvn -version`).
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/) (free).

### 2. Configure Environment Variable

**PowerShell (Windows):**
```powershell
$env:GEMINI_API_KEY="your_gemini_api_key_here"
```

**Command Prompt (Windows):**
```cmd
set GEMINI_API_KEY=your_gemini_api_key_here
```

**Bash / Zsh (Linux / macOS):**
```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
```

### 3. Run the Application

**Option A — One-Click (Windows):**
Double-click `run.bat` or run:
```cmd
run.bat
```

**Option B — Maven Command:**
```bash
mvn spring-boot:run
```

### 4. Access the Application
- **Main Web Interface:** [http://localhost:8080](http://localhost:8080)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/studymind_db`
  - Username: `sa`
  - Password: *(leave blank)*

---

## 📚 API Endpoints Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Main Chat & Study Dashboard |
| `POST` | `/api/chat/send` | Send question and receive context-grounded AI reply |
| `POST` | `/api/documents/upload` | Upload PDF or TXT study file |
| `GET` | `/api/documents/status` | Get active document metadata and page count |
| `POST` | `/api/tools/summary` | Generate structured revision summary |
| `POST` | `/api/tools/mcqs` | Generate 5 multiple-choice questions |
| `POST` | `/api/tools/flashcards` | Generate 5 active-recall flashcards |
| `POST` | `/api/modern/youtube-summary` | Summarize educational YouTube video |
| `POST` | `/api/modern/export/pdf` | Export study notes as a PDF file |
| `POST` | `/api/modern/export/excel` | Export study topics as an Excel spreadsheet |

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
