package edu.fsadriann.foodpbproyecto.model.order;

import edu.fsadriann.foodpbproyecto.model.cuadrante.CuadranteInterface;

import java.rmi.RemoteException;

/**
 * Contrato del servicio de pedidos.
 *
 * <ul>
 *   <li>RF-02 – Crear y enviar a cocina</li>
 *   <li>RF-03 – Facturación</li>
 *   <li>RF-04 – Buscar, modificar, cancelar</li>
 *   <li>RF-13 – Asignación de cuadrante y cálculo de ruta de entrega</li>
 * </ul>
 *
 * @author fsadriann
 */
public interface OrderInterface {
    Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException;  // RF-02
    void enviarPedidoACocina(Order pedido) throws RemoteException;                      // RF-02
    Order buscarPedido(String pedidoId) throws RemoteException;                         // RF-04
    boolean modificarPedido(Order pedido) throws RemoteException;                       // RF-04
    boolean cancelarPedido(String pedidoId) throws RemoteException;                     // RF-04
    double calcularFactura(Order pedido) throws RemoteException;                        // RF-03

    // ── RF-13 — Rutas de entrega ──────────────────────────────────────────────

    /**
     * Asigna el cuadrante de destino a un pedido existente.
     *
     * @param orderId          UUID del pedido
     * @param nombreCuadrante  nombre del cuadrante destino (debe existir en {@code cs})
     * @param cs               servicio de cuadrantes para validar existencia
     * @return {@code true} si la asignación fue exitosa; {@code false} si el pedido
     *         no existe, el cuadrante no existe, o el pedido está CANCELADO
     * @throws IllegalArgumentException si {@code nombreCuadrante} es nulo o vacío
     */
    boolean asignarCuadranteDestino(String orderId, String nombreCuadrante, CuadranteInterface cs);

    /**
     * Calcula la ruta óptima de entrega desde la base ({@value OrderService#ORIGEN_BASE})
     * hasta el cuadrante de destino del pedido, usando Dijkstra.
     *
     * @param orderId UUID del pedido con cuadranteDestino ya asignado
     * @param cs      servicio de cuadrantes con el grafo de rutas
     * @return lista ordenada de nombres de cuadrantes en la ruta óptima,
     *         o {@code null} si el pedido no existe, está cancelado,
     *         no tiene destino asignado, o no hay ruta disponible
     */
    edu.fsadriann.app.linkedlist.singly.singly.LinkedList<String> calcularRutaEntrega(
            String orderId, CuadranteInterface cs);
}

