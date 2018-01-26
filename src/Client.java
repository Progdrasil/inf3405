// Source :
// Ray Toal
// Department of Electrical Engineering and Computer Science
// Loyola Marymount University
// http://cs.lmu.edu/~ray/notes/javanetexamples/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class Client {

    private BufferedImage in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Capitalize Client");
    private JTextField dataField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(8, 60);

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public Client() {

        // Layout GUI
        messageArea.setEditable(false);
        frame.getContentPane().add(dataField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");

        // Add Listeners
//        dataField.addActionListener(new ActionListener() {
//            /**
//             * Responds to pressing the enter key in the textfield
//             * by sending the contents of the text field to the
//             * server and displaying the response from the server
//             * in the text area.  If the response is "." we exit
//             * the whole application, which closes all sockets,
//             * streams and windows.
//             */
//            public void actionPerformed(ActionEvent e) {
//                out.println(dataField.getText());
//                   String response;
//                try {
//                    response = in.readLine();
//                    if (response == null || response.equals("")) {
//                          System.exit(0);
//                      }
//                } catch (IOException ex) {
//                       response = "Error: " + ex;
//                }
//                messageArea.append(response + "\n");
//                dataField.selectAll();
////            }
//        });
    }

    /**
     * Implements the connection logic by prompting the end user for
     * the server's IP address, connecting, setting up streams, and
     * consuming the welcome messages from the server.  The Capitalizer
     * protocol says that the server sends three lines of text to the
     * client immediately after establishing a connection.
     */
    @SuppressWarnings("resource")
	public void connectToServer() throws IOException {

        // Get the server address from a dialog box.
    	String serverAddress = "";
    	do {
            serverAddress = JOptionPane.showInputDialog(frame,"Enter IP Address of the Server:","Welcome to the Capitalization Program",JOptionPane.QUESTION_MESSAGE);
            if(!checkIp(serverAddress)) {
           	 JOptionPane.showMessageDialog(frame, "Ce que tu m'as donne n'est pas une addresse IP!", "Dialog",
            	        JOptionPane.ERROR_MESSAGE);
            }
    	} while(!checkIp(serverAddress));
        
        // Get port number and verify between 5000 5050
        int port = 0;
        do  {
        	 String portStr = JOptionPane.showInputDialog(frame,"Enter the port of the Server (5000-5050):","Welcome to the Capitalization Program",JOptionPane.QUESTION_MESSAGE);;
             port = Integer.parseInt(portStr);
             if(!checkPort(port)) {
            	 JOptionPane.showMessageDialog(frame, "Le port n'est pas entre 5000 et 5050", "Dialog",
             	        JOptionPane.ERROR_MESSAGE);
             }
        } while (!checkPort(port));
        
        // Get the photo photo
		JFileChooser j = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
	    j.setFileFilter(filter);
		Integer opt = j.showOpenDialog(frame);
		String pathName = "";
		try {
			if(opt == JFileChooser.APPROVE_OPTION) {
				pathName = j.getSelectedFile().getPath();
				System.out.print(pathName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

        
        Socket socket;
		socket = new Socket(serverAddress, (int)port);
		OutputStream outStream = socket.getOutputStream();
		
        System.out.format("The capitalization server is running on %s:%d%n", serverAddress, port);
        
        in = ImageIO.read(new File(pathName));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        
        ImageIO.write(in, "JPEG", byteArrayOutputStream);
        
        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        outStream.write(size);
        outStream.write(byteArrayOutputStream.toByteArray());
        outStream.flush();
//        out = new PrintWriter(socket.getOutputStream(), true);

        // Consume the initial welcoming messages from the server
//        for (int i = 0; i < 3; i++) {
//            messageArea.append(in.readLine() + "\n");
//        }
    }
    
    private boolean checkIp(String IpAddr) {
    	Pattern PATTERN = Pattern.compile(
    	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    	return PATTERN.matcher(IpAddr).matches();
    }
    
    private boolean checkPort(int port) {
    	return port >= 5000 && port <= 5050;
    }

    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(true);
        client.connectToServer();
    }
}
