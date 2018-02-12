import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class csv {

	public static void main(String[] args) {

        String csvFile = "test.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

    String[] country = null;
    JFrame frame = new JFrame("Capitalize Client");
    String user = JOptionPane.showInputDialog(frame,"Username","Connexion",JOptionPane.QUESTION_MESSAGE);;
	String pass = JOptionPane.showInputDialog(frame,"Password","Connexion",JOptionPane.QUESTION_MESSAGE);;
	int pos = 0;

    try {
    	if(new File(csvFile).exists()){
    		FileWriter writer = new FileWriter(csvFile,true);
        br = new BufferedReader(new FileReader(csvFile));
        while ((line = br.readLine()) != null)
        	 country = line.split(cvsSplitBy);
         pos = Contains(country, user);
		 if(pos == -1){
			 StringBuilder sb = new StringBuilder();
 			sb.append(user);
 		    sb.append(",");
 		    sb.append(pass);
 		    sb.append('\n');
	        writer.write(sb.toString());
	        writer.close(); 
		 }
		 else{
			 String text = 	country[pos+1].equals(pass) ? "Bienvenue "+ country[pos]:"LE mot de passe n'est pas valide!" ;
			 System.out.println(text);
		 }
		 }   	
        else{
		   	 FileWriter writer = new FileWriter(csvFile,true);
        	        StringBuilder sb = new StringBuilder();
        			sb.append(user);
        		    sb.append(",");
        		    sb.append(pass);
        		    sb.append('\n');
        	        writer.write(sb.toString());
        	        writer.close();        		 
        	 }
         

        
       
        
	    System.out.println("DONE  ");
	} 
    catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        

}
	
static int Contains(String[] data, String user){
		for (int i = 0; i<data.length; i = i+2){
			if (data[i].equals(user))
				return i;
			}
			return -1;
}
}


