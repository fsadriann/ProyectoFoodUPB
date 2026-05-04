package edu.fsadriann.client.controller.order;

import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

public class OrderController {

	private final ClientModel model;
	private final OperatorView view;

	public OrderController(ClientModel model, OperatorView view) {
		this.model = model;
		this.view = view;
	}

	public boolean createOrderForClient(User client) {
		if (client == null) {
			view.showError("Primero busca un cliente válido.");
			return false;
		}
		Order existingOrder = model.getCurrentOrder();
		if (existingOrder != null) {
			model.cancelarPedido(existingOrder.getOrderId());
		}
		Order order = model.crearPedido(client.getCedula(), client.isPremium());
		if (order == null) {
			view.showError("No se pudo crear el pedido.");
			return false;
		}
		refreshCurrentOrderTable(order);
		clearInvoiceView();
		view.showTab(1);
		view.setMessage("Pedido creado para " + client.getNombreCompleto().trim() + ".");
		return true;
	}

	public void addProduct(Product product) {
		if (product == null) {
			view.showError("Selecciona un producto válido.");
			return;
		}

		Order currentOrder = ensureCurrentOrder();
		if (currentOrder == null) {
			return;
		}

		if (!model.agregarProductoAPedido(currentOrder.getOrderId(), product)) {
			view.showError("No se pudo agregar el producto al pedido.");
			return;
		}

		refreshCurrentOrderTable(currentOrder);
		view.setMessage("Producto agregado: " + product.getNombre());
	}

