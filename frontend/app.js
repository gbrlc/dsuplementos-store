// =====================
// PRODUTOS
// =====================
let products = [
  { id: 1, nome: "Whey Protein", preco: 119.90, imagem: "assets/products/wheyintegral.jpeg" },
  { id: 2, nome: "Creatina", preco: 89.90, imagem: "assets/products/creatinaabsolut.jpeg" },
  { id: 3, nome: "Bone Crusher", preco: 89.90, imagem: "assets/products/colageno2.jpeg" },
  { id: 4, nome: "Colageno", preco: 89.90, imagem: "assets/products/colageno.jpeg" },
  { id: 5, nome: "Creatina Black", preco: 89.90, imagem: "assets/products/creatina3.jpeg" },
  { id: 6, nome: "Creatina Mono", preco: 89.90, imagem: "assets/products/creatina.jpeg" },
  { id: 7, nome: "Multi", preco: 89.90, imagem: "assets/products/multi.jpeg" },
  { id: 8, nome: "WHEY INTEGRAL", preco: 89.90, imagem: "assets/products/wheyintegral.jpeg" },
  { id: 9, nome: "Whey Max", preco: 89.90, imagem: "assets/products/wheymax.jpeg" },
  { id: 10, nome: "WHEY ZEO", preco: 89.90, imagem: "assets/products/wheyy.jpeg" },
  { id: 11, nome: "Whey", preco: 79.90, imagem: "assets/products/multi.jpeg" },
  { id: 12, nome: "Whey", preco: 79.90, imagem: "assets/products/multi.jpeg" }
];

// =====================
// ELEMENTOS HTML
// =====================
const productsContainer = document.getElementById("products");
const carrinhoContainer = document.getElementById("carrinho");

// =====================
// CARRINHO (com persistência)
// =====================
let carrinho = JSON.parse(localStorage.getItem("carrinho")) || [];

// =====================
// SALVAR CARRINHO
// =====================
function salvarCarrinho() {
  localStorage.setItem("carrinho", JSON.stringify(carrinho));
}

// =====================
// RENDERIZAR PRODUTOS
// =====================
function renderizarProdutos() {
  productsContainer.innerHTML = "";

  products.forEach(product => {
    const card = document.createElement("div");
    card.className = "product-card";

    card.innerHTML = `
      <img src="${product.imagem}" alt="${product.nome}">
      <h3>${product.nome}</h3>
      <p>R$ ${product.preco.toFixed(2)}</p>
      <button>Adicionar ao carrinho</button>
    `;

    card.querySelector("button").addEventListener("click", () => {
      adicionarAoCarrinho(product);
    });

    productsContainer.appendChild(card);
  });
}

// =====================
// ADICIONAR
// =====================
function adicionarAoCarrinho(produto) {
  carrinho.push(produto);
  salvarCarrinho();
  renderizarCarrinho();
}

// =====================
// REMOVER ITEM
// =====================
function removerItem(index) {
  carrinho.splice(index, 1);
  salvarCarrinho();
  renderizarCarrinho();
}

// =====================
// RENDERIZAR CARRINHO
// =====================
function renderizarCarrinho() {
  carrinhoContainer.innerHTML = "<h2>Carrinho</h2>";

  let total = 0;

  carrinho.forEach((item, index) => {
    total += item.preco;

    carrinhoContainer.innerHTML += `
      <div style="margin-bottom:10px;">
        <h3>Total: R$ ${total.toFixed(2)}</h3>
        <button onclick="finalizarCompra()">Finalizar Compra</button>
      </div>
    `;
  });

  carrinhoContainer.innerHTML += `
    <h3>Total: R$ ${total.toFixed(2)}</h3>
  `;
}

// =====================
// INICIAR
// =====================
renderizarProdutos();
renderizarCarrinho();


// =====================
// LOGIN
// =====================
function login() {
  const usuario = document.getElementById("usuario").value;

  if (!usuario) {
    alert("Digite um usuário");
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

  if (usuario) {
    document.getElementById("auth").style.display = "none";
    document.getElementById("user-info").style.display = "block";
    document.getElementById("nomeUsuario").innerText = usuario;
  } else {
    document.getElementById("auth").style.display = "block";
    document.getElementById("user-info").style.display = "none";
  }
}
atualizarUsuario();
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