package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa un pedido de domicilio en el sistema Food UPB.
 * Su ciclo de vida está controlado por {@link EstadoPedido}.
 * Los precios se manejan en pesos colombianos (COP) como {@code double}.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificador único del pedido (UUID). */
    private final String orderId;

    /** Cédula del cliente que realiza el pedido. */
    private String cedulaCliente;

    /** Estado actual del pedido en su ciclo de vida. */
    private EstadoPedido estado;

    /** Indica si el cliente es premium (afecta el costo de domicilio). */
    private boolean isPremium;

    /** Productos en el carrito del pedido. */
    private LinkedList<Product> carrito;

    /** Suma de (precio × cantidad) de cada producto, sin impuestos. */
    private double subtotal;

    /** IVA calculado sobre el subtotal (19% por defecto). */
    private double impuesto;

    /** Costo de domicilio (0 si es premium, calculado por distancia si no). */
    private double costoDomi;

    /** Total final: subtotal + impuesto + costoDomi. */
    private double total;

    /** Cantidad de líneas de producto en el carrito. */
    private int cantProductos;

    /** Marca de tiempo de creación del pedido (epoch ms). */
    private final long timestamp;

    /**
     * Cuadrante de destino para la entrega.
     * Es {@code null} hasta que {@code OrderService.asignarCuadranteDestino} lo establezca.
     */
    private String cuadranteDestino;

    /**
     * Crea un pedido nuevo en estado {@link EstadoPedido#PENDIENTE}.
     *
     * @param cedulaCliente cédula del cliente que realiza el pedido
     * @param isPremium     {@code true} si el cliente tiene membresía premium
     */
    public Order(String cedulaCliente, boolean isPremium) {
        this.orderId       = UUID.randomUUID().toString();
        this.cedulaCliente = cedulaCliente;
        this.isPremium     = isPremium;
        this.estado        = EstadoPedido.PENDIENTE;
        this.carrito       = new LinkedList<>();
        this.subtotal      = 0.0;
        this.impuesto      = 0.0;
        this.costoDomi     = 0.0;
        this.total         = 0.0;
        this.cantProductos = 0;
        this.timestamp     = System.currentTimeMillis();
    }

    /**
     * Crea un pedido no-premium. Constructor de compatibilidad para código existente.
     *
     * @param cedulaCliente cédula del cliente
     */
    public Order(String cedulaCliente) {
        this(cedulaCliente, false);
    }

    /** @return identificador único del pedido */
    public String getOrderId()              { return orderId; }

    /** @return carrito con los productos del pedido */
    public LinkedList<Product> getCarrito() { return carrito; }

    /** @return cédula del cliente propietario del pedido */
    public String getCedulaCliente()        { return cedulaCliente; }

    /** @return estado actual en el ciclo de vida del pedido */
    public EstadoPedido getEstado()         { return estado; }

    /** @return {@code true} si el cliente es premium */
    public boolean isPremium()              { return isPremium; }

    /** @return subtotal sin impuestos ni domicilio */
    public double getSubtotal()             { return subtotal; }

    /** @return total final del pedido (subtotal + IVA + domicilio) */
    public double getTotal()                { return total; }

    /** @return cantidad de líneas de producto en el carrito */
    public int getCantProductos()           { return cantProductos; }

    /** @return IVA aplicado al pedido */
    public double getImpuesto()             { return impuesto; }

    /** @return costo de domicilio del pedido */
    public double getCostoDomi()            { return costoDomi; }

    /** @return marca de tiempo de creación (epoch ms) */
    public long getTimestamp()              { return timestamp; }

    /**
     * @return nombre del cuadrante de destino asignado para la entrega,
     *         o {@code null} si aún no ha sido asignado
     */
    public String getCuadranteDestino()     { return cuadranteDestino; }

    /**
     * Actualiza el estado del pedido.
     * La validación de transiciones válidas es responsabilidad del Service.
     *
     * @param estado nuevo estado del pedido
     */
    public void setEstado(EstadoPedido estado)      { this.estado     = estado; }

    /** @param subtotal suma de precios × cantidades sin impuestos */
    public void setSubtotal(double subtotal)        { this.subtotal   = subtotal; }

    /** @param impuesto IVA calculado */
    public void setImpuesto(double impuesto)        { this.impuesto   = impuesto; }

    /** @param costoDomi costo de domicilio */
    public void setCostoDomi(double costoDomi)      { this.costoDomi  = costoDomi; }

    /** @param total total final del pedido */
    public void setTotal(double total)              { this.total      = total; }

    /** @param cantProductos cantidad de líneas en el carrito */
    public void setCantProductos(int cantProductos) { this.cantProductos = cantProductos; }

    /**
     * Establece el cuadrante de destino del pedido. Solo debe invocarlo {@code OrderService}.
     *
     * @param cuadranteDestino nombre del cuadrante de destino
     */
    public void setCuadranteDestino(String cuadranteDestino) {
        this.cuadranteDestino = cuadranteDestino;
    }

    /**
     * Agrega un producto al carrito e incrementa el contador de líneas.
     *
     * @param product producto a agregar
     */
    public void agregarProducto(Product product) {
        carrito.add(product);
        cantProductos++;
    }

    /**
     * Elimina un producto del carrito por referencia de objeto.
     *
     * @param product producto a eliminar
     * @return {@code true} si fue encontrado y eliminado
     */
    public boolean quitarProducto(Product product) {
        boolean removed = carrito.remove(product);
        if (removed && cantProductos > 0) cantProductos--;
        return removed;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='"       + orderId        + '\'' +
                ", cliente='" + cedulaCliente  + '\'' +
                ", estado="  + estado                +
                ", destino='" + (cuadranteDestino != null ? cuadranteDestino : "sin asignar") + '\'' +
                ", total=$"  + total                 +
                ", items="   + cantProductos         +
                '}';
    }
}
