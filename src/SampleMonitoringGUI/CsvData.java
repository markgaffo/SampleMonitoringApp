package SampleMonitoringGUI;

public class CsvData {

	int sampleNumber;
	String Date, Department, Tests, Requesttime, Starttime; 
	
	public CsvData(){
	}
	public CsvData(int sampleNumber, String date, String department, String tests, String requesttime, String starttime) {
            this.sampleNumber = sampleNumber;
            Date = date;
            Department = department;
            Tests = tests;
            Requesttime = requesttime;
            Starttime = starttime;
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

	public String getRequesttime(){
            return Requesttime;
	}
	public void setRequesttime(String requesttime){
            Requesttime = requesttime;
	}

	public String getStarttime(){
            return Starttime;
	}
	public void setStarttime(String starttime){
            Starttime = starttime;
	}
}
