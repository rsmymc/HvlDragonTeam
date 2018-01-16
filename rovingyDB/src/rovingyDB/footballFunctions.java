package rovingyDB;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class footballFunctions {

	MySQLDBMobile dbMobile = new MySQLDBMobile();
	
		
	public void readDataFromText() throws IOException{

		dbMobile.connectToDB(dbType.KITAPP);

		BufferedReader br = new BufferedReader(
				new FileReader("C:\\Users\\hp p\\workspace\\repliklerDatabase\\src\\repliklerDatabase\\fot.txt"));

		String line = br.readLine();

		while (line != null) {

			try {
				insertToFootballTable(line);
			} catch (Exception e) {

			}
			line = br.readLine();
		}

		br.close();
		dbMobile.conClose();
	}
		
//____________________________________________________________________________________
	
	
	public void insertToFootballTable(String name)  {

			dbMobile.executeUpdate("insert into FotMaster (Text) values ('"
					+ name + "')");
		
	}
	
	
	public Boolean isAdded(int ID,String table)  {
		
		ResultSet rs =dbMobile.executeQuery("SELECT ID FROM "+table+" Where ID=" + ID);

		try {
			return rs.last();
		} catch (SQLException e) {
			return false;
		}
	}

	public String getHTML(String urlToRead)  {
		StringBuilder result = new StringBuilder();
		BufferedReader rd;
		try {
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),	"UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			
		}
		
		
		return result.toString();
	}

	public void savePoster(String urlposter, int ID){
		
		try {
			URL url = new URL(urlposter.replace("SX300", "SX750"));
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();

			FileOutputStream fos = new FileOutputStream("D://" + ID + ".jpg");
			fos.write(response);
			fos.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	
}
