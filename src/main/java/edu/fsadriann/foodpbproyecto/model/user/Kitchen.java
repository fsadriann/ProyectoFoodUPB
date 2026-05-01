package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.product.Product;

public class Kitchen extends User {
    public Kitchen(String id, String nombres, String apellidos, Rol rol, String cedula, boolean isPremium, int telefono, String direccion, LinkedList<Product> favProductos) {
        super(id, nombres, apellidos, rol, cedula, isPremium, telefono, direccion, favProductos);
    }
}
