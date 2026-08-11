const favoritesGrid = document.getElementById("favorites-grid");

async function carregarFavoritos() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=favoritos.html";
    return;
  }

  try {
    const favoritos = await requisicaoApi("/favoritos");
    renderizarFavoritos(favoritos);
  } catch (erro) {
    favoritesGrid.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

function renderizarFavoritos(produtos) {
  if (!produtos.length) {
    favoritesGrid.innerHTML = '<p class="empty-state">Você ainda não salvou nenhum produto.<br /><a class="button button-primary" href="index.html#catalogo">Explorar catálogo</a></p>';
    return;
  }

  favoritesGrid.innerHTML = produtos.map((produto) => `
    <article class="product-card">
      <a class="product-image" href="produto.html?id=${produto.id}">
        <img src="${imagemUrl(produto.imagemUrl)}" alt="${escaparHtml(produto.nome)}" />
      </a>
      <button class="favorite-button is-favorite" type="button" data-remove-favorite="${produto.id}" aria-label="Remover dos favoritos" title="Remover dos favoritos">♥</button>
      <div class="product-content">
        <p class="product-category">${categoriaLegivel(produto.categoria)}</p>
        <a class="product-name" href="produto.html?id=${produto.id}">${escaparHtml(produto.nome)}</a>
        <p class="product-brand">${escaparHtml(produto.marca)}</p>
        <div class="product-meta"><strong>${formatarMoeda(produto.preco)}</strong><span>${produto.quantidadeAvaliacoes ? `${produto.mediaAvaliacoes.toFixed(1)} / 5` : "Sem avaliações"}</span></div>
        <button class="button button-primary product-add" type="button" data-add-product="${produto.id}">Adicionar</button>
      </div>
    </article>
  `).join("");

  favoritesGrid.querySelectorAll("[data-add-product]").forEach((button) => {
    button.addEventListener("click", () => adicionarAoCarrinho(Number(button.dataset.addProduct)));
  });
  favoritesGrid.querySelectorAll("[data-remove-favorite]").forEach((button) => {
    button.addEventListener("click", () => removerFavorito(Number(button.dataset.removeFavorite)));
  });
}

async function removerFavorito(produtoId) {
  try {
    await requisicaoApi(`/favoritos/${produtoId}`, { method: "DELETE" });
    mostrarToast("Produto removido dos favoritos.");
    await carregarFavoritos();
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

carregarFavoritos();
