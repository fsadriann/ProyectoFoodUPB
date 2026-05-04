package edu.fsadriann.server.model.product;

import java.io.Serializable;

public class Product implements Serializable {

    private static final long serialVersionUID = 1L;
    private String productoId;
    private String nombre;
    private String descripcion;
    private String categoria;    // "PLATO_PRINCIPAL", "BEBIDA", "ENTRADA", "POSTRE"
    private int precio;
    private int cantidad;        // cantidad en este ítem del pedido
    private boolean isComplejo;  // afecta prioridad en cola de cocina
    private boolean disponible;

    public Product(String productoId, String nombre, String categoria, int precio, boolean isComplejo) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = 1;
        this.isComplejo = isComplejo;
        this.disponible = true;
    }

    // get
    public String getProductoId() {
        return productoId;
    }
    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getCategoria() {
        return categoria;
    }
    public int getPrecio() {
        return precio;
    }
    public int getCantidad() {
        return cantidad;
    }
    public boolean isComplejo() {
        return isComplejo;
    }
    public boolean isDisponible() {
        return disponible;
    }

    // set
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
    public void setPrecio(int precio) {
        this.precio = precio;
    }
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return nombre + " x" + cantidad + " = $" + (precio * cantidad)
                + (isComplejo ? " [complejo]" : "");
    }
}
