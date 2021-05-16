/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoringGUI;

import java.sql.Connection;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mark
 */
public class ReportDBConnectionTest {
    
    public ReportDBConnectionTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testReportDBConnection() {
        System.out.println("ReportDBConnection");
        Connection expResult = null;
        Connection result = ReportDBConnection.ReportDBConnection();
        assertEquals(expResult, result);
    }


    @Test
    public void testInsertDataIntoDatabase() throws Exception {
        System.out.println("insertDataIntoDatabase");
        ArrayList<ReportData> dataList = null;
        ReportDBConnection instance = new ReportDBConnection();
        boolean expResult = true;
        boolean result = instance.insertDataIntoDatabase(dataList);
        assertEquals(expResult, result);
    }


    @Test
    public void testGetDataFromDatabase() throws Exception {
        System.out.println("getDataFromDatabase");
        ReportDBConnection instance = new ReportDBConnection();
        ArrayList<ReportData> expResult = null;
        ArrayList<ReportData> result = instance.getDataFromDatabase();
        assertEquals(expResult, result);

    }

    @Test
    public void testGetDateDataFromDatabase() throws Exception {
        System.out.println("getDateDataFromDatabase");
        String from = "";
        String to = "";
        ReportDBConnection instance = new ReportDBConnection();
        ArrayList<ReportData> expResult = null;
        ArrayList<ReportData> result = instance.getDateDataFromDatabase(from, to);
        assertEquals(expResult, result);
    }

    @Test
    public void testDeleteAllFromDatabase() throws Exception {
        System.out.println("deleteAllFromDatabase");
        ReportDBConnection instance = new ReportDBConnection();
        int expResult = 0;
        int result = instance.deleteAllFromDatabase();
        assertEquals(expResult, result);
    }

    @Test
    public void testReadAndWriteCSVFileToReportDatabase() {
        System.out.println("readAndWriteCSVFileToReportDatabase");
        String fileName = "";
        boolean expResult = false;
        boolean result = ReportDBConnection.readAndWriteCSVFileToReportDatabase(fileName);
        assertEquals(expResult, result);
    }
    
}
