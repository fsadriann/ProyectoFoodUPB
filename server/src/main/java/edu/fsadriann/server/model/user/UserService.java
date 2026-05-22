package edu.fsadriann.server.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.server.model.order.Order;
import edu.fsadriann.server.model.order.OrderService;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio que gestiona los usuarios, la autenticación y las sesiones activas en Food UPB.
 * Carga los datos desde archivos JSON al iniciar y persiste los cambios automáticamente.
 */
public class UserService implements UserInterface {

    private final LinkedList<User>            clientes;
    private final LinkedList<String>          bitacora;
    private final LinkedList<SessionEntry>    sesionesActivas;
    private final LinkedList<CredentialEntry> credenciales;
    private final OrderService                orderService;
    private final UserRepository              repository = new UserRepository();

    private static class SessionEntry {
        private final String sesionId;
        private final String cedula;

        private SessionEntry(String sesionId, String cedula) {
            this.sesionId = sesionId;
            this.cedula   = cedula;
        }
    }

    private static class CredentialEntry {
        private final String cedula;
        private String contrasena;

        private CredentialEntry(String cedula, String contrasena) {
            this.cedula     = cedula;
            this.contrasena = contrasena;
        }
    }

    /**
     * Crea el servicio de usuarios sin integración con el servicio de pedidos.
     */
    public UserService() {
        this(null);
    }

    /**
     * Crea el servicio de usuarios con integración al servicio de pedidos.
     *
     * @param orderService servicio de pedidos para consultar el historial de clientes
     */
    public UserService(OrderService orderService) {
        this.clientes        = new LinkedList<>();
        this.sesionesActivas = new LinkedList<>();
        this.credenciales    = new LinkedList<>();
        this.orderService    = orderService;
        this.bitacora        = new LinkedList<>();
        cargarDatos();
    }

    private void cargarDatos() {
        Iterator<User> itU = repository.findAllUsers().iterator();
        while (itU.hasNext()) {
            User u = itU.next();
            if (u != null) clientes.add(u);
        }
        Iterator<UserRepository.CredentialEntry> itC = repository.findAllCredentials().iterator();
        while (itC.hasNext()) {
            UserRepository.CredentialEntry c = itC.next();
            if (c != null) credenciales.add(new CredentialEntry(c.cedula, c.contrasena));
        }
    }

