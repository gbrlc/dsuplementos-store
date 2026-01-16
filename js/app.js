const products = [
  {
    id: 1,
    nome: "Whey Protein",
    preco: 119.90,
    imagem: "assets/products/wheyhd.png"
  },
  {
    id: 2,
    nome: "Creatina",
    preco: 89.90,
    imagem: "assets/products/creatinaabsolut.png"
  },
  {
    id: 3,
    nome: "Bone Crusher",
    preco: 89.90,
    imagem: "assets/products/bonecrusher.png"
  },
  {
    id: 4,
    nome: "Colageno",
    preco: 89.90,
    imagem: "assets/products/colageno.png"
  },
  {
    id: 5,
    nome: "Creatina Black",
    preco: 89.90,
    imagem: "assets/products/creatinablackskull.png"
  },
  {
    id: 6,
    nome: "Creatina Mono",
    preco: 89.90,
    imagem: "assets/products/creatinamono.png"
  },
  {
    id: 7,
    nome: "Multi",
    preco: 89.90,
    imagem: "assets/products/multi.png"
  },
  {
    id: 8,
    nome: "WHEY INTEGRAL",
    preco: 89.90,
    imagem: "assets/products/wheyintegral.png"
  },
  {
    id: 9,
    nome: "wheymax",
    preco: 89.90,
    imagem: "assets/products/wheymax.png"
  },

  {
    id: 9,
    nome: "WHEY ZEO",
    preco: 89.90,
    imagem: "assets/products/wheyzero.png"
  },
 
  // depois você adiciona os outros 10
];

const productsContainer = document.getElementById("products");

products.forEach(product => {
  const card = document.createElement("div");
  card.className = "product-card";

  card.innerHTML = `
    <img src="${product.imagem}" alt="${product.nome}">
    <h3>${product.nome}</h3>
    <p>R$ ${product.preco.toFixed(2)}</p>
    <button>Adicionar ao carrinho</button>
  `;

  productsContainer.appendChild(card);
});