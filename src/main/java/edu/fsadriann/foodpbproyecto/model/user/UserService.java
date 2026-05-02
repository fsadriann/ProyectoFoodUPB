package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.doubly.doubly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService implements UserInterface {
    private final LinkedList<User> clientes;
    private final Map<String, String> sesionesActivas;
    private final Map<String, String> credenciales;

    public UserService() {
        this.clientes       = new LinkedList<>();
        this.sesionesActivas = new HashMap<>();
        this.credenciales   = new HashMap<>();
    }

    @Override
    public String login(String correo, String contrasena) throws RemoteException {
        if (correo == null || contrasena == null) return null;
        User usuario = buscarPorId(correo);
        if (usuario == null) return null;
        String passGuardada = credenciales.get(usuario.getCedula());
        if (passGuardada == null || !passGuardada.equals(contrasena)) return null;
        String sesionId = UUID.randomUUID().toString();
        sesionesActivas.put(sesionId, usuario.getCedula());
        return sesionId;
    }

    @Override
    public void logout(String sesionId) throws RemoteException {
        if (sesionId == null) return;
        sesionesActivas.remove(sesionId);
    }

    @Override
    public boolean validarSesion(String sesionId) throws RemoteException {
        if (sesionId == null) return false;
        return sesionesActivas.containsKey(sesionId);
    }

    @Override
    public boolean cambiarContrasena(String cedula, String actual, String nueva)
            throws RemoteException {
        if (cedula == null || actual == null || nueva == null || nueva.isBlank()) return false;
        if (buscarPorCedula(cedula) == null) return false;
        String passGuardada = credenciales.get(cedula);
        if (passGuardada == null || !passGuardada.equals(actual)) return false;
        credenciales.put(cedula, nueva);
        sesionesActivas.entrySet().removeIf(e -> cedula.equals(e.getValue()));
        return true;
    }

    @Override
    public User buscarClientePorTelefono(String telefono) throws RemoteException {
        if (telefono == null || telefono.isBlank()) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(String.valueOf(u.getTelefono()))) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User registrarCliente(User user) throws RemoteException {
        if (user == null) return null;

        if (buscarClientePorTelefono(String.valueOf(user.getTelefono())) != null) {
            throw new IllegalArgumentException(
                    "Teléfono ya registrado: " + user.getTelefono());
        }
        if (buscarPorCedula(user.getCedula()) != null) {
            throw new IllegalArgumentException(
                    "Cédula ya registrada: " + user.getCedula());
        }
        clientes.add(user);
        credenciales.putIfAbsent(user.getCedula(), user.getCedula());
        return user;
    }

    @Override
    public boolean actualizarCliente(User user) throws RemoteException {
        if (user == null || user.getCedula() == null) return false;
        boolean removed = clientes.remove(u -> user.getCedula().equals(u.getCedula()));
        if (removed) clientes.add(user);
        return removed;
    }

    @Override
    public boolean actualizarPerfil(User user) throws RemoteException {
        return actualizarCliente(user);
    }

    @Override
    public boolean eliminarCliente(String cedula) throws RemoteException {
        if (cedula == null) return false;
        boolean removed = clientes.remove(u -> cedula.equals(u.getCedula()));
        if (removed) {
            // Limpiar credencial
            credenciales.remove(cedula);
            // Invalidar todas las sesiones del usuario eliminado
            sesionesActivas.entrySet().removeIf(e -> cedula.equals(e.getValue()));
        }
        return removed;
    }

    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order>
    getPedidosFrecuentes(String cedula) throws RemoteException {
        // TODO RF-01: inyectar OrderService y delegar la búsqueda real
        return new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
    }

    private User buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && cedula.equals(u.getCedula())) return u;
        }
        return null;
    }

    private User buscarPorId(String correo) {
        if (correo == null) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && correo.equals(u.getId())) return u;
        }
        return null;
    }

    public void registrarCredencial(String cedula, String contrasena) throws RemoteException {
        if (cedula == null || contrasena == null || contrasena.isBlank())
            throw new IllegalArgumentException("Cédula y contraseña son obligatorias.");
        if (buscarPorCedula(cedula) == null)
            throw new IllegalArgumentException("No existe cliente con cédula: " + cedula);
        credenciales.put(cedula, contrasena);
    }
}