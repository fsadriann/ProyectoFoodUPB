package edu.fsadriann.client.controller.operator;

import edu.fsadriann.client.controller.order.OrderController;
import edu.fsadriann.client.controller.product.ProductController;
import edu.fsadriann.client.model.operator.OperatorModel;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.view.operator.OperatorView;
import edu.fsadriann.view.operator.RegisterClientDialog;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableModel;

public class OperatorController {

	private final OperatorModel    model;
	private final OperatorView     view;
	private final ProductController productController;
	private final OrderController  orderController;
	private Runnable logoutAction;
	private boolean  initialized;

	public OperatorController(OperatorModel model, OperatorView view) {
		this(model, view, new ProductController(model, view), new OrderController(model, view));
	}

	public OperatorController(OperatorModel model, OperatorView view,
							  ProductController productController, OrderController orderController) {
		this.model             = model;
		this.view              = view;
		this.productController = productController;
		this.orderController   = orderController;
	}

	public void init() {
		if (!initialized) {
			view.initComponents(text -> {
				productController.searchProduct(text);
				return null;
			});

			view.addSearchClientListener(this::handleSearchClient);
			view.addLoadProductsListener(this::obtenerProductos);
			view.addGenerateInvoiceListener(() -> orderController.generateInvoice());
			view.addRemoveProductListener(() -> orderController.removeSelectedProduct());
			view.addChangeQuantityListener(() -> orderController.changeSelectedProductQuantity());
			view.addClearOrderListener(() -> orderController.clearOrder());
			view.addClearInvoiceOrderListener(() -> orderController.clearInvoiceOrder());
			view.addSendToKitchenListener(() -> orderController.sendToKitchen());
			view.addLogoutListener(this::handleLogout);

			// Al cambiar cuadrante manualmente, refrescar total estimado
			view.addCuadranteSelectorListener(() -> orderController.refreshOrderTotal());

			view.getProductsTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						int row = view.getSelectedProductRow();
						Product selected = productController.getDisplayedProduct(row);
						orderController.addProduct(selected);
					}
				}
			});
			initialized = true;
		}

		view.setRegisterEnabled(model.isConnected());
		obtenerProductos();
		view.setMessage(model.isConnected()
				? "Conectado al servidor."
				: "No hay conexión con el servidor.");
	}

	public void obtenerProductos() {
		productController.loadCatalog();
	}

	public void show() { view.setVisible(true);  }
	public void hide() { view.setVisible(false); }

	public void setLogoutAction(Runnable logoutAction) {
		this.logoutAction = logoutAction;
	}

	public OperatorModel getModel() { return model; }
	public OperatorView getView() { return view;  }

	// ─────────────────────────────────────────────────────────────────────────
	// SEARCH CLIENT
	// ─────────────────────────────────────────────────────────────────────────

	private void handleSearchClient() {
		String phone = view.getSearchPhone();
		if (phone == null || phone.isBlank()) {
			view.showError("Ingresa un número telefónico para buscar.");
			return;
		}

		User client = model.buscarClientePorTelefono(phone);

		if (client == null) {
			client = handleRegisterClient(phone);
			if (client == null) return;
		}

		model.setCurrentClient(client);
		orderController.createOrderForClient(client);
		loadFrequentOrders(client);

		// Cargar cuadrantes en el selector
		LinkedList<edu.fsadriann.server.model.cuadrante.Cuadrante> cuads = model.listarCuadrantes();
		LinkedList<String> nombres = new LinkedList<>();
		Iterator<edu.fsadriann.server.model.cuadrante.Cuadrante> itC = cuads.iterator();
		while (itC.hasNext()) {
			edu.fsadriann.server.model.cuadrante.Cuadrante c = itC.next();
			if (c != null && !"UPB".equals(c.getNombre())) nombres.add(c.getNombre());
		}
		Object[] raw = nombres.toArray();
		String[] nombresArr = new String[raw.length];
		for (int i = 0; i < raw.length; i++) nombresArr[i] = (String) raw[i];
		view.setCuadrantesDisponibles(nombresArr);

		// Detectar cuadrante por dirección
		String direccion          = client.getDireccion();
		String cuadranteDetectado = detectarCuadrante(direccion);
		if (cuadranteDetectado != null) {
			view.setSelectedCuadrante(cuadranteDetectado);
		}

		// FIX: 5 columnas — Nombres | Correo | Teléfono | Tipo | Cuadrante
		// El cuadrante se pasa directamente para que la tarjeta del cliente lo muestre
		DefaultTableModel clientTable = view.getClientTableModel();
		clientTable.setRowCount(0);
		clientTable.addRow(new Object[]{
				client.getNombreCompleto().trim(),
				client.getId(),
				client.getTelefono(),
				client.isPremium() ? "Premium" : "Estándar",
				// FIX: si se detectó un cuadrante se muestra, si no se deja vacío
				// para que el operador lo seleccione manualmente en el combo
				cuadranteDetectado != null ? cuadranteDetectado : ""
		});

		view.showTab(1);

		// FIX: un solo mensaje de estado, unificado
		String msgCuadrante = cuadranteDetectado != null
				? "Cuadrante detectado: " + cuadranteDetectado + "."
				: "Selecciona el cuadrante manualmente.";
		view.setMessage("Cliente cargado y pedido iniciado. " + msgCuadrante);
	}

	private String detectarCuadrante(String direccion) {
		if (direccion == null || direccion.isBlank()) return null;
		String d = direccion.toLowerCase();

		if (d.contains("floridablanca"))                    return "Floridablanca";
		if (d.contains("girón") || d.contains("giron"))    return "Giron";
		if (d.contains("piedecuesta"))                      return "Piedecuesta";
		if (d.contains("lagos") || d.contains("cacique"))  return "Lagos";
		if (d.contains("cabecera"))                         return "Cabecera";
		if (d.contains("provenza"))                         return "Provenza";
		if (d.contains("san francisco") || d.contains("sanfrancisco")) return "SanFrancisco";
		if (d.contains("mejoras"))                          return "Mejoras";
		if (d.contains("centro") || d.contains("bucaramanga")) return "Centro";

		return null;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// REGISTER CLIENT
	// ─────────────────────────────────────────────────────────────────────────

	private User handleRegisterClient(String phone) {
		RegisterClientDialog dialog = new RegisterClientDialog(view, phone);
		dialog.setVisible(true);

		User newUser = dialog.getResult();
		if (newUser == null) return null;

		User registered = model.registrarCliente(newUser);
		if (registered == null) {
			view.showError("No se pudo registrar el cliente.\n" + model.getLogger());
			return null;
		}
		return registered;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// FREQUENT ORDERS
	// ─────────────────────────────────────────────────────────────────────────

	private void loadFrequentOrders(User client) {
		DefaultTableModel modelTable = view.getRecentOrdersModel();
		modelTable.setRowCount(0);
		LinkedList<Order> pedidos = model.getPedidosFrecuentes(client.getCedula());
		Iterator<Order> iterator = pedidos.iterator();
		while (iterator.hasNext()) {
			Order pedido = iterator.next();
			if (pedido == null) continue;
			modelTable.addRow(new Object[]{
					shortId(pedido.getOrderId()),
					pedido.getEstado() != null ? pedido.getEstado().name() : "—",
					pedido.getCantProductos(),
					"$" + Math.round(pedido.getTotal())
			});
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	// LOGOUT
	// ─────────────────────────────────────────────────────────────────────────

	private void handleLogout() {
		model.logout();
		hide();
		if (logoutAction != null) {
			logoutAction.run();
		}
	}

	private String shortId(String orderId) {
		if (orderId == null || orderId.isBlank()) return "—";
		return orderId.length() > 8 ? orderId.substring(0, 8) + "..." : orderId;
	}
}