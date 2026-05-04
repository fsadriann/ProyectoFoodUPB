package edu.fsadriann.client.controller.order;

import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Controlador de pedidos para el flujo del operador.
 *
 * Responsabilidades:
 *  - Crear el pedido al identificar un cliente
 *  - Agregar / quitar / cambiar cantidad de productos en el pedido actual
 *  - Generar la factura (calcular totales y poblar el card de factura)
 *  - Enviar el pedido a cocina
 *  - Limpiar el pedido o la factura
 *
 * Nunca habla directamente con OrderService ni ProductService:
 * toda comunicación pasa por ClientModel (RMI encapsulado).
 */
public class OrderController {

	private static final NumberFormat COP = NumberFormat.getNumberInstance(new Locale("es", "CO"));

	private final ClientModel   model;
	private final OperatorView  view;

	// Cache local de productos mostrados en currentOrderTable (para quitar/editar)
	// El índice coincide con la fila de currentOrderModel.
	private java.util.List<Product> currentOrderProducts = new java.util.ArrayList<>();

	public OrderController(ClientModel model, OperatorView view) {
		this.model = model;
		this.view  = view;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// CREAR PEDIDO
	// ─────────────────────────────────────────────────────────────────────────

	public void createOrderForClient(User client) {
		if (client == null) {
			view.showError("Cliente inválido para crear pedido.");
			return;
		}

		// Si ya hay un pedido pendiente, cancelarlo primero
		Order existing = model.getCurrentOrder();
		if (existing != null && existing.getEstado() == EstadoPedido.PENDIENTE) {
			model.cancelarPedido(existing.getOrderId());
		}

		boolean isPremium = client.isPremium();  // Consulta el flag del usuario
		Order order = model.crearPedido(client.getCedula(), isPremium);

		if (order == null) {
			view.showError("No se pudo crear el pedido. Verifica la conexión con el servidor.");
			return;
		}

		// Limpiar tablas de la vista para el nuevo pedido
		clearOrderTable();
		clearInvoiceView();

		view.setMessage("Pedido " + shortId(order.getOrderId()) + " creado para " + client.getNombreCompleto().trim());
		view.showTab(1); // Avanzar al paso de productos
	}

	// ─────────────────────────────────────────────────────────────────────────
	// AGREGAR PRODUCTO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Agrega un producto al pedido actual y actualiza la tabla currentOrder.
	 * Si el producto ya está en el carrito, incrementa su cantidad.
	 */
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

		// Buscar si ya existe en el carrito local para incrementar cantidad
		int existingIndex = findProductInCurrentOrder(product.getProductoId());

		if (existingIndex >= 0) {
			// Ya existe: incrementar cantidad en el modelo de vista
			Product existing = currentOrderProducts.get(existingIndex);
			existing.setCantidad(existing.getCantidad() + 1);
			// Actualizar la fila en la tabla
			DefaultTableModel tableModel = view.getCurrentOrderModel();
			tableModel.setValueAt(existing.getCantidad(), existingIndex, 1);
			// Persistir el cambio en servidor (quitamos y volvemos a agregar con nueva cantidad)
			syncProductToServer(order, existing);
		} else {
			// Producto nuevo: clonar con cantidad 1 para no mutar el catálogo
			Product toAdd = cloneWithQuantity(product, 1);
			boolean added = model.agregarProductoAPedido(order.getOrderId(), toAdd);
			if (!added) {
				view.showError("No se pudo agregar el producto al pedido.");
				return;
			}
			currentOrderProducts.add(toAdd);
			view.getCurrentOrderModel().addRow(new Object[]{
					toAdd.getNombre(),
					toAdd.getCantidad()
			});
		}

		view.setMessage("«" + product.getNombre() + "» agregado al pedido.");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// QUITAR PRODUCTO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Quita el producto seleccionado en currentOrderTable del pedido actual.
	 */
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
			view.setMessage("«" + toRemove.getNombre() + "» eliminado del pedido.");
		} else {
			view.showError("No se pudo eliminar el producto. Verifica el estado del pedido.");
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// CAMBIAR CANTIDAD
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Abre un diálogo para cambiar la cantidad del producto seleccionado.
	 */
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

		if (input == null || input.isBlank()) return; // Cancelado

		int cantidad;
		try {
			cantidad = Integer.parseInt(input.trim());
		} catch (NumberFormatException e) {
			view.showError("Ingresa un número válido.");
			return;
		}

		if (cantidad <= 0) {
			// Si pone 0 o negativo, preguntar si quiere eliminar
			if (view.confirm("La cantidad es 0. ¿Deseas eliminar el producto del pedido?")) {
				removeSelectedProduct();
			}
			return;
		}

		// Quitar el producto y volver a agregar con nueva cantidad
		boolean removed = model.quitarProductoAPedido(order.getOrderId(), product);
		if (!removed) {
			view.showError("No se pudo actualizar la cantidad.");
			return;
		}

		product.setCantidad(cantidad);
		boolean added = model.agregarProductoAPedido(order.getOrderId(), product);
		if (!added) {
			view.showError("No se pudo re-agregar el producto con la nueva cantidad.");
			return;
		}

		// Actualizar tabla
		view.getCurrentOrderModel().setValueAt(cantidad, row, 1);
		view.setMessage("Cantidad actualizada a " + cantidad + " para «" + product.getNombre() + "».");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// GENERAR FACTURA
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Calcula la factura del pedido actual y puebla el card de factura en la vista.
	 */
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

		double total = model.calcularFactura(order);

		// Refrescar la referencia local del pedido (calcularFactura actualiza sus campos)
		Order updated = model.getCurrentOrder();
		if (updated == null) updated = order;

		// Poblar datos del cliente
		User client = model.getCurrentClient();
		if (client != null) {
			view.setInvoiceClient(client.getNombreCompleto().trim());
			view.setInvoiceTipo(updated.isPremium() ? "Premium" : "Estándar");
			view.setInvoiceDireccion(
					client.getDireccion() != null ? client.getDireccion() : "—"
			);
		}

		// Cuadrante (puede estar vacío si aún no se asignó)
		view.setInvoiceCuadrante(
				updated.getCuadranteDestino() != null ? updated.getCuadranteDestino() : "—"
		);

		// Totales
		view.setInvoiceSubtotal("$" + COP.format(Math.round(updated.getSubtotal())));
		view.setInvoiceIva("$"       + COP.format(Math.round(updated.getImpuesto())));
		view.setInvoiceDomicilio("$" + COP.format(Math.round(updated.getCostoDomi())));
		view.setInvoiceTotal("$"     + COP.format(Math.round(total)));

		// Poblar tabla de ítems en la factura
		DefaultTableModel invoiceModel = view.getInvoiceOrderModel();
		invoiceModel.setRowCount(0);
		for (Product p : currentOrderProducts) {
			invoiceModel.addRow(new Object[]{ p.getNombre(), p.getCantidad() });
		}

		view.showTab(2); // Avanzar al paso de factura
		view.setMessage("Factura generada. Total: $" + COP.format(Math.round(total)));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// ENVIAR A COCINA
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Valida el pedido y lo envía a cocina cambiando su estado a EN_PREPARACION.
	 */
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
			view.showTab(3); // Paso: Confirmar
			view.setMessage("Pedido " + shortId(order.getOrderId()) + " en preparación.");
			// Limpiar estado local para el siguiente pedido
			clearOrderTable();
			clearInvoiceView();
			model.clearCurrentOrder();
		} else {
			view.showError(
					"No se pudo enviar el pedido a cocina.\n" +
							"Verifica que el pedido esté en estado PENDIENTE y tenga productos."
			);
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// LIMPIAR PEDIDO / FACTURA
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Limpia todos los productos del pedido actual (cancela y recrea).
	 */
	public void clearOrder() {
		Order order = model.getCurrentOrder();
		if (order == null) {
			clearOrderTable();
			return;
		}
		if (!view.confirm("¿Limpiar todos los productos del pedido actual?")) {
			return;
		}

		// Cancelar el pedido actual y crear uno nuevo para el mismo cliente
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
			// Si no se pudo cancelar (ya fue a cocina), solo limpiar vista
			clearOrderTable();
			view.setMessage("Vista del pedido limpiada.");
		}
	}

