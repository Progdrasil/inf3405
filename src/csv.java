import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class csv {
	public String file = "users.csv";
	public String delimiter = ",";

	public boolean login(String user, String pass) {

		File csvFile = null;
		String line = "";
		String[] range = null;
		int pos = 0;
		boolean	exists = false;

		// return values
		boolean status = false;
		String text = "";

		try {
			if((csvFile = new File(file)).exists()){
				Scanner sc = new Scanner(csvFile);
				sc.useDelimiter(delimiter);
				while (sc.hasNextLine() && !exists) {
					line = sc.nextLine();
					range = line.split(delimiter);
					pos = Contains(range, user);
					if (pos != -1) {
						exists = true;
						break;
					}
				}

				if (!exists) {
					addNewUser(user, pass);
					status = true;
				}
				else if (range[pos + 1].equals(pass)) {
					text = "Bienvenue " + range[pos];
					status = true;
				} else {
					text = "Le mot de passe n'est pas valide!";
					status = false;
				}
			}
			else{
				addNewUser(user, pass);
				status = true;
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = false;
		}

		// print message and return success status
		System.out.println(text);
		return status;
	}

	private void addNewUser(String user, String pass) {
		try {
			FileWriter writer = new FileWriter(file,true);
			StringBuilder sb = new StringBuilder();
			sb.append(user);
			sb.append(delimiter);
			sb.append(pass);
			sb.append('\n');
			writer.write(sb.toString());
			writer.close();
		}
		catch (IOException e) {
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