    private void registrar(String evento) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String timestamp = now.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bitacora.add(timestamp + " | " + evento);
    }

    /**
     * Autentica a un usuario por su correo y contraseña.
     *
     * @param correo     correo electrónico del usuario
     * @param contrasena contraseña de acceso
     * @return rol del usuario si las credenciales son correctas, o {@code null} si fallan
     */
    @Override
    public Rol login(String correo, String contrasena) throws RemoteException {
        if (correo == null || contrasena == null) return null;
        User usuario = buscarPorId(correo);
        if (usuario == null) return null;
        String passGuardada = getContrasena(usuario.getCedula());
        if (passGuardada == null || !passGuardada.equals(contrasena)) return null;
        registrar("Login exitoso: " + correo + " | rol=" + usuario.getRol());
        return usuario.getRol();
    }

    /**
     * Cierra la sesión activa del usuario.
     *
     * @param sesionId identificador de la sesión a cerrar
     */
    @Override
    public void logout(String sesionId) throws RemoteException {
        if (sesionId == null) return;
        sesionesActivas.remove(s -> s != null && sesionId.equals(s.sesionId));
    }

    /**
     * Verifica si una sesión está activa en el sistema.
     *
     * @param sesionId identificador de sesión a validar
     * @return {@code true} si la sesión existe y es válida
     */
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

    /**
     * Cambia la contraseña de un usuario, verificando primero la contraseña actual.
     * Cierra las sesiones activas del usuario después del cambio.
     *
     * @param cedula  cédula del usuario
     * @param actual  contraseña actual
     * @param nueva   nueva contraseña
     * @return {@code true} si el cambio fue exitoso
     */
    @Override
    public boolean cambiarContrasena(String cedula, String actual, String nueva)
            throws RemoteException {
        if (cedula == null || actual == null || nueva == null || nueva.isBlank()) return false;
        if (buscarPorCedula(cedula) == null) return false;
        String passGuardada = getContrasena(cedula);
        if (passGuardada == null || !passGuardada.equals(actual)) return false;
        setContrasena(cedula, nueva);
        repository.saveCredential(cedula, nueva);
        sesionesActivas.remove(s -> s != null && cedula.equals(s.cedula));
        return true;
    }

    /**
     * Busca un cliente por su número de teléfono.
     *
     * @param telefono número de teléfono
     * @return el usuario encontrado, o {@code null} si no existe
     */
    @Override
    public User buscarClientePorTelefono(String telefono) throws RemoteException {
        if (telefono == null || telefono.isBlank()) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(u.getTelefono())) {
                return u;
            }
        }
        return null;
    }

    /**
     * Registra un nuevo cliente en el sistema. El teléfono y la cédula deben ser únicos.
     * Asigna como contraseña inicial la cédula del cliente.
     *
     * @param user datos del cliente a registrar
     * @return el usuario registrado
     * @throws IllegalArgumentException si el teléfono o la cédula ya están registrados
     */
    @Override
    public User registrarCliente(User user) throws RemoteException {
        if (user == null) return null;
        if (buscarClientePorTelefono(user.getTelefono()) != null) {
            throw new IllegalArgumentException(
                    "Teléfono ya registrado: " + user.getTelefono());
        }
        if (buscarPorCedula(user.getCedula()) != null) {
            throw new IllegalArgumentException(
                    "Cédula ya registrada: " + user.getCedula());
        }
        clientes.add(user);
        repository.saveUser(user);
        if (getContrasena(user.getCedula()) == null) {
            setContrasena(user.getCedula(), user.getCedula());
            repository.saveCredential(user.getCedula(), user.getCedula());
        }
        registrar("Usuario registrado: " + user.getId() + " | rol=" + user.getRol());
        return user;
    }

    /**
     * Retorna todos los usuarios registrados en el sistema.
     *
     * @return lista de usuarios
     */
    @Override
    public LinkedList<User> listarUsuarios() throws RemoteException {
        return clientes;
    }

    /**
     * @return cantidad total de usuarios registrados
     */
    @Override
    public int getTotalUsuarios() throws RemoteException {
        int total = 0;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            if (it.next() != null) total++;
        }
        return total;
    }

    /**
     * Actualiza los datos de un cliente existente.
     *
     * @param user datos actualizados del cliente
     * @return {@code true} si se actualizó exitosamente
     */
    @Override
    public boolean actualizarCliente(User user) throws RemoteException {
        if (user == null || user.getCedula() == null) return false;
        boolean removed = clientes.remove(u -> user.getCedula().equals(u.getCedula()));
        if (removed) {
            clientes.add(user);
            repository.saveUser(user);
            registrar("Usuario actualizado: " + user.getId());
        }
        return removed;
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param user datos del perfil a actualizar
     * @return {@code true} si se actualizó exitosamente
     */
    @Override
    public boolean actualizarPerfil(User user) throws RemoteException {
        return actualizarCliente(user);
    }

    /**
     * Elimina un cliente del sistema y sus credenciales y sesiones asociadas.
     *
     * @param cedula cédula del cliente a eliminar
     * @return {@code true} si se eliminó exitosamente
     */
    @Override
    public boolean eliminarCliente(String cedula) throws RemoteException {
        if (cedula == null) return false;
        boolean removed = clientes.remove(u -> cedula.equals(u.getCedula()));
        if (removed) {
            credenciales.remove(c -> c != null && cedula.equals(c.cedula));
            sesionesActivas.remove(s -> s != null && cedula.equals(s.cedula));
            repository.deleteUser(cedula);
            repository.deleteCredential(cedula);
            registrar("Usuario eliminado: cedula=" + cedula);
        }
        return removed;
    }

    /**
     * Retorna los hasta 10 pedidos más frecuentes de un cliente, ordenados por cantidad de ítems.
     *
     * @param cedula cédula del cliente
     * @return lista de pedidos frecuentes
     */
    @Override
    public LinkedList<Order> getPedidosFrecuentes(String cedula) throws RemoteException {
        LinkedList<Order> resultado = new LinkedList<>();
        if (cedula == null || cedula.isBlank() || orderService == null) {
            return resultado;
        }
        LinkedList<Order> pedidos = orderService.getPedidosPorCliente(cedula);
        if (pedidos == null || pedidos.isEmpty()) return resultado;
        pedidos.sort(o -> o != null ? -o.getCantProductos() : 0);
        Iterator<Order> it = pedidos.iterator();
        int total = 0;
        while (it.hasNext() && total < 10) {
            Order pedido = it.next();
            if (pedido == null) continue;
            resultado.add(pedido);
            total++;
        }
        return resultado;
    }

    /**
     * Registra las credenciales de acceso para un cliente existente.
     *
     * @param cedula     cédula del cliente
     * @param contrasena contraseña a asignar
     * @throws IllegalArgumentException si los parámetros son inválidos o el cliente no existe
     */
    @Override
    public void registrarCredencial(String cedula, String contrasena) throws RemoteException {
        if (cedula == null || contrasena == null || contrasena.isBlank())
            throw new IllegalArgumentException("Cédula y contraseña son obligatorias.");
        if (buscarPorCedula(cedula) == null)
            throw new IllegalArgumentException("No existe cliente con cédula: " + cedula);
        setContrasena(cedula, contrasena);
        repository.saveCredential(cedula, contrasena);
    }

    /** @return lista de eventos registrados en la bitácora del servicio de usuarios */
    public LinkedList<String> getBitacora() {
        return bitacora;
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
}
