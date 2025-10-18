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

public class Servidor {

    //atributos para la conexion
    private int puerto = 5000;
    private Contador contadorClientes;
    private int max_clientes = 3;
    private ServerSocket serverSocket;
    
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
            
            while (true) {
                Socket socket = serverSocket.accept();
                
                if (contadorClientes.getContador() < max_clientes) {
                    ManejadorCliente manejador = new ManejadorCliente(socket, this);
                    new Thread(manejador).start();
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
    public synchronized void registrarClienteConectado(String usuario, ManejadorCliente manejador) {
        clientesConectados.put(usuario, manejador);
        contadorClientes.incrementar();
        log("Usuario conectado: " + usuario);
        manejador.enviarMensaje("CONEXION_EXITOSA");
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
