package edu.fsadriann.server.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.order.EstadoPedido;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class DeliveryService implements DeliveryInterface {

    private final LinkedList<Delivery> entregas;
    private final CuadranteInterface cuadranteService;

    public DeliveryService(CuadranteInterface cuadranteService) {
        this.entregas          = new LinkedList<>();
        this.cuadranteService  = cuadranteService;
    }

    @Override
    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId, OrderService os) {
        if (orderId == null) return null;
        if (repartidorId == null || repartidorId.isBlank())
            throw new IllegalArgumentException("El repartidorId no puede ser nulo ni vacío.");
        if (yaEstaAsignado(orderId)) return null;

        try {
            Order pedido = os.buscarPedido(orderId);
            if (pedido == null) return null;
            if (pedido.getEstado() != EstadoPedido.LISTO) return null;
            if (pedido.getCuadranteDestino() == null) return null;

            // Usar Dijkstra del CuadranteService
            LinkedList<String> ruta = cuadranteService.calcularRutaMasCorta(
                    "UPB", pedido.getCuadranteDestino());

            // Si no hay ruta con Dijkstra, fallback a ruta simple
            if (ruta == null) {
                ruta = os.calcularRutaEntrega(orderId);
            }
            if (ruta == null) return null;

            Delivery delivery = new Delivery(orderId, repartidorId, ruta);
            entregas.add(delivery);
            return delivery;
        } catch (RemoteException e) {
            return null;
        }
    }

    @Override
    public boolean iniciarEntrega(String orderId, OrderService os) {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false;
        return os.marcarEnCamino(orderId);
    }

    @Override
    public boolean completarEntrega(String orderId, OrderService os) {
        if (orderId == null) return false;
        if (!yaEstaAsignado(orderId)) return false;
        return os.marcarEntregado(orderId);
    }


    @Override
    public Delivery buscarEntregaPorPedido(String orderId) {
        if (orderId == null) return null;
        Iterator<Delivery> it = entregas.iterator();
        while (it.hasNext()) {
            Delivery d = it.next();
            if (d != null && orderId.equals(d.getOrderId())) return d;
        }
        return null;
    }

    @Override
    public boolean yaEstaAsignado(String orderId) {
        return buscarEntregaPorPedido(orderId) != null;
    }
}
