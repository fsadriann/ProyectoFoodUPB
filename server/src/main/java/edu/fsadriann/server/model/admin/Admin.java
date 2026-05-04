package edu.fsadriann.server.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;

public class Admin extends User {

    public Admin(String id, String nombres, String apellidos, Rol rol, String cedula, boolean isPremium, String telefono, String direccion, LinkedList<Product> favProductos) {
        super(id, nombres, apellidos, rol, cedula, isPremium, telefono, direccion, favProductos);
    }
}
