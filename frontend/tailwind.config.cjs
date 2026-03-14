/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        background: "#050816",
        card: "#111827",
        accentGreen: "#22c55e",
        accentRed: "#ef4444",
      },
    },
  },
  plugins: [],
};

