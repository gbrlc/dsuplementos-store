const products = [
  { id: 1, nome: "Whey Protein", preco: 119.90, imagem: "assets/products/wheyintegral.jpeg" },
  { id: 2, nome: "Creatina", preco: 89.90, imagem: "assets/products/creatinaabsolut.jpeg" },
  { id: 3, nome: "Bone Crusher", preco: 89.90, imagem: "assets/products/colageno2.jpeg" },
  { id: 4, nome: "Colageno", preco: 89.90, imagem: "assets/products/colageno.jpeg" },
  { id: 5, nome: "Creatina Black", preco: 89.90, imagem: "assets/products/creatina3.jpeg" },
  { id: 6, nome: "Creatina Mono", preco: 89.90, imagem: "assets/products/creatina.jpeg" },
  { id: 7, nome: "Multi", preco: 89.90, imagem: "assets/products/multi.jpeg" },
  { id: 8, nome: "Whey Integral", preco: 89.90, imagem: "assets/products/wheyintegral.jpeg" },
  { id: 9, nome: "Whey Max", preco: 89.90, imagem: "assets/products/wheymax.jpeg" },
  { id: 10, nome: "Whey Zeo", preco: 89.90, imagem: "assets/products/wheyy.jpeg" },
  { id: 11, nome: "Whey Black", preco: 79.90, imagem: "assets/products/wheyblack.jpeg" },
  { id: 12, nome: "Colageno EPA", preco: 79.90, imagem: "assets/products/colagenoepa.jpeg" }
];

const productsContainer = document.getElementById("products");
const carrinhoContainer = document.getElementById("carrinho");
const productCount = document.getElementById("product-count");

let carrinho = JSON.parse(localStorage.getItem("carrinho")) || [];

function formatarMoeda(valor) {
  return valor.toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL"
  });
}

function salvarCarrinho() {
  localStorage.setItem("carrinho", JSON.stringify(carrinho));
}

function renderizarProdutos() {
  productsContainer.innerHTML = "";
  productCount.innerText = `${products.length} produtos`;

  products.forEach((product) => {
    const card = document.createElement("article");
    card.className = "product-card";

    card.innerHTML = `
      <div class="product-image">
        <img src="${product.imagem}" alt="${product.nome}">
      </div>
      <div class="product-info">
        <h3>${product.nome}</h3>
        <p class="product-price">${formatarMoeda(product.preco)}</p>
        <button type="button">Adicionar ao carrinho</button>
      </div>
    `;

    card.querySelector("button").addEventListener("click", () => {
      adicionarAoCarrinho(product);
    });

    productsContainer.appendChild(card);
  });
}

function adicionarAoCarrinho(produto) {
  const itemExistente = carrinho.find((item) => item.id === produto.id);

  if (itemExistente) {
    itemExistente.quantidade += 1;
  } else {
    carrinho.push({ ...produto, quantidade: 1 });
  }

  salvarCarrinho();
  renderizarCarrinho();
}

function removerItem(id) {
  carrinho = carrinho.filter((item) => item.id !== id);
  salvarCarrinho();
  renderizarCarrinho();
}

function renderizarCarrinho() {
  carrinhoContainer.innerHTML = "<h2>Carrinho</h2>";

  if (carrinho.length === 0) {
    carrinhoContainer.innerHTML += '<p class="cart-empty">Seu carrinho esta vazio.</p>';
    return;
  }

  const total = carrinho.reduce((soma, item) => {
    return soma + item.preco * item.quantidade;
  }, 0);

  const lista = document.createElement("ul");
  lista.className = "cart-list";

  carrinho.forEach((item) => {
    const li = document.createElement("li");
    li.className = "cart-item";
    li.innerHTML = `
      <div>
        <strong>${item.nome}</strong>
        <span>${item.quantidade} x ${formatarMoeda(item.preco)}</span>
      </div>
      <button
        type="button"
        class="remove-button"
        aria-label="Remover ${item.nome}"
        onclick="removerItem(${item.id})"
      >
        x
      </button>
    `;

    lista.appendChild(li);
  });

  carrinhoContainer.appendChild(lista);
  carrinhoContainer.innerHTML += `
    <div class="cart-total">
      <span>Total</span>
      <strong>${formatarMoeda(total)}</strong>
    </div>
    <button type="button" onclick="finalizarCompra()">Finalizar compra</button>
  `;
}

function login() {
  const usuario = document.getElementById("usuario").value.trim();

  if (!usuario) {
    alert("Digite um usuario");
    return;
  }

  localStorage.setItem("usuario", usuario);
  atualizarUsuario();
}

function logout() {
  localStorage.removeItem("usuario");
  atualizarUsuario();
}

function atualizarUsuario() {
  const usuario = localStorage.getItem("usuario");
  const auth = document.getElementById("auth");
  const userInfo = document.getElementById("user-info");
  const nomeUsuario = document.getElementById("nomeUsuario");

  if (usuario) {
    auth.hidden = true;
    userInfo.hidden = false;
    nomeUsuario.innerText = usuario;
  } else {
    auth.hidden = false;
    userInfo.hidden = true;
    nomeUsuario.innerText = "";
  }
}

function finalizarCompra() {
  const usuario = localStorage.getItem("usuario");

  if (!usuario) {
    alert("Faça login para finalizar a compra");
    return;
  }

  if (carrinho.length === 0) {
    alert("Carrinho vazio");
    return;
  }

  alert("Compra realizada com sucesso!");

  carrinho = [];
  localStorage.removeItem("carrinho");
  renderizarCarrinho();
}

renderizarProdutos();
renderizarCarrinho();
atualizarUsuario();
