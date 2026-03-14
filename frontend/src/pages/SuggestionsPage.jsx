import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api, { getSessionId } from "../api";

const SECTION_ORDER = [
  "Portfolio Summary",
  "Risk Analysis",
  "Rebalancing Advice",
  "Better Fund Alternatives",
  "Tax Optimisation",
  "Goal-based Planning",
];

function parseSections(text) {
  if (!text || typeof text !== "string") return [];
  const sections = [];
  const regex = /(?:^|\n)\s*(#{1,3}\s*)?([A-Za-z\s\-]+)(?:\s*—|\s*:)?\s*[\r\n]+/g;
  let lastEnd = 0;
  let match;
  const titles = [];
  while ((match = regex.exec(text)) !== null) {
    const title = match[2].trim();
    if (SECTION_ORDER.some((s) => title.toLowerCase().includes(s.toLowerCase()))) {
      titles.push({ title, start: match.index, end: match.index + match[0].length });
    }
  }
  for (let i = 0; i < titles.length; i++) {
    const start = titles[i].end;
    const end = i + 1 < titles.length ? titles[i + 1].start : text.length;
    sections.push({
      title: titles[i].title,
      body: text.slice(start, end).trim(),
    });
  }
  if (sections.length === 0 && text.trim()) {
    sections.push({ title: "Analysis", body: text.trim() });
  }
  return sections;
}

export default function SuggestionsPage() {
  const navigate = useNavigate();
  const sessionId = getSessionId();
  const [suggestion, setSuggestion] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [analysed, setAnalysed] = useState(false);

  useEffect(() => {
    if (!sessionId) {
      navigate("/", { replace: true });
      return;
    }
  }, [sessionId, navigate]);

  const runAnalysis = async () => {
    setError("");
    setLoading(true);
    setSuggestion(null);
    try {
      const { data } = await api.post("/api/suggestions/analyse", { sessionId });
      setSuggestion(data);
      setAnalysed(true);
    } catch (err) {
      setError(
        err.response?.data?.message || err.message || "Analysis failed. Check backend and API key."
      );
    } finally {
      setLoading(false);
    }
  };

  if (!sessionId) return null;

  const sections = suggestion?.summary ? parseSections(suggestion.summary) : [];
  const hasStored =
    suggestion?.riskAnalysis ||
    suggestion?.rebalancing ||
    suggestion?.alternatives ||
    suggestion?.taxTips ||
    suggestion?.goalPlanning;

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-semibold text-white mb-2">AI Suggestions</h1>
      <p className="text-slate-400 text-sm mb-6">
        Get a narrative analysis, risk view, rebalancing tips, alternatives, tax tips, and
        goal-based planning.
      </p>

      {!suggestion && !loading && (
        <div className="card text-center py-12">
          <p className="text-slate-400 mb-4">Run analysis to see AI-powered suggestions.</p>
          <button onClick={runAnalysis} className="btn-primary">
            Analyse My Portfolio
          </button>
        </div>
      )}

      {loading && (
        <div className="card space-y-4">
          <div className="h-4 bg-slate-700 rounded animate-pulse w-3/4" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-full" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-5/6" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-full" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-4/5" />
          <div className="h-8 bg-slate-700 rounded animate-pulse w-1/2 mt-6" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-full" />
          <div className="h-4 bg-slate-700 rounded animate-pulse w-2/3" />
        </div>
      )}

      {error && (
        <div className="card border-accentRed/50">
          <p className="text-accentRed">{error}</p>
          <button onClick={runAnalysis} className="btn-secondary mt-4">
            Retry Analysis
          </button>
        </div>
      )}

      {suggestion && !loading && (
        <div className="space-y-6">
          {sections.length > 0 ? (
            sections.map((sec, i) => (
              <div key={i} className="card">
                <h2 className="text-lg font-medium text-accentGreen mb-3">{sec.title}</h2>
                <div className="text-slate-300 text-sm whitespace-pre-wrap leading-relaxed">
                  {sec.body}
                </div>
              </div>
            ))
          ) : hasStored ? (
            <>
              {suggestion.summary && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">Portfolio Summary</h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">{suggestion.summary}</div>
                </div>
              )}
              {suggestion.riskAnalysis && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">Risk Analysis</h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">
                    {suggestion.riskAnalysis}
                  </div>
                </div>
              )}
              {suggestion.rebalancing && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">Rebalancing Advice</h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">
                    {suggestion.rebalancing}
                  </div>
                </div>
              )}
              {suggestion.alternatives && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">
                    Better Fund Alternatives
                  </h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">
                    {suggestion.alternatives}
                  </div>
                </div>
              )}
              {suggestion.taxTips && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">Tax Optimisation</h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">
                    {suggestion.taxTips}
                  </div>
                </div>
              )}
              {suggestion.goalPlanning && (
                <div className="card">
                  <h2 className="text-lg font-medium text-accentGreen mb-3">Goal-based Planning</h2>
                  <div className="text-slate-300 text-sm whitespace-pre-wrap">
                    {suggestion.goalPlanning}
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="card">
              <h2 className="text-lg font-medium text-accentGreen mb-3">Analysis</h2>
              <div className="text-slate-300 text-sm whitespace-pre-wrap">
                {suggestion.summary || "No structured sections found."}
              </div>
            </div>
          )}
          <div className="flex justify-end">
            <button onClick={runAnalysis} className="btn-secondary">
              Re-run Analysis
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