	/**
	 * Limpia únicamente el panel de factura en la vista (no afecta el pedido).
	 */
	public void clearInvoiceOrder() {
		clearInvoiceView();
		view.setMessage("Factura limpiada.");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// HELPERS PRIVADOS
	// ─────────────────────────────────────────────────────────────────────────

	/** Limpia la tabla de pedido actual y el cache local. */
	private void clearOrderTable() {
		currentOrderProducts.clear();
		SwingUtilities.invokeLater(() -> view.getCurrentOrderModel().setRowCount(0));
	}

	/** Limpia todos los campos del card de factura. */
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

	/**
	 * Busca un producto en el cache local por ID.
	 * @return índice en currentOrderProducts, o -1 si no existe.
	 */
	private int findProductInCurrentOrder(String productoId) {
		for (int i = 0; i < currentOrderProducts.size(); i++) {
			if (productoId.equals(currentOrderProducts.get(i).getProductoId())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Re-sincroniza un producto existente al servidor:
	 * lo quita y lo vuelve a agregar con la cantidad actualizada.
	 */
	private void syncProductToServer(Order order, Product product) {
		// Quitar la versión anterior (cantidad previa)
		Product toRemove = cloneWithQuantity(product, product.getCantidad() - 1);
		model.quitarProductoAPedido(order.getOrderId(), toRemove);
		// Agregar con cantidad nueva
		model.agregarProductoAPedido(order.getOrderId(), product);
	}

	/**
	 * Clona un producto con una cantidad específica para no mutar el catálogo original.
	 */
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