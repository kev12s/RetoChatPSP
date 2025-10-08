/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

public class Contador {
   private int valor = 0;

    public Contador() {}

    public synchronized int incrementar() {
        valor++;
        return valor;
    }

    public synchronized int decrementar() {
        valor--;
        return valor;
    }

    public synchronized int getContador() {
        return valor;
    } 
}
