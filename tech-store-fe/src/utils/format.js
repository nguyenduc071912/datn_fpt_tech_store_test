export const formatVND = (v) =>
  v ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v) : "—";

export const formatDate = (d) =>
  d ? new Date(d).toLocaleDateString("vi-VN") : "—";

export const formatDateTime = (d) =>
  d ? new Date(d).toLocaleString("vi-VN") : "—";

export const initials = (name) =>
  name ? name.split(" ").slice(-2).map(n => n[0]).join("").toUpperCase() : "?";
