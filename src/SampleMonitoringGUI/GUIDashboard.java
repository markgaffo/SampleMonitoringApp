package SampleMonitoringGUI;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;
import com.opencsv.CSVReader;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import SampleMonitoring.CSVFilePath;
import SampleMonitoring.ReadWrite;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.entity.StandardEntityCollection;

public class GUIDashboard extends javax.swing.JFrame {

    public static CsvDBConnection csvCon = new CsvDBConnection();
    public static ReportDBConnection repCon = new ReportDBConnection();
    DefaultTableModel model;

    Connection csvConn = null;
    Connection repConn = null;
    PreparedStatement prep = null;
    ResultSet rs = null;
    
    int xMouse;
    int yMouse;
    public static String csvFile;
    public static String line ="";
    public static String sampleNum;
    List<CsvData> lstRecords = null;
    
    Timer timer = new Timer();
    TimerTask myTask = new TimerTask() {
        @Override
        public void run() {
            System.out.println("One minute has passed");

            try{
                GUIDashboard.csvCon.compareCsvToDatabase();
                UpdateTable();
                doTests();
                System.out.print("UPDATED TABLE");

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    
    public GUIDashboard() {
        initComponents();
        clock();
        setTableParmas();
        UpdateTable();   
        timer.schedule(myTask, 10000, 60000);
    }
    
    private void setTableParmas(){
        sampleTable.getColumnModel().getColumn(0).setHeaderValue("Sample Number");
        sampleTable.getColumnModel().getColumn(1).setHeaderValue("Date");
        sampleTable.getColumnModel().getColumn(2).setHeaderValue("Department");
        sampleTable.getColumnModel().getColumn(3).setHeaderValue("Tests");
        sampleTable.getColumnModel().getColumn(4).setHeaderValue("Request Time");
        sampleTable.getColumnModel().getColumn(5).setHeaderValue("Start Time");
        
        sampleTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        sampleTable.getColumnModel().getColumn(1).setPreferredWidth(25);
        sampleTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        sampleTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        sampleTable.getColumnModel().getColumn(4).setPreferredWidth(20);
        sampleTable.getColumnModel().getColumn(5).setPreferredWidth(20);
        
        sampleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        sampleTable.getTableHeader().setOpaque(false);
        sampleTable.getTableHeader().setBackground(new Color(32, 136, 203));
        sampleTable.getTableHeader().setForeground(new Color(255, 255, 255));
        sampleTable.setRowHeight(25);
    }
    
    public void clock(){
        Thread th = new Thread(){
            public void run(){
                try{
                    while(true){
                        Calendar cl = Calendar.getInstance();

                        SimpleDateFormat sdf24h = new SimpleDateFormat("HH:mm:ss");
                        Date dat = cl.getTime();
                        String time24 = sdf24h.format(dat);
                        
                        SimpleDateFormat sdfday = new SimpleDateFormat("dd/MM/yyyy");
                        String date = sdfday.format(dat);
                        
                        timeTf.setText("Time: "+time24);
                        dateTf.setText("Current Date: "+date);
                        sleep(1000);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        };
        th.start();
    }
    
    public void progressTime(){
        Date systemTimeD = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String stringtime = delayTf.getText();
        
        try {
            Date time = sdf.parse(stringtime);
            long timeDifMin = systemTimeD.getMinutes()-time.getMinutes();
            long timeDifHour = systemTimeD.getHours()-time.getHours();  
            if(timeDifHour >= 1 && timeDifHour <= 22 ){
                timeDifMin = (systemTimeD.getMinutes()+60)-time.getMinutes();
                if(timeDifMin >= 41){
                    progressBar.setValue(99);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 40 && timeDifMin >= 36){
                    progressBar.setValue(90);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 35 && timeDifMin >= 32){
                    progressBar.setValue(80);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 31 && timeDifMin >= 28){
                    progressBar.setValue(70);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 27 && timeDifMin >= 24){
                    progressBar.setValue(60);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 23 && timeDifMin >= 20){
                    progressBar.setValue(50);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 19 && timeDifMin >= 16){
                    progressBar.setValue(40);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 15 && timeDifMin >= 12){
                    progressBar.setValue(30);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 11 && timeDifMin >= 8){
                    progressBar.setValue(20);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 7 && timeDifMin >= 4){
                    progressBar.setValue(10);
                    progressBar.setStringPainted(true); 
                }else{
                    progressBar.setValue(1);
                    progressBar.setStringPainted(true); 
                }
            }else if(timeDifHour == -23){
                timeDifMin = systemTimeD.getMinutes()-(time.getMinutes()+60);
                if(timeDifMin > 41){
                    progressBar.setValue(1);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 40 && timeDifMin >= 36){
                    progressBar.setValue(10);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 35 && timeDifMin <= 32){
                    progressBar.setValue(20);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 31 && timeDifMin <= 28){
                    progressBar.setValue(30);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 27 && timeDifMin <= 24){
                    progressBar.setValue(40);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 23 && timeDifMin <= 20){
                    progressBar.setValue(50);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 19 && timeDifMin <= 16){
                    progressBar.setValue(60);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 15 && timeDifMin <= 12){
                    progressBar.setValue(70);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 11 && timeDifMin <= 8){
                    progressBar.setValue(80);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin >= 7 && timeDifMin <= 4){
                    progressBar.setValue(90);
                    progressBar.setStringPainted(true); 
                }else{
                    progressBar.setValue(99);
                    progressBar.setStringPainted(true); 
                }
                
            }else if(timeDifHour == 23){
                progressBar.setValue(1);
                progressBar.setStringPainted(true); 
            }else if(timeDifHour < 0){
                progressBar.setValue(1);
                progressBar.setStringPainted(true); 
            }else{
                if(timeDifMin > 41){
                    progressBar.setValue(99);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 40 && timeDifMin >= 36){
                    progressBar.setValue(90);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 35 && timeDifMin >= 32){
                    progressBar.setValue(80);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 31 && timeDifMin >= 28){
                    progressBar.setValue(70);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 27 && timeDifMin >= 24){
                    progressBar.setValue(60);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 23 && timeDifMin >= 20){
                    progressBar.setValue(50);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 19 && timeDifMin >= 16){
                    progressBar.setValue(40);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 15 && timeDifMin >= 12){
                    progressBar.setValue(30);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 11 && timeDifMin >= 8){
                    progressBar.setValue(20);
                    progressBar.setStringPainted(true); 
                }else if(timeDifMin <= 7 && timeDifMin >= 4){
                    progressBar.setValue(10);
                    progressBar.setStringPainted(true); 
                }else{
                    progressBar.setValue(1);
                    progressBar.setStringPainted(true); 
                } 
            } 
        }catch(ParseException ex){
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void UpdateTable(){
        if(csvConn != null){
            try {
                if(csvConn.isClosed()){
                    csvConn=CsvDBConnection.CsvDBConnection();
                }
                else{
                    csvConn.close();
                    csvConn=CsvDBConnection.CsvDBConnection();
                }
            }catch(SQLException ex){
                Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            csvConn=CsvDBConnection.CsvDBConnection();
        }
        
        try {
            String sql = "select * from csv_table";
            prep=csvConn.prepareStatement(sql);
            rs=prep.executeQuery();
            sampleTable.setModel(DbUtils.resultSetToTableModel(rs));
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }finally{
            try{
                rs.close();
                prep.close();
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
        }
        try {
            String sql = "select count(SampleNumber) from csv_table";
            prep=csvConn.prepareStatement(sql);
            rs=prep.executeQuery();
            if(rs.next()){
                String sum = rs.getString("count(SampleNumber)");
                totalsampleTf.setText(sum);
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }finally{
            try{
                rs.close();
                prep.close();
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }

    
    public void doTests(){
        String tests1 = testsGroup1Tf.getText();
        String tests2 = testsGroup2Tf.getText();
        String tests3 = testsGroup3Tf.getText();
        tests1 = tests1.replaceAll("\\s", "");
        tests2 = tests2.replaceAll("\\s", "");
        tests3 = tests3.replaceAll("\\s", "");

        String[] testsGroup1 = tests1.split(",");
        String[] testsGroup2 = tests2.split(",");
        String[] testsGroup3 = tests3.split(",");
        
        tests1 = null;
        tests2 = null;
        tests3 = null;
        System.gc();
        ArrayList<String> timeList = new ArrayList<>();
        
        try{
            ArrayList<CsvData> dataList = csvCon.getDataFromDatabase();
            String test1 = timeGroup1Tf.getText();
            String test2 = timeGroup2Tf.getText();
            String test3 = timeGroup3Tf.getText();
            String defaultTest = defaultAlertTimeTf.getText();
            String newTimeString;
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));    
            test1 = timeToString(Integer.parseInt(test1));
            test2 = timeToString(Integer.parseInt(test2));
            test3 = timeToString(Integer.parseInt(test3));
            defaultTest = timeToString(Integer.parseInt(defaultTest));
            LocalTime testGroup1Time = LocalTime.parse(test1);
            LocalTime testGroup2Time = LocalTime.parse(test2);
            LocalTime testGroup3Time = LocalTime.parse(test3);
            LocalTime defaultTestTime = LocalTime.parse(defaultTest);
            
            int hours = 0, mins = 0; 
            String minsString = null;
            for(CsvData data : dataList){
                String old = data.getStarttime();
                LocalTime oldSampleTime = LocalTime.parse(old);
                LocalTime systemTime = LocalTime.now();
                
                String testsString = data.getTests();
                String[] tests = testsString.split(",");
                
                String posTest1 = "";
                String posTest2 = "";
                String posTest3 = "";
                
                //checking for tests set in groups and sample
                for(String dataTest : tests){
                    boolean found = false;
                    for(String testCase : testsGroup1){
                        if(dataTest.equals(testCase)){
                            found = true;
                            posTest1 = testCase;
                            break;
                        }
                    }
                    if(found){
                        break;
                    }
                }
                
                for(String dataTest : tests){
                    boolean found = false;
                    for(String testCase : testsGroup2){
                        if(dataTest.equals(testCase)){
                            found = true;
                            posTest2 = testCase;

                            break;
                        }
                    }
                    if(found){
                        break;
                    }
                }
                
                for(String dataTest : tests){
                    boolean found = false;
                    for(String testCase : testsGroup3){
                        if(dataTest.equals(testCase)){
                            found = true;
                            posTest3 = testCase;
                            break;
                        }
                    }
                    if(found){
                        break;
                    }
                }
                //if a sample contains any test
                if(!posTest1.equals("") || !posTest2.equals("") || !posTest3.equals("")){
                    if(posTest1.equals(posTest2) && !posTest1.equals("")){

                        if(testGroup1Time.getHour() > testGroup2Time.getHour()){
                            hours = testGroup1Time.getHour() + oldSampleTime.getHour();
                            mins =  testGroup1Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            //to ensure format is correct 
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                            
                        }else if(testGroup1Time.getHour() == testGroup2Time.getHour()){
                            if(testGroup1Time.getMinute() > testGroup2Time.getMinute()){
                                hours = testGroup1Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup1Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);
                                
                            }else {
                                hours = testGroup2Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup2Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);
                            }
                            
                        }else {
                            hours = testGroup2Time.getHour() + oldSampleTime.getHour();
                            mins = testGroup2Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                        }
                        
                    }else if(posTest1.equals(posTest3) && !posTest1.equals("")){
                        if(testGroup1Time.getHour() > testGroup3Time.getHour()){
                            hours = testGroup1Time.getHour() + oldSampleTime.getHour();
                            mins = testGroup1Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                            
                        }else if(testGroup1Time.getHour() == testGroup3Time.getHour()){
                            if(testGroup1Time.getMinute() > testGroup3Time.getMinute()){
                                hours = testGroup1Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup1Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);
                                
                            }else {
                                hours = testGroup3Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup3Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);
                            }
                        }else {
                            hours = testGroup3Time.getHour() + oldSampleTime.getHour();
                            mins = testGroup3Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                        }
                        
                    }else if(posTest2.equals(posTest3) && !posTest2.equals("")){ 
                        if(testGroup2Time.getHour() > testGroup3Time.getHour()){
                            hours = testGroup2Time.getHour() + oldSampleTime.getHour();
                            mins = testGroup2Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                            
                        }else if(testGroup2Time.getHour() == testGroup3Time.getHour()){
                            if(testGroup2Time.getMinute() > testGroup3Time.getMinute()){
                                
                                hours = testGroup2Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup2Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);
                                
                            }else {
                                hours = testGroup3Time.getHour() + oldSampleTime.getHour();
                                mins = testGroup3Time.getMinute() + oldSampleTime.getMinute();
                                if(mins > 59){
                                    mins = mins - 60;
                                    hours = hours +1;
                                }
                                hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                                String lessThan10 = mins < 10 ? "0" : "";
                                minsString =  lessThan10 + mins + "";
                                mins = Integer.parseInt(minsString);

                                newTimeString = hours + ":" + minsString;
                                timeList.add(newTimeString);  
                            }
                            
                        }else{
                            hours = testGroup3Time.getHour() + oldSampleTime.getHour();
                            mins = testGroup3Time.getMinute() + oldSampleTime.getMinute();
                            if(mins > 59){
                                mins = mins - 60;
                                hours = hours +1;
                            }
                            hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                            String lessThan10 = mins < 10 ? "0" : "";
                            minsString =  lessThan10 + mins + "";
                            mins = Integer.parseInt(minsString);

                            newTimeString = hours + ":" + minsString;
                            timeList.add(newTimeString);
                            
                        }
                        
                    }else if(!posTest3.equals("")){
                        
                        hours = testGroup3Time.getHour() + oldSampleTime.getHour();
                        mins = testGroup3Time.getMinute() + oldSampleTime.getMinute();
                        if(mins > 59){
                            mins = mins - 60;
                            hours = hours +1;
                        }
                        hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                        String lessThan10 = mins < 10 ? "0" : "";
                        minsString =  lessThan10 + mins + "";
                        mins = Integer.parseInt(minsString);

                        newTimeString = hours + ":" + minsString;
                        timeList.add(newTimeString);
                        
                    }else if(!posTest2.equals("")){
                        
                        hours = testGroup2Time.getHour() + oldSampleTime.getHour();
                        mins = testGroup2Time.getMinute() + oldSampleTime.getMinute();
                        if(mins > 59){
                            mins = mins - 60;
                            hours = hours +1;
                        }
                        hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                        String lessThan10 = mins < 10 ? "0" : "";
                        minsString =  lessThan10 + mins + "";
                        mins = Integer.parseInt(minsString);

                        newTimeString = hours + ":" + minsString;
                        timeList.add(newTimeString);
                        
                    }else if(!posTest1.equals("")){
                        
                        hours = testGroup1Time.getHour() + oldSampleTime.getHour();
                        mins = testGroup1Time.getMinute() + oldSampleTime.getMinute();
                        if(mins > 59){
                            mins = mins - 60;
                            hours = hours +1;
                        }
                        
                        hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                        String lessThan10 = mins < 10 ? "0" : "";
                        minsString =  lessThan10 + mins + "";
                        mins = Integer.parseInt(minsString);

                        newTimeString = hours + ":" + minsString;
                        timeList.add(newTimeString);
                        
                    }
                    
                }else{
                //for samples with no specific test delay
                    
                    hours = oldSampleTime.getHour() + defaultTestTime.getHour();
                    System.out.println(hours);
                    mins = oldSampleTime.getMinute() + defaultTestTime.getMinute();
                    if (mins > 59){
                        mins = mins - 60;
                        hours = hours +1;
                    }
                    
                    hours = Integer.parseInt((hours < 10 ? "0" : "") + hours);
                    String lessThan10 = mins < 10 ? "0" : "";
                    minsString =  lessThan10 + mins + "";
                    mins = Integer.parseInt(minsString);

                    newTimeString = hours + ":" + minsString;
                    timeList.add(newTimeString); 
                }

                System.out.print("System Time is : " + (systemTime.getHour() < 10 ? "0" : "") + systemTime.getHour() + ":" + (systemTime.getMinute() < 10 ? "0" : "") + systemTime.getMinute() + " old sample time is : " + (data.getStarttime() ) + " and new sample time is :" + hours + ":" + minsString + "\n" );

                LocalDate date = null;  
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                date = LocalDate.parse(data.getDate(), dateFormatter);
                int sampNum = data.getSampleNumber();
                
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                String hoursString;
                String minutesString;
                boolean over23 = false;
                boolean over24 = false;
                
                
                if(hours < 10){
                    hoursString = "0" + hours;
                }else if(hours == 24){
                    over23 = true;
                    hoursString = "00";
                }else if(hours > 24){
                    over24 = true;
                    hoursString = "00";
                }else{
                    hoursString = hours + "";
                }

                if(mins < 10){
                    minutesString = "0" + mins;
                }else{
                    minutesString = mins + "";
                }
                

                LocalTime sampleTime = LocalTime.parse(hoursString + ":" + minutesString, timeFormatter);
                calculateDifference(sampleTime, date, over23, over24, sampNum);
            }
            
            System.out.print("List of times after tests is : " + timeList + " and its size is : " + timeList.size() + "\n");
            
        } catch (SQLException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    
    private void calculateDifference(LocalTime sampleTime, LocalDate sampleDate, boolean over23, boolean over24, int sampNum){

        LocalTime systemTime = LocalTime.now();
        LocalDate systemDate = LocalDate.now();    

        if(over23){
            sampleDate = sampleDate.plusDays(1);
        }
        if(over24){
            sampleDate = sampleDate.plusDays(1);
            sampleTime = sampleTime.plusMinutes(60);
        }  
        
        if (systemDate.isAfter(sampleDate)){
            JOptionPane.showMessageDialog(null, "Sample: "+ sampNum+ " is delayed!");
            System.out.println("Sample is late by " + systemDate.until(sampleDate,DAYS) + " days and " + systemTime.until(sampleTime, MINUTES) + " minutes");
        }
        else if(sampleDate.isEqual(systemDate)){
            if(systemTime.isAfter(sampleTime)){
                JOptionPane.showMessageDialog(null, "Sample: "+ sampNum+ " is delayed!");
                System.out.println("Sample is late by " + systemDate.until(sampleDate,DAYS) + " days and " + systemTime.until(sampleTime, MINUTES) + " minutes");

            }
        }
    }
    
    private String timeToString(int pTime){
        //settings time need to be edited to convert to LocalTime
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }
    
    public long bigger(long x, long y, long z){
        if(x > y && x > z){
            return x;
        }

        else if(y > z){
            return y;
        }
        else{
            return z;
        }
    }
    
    private void renderChart(String from, String to) throws SQLException{
        String fromDate = from;
        String toDate = to;
        Pair<DefaultCategoryDataset, DefaultCategoryDataset> p;
        
        
        p = createDataset1(fromDate, toDate);
        
        //Bar
        final CategoryDataset dataset1 = p.getKey();
        //Line
        final CategoryDataset dataset2 = p.getValue();

        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            "Turn Around Time Stats", // chart title
            "Date",                   // X axis label
            "% of TAT > 1 hour",      // left Y axis label
            dataset1,                 // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips?
            false                     // url 
        );

        // setting chart parmas
        chart.setBackgroundPaint(Color.white);
        
        // plot needed for setting design
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.getDomainAxis().setCategoryMargin(0.5);
        ((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
        BarRenderer barRenderer = (BarRenderer)plot.getRenderer();
        barRenderer.setSeriesPaint(0, new Color(8,118,188));
        barRenderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        barRenderer.setSeriesItemLabelsVisible(1, true);
        barRenderer.setBaseItemLabelsVisible(true);
        barRenderer.setBaseSeriesVisible(true);
        
         
        
        //right Y axis
        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 1);
        ValueMarker marker = new ValueMarker(10);  // position is the value on the axis
        marker.setPaint(Color.red);
        plot.addRangeMarker(marker);
        
        CategoryPlot plot2 = (CategoryPlot) chart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot2.getRangeAxis();
        rangeAxis.setRange(0, 50);
        
        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        final ValueAxis axis2 = new NumberAxis("Average minutes to authorisation");
        plot.setRangeAxis(1, axis2);

        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        renderer2.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        renderer2.setSeriesItemLabelsVisible(1, true);
        renderer2.setBaseItemLabelsVisible(true);
        renderer2.setBaseSeriesVisible(true);
        plot.setRenderer(1, renderer2);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        
        //save as png
        try{
            final ChartRenderingInfo infoPic = new ChartRenderingInfo(new StandardEntityCollection());
            final File graphF = new File("ReportGraph.png");
            ChartUtilities.saveChartAsPNG(graphF, chart, 1200, 800, infoPic);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
        
        //add the chart in the panel
        final ChartPanel cp = new ChartPanel(chart);
        cp.setDomainZoomable(false);
        cp.setRangeZoomable(false);
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.validate();
    }

    private Pair createDataset1(String from, String to) throws SQLException {

        //getting all the data between date range and setting up the dateobject
        ArrayList<DateOverHour> dateSet = new ArrayList<>();
        ArrayList<ReportData> dateListFromDB = repCon.getDateDataFromDatabase(from, to);
        int allTotalSamp = 0;
        int allOverHour = 0;
        
            Map<String, List<ReportData>> dateGrouped = 
                        dateListFromDB.stream().collect(Collectors.groupingBy(w -> w.Date));
            
            for (Map.Entry<String, List<ReportData>> entry : dateGrouped.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    
                double totalTime = 0.0;
                double overHr = 0.0;
                double totalSamplesPerDay = 0.0;
                double averageTime = 0.0;
                double percentOverHr = 0.0;
                List<ReportData> dataList = entry.getValue();
                
                for(ReportData data : dataList){
                    totalSamplesPerDay = totalSamplesPerDay+1;
                    
                    int SampNum = data.getSampleNumber();              
                    String StartDate = data.getDate();
                    String Department = data.getDepartment();
                    String Tests = data.getTests();
                    String RequestTime = data.getRequestTime();
                    String StartTime = data.getStartTime();
                    String ReportedDate = data.getReportedDate();
                    String ReportedTime = data.getReportedTime();
                    String FinishedTime = data.getFinishedTime();
                    
                    //System.out.println(SampNum);
                    //convert starttime and finishedtime to (HH:mm)                  
                    String[] startSplit = StartTime.split(":");
                    int startHour = Integer.parseInt(startSplit[0]);
                    int startMin = Integer.parseInt(startSplit[1]);
                    
                    String[] endSplit = FinishedTime.split(":");
                    int endHour = Integer.parseInt(endSplit[0]);
                    int endMin = Integer.parseInt(endSplit[1]);
                        
                    int timeDifMin = endMin-startMin;
                    int timeDifHour = endHour-startHour;
                    
                    if(timeDifHour < 1 && timeDifHour > -1){
                        totalTime = totalTime+timeDifMin;
                    }else if(timeDifHour == 1){
                        totalTime = totalTime+(timeDifMin+60);
                        if(timeDifMin+60 >= 60){
                            overHr = overHr+1;
                        }
                    }else if(timeDifHour == 2){
                        totalTime = totalTime+(timeDifMin+120);
                        overHr = overHr+1;
                    }else if(timeDifHour == 3){
                        totalTime = totalTime+(timeDifMin+180);
                        overHr = overHr+1;
                    }else if(timeDifHour == -23){
                        totalTime = totalTime+(timeDifMin+60);
                        if(timeDifMin+60 >= 60){
                            overHr = overHr+1;
                        }
                    }else if(timeDifHour == -22){
                        totalTime = totalTime+(timeDifMin+120);
                        overHr = overHr+1;
                    }else if(timeDifHour == -21){
                        totalTime = totalTime+(timeDifMin+180);
                        overHr = overHr+1;
                    }              
                }

                System.out.println("Total samp in day: "+totalSamplesPerDay);
                System.out.println("Total samp time: "+totalTime);
                allOverHour = (int) (allOverHour + overHr);
                allTotalSamp = (int) (allTotalSamp + totalSamplesPerDay);
                
                //to give 2 digit precent % value
                averageTime = (totalTime/totalSamplesPerDay);
                averageTime = Math.round(averageTime * 100);
                averageTime = averageTime/100;
                
                //to give 2 digit precent % value
                percentOverHr = overHr/totalSamplesPerDay;
                percentOverHr = percentOverHr * 100;
                percentOverHr = Math.round(percentOverHr * 100);
                percentOverHr = percentOverHr/100;
                
                //passing info for each date
                DateOverHour feed = new DateOverHour();
                feed.setDate(entry.getKey());
                feed.setOverHr(percentOverHr);
                feed.setAvgTime(averageTime);
                dateSet.add(feed);
                
                System.out.println("Average time: " + averageTime);
                System.out.println("Total over hr: " + overHr);
                System.out.println("Percent over hr: "+percentOverHr);
            }

            //to sort the dates in the correct order
            Collections.sort(dateSet, (DateOverHour o1, DateOverHour o2) -> {
                DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    return f.parse(o1.getDate()).compareTo(f.parse(o2.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            });
  
        //both sets needed to be plotted
        //both dataset keys
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        final String series1 = "% Over an Hour";
        final String series2 = "Average minutes";
        
        String all = String.valueOf(allTotalSamp);
        String over = String.valueOf(allOverHour);
        repTotalSampTf.setText(all);
        repOverHourTf.setText(over);
        //gets average time and times over hour for each date
        for(DateOverHour data : dateSet){
            dataset.addValue(data.getOverHr(), series1, data.getDate());
            dataset2.addValue(data.getAvgTime(), series2, data.getDate());
        }
        //too pass both DefaultCategoryDatasets to be plotted
        Pair<DefaultCategoryDataset, DefaultCategoryDataset> p = new Pair<>(dataset,dataset2);
        return p;
    }

    public static boolean readAndWriteCSVFileToDatabase() throws SQLException {    
        try{
            csvFile = CSVFilePath.path;
            System.out.println("reading from " + csvFile);
            FileReader filereader = new FileReader(csvFile); 
            CSVReader csvReader = new CSVReader(filereader); 
            String[] nextRecord; 
            ArrayList<CsvData> dataList = new ArrayList<CsvData>();
            
            while((nextRecord = csvReader.readNext()) != null){
                CsvData data = new CsvData();
                data.setSampleNumber(Integer.parseInt(nextRecord[0]));
                data.setDate(nextRecord[1]);
                data.setDepartment(nextRecord[2]);
                data.setTests(nextRecord[3]);
                data.setRequesttime(nextRecord[4]);
                String time = nextRecord[5];
                if(time.length() == 1){
                    time = "00:0" + time;
                }else if(time.length() == 2){
                    time = "00:" + time;
                }else if(time.length() == 3){
                    time = "0"+time.substring(0, time.length() - 2) + ":" + time.substring(time.length() - 2, time.length());
                }else{
                    time = time.substring(0, time.length() - 2) + ":" + time.substring(time.length() - 2, time.length());
                }
                data.setStarttime(time);
                dataList.add(data);
            }
        System.out.println("Size : " + dataList.size());
        return csvCon.insertDataIntoDatabase(dataList);
        }catch(IOException e){
            e.printStackTrace();
        }catch(NumberFormatException ex){
            ex.printStackTrace();
        } 
        return false;
    }
    
    public static ArrayList<CsvData> readAndReturnFromCSV() throws IOException{
        ArrayList<CsvData> dataList = new ArrayList<CsvData>();
        FileReader filereader;
        CSVReader csvReader = null;
        csvFile = CSVFilePath.path;

        try{
            filereader = new FileReader(csvFile); 
            csvReader = new CSVReader(filereader); 

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
            System.out.println("reading from " + csvFile);
            String[] nextRecord; 
           
            while((nextRecord = csvReader.readNext()) != null){
                CsvData data = new CsvData();
                data.setSampleNumber(Integer.parseInt(nextRecord[0]));
                data.setDate(nextRecord[1]);
                data.setDepartment(nextRecord[2]);
                data.setTests(nextRecord[3]);
                data.setRequesttime(nextRecord[4]);
                String time = nextRecord[5];
                if(time.length() == 1){
                    time = "00:0" + time;
                }else if(time.length() == 2){
                    time = "00:" + time;
                }else if(time.length() == 3){
                    time = "0"+time.substring(0, time.length() - 2) + ":" + time.substring(time.length() - 2, time.length());
                }else{
                    time = time.substring(0, time.length() - 2) + ":" + time.substring(time.length() - 2, time.length());
                }
            data.setStarttime(time);
            dataList.add(data);
            }

        return dataList;
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
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHead = new javax.swing.JPanel();
        exitbtn = new javax.swing.JButton();
        minbtn = new javax.swing.JButton();
        toppnl = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        samplebtn = new javax.swing.JPanel();
        indicator1 = new javax.swing.JPanel();
        sampleLbl = new javax.swing.JLabel();
        reportBtn = new javax.swing.JPanel();
        indicator2 = new javax.swing.JPanel();
        sampleLbl1 = new javax.swing.JLabel();
        settingsBtn = new javax.swing.JPanel();
        indicator3 = new javax.swing.JPanel();
        sampleLbl2 = new javax.swing.JLabel();
        dateTf = new javax.swing.JLabel();
        timeTf = new javax.swing.JLabel();
        bodypnl = new javax.swing.JPanel();
        defaultpage = new javax.swing.JPanel();
        reportDash = new javax.swing.JPanel();
        chartPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        delcsvBtn = new javax.swing.JButton();
        dateSelect = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        dateToTf = new com.toedter.calendar.JDateChooser();
        dateFromTf = new com.toedter.calendar.JDateChooser();
        dateApplyBtn = new javax.swing.JButton();
        loadcsvBtn = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        repTotalSampTf = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        repOverHourTf = new javax.swing.JTextField();
        settingsDash = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        testsGroup3Tf = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        testsGroup2Tf = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        testsGroup1Tf = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        timeGroup1Tf = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        timeGroup2Tf = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        timeGroup3Tf = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        setCsvPathBtn = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        defaultAlertTimeTf = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sampleDash = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sampleTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        csvBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        sampleNumTableTf = new javax.swing.JTextField();
        delayBtn = new javax.swing.JButton();
        delayTf = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        testsTf = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        delayTitleLbl = new javax.swing.JLabel();
        totalSamplepanel = new javax.swing.JPanel();
        totalsamplesLbl = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        totalsampleTf = new javax.swing.JTextField();
        delCsvBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dashboard");
        setUndecorated(true);
        setSize(new java.awt.Dimension(1000, 750));

        pnlHead.setBackground(new java.awt.Color(4, 68, 108));
        pnlHead.setPreferredSize(new java.awt.Dimension(40, 18));
        pnlHead.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                pnlHeadMouseDragged(evt);
            }
        });
        pnlHead.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlHeadMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pnlHeadMousePressed(evt);
            }
        });

        exitbtn.setBackground(new java.awt.Color(4, 68, 108));
        exitbtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        exitbtn.setForeground(new java.awt.Color(204, 204, 204));
        exitbtn.setText("X");
        exitbtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        exitbtn.setContentAreaFilled(false);
        exitbtn.setFocusable(false);
        exitbtn.setOpaque(true);
        exitbtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitbtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                exitbtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                exitbtnMouseExited(evt);
            }
        });

        minbtn.setBackground(new java.awt.Color(4, 68, 108));
        minbtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        minbtn.setForeground(new java.awt.Color(204, 204, 204));
        minbtn.setText("-");
        minbtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        minbtn.setContentAreaFilled(false);
        minbtn.setFocusable(false);
        minbtn.setMaximumSize(new java.awt.Dimension(11, 17));
        minbtn.setMinimumSize(new java.awt.Dimension(11, 17));
        minbtn.setOpaque(true);
        minbtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minbtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minbtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minbtnMouseExited(evt);
            }
        });
        minbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minbtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlHeadLayout = new javax.swing.GroupLayout(pnlHead);
        pnlHead.setLayout(pnlHeadLayout);
        pnlHeadLayout.setHorizontalGroup(
            pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeadLayout.createSequentialGroup()
                .addContainerGap(961, Short.MAX_VALUE)
                .addComponent(minbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(exitbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4))
        );
        pnlHeadLayout.setVerticalGroup(
            pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeadLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        toppnl.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SampleMonitoringImages/login pic_1.PNG"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Sitka Heading", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(8, 118, 188));
        jLabel1.setText("Welcome to Tallaght Laboratory Dashboard");

        samplebtn.setBackground(new java.awt.Color(255, 255, 255));
        samplebtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        samplebtn.setFocusable(false);
        samplebtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                samplebtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                samplebtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                samplebtnMouseExited(evt);
            }
        });

        indicator1.setPreferredSize(new java.awt.Dimension(224, 16));

        javax.swing.GroupLayout indicator1Layout = new javax.swing.GroupLayout(indicator1);
        indicator1.setLayout(indicator1Layout);
        indicator1Layout.setHorizontalGroup(
            indicator1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        indicator1Layout.setVerticalGroup(
            indicator1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        sampleLbl.setFont(new java.awt.Font("Sitka Heading", 1, 24)); // NOI18N
        sampleLbl.setForeground(new java.awt.Color(8, 118, 188));
        sampleLbl.setText("Samples");

        javax.swing.GroupLayout samplebtnLayout = new javax.swing.GroupLayout(samplebtn);
        samplebtn.setLayout(samplebtnLayout);
        samplebtnLayout.setHorizontalGroup(
            samplebtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(samplebtnLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(sampleLbl)
                .addContainerGap(72, Short.MAX_VALUE))
            .addComponent(indicator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        samplebtnLayout.setVerticalGroup(
            samplebtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, samplebtnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sampleLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indicator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        reportBtn.setBackground(new java.awt.Color(255, 255, 255));
        reportBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        reportBtn.setFocusable(false);
        reportBtn.setPreferredSize(new java.awt.Dimension(216, 64));
        reportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reportBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                reportBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                reportBtnMouseExited(evt);
            }
        });

        javax.swing.GroupLayout indicator2Layout = new javax.swing.GroupLayout(indicator2);
        indicator2.setLayout(indicator2Layout);
        indicator2Layout.setHorizontalGroup(
            indicator2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 214, Short.MAX_VALUE)
        );
        indicator2Layout.setVerticalGroup(
            indicator2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        sampleLbl1.setFont(new java.awt.Font("Sitka Heading", 1, 24)); // NOI18N
        sampleLbl1.setForeground(new java.awt.Color(8, 118, 188));
        sampleLbl1.setText("Report");

        javax.swing.GroupLayout reportBtnLayout = new javax.swing.GroupLayout(reportBtn);
        reportBtn.setLayout(reportBtnLayout);
        reportBtnLayout.setHorizontalGroup(
            reportBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(indicator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reportBtnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sampleLbl1)
                .addGap(70, 70, 70))
        );
        reportBtnLayout.setVerticalGroup(
            reportBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reportBtnLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(sampleLbl1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(indicator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        settingsBtn.setBackground(new java.awt.Color(255, 255, 255));
        settingsBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        settingsBtn.setFocusable(false);
        settingsBtn.setPreferredSize(new java.awt.Dimension(216, 64));
        settingsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingsBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                settingsBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                settingsBtnMouseExited(evt);
            }
        });

        javax.swing.GroupLayout indicator3Layout = new javax.swing.GroupLayout(indicator3);
        indicator3.setLayout(indicator3Layout);
        indicator3Layout.setHorizontalGroup(
            indicator3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        indicator3Layout.setVerticalGroup(
            indicator3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        sampleLbl2.setFont(new java.awt.Font("Sitka Heading", 1, 24)); // NOI18N
        sampleLbl2.setForeground(new java.awt.Color(8, 118, 188));
        sampleLbl2.setText("Settings");

        javax.swing.GroupLayout settingsBtnLayout = new javax.swing.GroupLayout(settingsBtn);
        settingsBtn.setLayout(settingsBtnLayout);
        settingsBtnLayout.setHorizontalGroup(
            settingsBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsBtnLayout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(sampleLbl2)
                .addContainerGap(63, Short.MAX_VALUE))
            .addComponent(indicator3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        settingsBtnLayout.setVerticalGroup(
            settingsBtnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsBtnLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sampleLbl2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(indicator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        dateTf.setFont(new java.awt.Font("Sitka Display", 1, 18)); // NOI18N
        dateTf.setForeground(new java.awt.Color(8, 118, 188));

        timeTf.setFont(new java.awt.Font("Sitka Display", 1, 18)); // NOI18N
        timeTf.setForeground(new java.awt.Color(8, 118, 188));

        javax.swing.GroupLayout toppnlLayout = new javax.swing.GroupLayout(toppnl);
        toppnl.setLayout(toppnlLayout);
        toppnlLayout.setHorizontalGroup(
            toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toppnlLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toppnlLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(toppnlLayout.createSequentialGroup()
                                .addComponent(samplebtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(reportBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(settingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toppnlLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toppnlLayout.createSequentialGroup()
                                .addComponent(dateTf, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toppnlLayout.createSequentialGroup()
                                .addComponent(timeTf, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24))))))
        );
        toppnlLayout.setVerticalGroup(
            toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toppnlLayout.createSequentialGroup()
                .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(toppnlLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel2))
                    .addGroup(toppnlLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dateTf, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(timeTf, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(reportBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(settingsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                            .addComponent(samplebtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(15, 15, 15))
        );

        bodypnl.setLayout(new java.awt.CardLayout());

        defaultpage.setBackground(new java.awt.Color(245, 245, 245));

        javax.swing.GroupLayout defaultpageLayout = new javax.swing.GroupLayout(defaultpage);
        defaultpage.setLayout(defaultpageLayout);
        defaultpageLayout.setHorizontalGroup(
            defaultpageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1007, Short.MAX_VALUE)
        );
        defaultpageLayout.setVerticalGroup(
            defaultpageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
        );

        bodypnl.add(defaultpage, "card2");

        chartPanel.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(8, 118, 188));
        jLabel8.setText("Set sample date range");

        delcsvBtn.setBackground(new java.awt.Color(8, 118, 188));
        delcsvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        delcsvBtn.setForeground(new java.awt.Color(255, 255, 255));
        delcsvBtn.setText("Clear File");
        delcsvBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        delcsvBtn.setContentAreaFilled(false);
        delcsvBtn.setOpaque(true);
        delcsvBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delcsvBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delcsvBtnMouseExited(evt);
            }
        });
        delcsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delcsvBtnActionPerformed(evt);
            }
        });

        jLabel16.setText("To:");

        jLabel15.setText("From:");

        dateToTf.setDateFormatString("dd/MM/yyyy");
        dateToTf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        dateFromTf.setDateFormatString("dd/MM/yyyy");
        dateFromTf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        dateApplyBtn.setBackground(new java.awt.Color(8, 118, 188));
        dateApplyBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        dateApplyBtn.setForeground(new java.awt.Color(255, 255, 255));
        dateApplyBtn.setText("Apply");
        dateApplyBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        dateApplyBtn.setContentAreaFilled(false);
        dateApplyBtn.setOpaque(true);
        dateApplyBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                dateApplyBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                dateApplyBtnMouseExited(evt);
            }
        });
        dateApplyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateApplyBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dateSelectLayout = new javax.swing.GroupLayout(dateSelect);
        dateSelect.setLayout(dateSelectLayout);
        dateSelectLayout.setHorizontalGroup(
            dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dateSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateFromTf, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateToTf, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dateSelectLayout.createSequentialGroup()
                        .addGroup(dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dateSelectLayout.createSequentialGroup()
                .addGap(0, 29, Short.MAX_VALUE)
                .addComponent(dateApplyBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        dateSelectLayout.setVerticalGroup(
            dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dateSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateFromTf, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateToTf, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dateApplyBtn)
                .addContainerGap())
        );

        loadcsvBtn.setBackground(new java.awt.Color(8, 118, 188));
        loadcsvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        loadcsvBtn.setForeground(new java.awt.Color(255, 255, 255));
        loadcsvBtn.setText("Load CSV File");
        loadcsvBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        loadcsvBtn.setContentAreaFilled(false);
        loadcsvBtn.setOpaque(true);
        loadcsvBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loadcsvBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loadcsvBtnMouseExited(evt);
            }
        });
        loadcsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadcsvBtnActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(8, 118, 188));
        jLabel9.setText("to generate a chart");

        jLabel18.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(8, 118, 188));
        jLabel18.setText("Total Samples:");

        repTotalSampTf.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        repTotalSampTf.setBorder(null);

        jLabel19.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(8, 118, 188));
        jLabel19.setText("Samples Over 1 Hour:");

        repOverHourTf.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        repOverHourTf.setBorder(null);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(repOverHourTf))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(repTotalSampTf))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(dateSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(loadcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(repTotalSampTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(repOverHourTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                .addComponent(loadcsvBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(delcsvBtn)
                .addContainerGap())
        );

        javax.swing.GroupLayout reportDashLayout = new javax.swing.GroupLayout(reportDash);
        reportDash.setLayout(reportDashLayout);
        reportDashLayout.setHorizontalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 756, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        reportDashLayout.setVerticalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bodypnl.add(reportDash, "card4");

        settingsDash.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        testsGroup3Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(testsGroup3Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 246, 386, 34));

        jLabel20.setFont(new java.awt.Font("Sitka Small", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(8, 118, 188));
        jLabel20.setText("Test Group 3 (Moderate Priority)");
        jPanel4.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 216, -1, 24));

        testsGroup2Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(testsGroup2Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 166, 386, 34));

        jLabel5.setFont(new java.awt.Font("Sitka Small", 0, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(8, 118, 188));
        jLabel5.setText("Test Group 2 (High Priority)");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 136, -1, 24));

        testsGroup1Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(testsGroup1Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 86, 386, 34));

        jLabel3.setFont(new java.awt.Font("Sitka Small", 0, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 118, 188));
        jLabel3.setText("Test Group 1 (Urgent)");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 56, -1, 24));

        timeGroup1Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup1Tf.setText("20");
        timeGroup1Tf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeGroup1TfKeyTyped(evt);
            }
        });
        jPanel4.add(timeGroup1Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 86, -1, 34));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(8, 118, 188));
        jLabel14.setText("Mins");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(636, 96, -1, -1));

        timeGroup2Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup2Tf.setText("25");
        timeGroup2Tf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeGroup2TfKeyTyped(evt);
            }
        });
        jPanel4.add(timeGroup2Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 166, -1, 33));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(8, 118, 188));
        jLabel11.setText("Mins");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(636, 175, -1, -1));

        timeGroup3Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup3Tf.setText("30");
        timeGroup3Tf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeGroup3TfActionPerformed(evt);
            }
        });
        timeGroup3Tf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                timeGroup3TfKeyTyped(evt);
            }
        });
        jPanel4.add(timeGroup3Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 246, -1, 33));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(8, 118, 188));
        jLabel7.setText("Mins");
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(636, 255, -1, -1));

        jLabel21.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(8, 118, 188));
        jLabel21.setText("Default alert time is assigned to samples with no tests in a group");
        jPanel4.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 310, -1, 20));

        setCsvPathBtn.setBackground(new java.awt.Color(8, 118, 188));
        setCsvPathBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        setCsvPathBtn.setForeground(new java.awt.Color(255, 255, 255));
        setCsvPathBtn.setText("Select Path");
        setCsvPathBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setCsvPathBtn.setContentAreaFilled(false);
        setCsvPathBtn.setOpaque(true);
        setCsvPathBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setCsvPathBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setCsvPathBtnMouseExited(evt);
            }
        });
        setCsvPathBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCsvPathBtnActionPerformed(evt);
            }
        });
        jPanel4.add(setCsvPathBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 403, 280, 30));

        jLabel10.setFont(new java.awt.Font("Sitka Small", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(8, 118, 188));
        jLabel10.setText("Adjust Alert Time and Select Sample File");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, -1, 24));

        jLabel12.setFont(new java.awt.Font("Sitka Small", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(8, 118, 188));
        jLabel12.setText("Default Sample Alert Time:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 333, -1, 20));

        defaultAlertTimeTf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        defaultAlertTimeTf.setText("40");
        defaultAlertTimeTf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                defaultAlertTimeTfKeyTyped(evt);
            }
        });
        jPanel4.add(defaultAlertTimeTf, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 330, -1, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(8, 118, 188));
        jLabel6.setText("Mins");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 340, 30, -1));

        jLabel4.setFont(new java.awt.Font("Sitka Small", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(8, 118, 188));
        jLabel4.setText("Set CSV file path to monitor samples");
        jPanel4.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 377, -1, 20));

        settingsDash.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, 830, 460));

        bodypnl.add(settingsDash, "card5");

        sampleDash.setBackground(new java.awt.Color(245, 245, 245));

        jScrollPane1.setBorder(null);

        sampleTable.setBackground(new java.awt.Color(247, 247, 247));
        sampleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Sample Number", "Date", "Department", "Tests", "Request Time", "Start Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sampleTable.setFocusable(false);
        sampleTable.setGridColor(new java.awt.Color(242, 242, 242));
        sampleTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        sampleTable.setRowHeight(25);
        sampleTable.setShowVerticalLines(false);
        sampleTable.getTableHeader().setReorderingAllowed(false);
        sampleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sampleTableMouseClicked(evt);
            }
        });
        sampleTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sampleTableKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                sampleTableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(sampleTable);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        csvBtn.setBackground(new java.awt.Color(8, 118, 188));
        csvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        csvBtn.setForeground(new java.awt.Color(255, 255, 255));
        csvBtn.setText("Update Samples");
        csvBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        csvBtn.setContentAreaFilled(false);
        csvBtn.setOpaque(true);
        csvBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                csvBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                csvBtnMouseExited(evt);
            }
        });
        csvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvBtnActionPerformed(evt);
            }
        });
        jPanel2.add(csvBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 370, 181, 35));

        jPanel1.setMaximumSize(new java.awt.Dimension(222, 153));
        jPanel1.setMinimumSize(new java.awt.Dimension(222, 153));

        sampleNumTableTf.setEditable(false);
        sampleNumTableTf.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sampleNumTableTf.setForeground(new java.awt.Color(8, 118, 188));
        sampleNumTableTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleNumTableTfActionPerformed(evt);
            }
        });

        delayBtn.setBackground(new java.awt.Color(8, 118, 188));
        delayBtn.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        delayBtn.setForeground(new java.awt.Color(255, 255, 255));
        delayBtn.setText("Update Time");
        delayBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        delayBtn.setContentAreaFilled(false);
        delayBtn.setMargin(new java.awt.Insets(14, 14, 14, 14));
        delayBtn.setOpaque(true);
        delayBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                delayBtnMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delayBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delayBtnMouseExited(evt);
            }
        });
        delayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delayBtnActionPerformed(evt);
            }
        });

        delayTf.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        delayTf.setForeground(new java.awt.Color(8, 118, 188));
        delayTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delayTfActionPerformed(evt);
            }
        });
        delayTf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                delayTfKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                delayTfKeyTyped(evt);
            }
        });

        testsTf.setEditable(false);
        testsTf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        testsTf.setForeground(new java.awt.Color(8, 118, 188));
        testsTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testsTfActionPerformed(evt);
            }
        });

        delayTitleLbl.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        delayTitleLbl.setForeground(new java.awt.Color(8, 118, 188));
        delayTitleLbl.setText("Sample details");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(sampleNumTableTf))
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(testsTf, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(delayBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(delayTitleLbl)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(delayTitleLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sampleNumTableTf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(testsTf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delayBtn)
                .addGap(15, 15, 15))
        );

        jPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, 240, 180));

        totalsamplesLbl.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        totalsamplesLbl.setForeground(new java.awt.Color(8, 118, 188));
        totalsamplesLbl.setText("Total Number of Samples:");

        totalsampleTf.setEditable(false);
        totalsampleTf.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        totalsampleTf.setForeground(new java.awt.Color(8, 118, 188));
        totalsampleTf.setBorder(null);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(totalsampleTf, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(totalsampleTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout totalSamplepanelLayout = new javax.swing.GroupLayout(totalSamplepanel);
        totalSamplepanel.setLayout(totalSamplepanelLayout);
        totalSamplepanelLayout.setHorizontalGroup(
            totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalSamplepanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(totalsamplesLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        totalSamplepanelLayout.setVerticalGroup(
            totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalSamplepanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(totalsamplesLbl)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(totalSamplepanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 12, 329, -1));

        delCsvBtn.setBackground(new java.awt.Color(8, 118, 188));
        delCsvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 16)); // NOI18N
        delCsvBtn.setForeground(new java.awt.Color(255, 255, 255));
        delCsvBtn.setText("Clear Current Samples");
        delCsvBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        delCsvBtn.setContentAreaFilled(false);
        delCsvBtn.setOpaque(true);
        delCsvBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                delCsvBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                delCsvBtnMouseExited(evt);
            }
        });
        delCsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delCsvBtnActionPerformed(evt);
            }
        });
        jPanel2.add(delCsvBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 410, 181, 35));

        javax.swing.GroupLayout sampleDashLayout = new javax.swing.GroupLayout(sampleDash);
        sampleDash.setLayout(sampleDashLayout);
        sampleDashLayout.setHorizontalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                .addContainerGap())
        );
        sampleDashLayout.setVerticalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bodypnl.add(sampleDash, "card3");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bodypnl, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(toppnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlHead, javax.swing.GroupLayout.PREFERRED_SIZE, 1007, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlHead, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(toppnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(bodypnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(1000, 750));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void exitbtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitbtnMouseClicked
        System.exit(0);
    }//GEN-LAST:event_exitbtnMouseClicked

    private void minbtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minbtnMouseClicked
        this.setState(Frame.ICONIFIED);
    }//GEN-LAST:event_minbtnMouseClicked

    private void pnlHeadMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHeadMouseDragged
        int x = evt.getXOnScreen();
        int y = evt.getYOnScreen();
        this.setLocation(x - xMouse, y - yMouse);
    }//GEN-LAST:event_pnlHeadMouseDragged

    private void pnlHeadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHeadMouseClicked

    }//GEN-LAST:event_pnlHeadMouseClicked

    private void pnlHeadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlHeadMousePressed
        xMouse = evt.getX();
        yMouse = evt.getY();
    }//GEN-LAST:event_pnlHeadMousePressed

    private void exitbtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitbtnMouseEntered
        exitbtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_exitbtnMouseEntered

    private void minbtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minbtnMouseEntered
        minbtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_minbtnMouseEntered

    private void minbtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minbtnMouseExited
        minbtn.setBackground(new Color(4,68,108));
    }//GEN-LAST:event_minbtnMouseExited

    private void exitbtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitbtnMouseExited
        exitbtn.setBackground(new Color(4,68,108));
    }//GEN-LAST:event_exitbtnMouseExited

    private void samplebtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_samplebtnMouseClicked
       //(8,118,188)
        indicator1.setBackground(new Color(8,118,188));
        indicator2.setBackground(new Color(255,255,255));
        indicator3.setBackground(new Color(255,255,255));
        bodypnl.removeAll();
        bodypnl.repaint();
        bodypnl.revalidate();
        bodypnl.add(sampleDash);
        bodypnl.repaint();
        bodypnl.revalidate();
    }//GEN-LAST:event_samplebtnMouseClicked

    private void samplebtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_samplebtnMouseEntered
        samplebtn.setBackground(new Color(240,240,240));
    }//GEN-LAST:event_samplebtnMouseEntered

    private void samplebtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_samplebtnMouseExited
        samplebtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_samplebtnMouseExited

    private void reportBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportBtnMouseClicked
        indicator2.setBackground(new Color(8,118,188));
        indicator1.setBackground(new Color(255,255,255));
        indicator3.setBackground(new Color(255,255,255));
        bodypnl.removeAll();
        bodypnl.repaint();
        bodypnl.revalidate();
        bodypnl.add(reportDash);
        bodypnl.repaint();
        bodypnl.revalidate();
    }//GEN-LAST:event_reportBtnMouseClicked

    private void reportBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportBtnMouseEntered
        reportBtn.setBackground(new Color(240,240,240));
    }//GEN-LAST:event_reportBtnMouseEntered

    private void reportBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportBtnMouseExited
        reportBtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_reportBtnMouseExited

    private void settingsBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsBtnMouseClicked
        indicator3.setBackground(new Color(8,118,188));
        indicator1.setBackground(new Color(255,255,255));
        indicator2.setBackground(new Color(255,255,255));
        bodypnl.removeAll();
        bodypnl.repaint();
        bodypnl.revalidate();
        bodypnl.add(settingsDash);
        bodypnl.repaint();
        bodypnl.revalidate();
    }//GEN-LAST:event_settingsBtnMouseClicked

    private void settingsBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsBtnMouseEntered
        settingsBtn.setBackground(new Color(240,240,240));
    }//GEN-LAST:event_settingsBtnMouseEntered

    private void settingsBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsBtnMouseExited
        settingsBtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_settingsBtnMouseExited

    private void csvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvBtnActionPerformed
        try {
            GUIDashboard.csvCon.compareCsvToDatabase();
            UpdateTable();
        } catch (SQLException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_csvBtnActionPerformed

    private void sampleTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sampleTableMouseClicked
        if(csvConn != null ){
            try {
                if(csvConn.isClosed()){
                    csvConn=CsvDBConnection.CsvDBConnection();
                }
                else{
                    csvConn.close();
                    csvConn=CsvDBConnection.CsvDBConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            csvConn=CsvDBConnection.CsvDBConnection();
        }
        
        try{
            int row = sampleTable.getSelectedRow();
            String table_click=(sampleTable.getModel().getValueAt(row, 0).toString());
            sampleNum=table_click;
            String sql ="select * from csv_table where SampleNumber ='"+table_click+"' ";
            prep=csvConn.prepareStatement(sql);
            rs=prep.executeQuery();
            if(rs.next()){
                String time = rs.getString("StartTime");
                String num = rs.getString("SampleNumber");
                String test = rs.getString("Tests");
                delayTf.setText(time);
                sampleNumTableTf.setText(num);
                testsTf.setText(test);
                progressTime();
            } 
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }finally{
            try{
                rs.close();
                prep.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_sampleTableMouseClicked

    private void delayTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delayTfActionPerformed

    }//GEN-LAST:event_delayTfActionPerformed

    private void loadcsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadcsvBtnActionPerformed
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv");
        fc.setFileFilter(filter);
        int i = fc.showOpenDialog(null);
        if (i == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String filepath = f.getPath();
            readAndWriteCSVFileToReportDatabase(filepath);
        }
    }//GEN-LAST:event_loadcsvBtnActionPerformed

    private void delcsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delcsvBtnActionPerformed
        int j = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete the file?",
                "Clear",JOptionPane.YES_NO_OPTION);
        if(j==0){
            try {
                int i = repCon.deleteAllFromDatabase();
                if(i > 0){
                    JOptionPane.showMessageDialog(this, "The table has been cleared");
                }else{
                    JOptionPane.showMessageDialog(this, "Table is cleared");
                }
            } catch (SQLException ex) {
                Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_delcsvBtnActionPerformed

    private void sampleNumTableTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleNumTableTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sampleNumTableTfActionPerformed

    private void delayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delayBtnActionPerformed
        if(delayTf.getText().length() > 4 && delayTf.getText().length() < 6){
            if(csvConn != null ){
                try {
                    if(csvConn.isClosed()){
                        csvConn=CsvDBConnection.CsvDBConnection();
                    }
                    else{
                        csvConn.close();
                        csvConn=CsvDBConnection.CsvDBConnection();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                csvConn=CsvDBConnection.CsvDBConnection();
            }

            try{
                String samp = sampleNumTableTf.getText();
                String time = delayTf.getText();

                String sql = "update csv_table set StartTime ='"+time+"' where SampleNumber='"+samp+"'";
                prep=csvConn.prepareStatement(sql);
                prep.execute();
                JOptionPane.showMessageDialog(null, "Start time updated");

            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }finally{
                try{
                    prep.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            UpdateTable();
        }else{
            JOptionPane.showMessageDialog(null, "Make sure a sample is selected and a correct time is entered");
        }
        
    }//GEN-LAST:event_delayBtnActionPerformed

    private void delayBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseEntered
        delayBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_delayBtnMouseEntered

    private void delayBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseExited
        delayBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_delayBtnMouseExited

    private void delayBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_delayBtnMouseClicked

    private void sampleTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleTableKeyPressed
        
    }//GEN-LAST:event_sampleTableKeyPressed

    private void sampleTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleTableKeyReleased
 
    }//GEN-LAST:event_sampleTableKeyReleased

    private void setCsvPathBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setCsvPathBtnActionPerformed
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv");
        fc.setFileFilter(filter);
        int i = fc.showOpenDialog(null);

        if (i == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String filepath = f.getPath();
            setCsvPathBtn.setName(filepath);
        }
        if(setCsvPathBtn.getName() != null){
            CSVFilePath.path = setCsvPathBtn.getName();
            JOptionPane.showMessageDialog(null, "The Path: "+CSVFilePath.path+" has been set");
            ReadWrite.writeFile();  
        }else{
            JOptionPane.showMessageDialog(null, "Make sure a file was selected.");
        }
    }//GEN-LAST:event_setCsvPathBtnActionPerformed

    private void dateApplyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateApplyBtnActionPerformed

        String from = ((JTextField)dateFromTf.getDateEditor().getUiComponent()).getText();
        String to = ((JTextField)dateToTf.getDateEditor().getUiComponent()).getText();

        try {
            renderChart(from, to);
            JOptionPane.showMessageDialog(null, "A PNG image of the graph has been saved");
        } catch (SQLException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Make sure a historical sample csv file has been saved");
        }

    }//GEN-LAST:event_dateApplyBtnActionPerformed

    private void setCsvPathBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setCsvPathBtnMouseEntered
        setCsvPathBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_setCsvPathBtnMouseEntered

    private void setCsvPathBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_setCsvPathBtnMouseExited
        setCsvPathBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_setCsvPathBtnMouseExited

    private void loadcsvBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadcsvBtnMouseEntered
        loadcsvBtn.setBackground(new Color(51,153,255));                                   
                                  
        
    }//GEN-LAST:event_loadcsvBtnMouseEntered

    private void loadcsvBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loadcsvBtnMouseExited
        loadcsvBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_loadcsvBtnMouseExited

    private void delcsvBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delcsvBtnMouseEntered
        delcsvBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_delcsvBtnMouseEntered

    private void delcsvBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delcsvBtnMouseExited
        delcsvBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_delcsvBtnMouseExited

    private void dateApplyBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateApplyBtnMouseEntered
        dateApplyBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_dateApplyBtnMouseEntered

    private void dateApplyBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dateApplyBtnMouseExited
        dateApplyBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_dateApplyBtnMouseExited

    private void delCsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delCsvBtnActionPerformed
        int i = JOptionPane.showConfirmDialog(null,"Clearing all samples will delete all the time change applied." 
                + "\n             Are you sure you want to clear all samples?",
                "Clear",JOptionPane.YES_NO_OPTION);
        if(i==0){
            try {                                          
                int del = csvCon.deleteAllFromDatabase();
                UpdateTable();
            }catch(SQLException ex){
                Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_delCsvBtnActionPerformed

    private void delCsvBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delCsvBtnMouseEntered
        delCsvBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_delCsvBtnMouseEntered

    private void delCsvBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delCsvBtnMouseExited
        delCsvBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_delCsvBtnMouseExited

    private void csvBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_csvBtnMouseEntered
        csvBtn.setBackground(new Color(51,153,255));
    }//GEN-LAST:event_csvBtnMouseEntered

    private void csvBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_csvBtnMouseExited
        csvBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_csvBtnMouseExited

    private void minbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minbtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minbtnActionPerformed

    private void testsTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testsTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_testsTfActionPerformed

    private void delayTfKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_delayTfKeyPressed
        
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            if(delayTf.getText().length() > 4 && delayTf.getText().length() < 6){
                if(csvConn != null ){
                    try {
                        if(csvConn.isClosed()){
                            csvConn=CsvDBConnection.CsvDBConnection();
                        }
                        else{
                            csvConn.close();
                            csvConn=CsvDBConnection.CsvDBConnection();
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else {
                    csvConn=CsvDBConnection.CsvDBConnection();
                }

                try{
                    String samp = sampleNumTableTf.getText();
                    String time = delayTf.getText();

                    String sql = "update csv_table set StartTime ='"+time+"' where SampleNumber='"+samp+"'";
                    prep=csvConn.prepareStatement(sql);
                    prep.execute();
                    JOptionPane.showMessageDialog(null, "Start time updated");

                }catch(Exception e){
                    JOptionPane.showMessageDialog(null, e);
                }finally{
                    try{
                        prep.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                UpdateTable();
            }else{
                JOptionPane.showMessageDialog(null, "Make sure a sample is selected and a correct time is entered");
            }
        }
        
        
        
    }//GEN-LAST:event_delayTfKeyPressed

    private void delayTfKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_delayTfKeyTyped
        char c = evt.getKeyChar();
        if(!Character.isDigit(c)){
            evt.consume();
        }
    }//GEN-LAST:event_delayTfKeyTyped

    private void defaultAlertTimeTfKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_defaultAlertTimeTfKeyTyped
        char c = evt.getKeyChar();
        if(!Character.isDigit(c)){
            evt.consume();
        }
    }//GEN-LAST:event_defaultAlertTimeTfKeyTyped

    private void timeGroup3TfKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeGroup3TfKeyTyped
        char c = evt.getKeyChar();
        if(!Character.isDigit(c)){
            evt.consume();
        }
    }//GEN-LAST:event_timeGroup3TfKeyTyped

    private void timeGroup2TfKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeGroup2TfKeyTyped
        char c = evt.getKeyChar();
        if(!Character.isDigit(c)){
            evt.consume();
        }
    }//GEN-LAST:event_timeGroup2TfKeyTyped

    private void timeGroup1TfKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_timeGroup1TfKeyTyped
        char c = evt.getKeyChar();
        if(!Character.isDigit(c)){
            evt.consume();
        }
    }//GEN-LAST:event_timeGroup1TfKeyTyped

    private void timeGroup3TfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeGroup3TfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_timeGroup3TfActionPerformed

    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUIDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUIDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUIDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUIDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUIDashboard().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bodypnl;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton csvBtn;
    private javax.swing.JButton dateApplyBtn;
    private com.toedter.calendar.JDateChooser dateFromTf;
    private javax.swing.JPanel dateSelect;
    private javax.swing.JLabel dateTf;
    private com.toedter.calendar.JDateChooser dateToTf;
    private javax.swing.JTextField defaultAlertTimeTf;
    private javax.swing.JPanel defaultpage;
    private javax.swing.JButton delCsvBtn;
    private javax.swing.JButton delayBtn;
    private javax.swing.JTextField delayTf;
    private javax.swing.JLabel delayTitleLbl;
    private javax.swing.JButton delcsvBtn;
    private javax.swing.JButton exitbtn;
    private javax.swing.JPanel indicator1;
    private javax.swing.JPanel indicator2;
    private javax.swing.JPanel indicator3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadcsvBtn;
    private javax.swing.JButton minbtn;
    private javax.swing.JPanel pnlHead;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField repOverHourTf;
    private javax.swing.JTextField repTotalSampTf;
    private javax.swing.JPanel reportBtn;
    private javax.swing.JPanel reportDash;
    private javax.swing.JPanel sampleDash;
    private javax.swing.JLabel sampleLbl;
    private javax.swing.JLabel sampleLbl1;
    private javax.swing.JLabel sampleLbl2;
    private javax.swing.JTextField sampleNumTableTf;
    private javax.swing.JTable sampleTable;
    private javax.swing.JPanel samplebtn;
    private javax.swing.JButton setCsvPathBtn;
    private javax.swing.JPanel settingsBtn;
    private javax.swing.JPanel settingsDash;
    private javax.swing.JTextField testsGroup1Tf;
    private javax.swing.JTextField testsGroup2Tf;
    private javax.swing.JTextField testsGroup3Tf;
    private javax.swing.JTextField testsTf;
    private javax.swing.JTextField timeGroup1Tf;
    private javax.swing.JTextField timeGroup2Tf;
    private javax.swing.JTextField timeGroup3Tf;
    private javax.swing.JLabel timeTf;
    private javax.swing.JPanel toppnl;
    private javax.swing.JPanel totalSamplepanel;
    private javax.swing.JTextField totalsampleTf;
    private javax.swing.JLabel totalsamplesLbl;
    // End of variables declaration//GEN-END:variables
}
