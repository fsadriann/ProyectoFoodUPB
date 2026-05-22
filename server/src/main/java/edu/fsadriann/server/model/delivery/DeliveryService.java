package edu.fsadriann.server.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;

import java.rmi.RemoteException;

/**
 * Servicio que gestiona las entregas a domicilio.
 * Coordina con {@link CuadranteInterface} para calcular la ruta y con
 * {@link OrderService} para actualizar el estado del pedido.
 */
public class DeliveryService implements DeliveryInterface {

    private final LinkedList<Delivery> entregas;
    private final CuadranteInterface   cuadranteService;
    private final OrderService         orderService;

    /**
     * Crea el servicio de entregas con las dependencias necesarias.
     *
     * @param cuadranteService servicio de cuadrantes para calcular rutas
     * @param orderService     servicio de pedidos para actualizar estados
     */
    public DeliveryService(CuadranteInterface cuadranteService, OrderService orderService) {
        this.entregas         = new LinkedList<>();
        this.cuadranteService = cuadranteService;
        this.orderService     = orderService;
    }

    /**
     * Asigna un pedido listo a un repartidor y calcula la ruta de entrega.
     * El pedido debe estar en estado {@link EstadoPedido#LISTO} y tener cuadrante asignado.
     *
     * @param orderId      identificador del pedido
     * @param repartidorId identificador del repartidor
     * @return el registro de entrega creado, o {@code null} si no se pudo asignar
     */
    @Override
    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId)
            throws RemoteException {
        if (orderId == null) return null;
        if (repartidorId == null || repartidorId.isBlank())
            throw new IllegalArgumentException("El repartidorId no puede ser nulo ni vacío.");
        if (yaEstaAsignado(orderId)) return null;

        Order pedido = orderService.buscarPedido(orderId);
        if (pedido == null) return null;
        if (pedido.getEstado() != EstadoPedido.LISTO) return null;
        if (pedido.getCuadranteDestino() == null) return null;

        LinkedList<String> ruta = cuadranteService.calcularRutaMasCorta(
                "UPB", pedido.getCuadranteDestino());
        if (ruta == null) ruta = orderService.calcularRutaEntrega(orderId);
        if (ruta == null) return null;

        Delivery delivery = new Delivery(orderId, repartidorId, ruta);
        entregas.add(delivery);
        return delivery;
    }

    /**
     * Inicia el trayecto de un pedido asignado, cambiando su estado a EN_CAMINO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el inicio fue exitoso
     */
    @Override
    public boolean iniciarEntrega(String orderId) throws RemoteException {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false;
        Order pedido = orderService.buscarPedido(orderId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.LISTO) return false;
        return orderService.marcarEnCamino(orderId);
    }

    /**
     * Registra la entrega como completada, cambiando el estado del pedido a ENTREGADO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si la entrega se completó exitosamente
     */
    @Override
    public boolean completarEntrega(String orderId) throws RemoteException {
        if (orderId == null) return false;
        Order pedido = orderService.buscarPedido(orderId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.EN_CAMINO) return false;
        if (!yaEstaAsignado(orderId)) {
            entregas.add(new Delivery(orderId, "Sin asignar", new LinkedList<>()));
        }
        return orderService.marcarEntregado(orderId);
    }

    /**
     * Busca el registro de entrega asociado a un pedido.
     *
     * @param orderId identificador del pedido
     * @return el registro de entrega, o {@code null} si no está asignado
     */
    @Override
    public Delivery buscarEntregaPorPedido(String orderId) throws RemoteException {
        if (orderId == null) return null;
        Iterator<Delivery> it = entregas.iterator();
        while (it.hasNext()) {
            Delivery d = it.next();
            if (d != null && orderId.equals(d.getOrderId())) return d;
        }
        return null;
    }

    /**
     * Indica si un pedido ya tiene un repartidor asignado.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si ya está asignado
     */
    @Override
    public boolean yaEstaAsignado(String orderId) throws RemoteException {
        return buscarEntregaPorPedido(orderId) != null;
    }
}
