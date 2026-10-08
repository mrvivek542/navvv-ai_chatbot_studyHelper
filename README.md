# StudyMind AI — Java-Powered Study Assistant 🧠

An interactive, intelligent study assistant built with **Java 21**, **Spring Boot**, and **Apache PDFBox**, designed for students to revise course materials, engage in multi-turn tutor dialogues, and perform context-grounded Retrieval-Augmented Generation (RAG) with page-level citations.

Developed for the **GUVI – Galgotias University Project Board** (B.Tech AIML, 3rd Semester).

---

## 🌟 Key Features

1. **AI Academic Tutor (Multi-turn Chat):**
   - Natural, encouraging academic mentor dialogue.
   - Conversation history persisted in an embedded file-based H2 database using Spring Data JPA.
   - Multi-turn context preservation (follow-up doubts retain earlier conversational context).

2. **Study Material Upload & Extraction:**
   - Supports **PDF** and **TXT** lecture notes / textbook files up to **5 MB**.
   - Page-by-page text extraction powered by **Apache PDFBox 3.x**.
   - Real-time client-side and server-side validation with friendly error messages.

3. **Context-Grounded RAG (Retrieval-Augmented Generation):**
   - Pure Java **Chunking Engine** (~500 words with 50-word sliding window overlap).
   - In-memory **TF-IDF & Keyword Similarity Search** (no heavy external vector DB required).
   - Strict source citations: `[Source: <filename>, Page <pageNumber>]`.
   - **Anti-Hallucination Guardrail:** If an answer cannot be grounded in the student's file, it politely responds:
     > *"I couldn't find enough information in your uploaded sources."*

4. **Active Recall Revision Tools:**
   - **📝 Summary:** Structured overview, key definitions, and exam takeaways.
   - **🎯 5 MCQs:** Multiple-choice quiz questions with option distractors and explanations.
   - **⚡ 5 Flashcards:** Front (Concept/Question) and Back (Concise Answer) for rapid self-testing.

5. **Aesthetic Dark Theme Interface:**
   - Pinterest-inspired sleek slate & indigo dark UI.
   - Responsive layout with collapsible study session sidebar, live typing indicators, and smooth transitions.

---

## 🏗️ Architecture & OOP Design

The project strictly follows Clean Architecture and Object-Oriented Design principles:

```
com.studymind
├── ai
│   ├── AIService.java                   # Interface: Abstraction & DIP
│   └── GeminiAIService.java             # Implementation: java.net.http.HttpClient
├── controller
│   ├── HomeController.java              # MVC view routing
│   ├── ChatApiController.java          # REST: Conversation & messaging
│   ├── DocumentApiController.java      # REST: File upload & status
│   ├── SearchApiController.java        # REST: Diagnostic chunk search
│   └── StudyToolsApiController.java    # REST: Summary, MCQs, Flashcards
├── dto
│   ├── ChatRequest.java                 # Encapsulation of request payloads
│   ├── ChatResponse.java                # Response carrier with factory methods
│   └── DocumentUploadResponse.java      # Upload metadata carrier
├── exception
│   └── GlobalExceptionHandler.java      # AOP: Centralized error handling
├── model
│   ├── Conversation.java                # JPA Entity: One-to-Many association
│   ├── ChatMessage.java                 # JPA Entity: Many-to-One association
│   ├── ExtractedDocument.java           # In-memory document model
│   └── ExtractedPage.java               # Page tracking for citations
├── rag
│   ├── TextChunk.java                   # Chunk data carrier
│   ├── TextChunkingService.java         # Overlapping sliding window segmentation
│   └── KeywordSimilaritySearchService.java # Pure Java TF-IDF ranking algorithm
└── service
    ├── ChatService.java                 # Business orchestration
    ├── DocumentIngestionService.java    # Apache PDFBox text extraction
    ├── PromptService.java               # Centralized prompt templates
    └── StudyToolsService.java           # Active recall generation
```

### OOP Concepts Applied:
- **Abstraction & Polymorphism:** `AIService` decouples high-level controllers from low-level AI network calls.
- **Encapsulation:** Entities protect internal state and use getters/setters.
- **Composition & Association:** `Conversation` has a collection of `ChatMessage` objects (`@OneToMany`).
- **Single Responsibility Principle (SRP):** Distinct services for Ingestion, Chunking, Search, Prompts, and Chat.
- **Defensive Programming:** Robust validation and `@ControllerAdvice` prevent raw stack traces from reaching users.

---

## 💻 Tech Stack

- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.3.4 (Web, Thymeleaf, Spring Data JPA)
- **Build Tool:** Apache Maven 3.9.9
- **Database:** Embedded H2 Database (File-backed: `./data/studymind_db`)
- **PDF Extraction:** Apache PDFBox 3.0.3
- **AI Integration:** Google Gemini API via `java.net.http.HttpClient`
- **Frontend:** HTML5, CSS3 (Modern Dark Theme), Vanilla JavaScript (AJAX)

---

## 🚀 How to Run Locally

### 1. Prerequisites
- Java JDK 21+ installed and configured on your PATH (`java -version`).
- Apache Maven (a portable Maven is also included in this workspace).
- A free Gemini API key from [Google AI Studio](https://aistudio.google.com/).

### 2. Configure Environment Variable
Set your Gemini API key in your terminal session (never commit keys to git):

**PowerShell (Windows):**
```powershell
$env:GEMINI_API_KEY="your_gemini_api_key_here"
```

**Bash (Linux / macOS):**
```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
```

### 3. Build & Run Application
```powershell
# Compile the project
mvn clean compile

# Start the Spring Boot application
mvn spring-boot:run
```

### 4. Access the App
Open your browser and navigate to:
- **Web Dashboard:** [http://localhost:8080](http://localhost:8080)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:file:./data/studymind_db`
  - Username: `sa`
  - Password: *(leave blank)*

---

## 🔮 Future Scope
- Transition from keyword TF-IDF to dense vector embeddings using `pgvector` or Milvus.
- Multi-document cross-comparison and synthesis.
- Audio flashcard playback using Web Speech API.
