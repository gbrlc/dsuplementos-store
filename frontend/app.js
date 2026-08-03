const catalogoApresentacao = [
  { id: 1, nome: "Whey Protein", categoria: "WHEY", marca: "Integralmedica", preco: 119.9, estoque: 20, imagemUrl: "assets/products/wheyintegral.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 2, nome: "Creatina", categoria: "CREATINA", marca: "Absolut Nutrition", preco: 89.9, estoque: 15, imagemUrl: "assets/products/creatinaabsolut.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 3, nome: "Bone Crusher", categoria: "COLAGENO", marca: "Dark Lab", preco: 89.9, estoque: 10, imagemUrl: "assets/products/colageno2.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 4, nome: "Colageno", categoria: "COLAGENO", marca: "Max Titanium", preco: 89.9, estoque: 12, imagemUrl: "assets/products/colageno.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 5, nome: "Creatina Black", categoria: "CREATINA", marca: "Dark Lab", preco: 89.9, estoque: 18, imagemUrl: "assets/products/creatina3.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 6, nome: "Creatina Mono", categoria: "CREATINA", marca: "Absolut Nutrition", preco: 89.9, estoque: 16, imagemUrl: "assets/products/creatina.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 7, nome: "Multi", categoria: "VITAMINAS", marca: "Integralmedica", preco: 89.9, estoque: 14, imagemUrl: "assets/products/multi.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 8, nome: "Whey Integral", categoria: "WHEY", marca: "Integralmedica", preco: 89.9, estoque: 21, imagemUrl: "assets/products/wheyintegral.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 9, nome: "Whey Max", categoria: "WHEY", marca: "Max Titanium", preco: 89.9, estoque: 17, imagemUrl: "assets/products/wheymax.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 10, nome: "Whey Zeo", categoria: "WHEY", marca: "Max Titanium", preco: 89.9, estoque: 13, imagemUrl: "assets/products/wheyy.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 11, nome: "Whey Black", categoria: "WHEY", marca: "Dark Lab", preco: 79.9, estoque: 23, imagemUrl: "assets/products/wheyblack.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 },
  { id: 12, nome: "Colageno EPA", categoria: "COLAGENO", marca: "Max Titanium", preco: 79.9, estoque: 11, imagemUrl: "assets/products/colagenoepa.jpeg", mediaAvaliacoes: 0, quantidadeAvaliacoes: 0 }
];

let produtos = [];

const productsContainer = document.getElementById("products");
const searchInput = document.getElementById("search-products");
const categoryFilter = document.getElementById("category-filter");
const sortFilter = document.getElementById("sort-products");
const catalogStatus = document.getElementById("catalog-status");
const cartDialog = document.getElementById("cart-dialog");
const cartContent = document.getElementById("cart-content");

async function carregarProdutos() {
  catalogStatus.textContent = "Carregando catálogo...";
  try {
    produtos = await requisicaoApi("/produtos");
    catalogStatus.textContent = `${produtos.length} produtos disponíveis`;
  } catch {
    produtos = catalogoApresentacao;
    catalogStatus.textContent = "Catálogo de apresentação";
  }
  renderizarProdutos();
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

  productsContainer.innerHTML = lista.map((produto) => `
    <article class="product-card">
      <a class="product-image" href="produto.html?id=${produto.id}">
        <img src="${imagemUrl(produto.imagemUrl)}" alt="${escaparHtml(produto.nome)}" />
      </a>
      <div class="product-content">
        <p class="product-category">${categoriaLegivel(produto.categoria)}</p>
        <a class="product-name" href="produto.html?id=${produto.id}">${escaparHtml(produto.nome)}</a>
        <p class="product-brand">${escaparHtml(produto.marca)}</p>
        <div class="product-meta">
          <strong>${formatarMoeda(produto.preco)}</strong>
          <span>${produto.quantidadeAvaliacoes ? `${produto.mediaAvaliacoes.toFixed(1)} / 5` : "Sem avaliações"}</span>
        </div>
        <button class="button button-primary product-add" type="button" data-add-product="${produto.id}">Adicionar</button>
      </div>
    </article>
  `).join("");

  productsContainer.querySelectorAll("[data-add-product]").forEach((button) => {
    button.addEventListener("click", () => adicionarAoCarrinho(Number(button.dataset.addProduct)));
  });
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
  if (!carrinho.itens.length) {
    cartContent.innerHTML = '<p class="empty-state">Seu carrinho ainda está vazio.</p>';
    return;
  }

  cartContent.innerHTML = `
    <ul class="cart-list">
      ${carrinho.itens.map((item) => `
        <li class="cart-item">
          <img src="${imagemUrl(item.imagemUrl)}" alt="" />
          <div>
            <strong>${escaparHtml(item.nome)}</strong>
            <span>${formatarMoeda(item.precoUnitario)}</span>
          </div>
          <div class="quantity-control">
            <button type="button" data-change-quantity="${item.produtoId}" data-quantity="${item.quantidade - 1}" aria-label="Diminuir quantidade">−</button>
            <span>${item.quantidade}</span>
            <button type="button" data-change-quantity="${item.produtoId}" data-quantity="${item.quantidade + 1}" aria-label="Aumentar quantidade">+</button>
          </div>
        </li>
      `).join("")}
    </ul>
    <div class="cart-total"><span>Total</span><strong>${formatarMoeda(carrinho.total)}</strong></div>
    <a class="button button-primary checkout-link" href="checkout.html">Ir para checkout</a>
  `;

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
