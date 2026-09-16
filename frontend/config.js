const LOCAL_HOSTS = new Set(["localhost", "127.0.0.1", ""]);

if (!LOCAL_HOSTS.has(window.location.hostname)) {
  window.DSUPLEMENTOS_API_URL = "https://dsuplementos-store-1.onrender.com/api";
}
