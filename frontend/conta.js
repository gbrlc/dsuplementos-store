const accountContent = document.getElementById("account-content");

async function carregarConta() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=conta.html";
    return;
  }

  try {
    const usuario = await requisicaoApi("/auth/me");
    accountContent.innerHTML = `
      <p class="eyebrow">Minha conta</p>
      <h1>${escaparHtml(usuario.nome)}</h1>
      <dl class="account-details">
        <div><dt>E-mail</dt><dd>${escaparHtml(usuario.email)}</dd></div>
        <div><dt>Perfil</dt><dd>${usuario.role === "ADMIN" ? "Gestor" : "Cliente"}</dd></div>
      </dl>
      ${usuario.role === "ADMIN" ? '<a class="button button-primary" href="dashboard.html">Acessar gestão</a>' : '<a class="button button-primary" href="index.html#catalogo">Ver catálogo</a>'}
    `;
  } catch (erro) {
    accountContent.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

carregarConta();
