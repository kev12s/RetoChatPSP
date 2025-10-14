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
        
         try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);
            log("Servidor iniciado");
        } catch (IOException e) {
            System.err.println("Error al iniciar servidor: " + e.getMessage());
        }
    }

    public void iniciar() {
        try {
            System.out.println("Servidor iniciado. Esperando clientes...");

            while (true) {
                Socket cliente = serverSocket.accept();
                int idConexiones = contadorClientes.incrementar();

                if (idConexiones <= max_clientes) {
                    ManejadorCliente manejador = new ManejadorCliente(
                            cliente, idConexiones, contadorClientes
                    );
                    new Thread(manejador).start();
                } else {
                    cliente.close();
                    break;
                }
            }

            System.out.println("Se ha alcanzado el máximo de " + max_clientes + " clientes.");

        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
    
    //ESCRIBIR LOS LOGS EN UN FICHERO?!?!?!?!
     private synchronized void log(String mensaje) {
        String logGuardado = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + mensaje;
        logMensajes.add(logGuardado);
        
        //SERIA ESCRIBIR EN UN FICHERO EN VEZ DE MOSTRAR POR CONSOLA
        System.out.println(logGuardado);
    }
     
     

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
