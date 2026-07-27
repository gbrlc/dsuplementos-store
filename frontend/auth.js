const loginForm = document.getElementById("login-form");
const cadastroForm = document.getElementById("cadastro-form");
const feedback = document.getElementById("auth-feedback");
const redirect = new URLSearchParams(window.location.search).get("redirect");

function trocarAba(nome) {
  const loginAtivo = nome === "login";
  loginForm.hidden = !loginAtivo;
  cadastroForm.hidden = loginAtivo;
  document.querySelectorAll("[data-auth-tab]").forEach((aba) => {
    const ativa = aba.dataset.authTab === nome;
    aba.classList.toggle("active", ativa);
    aba.setAttribute("aria-selected", String(ativa));
  });
  feedback.textContent = "";
}

function mostrarFeedback(mensagem, tipo = "erro") {
  feedback.textContent = mensagem;
  feedback.dataset.tipo = tipo;
}

function destinoDepoisDoLogin(usuario) {
  if (redirect && /^[a-zA-Z0-9_-]+\.html$/.test(redirect)) {
    return redirect;
  }
  return usuario.role === "ADMIN" ? "dashboard.html" : "index.html";
}

async function enviarAutenticacao(form, caminho, incluiNome) {
  const button = form.querySelector("button[type=submit]");
  const dados = new FormData(form);
  const payload = {
    email: dados.get("email"),
    senha: dados.get("senha")
  };
  if (incluiNome) payload.nome = dados.get("nome");

  button.disabled = true;
  button.textContent = incluiNome ? "Criando..." : "Entrando...";
  try {
    const sessao = await requisicaoApi(caminho, {
      method: "POST",
      body: JSON.stringify(payload)
    });
    salvarSessao(sessao);
    window.location.href = destinoDepoisDoLogin(sessao.usuario);
  } catch (erro) {
    mostrarFeedback(erro.message);
  } finally {
    button.disabled = false;
    button.textContent = incluiNome ? "Criar conta" : "Entrar";
  }
}

document.querySelectorAll("[data-auth-tab]").forEach((aba) => {
  aba.addEventListener("click", () => trocarAba(aba.dataset.authTab));
});

loginForm.addEventListener("submit", (event) => {
  event.preventDefault();
  enviarAutenticacao(loginForm, "/auth/login", false);
});

cadastroForm.addEventListener("submit", (event) => {
  event.preventDefault();
  enviarAutenticacao(cadastroForm, "/auth/cadastro", true);
});

if (obterSessao()?.usuario) {
  mostrarFeedback("Você já está com uma conta ativa.", "sucesso");
}
