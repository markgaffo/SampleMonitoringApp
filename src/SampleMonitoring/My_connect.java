/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoring;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mark
 */
public class My_connect {
    private static String servername = "localhost"; 
    private static String username = "root"; 
    private static String dbname = "user_db"; 
    private static int portnum = 3306; 
    private static String password = ""; 
    
    public static Connection getConnection(){
        
        Connection mycon = null;
        
        MysqlDataSource datasource = new MysqlDataSource();
        
        datasource.setServerName(servername);
        datasource.setUser(username);
        datasource.setPassword(password);
        datasource.setDatabaseName(dbname);
        datasource.setPortNumber(portnum);
        
        try {
            mycon = datasource.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(" Get connection ->"+My_connect.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mycon;
    }
}
