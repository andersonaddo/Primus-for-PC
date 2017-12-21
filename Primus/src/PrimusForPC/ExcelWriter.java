
package PrimusForPC;

/**
 *This is a utility class that exports Primus's output to Excel (.xlsx) files.
 * @author ADDO_a
 */

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {
    
    //Documentation for use is in longCalculationThread, since it's also used there
    private AtomicDouble progressBuffer = new AtomicDouble(-1); 
    
    boolean shouldCancel; //Periodically checked
    JFXButton cancelButton; 
    
    public enum spreadsheetTypes{
        rightToLeftColumnLimit,
        rightToLeftRowLimit,
        topToBottomColumnLimit,
        topToBottomRowLimit,
    }
    
    //Creates a configured JFileChooser to make alert before overwriting old files
    private static JFileChooser fileManager = new JFileChooser(){
    @Override
    public void approveSelection(){
        File f = getSelectedFile();
        if(f.exists() && getDialogType() == SAVE_DIALOG){
            int result = JOptionPane.showConfirmDialog(null, 
                        FilenameUtils.getBaseName(f.getName()) + ".xlsx" + " already exists in this location, do you want to overwrite?",
                        "Existing file",
                        JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.CANCEL_OPTION:
                    cancelSelection();
                    return;
            }
        }
        super.approveSelection();
    }        
    };
    
    
    private static FileFilter filter = new FileNameExtensionFilter("Excel files","xlsx");
    private static boolean hasBeenInitialized = false;
    
    
    //Called only once
    private static void initializeChooser(){
        if (!hasBeenInitialized) hasBeenInitialized = true;
        else return;
        //Specializing the fileManager so it only shows directries .xlsx files 
        fileManager.setAcceptAllFileFilterUsed(false);
        fileManager.setFileFilter(filter);
    }
    
    
    /**
    * This is the only method that can be called externally to access this class's functionality
    * Note that this method can be extremely memory expensive when dealing with very large 
    * lists (100k items +). It can take GBs of RAM in some cases (eg: 1m items in a list).
    */
    public <T extends Object> void makeSpreadsheet
        (ArrayList<T> list,  spreadsheetTypes type, int max, String title, JFXProgressBar progressBar) 
                throws IOException, InterruptedException{
         
        progressBuffer.set(-1); //Resets the value, making it ready fo use again
        callProgressBarUpdate(0, progressBar);
        
        switch (type){
            case rightToLeftColumnLimit:
                makeSpreadsheetRightToLeft(list, false, max, title, progressBar);
                break;
            case rightToLeftRowLimit:
                makeSpreadsheetRightToLeft(list, true, max, title, progressBar);
                break;
            case topToBottomColumnLimit:
                makeExcelSpreadsheetToptoBottom(list, false, max, title, progressBar);
                break;
            case topToBottomRowLimit:
                makeExcelSpreadsheetToptoBottom(list, true, max, title, progressBar);
                break; 
        }
    }
    
        
    private <T extends Object> void makeSpreadsheetRightToLeft
        (ArrayList<T> list,  boolean maxRows, int max, String title, JFXProgressBar progressBar) 
                throws IOException, InterruptedException{
        initializeChooser();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Primus output"); 
        int rowPointer = 0;
        int columnPointer = 0;
        
        //For the ProgressBar
        double progress = 0;
        double progressIncrementValue = 1/(double)list.size();
        
        //Giving the spreadsheet an internal title also
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(title);
        
        row = sheet.createRow(++rowPointer);
        
        //Making the sheet with a max column limit
        if (!maxRows){            
            for (T number: list){ 
                if (columnPointer == max){
                    columnPointer = 0;
                    row = sheet.createRow(++rowPointer);
                }
                Cell cell = row.createCell(columnPointer++);
                cell.setCellValue(number.toString());
                
                progress += progressIncrementValue;
                callProgressBarUpdate(progress, progressBar);
                
                if(shouldCancel) return;
            }
            
        }else {
            //Making the sheet with a max row limit
            int columnWrapIndex = (int)Math.ceil(list.size()/(float)max);
            for (T number: list){ 
                if (columnPointer == columnWrapIndex){
                    columnPointer = 0;
                    row = sheet.createRow(++rowPointer);
                }
                Cell cell = row.createCell(columnPointer++);
                cell.setCellValue(number.toString());
                
                progress += progressIncrementValue;
                callProgressBarUpdate(progress, progressBar);
                
                if(shouldCancel) return;
            }         
        }
        writeToExcel(workbook, progressBar);
        
        
    }
        
        
    private <T extends Object> void makeExcelSpreadsheetToptoBottom
        (ArrayList<T> list,  boolean maxRows, int max, String title, JFXProgressBar progressBar) 
                throws IOException, InterruptedException{
        initializeChooser();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Primus output"); 
        int rowPointer = 0;
        int columnPointer = 0;
        boolean firstColumnFinished = false;
        
        //For the ProgressBar
        double progress = 0;
        double progressIncrementValue = 1/(double)list.size();
        
        //Giving the spreadsheet an internal title also
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(title);
        
        row = sheet.createRow(++rowPointer);
        
        //Making the sheet with a max column limit
        if (!maxRows){
            int rowWrapIndex = (int)Math.ceil(list.size()/(float)max); //The row that will wrap writing back to the top of the sheet
            for (T number: list){ 
                if (rowPointer == rowWrapIndex + 1){  //+1 Since counting starts from 1 and not 0, and the increment happens during use and not after
                    columnPointer++;
                    row = sheet.getRow(1); //Starts at one because of the sheet title has the first row reserved
                    rowPointer = 1;
                    firstColumnFinished = true;
                }
                Cell cell = row.createCell(columnPointer);
                cell.setCellValue(number.toString()); 
                progress += progressIncrementValue;
                callProgressBarUpdate(progress, progressBar);
                
                //Don't want to remake rows that have already been made previously
                if (!firstColumnFinished) row = sheet.createRow(++rowPointer);
                else row = sheet.getRow(++rowPointer);
                
                if(shouldCancel) return;
            }
            
        }else {
            //Making the sheet with a max row limit
            for (T number: list){ 
                if (rowPointer == max + 1){  //+1 Since counting starts from 1 and not 0, and the increment happens during use and not after
                    columnPointer++;
                    row = sheet.getRow(1); //Starts at one because of the sheet title has the first row reserved
                    rowPointer = 1;
                    firstColumnFinished = true;
                }
                Cell cell = row.createCell(columnPointer);
                cell.setCellValue(number.toString()); 
                progress += progressIncrementValue;
                callProgressBarUpdate(progress, progressBar);
                
                //Don't want to remake rows that have already been made previously
                if (!firstColumnFinished) row = sheet.createRow(++rowPointer);
                else row = sheet.getRow(++rowPointer);
                
                if(shouldCancel) return;
            }
        }
        writeToExcel(workbook, progressBar);
    }
    
    
    private void writeToExcel(XSSFWorkbook book, JFXProgressBar progressBar) throws IOException, InterruptedException{
        try {
            //<editor-fold defaultstate="collapsed" desc="Main Exporting logic">
        //Bringing up file explorer dialogue box
        int returnValue = fileManager.showSaveDialog(null);

        //If the user has chosen a file...
        if (returnValue == JFileChooser.APPROVE_OPTION){
            File file = fileManager.getSelectedFile();
    
        //Remove the extension (if any) and replace it with ".xlsx" if it doesn't have it already
        if (!FilenameUtils.getExtension(file.getName()) . equalsIgnoreCase("xlsx"))
            file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + ".xlsx");

        if (file.exists()){
            JOptionPane pane = new JOptionPane();
        
        //Notify user before overwriting an already existing file
        int result = JOptionPane.showConfirmDialog(null,
                FilenameUtils.getBaseName(file.getName()) + ".xlsx" + " already exists in this location, do you want to overwrite?",
                "Existing file",
                JOptionPane.YES_NO_CANCEL_OPTION);
        
        switch(result){
            case JOptionPane.YES_OPTION:
                Files.delete(file.toPath());
                while (file.exists()){
                    Thread.sleep(500);
                }
                break;
            case JOptionPane.NO_OPTION:
                return;
            case JOptionPane.CLOSED_OPTION:
                return;
            case JOptionPane.CANCEL_OPTION:
                return;
        }
    }
    
    Platform.runLater(new Runnable() {
        @Override
        public void run() {
            progressBar.setProgress(JFXProgressBar.INDETERMINATE_PROGRESS);
            cancelButton.setVisible(false); //Too dangerous to try and cancel when the outputstream is actually functioning.
        }
    });
    
    FileOutputStream out = new FileOutputStream(file);
    book.write(out);
    out.close();
    book.close();
    
}
//</editor-fold>
        } catch (Exception e) {
            notifyUserOfError(e);
        }

        
    }
    
    
 
    //Since this is called repeatedly at small intervals, this has throttling logic
    //So that the UI thread is only called if it has processed its previous update call
    private void callProgressBarUpdate(double value, JFXProgressBar bar){
        //-1 indicates that the UI thread has finished its last progressbar update task
        if (progressBuffer.getAndSet(value) == -1) 
           updateProgressBar(bar);
    }
    
    private void updateProgressBar(JFXProgressBar bar){
         Platform.runLater(new Runnable() {
        @Override
        public void run() {
            bar.setProgress(progressBuffer.getAndSet(-1));
          }
        });
    }
    
    private void notifyUserOfError(Exception ex){
        Platform.runLater(new Runnable() {
        @Override
        public void run() {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("File Error!");
            alert.setHeaderText(null);

            if (ex instanceof FileNotFoundException)
                alert.setContentText("The excel file that was being used has gone missing!");
            else if (ex instanceof AccessDeniedException)
                alert.setContentText("I've been denied access to this file! Try checking your file permissions.");
            else if (ex instanceof FileSystemException)
                alert.setContentText("I faced a bit of a problem, and the Excel export failed."
                        + " You've most likely opened the file I'm trying to use, or another program was using it.");
            else if (ex instanceof IOException)
                alert.setContentText("There was a problem with the outputting! Maybe you don't have enough space?");
            else
            alert.setContentText("Oops! I faced a bit of an error. Maybe you've opened the file I'm trying to use?"
                    + "Or perhaps the file is missing or being used by another program.");

            alert.showAndWait();
          }
        });                  
    }
}
