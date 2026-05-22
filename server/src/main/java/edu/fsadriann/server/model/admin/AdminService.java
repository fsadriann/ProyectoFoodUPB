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

public class AdminService implements AdminInterface {

    private final LinkedList<User>   operadores;
    private final LinkedList<Order>  pedidos;
    private final LinkedList<String> bitacora;
    private final UserService        userService;
    private final ProductService     productService;
    private final OrderService orderService;


    public AdminService() {
        this(null, null, null);
    }

    public AdminService(UserService userService, ProductService productService, OrderService orderService) {
        this.operadores     = new LinkedList<>();
        this.bitacora       = new LinkedList<>();
        this.pedidos        = new LinkedList<>();
        this.userService    = userService;
        this.productService = productService;
        this.orderService   = orderService;
    }

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

    @Override
    public boolean eliminarOperador(String cedula) throws RemoteException {
        if (cedula == null) return false;
        if (buscarPorCedula(cedula) == null) return false;
        operadores.remove(u -> u != null && cedula.equals(u.getCedula()));
        registrar("Operador eliminado: " + cedula);
        return true;
    }

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

    @Override
    public LinkedList<Order> generarReporte(String filtroFecha,
                                            String filtroEstado,
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

    public LinkedList<String> getBitacora() {
        return bitacora;
    }

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