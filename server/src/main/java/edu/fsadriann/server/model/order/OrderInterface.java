package edu.fsadriann.server.model.order;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.product.Product;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato remoto para la gestión del ciclo de vida de los pedidos.
 */
public interface OrderInterface extends Remote {

    /**
     * Crea un nuevo pedido en estado PENDIENTE.
     *
     * @param cedulaCliente cédula del cliente
     * @param isPremium     {@code true} si el cliente tiene membresía premium
     * @return el pedido creado
     */
    Order crearPedido(String cedulaCliente, boolean isPremium) throws RemoteException;

    /**
     * Envía un pedido pendiente a la cocina para su preparación.
     *
     * @param pedido pedido a enviar a cocina
     */
    void enviarPedidoACocina(Order pedido) throws RemoteException;

    /**
     * Agrega un producto al carrito de un pedido en estado PENDIENTE.
     *
     * @param pedidoId identificador del pedido
     * @param product  producto a agregar
     * @return {@code true} si se agregó exitosamente
     */
    boolean agregarProducto(String pedidoId, Product product) throws RemoteException;

    /**
     * Quita un producto del carrito de un pedido en estado PENDIENTE.
     *
     * @param pedidoId identificador del pedido
     * @param product  producto a quitar
     * @return {@code true} si se quitó exitosamente
     */
    boolean quitarProducto(String pedidoId, Product product) throws RemoteException;

    /**
     * Busca un pedido por su identificador.
     *
     * @param pedidoId identificador del pedido
     * @return el pedido encontrado, o {@code null} si no existe
     */
    Order buscarPedido(String pedidoId) throws RemoteException;

    /**
     * Actualiza los datos de un pedido en estado PENDIENTE.
     *
     * @param pedido pedido con datos actualizados
     * @return {@code true} si se modificó exitosamente
     */
    boolean modificarPedido(Order pedido) throws RemoteException;

    /**
     * Cancela un pedido que no haya sido despachado aún.
     *
     * @param pedidoId identificador del pedido
     * @return {@code true} si se canceló exitosamente
     */
    boolean cancelarPedido(String pedidoId) throws RemoteException;

    /**
     * Calcula el valor total del pedido incluyendo IVA y costo de domicilio.
     *
     * @param pedido pedido a facturar
     * @return valor total en pesos colombianos
     */
    double calcularFactura(Order pedido) throws RemoteException;

    /**
     * Retorna todos los pedidos de un cliente.
     *
     * @param cedula cédula del cliente
     * @return lista de pedidos del cliente
     */
    LinkedList<Order> getPedidosPorCliente(String cedula) throws RemoteException;

    /**
     * Asigna la zona de entrega a un pedido.
     *
     * @param orderId         identificador del pedido
     * @param nombreCuadrante nombre del cuadrante de destino
     * @return {@code true} si se asignó exitosamente
     */
    boolean asignarCuadranteDestino(String orderId,
                                     String nombreCuadrante) throws RemoteException;

    /**
     * Calcula la ruta óptima desde UPB hasta el cuadrante de destino del pedido.
     *
     * @param orderId identificador del pedido
     * @return lista de cuadrantes en la ruta, o {@code null} si no hay cuadrante asignado
     */
    LinkedList<String> calcularRutaEntrega(String orderId) throws RemoteException;

    /**
     * Cambia el estado del pedido a EN_CAMINO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
    boolean marcarEnCamino(String orderId) throws RemoteException;

    /**
     * Cambia el estado del pedido a ENTREGADO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
    boolean marcarEntregado(String orderId) throws RemoteException;

    /**
     * Cambia el estado del pedido a LISTO.
     *
     * @param orderId identificador del pedido
     * @return {@code true} si el cambio fue exitoso
     */
    boolean marcarListo(String orderId) throws RemoteException;

    /**
     * Retorna los pedidos que están actualmente en preparación en cocina.
     *
     * @return lista de pedidos en estado EN_PREPARACION
     */
    LinkedList<Order> getPedidosEnPreparacion() throws RemoteException;

    /**
     * Retorna todos los pedidos registrados en el sistema.
     *
     * @return lista completa de pedidos
     */
    LinkedList<Order> listarTodosPedidos() throws RemoteException;

    /**
     * Cambia la cantidad de un producto dentro del carrito de un pedido PENDIENTE.
     *
     * @param pedidoId     identificador del pedido
     * @param productoId   identificador del producto
     * @param nuevaCantidad nueva cantidad deseada
     * @return {@code true} si se actualizó exitosamente
     */
    boolean cambiarCantidadProducto(String pedidoId, String productoId,
                                     int nuevaCantidad) throws RemoteException;
}
