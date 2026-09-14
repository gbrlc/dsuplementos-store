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

  favoritesGrid.innerHTML = produtos
    .map((produto) => DSuplementosUI.productCard(produto, { favorito: true, acaoFavorito: "remove" }))
    .join("");

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
