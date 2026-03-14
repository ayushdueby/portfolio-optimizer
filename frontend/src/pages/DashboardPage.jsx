import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
} from "recharts";
import api, { getSessionId } from "../api";
import { formatRupee, formatPercent } from "../utils/format";

const PIE_COLORS = ["#22c55e", "#3b82f6", "#f59e0b", "#8b5cf6"];

export default function DashboardPage() {
  const navigate = useNavigate();
  const sessionId = getSessionId();
  const [portfolio, setPortfolio] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!sessionId) {
      navigate("/", { replace: true });
      return;
    }
    api
      .get(`/api/portfolio/${sessionId}`)
      .then(({ data }) => setPortfolio(data))
      .catch((err) => setError(err.response?.data?.message || err.message || "Failed to load portfolio."))
      .finally(() => setLoading(false));
  }, [sessionId, navigate]);

  const handleAnalyse = () => {
    navigate("/suggestions");
  };

  if (!sessionId) return null;
  if (loading) {
    return (
      <div className="space-y-4">
        <div className="h-24 bg-slate-800 rounded-xl animate-pulse" />
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-slate-800 rounded-xl animate-pulse" />
          ))}
        </div>
      </div>
    );
  }
  if (error || !portfolio) {
    return (
      <div className="card">
        <p className="text-accentRed">{error || "No portfolio data. Import first."}</p>
        <button onClick={() => navigate("/import")} className="btn-secondary mt-4">
          Go to Import
        </button>
      </div>
    );
  }

  const holdings = portfolio.holdings || [];
  const allocation = portfolio.assetAllocation || {};
  const pieData = Object.entries(allocation).map(([name, value]) => ({ name, value }));
  const categoryData = Object.entries(portfolio.categoryBreakdown || allocation).map(
    ([name, value]) => ({ name, value })
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-white">Portfolio Dashboard</h1>
        <button onClick={handleAnalyse} className="btn-primary">
          Analyse My Portfolio
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="card">
          <p className="text-slate-400 text-sm">Total Invested</p>
          <p className="text-xl font-semibold text-white mt-1">
            {formatRupee(portfolio.totalInvestedAmount)}
          </p>
        </div>
        <div className="card">
          <p className="text-slate-400 text-sm">Current Value</p>
          <p className="text-xl font-semibold text-white mt-1">
            {formatRupee(portfolio.currentPortfolioValue)}
          </p>
        </div>
        <div className="card">
          <p className="text-slate-400 text-sm">Gain / Loss</p>
          <p className={`text-xl font-semibold mt-1 ${
            (portfolio.overallGainLossAmount ?? 0) >= 0 ? "text-accentGreen" : "text-accentRed"
          }`}>
            {formatRupee(portfolio.overallGainLossAmount)} ({formatPercent(portfolio.overallGainLossPercent)})
          </p>
        </div>
        <div className="card">
          <p className="text-slate-400 text-sm">XIRR</p>
          <p className="text-xl font-semibold text-white mt-1">
            {formatPercent(portfolio.overallXirr)}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h2 className="text-lg font-medium text-white mb-4">Asset Allocation</h2>
          {pieData.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                >
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(v) => formatRupee(v)} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-slate-500 text-sm py-8 text-center">No allocation data</p>
          )}
        </div>
        <div className="card">
          <h2 className="text-lg font-medium text-white mb-4">Category Breakdown</h2>
          {categoryData.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={categoryData} layout="vertical" margin={{ left: 20, right: 20 }}>
                <XAxis type="number" tickFormatter={(v) => `₹${(v / 1e5).toFixed(1)}L`} />
                <YAxis type="category" dataKey="name" width={80} />
                <Tooltip formatter={(v) => formatRupee(v)} />
                <Bar dataKey="value" fill="#3b82f6" name="Value" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-slate-500 text-sm py-8 text-center">No category data</p>
          )}
        </div>
      </div>

      <div className="card overflow-hidden">
        <h2 className="text-lg font-medium text-white mb-4">Fund-wise Performance</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-400 border-b border-slate-700">
                <th className="pb-2 pr-4">Fund Name</th>
                <th className="pb-2 pr-4">Category</th>
                <th className="pb-2 pr-4 text-right">Invested</th>
                <th className="pb-2 pr-4 text-right">Current Value</th>
                <th className="pb-2 pr-4 text-right">Gain/Loss</th>
                <th className="pb-2 pr-4 text-right">XIRR</th>
                <th className="pb-2 pr-4 text-right">Units</th>
                <th className="pb-2 text-right">NAV</th>
              </tr>
            </thead>
            <tbody>
              {holdings.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-8 text-slate-500 text-center">
                    No holdings. Import a portfolio first.
                  </td>
                </tr>
              ) : (
                holdings.map((h, i) => (
                  <tr
                    key={i}
                    className={`border-b border-slate-800/50 ${
                      (h.gainLossAmount ?? 0) >= 0 ? "text-accentGreen" : "text-accentRed"
                    }`}
                  >
                    <td className="py-3 pr-4 text-white font-medium">{h.fundName || "—"}</td>
                    <td className="py-3 pr-4 text-slate-300">{h.category || "—"}</td>
                    <td className="py-3 pr-4 text-right text-slate-300">
                      {formatRupee(h.investedAmount)}
                    </td>
                    <td className="py-3 pr-4 text-right text-slate-300">
                      {formatRupee(h.currentValue)}
                    </td>
                    <td className="py-3 pr-4 text-right">
                      {formatRupee(h.gainLossAmount)} ({formatPercent(h.gainLossPercent)})
                    </td>
                    <td className="py-3 pr-4 text-right">{formatPercent(h.xirr)}</td>
                    <td className="py-3 pr-4 text-right text-slate-300">
                      {h.units != null ? Number(h.units).toFixed(2) : "—"}
                    </td>
                    <td className="py-3 text-right text-slate-300">
                      {h.currentNav != null ? Number(h.currentNav).toFixed(2) : "—"}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
