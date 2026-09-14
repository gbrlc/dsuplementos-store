let produtos = [];
let favoritosIds = new Set();

const productsContainer = document.getElementById("products");
const searchInput = document.getElementById("search-products");
const categoryFilter = document.getElementById("category-filter");
const sortFilter = document.getElementById("sort-products");
const catalogStatus = document.getElementById("catalog-status");
const cartDialog = document.getElementById("cart-dialog");
const cartContent = document.getElementById("cart-content");

async function carregarProdutos() {
  produtos = window.CATALOGO_APRESENTACAO;
  catalogStatus.textContent = "Catálogo de apresentação";
  renderizarProdutos();
  const favoritosPromise = carregarFavoritos();

  try {
    const produtosDaApi = await requisicaoApi("/produtos", { timeoutMs: 4000 });
    const temProdutosDaApi = Array.isArray(produtosDaApi) && produtosDaApi.length > 0;
    produtos = temProdutosDaApi ? produtosDaApi : window.CATALOGO_APRESENTACAO;
    catalogStatus.textContent = temProdutosDaApi ? `${produtos.length} produtos disponíveis` : "Catálogo de apresentação";
    renderizarProdutos();
  } catch {
    // O catálogo de apresentação já está visível e a API pode ser retomada depois.
  }

  favoritosPromise.then(renderizarProdutos);
}

async function carregarFavoritos() {
  if (!obterSessao()?.token) {
    favoritosIds = new Set();
    return;
  }

  try {
    const favoritos = await requisicaoApi("/favoritos", { timeoutMs: 4000 });
    favoritosIds = new Set(favoritos.map((produto) => produto.id));
  } catch {
    favoritosIds = new Set();
  }
}

function produtosFiltrados() {
  const termo = searchInput.value.trim().toLowerCase();
  const categoria = categoryFilter.value;
  const ordenar = sortFilter.value;

  return produtos
    .filter((produto) => !categoria || produto.categoria === categoria)
    .filter((produto) => {
      const texto = `${produto.nome} ${produto.marca} ${produto.categoria}`.toLowerCase();
      return !termo || texto.includes(termo);
    })
    .sort((primeiro, segundo) => {
      if (ordenar === "preco_asc") return Number(primeiro.preco) - Number(segundo.preco);
      if (ordenar === "preco_desc") return Number(segundo.preco) - Number(primeiro.preco);
      if (ordenar === "nome") return primeiro.nome.localeCompare(segundo.nome, "pt-BR");
      return primeiro.id - segundo.id;
    });
}

function renderizarProdutos() {
  const lista = produtosFiltrados();

  if (lista.length === 0) {
    productsContainer.innerHTML = '<p class="empty-state">Nenhum produto encontrado para este filtro.</p>';
    return;
  }

  productsContainer.innerHTML = lista
    .map((produto) => DSuplementosUI.productCard(produto, { favorito: favoritosIds.has(produto.id) }))
    .join("");

  productsContainer.querySelectorAll("[data-add-product]").forEach((button) => {
    button.addEventListener("click", () => adicionarAoCarrinho(Number(button.dataset.addProduct)));
  });
  productsContainer.querySelectorAll("[data-favorite-product]").forEach((button) => {
    button.addEventListener("click", () => alterarFavorito(Number(button.dataset.favoriteProduct)));
  });
}

async function alterarFavorito(produtoId) {
  if (!obterSessao()?.token) {
    window.location.href = `login.html?redirect=${encodeURIComponent(window.location.pathname.split("/").pop() || "index.html")}`;
    return;
  }

  const jaFavorito = favoritosIds.has(produtoId);
  try {
    await requisicaoApi(`/favoritos/${produtoId}`, { method: jaFavorito ? "DELETE" : "POST" });
    if (jaFavorito) {
      favoritosIds.delete(produtoId);
    } else {
      favoritosIds.add(produtoId);
    }
    renderizarProdutos();
    mostrarToast(jaFavorito ? "Produto removido dos favoritos." : "Produto salvo nos favoritos.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

async function abrirCarrinho() {
  if (!obterSessao()?.token) {
    cartContent.innerHTML = `
      <div class="empty-state">
        <p>Entre na sua conta para montar o carrinho.</p>
        <a class="button button-primary" href="login.html">Entrar</a>
      </div>
    `;
    cartDialog.showModal();
    return;
  }

  cartContent.innerHTML = '<p class="muted-text">Carregando carrinho...</p>';
  cartDialog.showModal();
  try {
    const carrinho = await requisicaoApi("/carrinho");
    renderizarCarrinho(carrinho);
  } catch (erro) {
    cartContent.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

function renderizarCarrinho(carrinho) {
  cartContent.innerHTML = DSuplementosUI.cart(carrinho);

  cartContent.querySelectorAll("[data-change-quantity]").forEach((button) => {
    button.addEventListener("click", () => alterarQuantidadeCarrinho(
      Number(button.dataset.changeQuantity),
      Number(button.dataset.quantity)
    ));
  });
}

async function alterarQuantidadeCarrinho(produtoId, quantidade) {
  try {
    const carrinho = quantidade === 0
      ? await requisicaoApi(`/carrinho/itens/${produtoId}`, { method: "DELETE" })
      : await requisicaoApi(`/carrinho/itens/${produtoId}`, {
          method: "PATCH",
          body: JSON.stringify({ quantidade })
        });
    renderizarCarrinho(carrinho);
    await atualizarCarrinho();
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

searchInput.addEventListener("input", renderizarProdutos);
categoryFilter.addEventListener("change", renderizarProdutos);
sortFilter.addEventListener("change", renderizarProdutos);

document.querySelectorAll("[data-category-shortcut]").forEach((shortcut) => {
  shortcut.addEventListener("click", () => {
    categoryFilter.value = shortcut.dataset.categoryShortcut;
    renderizarProdutos();
  });
});

document.querySelector("[data-open-cart]").addEventListener("click", abrirCarrinho);
document.querySelector("[data-close-cart]").addEventListener("click", () => cartDialog.close());

carregarProdutos();
