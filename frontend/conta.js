const accountContent = document.getElementById("account-content");
let enderecoEmEdicao = null;

async function carregarConta() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=conta.html";
    return;
  }

  try {
    const usuario = await requisicaoApi("/usuarios/me");
    const sessao = obterSessao();
    salvarSessao({ ...sessao, usuario });
    renderizarConta(usuario);
    await carregarEnderecos();
  } catch (erro) {
    accountContent.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

function renderizarConta(usuario) {
  const perfil = usuario.role === "ADMIN" ? "Gestor" : "Cliente";
  accountContent.innerHTML = `
    <section class="account-summary">
      <p class="eyebrow">Minha conta</p>
      <h1>${escaparHtml(usuario.nome)}</h1>
      <dl class="account-details">
        <div><dt>E-mail</dt><dd>${escaparHtml(usuario.email)}</dd></div>
        <div><dt>Perfil</dt><dd>${perfil}</dd></div>
      </dl>
      <div class="account-shortcuts">
        ${usuario.role === "ADMIN" ? '<a class="button button-secondary" href="dashboard.html">Abrir gestão</a>' : '<a class="button button-secondary" href="index.html#catalogo">Voltar ao catálogo</a>'}
        <a class="button button-secondary" href="pedidos.html">Meus pedidos</a>
      </div>
    </section>

    <div class="account-layout">
      <section class="account-panel">
        <div class="panel-heading">
          <div><p class="eyebrow">Dados pessoais</p><h2>Seu perfil</h2></div>
        </div>
        <form id="profile-form" class="account-form">
          <label>
            <span>Nome</span>
            <input name="nome" value="${escaparHtml(usuario.nome)}" maxlength="120" required />
          </label>
          <div class="form-actions">
            <button class="button button-primary" type="submit">Salvar nome</button>
            <p id="profile-feedback" class="form-feedback" aria-live="polite"></p>
          </div>
        </form>
      </section>

      <section class="account-panel">
        <div class="panel-heading">
          <div><p class="eyebrow">Segurança</p><h2>Alterar senha</h2></div>
        </div>
        <form id="password-form" class="account-form">
          <label>
            <span>Senha atual</span>
            <input name="senhaAtual" type="password" autocomplete="current-password" required />
          </label>
          <label>
            <span>Nova senha</span>
            <input name="novaSenha" type="password" autocomplete="new-password" minlength="8" required />
          </label>
          <div class="form-actions">
            <button class="button button-secondary" type="submit">Atualizar senha</button>
            <p id="password-feedback" class="form-feedback" aria-live="polite"></p>
          </div>
        </form>
      </section>

      <section class="account-panel account-addresses">
        <div class="panel-heading">
          <div><p class="eyebrow">Entrega</p><h2>Endereços</h2></div>
          <button class="button button-secondary" id="cancel-address-edit" type="button" hidden>Cancelar edição</button>
        </div>
        <div id="addresses-list" class="address-list" aria-live="polite"></div>
        <form id="address-form" class="account-form address-form">
          <div class="form-grid">
            <label>
              <span>Apelido</span>
              <input name="apelidoVisivel" maxlength="60" placeholder="Casa, trabalho..." />
            </label>
            <label>
              <span>CEP</span>
              <input name="cep" inputmode="numeric" maxlength="9" placeholder="00000-000" required />
            </label>
            <label class="form-full">
              <span>Logradouro</span>
              <input name="logradouro" maxlength="160" required />
            </label>
            <label>
              <span>Número</span>
              <input name="numero" maxlength="20" required />
            </label>
            <label>
              <span>Complemento</span>
              <input name="complemento" maxlength="100" />
            </label>
            <label>
              <span>Bairro</span>
              <input name="bairro" maxlength="100" required />
            </label>
            <label>
              <span>Cidade</span>
              <input name="cidade" maxlength="100" required />
            </label>
            <label>
              <span>UF</span>
              <input name="estado" maxlength="2" autocapitalize="characters" required />
            </label>
            <label class="checkbox-field">
              <input name="principal" type="checkbox" />
              <span>Usar como endereço principal</span>
            </label>
          </div>
          <div class="form-actions">
            <button class="button button-primary" type="submit" id="address-submit">Adicionar endereço</button>
            <p id="address-feedback" class="form-feedback" aria-live="polite"></p>
          </div>
        </form>
      </section>
    </div>
  `;

  document.getElementById("profile-form").addEventListener("submit", atualizarPerfil);
  document.getElementById("password-form").addEventListener("submit", atualizarSenha);
  document.getElementById("address-form").addEventListener("submit", salvarEndereco);
  document.getElementById("cancel-address-edit").addEventListener("click", cancelarEdicaoEndereco);
}

async function atualizarPerfil(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const feedback = document.getElementById("profile-feedback");
  const button = form.querySelector("button");
  button.disabled = true;
  feedback.textContent = "";

  try {
    const usuario = await requisicaoApi("/usuarios/me", {
      method: "PUT",
      body: JSON.stringify({ nome: new FormData(form).get("nome") })
    });
    salvarSessao({ ...obterSessao(), usuario });
    feedback.textContent = "Perfil atualizado.";
    feedback.dataset.tipo = "sucesso";
    document.querySelector(".account-summary h1").textContent = usuario.nome;
  } catch (erro) {
    feedback.textContent = erro.message;
    feedback.dataset.tipo = "erro";
  } finally {
    button.disabled = false;
  }
}

async function atualizarSenha(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const feedback = document.getElementById("password-feedback");
  const button = form.querySelector("button");
  button.disabled = true;
  feedback.textContent = "";

  try {
    const dados = new FormData(form);
    await requisicaoApi("/usuarios/me/senha", {
      method: "PATCH",
      body: JSON.stringify({
        senhaAtual: dados.get("senhaAtual"),
        novaSenha: dados.get("novaSenha")
      })
    });
    form.reset();
    feedback.textContent = "Senha atualizada.";
    feedback.dataset.tipo = "sucesso";
    window.setTimeout(() => {
      encerrarSessao();
      window.location.href = "login.html";
    }, 1000);
  } catch (erro) {
    feedback.textContent = erro.message;
    feedback.dataset.tipo = "erro";
  } finally {
    button.disabled = false;
  }
}

async function carregarEnderecos() {
  const container = document.getElementById("addresses-list");
  if (!container) return;

  try {
    const enderecos = await requisicaoApi("/usuarios/me/enderecos");
    renderizarEnderecos(enderecos);
  } catch (erro) {
    container.innerHTML = `<p class="form-feedback">${escaparHtml(erro.message)}</p>`;
  }
}

function renderizarEnderecos(enderecos) {
  const container = document.getElementById("addresses-list");
  if (!enderecos.length) {
    container.innerHTML = '<p class="muted-text">Você ainda não cadastrou um endereço.</p>';
    return;
  }

  container.innerHTML = enderecos.map((endereco) => `
    <article class="address-row">
      <div>
        <div class="address-title">
          <strong>${escaparHtml(endereco.apelido || "Endereço de entrega")}</strong>
          ${endereco.principal ? '<span class="status-badge active">Principal</span>' : ""}
        </div>
        <p>${escaparHtml(endereco.logradouro)}, ${escaparHtml(endereco.numero)}${endereco.complemento ? `, ${escaparHtml(endereco.complemento)}` : ""}</p>
        <p>${escaparHtml(endereco.bairro)} · ${escaparHtml(endereco.cidade)}/${escaparHtml(endereco.estado)} · ${escaparHtml(endereco.cep)}</p>
      </div>
      <div class="address-actions">
        <button type="button" class="table-action" data-edit-address="${endereco.id}">Editar</button>
        <button type="button" class="table-action danger-action" data-delete-address="${endereco.id}">Remover</button>
      </div>
    </article>
  `).join("");

  container.querySelectorAll("[data-edit-address]").forEach((button) => {
    button.addEventListener("click", () => editarEndereco(enderecos.find((endereco) => endereco.id === Number(button.dataset.editAddress))));
  });
  container.querySelectorAll("[data-delete-address]").forEach((button) => {
    button.addEventListener("click", () => removerEndereco(Number(button.dataset.deleteAddress)));
  });
}

function editarEndereco(endereco) {
  if (!endereco) return;
  enderecoEmEdicao = endereco;
  const form = document.getElementById("address-form");
  form.apelidoVisivel.value = endereco.apelido || "";
  form.cep.value = endereco.cep;
  form.logradouro.value = endereco.logradouro;
  form.numero.value = endereco.numero;
  form.complemento.value = endereco.complemento || "";
  form.bairro.value = endereco.bairro;
  form.cidade.value = endereco.cidade;
  form.estado.value = endereco.estado;
  form.principal.checked = endereco.principal;
  document.getElementById("address-submit").textContent = "Salvar endereço";
  document.getElementById("cancel-address-edit").hidden = false;
  form.scrollIntoView({ behavior: "smooth", block: "start" });
}

function cancelarEdicaoEndereco() {
  enderecoEmEdicao = null;
  const form = document.getElementById("address-form");
  form.reset();
  document.getElementById("address-feedback").textContent = "";
  document.getElementById("address-submit").textContent = "Adicionar endereço";
  document.getElementById("cancel-address-edit").hidden = true;
}

async function salvarEndereco(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const feedback = document.getElementById("address-feedback");
  const button = document.getElementById("address-submit");
  const dados = new FormData(form);
  const payload = {
    apelido: dados.get("apelidoVisivel"),
    cep: dados.get("cep"),
    logradouro: dados.get("logradouro"),
    numero: dados.get("numero"),
    complemento: dados.get("complemento"),
    bairro: dados.get("bairro"),
    cidade: dados.get("cidade"),
    estado: dados.get("estado"),
    principal: form.principal.checked
  };
  button.disabled = true;
  feedback.textContent = "";
  const atualizando = Boolean(enderecoEmEdicao);

  try {
    await requisicaoApi(
      enderecoEmEdicao ? `/usuarios/me/enderecos/${enderecoEmEdicao.id}` : "/usuarios/me/enderecos",
      { method: enderecoEmEdicao ? "PUT" : "POST", body: JSON.stringify(payload) }
    );
    cancelarEdicaoEndereco();
    await carregarEnderecos();
    mostrarToast(atualizando ? "Endereço atualizado." : "Endereço adicionado.");
  } catch (erro) {
    feedback.textContent = erro.message;
    feedback.dataset.tipo = "erro";
  } finally {
    button.disabled = false;
  }
}

async function removerEndereco(enderecoId) {
  const confirmar = window.confirm("Remover este endereço?");
  if (!confirmar) return;

  try {
    await requisicaoApi(`/usuarios/me/enderecos/${enderecoId}`, { method: "DELETE" });
    if (enderecoEmEdicao?.id === enderecoId) cancelarEdicaoEndereco();
    await carregarEnderecos();
    mostrarToast("Endereço removido.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
  }
}

carregarConta();
