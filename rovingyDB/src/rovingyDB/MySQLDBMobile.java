package rovingyDB;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

public class MySQLDBMobile {

	Connection con = null;
	Statement st = null;
	ResultSet rs = null;

	public MySQLDBMobile(){
		
	}
	
	public void connectToDB(dbType dbType) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			String url = "";
			
			String username = "";
			
			String password = "";
			
			switch (dbType) {
				case REPLIKLER:
					url = "jdbc:mysql://188.121.44.166/repliklerandro?useUnicode=true&characterEncoding=utf-8";
					
					username = "repliklerandro";
		
					password = "Replikler1!";
					break;
				case KITAPP:
					url = "jdbc:mysql://46.101.106.121/kitapsozleri111?useUnicode=true&characterEncoding=utf-8";
					
					username = "rovingy";
					
					password = "Rovingy.6878!";
					break;
				case BOOK:
					url = "jdbc:mysql://46.101.106.121/bookquotes1?useUnicode=true&characterEncoding=utf-8";
					
					username = "rovingy";
					
					password = "Rovingy.6878!";
					break;
				case SIIR:
					url = "jdbc:mysql://46.101.106.121/siirler1?useUnicode=true&characterEncoding=utf-8";
					
					username = "rovingy";
		
					password = "Rovingy.6878!";
					break;
				case POEMS:
					url = "jdbc:mysql://46.101.106.121/poems?useUnicode=true&characterEncoding=utf-8";
					
					username = "rovingy";
		
					password = "Rovingy.6878!";
					break;
			}
			
			con = DriverManager.getConnection(url, username, password);

			st = con.createStatement();

			System.out.println("Connected to DB succesfully");

		} catch (ClassNotFoundException ex) {

			ex.printStackTrace();

			System.out.println("ClassNotFoundException!");

		} catch (SQLException ex) {

			ex.printStackTrace();

			System.out.println("Connection failed!");
		}
	}

	public void conClose() {
		try {
			if (con!=null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception ex) {
		}
	}

	public void executeUpdate(String query) {
	
		try {
			st = con.createStatement();
		
			st.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public ResultSet executeQuery(String query) {
		
		
		try {
			st = con.createStatement();

			rs = st.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
		
		return rs;
	}

}
