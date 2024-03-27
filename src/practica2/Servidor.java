/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2;

import java.io.*;
import java.net.*;

/**
 *
 * @author cesar
 */
public class Servidor {

    public static void main(String[] args) throws InterruptedException {
        try {
            int pto=1234;
            DatagramSocket socketServidor = new DatagramSocket(pto);
            //byte[] bufferRecepcion = new byte[65535];
            socketServidor.setReuseAddress(true);
            //obtencion de ruta de la raiz
            File f = new File("");
            String ruta = f.getAbsolutePath();
            System.out.println("ruta actual" + ruta);
            
            System.out.println("Servidor iniciado... esperando datagramas..");
            while (true){
                // Recepción del nombre del archivo
                byte[] bufferNombre = new byte[65535];
                DatagramPacket paqueteNombreArchivo = new DatagramPacket(bufferNombre, bufferNombre.length);
                socketServidor.receive(paqueteNombreArchivo);
                String nombreArchivo = new String(paqueteNombreArchivo.getData(), 0, paqueteNombreArchivo.getLength());
                System.out.println("Nombre del archivo recibido: " + nombreArchivo);
                // Creación del archivo de salida
                FileOutputStream fos = new FileOutputStream(ruta + "/Archivos remotos/" + nombreArchivo);
                while (true) {
                    byte[] bufferRecepcion = new byte[65535];
                    DatagramPacket paqueteRecepcion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
                    socketServidor.receive(paqueteRecepcion);
                    System.out.println("ack: " + extractSequenceNumber(paqueteRecepcion.getData()) + "\n");
                    // Procesamiento del paquete
                    processPacket(paqueteRecepcion, fos);

                    // Envío de ACK
                    sendAck(paqueteRecepcion, socketServidor);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processPacket(DatagramPacket packet, FileOutputStream fos) throws IOException {
        // Obtener datos del paquete
        byte[] data = packet.getData();
        int length = packet.getLength();
        System.out.println("tamaño: " + length + "\n");
        // Escribir datos al archivo
        fos.write(data, 4, length - 4); // Excluye los primeros 4 bytes (número de secuencia)
    }

    private static void sendAck(DatagramPacket packet, DatagramSocket socket) throws IOException, InterruptedException {
        // Extracción del número de secuencia del paquete recibido
        int seqNum = extractSequenceNumber(packet.getData());

        // Envío de ACK
        String ack = String.valueOf(seqNum);
        byte[] bufferAck = ack.getBytes();
        DatagramPacket paqueteAck = new DatagramPacket(bufferAck, bufferAck.length,
                packet.getAddress(), packet.getPort());
        socket.send(paqueteAck);
        //System.out.println("Enviado ACK para paquete con secuencia: " + seqNum);
       // Thread.sleep(1000); // Espera 1 segundo antes de continuar
    }

    private static int extractSequenceNumber(byte[] packetData) {
        return ((packetData[0] & 0xFF) << 24) |
                ((packetData[1] & 0xFF) << 16) |
                ((packetData[2] & 0xFF) << 8) |
                (packetData[3] & 0xFF);
    }
}
