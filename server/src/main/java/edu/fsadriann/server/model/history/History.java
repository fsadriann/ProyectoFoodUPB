package edu.fsadriann.server.model.history;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.app.stack.list.Stack;
import edu.fsadriann.server.model.observer.Subject;

/**
 * Mantiene el historial de acciones del sistema usando una pila.
 * Extiende {@link Subject} para notificar a los observadores cuando se registra una nueva acción.
 */
public class History extends Subject {

    private Stack<Action> actions;

    /**
     * Crea un historial vacío.
     */
    public History() {
        this.actions = new Stack<>(new LinkedList<>());
    }

    /**
     * Registra una nueva acción en el historial y notifica a los observadores.
     *
     * @param description descripción de la acción a registrar
     */
    public void addAction(String description) {
        this.actions.push(new Action(description));
        this.notifyObservers();
    }

    /**
     * Retorna la descripción de la última acción registrada.
     *
     * @return texto con la fecha y descripción de la última acción,
     *         o un mensaje indicando que no hay acciones
     */
    public String getLastAction() {
        if (actions.isEmpty()) {
            return "No actions yet.";
        }
        Action lastAction = actions.peek();
        return lastAction.getTimestamp() + ": " + lastAction.getDescription();
    }
}
