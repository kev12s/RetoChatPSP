/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

/**
 *
 * @author 2dami
 */
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Servidor {
    private final int PUERTO = 5000;
    private Contador contadorClientes = new Contador();
    private final int MAX_CLIENTES = 3;
    
    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado. Esperando clientes...");
            
            while (contadorClientes.getContador() < MAX_CLIENTES) {
                try (Socket cliente = servidor.accept();
                     ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                     ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream())) {
                    
                    contadorClientes.incrementar();
                    System.out.println("Cliente " + contadorClientes.getContador() + " conectado.");
                    salida.writeObject("¡Hola Cliente " + contadorClientes.getContador() + "!");
                    
                } catch (Exception e) {
                    System.out.println("Error con un cliente: " + e.getMessage());
                }
            }
            
            System.out.println("Se ha alcanzado el máximo de " + MAX_CLIENTES + " clientes.");
            
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
