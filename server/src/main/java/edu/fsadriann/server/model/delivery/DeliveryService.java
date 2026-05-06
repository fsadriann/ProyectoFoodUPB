package edu.fsadriann.server.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;

import java.rmi.RemoteException;

public class DeliveryService implements DeliveryInterface {

    private final LinkedList<Delivery> entregas;
    private final CuadranteInterface   cuadranteService;
    private final OrderService         orderService;

    public DeliveryService(CuadranteInterface cuadranteService, OrderService orderService) {
        this.entregas         = new LinkedList<>();
        this.cuadranteService = cuadranteService;
        this.orderService     = orderService;
    }

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

    @Override
    public boolean iniciarEntrega(String orderId) throws RemoteException {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false;
        Order pedido = orderService.buscarPedido(orderId);
        if (pedido == null) return false;
        if (pedido.getEstado() != EstadoPedido.LISTO) return false;
        return orderService.marcarEnCamino(orderId);
    }

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

    @Override
    public boolean yaEstaAsignado(String orderId) throws RemoteException {
        return buscarEntregaPorPedido(orderId) != null;
    }
}