package SampleMonitoringGUI;
import SampleMonitoring.GUIDashboard;
import static SampleMonitoring.GUIDashboard.repCon;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import SampleMonitoringGUI.ReportData;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        String query = "INSERT INTO report_table (SampleNumber, StartDate, Department, Tests, RequestTime, "
                    + " StartTime, ReportedDate, ReportedTime, FinishedTime ) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";
        
        statement = con.prepareStatement(query);
        try{
            //con.setAutoCommit(false);
            for(ReportData data : dataList){

                statement.setInt(1, data.getSampleNumber());
                statement.setString(2, data.getDate());
                statement.setString(3, data.getDepartment());
                statement.setString(4, data.getTests());
                statement.setString(5, data.getRequestTime());
                statement.setString(6, data.getStartTime());
                statement.setString(7, data.getReportedDate());
                statement.setString(8, data.getReportedTime());
                statement.setString(9, data.getFinishedTime());
                statement.addBatch();
            }
            statement.executeBatch();
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
    
    public static boolean readAndWriteCSVFileToReportDatabase(String fileName) {    
        try{
            FileReader filereader = new FileReader(fileName); 
            CSVReader csvReader = new CSVReader(filereader); 
            String[] nextRecord; 
            ArrayList<ReportData> rdataList = new ArrayList<ReportData>();
            
            while((nextRecord = csvReader.readNext()) != null){
                //System.out.println(nextRecord[0]);
                ReportData rdata = new ReportData();
                rdata.setSampleNumber(Integer.parseInt(nextRecord[0]));
                //add year
                String sdate = nextRecord[1];
                sdate = sdate.substring(0, sdate.length()-2)+ "20" + sdate.substring(sdate.length()-2, sdate.length());
                rdata.setDate(sdate);
                rdata.setDepartment(nextRecord[2]);
                rdata.setTests(nextRecord[3]);
                rdata.setRequestTime(nextRecord[4]);
                //add time
                String stime = nextRecord[5];
                if(stime.length() == 1){
                    stime = "00:0" + stime;
                }else if(stime.length() == 2){
                    stime = "00:" + stime;
                }else if(stime.length() == 3){
                    stime = "0"+stime.substring(0, stime.length() - 2) + ":" + stime.substring(stime.length() - 2, stime.length());
                }else{
                    stime = stime.substring(0, stime.length() - 2) + ":" + stime.substring(stime.length() - 2, stime.length());
                }
                rdata.setStartTime(stime);
                //add year
                String rdate = nextRecord[6];
                rdate = rdate.substring(0, rdate.length()-2)+ "20" + rdate.substring(rdate.length()-2, rdate.length());
                rdata.setReportedDate(rdate);
                //add time
                String reptime = nextRecord[7];
                if(reptime.length() == 0){
                    reptime = "00:00" + reptime;
                }else if(reptime.length() == 1){
                    reptime = "00:0" + reptime;
                }else if(reptime.length() == 2){
                    reptime = "00:" + reptime;
                }else if(reptime.length() == 3){
                    reptime = "0"+reptime.substring(0, reptime.length() - 2) + ":" + reptime.substring(reptime.length() - 2, reptime.length());
                }else{
                    reptime = reptime.substring(0, reptime.length() - 2) + ":" + reptime.substring(reptime.length() - 2, reptime.length());
                }
                rdata.setReportedTime(reptime);
                String fintime = nextRecord[8];
                if(fintime.length() == 0){
                    fintime = "00:00" + fintime;
                }else if(fintime.length() == 1){
                    fintime = "00:0" + fintime;
                }else if(fintime.length() == 2){
                    fintime = "00:" + fintime;
                }else if(fintime.length() == 3){
                    fintime = "0"+fintime.substring(0, fintime.length() - 2) + ":" + fintime.substring(fintime.length() - 2, fintime.length());
                }else{
                    fintime = fintime.substring(0, fintime.length() - 2) + ":" + fintime.substring(fintime.length() - 2, fintime.length());
                }
                rdata.setFinishedTime(fintime);
                rdataList.add(rdata);
            }
        System.out.println("Size : " + rdataList.size());
        csvReader.close();
        return repCon.insertDataIntoDatabase(rdataList);
        }catch(IOException e){
            e.printStackTrace();
        }catch(NumberFormatException ex){
            ex.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
