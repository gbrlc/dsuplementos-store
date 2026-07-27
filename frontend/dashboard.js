const inventoryBody = document.getElementById("inventory-body");
const inventorySearch = document.getElementById("inventory-search");
const productDialog = document.getElementById("product-dialog");
const productForm = document.getElementById("product-form");
const productBrand = document.getElementById("product-brand");
const productDialogTitle = document.getElementById("product-dialog-title");
const productFormFeedback = document.getElementById("product-form-feedback");

let produtosInventario = [];
let marcas = [];
let produtoEmEdicao = null;

async function iniciarDashboard() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=dashboard.html";
    return;
  }

  try {
    const usuario = await requisicaoApi("/auth/me");
    if (usuario.role !== "ADMIN") {
      window.location.href = "index.html";
      return;
    }

    const sessao = obterSessao();
    salvarSessao({ ...sessao, usuario });
    document.getElementById("dashboard-user").textContent = `Olá, ${usuario.nome.split(" ")[0]}.`;
    [produtosInventario, marcas] = await Promise.all([
      requisicaoApi("/admin/produtos"),
      requisicaoApi("/marcas")
    ]);
    preencherMarcas();
    renderizarInventario();
  } catch (erro) {
    inventoryBody.innerHTML = `<tr><td colspan="6" class="table-error">${escaparHtml(erro.message)}</td></tr>`;
  }
}

function preencherMarcas() {
  productBrand.innerHTML = marcas.map((marca) => `<option value="${marca.id}">${escaparHtml(marca.nome)}</option>`).join("");
}

function produtosFiltrados() {
  const termo = inventorySearch.value.trim().toLowerCase();
  return produtosInventario.filter((produto) => {
    return !termo || `${produto.nome} ${produto.marca}`.toLowerCase().includes(termo);
  });
}

function renderizarInventario() {
  const produtos = produtosFiltrados();
  const totalEstoque = produtosInventario.reduce((soma, produto) => soma + produto.estoque, 0);
  document.getElementById("metric-products").textContent = produtosInventario.length;
  document.getElementById("metric-stock").textContent = totalEstoque;
  document.getElementById("metric-low-stock").textContent = produtosInventario.filter((produto) => produto.estoque <= 5).length;
  document.getElementById("metric-active").textContent = produtosInventario.filter((produto) => produto.ativo).length;

  if (!produtos.length) {
    inventoryBody.innerHTML = '<tr><td colspan="6" class="table-error">Nenhum produto encontrado.</td></tr>';
    return;
  }

  inventoryBody.innerHTML = produtos.map((produto) => `
    <tr>
      <td>
        <div class="inventory-product">
          <img src="${imagemUrl(produto.imagemUrl)}" alt="" />
          <div><strong>${escaparHtml(produto.nome)}</strong><span>${escaparHtml(produto.marca)}</span></div>
        </div>
      </td>
      <td>${categoriaLegivel(produto.categoria)}</td>
      <td>${formatarMoeda(produto.preco)}</td>
      <td>
        <form class="stock-editor" data-stock-form="${produto.id}">
          <input type="number" min="0" value="${produto.estoque}" aria-label="Estoque de ${escaparHtml(produto.nome)}" />
          <button type="submit">Atualizar</button>
        </form>
      </td>
      <td><span class="status-badge ${produto.ativo ? "active" : "inactive"}">${produto.ativo ? "Ativo" : "Inativo"}</span></td>
      <td><button class="table-action" type="button" data-edit-product="${produto.id}">Editar</button></td>
    </tr>
  `).join("");

  inventoryBody.querySelectorAll("[data-stock-form]").forEach((form) => {
    form.addEventListener("submit", (event) => atualizarEstoque(event, Number(form.dataset.stockForm)));
  });
  inventoryBody.querySelectorAll("[data-edit-product]").forEach((button) => {
    button.addEventListener("click", () => abrirProduto(Number(button.dataset.editProduct)));
  });
}

async function atualizarEstoque(event, produtoId) {
  event.preventDefault();
  const input = event.currentTarget.querySelector("input");
  const button = event.currentTarget.querySelector("button");
  button.disabled = true;
  try {
    const atualizado = await requisicaoApi(`/admin/produtos/${produtoId}/estoque`, {
      method: "PATCH",
      body: JSON.stringify({ estoque: Number(input.value) })
    });
    substituirProduto(atualizado);
    renderizarInventario();
    mostrarToast("Estoque atualizado.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  } finally {
    button.disabled = false;
  }
}

function abrirNovoProduto() {
  produtoEmEdicao = null;
  productDialogTitle.textContent = "Novo produto";
  productForm.reset();
  productForm.ativo.checked = true;
  productFormFeedback.textContent = "";
  productDialog.showModal();
}

function abrirProduto(id) {
  produtoEmEdicao = produtosInventario.find((produto) => produto.id === id);
  if (!produtoEmEdicao) return;

  productDialogTitle.textContent = "Editar produto";
  productForm.nome.value = produtoEmEdicao.nome;
  productForm.categoria.value = produtoEmEdicao.categoria;
  productForm.marcaId.value = produtoEmEdicao.marcaId;
  productForm.preco.value = produtoEmEdicao.preco;
  productForm.estoque.value = produtoEmEdicao.estoque;
  productForm.imagemUrl.value = produtoEmEdicao.imagemUrl || "";
  productForm.descricao.value = produtoEmEdicao.descricao;
  productForm.ativo.checked = produtoEmEdicao.ativo;
  productFormFeedback.textContent = "";
  productDialog.showModal();
}

productForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const dados = new FormData(productForm);
  const payload = {
    nome: dados.get("nome"),
    categoria: dados.get("categoria"),
    marcaId: Number(dados.get("marcaId")),
    preco: Number(dados.get("preco")),
    estoque: Number(dados.get("estoque")),
    imagemUrl: dados.get("imagemUrl"),
    descricao: dados.get("descricao"),
    ativo: productForm.ativo.checked
  };
  const button = productForm.querySelector("button[type=submit]");
  button.disabled = true;
  productFormFeedback.textContent = "";

  try {
    const produto = await requisicaoApi(
      produtoEmEdicao ? `/admin/produtos/${produtoEmEdicao.id}` : "/admin/produtos",
      { method: produtoEmEdicao ? "PUT" : "POST", body: JSON.stringify(payload) }
    );
    if (produtoEmEdicao) {
      substituirProduto(produto);
    } else {
      produtosInventario.push(produto);
    }
    productDialog.close();
    renderizarInventario();
    mostrarToast(produtoEmEdicao ? "Produto atualizado." : "Produto cadastrado.");
  } catch (erro) {
    productFormFeedback.textContent = erro.message;
    productFormFeedback.dataset.tipo = "erro";
  } finally {
    button.disabled = false;
  }
});

function substituirProduto(produtoAtualizado) {
  produtosInventario = produtosInventario.map((produto) => {
    return produto.id === produtoAtualizado.id ? produtoAtualizado : produto;
  });
}

inventorySearch.addEventListener("input", renderizarInventario);
document.querySelector("[data-new-product]").addEventListener("click", abrirNovoProduto);
document.querySelector("[data-close-product-dialog]").addEventListener("click", () => productDialog.close());

iniciarDashboard();
