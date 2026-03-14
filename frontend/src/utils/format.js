export function formatRupee(value) {
  if (value == null || Number.isNaN(value)) return "₹ —";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  }).format(value);
}

export function formatPercent(value) {
  if (value == null || Number.isNaN(value)) return "—%";
  const sign = value >= 0 ? "+" : "";
  return `${sign}${Number(value).toFixed(2)}%`;
}
