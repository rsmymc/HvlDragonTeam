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

import javax.print.attribute.standard.QueuedJobCount;

public class bookFunctionsEng {

	MySQLDBMobile dbMobile = new MySQLDBMobile();
	
	private static String MAIN_URL="https://www.goodreads.com/list/show/1.Best_Books_Ever?page=";
	private static int NUMBER_OF_PAGE=475;
	private ArrayList<String> tags = new ArrayList<>();
	public void readData() {

		dbMobile.connectToDB(dbType.BOOK);

		try {
			
			String content="";
			
			for (int i = 42; i <= NUMBER_OF_PAGE; i++) {
			 	content=getHTML(MAIN_URL+i);

					try {

						getDataFromContent(content);
					}
					 catch (Exception e) {
						 e.printStackTrace();
						 break;
					}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbMobile.conClose();
	}
	
	public void readDataByAuthor() {

		dbMobile.connectToDB(dbType.BOOK);

		try {
			ResultSet rs = dbMobile.executeQuery("SELECT ID FROM Author");
			
			while (rs.next()) {
				
				int authourID=rs.getInt("ID");
				
				getDataFromAuthorID(String.valueOf(authourID));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dbMobile.conClose();
	}

	
	public void getDataFromAuthorID(String authorID) throws Exception {

		String content = getHTML("https://www.goodreads.com/author/list/" + authorID);
		
		for (int i = 0; i < 15; i++) {

			try {

				String bookImage = getBookImage2(content);

				String bookID = getBookID(bookImage);

				if (!isAdded(bookID, "Books")) {

					String bookName = getBookName(content);

					String bookURL = getBookUrl(content);

					String bookContent = getHTML(bookURL);

					if (!isBookAdded(bookName, authorID)) {

						String quotesUrl = getQuotesUrl(bookContent);

						String quotesContent = getHTML(quotesUrl);

						savePoster(bookImage, bookID, "book");

						insertToBookTable(bookID, bookName, "", authorID);

						System.out.println(bookName);

						int count = 0;

						while (quotesContent.indexOf("&ldquo;") > -1 && count < 20) {

							try {

								String quote = getQuote(quotesContent);

								int likeCount = Integer.valueOf(getLikeCount(quotesContent)) / 100;

								quotesContent = quotesContent
										.substring(quotesContent.indexOf("likes") + "likes".length());

								insertToQuoteTable(bookID, quote, String.valueOf(likeCount));

								count++;

								System.out.println(bookName + " : " + likeCount + " : " + quote);

							} catch (Exception e) {
								e.printStackTrace();
								quotesContent = quotesContent
										.substring(quotesContent.indexOf("likes") + "likes".length());
							}

						}

					}
				}
				content = content
						.substring(content.indexOf("http://schema.org/Book") + "http://schema.org/Book".length());

			} catch (Exception e) {
				e.printStackTrace();
				content = content
						.substring(content.indexOf("http://schema.org/Book") + "http://schema.org/Book".length());
			}
		}
	}
	
//____________________________________________________________________________________
	
	
	
	private String getBookName(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("<a title=\"",index)+"<a title=\"".length();
		
		int endindex=content.indexOf("\" href=",index);
		
		String name =content.substring(startindex,endindex);
		
		return name.replace("&#39;", "''").replace("&amp;", "&").trim();
	}	
	
	private String getBookUrl(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("\" href=\"",index)+"\" href=\"".length();
		
		int endindex=content.indexOf("\">",startindex);
		
		String url =content.substring(startindex,endindex);
		
		return ("https://www.goodreads.com"+url).trim();
	}	
	
	private String getBookImage2(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("bookSmallImg\" itemprop=\"image\" src=\"",index)+"bookSmallImg\" itemprop=\"image\" src=\"".length();
		
		int endindex=content.indexOf("\" />",startindex);
		
		String url =content.substring(startindex,endindex);
		
		int i=url.lastIndexOf("s");
		
		return url.substring(0,i)+"l"+url.substring(i+1);
	}	
	
private String getBookImage(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("bookSmallImg\" src=\"",index)+"bookSmallImg\" src=\"".length();
		
		int endindex=content.indexOf("\" />",startindex);
		
		String url =content.substring(startindex,endindex);
		
		int i=url.lastIndexOf("s");
		
		return url.substring(0,i)+"l"+url.substring(i+1);
	}	
	
	private String getBookID(String bookImageUrl){
		
		int index=bookImageUrl.lastIndexOf("/");

		int endindex=bookImageUrl.indexOf(".",index);
		
		String id =bookImageUrl.substring(index+1,endindex);
		
		return id.trim();
	}	
	
	private String getAuthorName(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("itemprop=\"name\">",index)+"itemprop=\"name\">".length();
		
		int endindex=content.indexOf("</span>",startindex);
		
		String name =content.substring(startindex,endindex);
		
		return name.trim();
	}	
	
	private String getAuthorURL(String content){
		
		String identifier="http://schema.org/Book";
		
		int index=content.indexOf(identifier);
		
		int startindex=content.indexOf("<a class=\"authorName\" itemprop=\"url\" href=\"",index)+"<a class=\"authorName\" itemprop=\"url\" href=\"".length();
		
		int endindex=content.indexOf("\"><span",startindex);
		
		String url =content.substring(startindex,endindex);
		
		return url.trim();
	}	
	
	private String getAuthorID(String authorUrl){
	
		int index=authorUrl.lastIndexOf("/");

		int endindex=authorUrl.indexOf(".",index);
		
		String id =authorUrl.substring(index+1,endindex);
		
		return id.trim();
	}	
	
	private String getAuthorImage(String content){
		
		int index=content.indexOf("https://images.gr-assets.com/authors/");

		int endindex=content.indexOf(".jpg",index);
		
		String id =content.substring(index,endindex+4);
		
		return id.trim();
	}	
	
	private String getQuotesUrl(String content){
		
		int index=content.indexOf("\">More quotes");
		
		content=content.substring(0,index);

		int startindex=content.lastIndexOf("href=\"")+ "href=\"".length();
		
		String url =content.substring(startindex);
		
		return ("https://www.goodreads.com"+url).trim();
	}	
	

	private String getQuote(String content) {
		
		int startindex=content.indexOf("&ldquo;")+"&ldquo;".length();
		
		int endindex=content.indexOf("&rdquo;",startindex);
		
		String quote =content.substring(startindex,endindex).replace("<br />", "\n");
		
		for (String tag : tags) {

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
		}

		while (quote.indexOf("<") > -1) {

			String tag = quote.substring(quote.indexOf("<") + 1, quote.indexOf(">"));

			tags.add(tag);

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
		}
		
		return quote.replace("&#39;", "''").replace("&amp;", "&").trim();
	}
	
	private String getLikeCount(String content) {

		int index=content.indexOf("likes");

		content=content.substring(0,index);
		
		int endindex=content.lastIndexOf("\">")+"\">".length();
		
		String likeCount =content.substring(endindex);
		
		return likeCount.trim();
	}

	
	
	public void getDataFromContent(String content) throws Exception {

		for(int i=0;i<100;i++){
			
			try {
				
				{
				String bookName = getBookName(content);
				
				String bookURL = getBookUrl(content);
				
				String bookImage = getBookImage(content);
				
				String bookID = getBookID(bookImage);
				
				String authorName = getAuthorName(content);
				
				String authorURL = getAuthorURL(content);
				
				String authorID = getAuthorID(authorURL);
				
				String authorContent = getHTML(authorURL);
				
				String authorImage = getAuthorImage(authorContent);
				
				String bookContent = getHTML(bookURL);
				
				String quotesUrl=getQuotesUrl(bookContent);
				
				String quotesContent=getHTML(quotesUrl);
				
				savePoster(authorImage, authorID,"author");
				
				savePoster(bookImage, bookID,"book");
				
				insertToBookTable(bookID, bookName, "", authorID);
				
				insertToAuthorTable(authorName, authorID);
				
				int count=0;
				
				while(quotesContent.indexOf("&ldquo;")>-1 && count<20){
					
					try {
						
						String quote=getQuote(quotesContent);
						
						int likeCount=Integer.valueOf(getLikeCount(quotesContent))/100;
						
						quotesContent=quotesContent.substring(quotesContent.indexOf("likes")+"likes".length());
						
						insertToQuoteTable(bookID, quote, String.valueOf(likeCount));
						
						count++;
						
						System.out.println(bookName +" : "+ likeCount+" : "+quote);
						
					} catch (Exception e) {
						e.printStackTrace();
						quotesContent=quotesContent.substring(quotesContent.indexOf("likes")+"likes".length());
					}
				
				}
				}
				content=content.substring(content.indexOf("http://schema.org/Book")+"http://schema.org/Book".length());
				
			} catch (Exception e) {
				e.printStackTrace();
				content=content.substring(content.indexOf("http://schema.org/Book")+"http://schema.org/Book".length());
			}
			
		}

	}
	
	public void insertToBookTable(String bookID,String name,String year,String AuthorID)  {

		//if(!isAdded(bookID, "Books"))
		{
			dbMobile.executeUpdate("insert into Books (ID,Name,Year,AuthorID,LikeCount) values (" +bookID + ",'"
					+ name + "','" +  year + "'," + AuthorID + ",0)");
		}
	}

	public void insertToQuoteTable(String bookID,String text, String likeCount) {
		if(!isQuoteAdded(text)){	
			dbMobile.executeUpdate("insert into Quotes (BookID,Text,LikeCount) values (" + bookID+ "," + "'"
					+ text.replace("'", "''") + "',"+likeCount+")");
		}
	}
	
	public void insertToAuthorTable(String name,String AuthorID) {
		if(!isAdded(AuthorID, "Author")){
			dbMobile.executeUpdate("insert into Author (ID,Name) values (" +AuthorID + ",'"
					+ name + "')");
		}

	}
	
	public Boolean isQuoteAdded(String text) {
		
		ResultSet rs =dbMobile.executeQuery("SELECT Text FROM Quotes Where Text='" + text+"'");

		try {
			return rs.last();
		} catch (SQLException e) {
			return false;
		}
	}
	public Boolean isAdded(String ID,String table)  {
		
		ResultSet rs =dbMobile.executeQuery("SELECT ID FROM "+table+" Where ID=" + ID);

		try {
			return rs.last();
		} catch (SQLException e) {
			return false;
		}
	}
	public Boolean isBookAdded(String name,String authorID)  {
		
		ResultSet rs =dbMobile.executeQuery("SELECT ID FROM Books Where Name='" + name+"' and AuthorID="+authorID);

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

	public void savePoster(String urlposter, String ID,String folder){
		
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

			FileOutputStream fos = new FileOutputStream("D://"+folder+"//" + ID + ".jpg");
			fos.write(response);
			fos.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	public void insertDummyLike() throws SQLException{
		
		dbMobile.connectToDB(dbType.BOOK);
		ResultSet rs = dbMobile.executeQuery("SELECT ID,LikeCount FROM Quotes");
		
		int index=0;
		while (rs.next()) {
			
			
			int likeCount=rs.getInt("LikeCount");
			String id = rs.getString("ID");
			index++;
			System.out.println(index + " - " +id +" - "+likeCount);
			
			for (int i = 0; i < likeCount; i++) {
				
				dbMobile.executeUpdate("insert into Device_Like (DateTime,DeviceID,Type,ObjectID) values (NOW(),'Dummy"+i+"',2,"+id+")");
			}
		}
	}
	
	public void clearDublicateBooks(){
		dbMobile.connectToDB(dbType.BOOK);
		ResultSet rs = dbMobile.executeQuery("SELECT `AuthorID`,`Name`,count(*) FROM `Books`group by `AuthorID`,`Name`  having count(*)>1");
		
		try {
			while (rs.next()) {
				
				int authorID=rs.getInt("AuthorID");
				String name = rs.getString("Name");
				
				int bookID = 0;
				boolean isFirst = true;
				
				System.out.println(authorID+" - "+name);
				
				ResultSet rs2 = dbMobile.executeQuery("SELECT * FROM `Books` where Name='"+name+"' and `AuthorID`="+authorID);
				while (rs2.next()) {
					
					if(isFirst){
						bookID = rs2.getInt("ID");
						isFirst = false;
					} else {
						
						int bID=rs2.getInt("ID");
						
						dbMobile.executeUpdate("Delete from Books where ID="+bID);
						dbMobile.executeUpdate("Update Comments set BookID="+bookID+" where BookID="+bID);
						dbMobile.executeUpdate("Update Quotes set BookID="+bookID+" where BookID="+bID);
						dbMobile.executeUpdate("Update Device_Like set ObjectID="+bookID+" where Type=1 and ObjectID="+bID);
					}	
				}
				//dbMobile.executeUpdate("Update Books set LikeCount=(Select count(*) from Device_Like where Type=1 and ObjectID="+bookID+") where ID="+bookID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
