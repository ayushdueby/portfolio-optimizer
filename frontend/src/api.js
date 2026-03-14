import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

export const getSessionId = () => {
  let sessionId = localStorage.getItem("sessionId");
  return sessionId;
};

export const setSessionId = (id) => {
  localStorage.setItem("sessionId", id);
};

export default api;

