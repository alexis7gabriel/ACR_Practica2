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
    private static int TAMANO_VENTANA = 10; // Tamaño de la ventana predeterminado
    private static final int TIMEOUT = 3000; // 3 segundos de timeout
    public static void main(String[] args) {
    
        try {
            int pto = 1234;
            String dir = "127.0.0.1";
            InetAddress dst = InetAddress.getByName(dir);
            int x = 0;
          
            DatagramSocket cl = new DatagramSocket();
            
            while (true){
                // Selección de archivo
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Seleccione un archivo");
                int seleccion = fileChooser.showOpenDialog(null);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    FileInputStream fis = new FileInputStream(f);
                    // Enviar nombre del archivo como primer paquete
                    String nombreArchivo = f.getName();
                    byte[] tmp = nombreArchivo.getBytes();
                    DatagramPacket paqueteNombre = new DatagramPacket(tmp, tmp.length,
                            dst, pto);
                    cl.send(paqueteNombre);
                    int totalPaquetes = (int) Math.ceil((double) f.length() / TAMANO_VENTANA);
                    System.out.println(totalPaquetes);

                    int base = 0;
                    int siguienteSecuencia = 0;
                    while (base < totalPaquetes) {
                        // Envío de paquetes en la ventana actual
                        for (int i = base; i < Math.min(base + TAMANO_VENTANA, totalPaquetes); i++) {
                            byte[] bs = createPacketData(i, f, fis);
                            DatagramPacket paqueteEnvio = new DatagramPacket(bs, bs.length, dst, pto);
                            cl.send(paqueteEnvio);
                            //System.out.println("Enviado ACK para el paquete " + i);
                            //Thread.sleep(1000); // Espera 1 segundo
                            siguienteSecuencia++;
                        }

                        // Configuración del temporizador para el timeout
                        cl.setSoTimeout(3000);

                        // Recepción de acks o retransmisión en caso de timeout
                        for (int i = base; i < siguienteSecuencia; i++) {
                            try {
                                byte [] br = new byte [TAMANO_VENTANA];
                                DatagramPacket paqueteRecepcion = new DatagramPacket(br, br.length);
                                cl.receive(paqueteRecepcion);
                                String ack = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
                                int ackNum = Integer.parseInt(ack);
                                if (ackNum >= base) {
                                    base = ackNum + 1;
                                   // System.out.println("Recibido ACK para el paquete " + ackNum);
                                    //Thread.sleep(1000); // Espera 1 segundo
                                }
                            } catch (SocketTimeoutException e) {
                                // Timeout, retransmitir paquetes no confirmados
                                i = base; // Retroceder para retransmitir
                           }
                        }
                    } // while
                    fis.close();
                } else {
                    System.out.println("Fin");
                    break;
                }  
            } // while
            cl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }
    private static byte[] createPacketData(int sequenceNumber, File file, FileInputStream fis) throws IOException {
        int offset = sequenceNumber * TAMANO_VENTANA;
        System.out.println("offset" + offset + "\n");
        fis.skip(offset);
        int length = Math.min(fis.available(), TAMANO_VENTANA);
        byte[] packetData = new byte[TAMANO_VENTANA + 4];
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
