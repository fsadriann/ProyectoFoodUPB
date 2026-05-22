package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;

/**
 * Representa al personal de cocina en el sistema Food UPB.
 * Hereda los atributos de {@link User} y opera bajo el rol {@link Rol#COCINA}.
 */
public class Kitchen extends User {

    /**
     * Crea un usuario de cocina con los datos completos.
     */
    public Kitchen(String id, String nombres, String apellidos, Rol rol, String cedula,
                   boolean isPremium, String telefono, String direccion,
                   LinkedList<Product> favProductos) {
        super(id, nombres, apellidos, rol, cedula, isPremium, telefono, direccion, favProductos);
    }
}
