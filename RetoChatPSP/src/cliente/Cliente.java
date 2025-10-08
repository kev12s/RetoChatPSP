/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

/**
 *
 * @author 2dami
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author barai
 */
public class Cliente {
    
    private final int PUERTO = 5000;
    private final String IP = "127.0.0.1";

    public void iniciar() {
        Socket cliente = null;
        ObjectInputStream entrada = null;
        ObjectOutputStream salida = null;
        try {
            cliente = new Socket(IP, PUERTO);
            System.out.println("Conexión realizada con servidor");
            salida = new ObjectOutputStream(cliente.getOutputStream());
            entrada = new ObjectInputStream(cliente.getInputStream());
            String mensaje = (String) entrada.readObject();
            System.out.println(mensaje);
            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (entrada != null) {
                    entrada.close();
                }
                if (salida != null) {
                    salida.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Fin cliente");
        }
    }

    public static void main(String[] args) {
        Cliente c = new Cliente();
        c.iniciar();
    }
}
