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

public class Servidor {

    //atributos para la conexion
    private int puerto = 5000;
    private Contador contadorClientes;
    private int max_clientes = 3;
    private ServerSocket serverSocket;
    
    //atributos para 
    private Map<String, ManejadorCliente> clientesConectados;
    private ArrayList<String> logMensajes;
    private Date fechaInicio;
    private String ultimoMensaje;
    //mas adelante hay que añadiir las estadisticas del servidor y un metodo para sacarlas  
    
    public Servidor() {
        this.clientesConectados = new HashMap<>();
        this.logMensajes = new ArrayList<>();
        this.fechaInicio = new Date();
        this.ultimoMensaje = "Ninguno";
        this.contadorClientes = new Contador();
    }

    public void iniciar() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);
            log("Servidor iniciado");
            
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
    
    //ESCRIBIR LOS LOGS EN UN FICHERO?!?!?!?!
     private synchronized void log(String mensaje) {
        String logGuardado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + mensaje;
        logMensajes.add(logGuardado);
        
        //SERIA ESCRIBIR EN UN FICHERO EN VEZ DE MOSTRAR POR CONSOLA
        System.out.println(logGuardado);
    }
     
      // MÉTODOS PRINCIPALES QUE HAY QUE IMPLEMENTAR
    public synchronized void registrarClienteConectado(String usuario, ManejadorCliente handler) {
        clientesConectados.put(usuario, handler);
        contadorClientes.incrementar();
        log("Usuario conectado: " + usuario);
        informarATodos("SERVIDOR: " + usuario + " se ha unido al chat", null);
    }
    
    public synchronized void clienteDesconectado(String usuario) {
        if (clientesConectados.remove(usuario) != null) {
        contadorClientes.decrementar();
        log("Usuario desconectado: " + usuario);
        informarATodos("SERVIDOR: " + usuario + " ha abandonado el chat", null);
    }
    }
    
    public synchronized void enviarMensajePublico(String usuario, String mensaje) {
        String mensajeCompleto = "PUBLICO [" + usuario + "]: " + mensaje;
        log("Mensaje público de " + usuario + ": " + mensaje);
        informarATodos(mensajeCompleto, usuario);
    }
    
    public synchronized void enviarMensajePrivado(String usuarioActual, String destinatario, String mensaje) {
        ManejadorCliente manejadorDestino = clientesConectados.get(destinatario);
        ManejadorCliente manejadorRemitente = clientesConectados.get(usuarioActual);
    
    if (manejadorDestino != null && manejadorRemitente != null) {
        String mensajePrivado = "PRIVADO de " + usuarioActual + ": " + mensaje;
        String mensajeConfirmacion = "PRIVADO para " + destinatario + ": " + mensaje;
        
        manejadorDestino.enviarMensaje(mensajePrivado);
        manejadorRemitente.enviarMensaje(mensajeConfirmacion);
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
