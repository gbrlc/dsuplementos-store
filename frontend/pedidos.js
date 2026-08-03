const ordersList = document.getElementById("orders-list");

async function carregarPedidos() {
  if (!obterSessao()?.token) {
    window.location.href = "login.html?redirect=pedidos.html";
    return;
  }

  try {
    const pedidos = await requisicaoApi("/pedidos");
    renderizarPedidos(pedidos);
  } catch (erro) {
    ordersList.innerHTML = `<p class="empty-state">${escaparHtml(erro.message)}</p>`;
  }
}

function renderizarPedidos(pedidos) {
  if (!pedidos.length) {
    ordersList.innerHTML = '<p class="empty-state">Você ainda não fez nenhum pedido.<br /><a class="button button-primary" href="index.html">Ir para a loja</a></p>';
    return;
  }

  ordersList.innerHTML = pedidos.map((pedido) => `
    <article class="order-row">
      <div class="order-header">
        <div><p class="eyebrow">Pedido #${pedido.id}</p><h2>${formatarData(pedido.criadoEm)}</h2></div>
        <span class="status-badge ${classeStatus(pedido.status)}">${statusLegivel(pedido.status)}</span>
      </div>
      <div class="order-content">
        <div>
          <strong>Entrega</strong>
          <p>${escaparHtml(descreverEnderecoPedido(pedido.endereco))}</p>
        </div>
        <div>
          <strong>Itens</strong>
          <p>${pedido.itens.map((item) => `${escaparHtml(item.nome)} × ${item.quantidade}`).join(", ")}</p>
        </div>
        <div class="order-total"><strong>Total</strong><span>${formatarMoeda(pedido.total)}</span></div>
      </div>
      ${acoesPedido(pedido)}
    </article>
  `).join("");

  ordersList.querySelectorAll("[data-pay-order]").forEach((button) => {
    button.addEventListener("click", () => atualizarPedido(button, "pagamento-simulado"));
  });
  ordersList.querySelectorAll("[data-cancel-order]").forEach((button) => {
    button.addEventListener("click", () => atualizarPedido(button, "cancelamento"));
  });
}

function acoesPedido(pedido) {
  if (pedido.status === "CRIADO") {
    return `<div class="order-actions"><button class="button button-primary" type="button" data-pay-order="${pedido.id}">Simular pagamento</button><button class="button button-secondary" type="button" data-cancel-order="${pedido.id}">Cancelar pedido</button></div>`;
  }
  if (pedido.status === "PAGO") {
    return `<div class="order-actions"><button class="button button-secondary" type="button" data-cancel-order="${pedido.id}">Cancelar pedido</button></div>`;
  }
  return "";
}

async function atualizarPedido(button, acao) {
  const pedidoId = Number(button.dataset.payOrder || button.dataset.cancelOrder);
  button.disabled = true;
  try {
    await requisicaoApi(`/pedidos/${pedidoId}/${acao}`, { method: "POST" });
    await carregarPedidos();
    mostrarToast(acao === "pagamento-simulado" ? "Pagamento confirmado." : "Pedido cancelado.");
  } catch (erro) {
    mostrarToast(erro.message, "erro");
    button.disabled = false;
  }
}

function statusLegivel(status) {
  return status.replaceAll("_", " ").toLowerCase().replace(/(^|\s)\S/g, (letra) => letra.toUpperCase());
}

function classeStatus(status) {
  return status === "CANCELADO" ? "inactive" : status === "PAGO" ? "active" : "pending";
}

function descreverEnderecoPedido(endereco) {
  return `${endereco.logradouro}, ${endereco.numero}${endereco.complemento ? `, ${endereco.complemento}` : ""} - ${endereco.cidade}/${endereco.estado}`;
}

function formatarData(valor) {
  return new Date(valor).toLocaleDateString("pt-BR", { dateStyle: "long" });
}

carregarPedidos();
