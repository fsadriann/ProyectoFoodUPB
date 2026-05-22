package edu.fsadriann.server.model.product;

import java.io.Serializable;

/**
 * Representa un producto del menú de Food UPB.
 * Puede pertenecer a las categorías: PLATO_PRINCIPAL, BEBIDA, ENTRADA o POSTRE.
 */
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String  productoId;
    private String  nombre;
    private String  descripcion;
    private String  categoria;
    private int     precio;
    private int     cantidad;
    private boolean isComplejo;
    private boolean disponible;

    /**
     * Crea un producto con su información básica.
     *
     * @param productoId identificador único del producto
     * @param nombre     nombre del producto
     * @param categoria  categoría del producto (ej. BEBIDA, ENTRADA)
     * @param precio     precio en pesos colombianos
     * @param isComplejo {@code true} si requiere el fogón grande en cocina
     */
    public Product(String productoId, String nombre, String categoria,
                   int precio, boolean isComplejo) {
        this.productoId = productoId;
        this.nombre     = nombre;
        this.categoria  = categoria;
        this.precio     = precio;
        this.cantidad   = 1;
        this.isComplejo = isComplejo;
        this.disponible = true;
    }

    /** @return identificador único del producto */
    public String getProductoId() { return productoId; }

    /** @return nombre del producto */
    public String getNombre()     { return nombre; }

    /** @return descripción del producto */
    public String getDescripcion() { return descripcion; }

    /** @return categoría del producto */
    public String getCategoria()  { return categoria; }

    /** @return precio en pesos colombianos */
    public int getPrecio()        { return precio; }

    /** @return cantidad de unidades de este ítem en el pedido */
    public int getCantidad()      { return cantidad; }

    /** @return {@code true} si el producto requiere preparación compleja */
    public boolean isComplejo()   { return isComplejo; }

    /** @return {@code true} si el producto está disponible en el menú */
    public boolean isDisponible() { return disponible; }

    /** @param cantidad nueva cantidad de unidades */
    public void setCantidad(int cantidad)       { this.cantidad    = cantidad; }

    /** @param precio nuevo precio en pesos colombianos */
    public void setPrecio(int precio)           { this.precio      = precio; }

    /** @param disponible {@code true} para activar, {@code false} para desactivar */
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    /** @param descripcion nueva descripción del producto */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return nombre + " x" + cantidad + " = $" + (precio * cantidad)
                + (isComplejo ? " [complejo]" : "");
    }
}
