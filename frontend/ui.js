const DSuplementosUI = (() => {
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

  return { cart, productCard };
})();

window.DSuplementosUI = DSuplementosUI;
