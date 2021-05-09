package SampleMonitoringGUI;

import SampleMonitoring.ReadWrite;
import SampleMonitoringGUI.CsvData;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;



public class CsvDBConnection{
    
    String url = "jdbc:sqlite:SampleData.db";
    Connection con;

    public static Connection CsvDBConnection(){
        ReadWrite.readFile();
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:SampleData.db");
            System.out.println("Connected");
            return con;
        }catch(ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage() + " ");
            return null;
        }
    }

    public boolean insertDataIntoDatabase(ArrayList<CsvData> dataList) throws SQLException{
        if(con != null){
            if(con.isClosed()){
                con=CsvDBConnection();
            }
            else{
                con.close();
                con=CsvDBConnection();
            }   
        }
        else{
            con=CsvDBConnection();
        }
        
        PreparedStatement statement = null;
        PreparedStatement newStatement = null;
        try{
            //con.setAutoCommit(false);
            for(CsvData data : dataList){
                String query = "INSERT INTO csv_table (SampleNumber, Date, Department, Tests, RequestTime, "
                    + " StartTime ) "
                    + "VALUES(?,?,?,?,?,?) ";

                statement = con.prepareStatement(query);
                statement.setInt(1, data.getSampleNumber());
                statement.setString(2, data.getDate());
                statement.setString(3, data.getDepartment());
                statement.setString(4, data.getTests());
                statement.setString(5, data.getRequesttime());
                statement.setString(6, data.getStarttime());
                try {
                    statement.execute();
                }catch(java.sql.SQLException e ){
                    //if key already exists 
                    String newQuery = "UPDATE csv_table SET Date = ?,Department = ?,Tests = ?,RequestTime = ?, "
                    + " StartTime = ? WHERE SampleNumber = ? ";
                newStatement = con.prepareStatement(newQuery);
                newStatement.setInt(6, data.getSampleNumber());
                newStatement.setString(1, data.getDate());
                newStatement.setString(2, data.getDepartment());
                newStatement.setString(3, data.getTests());
                newStatement.setString(4, data.getRequesttime());
                newStatement.setString(5, data.getStarttime());
                newStatement.execute();

                }
            }
            if(statement!= null){
                statement.close();
            }
            if(newStatement!=null){
                newStatement.close();
            }

            con.close();
            
            System.out.println("con closed returned true");
            return true;
            } catch(SQLException e){
                e.printStackTrace();
            }
        if(statement!= null){
            statement.close();
        }
        if(newStatement!=null){
            newStatement.close();
        }

        con.close();
        System.out.println("con closed returned false");

        return false;
    }

    public void compareCsvToDatabase() throws SQLException, IOException{
        ArrayList<CsvData> dataListFromDB = getDataFromDatabase();
        ArrayList<CsvData> dataListFromCSV = GUIDashboard.readAndReturnFromCSV();
        ArrayList<CsvData> newDataList = new ArrayList<CsvData>();
        
        for(int i = 0; i < dataListFromCSV.size(); i++){
            boolean foundInDB = false;
            
            CsvData csv = dataListFromCSV.get(i);
            int csvSample = csv.getSampleNumber();
            int dbSample;
            for (int j = 0; j < dataListFromDB.size(); j++){
                CsvData db = dataListFromDB.get(j);
                dbSample = db.getSampleNumber();
                if (dbSample == csvSample){
                    foundInDB = true;
                    newDataList.add(db);
                    break;
                }
            }
            if (foundInDB == false){
                newDataList.add(csv);
            }
        }       
        deleteAllFromDatabase();
        insertDataIntoDatabase(newDataList);
        System.out.println("New Size is : " + newDataList.size());       
    }
    
    public ArrayList<CsvData> getDataFromDatabase() throws SQLException{
        ArrayList<CsvData> dataList = new ArrayList<CsvData>();
        String query = "select * from csv_table ";
        Statement statement = null;
        if(con != null ){
            if(con.isClosed()){
                con=CsvDBConnection();
            }
            else{
                con.close();
                con=CsvDBConnection();
            }   
        }
        else{
            con=CsvDBConnection();
        }
        
        try{
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()){
                CsvData data = new CsvData();

                data.setSampleNumber(rs.getInt("SampleNumber"));
                data.setDate(rs.getString("Date"));
                data.setDepartment(rs.getString("Department"));
                data.setTests(rs.getString("Tests"));
                data.setRequesttime(rs.getString("RequestTime"));
                data.setStarttime(rs.getString("StartTime"));
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
        if(con != null ){
            if(con.isClosed()){
                con=CsvDBConnection();
            }
            else{
                con.close();
                con=CsvDBConnection();
            }   
        }
        else{
            con=CsvDBConnection();
        }
        
        String query = "DELETE from csv_table";
        Statement statement = null;
        int res = 0;
        try{
            statement = con.createStatement();
            
            res = statement.executeUpdate(query);
            statement.close();
            con.close();
        }catch(SQLException e){
            e.printStackTrace();
        } 
        if(statement != null){
            statement.close();
        }
        con.close();
        
        return res;
    }
}
