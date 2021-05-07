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
import java.sql.*;
import com.opencsv.CSVReader;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

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
                System.out.print("UPDATED TABLE");

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    };
    
    public GUIDashboard() {
        initComponents();
        clock();

        UpdateTable();   
        setTableParmas();
        renderChart();
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
            if(timeDifHour < 1){
                if(timeDifMin < 15){
                    progressBar.setValue(50);
                    progressBar.setStringPainted(true); 
                }else{
                    progressBar.setValue(25);
                    progressBar.setStringPainted(true); 
                }
            }else{
                progressBar.setValue(0);
                progressBar.setStringPainted(true); 
            } 
        }catch(ParseException ex){
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void UpdateTable(){
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

    
    private void renderChart(){
      final CategoryDataset dataset1 = createDataset1();

        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            "TAT Stats",        // chart title
            "Date",               // domain axis label
            "% TAT > 1 hour",                  // range axis label
            dataset1,                 // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips?
            false                     // URL generator?  Not required...
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xEE, 0xEE, 0xFF));
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        plot.getDomainAxis().setCategoryMargin(0.5);
        ((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
        BarRenderer barRenderer = (BarRenderer)plot.getRenderer();
        barRenderer.setSeriesPaint(0, Color.cyan);
        barRenderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        barRenderer.setSeriesItemLabelsVisible(1, true);
        barRenderer.setBaseItemLabelsVisible(true);
        barRenderer.setBaseSeriesVisible(true);
         
        
        final CategoryDataset dataset2 = createDataset2();
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
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        
        // add the chart to a panel...
        final ChartPanel cp = new ChartPanel(chart);
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.validate();

    }

    
    private CategoryDataset createDataset1() {

        // row keys...
        final String series1 = "% Over an Hour";

        // column keys...
        //for loop for date columns
        //if (lstRecords != null) {
        //  for(int i = 0; i < lstRecords.size(); i++){
        //      dto = lstRecords.get(i);
        //      String Dates = dto.getDate();
        //  }
        //}
        final String category1 = "22/04";
        final String category2 = "23/04";
        final String category3 = "24/04";
        final String category4 = "25/04";
        final String category5 = "26/04";
        final String category6 = "27/04";
        final String category7 = "28/04";
        final String category8 = "29/04";
        final String category9 = "30/04";
        final String category10 = "31/04";
        

        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        //for loop for each day value
        //loop 1st and 3rd value
        dataset.addValue(10, series1, category1);
        dataset.addValue(4.1, series1, category2);
        dataset.addValue(3.6, series1, category3);
        dataset.addValue(5.4, series1, category4);
        dataset.addValue(4.2, series1, category5);
        dataset.addValue(11.1, series1, category6);
        dataset.addValue(2.0, series1, category7);
        dataset.addValue(3.4, series1, category8);
        dataset.addValue(12.4, series1, category9);
        dataset.addValue(5.4, series1, category10);        

        return dataset;
    }

    private CategoryDataset createDataset2() {

        // row keys...
        final String series1 = "Average minutes";

        // column keys...
        final String category1 = "22/04";
        final String category2 = "23/04";
        final String category3 = "24/04";
        final String category4 = "25/04";
        final String category5 = "26/04";
        final String category6 = "27/04";
        final String category7 = "28/04";
        final String category8 = "29/04";
        final String category9 = "30/04";
        final String category10 = "31/04";
        
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(25.0, series1, category1);
        dataset.addValue(24.0, series1, category2);
        dataset.addValue(31.0, series1, category3);
        dataset.addValue(25.0, series1, category4);
        dataset.addValue(46.0, series1, category5);
        dataset.addValue(42.0, series1, category6);
        dataset.addValue(37.0, series1, category7);
        dataset.addValue(28.0, series1, category8);
        dataset.addValue(49.0, series1, category9);
        dataset.addValue(36.0, series1, category10);  
        
        return dataset;

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
//        System.out.println("Size : " + dataList.size());

        return dataList;
    }
    
    public static boolean readAndWriteCSVFileToReportDatabase(String fileName) {    
        try{
            FileReader filereader = new FileReader(fileName); 
            CSVReader csvReader = new CSVReader(filereader); 
            String[] nextRecord; 
            ArrayList<ReportData> rdataList = new ArrayList<ReportData>();
            while((nextRecord = csvReader.readNext()) != null){
                System.out.println(nextRecord[0]);
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
        loadcsvBtn = new javax.swing.JButton();
        delcsvBtn = new javax.swing.JButton();
        chartPanel = new javax.swing.JPanel();
        dateSelect = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        dateToTf = new com.toedter.calendar.JDateChooser();
        dateFromTf = new com.toedter.calendar.JDateChooser();
        dateApplyBtn = new javax.swing.JButton();
        settingsDash = new javax.swing.JPanel();
        testsGroup1Tf = new javax.swing.JTextField();
        testsGroup2Tf = new javax.swing.JTextField();
        testsGroup3Tf = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        screenalert1Box = new javax.swing.JCheckBox();
        screenalert2Box = new javax.swing.JCheckBox();
        screenalert3Box = new javax.swing.JCheckBox();
        screenalert1Box1 = new javax.swing.JCheckBox();
        screenalert2Box1 = new javax.swing.JCheckBox();
        screenalert3Box1 = new javax.swing.JCheckBox();
        screenalert1Box2 = new javax.swing.JCheckBox();
        screenalert2Box2 = new javax.swing.JCheckBox();
        screenalert3Box2 = new javax.swing.JCheckBox();
        setCsvPathBtn = new javax.swing.JButton();
        timeGroup1Tf = new javax.swing.JTextField();
        timeGroup2Tf = new javax.swing.JTextField();
        timeGroup3Tf = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        defaultAlertTimeTf = new javax.swing.JTextField();
        applyBtn1 = new javax.swing.JButton();
        sampleDash = new javax.swing.JPanel();
        totalSamplepanel = new javax.swing.JPanel();
        totalsamplesLbl = new javax.swing.JLabel();
        averageTimeLbl = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        totalsampleTf = new javax.swing.JTextField();
        deleteBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sampleTable = new javax.swing.JTable();
        csvBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        sampleNumTableTf = new javax.swing.JTextField();
        delayBtn = new javax.swing.JButton();
        delayTf = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        testsTf = new javax.swing.JTextField();

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
        exitbtn.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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

        javax.swing.GroupLayout pnlHeadLayout = new javax.swing.GroupLayout(pnlHead);
        pnlHead.setLayout(pnlHeadLayout);
        pnlHeadLayout.setHorizontalGroup(
            pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeadLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(minbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(exitbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        samplebtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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
        reportBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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
            .addGap(0, 212, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(indicator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        settingsBtn.setBackground(new java.awt.Color(255, 255, 255));
        settingsBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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
                .addContainerGap(61, Short.MAX_VALUE))
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
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        defaultpageLayout.setVerticalGroup(
            defaultpageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 503, Short.MAX_VALUE)
        );

        bodypnl.add(defaultpage, "card2");

        loadcsvBtn.setBackground(new java.awt.Color(255, 255, 255));
        loadcsvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        loadcsvBtn.setForeground(new java.awt.Color(8, 118, 188));
        loadcsvBtn.setText("Load csv file");
        loadcsvBtn.setContentAreaFilled(false);
        loadcsvBtn.setOpaque(true);
        loadcsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadcsvBtnActionPerformed(evt);
            }
        });

        delcsvBtn.setBackground(new java.awt.Color(255, 255, 255));
        delcsvBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        delcsvBtn.setForeground(new java.awt.Color(8, 118, 188));
        delcsvBtn.setText("Clear file");
        delcsvBtn.setContentAreaFilled(false);
        delcsvBtn.setOpaque(true);
        delcsvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delcsvBtnActionPerformed(evt);
            }
        });

        chartPanel.setLayout(new java.awt.BorderLayout());

        jLabel16.setText("To:");

        jLabel15.setText("From:");

        dateToTf.setDateFormatString("dd/MM/yyyy");

        dateFromTf.setDateFormatString("dd/MM/yyyy");

        dateApplyBtn.setBackground(new java.awt.Color(255, 255, 255));
        dateApplyBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        dateApplyBtn.setForeground(new java.awt.Color(8, 118, 188));
        dateApplyBtn.setText("Apply");
        dateApplyBtn.setContentAreaFilled(false);
        dateApplyBtn.setOpaque(true);
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
                .addGroup(dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dateApplyBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(dateToTf, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                    .addComponent(dateFromTf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        dateSelectLayout.setVerticalGroup(
            dateSelectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dateSelectLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateFromTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel16)
                .addGap(2, 2, 2)
                .addComponent(dateToTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dateApplyBtn)
                .addContainerGap())
        );

        javax.swing.GroupLayout reportDashLayout = new javax.swing.GroupLayout(reportDash);
        reportDash.setLayout(reportDashLayout);
        reportDashLayout.setHorizontalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 781, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reportDashLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loadcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(delcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(18, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, reportDashLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dateSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28))))
        );
        reportDashLayout.setVerticalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(dateSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(194, 194, 194)
                .addComponent(loadcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        bodypnl.add(reportDash, "card4");

        settingsDash.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        testsGroup1Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        testsGroup1Tf.setText("GLUC, DP, RNL");
        settingsDash.add(testsGroup1Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, 386, 34));

        testsGroup2Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        testsGroup2Tf.setText("TR, UL, PXU");
        settingsDash.add(testsGroup2Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 386, 34));

        testsGroup3Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        testsGroup3Tf.setText("LP, DP, CRP");
        settingsDash.add(testsGroup3Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 386, 34));

        jLabel3.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 118, 188));
        jLabel3.setText("Test Group 1 (Moderate Priority)");
        settingsDash.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, 24));

        jLabel4.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(8, 118, 188));
        jLabel4.setText("Set Path to CSV File:");
        settingsDash.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, -1, 24));

        jLabel5.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(8, 118, 188));
        jLabel5.setText("Test Group 2 (High Priority)");
        settingsDash.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, 24));

        screenalert1Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box.setText("Screen Alert");
        settingsDash.add(screenalert1Box, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 120, -1, 20));

        screenalert2Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box.setText("SMS Alert");
        screenalert2Box.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert2Box.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert2Box.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert2Box, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 120, -1, 20));

        screenalert3Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box.setText("Email Alert");
        screenalert3Box.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert3Box.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert3Box.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert3Box, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 120, -1, 20));

        screenalert1Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box1.setText("Screen Alert");
        screenalert1Box1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenalert1Box1ActionPerformed(evt);
            }
        });
        settingsDash.add(screenalert1Box1, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 200, -1, 20));

        screenalert2Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box1.setText("SMS Alert");
        screenalert2Box1.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert2Box1.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert2Box1.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert2Box1, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 200, -1, 20));

        screenalert3Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box1.setText("Email Alert");
        screenalert3Box1.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert3Box1.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert3Box1.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert3Box1, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 200, -1, 20));

        screenalert1Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box2.setText("Screen Alert");
        settingsDash.add(screenalert1Box2, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 280, -1, 20));

        screenalert2Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box2.setText("SMS Alert");
        screenalert2Box2.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert2Box2.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert2Box2.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert2Box2, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 280, -1, 20));

        screenalert3Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box2.setText("Email Alert");
        screenalert3Box2.setMaximumSize(new java.awt.Dimension(103, 27));
        screenalert3Box2.setMinimumSize(new java.awt.Dimension(103, 27));
        screenalert3Box2.setPreferredSize(new java.awt.Dimension(103, 27));
        settingsDash.add(screenalert3Box2, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 280, -1, 20));

        setCsvPathBtn.setBackground(new java.awt.Color(255, 255, 255));
        setCsvPathBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        setCsvPathBtn.setForeground(new java.awt.Color(8, 118, 188));
        setCsvPathBtn.setText("Set CSV file Path");
        setCsvPathBtn.setContentAreaFilled(false);
        setCsvPathBtn.setOpaque(true);
        setCsvPathBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setCsvPathBtnActionPerformed(evt);
            }
        });
        settingsDash.add(setCsvPathBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 420, 280, -1));

        timeGroup1Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup1Tf.setText("30");
        settingsDash.add(timeGroup1Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 110, -1, 34));

        timeGroup2Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup2Tf.setText("45");
        settingsDash.add(timeGroup2Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 190, -1, 33));

        timeGroup3Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timeGroup3Tf.setText("25");
        settingsDash.add(timeGroup3Tf, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 270, -1, 33));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(8, 118, 188));
        jLabel6.setText("Mins");
        settingsDash.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 340, -1, -1));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(8, 118, 188));
        jLabel7.setText("Mins");
        settingsDash.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 280, -1, -1));

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(8, 118, 188));
        jLabel11.setText("Mins");
        settingsDash.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 200, -1, -1));

        jLabel13.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(8, 118, 188));
        jLabel13.setText("Test Group 3 (Urgent)");
        settingsDash.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 240, -1, 24));

        jLabel10.setFont(new java.awt.Font("Sitka Small", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(8, 118, 188));
        jLabel10.setText("Adjust alert times here");
        settingsDash.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 20, -1, 24));

        jLabel12.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(8, 118, 188));
        jLabel12.setText("Alert time default:");
        settingsDash.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 340, -1, 20));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(8, 118, 188));
        jLabel14.setText("Mins");
        settingsDash.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 120, -1, -1));

        defaultAlertTimeTf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        defaultAlertTimeTf.setText("40");
        settingsDash.add(defaultAlertTimeTf, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 334, -1, 30));

        applyBtn1.setBackground(new java.awt.Color(255, 255, 255));
        applyBtn1.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        applyBtn1.setForeground(new java.awt.Color(8, 118, 188));
        applyBtn1.setText("Apply Changes");
        applyBtn1.setContentAreaFilled(false);
        applyBtn1.setOpaque(true);
        applyBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyBtn1ActionPerformed(evt);
            }
        });
        settingsDash.add(applyBtn1, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 420, -1, -1));

        bodypnl.add(settingsDash, "card5");

        sampleDash.setBackground(new java.awt.Color(245, 245, 245));

        totalSamplepanel.setBackground(new java.awt.Color(247, 247, 247));

        totalsamplesLbl.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        totalsamplesLbl.setForeground(new java.awt.Color(8, 118, 188));
        totalsamplesLbl.setText("Total Number of Samples:");

        averageTimeLbl.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        averageTimeLbl.setForeground(new java.awt.Color(8, 118, 188));
        averageTimeLbl.setText("Average Time To Report:");

        jLabel8.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        jLabel8.setText("20");

        jLabel9.setFont(new java.awt.Font("Sitka Heading", 0, 11)); // NOI18N
        jLabel9.setText("Mins");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel8)
                .addGap(2, 2, 2)
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        totalsampleTf.setEditable(false);
        totalsampleTf.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout totalSamplepanelLayout = new javax.swing.GroupLayout(totalSamplepanel);
        totalSamplepanel.setLayout(totalSamplepanelLayout);
        totalSamplepanelLayout.setHorizontalGroup(
            totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalSamplepanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addComponent(totalsamplesLbl)
                        .addGap(11, 11, 11))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, totalSamplepanelLayout.createSequentialGroup()
                        .addComponent(averageTimeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        totalSamplepanelLayout.setVerticalGroup(
            totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, totalSamplepanelLayout.createSequentialGroup()
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(totalsamplesLbl))
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(averageTimeLbl)
                        .addGap(33, 33, 33))
                    .addGroup(totalSamplepanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        deleteBtn.setText("Delete Database");
        deleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBtnActionPerformed(evt);
            }
        });

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

        csvBtn.setText("Load CSV");
        csvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvBtnActionPerformed(evt);
            }
        });

        sampleNumTableTf.setEditable(false);
        sampleNumTableTf.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sampleNumTableTf.setForeground(new java.awt.Color(8, 118, 188));
        sampleNumTableTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleNumTableTfActionPerformed(evt);
            }
        });

        delayBtn.setBackground(new java.awt.Color(255, 255, 255));
        delayBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        delayBtn.setForeground(new java.awt.Color(8, 118, 188));
        delayBtn.setText("Apply Changes");
        delayBtn.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        delayBtn.setContentAreaFilled(false);
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

        testsTf.setEditable(false);
        testsTf.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        testsTf.setForeground(new java.awt.Color(8, 118, 188));
        testsTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testsTfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(delayBtn)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(sampleNumTableTf, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(testsTf))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sampleNumTableTf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testsTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delayBtn))
        );

        javax.swing.GroupLayout sampleDashLayout = new javax.swing.GroupLayout(sampleDash);
        sampleDash.setLayout(sampleDashLayout);
        sampleDashLayout.setHorizontalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(sampleDashLayout.createSequentialGroup()
                        .addComponent(csvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(totalSamplepanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        sampleDashLayout.setVerticalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sampleDashLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(sampleDashLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(totalSamplepanel, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(csvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        bodypnl.add(sampleDash, "card3");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlHead, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
            .addComponent(bodypnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(toppnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        // TODO add your handling code here:
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
        renderChart();
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

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
        try {                                          
            int i = csvCon.deleteAllFromDatabase();
            if(i == 0){
                JOptionPane.showMessageDialog(this, "Database has no record now.");
            }else{
                JOptionPane.showMessageDialog(this, "Database is already empty Or Error deleting data.");
            }
            UpdateTable();
            
        }catch(SQLException ex){
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void csvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvBtnActionPerformed
        try {
            readAndWriteCSVFileToDatabase();
        } catch (SQLException ex) {
            Logger.getLogger(GUIDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        UpdateTable();
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
        // TODO add your handling code here:
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
        int i = repCon.deleteAllFromDatabase();
        if(i > 0){
            JOptionPane.showMessageDialog(this, "The file has been removed");
        }else{
            JOptionPane.showMessageDialog(this, "There is currently no file");
	}
    }//GEN-LAST:event_delcsvBtnActionPerformed

    private void sampleNumTableTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleNumTableTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sampleNumTableTfActionPerformed

    private void delayBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delayBtnActionPerformed
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
    }//GEN-LAST:event_delayBtnActionPerformed

    private void delayBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseEntered
        delayBtn.setBackground(new Color(240,240,240));
    }//GEN-LAST:event_delayBtnMouseEntered

    private void delayBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseExited
        delayBtn.setBackground(new Color(255,255,255));
    }//GEN-LAST:event_delayBtnMouseExited

    private void delayBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delayBtnMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_delayBtnMouseClicked

    private void testsTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testsTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_testsTfActionPerformed

    private void sampleTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleTableKeyPressed
        
    }//GEN-LAST:event_sampleTableKeyPressed

    private void sampleTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sampleTableKeyReleased
 
    }//GEN-LAST:event_sampleTableKeyReleased

    private void setCsvPathBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setCsvPathBtnActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.csv", "csv");
        fc.setFileFilter(filter);
        int i = fc.showOpenDialog(null);

        if (i == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            String filepath = f.getPath();
            setCsvPathBtn.setName(filepath);
        }
    }//GEN-LAST:event_setCsvPathBtnActionPerformed

    private void screenalert1Box1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenalert1Box1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_screenalert1Box1ActionPerformed

    private void dateApplyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateApplyBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateApplyBtnActionPerformed

    private void applyBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtn1ActionPerformed
        CSVFilePath.path = setCsvPathBtn.getName();
        System.out.println(CSVFilePath.path);
        ReadWrite.writeFile();
    }//GEN-LAST:event_applyBtn1ActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUIDashboard().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBtn1;
    private javax.swing.JLabel averageTimeLbl;
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
    private javax.swing.JButton delayBtn;
    private javax.swing.JTextField delayTf;
    private javax.swing.JButton delcsvBtn;
    private javax.swing.JButton deleteBtn;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadcsvBtn;
    private javax.swing.JButton minbtn;
    private javax.swing.JPanel pnlHead;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel reportBtn;
    private javax.swing.JPanel reportDash;
    private javax.swing.JPanel sampleDash;
    private javax.swing.JLabel sampleLbl;
    private javax.swing.JLabel sampleLbl1;
    private javax.swing.JLabel sampleLbl2;
    private javax.swing.JTextField sampleNumTableTf;
    private javax.swing.JTable sampleTable;
    private javax.swing.JPanel samplebtn;
    private javax.swing.JCheckBox screenalert1Box;
    private javax.swing.JCheckBox screenalert1Box1;
    private javax.swing.JCheckBox screenalert1Box2;
    private javax.swing.JCheckBox screenalert2Box;
    private javax.swing.JCheckBox screenalert2Box1;
    private javax.swing.JCheckBox screenalert2Box2;
    private javax.swing.JCheckBox screenalert3Box;
    private javax.swing.JCheckBox screenalert3Box1;
    private javax.swing.JCheckBox screenalert3Box2;
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
