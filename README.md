# Portfolio Summariser & Optimiser

Full-stack **Mutual Fund Portfolio Summariser and Optimisation Suggestion Tool**. No login/signup: users fill a profile, import their MF portfolio (PDF or CAMS/KFintech), and get AI-powered analysis and suggestions via Claude (Anthropic API).

## Project structure

```
portfolio optimiser/
├── backend/     # Java 17 + Spring Boot 3.x
└── frontend/    # React + Vite + Tailwind
```

## Prerequisites

- **Java 17**
- **Node.js 18+** and npm
- **MongoDB** (local or Atlas) running and reachable
- **Anthropic API key** for Claude

## Backend setup

1. Open `backend/src/main/resources/application.properties`.
2. Set:
   - `spring.data.mongodb.uri` — your MongoDB URI (e.g. `mongodb://localhost:27017/portfolio_optimizer`).
   - `anthropic.api.key` — your Claude API key (or set env var `ANTHROPIC_API_KEY`).
3. From the `backend` directory:

   ```bash
   mvn spring-boot:run
   ```

   Backend runs at **http://localhost:8080**. CORS is allowed for `http://localhost:5173`.

## Frontend setup

1. Copy env example and set API URL if needed:

   ```bash
   cd frontend
   cp .env.example .env
   # Edit .env: VITE_API_BASE_URL=http://localhost:8080
   ```

2. Install and run:

   ```bash
   npm install
   npm run dev
   ```

   Frontend runs at **http://localhost:5173**.

## Accurate portfolio data (recommended)

PDF parsing can be error-prone. For **100% accurate data** use either:

- **Manual entry** — On Import, choose "Manual / CSV (accurate)" → "Enter manually". Add each fund with Fund name, Units, Current NAV, Current value (₹), Invested amount (₹), and Category. Save to go to Dashboard.
- **CSV upload** — Same tab → "Upload CSV". Use the provided template (`/portfolio_template.csv`) or a CSV with columns: `fundName, units, currentNav, currentValue, investedAmount, folioNumber, category`. First row can be a header.

For **accurate parsing of CAS PDFs** (CAMS/KFintech), you can integrate a third-party API such as [CAS Parser](https://casparser.in/) (paid), which returns structured JSON from the same PDF.

## User flow

1. **Profile** (`/`) — Name, age, income, monthly investment, risk appetite → submit → `sessionId` saved in MongoDB and in browser `localStorage`.
2. **Import** (`/import`) — **Manual/CSV** (accurate), **Upload PDF**, or **Connect via PAN** → portfolio saved → go to Dashboard.
3. **Dashboard** (`/dashboard`) — Summary cards (invested, value, P&L, XIRR), asset allocation pie chart, category bar chart, fund-wise table. Button **Analyse My Portfolio** → Suggestions.
4. **Suggestions** (`/suggestions`) — Trigger analysis → backend calls Claude with profile + portfolio → response shown in sections (Summary, Risk, Rebalancing, Alternatives, Tax, Goals).

## API overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/user/profile` | Submit profile; returns `{ sessionId }` |
| GET    | `/api/user/profile/{sessionId}` | Get profile by session |
| POST   | `/api/portfolio/upload-pdf` | Form: `file`, `sessionId` |
| POST   | `/api/portfolio/fetch-cams` | Body: `{ pan, sessionId }` |
| GET    | `/api/portfolio/{sessionId}` | Get portfolio with computed metrics |
| POST   | `/api/suggestions/analyse` | Body: `{ sessionId }`; returns AI suggestion |

## Currency

All monetary values are in **Indian Rupees (₹)**.

## Env / secrets

- **Backend:** `application.properties` (and/or env vars) for MongoDB URI and `anthropic.api.key`.
- **Frontend:** `.env` with `VITE_API_BASE_URL` (default `http://localhost:8080`).

Add your **Claude API key** and **MongoDB URI** before running end-to-end.
