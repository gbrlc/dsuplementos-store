const productDetail = document.getElementById("product-detail");
const reviewsList = document.getElementById("reviews-list");
const reviewForm = document.getElementById("review-form");
const reviewLoginHint = document.getElementById("review-login-hint");
const reviewCount = document.getElementById("review-count");
const cartDialog = document.getElementById("cart-dialog");
const cartContent = document.getElementById("cart-content");
const productId = Number(new URLSearchParams(window.location.search).get("id"));
let produtoAtual = null;
let produtoFavorito = false;

async function carregarPaginaProduto() {
  if (!Number.isInteger(productId) || productId <= 0) {
    productDetail.innerHTML = '<p class="empty-state">Produto inválido.</p>';
    return;
  }

  const produtoApresentacao = window.CATALOGO_APRESENTACAO.find((produto) => produto.id === productId);
  if (produtoApresentacao) {
    produtoAtual = produtoApresentacao;
    produtoFavorito = false;
    renderizarProduto(produtoApresentacao);
    renderizarAvaliacoes([]);
  }
  configurarFormularioAvaliacao();

  const [resultadoProduto, avaliacoes, favorito] = await Promise.all([
    requisicaoApi(`/produtos/${productId}`, { timeoutMs: 4000 }).catch(() => null),
    requisicaoApi(`/produtos/${productId}/avaliacoes`, { timeoutMs: 4000 }).catch(() => []),
    obterSessao()?.token ? requisicaoApi(`/favoritos/${productId}`, { timeoutMs: 4000 }).catch(() => ({ favorito: false })) : Promise.resolve({ favorito: false })
  ]);

  if (resultadoProduto) {
    produtoAtual = resultadoProduto;
    produtoFavorito = favorito.favorito;
    renderizarProduto(resultadoProduto);
  } else if (!produtoApresentacao) {
    productDetail.innerHTML = '<p class="empty-state">Produto não encontrado.</p>';
  }
  renderizarAvaliacoes(avaliacoes);
}

function renderizarProduto(produto) {
  document.title = `${produto.nome} | DSuplementos Store`;
  productDetail.innerHTML = `
    <div class="product-detail-image">
      <img src="${imagemUrl(produto.imagemUrl)}" alt="${escaparHtml(produto.nome)}" width="426" height="640" decoding="async" />
    </div>
    <div class="product-detail-info">
      <p class="eyebrow">${categoriaLegivel(produto.categoria)}</p>
      <p class="detail-brand">${escaparHtml(produto.marca)}</p>
      <h1>${escaparHtml(produto.nome)}</h1>
      <button class="favorite-button detail-favorite ${produtoFavorito ? "is-favorite" : ""}" type="button" aria-pressed="${produtoFavorito}" aria-label="${produtoFavorito ? "Remover dos favoritos" : "Adicionar aos favoritos"}" title="${produtoFavorito ? "Remover dos favoritos" : "Adicionar aos favoritos"}">♥</button>
      <div class="rating-summary">
        <strong>${produto.quantidadeAvaliacoes ? produto.mediaAvaliacoes.toFixed(1) : "Novo"}</strong>
        <span>${produto.quantidadeAvaliacoes ? `${produto.quantidadeAvaliacoes} avaliações` : "Ainda sem avaliações"}</span>
      </div>
      <p class="detail-description">${escaparHtml(produto.descricao)}</p>
      <div class="detail-purchase">
        <strong>${formatarMoeda(produto.preco)}</strong>
        <span>${produto.estoque > 0 ? `${produto.estoque} unidades em estoque` : "Produto indisponível"}</span>
      </div>
      <button class="button button-primary detail-add" type="button" ${produto.estoque === 0 ? "disabled" : ""}>Adicionar ao carrinho</button>
    </div>
  `;

  productDetail.querySelector(".detail-add")?.addEventListener("click", () => adicionarAoCarrinho(produto.id));
  productDetail.querySelector(".detail-favorite")?.addEventListener("click", alterarFavoritoDaPagina);
}

