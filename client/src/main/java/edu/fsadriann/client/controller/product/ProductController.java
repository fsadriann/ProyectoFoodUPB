package edu.fsadriann.client.controller.product;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.client.model.ClientModel;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.view.operator.OperatorView;

import javax.swing.table.DefaultTableModel;

public class ProductController {

	private final ClientModel model;
	private final OperatorView view;
	private LinkedList<Product> displayedProducts;

	public ProductController(ClientModel model, OperatorView view) {
		this.model = model;
		this.view = view;
		this.displayedProducts = new LinkedList<>();
	}

	public void loadCatalog() {
		renderProducts(model.listarProductos(), "Catálogo cargado.");
	}

	public void searchProduct(String query) {
		String normalizedQuery = query == null ? "" : query.trim();
		if (normalizedQuery.isEmpty()) {
			loadCatalog();
			return;
		}

		LinkedList<Product> results = model.buscarProducto(normalizedQuery);
		renderProducts(results, results.isEmpty()
				? "No se encontraron productos para: " + normalizedQuery
				: "Resultados para: " + normalizedQuery);
	}

	public Product getDisplayedProduct(int row) {
		if (row < 0 || displayedProducts == null || row >= displayedProducts.size()) {
			return null;
		}
		int index = 0;
		Iterator<Product> iterator = displayedProducts.iterator();
		while (iterator.hasNext()) {
			Product product = iterator.next();
			if (index == row) {
				return product;
			}
			index++;
		}
		return null;
	}

	public ClientModel getModel() {
		return model;
	}

	private void renderProducts(LinkedList<Product> products, String message) {
		DefaultTableModel tableModel = view.getProductsTableModel();
		tableModel.setRowCount(0);
		displayedProducts = new LinkedList<>();

		if (products != null) {
			Iterator<Product> iterator = products.iterator();
			while (iterator.hasNext()) {
				Product product = iterator.next();
				if (product == null) {
					continue;
				}
				displayedProducts.add(product);
				tableModel.addRow(new Object[]{
					product.getNombre(),
					formatMoney(product.getPrecio()),
					product.isDisponible() ? "Disponible" : "No disponible"
				});
			}
		}

		view.setMessage(message);
	}

	private String formatMoney(double value) {
		return "$" + Math.round(value);
	}
}
