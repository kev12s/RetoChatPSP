/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kevin
 */
public class ListaClientes {

    private Map<String, HiloCliente> clientesConectados;

    public ListaClientes() {
        this.clientesConectados = new HashMap<>();
    }

    public synchronized boolean registrarClienteConectado(String usuario, HiloCliente hilo) {
        boolean insertado = false;
        
        if (!clientesConectados.containsKey(usuario)) {
            clientesConectados.put(usuario, hilo);
            insertado = true;
        }
        return insertado;
    }

    public synchronized boolean clienteDesconectado(String usuario) {
        boolean borrado = false;

        if (clientesConectados.containsKey(usuario)) {
            clientesConectados.remove(usuario);
            borrado = true;
        }

        return borrado;
    }

    public Map<String, HiloCliente> getClientesConectados() {
        return clientesConectados;
    }

    public int getContadorClientesConectados() {
        return clientesConectados.size();
    }
    
    public boolean existeUsuario(String usuario) {
        return clientesConectados.containsKey(usuario);
    }
}
