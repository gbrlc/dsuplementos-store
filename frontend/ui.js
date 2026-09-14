const DSuplementosUI = (() => {
  const headerLinks = {
    store: `<a href="index.html#catalogo">Catálogo</a><a href="index.html#categorias">Objetivos</a><a href="favoritos.html" data-favorites-link hidden>Favoritos</a><a href="dashboard.html" data-dashboard-link hidden>Gestão</a>`,
    account: `<a href="index.html#catalogo">Loja</a><a href="favoritos.html">Favoritos</a><a href="pedidos.html">Pedidos</a>`,
    admin: `<a href="index.html">Ver loja</a><a href="conta.html">Minha conta</a>`
  };

  function brand() {
    return `<a class="brand" href="index.html" aria-label="Página inicial DSuplementos Store"><img src="assets/products/logo.jpeg" alt="" width="256" height="256" decoding="async" /><span>DSuplementos</span></a>`;
  }

  function mountHeader() {
    const header = document.querySelector("[data-site-header]");
    if (!header) return;

    const mode = header.dataset.headerMode || "store";
    if (mode === "simple") {
      header.innerHTML = `${brand()}<a class="header-link" href="index.html">Voltar à loja</a>`;
      return;
    }

    const storeActions = `<a class="header-link" data-account-link href="login.html">Entrar</a><button class="cart-trigger" type="button" data-open-cart><span class="cart-trigger__label">Carrinho</span><span data-cart-count>0</span></button>`;
    const accountActions = `<a class="header-link" data-account-link href="conta.html">Minha conta</a><button class="header-link button-link" data-logout type="button">Sair</button>`;

    header.innerHTML = `${brand()}<nav class="main-nav" aria-label="Navegação principal">${headerLinks[mode] || headerLinks.account}</nav><div class="header-tools">${mode === "store" ? storeActions : accountActions}</div>`;
  }

  function avaliacaoProduto(produto) {
    return produto.quantidadeAvaliacoes
      ? `${Number(produto.mediaAvaliacoes).toFixed(1)} / 5`
      : "Sem avaliações";
  }

  function productCard(produto, { favorito = false, acaoFavorito = "toggle" } = {}) {
    const favoriteAttribute = acaoFavorito === "remove"
      ? `data-remove-favorite="${produto.id}"`
      : `data-favorite-product="${produto.id}"`;
    const favoriteLabel = favorito ? "Remover dos favoritos" : "Adicionar aos favoritos";

    return `
      <article class="product-card">
        <a class="product-image" href="produto.html?id=${produto.id}">
          <img src="${imagemUrl(produto.imagemUrl)}" alt="${escaparHtml(produto.nome)}" width="426" height="640" loading="lazy" decoding="async" />
        </a>
        <button class="favorite-button ${favorito ? "is-favorite" : ""}" type="button" ${favoriteAttribute} aria-pressed="${favorito}" aria-label="${favoriteLabel}" title="${favoriteLabel}">
          <span aria-hidden="true">♥</span>
        </button>
        <div class="product-content">
          <p class="product-category">${categoriaLegivel(produto.categoria)}</p>
          <a class="product-name" href="produto.html?id=${produto.id}">${escaparHtml(produto.nome)}</a>
          <p class="product-brand">${escaparHtml(produto.marca)}</p>
          <div class="product-meta">
            <strong>${formatarMoeda(produto.preco)}</strong>
            <span>${avaliacaoProduto(produto)}</span>
          </div>
          <button class="button button-primary product-add" type="button" data-add-product="${produto.id}">Adicionar</button>
        </div>
      </article>
    `;
  }

  function cart(carrinho, { permitirQuantidade = true } = {}) {
    if (!carrinho.itens.length) {
      return '<p class="empty-state">Seu carrinho ainda está vazio.</p>';
    }

    return `
      <ul class="cart-list">
        ${carrinho.itens.map((item) => `
          <li class="cart-item">
            <img src="${imagemUrl(item.imagemUrl)}" alt="" width="426" height="640" loading="lazy" decoding="async" />
            <div>
              <strong>${escaparHtml(item.nome)}</strong>
              <span>${permitirQuantidade ? formatarMoeda(item.precoUnitario) : `${item.quantidade} × ${formatarMoeda(item.precoUnitario)}`}</span>
            </div>
            ${permitirQuantidade ? `
              <div class="quantity-control" aria-label="Quantidade de ${escaparHtml(item.nome)}">
                <button type="button" data-change-quantity="${item.produtoId}" data-quantity="${item.quantidade - 1}" aria-label="Diminuir quantidade">−</button>
                <span>${item.quantidade}</span>
                <button type="button" data-change-quantity="${item.produtoId}" data-quantity="${item.quantidade + 1}" aria-label="Aumentar quantidade">+</button>
              </div>
            ` : ""}
          </li>
        `).join("")}
      </ul>
      <div class="cart-total"><span>Total</span><strong>${formatarMoeda(carrinho.total)}</strong></div>
      <a class="button button-primary checkout-link" href="checkout.html">Ir para checkout</a>
    `;
  }

  return { cart, mountHeader, productCard };
})();

window.DSuplementosUI = DSuplementosUI;
DSuplementosUI.mountHeader();
