/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoring;

import java.time.LocalDate;
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
public class MainClassTest {
    
    public MainClassTest() {
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
    public void testCalculateDifference() {
        System.out.println("calculateDifference");
        LocalTime sampleTime = null;
        LocalDate sampleDate = null;
        boolean over23 = false;
        boolean over24 = false;
        int sampNum = 0;
        MainClass instance = new MainClass();
        instance.calculateDifference(sampleTime, sampleDate, over23, over24, sampNum);
    }


    @Test
    public void testTimeToString() {
        System.out.println("timeToString");
        int pTime = 0;
        MainClass instance = new MainClass();
        String expResult = "";
        String result = instance.timeToString(pTime);
        assertEquals(expResult, result);
    }
    
}
