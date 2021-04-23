package SampleMonitoringGUI;

import SampleMonitoringGUI.CsvData;
import java.sql.*;
import javax.swing.*;
import java.util.ArrayList;



public class CsvDBConnection {
    
    String url = "jdbc:sqlite:SampleData.db";
    Connection con;

    public static Connection CsvDBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:SampleData.db");
            System.out.println("Connected");
            return con;
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage() + " ");
            return null;
        }
    }

    public boolean insertDataIntoDatabase(ArrayList<CsvData> dataList) {
        con=CsvDBConnection();
        try {
            for (CsvData data : dataList) {
                String query = "INSERT INTO csv_table (SampleNumber, Date, Department, Tests, RequestTime, "
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

        String query = "select * from csv_table ";
        Statement statement;
        con=CsvDBConnection();
        try{
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                CsvData data = new CsvData();

                data.setSampleNumber(rs.getInt("SampleNumber"));
                data.setDate(rs.getString("Date"));
                data.setDepartment(rs.getString("Department"));
                data.setTests(rs.getString("Tests"));
                data.setRequesttime(rs.getString("RequestTime"));
                data.setStarttime(rs.getString("StartTime"));
                dataList.add(data);
            }
            return dataList;
        }catch (SQLException e){
            e.printStackTrace();
        } 
        return null;
    }

    public int deleteAllFromDatabase() {
        con=CsvDBConnection();
        String query = "DELETE from csv_table";
        Statement statement;
        try {
            statement = con.createStatement();
            return statement.executeUpdate(query);
        }catch(SQLException e){
            e.printStackTrace();
        } 
        return 0;
    }
}
