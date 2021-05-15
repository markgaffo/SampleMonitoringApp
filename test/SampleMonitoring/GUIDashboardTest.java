/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoring;

import java.time.LocalTime;
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


    @Test
    public void testClock() {
        System.out.println("clock");
        GUIDashboard instance = new GUIDashboard();
        instance.clock();
        LocalTime expResult = LocalTime.now();
        //assertEquals(expResult, instance.clock());
    }


    @Test
    public void testProgressTime() {
        System.out.println("progressTime");
        GUIDashboard instance = new GUIDashboard();
        instance.progressTime();
    }

    @Test
    public void testDelayCalculation() {
        System.out.println("delayCalculation");
        GUIDashboard instance = new GUIDashboard();
        instance.delayCalculation();
    }


    @Test
    public void testRenderChart() throws Exception {
        System.out.println("renderChart");
        String from = "";
        String to = "";
        GUIDashboard instance = new GUIDashboard();
        instance.renderChart(from, to);
    }

    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        GUIDashboard.main(args);
    }
    
}
