package rovingyDB;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.json.JSONObject;

public class Functions {

	MySQLDB db = new MySQLDB();

	MySQLDBMobile dbMobile = new MySQLDBMobile();

	private boolean isSeries = false;
	private String moiveName;
	private String year = "";
	private ArrayList<String> quotes;
	private ArrayList<String> tags = new ArrayList<>();
	private String url;
	private String id;
	private ResultSet categories;

	public void readData() {

		db.connectToDB();
		dbMobile.connectToDB(dbType.REPLIKLER);

		try {
			ResultSet rs = db.executeStatament(
					"SELECT ID,post_content,post_title,post_name FROM rpl_posts where post_status='publish' and post_type='post' and post_date > '2016-09-23' ");//post_date > '2016-09-23' bundan sonrakiler çekilecek gelece sefere
			while (rs.next()) {
				
				if (!isAdded(rs.getString("ID"))) {

					isSeries = false;
					quotes = new ArrayList<>();

					try {

						getQuotesFromContent(rs.getString("post_content"));

						getNameAndYear(rs.getString("post_title"));

						this.url = "http://replikler.net/replik/" + rs.getString("post_name");

						this.id = rs.getString("ID");

						this.categories = getCategories(this.id);

						if (this.categories.last()) {

							this.categories.beforeFirst();

							getImageContent(rs.getString("post_content"));

							insertToMovieTable();

							insertToQuoteTable();

							insertToCategoryTable();

							System.out.println(this.id + this.moiveName);
						}

					} catch (Exception e) {

					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.conClose();
		dbMobile.conClose();
	}

	private void print() throws SQLException {
		System.out.println(
				"Name :" + moiveName + "\nYear :" + this.year + "\nURL :" + this.url + "\nisSeries :" + isSeries);
		System.out.println("\nQuotes ");
		for (String string : quotes) {

			System.out.println("\n" + string);
		}

		System.out.println("\nCategories ");

		while (this.categories.next()) {
			System.out.println(this.categories.getString("term_taxonomy_id"));
		}
	}

	public void getImageContent(String content) {

		String url = "";

		int startIndex = content.indexOf("http://replikler.net/wp-content/uploads");

		String[] formats = { ".jpg", ".jpeg", ".gif", ".png" };

		for (String format : formats) {

			if (content.indexOf(format, startIndex) > -1) {
				url = content.substring(startIndex, content.indexOf(format, startIndex) + format.length());
			}

			try {
				savePoster(url, this.id);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void getQuotesFromContent(String content) {

		while (content.indexOf("<blockquote>") > -1) {

			String quote = content.substring(content.indexOf("<blockquote>") + "<blockquote>".length(),
					content.indexOf("</blockquote>"));

			for (String tag : tags) {

				quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
			}

			while (quote.indexOf("<") > -1) {

				String tag = quote.substring(quote.indexOf("<") + 1, quote.indexOf(">"));

				tags.add(tag);

				quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
			}

			if (!quote.contains("Ýlgili Baðlantý"))
				quotes.add(quote);

			content = content.substring(content.indexOf("</blockquote>") + "</blockquote>".length());
		}
	}

	public void getNameAndYear(String title) {
		String name = title;

		if (title.lastIndexOf("(") > -1 && title.lastIndexOf(")") > -1) {
			String year = title.substring(title.lastIndexOf("("), title.lastIndexOf(")"));

			if (year.contains("Dizi"))
				isSeries = true;

			year = year.replace("(", "").replace("Dizi", "").trim();
			year = year.replace("(", "").replace("Anime", "").trim();
			this.year = year.substring(0, 4);
			name = title.substring(0, title.lastIndexOf("("));
		}

		name = name.replace("&#039;", "'");
		name = name.replace("replikleri", "").replace("Replikleri", "");

		this.moiveName = name;
	}

	public ResultSet getCategories(String ID) throws SQLException {
		ResultSet rs = db.executeStatament(
				"SELECT object_id,term_taxonomy_id FROM  rpl_term_relationships WHERE term_taxonomy_id not in (1,10,11,16,17,23,31,32) and term_taxonomy_id<35 and object_id="
						+ ID);

		return rs;
	}

	public Boolean isAdded(String ID) throws SQLException {
		ResultSet rs = dbMobile.executeQuery("SELECT ID FROM Movies Where ID=" + ID);

		return rs.last();
	}

	public void insertToMovieTable() {

		String name1 = "", name2 = "", name3 = "";

		String[] names = this.moiveName.split("/");

		name1 = names[0];
		try {
			name2 = names[1];
		} catch (Exception e) {
		}
		try {
			name3 = names[2];
		} catch (Exception e) {
		}

		dbMobile.executeUpdate("insert into Movies (ID,Name1,Name2,Name3,Year,Type,URL) values (" + this.id + ",'"
				+ name1.trim().replace("'", "''") + "','" + name2.trim().replace("'", "''") + "','"
				+ name3.trim().replace("'", "''") + "','" + this.year + "'," + ((this.isSeries) ? 2 : 1) + ",'"
				+ this.url + "')");

	}

	public void insertToQuoteTable() throws SQLException {

		for (String string : quotes) {

			dbMobile.executeUpdate("insert into Quotes (MovieID,Text) values (" + this.id + "," + "'"
					+ string.replace("'", "''") + "')");
		}
	}
	
	public void insertToEnglishQuoteTable() throws SQLException {

		for (String string : quotes) {

			dbMobile.executeUpdate("insert into Quotes_ENG (MovieID,Text) values (" + this.id + "," + "'"
					+ string.replace("'", "''") + "')");
		}
	}

	public void insertToCategoryTable() throws SQLException {

		while (this.categories.next()) {
			dbMobile.executeUpdate("insert into Category_Relationship (MovieID,CategoryID) values (" + this.id + ","
					+ "'" + this.categories.getString("term_taxonomy_id") + "')");

		}
	}

	public void getPosterbyID() {
		db.connectToDB();
		try {
			ResultSet rs = db.executeStatament(
					"SELECT post_content,ID FROM rpl_posts where post_status='publish' and post_type='post' and post_content like '%imdb.com%'");
			while (rs.next()) {
				try {
					String imdbID = getImdbID(rs.getString("post_content"));

					String poster = decodeJson(imdbID);

					savePoster(poster, rs.getString("ID"));

					System.out.println(this.moiveName);

				} catch (Exception e) {

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.conClose();
	}

	public void getPosterbyName() {
		db.connectToDB();
		
		try {
			ResultSet rs = db.executeStatament(
					"SELECT post_content,ID,post_title FROM rpl_posts where post_status='publish' and post_type='post' and post_content not like '%imdb.com%'");
			while (rs.next()) {
				try {
					getNameAndYear(rs.getString("post_title"));

					String poster = decodeJsonbyName();

					savePoster(poster, rs.getString("ID"));

					System.out.println(this.moiveName);

				} catch (Exception e) {

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.conClose();
	}

	public void getEnglishQuote() {
		db.connectToDB();
		dbMobile.connectToDB(dbType.REPLIKLER);
		try {
			
			quotes=new ArrayList<>();
			ResultSet rs = db.executeStatament(
					"SELECT post_content,ID,post_title FROM rpl_posts where post_status='publish' and post_type='post' and post_content  like '%imdb.com%'");
			while (rs.next()) {
				try {
					
					quotes=new ArrayList<>();
					
					this.id = rs.getString("ID");
					
					getNameAndYear(rs.getString("post_title"));

					String URL="http://www.imdb.com/title/"+getImdbID(rs.getString("post_content"))+"/quotes";
					
					String urlSource = getHTML(URL);
					
					getQuotesFromIMDB(urlSource);
					
					insertToEnglishQuoteTable();
					
					System.out.println(this.moiveName);
					
				} catch (Exception e) {

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.conClose();
	}

	public void getQuotesFromIMDB(String urlSource) {

		while (urlSource.indexOf("<div class=\"sodatext\">") > -1) {

			String quote = urlSource.substring(urlSource.indexOf("<div class=\"sodatext\">") +"<div class=\"sodatext\">".length(),
					urlSource.indexOf("<div class=\"did-you-know-actions\">"));

			quote=quote.replace("</p>", "\n").replace(":", ": ");
			
			
			for (String tag : tags) {

				quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
			}
			
			while (quote.indexOf("[") > -1) {

				String tag = quote.substring(quote.indexOf("[") + 1, quote.indexOf("]"));

				quote = quote.replace("[" + tag + "]", "").trim();
			}

			while (quote.indexOf("<") > -1) {

				String tag = quote.substring(quote.indexOf("<") + 1, quote.indexOf(">"));

				tags.add(tag);

				quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
			}
			
			quotes.add(quote);
			
			urlSource = urlSource.substring(urlSource.indexOf("<div class=\"did-you-know-actions\">") + "<div class=\"did-you-know-actions\">".length());
		}
	}
	public String getImdbID(String content) throws Exception {

		content = content.substring(content.lastIndexOf("title/") + 6);
		content = content.substring(0, content.indexOf("/"));
		return content;
	}

	public String decodeJson(String id) throws Exception {
		String url = "http://www.omdbapi.com/?i=" + id + "&plot=short&r=json";

		String result = getHTML(url);

		JSONObject jsonObj = new JSONObject(result);

		String poster = jsonObj.getString("Poster");

		return poster;
	}

	public String decodeJsonbyName() throws Exception {
		String url = "http://www.omdbapi.com/?t=" + this.moiveName.split("/")[0].trim().replace(" ", "+") + "&y="
				+ this.year + "&plot=short&r=json";

		String result = getHTML(url);

		JSONObject jsonObj = new JSONObject(result);

		String poster = jsonObj.getString("Poster");

		return poster;
	}

	public String getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}

	public void savePoster(String urlposter, String ID) throws IOException {
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
	}

	public void insertToRandomDaily(){
		int index=0;
		dbMobile.connectToDB(dbType.REPLIKLER);
		for(int i=101;i<29000;i=i+71){
		
			//String s="Delete From `Daily_Quotes_ENG`where QuoteID= "+i;
			String s="INSERT INTO `Daily_Quotes_ENG` VALUES(NOW() + INTERVAL "+index+" DAY, "+i+")";
			
			index++;
			dbMobile.executeUpdate(s);
		}
	}
	
	public void checkPoster() {
		dbMobile.connectToDB(dbType.REPLIKLER);

		String result = "";
		String id="";
		
		
		try {
			ResultSet rs = dbMobile.executeQuery("SELECT ID,name1 FROM Movies");
			while (rs.next()) {

				try {
					id=rs.getString("ID") ;
					URL url;

					url = new URL("http://replikler.net/quiz/moviequiz/images/" + rs.getString("ID") + ".jpg");

					BufferedImage c = ImageIO.read(url);
				} catch (IOException e) {
					e.printStackTrace();
					result+=id+",";
					System.out.println(result);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.conClose();
	}

}
