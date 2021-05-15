/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SampleMonitoringGUI;

/**
 *
 * @author Mark
 */
public class DateOverHour {
	String Date;
        double OverHr, AvgTime;
	
	public DateOverHour(){
	}
	public DateOverHour(String date, double overHr, double avgTime) {
            Date = date;
            OverHr = overHr;
            AvgTime = avgTime;
	}

	public String getDate(){
            return Date;
	}
	public void setDate(String date){
            this.Date = date;
	}
        public double getOverHr(){
            return OverHr;
	}
	public void setOverHr(double overHr){
            this.OverHr = overHr;
	}
        public double getAvgTime(){
            return AvgTime;
	}
	public void setAvgTime(double avgTime){
            this.AvgTime = avgTime;
	}
      
}
