package edu.fsadriann.server.model.observer;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;

/**
 * Clase base del sujeto en el patrón Observer.
 * Mantiene una lista de observadores y les notifica cuando ocurre un evento.
 */
public abstract class Subject implements Observable {

    /** Lista de observadores suscritos a este sujeto. */
    protected LinkedList<Observer> observers;

    /**
     * Inicializa el sujeto con una lista de observadores vacía.
     */
    protected Subject() {
        this.observers = new LinkedList<>();
    }

    /** {@inheritDoc} */
    @Override
    public void attach(Observer observer) {
        this.observers.add(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void detach(Observer observer) {
        this.observers.remove(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyObservers() {
        this.observers.forEach(observer -> {
            observer.update();
            return null;
        });
    }
}
