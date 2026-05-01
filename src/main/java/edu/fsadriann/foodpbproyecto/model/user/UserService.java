package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.doubly.doubly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.order.Order;
import edu.fsadriann.model.iterator.Iterator;

import java.rmi.RemoteException;

/**
 * Servicio de usuarios: implementa {@link UserInterface} con lógica real.
 *
 * <p>Almacena clientes en una {@code DoublyLinkedList} del JAR.
 * Usa el {@code Iterator} tipado del JAR para evitar problemas de cast con genéricos.
 *
 * <ul>
 *   <li>RF-01 – Buscar cliente por teléfono</li>
 *   <li>RF-11 – Registrar nuevo cliente (valida unicidad)</li>
 *   <li>RF-10 – Actualizar perfil (stub parcial)</li>
 *   <li>RF-09 – Login/logout (stubs, FASE 5)</li>
 * </ul>
 *
 * @author fsadriann
 */
public class UserService implements UserInterface {

    /** Almacén principal de clientes. DoublyLinkedList del JAR. */
    private final LinkedList<User> clientes;

    public UserService() {
        this.clientes = new LinkedList<>();
    }

    // ── RF-01 ────────────────────────────────────────────────────────────────

    /**
     * Busca un cliente por número de teléfono. RF-01.
     *
     * @param telefono teléfono como String (convierte internamente a int para comparar)
     * @return cliente encontrado o {@code null}
     */
    @Override
    public User buscarClientePorTelefono(String telefono) throws RemoteException {
        if (telefono == null) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && telefono.equals(String.valueOf(u.getTelefono()))) {
                return u;
            }
        }
        return null;
    }

    // ── RF-11 ────────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo cliente validando unicidad de teléfono y cédula. RF-11.
     *
     * @param user datos del cliente a registrar
     * @return cliente guardado
     * @throws IllegalArgumentException si el teléfono o cédula ya están registrados
     */
    @Override
    public User registrarCliente(User user) throws RemoteException {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (buscarClientePorTelefono(String.valueOf(user.getTelefono())) != null) {
            throw new IllegalArgumentException(
                    "Teléfono ya registrado: " + user.getTelefono());
        }
        if (buscarPorCedula(user.getCedula()) != null) {
            throw new IllegalArgumentException(
                    "Cédula ya registrada: " + user.getCedula());
        }
        clientes.add(user);
        return user;
    }

    // ── RF-10 ────────────────────────────────────────────────────────────────

    /** Actualiza datos del cliente identificado por cédula. */
    @Override
    public boolean actualizarCliente(User user) throws RemoteException {
        if (user == null) return false;
        boolean removed = clientes.remove(u -> user.getCedula().equals(u.getCedula()));
        if (removed) clientes.add(user);
        return removed;
    }

    /** @see #actualizarCliente(User) */
    @Override
    public boolean actualizarPerfil(User user) throws RemoteException {
        return actualizarCliente(user);
    }

    /** Elimina un cliente por cédula. */
    @Override
    public boolean eliminarCliente(String cedula) throws RemoteException {
        if (cedula == null) return false;
        return clientes.remove(u -> cedula.equals(u.getCedula()));
    }

    /**
     * Historial de pedidos del cliente. Requiere OrderService — stub por ahora.
     * TODO RF-01: implementar con inyección de OrderService.
     */
    @Override
    public edu.fsadriann.app.linkedlist.singly.singly.LinkedList<Order>
    getPedidosFrecuentes(String cedula) throws RemoteException {
        return new edu.fsadriann.app.linkedlist.singly.singly.LinkedList<>();
    }

    /** @return {@code false} — implementar en RF-09 (FASE 5) */
    @Override
    public boolean cambiarContrasena(String cedula, String actual, String nueva)
            throws RemoteException {
        // TODO RF-09
        return false;
    }

    // ── RF-09 stubs (FASE 5) ─────────────────────────────────────────────────

    @Override
    public String login(String correo, String contrasena) throws RemoteException {
        // TODO RF-09
        return null;
    }

    @Override
    public void logout(String sesionId) throws RemoteException {
        // TODO RF-09
    }

    @Override
    public boolean validarSesion(String sesionId) throws RemoteException {
        // TODO RF-09
        return false;
    }

    // ── Helper privado ───────────────────────────────────────────────────────

    /** Busca un cliente por cédula usando el Iterator tipado del JAR. */
    private User buscarPorCedula(String cedula) {
        if (cedula == null) return null;
        Iterator<User> it = clientes.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u != null && cedula.equals(u.getCedula())) return u;
        }
        return null;
    }
}
