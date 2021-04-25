package SampleMonitoringGUI;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;
import java.sql.*;
import com.opencsv.CSVReader;
import java.awt.Font;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GUIDashboard extends javax.swing.JFrame {

    static CsvDBConnection csvCon = new CsvDBConnection();
    static ReportDBConnection repCon = new ReportDBConnection();
    DefaultTableModel model;
    
    Connection csvConn = null;
    Connection repConn = null;
    PreparedStatement prep = null;
    ResultSet rs = null;
    
    int xMouse;
    int yMouse;
    public static String csvFile = "/Users/Mark/Dropbox/Final year/Project/netbeans projects/SampleMonitoringApp/ProjectTabledata.csv";
    public static String line ="";
    public static String sampleNum;
    
    public GUIDashboard() {
        initComponents();
        csvConn=CsvDBConnection.CsvDBConnection();
        UpdateTable();   
        CurrentDate();
        setTableParmas();
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
    
    public void CurrentDate(){
        Calendar calender = new GregorianCalendar();
        int day = calender.get(Calendar.DAY_OF_MONTH);
        int month = calender.get(Calendar.MONTH);
        int year = calender.get(Calendar.YEAR);
        
        dateTf.setText("Current Date: "+day+"/"+(month+1)+"/"+year);  
    }
    
    private void UpdateTable(){
        try {
            String sql = "select * from csv_table";
            prep=csvConn.prepareStatement(sql);
            rs=prep.executeQuery();
            sampleTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }finally{
            try{
                rs.close();
                prep.close();
            }catch(Exception e){
                
            }
        }
    }

    List<CsvData> lstRecords = null;

    public void searchAndFillTable() {
        lstRecords = csvCon.getDataFromDatabase();
        loadDataInTable();
    }   
    public void loadDataInTable() {
        model = (DefaultTableModel)sampleTable.getModel();
        model.setRowCount(0);
        CsvData dto;
        if (lstRecords != null) {
            model.setRowCount(lstRecords.size());
            for (int i = 0; i < lstRecords.size(); i++) {
                dto = lstRecords.get(i);
                model.setValueAt(dto.getSampleNumber(), i, 0);
                model.setValueAt(dto.getDate(), i, 1);
                model.setValueAt(dto.getDepartment(), i, 2);
                model.setValueAt(dto.getTests(), i, 3);
                model.setValueAt(dto.getRequesttime(), i, 4);
                model.setValueAt(dto.getStarttime(), i, 5);
            }
        }
    }

    public static boolean readAndWriteCSVFileToDatabase() {    
        try{
            FileReader filereader = new FileReader(csvFile); 
            CSVReader csvReader = new CSVReader(filereader); 
            String[] nextRecord; 
            ArrayList<CsvData> dataList = new ArrayList<CsvData>();
            while((nextRecord = csvReader.readNext()) != null){
                //System.out.println(nextRecord[3]);
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
                rdata.setDate(nextRecord[1]);
                rdata.setDepartment(nextRecord[2]);
                rdata.setTests(nextRecord[3]);
                rdata.setRequestTime(nextRecord[4]);
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
                rdata.setReportedDate(nextRecord[6]);
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
        bodypnl = new javax.swing.JPanel();
        defaultpage = new javax.swing.JPanel();
        reportDash = new javax.swing.JPanel();
        graph = new javax.swing.JLabel();
        monthlyrepBtn = new javax.swing.JButton();
        loadcsvBtn = new javax.swing.JButton();
        delcsvBtn = new javax.swing.JButton();
        settingsDash = new javax.swing.JPanel();
        group1Tf = new javax.swing.JTextField();
        group2Tf = new javax.swing.JTextField();
        group3Tf = new javax.swing.JTextField();
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
        applyBtn = new javax.swing.JButton();
        timechange1Tf = new javax.swing.JTextField();
        timechange2Tf = new javax.swing.JTextField();
        timechange3Tf = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        group3Tf2 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        sampleDash = new javax.swing.JPanel();
        totalSamplepanel = new javax.swing.JPanel();
        totalsamplesLbl = new javax.swing.JLabel();
        averageTimeLbl = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        delayTf = new javax.swing.JTextField();
        delayBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sampleTable = new javax.swing.JTable();
        csvBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Dashboard");
        setUndecorated(true);
        setSize(new java.awt.Dimension(1000, 750));

        pnlHead.setBackground(new java.awt.Color(4, 68, 108));
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
                .addComponent(exitbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlHeadLayout.setVerticalGroup(
            pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeadLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitbtn)
                    .addComponent(minbtn)))
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
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toppnlLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dateTf, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        toppnlLayout.setVerticalGroup(
            toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toppnlLayout.createSequentialGroup()
                .addGroup(toppnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(toppnlLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toppnlLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dateTf, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(56, 56, 56)
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
            .addGap(0, 495, Short.MAX_VALUE)
        );

        bodypnl.add(defaultpage, "card2");

        graph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SampleMonitoringImages/graph.PNG"))); // NOI18N

        monthlyrepBtn.setBackground(new java.awt.Color(255, 255, 255));
        monthlyrepBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        monthlyrepBtn.setForeground(new java.awt.Color(8, 118, 188));
        monthlyrepBtn.setText("3 Month Report");
        monthlyrepBtn.setContentAreaFilled(false);
        monthlyrepBtn.setOpaque(true);
        monthlyrepBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthlyrepBtnActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout reportDashLayout = new javax.swing.GroupLayout(reportDash);
        reportDash.setLayout(reportDashLayout);
        reportDashLayout.setHorizontalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(graph)
                .addGap(18, 18, 18)
                .addGroup(reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(monthlyrepBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(100, Short.MAX_VALUE))
        );
        reportDashLayout.setVerticalGroup(
            reportDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportDashLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(graph)
                .addGap(19, 19, 19))
            .addGroup(reportDashLayout.createSequentialGroup()
                .addGap(149, 149, 149)
                .addComponent(monthlyrepBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(loadcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(delcsvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        bodypnl.add(reportDash, "card4");

        group1Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        group1Tf.setText("GLUC, DP, RNL");

        group2Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        group2Tf.setText("TR, UL, PXU");

        group3Tf.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        group3Tf.setText("LP, DP, CRP");

        jLabel3.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(8, 118, 188));
        jLabel3.setText("Test Group 1 (Moderate Priority)");

        jLabel4.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(8, 118, 188));
        jLabel4.setText("Set Path to CSV File");

        jLabel5.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(8, 118, 188));
        jLabel5.setText("Test Group 2 (High Priority)");

        screenalert1Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box.setText("Screen Alert");

        screenalert2Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box.setText("SMS Alert");

        screenalert3Box.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box.setText("Email Alert");

        screenalert1Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box1.setText("Screen Alert");

        screenalert2Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box1.setText("SMS Alert");

        screenalert3Box1.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box1.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box1.setText("Email Alert");

        screenalert1Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert1Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert1Box2.setText("Screen Alert");

        screenalert2Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert2Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert2Box2.setText("SMS Alert");

        screenalert3Box2.setFont(new java.awt.Font("Sitka Heading", 1, 14)); // NOI18N
        screenalert3Box2.setForeground(new java.awt.Color(8, 118, 188));
        screenalert3Box2.setText("Email Alert");

        applyBtn.setBackground(new java.awt.Color(255, 255, 255));
        applyBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        applyBtn.setForeground(new java.awt.Color(8, 118, 188));
        applyBtn.setText("Apply Changes");
        applyBtn.setContentAreaFilled(false);
        applyBtn.setOpaque(true);

        timechange1Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timechange1Tf.setText("30");

        timechange2Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timechange2Tf.setText("45");

        timechange3Tf.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        timechange3Tf.setText("25");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(8, 118, 188));
        jLabel6.setText("Mins");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(8, 118, 188));
        jLabel7.setText("Mins");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(8, 118, 188));
        jLabel11.setText("Mins");

        group3Tf2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        group3Tf2.setText("/Users/Mark/Dropbox/Final year/Project/csvFiles/SampleData.csv");

        jLabel13.setFont(new java.awt.Font("Sitka Small", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(8, 118, 188));
        jLabel13.setText("Test Group 3 (Urgent)");

        javax.swing.GroupLayout settingsDashLayout = new javax.swing.GroupLayout(settingsDash);
        settingsDash.setLayout(settingsDashLayout);
        settingsDashLayout.setHorizontalGroup(
            settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsDashLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(applyBtn)
                .addGap(25, 25, 25))
            .addGroup(settingsDashLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsDashLayout.createSequentialGroup()
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(group3Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(group2Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(group1Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(settingsDashLayout.createSequentialGroup()
                                        .addComponent(timechange1Tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel6))
                                    .addGroup(settingsDashLayout.createSequentialGroup()
                                        .addComponent(timechange2Tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel11))
                                    .addGroup(settingsDashLayout.createSequentialGroup()
                                        .addComponent(timechange3Tf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel7))))
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addComponent(screenalert1Box)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert2Box)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert3Box))
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addComponent(screenalert1Box1)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert2Box1)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert3Box1))
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addComponent(screenalert1Box2)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert2Box2)
                                .addGap(29, 29, 29)
                                .addComponent(screenalert3Box2)))
                        .addGap(69, 69, 69))
                    .addGroup(settingsDashLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(settingsDashLayout.createSequentialGroup()
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(group3Tf2, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        settingsDashLayout.setVerticalGroup(
            settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsDashLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsDashLayout.createSequentialGroup()
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(timechange1Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6))
                            .addComponent(group1Tf))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(group2Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 1, Short.MAX_VALUE))
                            .addGroup(settingsDashLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(timechange2Tf, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                                    .addComponent(jLabel11))))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(group3Tf, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timechange3Tf, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                            .addComponent(jLabel7)))
                    .addGroup(settingsDashLayout.createSequentialGroup()
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(screenalert2Box, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert1Box, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert3Box, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(46, 46, 46)
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(screenalert2Box1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert1Box1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert3Box1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(45, 45, 45)
                        .addGroup(settingsDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(screenalert2Box2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert1Box2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(screenalert3Box2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(42, 42, 42)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(group3Tf2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(applyBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addGap(29, 29, 29))
        );

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

        jLabel10.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        jLabel10.setText("27");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(totalSamplepanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(averageTimeLbl, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        delayTf.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        delayTf.setForeground(new java.awt.Color(8, 118, 188));
        delayTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delayTfActionPerformed(evt);
            }
        });

        delayBtn.setBackground(new java.awt.Color(255, 255, 255));
        delayBtn.setFont(new java.awt.Font("Sitka Heading", 1, 18)); // NOI18N
        delayBtn.setForeground(new java.awt.Color(8, 118, 188));
        delayBtn.setText("Delay Sample");
        delayBtn.setContentAreaFilled(false);

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
        jScrollPane1.setViewportView(sampleTable);

        csvBtn.setText("Load CSV");
        csvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sampleDashLayout = new javax.swing.GroupLayout(sampleDash);
        sampleDash.setLayout(sampleDashLayout);
        sampleDashLayout.setHorizontalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalSamplepanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(sampleDashLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(delayBtn))
                    .addGroup(sampleDashLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(csvBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );
        sampleDashLayout.setVerticalGroup(
            sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(totalSamplepanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(sampleDashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delayTf, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delayBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(87, 87, 87)
                .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(csvBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(81, Short.MAX_VALUE))
            .addGroup(sampleDashLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bodypnl.add(sampleDash, "card3");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlHead, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(bodypnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(toppnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlHead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
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
        int i = csvCon.deleteAllFromDatabase();
        if(i > 0){
            JOptionPane.showMessageDialog(this, "Database has no record now.");
        }else{
            JOptionPane.showMessageDialog(this, "Database is already empty Or Error deleting data.");
	}
	searchAndFillTable();
    }//GEN-LAST:event_deleteBtnActionPerformed

    private void csvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvBtnActionPerformed
        readAndWriteCSVFileToDatabase();
        UpdateTable();
    }//GEN-LAST:event_csvBtnActionPerformed

    private void sampleTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sampleTableMouseClicked
        try{
            int row = sampleTable.getSelectedRow();
            String table_click=(sampleTable.getModel().getValueAt(row, 0).toString());
            sampleNum=table_click;
            String sql ="select * from csv_table where SampleNumber ='"+table_click+"' ";
            prep=csvConn.prepareStatement(sql);
            rs=prep.executeQuery();
            if(rs.next()){
                String add = rs.getString("StartTime");
                delayTf.setText(add);
            } 
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
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

    private void monthlyrepBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthlyrepBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_monthlyrepBtnActionPerformed

    private void delcsvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delcsvBtnActionPerformed
        int i = repCon.deleteAllFromDatabase();
        if(i > 0){
            JOptionPane.showMessageDialog(this, "The file has been removed");
        }else{
            JOptionPane.showMessageDialog(this, "There is currently no file");
	}
    }//GEN-LAST:event_delcsvBtnActionPerformed

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
    private javax.swing.JButton applyBtn;
    private javax.swing.JLabel averageTimeLbl;
    private javax.swing.JPanel bodypnl;
    private javax.swing.JButton csvBtn;
    private javax.swing.JLabel dateTf;
    private javax.swing.JPanel defaultpage;
    private javax.swing.JButton delayBtn;
    private javax.swing.JTextField delayTf;
    private javax.swing.JButton delcsvBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton exitbtn;
    private javax.swing.JLabel graph;
    private javax.swing.JTextField group1Tf;
    private javax.swing.JTextField group2Tf;
    private javax.swing.JTextField group3Tf;
    private javax.swing.JTextField group3Tf2;
    private javax.swing.JPanel indicator1;
    private javax.swing.JPanel indicator2;
    private javax.swing.JPanel indicator3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadcsvBtn;
    private javax.swing.JButton minbtn;
    private javax.swing.JButton monthlyrepBtn;
    private javax.swing.JPanel pnlHead;
    private javax.swing.JPanel reportBtn;
    private javax.swing.JPanel reportDash;
    private javax.swing.JPanel sampleDash;
    private javax.swing.JLabel sampleLbl;
    private javax.swing.JLabel sampleLbl1;
    private javax.swing.JLabel sampleLbl2;
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
    private javax.swing.JPanel settingsBtn;
    private javax.swing.JPanel settingsDash;
    private javax.swing.JTextField timechange1Tf;
    private javax.swing.JTextField timechange2Tf;
    private javax.swing.JTextField timechange3Tf;
    private javax.swing.JPanel toppnl;
    private javax.swing.JPanel totalSamplepanel;
    private javax.swing.JLabel totalsamplesLbl;
    // End of variables declaration//GEN-END:variables
}
