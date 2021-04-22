package SampleMonitoringGUI;

import SampleMonitoringGUI.CsvData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;



public class CsvDBConnection {
    String url = "jdbc:sqlite:SampleData.db";

	Connection con;

	public CsvDBConnection() {
            try {
		Class.forName("org.sqlite.JDBC");
		con = DriverManager.getConnection(url);
		System.out.println("Connected");
            } catch (ClassNotFoundException | SQLException e) {
		System.out.println(e.getMessage() + " ");
            }
	}

	public boolean insertDataIntoDatabase(ArrayList<CsvData> dataList) {
		try {
			for (CsvData data : dataList) {
				String query = "INSERT INTO SampleData (SampleNumber, Date, Department, Tests, RequestTime, "
						+ " StartTime ) "
						+ "VALUES(?,?,?,?,?,?)";

				PreparedStatement statement = con.prepareStatement(query);
				statement.setInt(1, data.getSampleNumber());
				statement.setString(2, data.getDate());
				statement.setString(3, data.getDepartment());
				statement.setString(4, data.getTests());
				statement.setString(5, data.getRequesttime());
				statement.setString(6, data.getStarttime());
				statement.execute();

			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<CsvData> getDataFromDatabase(int offset) {
        ArrayList<CsvData> dataList = new ArrayList<CsvData>();
		
		String query = "select * from SampleData ";

		Statement statement;
		try {
			statement = con.createStatement();
			ResultSet rs = statement.executeQuery(query);
			while (rs.next()) {
				CsvData data = new CsvData();

				data.setSampleNumber(rs.getInt("sample_number"));
				data.setDate(rs.getString("date"));
				data.setDepartment(rs.getString("department"));
				data.setTests(rs.getString("tests"));
				data.setRequesttime(rs.getString("request_time"));
				data.setStarttime(rs.getString("start_time"));

				dataList.add(data);
			}
			return dataList;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return null;

	}

	public int deleteAllFromDatabase() {
		String query = "DELETE from SampleData";

		Statement statement;
		try {
			statement = con.createStatement();
			
			return statement.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		
		return 0;
	}
}
