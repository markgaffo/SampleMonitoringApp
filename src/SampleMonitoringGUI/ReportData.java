package SampleMonitoringGUI;


public class ReportData{
    public int sampleNumber;
    public String Date, Department, Tests, RequestTime,
           StartTime, ReportedDate, ReportedTime, FinishedTime;
    
    public ReportData(){
    }
    
    public ReportData(int samplenumber, String date, String department, String tests,
            String requesttime, String starttime, String reporteddate, String reportedtime,
            String finishedtime){
            this.sampleNumber = samplenumber;
            Date = date;
            Department = department;
            Tests = tests;
            RequestTime = requesttime;
            StartTime = starttime;
            ReportedDate = reporteddate;
            ReportedTime = reportedtime;
            FinishedTime = finishedtime;
	}
    
    public int getSampleNumber(){
        return sampleNumber;
    }
    public void setSampleNumber(int sampleNumber){
        this.sampleNumber = sampleNumber;
    }

    public String getDate(){
        return Date;
    }
    public void setDate(String date){
        Date = date;
    }

    public String getDepartment(){
        return Department;
    }
    public void setDepartment(String department){
        Department = department;
    }

    public String getTests(){
        return Tests;
    }
    public void setTests(String tests){
        Tests = tests;
    }

    public String getRequestTime(){
        return RequestTime;
    }
    public void setRequestTime(String requesttime){
        RequestTime = requesttime;
    }

    public String getStartTime(){
        return StartTime;
    }
    public void setStartTime(String starttime){
        StartTime = starttime;
    }
    
    public String getReportedDate(){
        return ReportedDate;
    }
    public void setReportedDate(String reporteddate){
        ReportedDate = reporteddate;
    }
    
     public String getReportedTime(){
        return ReportedTime;
    }
    public void setReportedTime(String reportedtime){
        ReportedTime = reportedtime;
    }
    
     public String getFinishedTime(){
        return FinishedTime;
    }
    public void setFinishedTime(String finishedtime){
        FinishedTime = finishedtime;
    }
}
