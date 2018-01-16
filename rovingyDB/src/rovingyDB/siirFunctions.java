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

public class siirFunctions {

	MySQLDBMobile dbMobile = new MySQLDBMobile();

	private ArrayList<String> tags = new ArrayList<>();
	private static String MAIN_URL = "http://www.antoloji.com/populer-sairler/sayfa-";

	public void readDataFromAuthorID() throws IOException {

		dbMobile.connectToDB(dbType.SIIR);

		BufferedReader br = new BufferedReader(
				new FileReader("C:\\Users\\hp p\\workspace\\repliklerDatabase\\src\\repliklerDatabase\\id2.txt"));

		String line = br.readLine();

		while (line != null) {

			try {
				getDataFromAuthorName(line.split("~")[0], line.split("~")[1]);
			} catch (Exception e) {

			}
			line = br.readLine();
		}

		br.close();
		dbMobile.conClose();
	}

	public void readData() {

		dbMobile.connectToDB(dbType.KITAPP);

		try {

			for (int i = 1; i <= 18; i++) {

				String pagedURL = MAIN_URL + i + "/";

				String content = getHTML(pagedURL);

				getSair(content);
			}

		} catch (Exception e) {
		
			e.printStackTrace();
		}

		dbMobile.conClose();
	}

	

	// ____________________________________________________________________________________

	private void getSair(String content) {

		int startIndex = content.indexOf("class=\"liste_border");
		int endIndex = content.lastIndexOf("class=\"liste_border");

		content = content.substring(startIndex, endIndex);

		while (content.indexOf("target=\"_top") > -1) {

			String author = getAuthorURL(content);

			System.out.println(author);

			content = content.substring(content.indexOf("target=\"_top") + "target=\"_top".length());

		}
	}
	
	private String getAuthorURL(String content) {

		content = content.substring(0, content.indexOf("\" target=\"_top"));

		int index = content.lastIndexOf("<a href=\"/") + "<a href=\"/".length();

		String url = content.substring(index);

		url = url.replace("\"", "").trim();

		url = "http://www.antoloji.com/" + url;

		return url;
	}

	private String getAuthorImageURL(String authorID) {

		String authorContent = getHTML(authorID + "hayati");

		int index = authorContent.indexOf("<b>HAYATI</b>");

		int beginningIndex = authorContent.indexOf("<img src=\"", index) + "<img src=\"".length();

		String url = authorContent.substring(beginningIndex, authorContent.indexOf("\" align=\"left", beginningIndex));

		url = url.replace("\"", "").trim();

		return url;
	}

	private String getAuthorYear(String authorID) {

		String authorContent = getHTML(authorID + "hayati");

		int index = authorContent.indexOf("<b>HAYATI</b>") + "<b>HAYATI</b>".length();

		String year = authorContent.substring(index, authorContent.indexOf("</td></tr>", index));

		year = year.replace("\"", "").trim();

		return year;
	}

	private String getAuthorName(String authorContent) {

		int index = authorContent.indexOf("<a class=\"Sair_Adi\"");

		int beginningIndex = authorContent.indexOf("\">", index);

		String name = authorContent.substring(beginningIndex + "\">".length(),
				authorContent.indexOf("</a>", beginningIndex));

		name = name.replace("\"", "").trim();

		return name;
	}

	private String getPoemUrl(String poemsContent) {

		int index = poemsContent.indexOf("input type=\"checkbox\"");

		int beginningIndex = poemsContent.indexOf("a href=\"", index) + "a href=\"".length();

		String url = poemsContent.substring(beginningIndex, poemsContent.indexOf("\"><b>", beginningIndex));

		url = "http://www.antoloji.com" + url;

		url = url.replace("\"", "").trim();

		return url;
	}
	
	private String getPoemUrl2(String poemsContent) {

		int index = poemsContent.indexOf("?sayfa=siir&siir_id=");

		String url = poemsContent.substring(index, poemsContent.indexOf("\">", index));

		url = "http://www.siirdefteri.com/" + url;

		url = url.replace("\"", "").trim();

		return url;
	}

	private String getPoemName(String poemContent) {

		int index = poemContent.indexOf("Siir_baslik\"><span itemprop=\"name\">") + "Siir_baslik\"><span itemprop=\"name\">".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</span>", index));

		name = name.replace("\"", "").trim();

		return name;
	}
	
	private String getPoemName2(String poemContent) {

		int index = poemContent.indexOf("<h3 class=\"margin-top-0 margin-bottom-0\">") + "<h3 class=\"margin-top-0 margin-bottom-0\">".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</h3>", index));

		name = name.trim();

		return name;
	}

	
	private String getPoem(String poemContent) {

		int index = poemContent.indexOf("Siir_metin\"><div class=\"sZSfr\">") + "Siir_metin\"><div class=\"sZSfr\">".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</div>", index));

		name = name.replace("\"", "").trim();

		name = name.replace("<br>", "\n").trim();

		return clearTags(name);
	}
	private String getPoem2(String poemContent) {

		int index = poemContent.indexOf("<div class=\"siir\">") + "<div class=\"siir\">".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</div>", index));

		name = name.replace("<br/>", "\n").trim();

		return clearTags(name);
	}

