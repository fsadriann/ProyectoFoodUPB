package edu.fsadriann.server.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.model.iterator.Iterator;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.server.model.product.ProductService;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.server.model.user.UserService;

import java.rmi.RemoteException;

/**
 * Servicio de administración del sistema Food UPB.
 * Gestiona operadores, roles, reportes de pedidos y la bitácora de auditoría.
 */
public class AdminService implements AdminInterface {

    private final LinkedList<User>   operadores;
    private final LinkedList<Order>  pedidos;
    private final LinkedList<String> bitacora;
    private final UserService        userService;
    private final ProductService     productService;
    private final OrderService       orderService;

    /**
     * Crea el servicio de administración sin integración con otros servicios.
     */
    public AdminService() {
        this(null, null, null);
    }

    /**
     * Crea el servicio de administración con acceso a los demás servicios del sistema.
     *
     * @param userService    servicio de usuarios para leer la bitácora de sesiones
     * @param productService servicio de productos (disponible para futuras extensiones)
     * @param orderService   servicio de pedidos para consultar el historial completo
     */
    public AdminService(UserService userService, ProductService productService,
                        OrderService orderService) {
        this.operadores     = new LinkedList<>();
        this.bitacora       = new LinkedList<>();
        this.pedidos        = new LinkedList<>();
        this.userService    = userService;
        this.productService = productService;
        this.orderService   = orderService;
    }

    /**
     * Registra un nuevo operador en el sistema.
     * Si el operador no tiene rol asignado, se le asigna {@link Rol#OPERADOR} por defecto.
     *
     * @param operador datos del operador a crear
     * @return {@code true} si se creó exitosamente; {@code false} si ya existe
     */
    @Override
    public boolean crearOperador(User operador) throws RemoteException {
        if (operador == null || operador.getCedula() == null) return false;
        if (buscarPorCedula(operador.getCedula()) != null) return false;

        if (operador.getRol() == null) {
            operador = new User(
                    operador.getId(),
                    operador.getNombres(),
                    operador.getApellidos(),
                    Rol.OPERADOR,
                    operador.getCedula(),
                    operador.isPremium(),
                    operador.getTelefono(),
                    operador.getDireccion(),
                    operador.getFavProductos()
            );
        }

        operadores.add(operador);
        registrar("Operador creado: " + operador.getCedula());
        return true;
    }

    /**
     * Actualiza los datos de un operador existente.
     *
     * @param operador datos actualizados del operador
     * @return {@code true} si se editó exitosamente
     */
    @Override
    public boolean editarOperador(User operador) throws RemoteException {
        if (operador == null || operador.getCedula() == null) return false;

        User existente = buscarPorCedula(operador.getCedula());
        if (existente == null) return false;

        Rol rolFinal = (operador.getRol() != null) ? operador.getRol() : existente.getRol();

        User actualizado = new User(
                operador.getId(),
                operador.getNombres(),
                operador.getApellidos(),
                rolFinal,
                operador.getCedula(),
                operador.isPremium(),
                operador.getTelefono(),
                operador.getDireccion(),
                operador.getFavProductos()
        );

        final String cedula = operador.getCedula();
        operadores.remove(u -> u != null && cedula.equals(u.getCedula()));
        operadores.add(actualizado);
        registrar("Operador editado: " + cedula);
        return true;
    }

    /**
     * Elimina un operador del sistema por su cédula.
     *
     * @param cedula cédula del operador a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    @Override
    public boolean eliminarOperador(String cedula) throws RemoteException {
        if (cedula == null) return false;
        if (buscarPorCedula(cedula) == null) return false;
        operadores.remove(u -> u != null && cedula.equals(u.getCedula()));
        registrar("Operador eliminado: " + cedula);
        return true;
    }

    /**
     * Asigna un nuevo rol a un operador registrado.
     *
     * @param cedula cédula del operador
     * @param rol    nuevo rol a asignar
     * @return {@code true} si se asignó exitosamente
     */
    @Override
    public boolean asignarRol(String cedula, Rol rol) throws RemoteException {
        if (cedula == null || rol == null) return false;

        User existente = buscarPorCedula(cedula);
        if (existente == null) return false;

        User actualizado = new User(
                existente.getId(),
                existente.getNombres(),
                existente.getApellidos(),
                rol,
                existente.getCedula(),
                existente.isPremium(),
                existente.getTelefono(),
                existente.getDireccion(),
                existente.getFavProductos()
        );

        operadores.remove(u -> u != null && cedula.equals(u.getCedula()));
        operadores.add(actualizado);
        registrar("Rol actualizado: " + cedula + " -> " + rol.name());
        return true;
    }

    /**
     * Genera un reporte de pedidos aplicando filtros opcionales por estado y cuadrante.
     * Los parámetros {@code null} o en blanco no aplican filtro en ese campo.
     *
     * @param filtroFecha     fecha a filtrar (no implementado aún)
     * @param filtroEstado    estado del pedido a filtrar
     * @param filtroCuadrante cuadrante de destino a filtrar
     * @return lista de pedidos que cumplen los criterios
     */
    @Override
    public LinkedList<Order> generarReporte(String filtroFecha, String filtroEstado,
                                            String filtroCuadrante) throws RemoteException {
        LinkedList<Order> resultado = new LinkedList<>();
        Iterator<Order> it = pedidos.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            if (o == null) continue;
            if (filtroEstado != null && !filtroEstado.isBlank()) {
                if (o.getEstado() == null ||
                        !filtroEstado.equalsIgnoreCase(o.getEstado().name())) continue;
            }
            if (filtroCuadrante != null && !filtroCuadrante.isBlank()) {
                if (o.getCuadranteDestino() == null ||
                        !filtroCuadrante.equalsIgnoreCase(o.getCuadranteDestino())) continue;
            }
            resultado.add(o);
        }
        return resultado;
    }

    /**
     * Retorna la bitácora de acciones registradas por este servicio de administración.
     *
     * @return lista de entradas de la bitácora local
     */
    public LinkedList<String> getBitacora() {
        return bitacora;
    }

    /**
     * Retorna la bitácora consolidada de auditoría, combinando la local con la del servicio de usuarios.
     *
     * @return lista completa de eventos de auditoría
     */
    @Override
    public LinkedList<String> verBitacoraAuditoria() throws RemoteException {
        LinkedList<String> todas = new LinkedList<>();
        Iterator<String> it1 = bitacora.iterator();
        while (it1.hasNext()) {
            String e = it1.next();
            if (e != null) todas.add(e);
        }
        if (userService != null) {
            Iterator<String> it2 = userService.getBitacora().iterator();
            while (it2.hasNext()) {
                String e = it2.next();
                if (e != null) todas.add(e);
            }
        }
        return todas;
    }

    /**
     * Retorna todos los pedidos del sistema obtenidos desde el servicio de pedidos.
     *
     * @return lista completa de pedidos, o lista vacía si el servicio no está disponible
     */
    @Override
    public LinkedList<Order> getPedidosTodos() throws RemoteException {
        if (orderService == null) return new LinkedList<>();
        try {
            return orderService.listarTodosLosPedidos();
        } catch (Exception e) {
            return new LinkedList<>();
        }
    }

    private User buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        Iterator<User> it = operadores.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && cedula.equals(u.getCedula())) return u;
        }
        return null;
    }

    private void registrar(String evento) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String timestamp = now.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bitacora.add(timestamp + " | " + evento);
    }
}
