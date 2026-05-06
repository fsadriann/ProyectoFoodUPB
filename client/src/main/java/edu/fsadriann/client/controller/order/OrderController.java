package edu.fsadriann.client.controller.order;

import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderController {

	private static final double IVA            = 0.19;
	private static final double COSTO_DOMI_STD = 5_000.0;

	private static final NumberFormat COP = NumberFormat.getNumberInstance(new Locale("es", "CO"));

	private final ClientModel  model;
	private final OperatorView view;

	private java.util.List<Product> currentOrderProducts = new java.util.ArrayList<>();

	public OrderController(ClientModel model, OperatorView view) {
		this.model = model;
		this.view  = view;
	}

	// ── Crear pedido ──────────────────────────────────────────────────────────

	public void createOrderForClient(User client) {
		if (client == null) {
			view.showError("Cliente inválido para crear pedido.");
			return;
		}

		Order existing = model.getCurrentOrder();
		if (existing != null && existing.getEstado() == EstadoPedido.PENDIENTE) {
			model.cancelarPedido(existing.getOrderId());
		}

		boolean isPremium = client.isPremium();
		Order order = model.crearPedido(client.getCedula(), isPremium);

		if (order == null) {
			view.showError("No se pudo crear el pedido. Verifica la conexión con el servidor.");
			return;
		}

		clearOrderTable();
		clearInvoiceView();

		view.setMessage("Pedido " + shortId(order.getOrderId()) + " creado para "
				+ client.getNombreCompleto().trim()
				+ (isPremium ? " [Premium – domicilio gratis]" : " [Estándar]"));
		view.showTab(1);
	}

	// ── Agregar producto ──────────────────────────────────────────────────────

	public void addProduct(Product product) {
		if (product == null) {
			view.setMessage("Selecciona un producto válido.");
			return;
		}
		if (!product.isDisponible()) {
			view.showError("El producto «" + product.getNombre() + "» no está disponible.");
			return;
		}

		Order order = model.getCurrentOrder();
		if (order == null) {
			view.showError("No hay un pedido activo. Busca primero un cliente.");
			return;
		}

		int existingIndex = findProductInCurrentOrder(product.getProductoId());

		if (existingIndex >= 0) {
			Product existing = currentOrderProducts.get(existingIndex);
			existing.setCantidad(existing.getCantidad() + 1);

			DefaultTableModel tableModel = view.getCurrentOrderModel();
			tableModel.setValueAt(existing.getCantidad(), existingIndex, 1);
			tableModel.setValueAt(
					"$" + COP.format((long) existing.getPrecio() * existing.getCantidad()),
					existingIndex, 3);

			syncProductToServer(order, existing);
		} else {
			Product toAdd = cloneWithQuantity(product, 1);
			boolean added = model.agregarProductoAPedido(order.getOrderId(), toAdd);
			if (!added) {
				view.showError("No se pudo agregar el producto al pedido.");
				return;
			}
			currentOrderProducts.add(toAdd);

			view.getCurrentOrderModel().addRow(new Object[]{
					toAdd.getNombre(),
					toAdd.getCantidad(),
					"$" + COP.format(toAdd.getPrecio()),
					"$" + COP.format((long) toAdd.getPrecio() * toAdd.getCantidad())
			});
		}

		// FIX 2: actualizar total en tiempo real
		refreshOrderTotal();
		view.setMessage("«" + product.getNombre() + "» agregado al pedido.");
	}

	// ── Quitar producto ───────────────────────────────────────────────────────

	public void removeSelectedProduct() {
		int row = view.getSelectedCurrentOrderRow();
		if (row < 0 || row >= currentOrderProducts.size()) {
			view.showError("Selecciona un producto de la lista del pedido para quitarlo.");
			return;
		}

		Order order = model.getCurrentOrder();
		if (order == null) {
			view.showError("No hay un pedido activo.");
			return;
		}

		Product toRemove = currentOrderProducts.get(row);
		boolean removed = model.quitarProductoAPedido(order.getOrderId(), toRemove);

		if (removed) {
			currentOrderProducts.remove(row);
			view.getCurrentOrderModel().removeRow(row);
			view.clearOrderSelection(); // FIX 3: limpiar selección guardada
			refreshOrderTotal();        // FIX 2: actualizar total
			view.setMessage("«" + toRemove.getNombre() + "» eliminado del pedido.");
		} else {
			view.showError("No se pudo eliminar el producto. Verifica el estado del pedido.");
		}
	}

	// ── Cambiar cantidad ──────────────────────────────────────────────────────

	public void changeSelectedProductQuantity() {
		int row = view.getSelectedCurrentOrderRow();
		if (row < 0 || row >= currentOrderProducts.size()) {
			view.showError("Selecciona un producto del pedido para cambiar su cantidad.");
			return;
		}

		Order order = model.getCurrentOrder();
		if (order == null) {
			view.showError("No hay un pedido activo.");
			return;
		}

		Product product = currentOrderProducts.get(row);
		String input = JOptionPane.showInputDialog(
				null,
				"Nueva cantidad para «" + product.getNombre() + "»:",
				"Cambiar cantidad",
				JOptionPane.PLAIN_MESSAGE
		);

		if (input == null || input.isBlank()) return;

		int cantidad;
		try {
			cantidad = Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			view.showError("Ingresa un número válido.");
			return;
		}

		if (cantidad <= 0) {
			if (view.confirm("La cantidad es 0. ¿Deseas eliminar el producto del pedido?")) {
				removeSelectedProduct();
			}
			return;
		}

		// FIX: un solo llamado al servidor — modifica directo en el carrito
		boolean ok = model.cambiarCantidadProducto(
				order.getOrderId(), product.getProductoId(), cantidad);

		if (!ok) {
			view.showError("No se pudo actualizar la cantidad.");
			return;
		}

		product.setCantidad(cantidad);

		DefaultTableModel tableModel = view.getCurrentOrderModel();
		tableModel.setValueAt(cantidad, row, 1);
		tableModel.setValueAt("$" + COP.format((long) product.getPrecio() * cantidad), row, 3);

		refreshOrderTotal();
		view.setMessage("Cantidad actualizada a " + cantidad + " para «" + product.getNombre() + "».");
	}
	// ── Generar factura ───────────────────────────────────────────────────────

	public void generateInvoice() {
		Order order = model.getCurrentOrder();
		if (order == null) {
			view.showError("No hay un pedido activo para facturar.");
			return;
		}
		if (currentOrderProducts.isEmpty()) {
			view.showError("El pedido no tiene productos. Agrega al menos uno.");
			return;
		}

		String cuadranteSeleccionado = view.getSelectedCuadrante();
		if (cuadranteSeleccionado == null || cuadranteSeleccionado.isBlank()) {
			view.showError("Selecciona el cuadrante de destino antes de generar la factura.");
			return;
		}
		order.setCuadranteDestino(cuadranteSeleccionado);
		boolean cuadAsignado = model.asignarCuadranteDestino(order.getOrderId(), cuadranteSeleccionado);
		if (!cuadAsignado) {
			view.showError("No se pudo asignar el cuadrante al pedido en el servidor.");
			return;
		}

		double serverTotal = model.calcularFactura(order);

		double subtotal = 0.0;
		for (Product p : currentOrderProducts) {
			subtotal += (double) p.getPrecio() * p.getCantidad();
		}
		double iva   = subtotal * IVA;
		// Usar el domicilio real calculado por el servidor (Dijkstra desde UPB)
		double domi  = order.isPremium() ? 0.0 : Math.max(0, serverTotal - subtotal - iva);
		double total = serverTotal > 0 ? serverTotal : (subtotal + iva + domi);

		User client = model.getCurrentClient();
		if (client != null) {
			view.setInvoiceClient(client.getNombreCompleto().trim());
			view.setInvoiceTipo(order.isPremium() ? "Premium (domicilio gratis)" : "Estándar");
			view.setInvoiceDireccion(client.getDireccion() != null ? client.getDireccion() : "—");
		}

		view.setInvoiceCuadrante(cuadranteSeleccionado);
		view.setInvoiceSubtotal("$" + COP.format(Math.round(subtotal)));
		view.setInvoiceIva("$"       + COP.format(Math.round(iva)));
		view.setInvoiceDomicilio("$" + COP.format(Math.round(domi)));
		view.setInvoiceTotal("$"     + COP.format(Math.round(total)));

		DefaultTableModel invoiceModel = view.getInvoiceOrderModel();
		invoiceModel.setRowCount(0);
		for (Product p : currentOrderProducts) {
			invoiceModel.addRow(new Object[]{
					p.getNombre(),
					p.getCantidad(),
					"$" + COP.format(p.getPrecio()),
					"$" + COP.format((long) p.getPrecio() * p.getCantidad())
			});
		}

		view.showTab(2);
		view.setMessage("Factura generada para " + cuadranteSeleccionado
				+ ". Total: $" + COP.format(Math.round(total)));
	}

	// ── Enviar a cocina ───────────────────────────────────────────────────────

	public void sendToKitchen() {
		Order order = model.getCurrentOrder();
		if (order == null) {
			view.showError("No hay pedido activo.");
			return;
		}
		if (currentOrderProducts.isEmpty()) {
			view.showError("El pedido no tiene productos.");
			return;
		}
		if (!view.confirm("¿Confirmas el envío del pedido a cocina?")) {
			return;
		}

		boolean sent = model.enviarPedidoACocina(order);

		if (sent) {
			view.showSuccess("¡Pedido enviado a cocina exitosamente!");
			view.showTab(3);
			view.setMessage("Pedido " + shortId(order.getOrderId()) + " en preparación.");
			clearOrderTable();
			clearInvoiceView();
			model.clearCurrentOrder();
		} else {
			view.showError(
					"No se pudo enviar el pedido a cocina.\n" +
							"Verifica que el pedido esté en estado PENDIENTE y tenga productos.");
		}
	}

	// ── Limpiar pedido / factura ──────────────────────────────────────────────

	public void clearOrder() {
		Order order = model.getCurrentOrder();
		if (order == null) {
			clearOrderTable();
			return;
		}
		if (!view.confirm("¿Limpiar todos los productos del pedido actual?")) {
			return;
		}

		User client = model.getCurrentClient();
		boolean cancelled = model.cancelarPedido(order.getOrderId());

		if (cancelled && client != null) {
			clearOrderTable();
			Order newOrder = model.crearPedido(client.getCedula(), client.isPremium());
			if (newOrder == null) {
				view.showError("No se pudo reiniciar el pedido.");
				return;
			}
			view.setMessage("Pedido limpiado. Nuevo pedido: " + shortId(newOrder.getOrderId()));
		} else if (!cancelled) {
			clearOrderTable();
			view.setMessage("Vista del pedido limpiada.");
		}
	}

	public void clearInvoiceOrder() {
		clearInvoiceView();
		view.setMessage("Factura limpiada.");
	}

	// ── Helpers privados ──────────────────────────────────────────────────────

	// Accesible desde OperatorController para refrescar cuando cambia el cuadrante
    public void refreshOrderTotal() {
		double subtotal = 0.0;
		for (Product p : currentOrderProducts) {
			subtotal += (double) p.getPrecio() * p.getCantidad();
		}
		Order order = model.getCurrentOrder();
		boolean isPremium = order != null && order.isPremium();
		double iva = subtotal * IVA;

		double domi = COSTO_DOMI_STD;
		if (isPremium) {
			domi = 0.0;
		} else {
			String cuadrante = view.getSelectedCuadrante();
			if (cuadrante != null && !cuadrante.isBlank()) {
				double distKm = model.calcularDistanciaCuadrantes("UPB", cuadrante);
				if (distKm > 0) domi = 2_000.0 + (distKm * 800.0);
			}
		}

		double total = subtotal + iva + domi;
		view.setOrderTotal("$" + COP.format(Math.round(total)));
	}

	private void clearOrderTable() {
		currentOrderProducts.clear();
		SwingUtilities.invokeLater(() -> {
			view.getCurrentOrderModel().setRowCount(0);
			view.clearOrderSelection(); // FIX 3: resetear selección guardada
			view.setOrderTotal("$0");   // FIX 2: resetear total
		});
	}

	private void clearInvoiceView() {
		SwingUtilities.invokeLater(() -> {
			view.setInvoiceClient("—");
			view.setInvoiceTipo("—");
			view.setInvoiceDireccion("—");
			view.setInvoiceCuadrante("—");
			view.setInvoiceSubtotal("$0");
			view.setInvoiceIva("$0");
			view.setInvoiceDomicilio("$0");
			view.setInvoiceTotal("$0");
			view.getInvoiceOrderModel().setRowCount(0);
		});
	}

	private int findProductInCurrentOrder(String productoId) {
		for (int i = 0; i < currentOrderProducts.size(); i++) {
			if (productoId.equals(currentOrderProducts.get(i).getProductoId())) return i;
		}
		return -1;
	}

	private void syncProductToServer(Order order, Product product) {
		Product toRemove = cloneWithQuantity(product, product.getCantidad() - 1);
		model.quitarProductoAPedido(order.getOrderId(), toRemove);
		model.agregarProductoAPedido(order.getOrderId(), product);
	}

	private Product cloneWithQuantity(Product source, int cantidad) {
		Product clone = new Product(
				source.getProductoId(),
				source.getNombre(),
				source.getCategoria(),
				source.getPrecio(),
				source.isComplejo()
		);
		clone.setDescripcion(source.getDescripcion());
		clone.setDisponible(source.isDisponible());
		clone.setCantidad(cantidad);
		return clone;
	}

	private String shortId(String id) {
		if (id == null || id.isBlank()) return "—";
		return id.length() > 8 ? id.substring(0, 8) + "..." : id;
	}
}