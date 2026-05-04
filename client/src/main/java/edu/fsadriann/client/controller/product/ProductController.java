package edu.fsadriann.client.controller.product;

import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del catálogo de productos para el flujo del operador.
 *
 * Responsabilidades:
 *  - Cargar el catálogo completo desde el servidor
 *  - Buscar productos por nombre / ID
 *  - Exponer el producto en la fila seleccionada de la tabla (para que
 *    OrderController pueda agregarlo al pedido con doble clic)
 *
 * Toda comunicación con ProductService pasa por ClientModel (RMI encapsulado).
 */
public class ProductController {

	private final ClientModel  model;
	private final OperatorView view;

	// Cache local de los productos mostrados en productsTable.
	// El índice coincide con la fila del DefaultTableModel.
	private final List<Product> displayedProducts = new ArrayList<>();

	public ProductController(ClientModel model, OperatorView view) {
		this.model = model;
		this.view  = view;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// CARGAR CATÁLOGO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Carga todos los productos del servidor y los muestra en la tabla de menú.
	 * Llamado al iniciar la vista y con el botón "Cargar productos".
	 */
	public void loadCatalog() {
		if (!model.isConnected()) {
			view.setMessage("Sin conexión al servidor. No se puede cargar el catálogo.");
			return;
		}

		LinkedList<Product> products = model.listarProductos();
		populateTable(products);
		view.setMessage("Catálogo cargado: " + displayedProducts.size() + " producto(s).");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// BUSCAR PRODUCTO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Busca productos por nombre o ID y actualiza la tabla de menú con los resultados.
	 * Si la búsqueda está vacía, recarga el catálogo completo.
	 *
	 * @param query texto a buscar (nombre parcial o ID exacto)
	 */
	public void searchProduct(String query) {
		if (!model.isConnected()) {
			view.setMessage("Sin conexión al servidor.");
			return;
		}

		if (query == null || query.isBlank()) {
			loadCatalog();
			return;
		}

		LinkedList<Product> results = model.buscarProducto(query.trim());
		populateTable(results);

		int count = displayedProducts.size();
		view.setMessage(count > 0
				? count + " resultado(s) para «" + query.trim() + "»."
				: "Sin resultados para «" + query.trim() + "».");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// ACCESO AL PRODUCTO SELECCIONADO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Devuelve el producto correspondiente a la fila indicada de la tabla,
	 * o {@code null} si el índice es inválido.
	 *
	 * Usado por OperatorController en el listener de doble clic sobre la tabla.
	 *
	 * @param row índice de fila (base 0)
	 */
	public Product getDisplayedProduct(int row) {
		if (row < 0 || row >= displayedProducts.size()) {
			return null;
		}
		return displayedProducts.get(row);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// HELPER PRIVADO
	// ─────────────────────────────────────────────────────────────────────────

	/**
	 * Vuelca una LinkedList de productos en el DefaultTableModel de la vista
	 * y actualiza el cache local {@code displayedProducts}.
	 */
	private void populateTable(LinkedList<Product> products) {
		displayedProducts.clear();

		DefaultTableModel tableModel = view.getProductsTableModel();
		tableModel.setRowCount(0); // Limpiar filas previas

		if (products == null) return;

		Iterator<Product> it = products.iterator();
		while (it.hasNext()) {
			Product p = it.next();
			if (p == null) continue;

			displayedProducts.add(p);
			tableModel.addRow(new Object[]{
					p.getProductoId(),
					p.getNombre(),
					p.getCategoria(),
					"$" + p.getPrecio(),
					p.isDisponible() ? "Sí" : "No"
			});
		}
	}
}