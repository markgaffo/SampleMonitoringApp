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
public class CsvDBConnectionTest {
    
    public CsvDBConnectionTest() {
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
    public void testCsvDBConnection() {
        System.out.println("CsvDBConnection");
        Connection expResult = null;
        Connection result = CsvDBConnection.CsvDBConnection();
        assertEquals(expResult, result);

    }


    @Test
    public void testInsertDataIntoDatabase() throws Exception {
        System.out.println("insertDataIntoDatabase");
        ArrayList<CsvData> dataList = null;
        CsvDBConnection instance = new CsvDBConnection();
        boolean expResult = false;
        boolean result = instance.insertDataIntoDatabase(dataList);
        assertEquals(expResult, result);
    }

    
    @Test
    public void testCompareCsvToDatabase() throws Exception {
        System.out.println("compareCsvToDatabase");
        CsvDBConnection instance = new CsvDBConnection();
        instance.compareCsvToDatabase();
    }


    @Test
    public void testGetDataFromDatabase() throws Exception {
        System.out.println("getDataFromDatabase");
        CsvDBConnection instance = new CsvDBConnection();
        ArrayList<CsvData> expResult = null;
        ArrayList<CsvData> result = instance.getDataFromDatabase();
        assertEquals(expResult, result);

    }

    @Test
    public void testDeleteAllFromDatabase() throws Exception {
        System.out.println("deleteAllFromDatabase");
        CsvDBConnection instance = new CsvDBConnection();
        int expResult = 0;
        int result = instance.deleteAllFromDatabase();
        assertEquals(expResult, result);
    }


    @Test
    public void testReadAndReturnFromCSV() throws Exception {
        System.out.println("readAndReturnFromCSV");
        ArrayList<CsvData> expResult = null;
        ArrayList<CsvData> result = CsvDBConnection.readAndReturnFromCSV();
        assertEquals(expResult, result);
    }


    @Test
    public void testReadAndWriteCSVFileToDatabase() throws Exception {
        System.out.println("readAndWriteCSVFileToDatabase");
        boolean expResult = false;
        boolean result = CsvDBConnection.readAndWriteCSVFileToDatabase();
        assertEquals(expResult, result);
    }
    
}
