const checkoutContent = document.getElementById("checkout-content");

async function iniciarCheckout() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=checkout.html";
    return;
  }

  try {
    const [carrinho, enderecos, frete] = await Promise.all([
      requisicaoApi("/carrinho"),
      requisicaoApi("/usuarios/me/enderecos"),
      requisicaoApi("/pedidos/simulacao-frete")
    ]);
    renderizarCheckout(carrinho, enderecos, frete);
  } catch (erro) {
    checkoutContent.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}<br /><a class="button button-secondary" href="index.html">Voltar à loja</a></p>`;
  }
}

function renderizarCheckout(carrinho, enderecos, frete) {
  if (!carrinho.itens.length) {
    checkoutContent.innerHTML = '<p class="empty-state">Seu carrinho está vazio.<br /><a class="button button-primary" href="index.html">Explorar catálogo</a></p>';
    return;
  }
  if (!enderecos.length) {
    checkoutContent.innerHTML = `
      <section class="checkout-heading"><p class="eyebrow">Checkout</p><h1>Falta definir a entrega.</h1></section>
      <section class="checkout-block checkout-empty">
        <p class="muted-text">Cadastre um endereço antes de criar o pedido.</p>
        <a class="button button-primary" href="conta.html">Cadastrar endereço</a>
      </section>
    `;
    return;
  }

  checkoutContent.innerHTML = `
    <section class="checkout-heading">
      <p class="eyebrow">Checkout</p>
      <h1>Revise e confirme seu pedido.</h1>
    </section>
    <div class="checkout-layout">
      <div class="checkout-main">
        <section class="checkout-block">
          <div class="panel-heading"><div><p class="eyebrow">Entrega</p><h2>Onde deseja receber?</h2></div><a class="table-action" href="conta.html">Gerenciar</a></div>
          <label>
            <span>Endereço</span>
            <select id="checkout-address">
              ${enderecos.map((endereco) => `<option value="${endereco.id}" ${endereco.principal ? "selected" : ""}>${escaparHtml(descreverEndereco(endereco))}</option>`).join("")}
            </select>
          </label>
        </section>
        <section class="checkout-block">
          <div class="panel-heading"><div><p class="eyebrow">Itens</p><h2>Seu carrinho</h2></div><a class="table-action" href="index.html">Editar</a></div>
          <ul class="checkout-items">
            ${carrinho.itens.map((item) => `
              <li><span>${escaparHtml(item.nome)} <small>× ${item.quantidade}</small></span><strong>${formatarMoeda(item.subtotal)}</strong></li>
            `).join("")}
          </ul>
        </section>
      </div>
      <aside class="checkout-summary">
        <p class="eyebrow">Resumo</p>
        <h2>Valores do pedido</h2>
        <dl class="summary-values">
          <div><dt>Produtos</dt><dd>${formatarMoeda(frete.subtotal)}</dd></div>
          <div><dt>Frete</dt><dd>${Number(frete.frete) === 0 ? "Grátis" : formatarMoeda(frete.frete)}</dd></div>
          <div class="summary-total"><dt>Total</dt><dd>${formatarMoeda(frete.total)}</dd></div>
        </dl>
        <form id="checkout-form">
          <p id="checkout-feedback" class="form-feedback" aria-live="polite"></p>
          <button class="button button-primary" type="submit">Criar pedido</button>
        </form>
      </aside>
    </div>
  `;
  document.getElementById("checkout-form").addEventListener("submit", criarPedido);
}

function descreverEndereco(endereco) {
  return `${endereco.apelido || "Endereço"}: ${endereco.logradouro}, ${endereco.numero} - ${endereco.cidade}/${endereco.estado}`;
}

async function criarPedido(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const feedback = document.getElementById("checkout-feedback");
  const button = form.querySelector("button");
  button.disabled = true;
  feedback.textContent = "";

  try {
    const pedido = await requisicaoApi("/pedidos", {
      method: "POST",
      body: JSON.stringify({ enderecoId: Number(document.getElementById("checkout-address").value) })
    });
    await atualizarCarrinho();
    checkoutContent.innerHTML = `
      <section class="checkout-success">
        <p class="eyebrow">Pedido criado</p>
        <h1>Pedido #${pedido.id} recebido.</h1>
        <p>O estoque foi reservado. Nesta versão de estudo, você pode simular o pagamento na página de pedidos.</p>
        <a class="button button-primary" href="pedidos.html">Ver meus pedidos</a>
      </section>
    `;
  } catch (erro) {
    feedback.textContent = erro.message;
    feedback.dataset.tipo = "erro";
  } finally {
    button.disabled = false;
  }
}

iniciarCheckout();
