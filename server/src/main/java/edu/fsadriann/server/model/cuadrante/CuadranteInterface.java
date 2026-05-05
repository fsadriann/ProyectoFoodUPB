package edu.fsadriann.server.model.cuadrante;

import edu.fsadriann.app.linkedlist.singly.singly.LinkedList;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CuadranteInterface extends Remote {
    boolean agregarCuadrante(Cuadrante cuadrante) throws RemoteException;
    boolean editarCuadrante(Cuadrante cuadrante) throws RemoteException;
    Cuadrante buscarCuadrante(String nombre) throws RemoteException;
    LinkedList<Cuadrante> listarCuadrantes() throws RemoteException;
    boolean conectarCuadrantes(String nombreA, String nombreB, double distanciaKm) throws RemoteException;
    LinkedList<String> calcularRutaMasCorta(String origen, String destino) throws RemoteException;
    double calcularDistancia(String origen, String destino) throws RemoteException;
    LinkedList<String> calcularRutaPorSaltos(String origen, String destino) throws RemoteException;
    boolean existeRuta(String origen, String destino) throws RemoteException;
    LinkedList<String> vecinosDirectos(String nombre) throws RemoteException;
    int numeroCuadrantes() throws RemoteException;
    int numeroConexiones() throws RemoteException;
    String verMatrizAdyacencia() throws RemoteException;
}