	private String clearTags(String quote) {

		for (String tag : tags) {

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
		}

		while (quote.indexOf("<") > -1) {

			String tag = quote.substring(quote.indexOf("<") + 1, quote.indexOf(">"));

			tags.add(tag);

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
		}

		quote = quote.replace("&quot;", "");
		return quote;
	}

	int authorID = 0;
	
	public void getDataFromAuthorID(String authorPageURL) throws Exception {

		authorID ++;
		
		String authorPageContent = getHTML(authorPageURL);

		String authorName = getAuthorName(authorPageContent);

		String authorImageURL = getAuthorImageURL(authorPageURL);

		String authorYear = getAuthorYear(authorPageURL);

		int pageIndex = 1;

		while (true) {

			String pagedauthorBooksURL = authorPageURL + "siirleri/ara-/sirala-/sayfa-" + pageIndex + "/";

			String authorPoemsContent = getHTML(pagedauthorBooksURL);
			
			if(authorPoemsContent.indexOf("input type=\"checkbox\"") == -1)
				break;

			while (authorPoemsContent.indexOf("input type=\"checkbox\"") > -1) {

				String poemURL = getPoemUrl(authorPoemsContent);

				if (poemURL == null)
					break;

				String poemContent = getHTML(poemURL);
				
				String poemName = getPoemName(poemContent);
				
				String poem = getPoem(poemContent);
				
				insertToAuthorTable(authorName, ""+authorID ,authorPageURL, authorYear);

				insertToPoemTable(""+authorID, poem,poemURL,poemName);
				
				savePoster(authorImageURL, ""+authorID,"sair");
				
				System.out.println(authorName + "-" + poemName +"\n"+ poem);
				
				authorPoemsContent = authorPoemsContent.substring(
						authorPoemsContent.indexOf("input type=\"checkbox\"") + "input type=\"checkbox\"".length());
			}
			pageIndex++;
		}
	}
	
	public void getDataFromAuthorName(String authorPageURL, String authorID) throws Exception {

		
		String authorPageContent = getHTML(authorPageURL);
	
			while (authorPageContent.indexOf("?sayfa=siir&siir_id=") > -1) {

				String poemURL = getPoemUrl2(authorPageContent);

				if (poemURL == null)
					break;

				String poemContent = getHTML(poemURL);
				
				String poemName = getPoemName2(poemContent);
				
				String poem = getPoem2(poemContent);
			
				insertToPoemTable(""+authorID, poem,poemURL,poemName);
				
				System.out.println(authorID + "-" + poemName +"\n"+ poem);
				
				authorPageContent = authorPageContent.substring(
						authorPageContent.indexOf("?sayfa=siir&siir_id=") + "?sayfa=siir&siir_id=".length());
			}
	}


	public void insertToPoemTable(String authorID, String text, String URL,String poemName) {
		if (!isPoemAdded(text)) {
			dbMobile.executeUpdate("insert into Poems (AuthorID,Text,URL,Name) values (" + authorID + "," + "'"
					+ text.replace("'", "''") + "','" + URL + "','"+ poemName.replace("'", "''")+"')");
		}
	}

	public void insertToAuthorTable(String name, String AuthorID, String URL, String year) {
		if (!isAdded(AuthorID, "Author")) {
			dbMobile.executeUpdate("insert into Author (ID,Name,URL,Year) values (" + AuthorID + ",'" + name + "','"+URL+"','"+year +"')");
		}

	}

	public Boolean isPoemAdded(String text) {

		ResultSet rs = dbMobile.executeQuery("SELECT Text FROM Poems Where Text='" + text + "'");
		//ResultSet rs2 = dbMobile.executeQuery("SELECT Text FROM Quotes Where Text='" + text + "'");

		try {
			return (rs.last());
		} catch (SQLException e) {
			return false;
		}
	}

	public Boolean isAdded(String ID, String table) {

		ResultSet rs = dbMobile.executeQuery("SELECT ID FROM " + table + " Where ID=" + ID);

		try {
			return rs.last();
		} catch (SQLException e) {
			return false;
		}
	}

	public String getHTML(String urlToRead) {
		StringBuilder result = new StringBuilder();
		BufferedReader rd;
		try {
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {

		}

		return result.toString();
	}

	public void savePoster(String urlposter, String ID, String folder) {

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

			FileOutputStream fos = new FileOutputStream("D://" + folder + "//" + ID + ".jpg");
			fos.write(response);
			fos.close();
		} catch (Exception e) {
			
		}

	}

	
}
