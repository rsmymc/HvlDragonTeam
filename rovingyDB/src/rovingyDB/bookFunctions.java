package rovingyDB;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class bookFunctions {

	MySQLDBMobile dbMobile = new MySQLDBMobile();
	
	private ArrayList<String> tags = new ArrayList<>();
	private static String MAIN_URL="http://www.neokur.com/alintilar/&tx=be&ss=";
	private static int NUMBER_OF_PAGE=155;
	
	public void readDataFromAuthorID() throws IOException{

		dbMobile.connectToDB(dbType.KITAPP);

		BufferedReader br = new BufferedReader(
				new FileReader("C:\\Users\\hp p\\workspace\\repliklerDatabase\\src\\repliklerDatabase\\id.txt"));

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
			
			String content="";
			
			for (int i = 2; i <= NUMBER_OF_PAGE; i++) {
			 	content=getHTML(MAIN_URL+i);

					try {

						//getDataFromContent(content);
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

	
//____________________________________________________________________________________
	private String getAuthorImageURL(String authorContent){
		
		int index=authorContent.indexOf("/photos/yazar/");
		
		String url=authorContent.substring(index,authorContent.indexOf("class=\"img_w_156 rad_4\"",index));
		
		url=url.replace("\"", "").trim();
		
		url="http://www.neokur.com/"+url;
		
		return url;
	}	
	
	private String getAuthorName(String authorContent){
		
		int index=authorContent.indexOf("main_baslik");
		
		int beginningIndex=authorContent.indexOf("class=\"c_siyah\">",index);
		
		String url=authorContent.substring(beginningIndex+"class=\"c_siyah\">".length(),authorContent.indexOf("</a></h1>",beginningIndex));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	private String getAuthorBooksURL(String authorContent){
		
		int index=authorContent.indexOf("middle_list_menu");
		
		int beginningIndex=authorContent.indexOf("http",index);
		
		String url=authorContent.substring(beginningIndex,authorContent.indexOf("\">Kitaplar"));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	private String getBooksURL(String authorBooksContent){
		
		int index=authorBooksContent.indexOf("div_w_90 float_l pos_rel m_r_10");
		
		if(index==-1)
			return null;
		
		int beginningIndex=authorBooksContent.indexOf("http",index);
		
		String url=authorBooksContent.substring(beginningIndex,authorBooksContent.indexOf("\" ><img",beginningIndex));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	private String getBookID(String bookURL){
		
		String bookIdentifier="kitap/";
		
		String ID=bookURL.substring(bookURL.indexOf(bookIdentifier)+bookIdentifier.length(),bookURL.lastIndexOf("/"));
		
		return ID;
	}	
	
	private String getBookImageURL(String authorBooksContent){
		
		int index=authorBooksContent.indexOf("div_w_90 float_l pos_rel m_r_10");
		
		if(index==-1)
			return null;
		
		int beginningIndex=authorBooksContent.indexOf("<img src=\"",index)+"<img src=\"".length();
		
		String url=authorBooksContent.substring(beginningIndex,authorBooksContent.indexOf("\" class=\"kitap_90\"",beginningIndex));
		
		url=url.replace("\"", "").trim();
		
		return url;
	}	
	
	private String getBookName(String authorBooksContent){
		
		int index=authorBooksContent.indexOf("div_w_90 float_l pos_rel m_r_10");
		
		if(index==-1)
			return null;
		
		int beginningIndex=authorBooksContent.indexOf("c_siyah f_16 w_b\" >",index)+"c_siyah f_16 w_b\" >".length();
		
		String name=authorBooksContent.substring(beginningIndex,authorBooksContent.indexOf("</a>",beginningIndex));
		
		name=name.replace("\"", "").trim();
		
		return name;
	}	

	private String getPublishedYear(String bookContent){
		
		String bookIdentifier="Basým Tarihi";
		
		String year="";
		
		if(bookContent.indexOf(bookIdentifier)>-1){
		
			year=bookContent.substring(bookContent.indexOf(bookIdentifier)+bookIdentifier.length(),bookContent.indexOf("Neokur Puaný"));
		
			year=year.replace("</td>", "").replace("<td valign=\"top\">", "").replace("<tr>", "").replace("</tr>", "").replace(":", "").trim();
		}
		
		if(year.equals("0"))
			year="";
		return year;
	}	
	
	private String getQuote(String quotePageContent){
		
		int index=quotePageContent.indexOf("/img/tirnak.jpg");
		
		if(index==-1)
			return null;
		
		int beginningIndex=quotePageContent.indexOf("<div class=\"f_14\" >",index)+"<div class=\"f_14\" >".length();
		
		String quote=quotePageContent.substring(beginningIndex,quotePageContent.indexOf("</div>",beginningIndex));
		
		quote=quote.replace("<br />", "\n").trim();
		
		return clearTags(quote);
	}	
	
	
	
	private String clearTags(String quote){
		
		for (String tag : tags) {

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "");
		}

		while (quote.indexOf("<") > -1) {

			String tag = quote.substring(quote.indexOf("<") + 1, quote.indexOf(">"));

			tags.add(tag);

			quote = quote.replace("<" + tag + ">", "").replace("</" + tag + ">", "").trim();
		}
		
		quote=quote.replace("&quot;", "");
		return quote;
	}
	
	private String getLikeCount(String quotePageContent){
		
		int index=quotePageContent.indexOf("</span> beðen");
		
		if(index==-1)
			return null;
		
		quotePageContent=quotePageContent.substring(0,index);
		
		int beginningIndex=quotePageContent.lastIndexOf(">")+1;
		
		String likeCount=quotePageContent.substring(beginningIndex);
		
		likeCount=likeCount.replace("\"", "").trim();
		
		return likeCount;
	}	


	public void getDataFromAuthorID(String authorID) throws Exception {
		
			String authorPageURL = "http://www.neokur.com/isim/"+ authorID;

			String authorPageContent = getHTML(authorPageURL);
			
			String authorName = getAuthorName(authorPageContent);
			
			String authorImageURL =getAuthorImageURL(authorPageContent);
			
			
			
			String authorBooksURL=getAuthorBooksURL(authorPageContent);
		
			int index=1;
			
			while(true){
				String pagedauthorBooksURL=authorBooksURL+"&ss="+index;
				
				String authorBooksContent = getHTML(pagedauthorBooksURL);
				
				String bookURL=getBooksURL(authorBooksContent);
				
				if(bookURL == null)
					break;
				
				while(true){
					bookURL=getBooksURL(authorBooksContent);
					
					if(bookURL == null){
						break;
					} else {
					
						String bookID = getBookID(bookURL);
					
							String bookName = getBookName(authorBooksContent);
							
							String bookImageURL= getBookImageURL(authorBooksContent);
							
							String bookDetailContent = getHTML(bookURL+"/kitap-detay");
							
							String publishedYear = getPublishedYear(bookDetailContent);
							
							String quotePageURL= bookURL+"/kitap-alintilari";
							
							int pageIndex=1;
						
							while(true){
								
								String pagedquotePageURL=quotePageURL+"&ss="+pageIndex;
								
								String quotePageContent = getHTML(pagedquotePageURL);
								
								String quote=getQuote(quotePageContent);
								
								if(quote == null)
									break;
								
								while(true){
									quote=getQuote(quotePageContent);
									
									if(quote == null){
										break;
									} else {
										
										
									}
							
									String likeCount= getLikeCount(quotePageContent);
									
									insertToQuoteTable(bookID, quote,likeCount);
									
									insertToBookTable(bookID,bookName,publishedYear,authorID);
									
									insertToAuthorTable(authorName, authorID);
									
									savePoster(authorImageURL,authorID,"author");
									
									savePoster(bookImageURL, bookID,"book");
									
									System.out.println(authorName +"-"+bookName +"-"+quote);
								
									quotePageContent = quotePageContent.substring(quotePageContent.indexOf("/img/tirnak.jpg") + "/img/tirnak.jpg".length());
								}
									
								pageIndex++;
							}
					}
					
					authorBooksContent = authorBooksContent.substring(authorBooksContent.indexOf("div_w_90 float_l pos_rel m_r_10") + "div_w_90 float_l pos_rel m_r_10".length());
				}
				index++;
			}
	}

	public void insertToBookTable(String bookID,String name,String year,String AuthorID)  {

		if(!isAdded(bookID, "Books")){
			dbMobile.executeUpdate("insert into Books (ID,Name,Year,AuthorID) values (" +bookID + ",'"
					+ name.replace("'", "''") + "','" +  year + "'," + AuthorID + ")");
		}
	}

	public void insertToQuoteTable(String bookID,String text, String likeCount) {
		if(!isQuoteAdded(text))
		{	
			dbMobile.executeUpdate("insert into newQuotes (BookID,Text,LikeCount) values (" + bookID+ "," + "'"
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
		
		ResultSet rs =dbMobile.executeQuery("SELECT Text FROM newQuotes Where Text='" + text+"'");
		ResultSet rs2 =dbMobile.executeQuery("SELECT Text FROM Quotes Where Text='" + text+"'");

		try {
			return (rs.last() || rs2.last());
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
		
		dbMobile.connectToDB(dbType.KITAPP);
		ResultSet rs = dbMobile.executeQuery("SELECT ID,LikeCount FROM Newquotes");
		while (rs.next()) {
			
			
			int likeCount=rs.getInt("LikeCount");
			String id = rs.getString("ID");
			
			System.out.println(id +"-"+likeCount);
			
			for (int i = 0; i < likeCount; i++) {
				
				dbMobile.executeUpdate("insert into Device_Like (DateTime,DeviceID,Type,ObjectID) values (NOW(),'Dummy"+i+"',2,"+id+")");
			}
		}
	}
	
	public void filterQuotes() throws SQLException{
		
		dbMobile.connectToDB(dbType.KITAPP);
		ResultSet rsBook = dbMobile.executeQuery("SELECT ID FROM Books where AuthorID in (50819)");
		while (rsBook.next()) {
			
			String id = rsBook.getString("ID");
			
			ResultSet rsQuote = dbMobile.executeQuery("SELECT ID,BookID,Text,LikeCount FROM newQuotes where BookID="+id+" order by LikeCount Desc limit 0,20");
			
			while (rsQuote.next()) {
				
				String ID = rsQuote.getString("ID");
				String bookID = rsQuote.getString("BookID");
				String text = rsQuote.getString("Text");
				String likeCount = rsQuote.getString("LikeCount");
				
				dbMobile.executeUpdate("insert into Quotes (BookID,Text,LikeCount) values (" + bookID+ "," + "'"
						+ text.replace("'", "''") + "',"+likeCount+")");
				
				System.out.println(bookID +"-"+text +"-"+likeCount);
			}
		}
	}
	
	public void deleteExist(){
		
		dbMobile.connectToDB(dbType.KITAPP);
		
		File folder = new File("D://book");
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  
		    	 String name=listOfFiles[i].getName().replace(".jpg", "");
		    	  
		    	 if(!isAdded(name, "Books")){
		    		 if(listOfFiles[i].delete()){
		     			System.out.println(listOfFiles[i].getName() + " is deleted!");
		     		}else{
		     			//System.out.println("Delete operation is failed.");
		     		}
		    	 } else {
		    		 System.out.println(listOfFiles[i].getName()+ "Delete operation is failed.");
		    	 }
		    	  
		        //System.out.println("File " + name);
		        
		        
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
	}
	
	public void poem(){
		dbMobile.connectToDB(dbType.KITAPP);
		int index=0;
		
		ResultSet rsBook = dbMobile.executeQuery("SELECT ID,Text FROM Quotes");
		try {
			while (rsBook.next()) {
				index++;
				boolean flag = false;
				String id = rsBook.getString("ID");
				String text = rsBook.getString("Text");
				
				for (int i = 0; i < text.length()-1; i++) {
					
					if(Character.isUpperCase(text.charAt(i+1)) && Character.isLowerCase(text.charAt(i))){
						
						text = text.substring(0,i+1) + "\n" + text.substring(i+1);
						flag=true;
					}
					
				}
				
				if(flag){
				
					dbMobile.executeUpdate("update Quotes set Text='"+text + "' where ID=" + id);
				
					System.out.println(index+" - "+id +" - "+text);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void clearDublicateBooks(){
		dbMobile.connectToDB(dbType.KITAPP);
		ResultSet rs = dbMobile.executeQuery("SELECT `AuthorID`,`Name`,count(*) FROM `Books` group by `AuthorID`,`Name`  having count(*)>1");
		
		try {
			while (rs.next()) {
				
				int authorID=rs.getInt("AuthorID");
				String name = rs.getString("Name");
				
				System.out.println(authorID+" - "+name);
				
				int bookID = 0;
				boolean isFirst = true;
				
				ResultSet rs2 = dbMobile.executeQuery("SELECT * FROM `Books` where Name='"+name.replace("'", "''") +"' and `AuthorID`="+authorID);
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
				dbMobile.executeUpdate("Update Books set LikeCount=(Select count(*) from Device_Like where Type=1 and ObjectID="+bookID+") where ID="+bookID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
