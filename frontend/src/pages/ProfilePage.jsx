import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { setSessionId } from "../api";

const RISK_OPTIONS = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
];

export default function ProfilePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    age: "",
    monthlyIncome: "",
    monthlyInvestment: "",
    riskAppetite: "MEDIUM",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const payload = {
        name: form.name.trim(),
        age: form.age ? Number(form.age) : null,
        monthlyIncome: form.monthlyIncome ? Number(form.monthlyIncome) : null,
        monthlyInvestment: form.monthlyInvestment ? Number(form.monthlyInvestment) : null,
        riskAppetite: form.riskAppetite,
      };
      const { data } = await api.post("/api/user/profile", payload);
      if (data?.sessionId) {
        setSessionId(data.sessionId);
        navigate("/import");
      } else {
        setError("No session received.");
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Failed to save profile.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-2xl font-semibold text-white mb-2">User Profile</h1>
      <p className="text-slate-400 text-sm mb-6">
        Tell us about yourself so we can tailor analysis and suggestions.
      </p>
      <form onSubmit={handleSubmit} className="card space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">Name</label>
          <input
            type="text"
            name="name"
            value={form.name}
            onChange={handleChange}
            className="input"
            placeholder="Your name"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">Age</label>
          <input
            type="number"
            name="age"
            value={form.age}
            onChange={handleChange}
            className="input"
            placeholder="25"
            min={18}
            max={100}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Monthly Income (₹)
          </label>
          <input
            type="number"
            name="monthlyIncome"
            value={form.monthlyIncome}
            onChange={handleChange}
            className="input"
            placeholder="150000"
            min={0}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Monthly Investment Amount (₹)
          </label>
          <input
            type="number"
            name="monthlyInvestment"
            value={form.monthlyInvestment}
            onChange={handleChange}
            className="input"
            placeholder="20000"
            min={0}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Risk Appetite
          </label>
          <select
            name="riskAppetite"
            value={form.riskAppetite}
            onChange={handleChange}
            className="input"
          >
            {RISK_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        {error && (
          <p className="text-accentRed text-sm">{error}</p>
        )}
        <button
          type="submit"
          disabled={loading}
          className="btn-primary w-full py-3 disabled:opacity-50"
        >
          {loading ? "Saving…" : "Save & Continue to Import"}
        </button>
      </form>
    </div>
  );
}
