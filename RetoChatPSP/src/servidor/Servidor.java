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
    private int max_clientes = 3;
    private ServerSocket serverSocket;
    private Thread hiloEstadisticas;
    private boolean estadisticasEjecutandose = true;
    private int intervaloEstadisticas = 10000;

    //atributos para los registros 
    private ArrayList<String> logMensajes;
    private Date fechaInicio;
    private String ultimoMensaje;
    private ListaClientes listaClientes;
    private ListaMensajes listaMensajes;

    public Servidor() {
        this.logMensajes = new ArrayList<>();
        this.fechaInicio = new Date();
        this.ultimoMensaje = "Ninguno";
        this.listaClientes = new ListaClientes();
        this.listaMensajes = new ListaMensajes();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);
            log("Servidor iniciado en puerto " + puerto);

            //hilo para que salgan estadisticas
            hiloEstadisticas = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (estadisticasEjecutandose) {
                        try {
                            long tiempoActivo = (new Date().getTime() - fechaInicio.getTime()) / 1000;
                            int usuariosConectados = listaClientes.getContadorClientesConectados();

                            System.out.println("=== ESTADÍSTICAS DEL SERVIDOR ===");
                            System.out.println("Hora: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                            System.out.println("Usuarios conectados: " + usuariosConectados);
                            System.out.println("Tiempo activo: " + tiempoActivo + " segundos");
                            System.out.println("Mensajes totales: " + listaMensajes.getTotalMensajes());
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

            //bucle para aceptar un máximo de 3 clientes
            while (true) {
                Socket socket = serverSocket.accept();

                //contador manejado con la lista sincronizada de la clase ListaClientes
                if (listaClientes.getContadorClientesConectados() < max_clientes) {
                    HiloCliente hiloCliente = new HiloCliente(socket, this);
                    new Thread(hiloCliente).start();
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

    //metodo para los logs, escribe en un fichero
    private void log(String mensaje) {
        File fichLogs = new File("logs.dat");
        String logGuardado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + mensaje;
        logMensajes.add(logGuardado);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichLogs, true));
            oos.writeObject(logGuardado);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(logGuardado);
    }

    // MÉTODOS PRINCIPALES QUE HAY QUE IMPLEMENTAR
    //conecta al cliente usando la clase ListaClientes
    public void registrarClienteConectado(String usuario, HiloCliente hilo) {
        if (listaClientes.registrarClienteConectado(usuario, hilo)) {
            log("Usuario conectado: " + usuario);
            hilo.enviarMensaje("CONEXION_EXITOSA");
            informarATodos("SERVIDOR: " + usuario + " se ha unido al chat", null);
        }
    }

    //desconecta al cliente usando la clase ListaClientes
    public void clienteDesconectado(String usuario) {
        if (listaClientes.clienteDesconectado(usuario)) {
            log("Usuario desconectado: " + usuario);
            informarATodos("SERVIDOR: " + usuario + " ha abandonado el chat", null);
        }
    }

    public void enviarMensajePublico(String usuario, String mensaje) {
        String mensajeCompleto = "PUBLICO [" + usuario + "]: " + mensaje;
        informarATodos(mensajeCompleto, usuario);
        
        listaMensajes.agregarMensajePublico(usuario, mensaje);
        ultimoMensaje = mensaje;
        log("Mensaje público de " + usuario + ": " + mensaje);
    }

    public void enviarMensajePrivado(String usuarioActual, String destinatario, String mensaje) {
        HiloCliente hiloDestino = listaClientes.getClientesConectados().get(destinatario);
        HiloCliente hiloRemitente = listaClientes.getClientesConectados().get(usuarioActual);

        if (hiloDestino != null && hiloRemitente != null) {
            String mensajePrivado = "PRIVADO de " + usuarioActual + ": " + mensaje;
            String mensajeConfirmacion = "PRIVADO para " + destinatario + ": " + mensaje;

            hiloDestino.enviarMensaje(mensajePrivado);
            hiloRemitente.enviarMensaje(mensajeConfirmacion);
            
            listaMensajes.agregarMensajePrivado(usuarioActual, destinatario, mensaje);
            ultimoMensaje = "PRIVADO: " + usuarioActual + " a " + destinatario + ": " + mensaje;;
            log("Mensaje privado " + usuarioActual + " a " + destinatario + ": " + mensaje);
        } else {
            // Usuario no encontrado - enviar mensaje de error al remitente
            if (hiloRemitente != null) {
                hiloRemitente.enviarMensaje("ERROR_PRIVADO: El usuario '" + destinatario + "' no está conectado");
            }
        }
    }

    //metodo para enviar el mensaje público a cada cliente que usamos en el método enviar mensaje público, 
    //tambien usamos la clase ListaClientes para enviarselo a todos menos a si mismo
    private void informarATodos(String mensaje, String usuarioActual) {
        for (Map.Entry<String, HiloCliente> entry : listaClientes.getClientesConectados().entrySet()) {
            if (!entry.getKey().equals(usuarioActual)) {
                entry.getValue().enviarMensaje(mensaje);
            }
        }
    }

    public ListaClientes getListaClientes() {
        return listaClientes;
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
