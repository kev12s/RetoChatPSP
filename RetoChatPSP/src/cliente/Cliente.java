package cliente;

import java.io.*;
import java.net.*;

public class Cliente {

    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private String usuario;
    private boolean conectado = false;
    private boolean detenerRecepcion = false;

    public boolean conectar(String servidor, int puerto, String usuario) {
        try {
            this.usuario = usuario;
            socket = new Socket(servidor, puerto);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Enviar usuario
            salida.writeObject(usuario);

            // Recibir confirmación
            String respuesta = (String) entrada.readObject();
            if ("CONEXION_EXITOSA".equals(respuesta)) {
                conectado = true;
                return true;
            }

        } catch (Exception e) {
            System.out.println("Error conectando: " + e.getMessage());
        }
        return false;
    }

    public void enviarMensaje(String mensaje) throws IOException {
        if (conectado) {
            salida.writeObject(mensaje);
        }
    }

    public void enviarMensajePrivado(String destinatario, String mensaje) throws IOException {
        enviarMensaje("/privado " + destinatario + " " + mensaje);
    }

    public void detenerRecepcion() {
        detenerRecepcion = true; // ← Para que el hilo receptor se detenga
    }

    public String recibirMensaje() throws IOException, ClassNotFoundException {
        if (conectado && !detenerRecepcion) {
            return (String) entrada.readObject();
        }
        return null;
    }

    public void desconectar() {
        conectado = false;
        try {
            if (salida != null) {
                salida.writeObject("/salir");
            }
        } catch (IOException e) {

        } finally {
            try {
                if (salida != null) {
                    salida.close();
                }
                if (entrada != null) {
                    entrada.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConectado() {
        return conectado;
    }

    public String getUsuario() {
        return usuario;
    }
}
