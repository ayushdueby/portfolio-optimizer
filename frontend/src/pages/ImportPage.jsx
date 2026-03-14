import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getSessionId } from "../api";

const CATEGORIES = ["EQUITY", "DEBT", "HYBRID", "OTHER"];

const emptyRow = () => ({
  fundName: "",
  units: "",
  currentNav: "",
  currentValue: "",
  investedAmount: "",
  folioNumber: "",
  category: "EQUITY",
});

export default function ImportPage() {
  const navigate = useNavigate();
  const sessionId = getSessionId();
  const [mode, setMode] = useState("manual"); // "pdf" | "cams" | "manual"
  const [subMode, setSubMode] = useState("form"); // "form" | "csv" when mode is manual
  const [pdfFile, setPdfFile] = useState(null);
  const [pdfPassword, setPdfPassword] = useState("");
  const [pan, setPan] = useState("");
  const [manualRows, setManualRows] = useState([emptyRow()]);
  const [csvFile, setCsvFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  if (!sessionId) {
    navigate("/", { replace: true });
    return null;
  }

  const handlePdfChange = (e) => {
    setPdfFile(e.target.files?.[0] || null);
    setError("");
  };

  const handleUploadPdf = async (e) => {
    e.preventDefault();
    if (!pdfFile) {
      setError("Please select a PDF file.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", pdfFile);
      formData.append("sessionId", sessionId);
      if (pdfPassword.trim()) formData.append("password", pdfPassword.trim());
      const { data } = await api.post("/api/portfolio/upload-pdf", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (data) navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Upload failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleFetchCams = async (e) => {
    e.preventDefault();
    if (!pan.trim()) {
      setError("Please enter your PAN.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const { data } = await api.post("/api/portfolio/fetch-cams", {
        pan: pan.trim().toUpperCase(),
        sessionId,
      });
      if (data) navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Fetch failed.");
    } finally {
      setLoading(false);
    }
  };

  const addManualRow = () => setManualRows((r) => [...r, emptyRow()]);
  const removeManualRow = (i) => setManualRows((r) => r.filter((_, j) => j !== i));
  const updateManualRow = (i, field, value) => {
    setManualRows((r) => {
      const next = [...r];
      next[i] = { ...next[i], [field]: value };
      return next;
    });
  };

  const handleSaveManual = async (e) => {
    e.preventDefault();
    const holdings = manualRows
      .filter((r) => r.fundName && (r.currentValue || (r.units && r.currentNav)))
      .map((r) => ({
        fundName: r.fundName.trim(),
        folioNumber: r.folioNumber?.trim() || null,
        category: r.category,
        units: r.units ? Number(r.units) : null,
        currentNav: r.currentNav ? Number(r.currentNav) : null,
        currentValue: r.currentValue ? Number(r.currentValue) : null,
        investedAmount: r.investedAmount ? Number(r.investedAmount) : null,
      }));
    if (holdings.length === 0) {
      setError("Add at least one fund with Fund name and (Current value or Units + NAV).");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const { data } = await api.post(
        `/api/portfolio/manual?sessionId=${encodeURIComponent(sessionId)}`,
        holdings
      );
      if (data) navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Save failed.");
    } finally {
      setLoading(false);
    }
  };

  const handleUploadCsv = async (e) => {
    e.preventDefault();
    if (!csvFile) {
      setError("Please select a CSV file.");
      return;
    }
    setError("");
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", csvFile);
      formData.append("sessionId", sessionId);
      const { data } = await api.post("/api/portfolio/upload-csv", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      if (data) navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || err.message || "CSV upload failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-semibold text-white mb-2">Import Portfolio</h1>
      <p className="text-slate-400 text-sm mb-6">
        For accurate data, use <strong className="text-accentGreen">Manual entry</strong> or{" "}
        <strong className="text-accentGreen">CSV upload</strong>. PDF parsing may have errors.
      </p>

      <div className="flex flex-wrap gap-2 mb-6">
        <button
          type="button"
          onClick={() => setMode("manual")}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
            mode === "manual" ? "bg-indigo-600 text-white" : "bg-slate-800 text-slate-300 hover:bg-slate-700"
          }`}
        >
          Manual / CSV (accurate)
        </button>
        <button
          type="button"
          onClick={() => setMode("pdf")}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
            mode === "pdf" ? "bg-indigo-600 text-white" : "bg-slate-800 text-slate-300 hover:bg-slate-700"
          }`}
        >
          Upload PDF
        </button>
        <button
          type="button"
          onClick={() => setMode("cams")}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
            mode === "cams" ? "bg-indigo-600 text-white" : "bg-slate-800 text-slate-300 hover:bg-slate-700"
          }`}
        >
          Connect via PAN
        </button>
      </div>

      <div className="card">
        {mode === "manual" && (
          <>
            <div className="flex gap-2 mb-4">
              <button
                type="button"
                onClick={() => setSubMode("form")}
                className={`px-3 py-1.5 rounded text-sm ${subMode === "form" ? "bg-indigo-600" : "bg-slate-700"}`}
              >
                Enter manually
              </button>
              <button
                type="button"
                onClick={() => setSubMode("csv")}
                className={`px-3 py-1.5 rounded text-sm ${subMode === "csv" ? "bg-indigo-600" : "bg-slate-700"}`}
              >
                Upload CSV
              </button>
            </div>

            {subMode === "form" ? (
              <form onSubmit={handleSaveManual} className="space-y-4">
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="text-left text-slate-400 border-b border-slate-700">
                        <th className="pb-2 pr-2">Fund name *</th>
                        <th className="pb-2 pr-2">Units</th>
                        <th className="pb-2 pr-2">Current NAV</th>
                        <th className="pb-2 pr-2">Current value (₹) *</th>
                        <th className="pb-2 pr-2">Invested (₹)</th>
                        <th className="pb-2 pr-2">Category</th>
                        <th className="pb-2 w-8" />
                      </tr>
                    </thead>
                    <tbody>
                      {manualRows.map((row, i) => (
                        <tr key={i} className="border-b border-slate-800/50">
                          <td className="py-1.5 pr-2">
                            <input
                              type="text"
                              value={row.fundName}
                              onChange={(e) => updateManualRow(i, "fundName", e.target.value)}
                              className="input py-1.5 text-sm min-w-[180px]"
                              placeholder="Scheme name"
                            />
                          </td>
                          <td className="py-1.5 pr-2">
                            <input
                              type="number"
                              step="any"
                              value={row.units}
                              onChange={(e) => updateManualRow(i, "units", e.target.value)}
                              className="input py-1.5 text-sm w-24"
                              placeholder="—"
                            />
                          </td>
                          <td className="py-1.5 pr-2">
                            <input
                              type="number"
                              step="any"
                              value={row.currentNav}
                              onChange={(e) => updateManualRow(i, "currentNav", e.target.value)}
                              className="input py-1.5 text-sm w-24"
                              placeholder="—"
                            />
                          </td>
                          <td className="py-1.5 pr-2">
                            <input
                              type="number"
                              step="any"
                              value={row.currentValue}
                              onChange={(e) => updateManualRow(i, "currentValue", e.target.value)}
                              className="input py-1.5 text-sm w-28"
                              placeholder="₹"
                            />
                          </td>
                          <td className="py-1.5 pr-2">
                            <input
                              type="number"
                              step="any"
                              value={row.investedAmount}
                              onChange={(e) => updateManualRow(i, "investedAmount", e.target.value)}
                              className="input py-1.5 text-sm w-28"
                              placeholder="₹"
                            />
                          </td>
                          <td className="py-1.5 pr-2">
                            <select
                              value={row.category}
                              onChange={(e) => updateManualRow(i, "category", e.target.value)}
                              className="input py-1.5 text-sm w-24"
                            >
                              {CATEGORIES.map((c) => (
                                <option key={c} value={c}>{c}</option>
                              ))}
                            </select>
                          </td>
                          <td className="py-1.5">
                            <button
                              type="button"
                              onClick={() => removeManualRow(i)}
                              className="text-accentRed hover:underline text-xs"
                            >
                              Remove
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <button type="button" onClick={addManualRow} className="btn-secondary text-sm py-1.5 px-3">
                  + Add row
                </button>
                <p className="text-slate-500 text-xs">* Fund name and either Current value (₹) or Units + Current NAV are required.</p>
                {error && <p className="text-accentRed text-sm">{error}</p>}
                <button type="submit" disabled={loading} className="btn-primary w-full py-3 disabled:opacity-50">
                  {loading ? "Saving…" : "Save portfolio & go to Dashboard"}
                </button>
              </form>
            ) : (
              <form onSubmit={handleUploadCsv} className="space-y-4">
                <p className="text-slate-300 text-sm">
                  Upload a CSV with columns: <code className="bg-slate-800 px-1 rounded">fundName, units, currentNav, currentValue, investedAmount, folioNumber, category</code>. First row can be a header.
                </p>
                <a
                  href="/portfolio_template.csv"
                  download
                  className="text-accentGreen text-sm hover:underline"
                >
                  Download template CSV
                </a>
                <input
                  type="file"
                  accept=".csv"
                  onChange={(e) => { setCsvFile(e.target.files?.[0] || null); setError(""); }}
                  className="block w-full text-sm text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:bg-slate-700 file:text-white"
                />
                {error && <p className="text-accentRed text-sm">{error}</p>}
                <button type="submit" disabled={loading || !csvFile} className="btn-primary w-full py-3 disabled:opacity-50">
                  {loading ? "Uploading…" : "Upload CSV & go to Dashboard"}
                </button>
              </form>
            )}
          </>
        )}

        {mode === "pdf" && (
          <form onSubmit={handleUploadPdf} className="space-y-4">
            <label className="block text-sm font-medium text-slate-300 mb-1">Select CAMS/Karvy statement (PDF)</label>
            <input
              type="file"
              accept=".pdf"
              onChange={handlePdfChange}
              className="block w-full text-sm text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:bg-slate-700 file:text-white"
            />
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">PDF password (if protected)</label>
              <input
                type="password"
                value={pdfPassword}
                onChange={(e) => setPdfPassword(e.target.value)}
                className="input"
                placeholder="Leave blank if not protected"
                autoComplete="off"
              />
            </div>
            <p className="text-slate-500 text-xs">PDF parsing may have errors. Prefer Manual/CSV for accurate data.</p>
            {error && <p className="text-accentRed text-sm">{error}</p>}
            <button type="submit" disabled={loading || !pdfFile} className="btn-primary w-full py-3 disabled:opacity-50">
              {loading ? "Uploading…" : "Upload & Continue"}
            </button>
          </form>
        )}

        {mode === "cams" && (
          <form onSubmit={handleFetchCams} className="space-y-4">
            <label className="block text-sm font-medium text-slate-300 mb-1">PAN Number</label>
            <input
              type="text"
              value={pan}
              onChange={(e) => setPan(e.target.value.toUpperCase())}
              className="input"
              placeholder="ABCDE1234F"
              maxLength={10}
            />
            <p className="text-slate-500 text-xs">API integration not yet configured. Use Manual/CSV for now.</p>
            {error && <p className="text-accentRed text-sm">{error}</p>}
            <button type="submit" disabled={loading} className="btn-primary w-full py-3 disabled:opacity-50">
              {loading ? "Fetching…" : "Fetch Portfolio & Continue"}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
