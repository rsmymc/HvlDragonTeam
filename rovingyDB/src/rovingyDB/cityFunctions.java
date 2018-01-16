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

public class cityFunctions {

	MySQLDBMobile dbMobile = new MySQLDBMobile();
	
	private static String MAIN_URL="http://www.sehirler.net/yabancisehirresimleri.php";
	
	public void readData() throws IOException{

		dbMobile.connectToDB(dbType.KITAPP);

	
		try {
			getData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		dbMobile.conClose();
	}

	

	
//____________________________________________________________________________________
	
	
	private String getCountryURL(String content){
		
		String identifier="http://www.sehirler.net/kategori-yabanci-sehirler-83";
		
		int index=content.indexOf(identifier);
		
		if(index==-1)
			return null;
		
		String url=content.substring(index,content.indexOf("\" title",index));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
private String getCountryName(String content){
		
		String identifier="http://www.sehirler.net/kategori-yabanci-sehirler-83-";
		
		int index=content.indexOf(identifier)+identifier.length();
		
		String url=content.substring(index,content.indexOf("-",index));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	private String getCityImage(String content){
		
		String identifier="/data/thumbnails";
		
		int index=content.indexOf(identifier);
		
		String url=content.substring(index,content.indexOf("\" border",index));
		
		url=url.replace("\"", "").trim().replace("thumbnails", "media");
		
		url = "http://www.sehirler.net"+url;
		
		return url;
	}	
	
	private String getCityName(String content){
		
		String identifier="/data/thumbnails";
		
		int beginindex=content.indexOf(identifier);
		
		int index=content.indexOf("alt=\"",beginindex);
		
		String url=content.substring(index+"alt=\"".length(),content.indexOf("\" /></a>",beginindex));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	
	
	public void getData() throws Exception {

		String mainPageContent = getHTML(MAIN_URL);

		int id = 0;

		while (true) {

			String countryURL = getCountryURL(mainPageContent);
			
			String countryName = getCountryName(mainPageContent);

			if (countryURL == null)
				break;

			int page=1;
			
			String countryContent = getHTML(countryURL+"?page="+page);		
			
			while (true) {
				
				page++;
				
				int index = countryContent.indexOf(" you wish detail page");

				countryContent = countryContent.substring(countryContent.indexOf("you wish detail page in a small")
						+ "you wish detail page in a small".length());

				while (index > -1) {

					updateToCityTable(id, countryName);
					
					//String cityImageURL = getCityImage(countryContent);

					String cityName = getCityName(countryContent);

					//insertToCityTable(id, cityName);

					//savePoster(cityImageURL, id);

					System.out.println(id+" - "+cityName);
					
					index = countryContent.indexOf(" you wish detail page");

					countryContent = countryContent.substring(countryContent.indexOf("you wish detail page in a small")
							+ "you wish detail page in a small".length());
					
					id++;
				}
				
				if(countryContent.indexOf("Son Sayfa &raquo")>-1){
					
					countryContent = getHTML(countryURL+"?page="+page);
				} else{
				
					break;
				}
			}

			mainPageContent = mainPageContent
					.substring(mainPageContent.indexOf("http://www.sehirler.net/kategori-yabanci-sehirler-83")
							+ "http://www.sehirler.net/kategori-yabanci-sehirler-83".length());

		}
	}

	public void insertToCityTable(int ID,String name)  {

		if(!isAdded(ID, "Master")){
			dbMobile.executeUpdate("insert into Master (ID,Text) values (" +ID + ",'"
					+ name + "')");
		}
	}
	
	public void updateToCityTable(int ID,String countryName)  {

			dbMobile.executeUpdate("update Master set Country = '"
					+ countryName + "' where ID="+ID);
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
