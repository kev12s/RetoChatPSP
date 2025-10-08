/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private  Socket socket;
    private  int clientId;
    private  Contador conexionesActivas;

    public ManejadorCliente(Socket socket, int clientId, Contador conexionesActivas) {
        this.socket = socket;
        this.clientId = clientId;
        this.conexionesActivas = conexionesActivas;
    }

    @Override
    public void run() {
        String saludoCliente;
        Object obj;

        try (Socket s = this.socket;
             ObjectOutputStream salida = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(s.getInputStream())) {

            // Enviar confirmación al cliente
            salida.writeObject("Conexión exitosa al servidor. Eres el cliente #" + clientId);

            // Recibir saludo del cliente
            obj = entrada.readObject();
            if (obj instanceof String) {
                saludoCliente = (String) obj;     // ya era String
            } else {
                saludoCliente = String.valueOf(obj); // lo convierto a texto (maneja null)
            }
            System.out.println("[Hilo Cliente #" + clientId + "] dice: " + saludoCliente);

        } catch (Exception e) {
            System.out.println("[Hilo Cliente #" + clientId + "] Error: " + e.getMessage());
        } finally {
            int restantes = conexionesActivas.decrementar();
            System.out.println("[Hilo Cliente #" + clientId + "] finalizado. Conexiones activas: " + restantes);
        }
    }
}
