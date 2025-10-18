package servidor;

import java.io.*;
import java.net.*;

public class ManejadorCliente implements Runnable {

    private Socket socket;
    private Servidor servidor;
    private ObjectOutputStream salida;
    private String usuario;

    public ManejadorCliente(Socket socket, Servidor servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            usuario = (String) entrada.readObject();

            servidor.registrarClienteConectado(usuario, this);
            salida.writeObject("CONEXION_EXITOSA");

            //El bucle de los mensajes
            while (true) {
                String mensaje = (String) entrada.readObject();
                if (mensaje == null || mensaje.equals("/salir")) {
                    break;
                }

                if (mensaje.startsWith("/privado ")) {
                    String[] partes = mensaje.split(" ", 3);
                    if (partes.length == 3) {
                        servidor.enviarMensajePrivado(usuario, partes[1], partes[2]);
                    }
                } else {
                    servidor.enviarMensajePublico(usuario, mensaje);
                }
            }
        } catch (Exception e) {
            System.out.println("Cliente desconectado: " + usuario);
        } finally {
            servidor.clienteDesconectado(usuario);
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush(); //hace que se envie el mensaje al momento
        } catch (IOException e) {
            System.err.println("Error enviando a " + usuario);
        }
    }
}
