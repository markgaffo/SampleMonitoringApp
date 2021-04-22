package SampleMonitoring;

import java.sql.*;
import javax.swing.JOptionPane;

public class DataConnection {
    Connection conn = null;
    public static Connection ConnectDb(){
        try{
            
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Mark\\Dropbox\\Final year\\Project\\netbeans projects\\MidPointProject\\SampleData.db");
            return conn;
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
            return null;
            
        }          
    } 
            
           
}
