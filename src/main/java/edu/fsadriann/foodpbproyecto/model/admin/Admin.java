package edu.fsadriann.foodpbproyecto.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.product.Product;
import edu.fsadriann.foodpbproyecto.model.user.Rol;
import edu.fsadriann.foodpbproyecto.model.user.User;

public class Admin extends User {

    public Admin(String id, String nombres, String apellidos, Rol rol, String cedula, boolean isPremium, int telefono, String direccion, LinkedList<Product> favProductos) {
        super(id, nombres, apellidos, rol, cedula, isPremium, telefono, direccion, favProductos);
    }
}
