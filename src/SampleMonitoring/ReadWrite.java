package SampleMonitoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadWrite {
    
    public static void readFile(){

        try{
            System.out.println("Started reading file.");
            FileInputStream fis = new FileInputStream("csvFilePath.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            try{
                CSVFilePath.path = (String)ois.readObject();
            }catch(ClassNotFoundException ex){
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("Finished reading file.");
            ois.close();
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void writeFile(){

        try{
            File file = new File("csvFilePath.ser");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(CSVFilePath.path);
            oos.close();

            System.out.println("Finished writing file.");
        }
        catch(IOException e){
         System.out.println(e.getMessage());
        }
    }
   
}
