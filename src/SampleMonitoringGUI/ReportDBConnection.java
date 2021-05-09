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
    
    public boolean insertDataIntoDatabase(ArrayList<ReportData> dataList) throws SQLException{
        if(con != null){
            if(con.isClosed()){
                con=ReportDBConnection();
            }
            else{
                con.close();
                con=ReportDBConnection();
            }   
        }
        else{
            con=ReportDBConnection();
        }
        PreparedStatement statement = null;
        try{
            //con.setAutoCommit(false);
            for(ReportData data : dataList){
                String query = "INSERT INTO report_table (SampleNumber, StartDate, Department, Tests, RequestTime, "
                    + " StartTime, ReportedDate, ReportedTime, FinishedTime ) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";

                statement = con.prepareStatement(query);
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
            
            con.close();
            return true;
            }catch(SQLException e){
                e.printStackTrace();
            }
        if(statement!= null){
            statement.close();
        }
        con.close();
        return false;
    }

    public ArrayList<ReportData> getDataFromDatabase() throws SQLException{
        ArrayList<ReportData> dataList = new ArrayList<ReportData>();

        String query = "select * from report_table ";
        Statement statement = null;
        if(con != null){
            if(con.isClosed()){
                con=ReportDBConnection();
            }
            else{
                con.close();
                con=ReportDBConnection();
            }   
        }
        else{
            con=ReportDBConnection();
        }
        try{
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                ReportData data = new ReportData();

                data.setSampleNumber(rs.getInt("SampleNumber"));
                data.setDate(rs.getString("StartDate"));
                data.setDepartment(rs.getString("Department"));
                data.setTests(rs.getString("Tests"));
                data.setRequestTime(rs.getString("RequestTime"));
                data.setStartTime(rs.getString("StartTime"));
                data.setReportedDate(rs.getString("ReportedDate"));
                data.setReportedTime(rs.getString("ReportedTime"));
                data.setFinishedTime(rs.getString("FinishedTime"));
                dataList.add(data);
            }
            statement.close();
            con.close();
            return dataList;
        }catch (SQLException e){
            e.printStackTrace();
        } 
        if(statement != null){
            statement.close();
        }
        con.close();
        return null;
    }
    
    public ArrayList<ReportData> getDateDataFromDatabase(String from, String to) throws SQLException{
        
        ArrayList<ReportData> dataList = new ArrayList<ReportData>();
        String query = "select * from report_table WHERE "
                    + "StartDate >= '"+from+"' "
                    + "and StartDate <= '"+to+"' ";
        Statement statement = null;
        
        if(con != null ){
            if(con.isClosed()){
                con=ReportDBConnection();
            }
            else{
                con.close();
                con=ReportDBConnection();
            }   
        }
        else{
            con=ReportDBConnection();
        }
        
        try{
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                ReportData data = new ReportData();
                data.setSampleNumber(rs.getInt("SampleNumber"));
                data.setDate(rs.getString("StartDate"));
                data.setDepartment(rs.getString("Department"));
                data.setTests(rs.getString("Tests"));
                data.setRequestTime(rs.getString("RequestTime"));
                data.setStartTime(rs.getString("StartTime"));
                data.setReportedDate(rs.getString("ReportedDate"));
                data.setReportedTime(rs.getString("ReportedTime"));
                data.setFinishedTime(rs.getString("FinishedTime"));
                dataList.add(data);
            }
            statement.close();
            con.close();
            return dataList;
        }catch (SQLException e){
            e.printStackTrace();
        } 
        if(statement != null){
            statement.close();
        }
        con.close();
        return null;
    }

    public int deleteAllFromDatabase() throws SQLException{
        if(con != null){
            if(con.isClosed()){
                con=ReportDBConnection();
            }
            else{
                con.close();
                con=ReportDBConnection();
            }   
        }
        else{
            con=ReportDBConnection();
        }
        String query = "DELETE from report_table";
        Statement statement = null;
        try{
            statement = con.createStatement();
            return statement.executeUpdate(query);
        }catch(SQLException e){
            e.printStackTrace();
        } 
        if(statement != null){
            statement.close();
        }
        con.close();
        return 0;
    }
}
