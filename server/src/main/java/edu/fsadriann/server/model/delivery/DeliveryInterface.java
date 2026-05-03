package edu.fsadriann.server.model.delivery;

import edu.fsadriann.server.model.cuadrante.CuadranteInterface;
import edu.fsadriann.server.model.order.OrderService;

public interface DeliveryInterface {
    Delivery asignarPedidoARepartidor(String orderId, String repartidorId, CuadranteInterface cs, OrderService os);

    boolean iniciarEntrega(String orderId, OrderService os);
    boolean completarEntrega(String orderId, OrderService os);
    Delivery buscarEntregaPorPedido(String orderId);
    boolean yaEstaAsignado(String orderId);
}
