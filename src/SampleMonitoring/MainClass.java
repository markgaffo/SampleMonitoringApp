package SampleMonitoring;

import java.time.LocalDate;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * x17443036
 * @author Mark
 */
public class MainClass {
    public static void main(String[] args){
        
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            e.printStackTrace();
        }
        
        LoginGUI guiD = new LoginGUI();
        guiD.setVisible(true);
    
    }
    
    public void calculateDifference(LocalTime sampleTime, LocalDate sampleDate, boolean over23, boolean over24, int sampNum){

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
    
    public String timeToString(int pTime){
        //settings test times to LocalTime var
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }
    
}