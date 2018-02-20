// Source :
// Ray Toal
// Department of Electrical Engineering and Computer Science
// Loyola Marymount University
// http://cs.lmu.edu/~ray/notes/javanetexamples/

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A server program which accepts requests from clients to
 * capitalize strings.  When clients connect, a new thread is
 * started to handle an interactive dialog in which the client
 * sends in a string and the server thread sends back the
 * capitalized version of the string.
 * 
 * The program is runs in an infinite loop, so shutdown in platform
 * dependent.  If you ran it from a console window with the "java"
 * interpreter, Ctrl+C generally will shut it down.
 */
public class Server {

    /**
     * Application method to run the server runs in an infinite loop
     * listening on port 5000.  When a connection is requested, it
     * spawns a new thread to do the servicing and immediately returns
     * to listening.  The server keeps a unique client number for each
     * client that connects just to show interesting logging
     * messages.  It is certainly not necessary to do this.
     */
    public static void main(String[] args) throws Exception {
       
        int clientNumber = 0;
        
        System.out.println("Welcome to the Server");
        System.out.println("Enter IP Address of the Server: ");
        
        // Get the server address from a dialog box
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String serverAddress = br.readLine();
        int port = 5000;
        
		ServerSocket listener;
		InetAddress locIP = InetAddress.getByName(serverAddress);
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(locIP, port));
		
        System.out.format("The server is running on %s:%d%n", serverAddress, port);
    
        try {
            while (true) {
                new Capitalizer(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }


    /**
     * A private thread to handle capitalization requests on a particular
     * socket.  The client terminates the dialogue by sending a single line
     * containing only a period.
     */
    private static class Capitalizer extends Thread {
        private Socket socket;
        private int clientNumber;

        public Capitalizer(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            String user;

            try {
                PrintWriter result = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader login = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                user = login.readLine();
                String pass = login.readLine();

                csv db = new csv();
                boolean success = db.login(user, pass);
                if (!success) {
                    result.println("Erreur dans la saisie du mot de passe");
                    socket.close();
                    return;
                } else {
                    result.println("Welcome " + user);
                }
            } catch (IOException e) {
                log(e.toString());
                try {
                    socket.close();
                } catch (IOException ee) {
                    log("Couldn't close a socket, what's going on?");
                }
                return;
            }

            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
                BufferedReader imageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String imageName = imageReader.readLine();

                System.out.format( "[%s - %s:%d - %s] : Image %s recue pour taitement%n", user,
                        socket.getInetAddress().toString(), socket.getPort(),
                        new Timestamp(System.currentTimeMillis()).toString(), imageName);

            	InputStream inputStream = socket.getInputStream();
				OutputStream outStream = socket.getOutputStream();
            	
            	byte[] sizeArr = new byte[4];
            	inputStream.read(sizeArr);
            	int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
            	
            	byte[] imageArr = new byte[size];
            	inputStream.read(imageArr);
            	
                BufferedImage in = ImageIO.read(new ByteArrayInputStream(imageArr));
                BufferedImage sobel = Sobel.process(in);

				// retourne image traiter
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				// Transforme data de l'image a un stream
				ImageIO.write(sobel, "JPEG", byteArrayOutputStream);

				// Envoie data stream a travers socket
				byte[] outSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				outStream.write(outSize);
				outStream.write(byteArrayOutputStream.toByteArray());
				outStream.flush();

				outStream.close();
				inputStream.close();

            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
    }
}
