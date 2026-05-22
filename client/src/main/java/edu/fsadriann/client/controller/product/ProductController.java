package edu.fsadriann.client.controller.product;

import edu.fsadriann.client.model.operator.OperatorModel;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.table.DefaultTableModel;

public class ProductController {

	private final OperatorModel  model;
	private final OperatorView view;

	private final LinkedList<Product> displayedProducts = new LinkedList<>();

	public ProductController(OperatorModel model, OperatorView view) {
		this.model = model;
		this.view  = view;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// CARGAR CATÁLOGO
	// ─────────────────────────────────────────────────────────────────────────

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

	public Product getDisplayedProduct(int row) {
		if (row < 0 || row >= displayedProducts.size()) return null;
		Object[] arr = displayedProducts.toArray();
		return (Product) arr[row];
	}

	// ─────────────────────────────────────────────────────────────────────────
	// HELPER PRIVADO
	// ─────────────────────────────────────────────────────────────────────────

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