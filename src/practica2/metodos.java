/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.*;
import net.lingala.zip4j.exception.ZipException;
/**
 *
 * @author cesar
 */
public class metodos {

    private String raiz;
    private String nombre;
    private String comando;
    private boolean local;

    metodos() {
        raiz = "";
        nombre = "";
        comando = "";
        local = false;
    }

    public static void listaArchivos(File f2, List<String> listaNombres, String aux) {
        File[] listaFiles = f2.listFiles();
        String aux2 = aux;
        boolean aux3 = true;
        if (listaNombres.isEmpty()) {
            System.out.println(aux2 + f2.getName() + "  <- raiz local");
        } else {
            System.out.println(aux2 + f2.getName());
        }
        listaNombres.add(aux2 + f2.getName());
        for (File file : listaFiles) {

            if (file.isDirectory()) {

                if (aux3) {
                    aux3 = false;
                    aux = aux + "\t";
                }

                listaArchivos(file, listaNombres, aux);
            } else {
                System.out.println(aux2 + "\t" + file.getName());
                listaNombres.add(aux2 + "\t" + file.getName());
            }

        }
    }
    //funcion que envia del servidor a cliente el listado de archivos del directorio
    public static void enviar(DataOutputStream dos, List<String> listaNombres) {
        try {
            //orientado a byte
            //System.out.println("Cliente conectado desde " + cl.getInetAddress() + ":" + cl.getPort());
            dos.writeInt(listaNombres.size());
            for (String element : listaNombres) {
                dos.writeUTF(element);

            }
            dos.flush();

            /*ObjectOutputStream oos2 = new ObjectOutputStream(cl.getOutputStream());
            oos2.writeObject(listaNombres);
            oos2.flush();*/
            //System.out.println("Cliente conectado.. Enviando objeto");
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }
    //fucion que envia archivos que primero se comprimieron
    public static void enviar(DataOutputStream dos, String ruta, String nombre) {
        try {

            dos.writeUTF(nombre);
            if (!nombre.equals("error")) {
                DataInputStream dis = new DataInputStream(new FileInputStream(ruta));
                int l = 0, porcentaje;
                long enviados = 0;
                File f = new File(ruta);
                long tam = f.length();
                dos.writeLong(tam);
                while (enviados < tam) {
                    byte[] buffer = new byte[1500];
                    l = dis.read(buffer);
                    dos.write(buffer, 0, l);
                    dos.flush();
                    enviados = enviados + l;
                    porcentaje = (int) ((enviados * 100) / tam);
                    System.out.println("\rEnviado el " + porcentaje + " % del archivo");
                }
                dis.close();
                File zip = new File(ruta);
                zip.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    public static void recibir(DataInputStream dis, String ruta) {
        try {
            //DataInputStream dis = new DataInputStream(cl.getInputStream());
            int l = 0, porcentaje;
            long recibidos = 0;

            String nombre = dis.readUTF();
            if (nombre.equals("error")) {
                System.out.println("Directorio o archivo inexistente");
            } else {
                long tam = dis.readLong();
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(ruta + "\\" + nombre + ".zip"));
                while (recibidos < tam) {
                    byte[] buffer = new byte[1500];
                    l = dis.read(buffer);
                    dos.write(buffer, 0, l);
                    dos.flush();
                    recibidos = recibidos + l;
                    porcentaje = (int) ((recibidos * 100) / tam);
                    System.out.println("\rRecibiendo el " + porcentaje + " % del archivo");
                }
                dos.close();
                unzip(ruta);//se descomprime el archivo
                File zip = new File(ruta + "\\" + nombre + ".zip");

                zip.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    public static File crearCarpeta(String raiz, String carpeta) {
        String ruta_archivos = raiz + "\\" + carpeta + "\\";
        System.out.println("ruta:" + ruta_archivos);
        File f2 = new File(ruta_archivos);
        f2.mkdirs();
        f2.setWritable(true);
        return f2;
    }

    public static String cambiarCarpeta(String raiz, String carpeta) {
        if (carpeta.equals("..")) {
            String ruta_archivos = raiz;
            File f2 = new File(ruta_archivos);
            return f2.getParent();
        }
        String ruta_archivos = raiz + "\\" + carpeta + "\\";
        System.out.println("ruta:" + ruta_archivos);
        File f2 = new File(ruta_archivos);
        if (!f2.exists()) {
            System.out.println("directorio no valido");
            return raiz;
        }
        /*if (f2.isDirectory()) {
            System.out.println("es directorio");
        }*/
        return ruta_archivos;
    }

    /*public static File cambiarCarpeta(String carpeta) {
        File f = new File("");
        String ruta = f.getAbsolutePath();
        File f2 = new File(ruta);
        String parent=f2.getParent();
        //String carpeta = "Archivos remotos";
        
        String ruta_archivos = parent + "\\" + carpeta + "\\";
        System.out.println("ruta:" + ruta_archivos);
        File f3 = new File(ruta_archivos);
        return f3;
    }*/
    public static void eliminar(File archivo) {
        if (archivo.isDirectory()) {
            File[] listaFiles = archivo.listFiles();
            //System.out.println(aux2 + f2.getName());
            //listaNombres.add(aux2 + f2.getName());
            for (File file : listaFiles) {

                if (file.isDirectory()) {

                    eliminar(file);
                } else {

                    file.delete();
                }

            }
        }
        archivo.delete();//se eliminar el directorio hasta que este vacio
    }

    public void comandos(String linea) {
        //envio de comandos
        if (linea.startsWith("list")) {
            if (linea.trim().length() == 4) {
                comando = linea.trim();
            }
        } else if (linea.startsWith("mkdir")) {
            comando = linea.substring(0, 5);
            linea = linea.substring(6, linea.length());
            if (linea.startsWith("local")) {
                nombre = linea.substring(6);
                local = true;
            } else if (linea.startsWith("remoto")) {
                nombre = linea.substring(7);
            } else {
                comando = "";
            }
        } else if (linea.startsWith("rmdir")) {
            comando = linea.substring(0, 5);
            linea = linea.substring(6, linea.length());
            if (linea.startsWith("local")) {
                nombre = linea.substring(6);
                local = true;
            } else if (linea.startsWith("remoto")) {
                nombre = linea.substring(7);
            } else {
                comando = "";
            }

        } else if (linea.startsWith("cd")) {
            comando = linea.substring(0, 2);
            linea = linea.substring(3, linea.length());
            if (linea.startsWith("local")) {
                nombre = linea.substring(6);
                local = true;
            } else if (linea.startsWith("remoto")) {
                nombre = linea.substring(7);
            } else {
                comando = "";
            }
        } else if (linea.startsWith("put")) {
            comando = linea.substring(0, 3);
            nombre = linea.substring(4);
        } else if (linea.startsWith("get")) {
            comando = linea.substring(0, 3);
            nombre = linea.substring(4);
        } else if (linea.startsWith("quit")) {
            comando = linea.substring(0, 4);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public boolean getLocal() {
        return local;
    }

    public String getComando() {
        return comando;
    }

    /*public String getRaiz() {
        return raiz;
    }
    public void setRaiz(String r) {
        raiz=r;
    }*/
    //fucion que recibe la lista de archivos del directorio remoto
    //list es orientado a byte, pero envia string por string
    public static void mostrarArchivos(DataInputStream dis) {
        try {
            //lista Remotos
            List<String> listaArchivosRemoto = new ArrayList<String>();
            //System.out.println("Comienza descarga de la lista ");
            //DataInputStream dis = new DataInputStream(cl.getInputStream());
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                String nombre = dis.readUTF();
                listaArchivosRemoto.add(nombre);
                if (i == 0) {
                    System.out.println(nombre + "  <- raiz remota");
                } else {
                    System.out.println(nombre);
                }
            }
            System.out.println("---------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    public static void zip(String raiz, String nombre, DataOutputStream dos) {
        try {
            String ruta_archivos = raiz + "\\" + nombre + "\\";
            System.out.println("ruta:" + ruta_archivos);
            File file = new File(ruta_archivos);
            if (file.exists()) {
                if (file.isDirectory()) {
                    new ZipFile(raiz + "\\" + "temporal.zip").addFolder(file);
                } else {
                    new ZipFile(raiz + "\\" + "temporal.zip").addFile(file);
                }
                enviar(dos, raiz + "\\" + "temporal.zip", "temporal");
            } else {
                System.out.println("Directorio o archivo inexistente");
                enviar(dos, "", "error");
            }

        } catch (ZipException e) {
            e.printStackTrace();
        }

    }

    public static void unzip(String raiz) {
        try {

            System.out.println("ruta:" + raiz);
            new ZipFile(raiz + "\\temporal.zip").extractAll(raiz);

        } catch (ZipException e) {
            e.printStackTrace();
        }

    }
}
