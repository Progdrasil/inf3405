// Source :
// Ray Toal
// Department of Electrical Engineering and Computer Science
// Loyola Marymount University
// http://cs.lmu.edu/~ray/notes/javanetexamples/

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
        
        JFrame frame = new JFrame("Capitalize Server");
        
        // Get the server address from a dialog box.
        String serverAddress = JOptionPane.showInputDialog(frame,"Enter IP Address of the Server:","Welcome to the Capitalization Program", JOptionPane.QUESTION_MESSAGE);
        int port = 5000;
        
		ServerSocket listener;
		InetAddress locIP = InetAddress.getByName(serverAddress);
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(locIP, port));
		
        System.out.format("The capitalization server is running on %s:%d%n", serverAddress, port);
    
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
            log("New connection with client# " + clientNumber + " at " + socket);
        }

        /**
         * Services this thread's client by first sending the
         * client a welcome message then repeatedly reading strings
         * and sending back the capitalized version of the string.
         */
        public void run() {
            try {

                // Decorate the streams so we can send characters
                // and not just bytes.  Ensure output is flushed
                // after every newline.
            	InputStream inputStream = socket.getInputStream();
            	
            	byte[] sizeArr = new byte[4];
            	inputStream.read(sizeArr);
            	int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
            	
            	byte[] imageArr = new byte[size];
            	inputStream.read(imageArr);
            	
                BufferedImage in = ImageIO.read(new ByteArrayInputStream(imageArr));
                BufferedImage sobel = Sobel.process(in);
                
                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new TestPane(sobel));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Send a welcome message to the client.
                out.println("Hello, you are client #" + clientNumber + ".");
                out.println("Enter a line with only a period to quit\n");

            } catch (IOException e) {
                log("Error handling client# " + clientNumber + ": " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Couldn't close a socket, what's going on?");
                }
                log("Connection with client# " + clientNumber + " closed");
            }
        }

        /**
         * Logs a simple message.  In this case we just write the
         * message to the server applications standard output.
         */
        private void log(String message) {
            System.out.println(message);
        }
        
        public class TestPane extends JPanel {

            private BufferedImage img = null;

            public TestPane(BufferedImage image) {
                img = image;
            }

            @Override
            public Dimension getPreferredSize() {
                return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
            }

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                if (img != null) {
                    g2d.drawImage(img, 0, 0, this);
                }
                g2d.dispose();
            }

        }
    }
}
