import { Routes, Route, NavLink, useNavigate } from "react-router-dom";
import ProfilePage from "./pages/ProfilePage";
import ImportPage from "./pages/ImportPage";
import DashboardPage from "./pages/DashboardPage";
import SuggestionsPage from "./pages/SuggestionsPage";
import { getSessionId } from "./api";

function App() {
  const navigate = useNavigate();
  const sessionId = getSessionId();

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-slate-800 bg-black/60 backdrop-blur sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-xl font-semibold tracking-tight">
              Portfolio <span className="text-accentGreen">Optimiser</span>
            </span>
            <span className="ml-3 text-xs text-slate-400 border border-slate-700 rounded px-2 py-0.5">
              Mutual Funds · India
            </span>
          </div>
          <nav className="flex items-center gap-4 text-sm">
            <NavLink
              to="/"
              className={({ isActive }) =>
                `hover:text-accentGreen ${isActive ? "text-accentGreen" : "text-slate-300"}`
              }
            >
              Profile
            </NavLink>
            <NavLink
              to="/import"
              className={({ isActive }) =>
                `hover:text-accentGreen ${isActive ? "text-accentGreen" : "text-slate-300"}`
              }
              onClick={(e) => {
                if (!sessionId) {
                  e.preventDefault();
                  navigate("/");
                }
              }}
            >
              Import
            </NavLink>
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `hover:text-accentGreen ${isActive ? "text-accentGreen" : "text-slate-300"}`
              }
              onClick={(e) => {
                if (!sessionId) {
                  e.preventDefault();
                  navigate("/");
                }
              }}
            >
              Dashboard
            </NavLink>
            <NavLink
              to="/suggestions"
              className={({ isActive }) =>
                `hover:text-accentGreen ${isActive ? "text-accentGreen" : "text-slate-300"}`
              }
              onClick={(e) => {
                if (!sessionId) {
                  e.preventDefault();
                  navigate("/");
                }
              }}
            >
              Suggestions
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="flex-1">
        <div className="max-w-6xl mx-auto px-4 py-6">
          <Routes>
            <Route path="/" element={<ProfilePage />} />
            <Route path="/import" element={<ImportPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/suggestions" element={<SuggestionsPage />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

export default App;

