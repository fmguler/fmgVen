/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package portforwarder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fmguler
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //arg0 -L or -R
        //arg1 port
        //arg2 host
        //arg3 hostport

        if (args.length == 0) {
            System.out.println("Requires arguments");
            return;
        }

        if (args[0].equals("-L")) {

            try {
                int port = Integer.parseInt(args[1]);
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket ourclient = serverSocket.accept();
                    //yeni baðlantý oldu, biz de baðlanalým
                    String host = args[2];
                    int hostPort = Integer.parseInt(args[3]);
                    Socket toHost = new Socket(host, hostPort);

                    pipe(ourclient.getInputStream(), toHost.getOutputStream(), toHost.getInputStream(), ourclient.getOutputStream());
                }

            } catch (IOException ex) {
                System.out.println("Error forwarding");
                ex.printStackTrace();
            }

        } else if (args[0].equals("-R")) {
        } else {
            System.out.println("Invalid switch");
        }
    }

    public static void pipe(final InputStream is1, final OutputStream os1, final InputStream is2, final OutputStream os2) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is1.read(buf)) != -1) {
                        os1.write(buf, 0, c);
                    }
                    System.out.println("one pipe finished-1");
                } catch (IOException ex) {
                    System.out.println("Error piping is1 to os2");
                    ex.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buf = new byte[1024];
                    int c = 0;
                    while ((c = is2.read(buf)) != -1) {
                        os2.write(buf, 0, c);
                    }
                    System.out.println("one pipe finished-2");
                } catch (IOException ex) {
                    System.out.println("Error piping is1 to os2");
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}
