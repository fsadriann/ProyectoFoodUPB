package edu.fsadriann.foodpbproyecto.model.user;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import edu.fsadriann.foodpbproyecto.model.product.Product;

public class User {
    private String cedula;
    private boolean isPremium;
    private int telefono;
    private String direccion;
    private LinkedList<Product> favProductos;

    public User(String id, String nombres, String apellidos, Rol rol, String cedula, boolean isPremium, int telefono, String direccion, LinkedList<Product> favProductos) {
        this.cedula = cedula;
        this.isPremium = false;
        this.telefono = telefono;
        this.direccion = direccion;
        this.favProductos = new LinkedList<>();
    }


    public String getCedula() {
        return cedula;
    }
    public boolean isPremium() {
        return isPremium;
    }
    public int getTelefono() {
        return telefono;
    }
    public String getDireccion() {
        return direccion;
    }
    public LinkedList<Product> getFavProductos(){
        return favProductos;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }
    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
