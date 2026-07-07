# SpringGen AI 🚀 - Spring Boot Code Generator
demo : (https://springgen-ai.onrender.com/)

SpringGen AI is an AI-powered code generation application that creates a fully-configured Spring Boot project structure (packaged in a downloadable ZIP file) based on a simple natural language prompt. 

It leverages the **Google Gemini API** to analyze the requirements, map project metadata, design entity models, and generate complete code files. If the Gemini API is not configured, it seamlessly falls back to an offline local parser.

---

## 🏗️ Architecture & Tech Stack

The project is structured as a monorepo containing a React frontend and a Spring Boot backend:

```
Spring-Gen-Ai/
├── backend/            # Spring Boot REST API
├── frontend/           # React + Vite Web UI
├── Dockerfile          # Multi-stage production build (Frontend + Backend)
├── render.yaml         # Render deployment blueprint
└── run-mysql.ps1       # Optional Sandboxed MySQL launcher
```

### Backend
- **Framework**: Java 21, Spring Boot 3.4.x
- **Build Tool**: Apache Maven
- **Database**: H2 (In-memory database for rapid development)
- **Security**: Spring Security (CORS enabled for cross-origin local and cloud requests)
- **API Endpoints**: 
  - `POST /api/projects/analyze` - Parses prompt and generates structure metadata
  - `POST /api/projects/preview` - Generates individual code files for live preview
  - `POST /api/projects/download` - Compiles and downloads the complete project as a `.zip` file

### Frontend
- **Framework**: React 19 + Vite 8
- **Styling**: Vanilla CSS (Modern, responsive layout with loading spinners, code previewers, and visual configuration editing)
- **Routing/HTTP**: Fetches dynamically routing based on host environment (bypasses localtunnel reminders automatically)

---

## 🛠️ Local Development Setup

### Prerequisites
- **Java**: JDK 21
- **Node.js**: Node 18+ and `npm`
- **Maven**: Maven 3.9+ (or use the bundled Maven in your IDE)

---

### 1. Running the Backend

1. Navigate to the `backend` folder:
   ```bash
   cd backend
   ```
2. *(Optional)* Set your Google Gemini API Key:
   ```bash
   # Linux/macOS
   export GEMINI_API_KEY="your-api-key"
   
   # Windows PowerShell
   $env:GEMINI_API_KEY="your-api-key"
   ```
   > [!NOTE]
   > If the key is not set, the app will log a warning and fallback to a local parser offline.

3. Run the Spring Boot application:
   ```bash
   # Using local Maven
   mvn spring-boot:run
   
   # Using IntelliJ bundled Maven (Windows PowerShell)
   & "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
   ```
4. The server will start on **`http://localhost:8080`**.
   - API Docs: `http://localhost:8080/v3/api-docs`
   - H2 Database Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: *empty*)

---

### 2. Running the Frontend

1. Navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   # Standard
   npm run dev
   
   # If Windows execution policy restricts running .ps1 scripts:
   npm.cmd run dev
   ```
4. Access the web app at **`http://localhost:5173/SpringGen-AI/`**.

---

## 🚀 Deployment Guide

### Monolith Production Build (Docker)
The root repository contains a multi-stage `Dockerfile` which:
1. Builds the React frontend.
2. Copies the static distribution assets into the Spring Boot resource folder (`/static`).
3. Builds the Java JAR file containing both backend and frontend.

To build and run the Docker container locally:
```bash
# Build the image
docker build -t springgen-ai .

# Run the container
docker run -p 8080:8080 -e GEMINI_API_KEY="your-api-key" springgen-ai
```

### Deploying the Backend (Render)
You can deploy the backend using the pre-configured [render.yaml](file:///d:/springbot/render.yaml) file:
1. Connect your GitHub repository to Render.
2. Render will automatically parse the blueprint and configure a Docker-based Web Service.
3. Configure the `GEMINI_API_KEY` under environment variables in Render.

### Deploying the Frontend (Vercel)
If you deploy the static frontend separately to Vercel:
1. Import your project repository to Vercel.
2. Go to the project **Settings** > **General**.
3. Set the **Root Directory** to `frontend`.
4. Add the following **Environment Variable**:
   - **Key**: `VITE_API_BASE_URL`
   - **Value**: `https://your-backend-api-url.com/api/projects` (Your active deployed backend endpoint)
5. Deploy/Redeploy the project.