async function alterarFavoritoDaPagina() {
  if (!obterSessao()?.token) {
    window.location.href = `login.html?redirect=${encodeURIComponent(`produto.html?id=${productId}`)}`;
    return;
  }

  try {
    await requisicaoApi(`/favoritos/${productId}`, { method: produtoFavorito ? "DELETE" : "POST" });
    produtoFavorito = !produtoFavorito;
    renderizarProduto(produtoAtual);
    mostrarToast(produtoFavorito ? "Produto salvo nos favoritos." : "Produto removido dos favoritos.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

function renderizarAvaliacoes(avaliacoes) {
  reviewCount.textContent = avaliacoes.length === 1 ? "1 avaliação" : `${avaliacoes.length} avaliações`;

  if (!avaliacoes.length) {
    reviewsList.innerHTML = '<p class="empty-state">Este produto ainda não recebeu avaliações.</p>';
    return;
  }

  reviewsList.innerHTML = avaliacoes.map((avaliacao) => `
    <article class="review-card">
      <div class="review-header">
        <div>
          <strong>${escaparHtml(avaliacao.usuario)}</strong>
          <span>${new Intl.DateTimeFormat("pt-BR", { dateStyle: "medium" }).format(new Date(avaliacao.criadoEm))}</span>
        </div>
        <span class="review-rating">${avaliacao.nota} / 5</span>
      </div>
      <p>${escaparHtml(avaliacao.comentario)}</p>
      ${avaliacao.fotos.length ? `
        <div class="review-photos">
          ${avaliacao.fotos.map((foto) => `<a href="${imagemUrl(foto.url)}" target="_blank" rel="noreferrer"><img src="${imagemUrl(foto.url)}" alt="Foto da avaliação de ${escaparHtml(avaliacao.usuario)}" loading="lazy" decoding="async" /></a>`).join("")}
        </div>
      ` : ""}
    </article>
  `).join("");
}

function configurarFormularioAvaliacao() {
  if (!obterSessao()?.token) {
    reviewLoginHint.hidden = false;
    reviewForm.hidden = true;
    return;
  }

  reviewLoginHint.hidden = true;
  reviewForm.hidden = false;
}

reviewForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const button = reviewForm.querySelector("button[type=submit]");
  const formData = new FormData(reviewForm);
  const fotos = formData.getAll("fotos").filter((foto) => foto instanceof File && foto.size > 0);

  if (fotos.length > 4) {
    mostrarToast("Selecione no máximo 4 fotos.", "erro");
    return;
  }

  button.disabled = true;
  button.textContent = "Publicando...";
  try {
    const avaliacao = await requisicaoApi(`/produtos/${productId}/avaliacoes`, {
      method: "POST",
      body: JSON.stringify({
        nota: Number(formData.get("nota")),
        comentario: formData.get("comentario")
      })
    });

    if (fotos.length) {
      const upload = new FormData();
      fotos.forEach((foto) => upload.append("fotos", foto));
      await requisicaoApi(`/avaliacoes/${avaliacao.id}/fotos`, { method: "POST", body: upload });
    }

    reviewForm.reset();
    mostrarToast("Avaliação publicada.");
    await carregarPaginaProduto();
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  } finally {
    button.disabled = false;
    button.textContent = "Publicar avaliação";
  }
});

async function abrirCarrinho() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html";
    return;
  }
  cartContent.innerHTML = '<p class="muted-text">Carregando carrinho...</p>';
  cartDialog.showModal();
  try {
    const carrinho = await requisicaoApi("/carrinho");
    if (!carrinho.itens.length) {
      cartContent.innerHTML = '<p class="empty-state">Seu carrinho ainda está vazio.</p>';
      return;
    }
    cartContent.innerHTML = `
      <ul class="cart-list">
        ${carrinho.itens.map((item) => `<li class="cart-item"><img src="${imagemUrl(item.imagemUrl)}" alt="" width="426" height="640" loading="lazy" decoding="async" /><div><strong>${escaparHtml(item.nome)}</strong><span>${item.quantidade} x ${formatarMoeda(item.precoUnitario)}</span></div></li>`).join("")}
      </ul>
      <div class="cart-total"><span>Total</span><strong>${formatarMoeda(carrinho.total)}</strong></div>
      <a class="button button-primary checkout-link" href="checkout.html">Ir para checkout</a>
    `;
  } catch (erro) {
    cartContent.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

document.querySelector("[data-open-cart]").addEventListener("click", abrirCarrinho);
document.querySelector("[data-close-cart]").addEventListener("click", () => cartDialog.close());

carregarPaginaProduto();
