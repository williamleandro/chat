package servidor;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;

import mensaje.Mensaje;

public class HiloServidor extends Thread {
	
    private Socket socket;
    private Map <String, ArrayList<Socket>> map;
    private String nombreSala;
    
    public HiloServidor(Socket socket, Map <String, ArrayList<Socket>> map, String nombreSala) {
        super("ThreadServer");
        this.socket = socket;
        this.map = map;
        this.nombreSala=nombreSala;
    }


    @SuppressWarnings("deprecation")
	public void run() {

        DataInputStream data;
        Iterator<Socket> iterador;
        String mensaje = null;

        try {
            do {
                if (mensaje != null) {
                	
                	final Gson gson = new Gson();
                    final Mensaje mensajeResivido = gson.fromJson(mensaje, Mensaje.class);
                	
                    System.out.println(mensajeResivido.getHora().getTime().toString().substring(11,16) + "- " + mensajeResivido.getEmisor() + " dijo: " + mensajeResivido.getMensaje());
                    iterador = map.get(nombreSala).iterator(); //creo un interador de los clientes.

                    while (iterador.hasNext()) {
                        Socket cliente = iterador.next(); //le pido un cliente de la coleccion.
                        try {

                            // si el socket extraido es distinto al socket del
                            // hilo
                            // se enviara el msg a todos los usuarios de la
                            // coleccion menos el que envio dicho msg.
                            if (!cliente.equals(socket)) {
                                PrintStream ps = new PrintStream(
                                        cliente.getOutputStream());                              
                                
                                ps.println(mensaje);// envia el mensaje al
                                                // correspondiente socket.
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // indico que el flujo de informacion provenga del usuario de
                // este hilo.
                data = new DataInputStream(socket.getInputStream());

            } while ((mensaje = data.readLine()) != null);

            Servidor.cantActualClientes--;
            map.get(nombreSala).remove(socket);
            System.out.println("Un cliente se ha desconectado.");
        } catch (IOException e) {
            try {
                Servidor.cantActualClientes--;
                map.get(nombreSala).remove(socket);	//Soluciona problema de parar servidor.
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("La conexion ha finalizado.");
        }
    }
}
