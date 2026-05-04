package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.model.iterator.Iterator;

import java.util.Arrays;

import java.rmi.RemoteException;

public class UserService implements UserInterface {
    private final LinkedList<User> clientes;
    private final LinkedList<SessionEntry> sesionesActivas;
    private final LinkedList<CredentialEntry> credenciales;
    private final OrderService orderService;

    private static class SessionEntry {
        private final String sesionId;
        private final String cedula;

        private SessionEntry(String sesionId, String cedula) {
            this.sesionId = sesionId;
            this.cedula = cedula;
        }
    }

    private static class CredentialEntry {
        private final String cedula;
        private String contrasena;

        private CredentialEntry(String cedula, String contrasena) {
            this.cedula = cedula;
            this.contrasena = contrasena;
        }
    }

    public UserService() {
        this(null);
    }

    public UserService(OrderService orderService) {
        this.clientes = new LinkedList<>();
        this.sesionesActivas = new LinkedList<>();
        this.credenciales = new LinkedList<>();
        this.orderService = orderService;
        seedDefaultData();
    }

    @Override
    public Rol login(String correo, String contrasena) throws RemoteException {
        if (correo == null || contrasena == null) return null;
        User usuario = buscarPorId(correo);
        if (usuario == null) return null;
        String passGuardada = getContrasena(usuario.getCedula());
        if (passGuardada == null || !passGuardada.equals(contrasena)) return null;
        return usuario.getRol();
    }

    @Override
    public void logout(String sesionId) throws RemoteException {
        if (sesionId == null) return;
        sesionesActivas.remove(s -> s != null && sesionId.equals(s.sesionId));
    }

    @Override
    public boolean validarSesion(String sesionId) throws RemoteException {
        if (sesionId == null) return false;
        Iterator<SessionEntry> it = sesionesActivas.iterator();
        while (it.hasNext()) {
            SessionEntry entry = it.next();
            if (entry != null && sesionId.equals(entry.sesionId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cambiarContrasena(String cedula, String actual, String nueva)
            throws RemoteException {
        if (cedula == null || actual == null || nueva == null || nueva.isBlank()) return false;
        if (buscarPorCedula(cedula) == null) return false;
        String passGuardada = getContrasena(cedula);
        if (passGuardada == null || !passGuardada.equals(actual)) return false;
        setContrasena(cedula, nueva);
        sesionesActivas.remove(s -> s != null && cedula.equals(s.cedula));
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
        if (getContrasena(user.getCedula()) == null) {
            setContrasena(user.getCedula(), user.getCedula());
        }
        return user;
    }

    @Override
    public LinkedList<User> listarUsuarios() throws RemoteException {
        return clientes;
    }

    @Override
    public int getTotalUsuarios() throws RemoteException {
        int total = 0;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            if (it.next() != null) {
                total++;
            }
        }
        return total;
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
            credenciales.remove(c -> c != null && cedula.equals(c.cedula));
            // Invalidar todas las sesiones del usuario eliminado
            sesionesActivas.remove(s -> s != null && cedula.equals(s.cedula));
        }
        return removed;
    }

    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order>
    getPedidosFrecuentes(String cedula) throws RemoteException {
        edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order> resultado = new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
        if (cedula == null || cedula.isBlank() || orderService == null) {
            return resultado;
        }

        Order[] pedidos = orderService.getPedidosPorCliente(cedula).toArray();
        if (pedidos == null || pedidos.length == 0) {
            return resultado;
        }

        Arrays.sort(pedidos, (a, b) -> Integer.compare(
                b != null ? b.getCantProductos() : 0,
                a != null ? a.getCantProductos() : 0));

        int total = 0;
        for (Order pedido : pedidos) {
            if (pedido == null) {
                continue;
            }
            resultado.add(pedido);
            total++;
            if (total == 10) {
                break;
            }
        }
        return resultado;
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

    private String getContrasena(String cedula) {
        Iterator<CredentialEntry> it = credenciales.iterator();
        while (it.hasNext()) {
            CredentialEntry entry = it.next();
            if (entry != null && cedula.equals(entry.cedula)) {
                return entry.contrasena;
            }
        }
        return null;
    }

    private void setContrasena(String cedula, String contrasena) {
        Iterator<CredentialEntry> it = credenciales.iterator();
        while (it.hasNext()) {
            CredentialEntry entry = it.next();
            if (entry != null && cedula.equals(entry.cedula)) {
                entry.contrasena = contrasena;
                return;
            }
        }
        credenciales.add(new CredentialEntry(cedula, contrasena));
    }

    @Override
    public void registrarCredencial(String cedula, String contrasena) throws RemoteException {
        if (cedula == null || contrasena == null || contrasena.isBlank())
            throw new IllegalArgumentException("Cédula y contraseña son obligatorias.");
        if (buscarPorCedula(cedula) == null)
            throw new IllegalArgumentException("No existe cliente con cédula: " + cedula);
        setContrasena(cedula, contrasena);
    }

    private void seedDefaultData() {
        User operador = new User(
                "operator@test.com",
                "Operador",
                "Food UPB",
                Rol.OPERADOR,
                "1001",
                false,
                300123456,
                "Campus UPB",
                null
        );

        User cliente = new User(
                "cliente@upb.com",
                "Cliente",
                "Demo",
            Rol.CLIENTE,
                "2001",
                true,
                300765432,
                "Cra 1 # 1-1",
                null
        );

        User admin = new User(
                "admin@test.com",
                "Admin",
                "Sistema",
                Rol.ADMIN,
                "1234",
                true,
                300765432,
                "Cra 1 # 1-1",
                null
        );

        User cocina = new User(
                "cocina@test.com",
                "Cocina",
                "Food UPB",
                Rol.COCINA,
                "3001",
                false,
                300555111,
                "Central de cocina",
                null
        );

        User entrega = new User(
                "entrega@test.com",
                "Entrega",
                "Food UPB",
                Rol.ENTREGA,
                "4001",
                false,
                300555222,
                "Zona de reparto",
                null
        );

        User server = new User(
                "server@test.com",
                "Server",
                "Food UPB",
                Rol.SERVER,
                "5001",
                false,
                300555333,
                "Panel operativo",
                null
        );

        clientes.add(operador);
        clientes.add(cliente);
        clientes.add(admin);
        clientes.add(cocina);
        clientes.add(entrega);
        clientes.add(server);
        setContrasena(operador.getCedula(), "1234");
        setContrasena(cliente.getCedula(), "1234");
        setContrasena(admin.getCedula(), "1234");
        setContrasena(cocina.getCedula(), "1234");
        setContrasena(entrega.getCedula(), "1234");
        setContrasena(server.getCedula(), "1234");
    }
}