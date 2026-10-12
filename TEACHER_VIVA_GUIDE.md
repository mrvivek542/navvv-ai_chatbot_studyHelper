# 🎓 StudyMind AI — Teacher & Viva Presentation Guide

This guide is designed to help you explain **StudyMind AI** clearly to your professors, evaluators, and project examiners. It covers the core concept, OOP design, step-by-step technical workflow, and answers to common viva questions.

---

## 📌 1. One-Minute Project Pitch (Elevator Pitch)
> *"StudyMind AI is an interactive academic assistant built with Java 21, Spring Boot, and Google Gemini API. It allows students to upload textbooks and lecture notes (PDF/TXT), ask questions in natural language, and receive grounded answers with exact page-level citations. Unlike general chatbots that make up answers, our system uses an in-memory TF-IDF Retrieval-Augmented Generation (RAG) engine with an anti-hallucination guardrail to guarantee academic honesty. It also features one-click study tools like flashcards, MCQs, structured summaries, and YouTube lecture summarization."*

---

## 💡 2. Why Did We Build This? (Problem Statement)
- **Problem 1 (Information Overload):** Students struggle to quickly review 100+ page PDFs before exams.
- **Problem 2 (AI Hallucination):** Standard AI models often invent facts or formulas without citing source materials.
- **Problem 3 (Heavy Dependencies):** Most AI projects require external Python environments or paid cloud vector databases.
- **Solution:** StudyMind AI provides a **100% Java-native, lightweight, secure** study helper that runs locally, requires no external vector database, and grounds every answer directly in the student's materials.

---

## 🏗️ 3. OOP Concepts & Clean Architecture (Teacher Evaluation)

Teachers frequently ask how Object-Oriented Programming (OOP) and Software Engineering principles were applied. Here is how our codebase implements them:

| OOP Concept | Code Implementation | Explanation |
| :--- | :--- | :--- |
| **Abstraction** | `AIService.java` (Interface) | Defines the contract `generateResponse(prompt)` without exposing network or HTTP implementation details. |
| **Polymorphism** | `GeminiAIService.java` implements `AIService` | The chat service depends on the `AIService` interface. If tomorrow we switch from Gemini to Claude or OpenAI, controllers do not need to change. |
| **Encapsulation** | `Conversation`, `ChatMessage`, `TextChunk` | Internal state and fields are `private` with validated getters and setters. Sensitive logic is contained within dedicated services. |
| **Composition & Association** | `Conversation` and `ChatMessage` | A `Conversation` *has-a* collection of `ChatMessage` objects mapped using `@OneToMany(mappedBy = "conversation")`. |
| **Single Responsibility (SRP)** | Service Layer Separation | Distinct services for ingestion (`DocumentIngestionService`), chunking (`TextChunkingService`), search (`KeywordSimilaritySearchService`), and prompt generation (`PromptService`). |
| **Dependency Injection (IoC)** | Spring Boot `@Autowired` / Constructor Injection | Spring manages lifecycle and injects dependencies, keeping classes loosely coupled and unit-testable. |

---

## 🔄 4. How the RAG Pipeline Works (Step-by-Step Flow)

Explain this sequence when demonstrating the project to your teacher:

```
[Student Uploads PDF]
       ↓
1. DocumentIngestionService (Apache PDFBox 3.x extracts text page-by-page)
       ↓
2. TextChunkingService (Splits text into ~500-word segments with 50-word sliding overlap)
       ↓
3. KeywordSimilaritySearchService (Calculates TF-IDF scores between student query and chunks)
       ↓
4. Top Ranked Chunks (Extracts top 3 most relevant segments + page numbers)
       ↓
5. PromptService (Injects relevant text + strict anti-hallucination prompt guardrail)
       ↓
6. GeminiAIService (Pure Java HttpClient calls Google Gemini API)
       ↓
7. Response Formatted (Delivers answer with citations: [Source: Notes.pdf, Page 4])
```

---

## 🗄️ 5. Database & Data Persistence
- **Database:** Embedded **H2 Database** configured in persistent file mode (`./data/studymind_db`).
- **Why H2?** It requires zero external database installation, starts automatically with the Spring Boot application, and saves conversation history across restarts.
- **Entities:**
  - `Conversation`: Tracks session ID, title, and creation timestamp.
  - `ChatMessage`: Stores role (`user` vs `assistant`), content, source citations, and timestamps.

---

## 🛡️ 6. Enterprise Privacy & Defensive Security Hardening
Our application implements multi-layered defensive security across all tiers:

1. **Role-Based Access Control (RBAC) & Spring Security:**
   - Administrative paths and `/h2-console/**` are strictly protected behind `ROLE_ADMIN` authentication.
   - Passwords are encrypted with **BCrypt (12 hashing rounds)**; plaintext passwords are never stored.
2. **Denial-of-Service (DoS) & Quota Rate Limiting:**
   - `RateLimitingFilter` enforces a token-bucket limit per client IP (120 req/min general, 30 req/min for AI-heavy endpoints).
   - Prevents API budget exhaustion and brute-force attacks by returning HTTP 429 (`Too Many Requests`).
3. **Cross-Site Scripting (XSS) & Input Sanitization:**
   - User inputs and chat prompts are sanitized using `InputSanitizer` powered by `Jsoup.clean(..., Safelist.none())`, stripping all injected JavaScript and HTML.
4. **Formula & CSV Injection Defense:**
   - Exported spreadsheet cells beginning with `=`, `+`, `-`, or `@` are sanitized with `'` to neutralize formula execution in Microsoft Excel.
5. **Path Traversal & SSRF Prevention:**
   - Uploaded filenames are stripped of directory traversal sequences (`..`, `/`, `\`).
   - YouTube summarization verifies valid protocols and restricts hostnames strictly to YouTube domains.
6. **Hardened Security Headers:**
   - `Content-Security-Policy (CSP)`: Restricts script and resource loading to trusted CDNs and origin.
   - `X-Frame-Options: SAMEORIGIN`: Prevents clickjacking.
   - `X-Content-Type-Options: nosniff`: Prevents MIME-type sniffing.
   - `Referrer-Policy: strict-origin-when-cross-origin`.
7. **Environment Variable Isolation:**
   - Real Gemini API keys and database credentials are read strictly from OS environment variables (`$env:GEMINI_API_KEY`).
   - Repository includes `.env.example` as a safe template. Git commit logs have been verified to contain zero leaked secrets.
8. **Production Debug Mode Disabled:**
   - Server stack trace leakage in HTTP error responses is disabled (`server.error.include-stacktrace=never`).
   - H2 web console remote access is disabled (`web-allow-others=false`).

---

## ❓ 7. Top 7 Viva & Examiner Questions (With Ideal Answers)

### Q1: What is RAG and why did you choose it over fine-tuning a model?
> **Answer:** *"Retrieval-Augmented Generation (RAG) dynamically searches relevant textbook chunks and provides them as context to the AI at prompt time. We chose RAG because fine-tuning is expensive, takes hours to train, and risks hallucinating. RAG allows real-time document updates, zero training costs, and exact page-level citations."*

### Q2: Why did you use pure Java TF-IDF instead of a vector database like Chroma or Pinecone?
> **Answer:** *"For student study sessions with documents up to 5–20 MB, external vector databases add unnecessary overhead, require Docker or Python sidecars, and consume heavy memory. A pure Java TF-IDF implementation runs in-memory in milliseconds with zero dependencies, keeping the application fast, lightweight, and self-contained."*

### Q3: How do you handle sliding window chunking and why is overlap important?
> **Answer:** *"We segment text into ~500-word chunks with a 50-word sliding window overlap (`TextChunkingService`). The overlap ensures that critical sentences spanning across segment boundaries are not split in half, preserving semantic context."*

### Q4: How does multi-turn conversational memory work?
> **Answer:** *"When a student asks a follow-up question, `ChatService` retrieves the previous messages of the active `Conversation` entity from the H2 database and appends them to the prompt history. This allows the AI to remember earlier doubts in the conversation."*

### Q5: What happens if a student asks an off-topic question about an uploaded document?
> **Answer:** *"Our system checks the similarity threshold score. If no relevant chunks match the query, the AI guardrail triggers: 'I couldn't find enough information in your uploaded sources.' This prevents the model from generating unverified or hallucinated facts."*

### Q6: What library did you use for PDF extraction?
> **Answer:** *"We used Apache PDFBox 3.0.3. It iterates through the PDF document page-by-page using `PDFTextStripper`, storing the page numbers so that every chunk maintains an exact citation reference."*

### Q7: What additional features does StudyMind AI have beyond chatting?
> **Answer:** *"It includes active-recall revision tools: one-click generation of structured revision summaries, 5 multiple-choice questions with answer keys, 5 concept flashcards, YouTube video lecture summarization, and export of notes to downloadable PDF and Excel files."*
