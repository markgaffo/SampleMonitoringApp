package SampleMonitoring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.UIManager;
import SampleMonitoringGUI.GUIDashboard;


/**
 *
 * @author Mark
 */
public class MainClass {
    public static void main(String[] args) throws ParseException {
        
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            e.printStackTrace();
        }
        
        LoginGUI guiD = new LoginGUI();
        guiD.setVisible(true);

        
        //can be removed
        String csvFile = CSVFilePath.path;
        String line ="";
        Date systemTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String test = sdf.format(systemTime);
        System.out.println(test);
        
        ArrayList<String> timeList = new ArrayList<String>();
        ArrayList<Date> timeHolder = new ArrayList<Date>();
        ArrayList<String> holder = new ArrayList<String>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
//           while((line = br.readLine()) != null){
//                String[] values = line.split(",");
//                timeList.add(values[4]);                  //broken.
//           }

            while((line = br.readLine()) != null){
                
                    String[] values = line.split(",");
                    for(int i = 0; i < values.length ;i++){    
                        try{
                            sdf.parse(values[i]);
                            timeList.add(values[i]);
                            break;
                            }catch(ParseException e){
//                                br.readLine();
//                                System.out.println(e.getMessage());
                        }
                    }
               
            }
           System.out.println(timeList);
        }catch(IOException e){
            e.printStackTrace();
        }

        for(int i = 0; i<timeList.size(); i++){ 
            if (timeList.get(i).length() > 4){
                continue;
            }
            holder.add(timeList.get(i));
           try{
                
            timeHolder.add(sdf.parse(holder.get(i)));
            if(timeHolder.get(i).getMinutes() < (systemTime.getMinutes()+30)){
                System.out.println("Sample time is greater than system");
            }else{
                System.out.println("Sample time is Less than system");
            }  
           }catch(ParseException e){
               System.out.print(e.getMessage());
           }
                               
           
        }
        //Can be removed
        //getting mins and hours of sample 1 then checking that vrs the current system time
        String sampleTime1 = timeList.get(0);
        String sampleTime2 = timeList.get(1);
        
        Date time1 = sdf.parse(sampleTime1);
        Date time2 = sdf.parse(sampleTime2);
        
        long timeDifMin = systemTime.getMinutes()-time1.getMinutes();
        long timeDifHour = systemTime.getHours()-time1.getHours();
        
        System.out.println("The time difference from sample 1 and now is "+timeDifHour+"hrs "+timeDifMin+"mins");
        
        if(timeDifHour < 1){
            if(timeDifMin < 31){
                System.out.println("Sample is within 30mins");
            }else{
                System.out.println("Over 30 Mins");
            }
        }else{
            System.out.println("Over hour delay");
        }
        //till here
    }
    
}