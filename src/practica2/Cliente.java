/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;

/**
 *
 * @author cesar
 */
public class Cliente {
    private static int TAMANO_VENTANA = 5; // Tamaño de la ventana predeterminado
    private static final int TIMEOUT = 3000; // 3 segundos de timeout
    public static void main(String[] args) {
    
        try {
            int pto = 1234;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            int x = 0;
            byte[] br = new byte [1028];
            byte[] bs = new byte [1028];
          
            DatagramSocket cl = new DatagramSocket();
            System.out.println("size:" + cl.getSendBufferSize() + "\n");
            cl.setReuseAddress(true);
            
            while (true){
                // Selección de archivo
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Seleccione un archivo");
                int seleccion = fileChooser.showOpenDialog(null);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File archivoSeleccionado = fileChooser.getSelectedFile();
                    FileInputStream fis = new FileInputStream(archivoSeleccionado);

                    // Envío del nombre del archivo como primer paquete
                    String nombreArchivo = archivoSeleccionado.getName();
                    DatagramPacket paqueteNombre = new DatagramPacket(nombreArchivo.getBytes(), nombreArchivo.getBytes().length,
                            dst, pto);
                    cl.send(paqueteNombre);

                    int totalPaquetes = (int) Math.ceil((double) archivoSeleccionado.length() / bs.length);
                    
                    System.out.println("total paquetes" + totalPaquetes + "\n");

                    int base = 0;
                    int siguienteSecuencia = 0;

                    while (siguienteSecuencia < totalPaquetes) {
                        // Envío de paquetes en la ventana actual
                        for (int i = base; i < Math.min(base + TAMANO_VENTANA, totalPaquetes); i++) {
                            bs = createPacketData(i, archivoSeleccionado, fis, totalPaquetes);
                            DatagramPacket paqueteEnvio = new DatagramPacket(bs, bs.length, dst, pto);
                            cl.send(paqueteEnvio);
                            siguienteSecuencia++;
                        }

                        // Configuración del temporizador para el timeout
                        cl.setSoTimeout(TIMEOUT);

                        // Recepción de acks o retransmisión en caso de timeout
                        for (int i = base; i < siguienteSecuencia; i++) {
                            try {
                                DatagramPacket paqueteRecepcion = new DatagramPacket(br, br.length);
                                cl.receive(paqueteRecepcion);
                                String ack = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
                                System.out.println("se recibio el ack: " + ack + "\n");
                                int ackNum = Integer.parseInt(ack);
                                if (ackNum >= base) {
                                    base = ackNum;
                                }
                                System.out.println("base: " + base + "\n");
                            } catch (SocketTimeoutException e) {
                                // Timeout, retransmitir paquetes no confirmados
                                System.out.println("entro en retroceder n \n");
                                i = base - 1; // Retroceder para retransmitir
                            }
                        }
                    }
                    System.out.println("Archivo pasado con exito\n");
                } else {
                    // El cliente decide no enviar más archivos
                    //fis.close();
                    break;
                }
            } // while
            cl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }
    private static byte[] createPacketData(int sequenceNumber, File file, FileInputStream fis, int total) throws IOException {
        int offset = (sequenceNumber) * 1024;
        System.out.println("salto:" + offset + "\n");
        //if(offset != 0) fis.skip(1024);
        //System.out.println("disponible:" + fis.available() + "\n");
        int length = 0;
        if(((sequenceNumber+1)*1024) < file.length()){
            length = 1024;
        } else {
            length = (int) file.length() % 1024;
        }
        System.out.println("tamm paquete a enviar:" + length + "\n");
        byte[] packetData = new byte[length + 4];
        byte[] sequenceBytes = intToBytes(sequenceNumber);
        System.arraycopy(sequenceBytes, 0, packetData, 0, 4);
        fis.read(packetData, 4, length);
        return packetData;
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
