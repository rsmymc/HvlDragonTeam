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

public class siirFunctionsEng {

	MySQLDBMobile dbMobile = new MySQLDBMobile();

	private ArrayList<String> tags = new ArrayList<>();
	private static String MAIN_URL = "https://www.poemhunter.com/p/t/l.asp?a=0&l=Top500&p=";

	public void readDataFromAuthorID() throws IOException {

		dbMobile.connectToDB(dbType.POEMS);

		BufferedReader br = new BufferedReader(
				new FileReader("C:\\Users\\hp p\\workspace\\repliklerDatabase\\src\\repliklerDatabase\\id3.txt"));

		String line = br.readLine();

		while (line != null) {

			try {
				getDataFromAuthorID(line);
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

			for (int i = 1; i <= 10; i++) {

				String pagedURL = MAIN_URL + i;

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

		int startIndex = content.indexOf("<div class=\"rank\">");
		int endIndex = content.lastIndexOf("<div class=\"info\">") +"<div class=\"info\">".length();

		content = content.substring(startIndex,endIndex);

		while (content.indexOf("<div class=\"info\">") > -1) {

			String author = getAuthorURL(content);

			System.out.println(author);

			content = content.substring(content.indexOf("<div class=\"info\">") + "<div class=\"info\">".length());

		}
	}
	
	private String getAuthorURL(String content) {

		content = content.substring(0, content.indexOf("<div class=\"info\">"));

		int index = content.lastIndexOf("<a href=\"/") + "<a href=\"/".length();
		
		int endIndex = content.indexOf("title", index);

		String url = content.substring(index,endIndex);

		url = url.replace("\"", "").trim();

		url = "https://www.poemhunter.com/" + url;

		return url;
	}

	private String getAuthorImageURL(String authorContent) {


		int beginningIndex = authorContent.indexOf("photoImg\" src=\"") + "photoImg\" src=\"".length();

		String url = authorContent.substring(beginningIndex, authorContent.indexOf("\" alt", beginningIndex));

		url = url.replace("\"", "").trim();

		return "https://www.poemhunter.com"+url;
	}

	private String getAuthorYear(String authorID) {

		String authorContent = getHTML(authorID + "hayati");

		int index = authorContent.indexOf("<b>HAYATI</b>") + "<b>HAYATI</b>".length();

		String year = authorContent.substring(index, authorContent.indexOf("</td></tr>", index));

		year = year.replace("\"", "").trim();

		return year;
	}

	private String getAuthorName(String authorContent) {

		int beginningIndex = authorContent.indexOf("<h1>");

		String name = authorContent.substring(beginningIndex + "<h1>".length(),
				authorContent.indexOf("</h1>", beginningIndex));

		name = name.replace("\"", "").trim();

		return name;
	}

	private String getPoemUrl(String poemsContent) {

		int index = poemsContent.indexOf("<td class=\"title\">");

		int beginningIndex = poemsContent.indexOf("a href=\"", index) + "a href=\"".length();

		String url = poemsContent.substring(beginningIndex, poemsContent.indexOf("\" title", beginningIndex));

		url = "https://www.poemhunter.com" + url;

		url = url.replace("\"", "").trim();

		return url;
	}
	
	private String getPoemName(String poemContent) {

		int index = poemContent.indexOf("<h1 class=\"title w-660\" itemprop=\"name\">") + "<h1 class=\"title w-660\" itemprop=\"name\">".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</h1>", index));

		name = name.substring(0,name.indexOf("- Poem by"));
		
		name = name.replace("\"", "").trim();

		return name;
	}
	
	
	private String getPoem(String poemContent) {

		poemContent = poemContent.substring(poemContent.indexOf("class=\"KonaBody\""));
		
		int index = poemContent.indexOf("<p>") + "<p>".length();

		String name = poemContent.substring(index,
				poemContent.indexOf("</p>", index));

		name = name.replace("\"", "").trim();

		name = name.replace("<br />", "\n").trim();

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

	int authorID = 288;
	
	public void getDataFromAuthorID(String authorPageURL) throws Exception {

		authorID ++;
		
		String authorPageContent = getHTML(authorPageURL);

		String authorName = getAuthorName(authorPageContent);

		String authorImageURL = getAuthorImageURL(authorPageContent);

		String authorYear = "";

		int pageIndex = 1;

		String pagedauthorBooksURL = authorPageURL + "poems/page-" + pageIndex + "/";

		String authorPoemsContent = getHTML(pagedauthorBooksURL);
		
		while (true) {

			while (authorPoemsContent.indexOf("<td class=\"title\">") > -1) {

				try {
					String poemURL = getPoemUrl(authorPoemsContent);

					if (poemURL == null)
						break;

					String poemContent = getHTML(poemURL);
					
					String poemName = getPoemName(poemContent);
					
					String poem = getPoem(poemContent);
					
					insertToAuthorTable(authorName, ""+authorID ,authorPageURL, authorYear);

					insertToPoemTable(""+authorID, poem,poemURL,poemName);
					
					System.out.println(authorName + "-" + poemName +"\n"+ poem);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				authorPoemsContent = authorPoemsContent.substring(
						authorPoemsContent.indexOf("<td class=\"title\">") + "<td class=\"title\">".length());
			}
			
			savePoster(authorImageURL, ""+authorID,"sair");
			
			if(authorPoemsContent.indexOf("next page") == -1)
				break;
			
			pageIndex++;
			
			pagedauthorBooksURL = authorPageURL + "poems/page-" + pageIndex + "/";

			authorPoemsContent = getHTML(pagedauthorBooksURL);
			
			
			
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
