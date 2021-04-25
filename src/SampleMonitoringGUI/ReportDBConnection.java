package SampleMonitoringGUI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import SampleMonitoringGUI.ReportData;

public class ReportDBConnection{
    String url = "jdbc:sqlite:ReportData.db";
    Connection con;
    
    public static Connection ReportDBConnection(){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:ReportData.db");
            System.out.println("Connected");
            return con;
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage() + " ");
            return null;
        }
    }
    
    public boolean insertDataIntoDatabase(ArrayList<ReportData> dataList){
        con=ReportDBConnection();
        try{
            //con.setAutoCommit(false);
            for(ReportData data : dataList){
                String query = "INSERT INTO report_table (SampleNumber, Date, Department, Tests, RequestTime, "
                    + " StartTime, ReportedDate, ReportedTime, FinishedTime ) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";

                PreparedStatement statement = con.prepareStatement(query);
                statement.setInt(1, data.getSampleNumber());
                statement.setString(2, data.getDate());
                statement.setString(3, data.getDepartment());
                statement.setString(4, data.getTests());
                statement.setString(5, data.getRequestTime());
                statement.setString(6, data.getStartTime());
                statement.setString(7, data.getReportedDate());
                statement.setString(8, data.getReportedTime());
                statement.setString(9, data.getFinishedTime());
                statement.execute();
            }
            return true;
            }catch(SQLException e){
                e.printStackTrace();
            }
        return false;
    }

    public ArrayList<ReportData> getDataFromDatabase(){
        ArrayList<ReportData> dataList = new ArrayList<ReportData>();

        String query = "select * from report_table ";
        Statement statement;
        con=ReportDBConnection();
        try{
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                ReportData data = new ReportData();

                data.setSampleNumber(rs.getInt("SampleNumber"));
                data.setDate(rs.getString("Date"));
                data.setDepartment(rs.getString("Department"));
                data.setTests(rs.getString("Tests"));
                data.setRequestTime(rs.getString("RequestTime"));
                data.setStartTime(rs.getString("StartTime"));
                data.setReportedDate(rs.getString("ReportedDate"));
                data.setReportedTime(rs.getString("ReportedTime"));
                data.setFinishedTime(rs.getString("FinishedTime"));
                dataList.add(data);
            }
            return dataList;
        }catch (SQLException e){
            e.printStackTrace();
        } 
        return null;
    }

    public int deleteAllFromDatabase(){
        con=ReportDBConnection();
        String query = "DELETE from report_table";
        Statement statement;
        try{
            statement = con.createStatement();
            return statement.executeUpdate(query);
        }catch(SQLException e){
            e.printStackTrace();
        } 
        return 0;
    }
}
