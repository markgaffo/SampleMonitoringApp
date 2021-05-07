/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoringGUI;

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
public class GUIDashboardTest {
    
    public GUIDashboardTest() {
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

    /**
     * Test of clock method, of class GUIDashboard.
     */
    @Test
    public void testClock() {
        System.out.println("clock");
        GUIDashboard instance = new GUIDashboard();
        instance.clock();
    }

    /**
     * Test of progressTime method, of class GUIDashboard.
     */
    @Test
    public void testProgressTime() {
        System.out.println("progressTime");
        GUIDashboard instance = new GUIDashboard();
        instance.progressTime();
    }

    /**
     * Test of readAndWriteCSVFileToReportDatabase method, of class GUIDashboard.
     */
    @Test
    public void testReadAndWriteCSVFileToReportDatabase() {
        System.out.println("readAndWriteCSVFileToReportDatabase");
        String fileName = "";
        boolean expResult = false;
        boolean result = GUIDashboard.readAndWriteCSVFileToReportDatabase(fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of main method, of class GUIDashboard.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        GUIDashboard.main(args);
    }

    /**
     * Test of readAndReturnFromCSV method, of class GUIDashboard.
     */
    @Test
    public void testReadAndReturnFromCSV() throws Exception {
        System.out.println("readAndReturnFromCSV");
        ArrayList<CsvData> expResult = null;
        ArrayList<CsvData> result = GUIDashboard.readAndReturnFromCSV();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
