// Source :
// Ray Toal
// Department of Electrical Engineering and Computer Science
// Loyola Marymount University
// http://cs.lmu.edu/~ray/notes/javanetexamples/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class Client {

    private BufferedImage in;
    private BufferedImage out;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public Client() {
        System.out.println("Welcome to the Client");
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
    	String serverAddress;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	do {
    	    System.out.println("Enter IP address of the Server: ");
            serverAddress = br.readLine();
            if(!isIp(serverAddress)) {
                System.out.println("What you gave me is not an IP address! Please try again");
            }
    	} while(!isIp(serverAddress));
        
        // Get port number and verify between 5000 5050
        int port;
        do  {
            System.out.println("Enter the port of the Server (5000-5050):");
            String portStr = br.readLine();
            port = Integer.parseInt(portStr);
            if(!isPort(port)) {
                System.out.println("The port is not between 5000 and 5050");
            }
        } while (!isPort(port));

		// Create a socket
        Socket socket;
		socket = new Socket(serverAddress, port);
		OutputStream outStream = socket.getOutputStream();
		InputStream inStream = socket.getInputStream();

		System.out.format("%nThe Server is running on %s:%d%n", serverAddress, port);

		// Get the photo path
		Path filePath = null;
		System.out.println("Enter the path to the image you want to send: ");
		try {
			filePath = Paths.get(br.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Open the image
		File img = null;
		try {
			img = filePath.toFile();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}

        // Read original image
        out = ImageIO.read(img);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        // Transform image data to stream
        ImageIO.write(out, "JPEG", byteArrayOutputStream);

        // Send image stream through socket
        byte[] outSize = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        outStream.write(outSize);
        outStream.write(byteArrayOutputStream.toByteArray());
        outStream.flush();
        System.out.println("The image has been sent to the server, awaiting response ...");

        try {
			// recois taille de l'image traiter
			byte[] sizeArr = new byte[4];
			inStream.read(sizeArr);
			int inSize = ByteBuffer.wrap(sizeArr).asIntBuffer().get();

			// recois image traiter
			byte[] imageArr = new byte[inSize];
			inStream.read(imageArr);

			in = ImageIO.read(new ByteArrayInputStream(imageArr));
		}
		catch (IOException e) {
        	e.printStackTrace();
		}

		// ajout de -sobel au nom du fichier
		String[] fileName = filePath.getFileName().toString().split(".jpg");
		StringBuilder sb = new StringBuilder();
		sb.append(fileName[0]);
		sb.append("-sobel");
		sb.append(".jpg");
		Path outPath = Paths.get(filePath.getParent().toString(), sb.toString());

		// sauvegarde le fichier
		ImageIO.write(in, "JPEG", new File(outPath.toString()));
		System.out.format("The new image is saved at: %s%n", outPath.toString());

		// ferme les stream et le socket
		inStream.close();
		outStream.close();
		socket.close();
    }
    
    private boolean isIp(String IpAddr) {
    	Pattern PATTERN = Pattern.compile(
    	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    	return PATTERN.matcher(IpAddr).matches();
    }
    
    private boolean isPort(int port) {
    	return port >= 5000 && port <= 5050;
    }

    /**
     * Runs the client application.
     */
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.connectToServer();
    }
}
