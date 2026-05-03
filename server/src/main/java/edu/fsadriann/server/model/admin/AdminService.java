package edu.fsadriann.server.model.admin;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.user.Rol;
import edu.fsadriann.server.model.user.User;
import edu.fsadriann.model.list.List;

import java.rmi.RemoteException;

public class AdminService implements AdminInterface {

    private final LinkedList<User>   operadores;
    private final LinkedList<String> bitacora;
    private final LinkedList<Order>  pedidos;


    public AdminService() {
        this.operadores = new LinkedList<>();
        this.bitacora   = new LinkedList<>();
        this.pedidos    = new LinkedList<>();
    }

    public AdminService(LinkedList<Order> pedidos) {
        this.operadores = new LinkedList<>();
        this.bitacora   = new LinkedList<>();
        this.pedidos    = (pedidos != null) ? pedidos : new LinkedList<>();
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

        for (Order o : pedidos.toArray()) {
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
    @Override
    public LinkedList<String> verBitacoraAuditoria() throws RemoteException {
        return bitacora;
    }

    private User buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        for (User u : operadores.toArray()) {
            if (u != null && cedula.equals(u.getCedula())) return u;
        }
        return null;
    }

    private void registrar(String evento) {
        bitacora.add("[" + System.currentTimeMillis() + "] " + evento);
    }
}