	public void generateInvoice() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder == null) {
			view.showError("No hay un pedido activo para facturar.");
			return;
		}

		double total = model.calcularFactura(currentOrder);
		if (total <= 0 && currentOrder.getSubtotal() <= 0) {
			view.showError("No se pudo calcular la factura.");
			return;
		}

		refreshInvoiceView(currentOrder);
		view.showTab(2);
		view.setMessage("Factura calculada correctamente.");
	}

	public void clearOrder() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder != null) {
			model.cancelarPedido(currentOrder.getOrderId());
		}
		model.clearCurrentOrder();
		clearCurrentOrderView();
		view.setMessage("Pedido actual limpiado.");
	}

	public void clearInvoiceOrder() {
		clearInvoiceView();
		view.setMessage("Factura limpiada.");
	}

	public void removeSelectedProduct() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder == null) {
			view.showError("No hay un pedido activo.");
			return;
		}

		int row = view.getSelectedCurrentOrderRow();
		Product selectedProduct = getProductAtRow(currentOrder, row);
		if (selectedProduct == null) {
			view.showError("Selecciona un producto del pedido actual.");
			return;
		}

		if (!view.confirm("¿Quitar " + selectedProduct.getNombre() + " del pedido?")) {
			return;
		}

		if (!model.quitarProductoAPedido(currentOrder.getOrderId(), selectedProduct)) {
			view.showError("No se pudo quitar el producto.");
			return;
		}

		refreshCurrentOrderTable(currentOrder);
		view.setMessage("Producto eliminado del pedido.");
	}

	public void changeSelectedProductQuantity() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder == null) {
			view.showError("No hay un pedido activo.");
			return;
		}

		int row = view.getSelectedCurrentOrderRow();
		Product selectedProduct = getProductAtRow(currentOrder, row);
		if (selectedProduct == null) {
			view.showError("Selecciona un producto del pedido actual.");
			return;
		}

		String input = JOptionPane.showInputDialog(view,
				"Nueva cantidad para " + selectedProduct.getNombre() + ":",
				selectedProduct.getCantidad());
		if (input == null) {
			return;
		}

		int quantity;
		try {
			quantity = Integer.parseInt(input.trim());
		} catch (NumberFormatException ex) {
			view.showError("La cantidad debe ser numérica.");
			return;
		}

		if (quantity <= 0) {
			view.showError("La cantidad debe ser mayor que cero.");
			return;
		}

		selectedProduct.setCantidad(quantity);
		if (!model.modificarPedido(currentOrder)) {
			view.showError("No se pudo actualizar el pedido.");
			return;
		}

		refreshCurrentOrderTable(currentOrder);
		view.setMessage("Cantidad actualizada.");
	}

	public void sendToKitchen() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder == null) {
			view.showError("No hay un pedido activo para enviar a cocina.");
			return;
		}

		if (currentOrder.getSubtotal() <= 0) {
			model.calcularFactura(currentOrder);
		}

		if (!model.enviarPedidoACocina(currentOrder)) {
			view.showError("No se pudo enviar el pedido a cocina.");
			return;
		}

		refreshInvoiceView(currentOrder);
		model.clearCurrentOrder();
		view.setMessage("Pedido enviado a cocina.");
		view.showSuccess("Pedido enviado a cocina correctamente.");
	}

	public ClientModel getModel() {
		return model;
	}

	private Order ensureCurrentOrder() {
		Order currentOrder = model.getCurrentOrder();
		if (currentOrder != null) {
			return currentOrder;
		}

		User currentClient = model.getCurrentClient();
		if (currentClient == null) {
			view.showError("Primero selecciona un cliente.");
			return null;
		}

		return createOrderForClient(currentClient) ? model.getCurrentOrder() : null;
	}

	private void refreshCurrentOrderTable(Order order) {
		DefaultTableModel tableModel = view.getCurrentOrderModel();
		tableModel.setRowCount(0);
		if (order == null) {
			return;
		}

		Iterator<Product> iterator = order.getCarrito().iterator();
		while (iterator.hasNext()) {
			Product product = iterator.next();
			if (product == null) {
				continue;
			}
			tableModel.addRow(new Object[]{product.getNombre(), product.getCantidad()});
		}
	}

	private void refreshInvoiceView(Order order) {
		User client = model.getCurrentClient();
		view.setInvoiceClient(client != null ? client.getNombreCompleto().trim() : order.getCedulaCliente());
		view.setInvoiceTipo(client != null && client.isPremium() ? "Premium" : "Estándar");
		view.setInvoiceDireccion(client != null ? safeText(client.getDireccion()) : "—");
		view.setInvoiceCuadrante(safeText(order.getCuadranteDestino()));
		view.setInvoiceSubtotal(formatMoney(order.getSubtotal()));
		view.setInvoiceIva(formatMoney(order.getImpuesto()));
		view.setInvoiceDomicilio(formatMoney(order.getCostoDomi()));
		view.setInvoiceTotal(formatMoney(order.getTotal()));

		DefaultTableModel invoiceModel = view.getInvoiceOrderModel();
		invoiceModel.setRowCount(0);
		Iterator<Product> iterator = order.getCarrito().iterator();
		while (iterator.hasNext()) {
			Product product = iterator.next();
			if (product == null) {
				continue;
			}
			invoiceModel.addRow(new Object[]{product.getNombre(), product.getCantidad()});
		}
	}

	private void clearCurrentOrderView() {
		view.getCurrentOrderModel().setRowCount(0);
	}

	private void clearInvoiceView() {
		view.setInvoiceClient("—");
		view.setInvoiceTipo("—");
		view.setInvoiceDireccion("—");
		view.setInvoiceCuadrante("—");
		view.setInvoiceSubtotal("$0");
		view.setInvoiceIva("$0");
		view.setInvoiceDomicilio("$0");
		view.setInvoiceTotal("$0");
		DefaultTableModel invoiceModel = view.getInvoiceOrderModel();
		if (invoiceModel != null) {
			invoiceModel.setRowCount(0);
		}
	}

	private Product getProductAtRow(Order order, int row) {
		if (order == null || row < 0 || row >= order.getCarrito().size()) {
			return null;
		}
		Object[] items = order.getCarrito().toArray();
		if (row >= items.length) {
			return null;
		}
		Object value = items[row];
		return value instanceof Product ? (Product) value : null;
	}

	private String formatMoney(double value) {
		return "$" + Math.round(value);
	}

	private String safeText(String value) {
		return value == null || value.isBlank() ? "—" : value;
	}
}
