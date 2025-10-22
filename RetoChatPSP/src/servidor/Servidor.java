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
import java.awt.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {

    //atributos para la conexion
    private int puerto = 5000;
    private Contador contadorClientes;
    private int max_clientes = 3;
    private ServerSocket serverSocket;
    private Thread hiloEstadisticas;
    private boolean estadisticasEjecutandose = true;
    private int intervaloEstadisticas = 10000;

    //atributos para los registros 
    private Map<String, ManejadorCliente> clientesConectados;
    private ArrayList<String> logMensajes;
    private Date fechaInicio;
    private String ultimoMensaje;
    //mas adelante hay que añadiir las estadisticas del servidor y un metodo para sacarlas  

    public Servidor() {
        this.clientesConectados = new HashMap<>();
        this.logMensajes = new ArrayList<>();
        this.fechaInicio = new Date();
        this.ultimoMensaje = "Ninguno"; //hay que implementar
        this.contadorClientes = new Contador();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);
            log("Servidor iniciado");

            //hilo para que salgan estadisticas
            hiloEstadisticas = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (estadisticasEjecutandose) {
                        try {
                            long tiempoActivo = (new Date().getTime() - fechaInicio.getTime()) / 1000;
                            int usuariosConectados = contadorClientes.getContador();

                            System.out.println("=== ESTADÍSTICAS DEL SERVIDOR ===");
                            System.out.println("Hora: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                            System.out.println("Usuarios conectados: " + usuariosConectados);
                            System.out.println("Tiempo activo: " + tiempoActivo + " segundos");
                            System.out.println("Último mensaje: " + ultimoMensaje);
                            System.out.println("=================================");
                            Thread.sleep(intervaloEstadisticas);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            
            //corre el hilo de estadisticas
            hiloEstadisticas.start();

            //
            while (true) {
                Socket socket = serverSocket.accept();

                if (contadorClientes.getContador() < max_clientes) {
                    ManejadorCliente handler = new ManejadorCliente(socket, this);
                    new Thread(handler).start();
                } else {
                    ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
                    salida.writeObject("ERROR: Servidor lleno");
                    socket.close();
                }
            }

        } catch (IOException e) {
            System.err.println("Error en servidor: " + e.getMessage());
        }
    }

    private synchronized void log(String mensaje) {
        File fichLogs = new File("logs.dat");
        String logGuardado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + mensaje;
        logMensajes.add(logGuardado);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichLogs));
            oos.writeObject(logGuardado);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(logGuardado);
    }

    // MÉTODOS PRINCIPALES QUE HAY QUE IMPLEMENTAR
    public synchronized void registrarClienteConectado(String usuario, ManejadorCliente handler) {
        clientesConectados.put(usuario, handler);
        contadorClientes.incrementar();
        log("Usuario conectado: " + usuario);
        informarATodos("SERVIDOR: " + usuario + " se ha unido al chat", null);
    }

    //desconecta al cliente
    public synchronized void clienteDesconectado(String usuario) {
        if (clientesConectados.remove(usuario) != null) {
            contadorClientes.decrementar();
            log("Usuario desconectado: " + usuario);
            informarATodos("SERVIDOR: " + usuario + " ha abandonado el chat", null);
        }
    }

    public synchronized void enviarMensajePublico(String usuario, String mensaje) {
        String mensajeCompleto = "PUBLICO [" + usuario + "]: " + mensaje;      
        informarATodos(mensajeCompleto, usuario);
        ultimoMensaje=mensaje;
        log("Mensaje público de " + usuario + ": " + mensaje);
    }

    public synchronized void enviarMensajePrivado(String usuarioActual, String destinatario, String mensaje) {
        ManejadorCliente manejadorDestino = clientesConectados.get(destinatario);
        ManejadorCliente manejadorRemitente = clientesConectados.get(usuarioActual);

        if (manejadorDestino != null && manejadorRemitente != null) {
            String mensajePrivado = "PRIVADO de " + usuarioActual + ": " + mensaje;
            String mensajeConfirmacion = "PRIVADO para " + destinatario + ": " + mensaje;

            manejadorDestino.enviarMensaje(mensajePrivado);
            manejadorRemitente.enviarMensaje(mensajeConfirmacion);
            ultimoMensaje = "PRIVADO: " + usuarioActual + " a " + destinatario + ": " + mensaje;;
            log("Mensaje privado " + usuarioActual + " a " + destinatario + ": " + mensaje);
        }
    }

    private synchronized void informarATodos(String mensaje, String usuarioActual) {
        for (Map.Entry<String, ManejadorCliente> entry : clientesConectados.entrySet()) {
            if (!entry.getKey().equals(usuarioActual)) {
                entry.getValue().enviarMensaje(mensaje);
            }
        }
    }
    

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
