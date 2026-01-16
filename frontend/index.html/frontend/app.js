
fetch("http://localhost:3000/products")
  .then(res => res.json())
  .then(data => {
    const container = document.getElementById("products");

    data.forEach(p => {
      container.innerHTML += `
        <div class="card">
          <img src="${p.image}" width="150" />
          <h3>${p.name}</h3>
          <p>${p.description}</p>
          <strong>R$ ${p.price}</strong>
        </div>
      `;
    });
  });

const express = require("express");
const cors = require("cors");
const sqlite3 = require("sqlite3").verbose();

const app = express();
app.use(cors());
app.use(express.json());

const db = new sqlite3.Database("./database.sqlite");

// Criar tabela
db.run(`
  CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    price REAL,
    image TEXT,
    description TEXT
  )
`);

// Produtos mock
db.run(`
  INSERT INTO products (name, price, image, description)
  VALUES 
  ('Whey Protein', 129.90, 'https://via.placeholder.com/150', 'Whey concentrado 1kg'),
  ('Creatina', 79.90, 'https://via.placeholder.com/150', 'Creatina monohidratada'),
  ('Pré-Treino', 99.90, 'https://via.placeholder.com/150', 'Energia e foco')
`);

// Listar produtos
app.get("/products", (req, res) => {
  db.all("SELECT * FROM products", [], (err, rows) => {
    res.json(rows);
  });
});

app.listen(3000, () => {
  console.log("API rodando em http://localhost:3000");
});
