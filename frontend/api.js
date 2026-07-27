const API_BASE_URL = window.DSUPLEMENTOS_API_URL || "http://localhost:8080/api";
const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, "");
const SESSION_KEY = "dsuplementos.session";

function formatarMoeda(valor) {
  return Number(valor || 0).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });
}

function escaparHtml(valor = "") {
  return String(valor)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function imagemUrl(caminho) {
  if (!caminho) {
    return "assets/products/logo.jpeg";
  }
  if (caminho.startsWith("http://") || caminho.startsWith("https://") || caminho.startsWith("assets/")) {
    return caminho;
  }
  return `${API_ORIGIN}${caminho}`;
}

function obterSessao() {
  try {
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}

function salvarSessao(sessao) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(sessao));
  atualizarCabecalho();
}

function encerrarSessao() {
  localStorage.removeItem(SESSION_KEY);
  atualizarCabecalho();
}

async function requisicaoApi(caminho, opcoes = {}) {
  const headers = new Headers(opcoes.headers || {});
  const sessao = obterSessao();

  if (sessao?.token) {
    headers.set("Authorization", `Bearer ${sessao.token}`);
  }
  if (opcoes.body && !(opcoes.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${caminho}`, { ...opcoes, headers });
  } catch {
    throw new Error("Nao foi possivel conectar com a API. Inicie o backend e confira a configuracao.");
  }

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    if (response.status === 401) {
      encerrarSessao();
    }
    throw new Error(body?.mensagem || "Nao foi possivel concluir esta operacao.");
  }

  return body;
}

function atualizarCabecalho() {
  const sessao = obterSessao();
  const accountLink = document.querySelector("[data-account-link]");
  const dashboardLink = document.querySelector("[data-dashboard-link]");
  const logoutButton = document.querySelector("[data-logout]");

  if (accountLink) {
    accountLink.textContent = sessao?.usuario ? sessao.usuario.nome.split(" ")[0] : "Entrar";
    accountLink.href = sessao?.usuario ? "conta.html" : "login.html";
  }
  if (dashboardLink) {
    dashboardLink.hidden = sessao?.usuario?.role !== "ADMIN";
  }
  if (logoutButton) {
    logoutButton.hidden = !sessao?.usuario;
  }
}

function mostrarToast(mensagem, tipo = "sucesso") {
  let toast = document.getElementById("toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "toast";
    toast.className = "toast";
    toast.setAttribute("role", "status");
    document.body.appendChild(toast);
  }

  toast.textContent = mensagem;
  toast.dataset.tipo = tipo;
  toast.classList.add("visible");
  window.clearTimeout(mostrarToast.timeout);
  mostrarToast.timeout = window.setTimeout(() => toast.classList.remove("visible"), 3600);
}

async function atualizarCarrinho() {
  const count = document.querySelector("[data-cart-count]");
  if (!obterSessao()?.token) {
    if (count) count.textContent = "0";
    return null;
  }

  try {
    const carrinho = await requisicaoApi("/carrinho");
    if (count) {
      count.textContent = carrinho.itens.reduce((soma, item) => soma + item.quantidade, 0);
    }
    return carrinho;
  } catch (erro) {
    if (count) count.textContent = "0";
    return null;
  }
}

async function adicionarAoCarrinho(produtoId) {
  if (!obterSessao()?.token) {
    window.location.href = `login.html?redirect=${encodeURIComponent(window.location.pathname.split("/").pop() || "index.html")}`;
    return;
  }

  try {
    await requisicaoApi("/carrinho/itens", {
      method: "POST",
      body: JSON.stringify({ produtoId, quantidade: 1 })
    });
    await atualizarCarrinho();
    mostrarToast("Produto adicionado ao carrinho.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

function categoriaLegivel(categoria) {
  return (categoria || "").replaceAll("_", " ").toLowerCase().replace(/(^|\s)\S/g, (letra) => letra.toUpperCase());
}

document.addEventListener("DOMContentLoaded", () => {
  atualizarCabecalho();
  atualizarCarrinho();

  document.querySelector("[data-logout]")?.addEventListener("click", () => {
    encerrarSessao();
    window.location.href = "index.html";
  });
});
