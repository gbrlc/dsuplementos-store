const API_BASE_URL = window.DSUPLEMENTOS_API_URL || "http://localhost:8080/api";
const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, "");
const SESSION_KEY = "dsuplementos.session";
window.CATALOGO_APRESENTACAO = [
  { id: 1, nome: "Whey Protein", categoria: "WHEY", marca: "Integralmedica", preco: 119.9, estoque: 20, imagemUrl: "assets/products/wheyintegral.jpeg", descricao: "Whey protein para complementar sua rotina de treino.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 2, nome: "Creatina", categoria: "CREATINA", marca: "Absolut Nutrition", preco: 89.9, estoque: 15, imagemUrl: "assets/products/creatinaabsolut.jpeg", descricao: "Creatina monohidratada para sua rotina de força.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 3, nome: "Bone Crusher", categoria: "COLAGENO", marca: "Dark Lab", preco: 89.9, estoque: 10, imagemUrl: "assets/products/colageno2.jpeg", descricao: "Suplemento de colágeno para sua rotina.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 4, nome: "Colageno", categoria: "COLAGENO", marca: "Max Titanium", preco: 89.9, estoque: 12, imagemUrl: "assets/products/colageno.jpeg", descricao: "Colágeno para complementar seus cuidados diários.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 5, nome: "Creatina Black", categoria: "CREATINA", marca: "Dark Lab", preco: 89.9, estoque: 18, imagemUrl: "assets/products/creatina3.jpeg", descricao: "Creatina para complementar sua rotina de treino.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 6, nome: "Creatina Mono", categoria: "CREATINA", marca: "Absolut Nutrition", preco: 89.9, estoque: 16, imagemUrl: "assets/products/creatina.jpeg", descricao: "Creatina monohidratada para treinos consistentes.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 7, nome: "Multi", categoria: "VITAMINAS", marca: "Integralmedica", preco: 89.9, estoque: 14, imagemUrl: "assets/products/multi.jpeg", descricao: "Multivitamínico para complementar sua rotina.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 8, nome: "Whey Integral", categoria: "WHEY", marca: "Integralmedica", preco: 89.9, estoque: 21, imagemUrl: "assets/products/wheyintegral.jpeg", descricao: "Whey protein para complementar sua rotina de treino.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 9, nome: "Whey Max", categoria: "WHEY", marca: "Max Titanium", preco: 89.9, estoque: 17, imagemUrl: "assets/products/wheymax.jpeg", descricao: "Whey protein para a sua rotina de treino.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 10, nome: "Whey Zeo", categoria: "WHEY", marca: "Max Titanium", preco: 89.9, estoque: 13, imagemUrl: "assets/products/wheyy.jpeg", descricao: "Whey protein para complementar seus objetivos.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 11, nome: "Whey Black", categoria: "WHEY", marca: "Dark Lab", preco: 79.9, estoque: 23, imagemUrl: "assets/products/wheyblack.jpeg", descricao: "Whey protein para sua rotina de treino.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 12, nome: "Colageno EPA", categoria: "COLAGENO", marca: "Max Titanium", preco: 79.9, estoque: 11, imagemUrl: "assets/products/colagenoepa.jpeg", descricao: "Colágeno para complementar seus cuidados diários.", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 }
];
let renovacaoEmAndamento = null;

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
  const { tentarRenovar = true, timeoutMs, ...opcoesFetch } = opcoes;
  const headers = new Headers(opcoesFetch.headers || {});
  const sessao = obterSessao();
  const controller = timeoutMs ? new AbortController() : null;
  const timeout = controller ? window.setTimeout(() => controller.abort(), timeoutMs) : null;

  if (sessao?.token) {
    headers.set("Authorization", `Bearer ${sessao.token}`);
  }
  if (opcoesFetch.body && !(opcoesFetch.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${caminho}`, {
      ...opcoesFetch,
      headers,
      signal: controller?.signal || opcoesFetch.signal
    });
  } catch (erro) {
    if (erro.name === "AbortError") {
      throw new Error("A API demorou demais para responder.");
    }
    throw new Error("Nao foi possivel conectar com a API. Inicie o backend e confira a configuracao.");
  } finally {
    if (timeout) window.clearTimeout(timeout);
  }

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    if (response.status === 401 && tentarRenovar && caminho !== "/auth/refresh" && await renovarSessao()) {
      return requisicaoApi(caminho, { ...opcoesFetch, tentarRenovar: false });
    }
    if (response.status === 401) {
      encerrarSessao();
    }
    throw new Error(body?.mensagem || "Nao foi possivel concluir esta operacao.");
  }

  return body;
}

async function renovarSessao() {
  if (!renovacaoEmAndamento) {
    renovacaoEmAndamento = renovarSessaoInterna().finally(() => {
      renovacaoEmAndamento = null;
    });
  }
  return renovacaoEmAndamento;
}

async function renovarSessaoInterna() {
  const sessao = obterSessao();
  if (!sessao?.refreshToken) {
    return false;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: sessao.refreshToken })
    });
    if (!response.ok) {
      encerrarSessao();
      return false;
    }
    salvarSessao(await response.json());
    return true;
  } catch {
    return false;
  }
}

async function encerrarSessaoNoServidor() {
  const refreshToken = obterSessao()?.refreshToken;
  encerrarSessao();
  if (!refreshToken) {
    return;
  }

  try {
    await fetch(`${API_BASE_URL}/auth/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken })
    });
  } catch {
    // O navegador ja encerrou a sessao local mesmo se a API estiver indisponivel.
  }
}

function atualizarCabecalho() {
  const sessao = obterSessao();
  const accountLink = document.querySelector("[data-account-link]");
  const dashboardLink = document.querySelector("[data-dashboard-link]");
  const favoritesLink = document.querySelector("[data-favorites-link]");
  const logoutButton = document.querySelector("[data-logout]");

  if (accountLink) {
    accountLink.textContent = sessao?.usuario ? sessao.usuario.nome.split(" ")[0] : "Entrar";
    accountLink.href = sessao?.usuario ? "conta.html" : "login.html";
  }
  if (dashboardLink) {
    dashboardLink.hidden = sessao?.usuario?.role !== "ADMIN";
  }
  if (favoritesLink) {
    favoritesLink.hidden = !sessao?.usuario;
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

  document.querySelector("[data-logout]")?.addEventListener("click", async () => {
    await encerrarSessaoNoServidor();
    window.location.href = "index.html";
  });
});
