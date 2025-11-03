/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kevin
 */
public class ListaMensajes {
    
    private List<String> mensajes;

    public ListaMensajes() {
        this.mensajes = new ArrayList<>();
    }

    public synchronized void agregarMensajePublico(String usuario, String mensaje) {
        String mensajeCompleto = "PUBLICO - " + usuario + ": " + mensaje;
        mensajes.add(mensajeCompleto);
    }

    public synchronized void agregarMensajePrivado(String usuario, String destinatario, String mensaje) {
        String mensajeCompleto = "PRIVADO - " + usuario + " → " + destinatario + ": " + mensaje;
        mensajes.add(mensajeCompleto);
    }

    public List<String> getMensajes() {
        return new ArrayList<>(mensajes);
    }

    public int getTotalMensajes() {
        return mensajes.size();
    }

}
