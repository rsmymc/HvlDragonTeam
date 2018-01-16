package rovingyDB;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Statement;

public class MySQLDB {

	Connection con = null;
	Statement st = null;
	ResultSet rs = null;

	public MySQLDB(){
		
	}
	
	public void connectToDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			String url = "jdbc:mysql://37.148.204.23/nahnuh14";

			String username = "nahnuh14";

			String password = "naH11Nuh12!";

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

	public ResultSet executeStatament(String query) throws SQLException{
	
		st = con.createStatement();

		
		rs = st.executeQuery(query);
		
		return rs;
	}
}
