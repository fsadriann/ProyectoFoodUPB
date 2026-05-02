package edu.fsadriann.foodpbproyecto.model.delivery;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteInterface;
import edu.fsadriann.foodpbproyecto.model.order.EstadoPedido;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.foodpbproyecto.model.order.OrderService;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

public class DeliveryService implements DeliveryInterface {

    private final LinkedList<Delivery> entregas;

    public DeliveryService() {
        this.entregas = new LinkedList<>();
    }

    @Override
    public Delivery asignarPedidoARepartidor(String orderId, String repartidorId, CuadranteInterface cs, OrderService os) {
        // Validar entradas básicas
        if (orderId == null) return null;
        if (repartidorId == null || repartidorId.isBlank())
            throw new IllegalArgumentException("El repartidorId no puede ser nulo ni vacío.");


        if (yaEstaAsignado(orderId)) return null;
        try {
            Order pedido = os.buscarPedido(orderId);
            if (pedido == null) return null;
            if (pedido.getEstado() != EstadoPedido.LISTO) return null;
            if (pedido.getCuadranteDestino() == null) return null;

            var ruta = os.calcularRutaEntrega(orderId, cs);
            if (ruta == null) return null; // no hay ruta en el grafo

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
