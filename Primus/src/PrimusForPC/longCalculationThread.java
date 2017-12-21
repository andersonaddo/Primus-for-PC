package PrimusForPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.controlsfx.control.GridView;

/**
 * This is the thread to be used for long calculations to prevent the UI thread from freezing.
 * It's a one-time-use thread, meaning an instance shouldn't be reused once it has completed one calculation.
 * @author ADDO_a
 */
public class longCalculationThread extends Thread {
    
    private ArrayList<Long> longResults = new ArrayList<>();
    private ArrayList<String> stringResults = new ArrayList<>();
    
    private ExcelWriter excelWriter = new ExcelWriter();
    
    //I used progressBuffer to create mechanism that prevents UI thread flooding. 
    //A  progres bar ipdate task can only be sent to the UI thread if progressBuffer == -1,
    //and that can only happen if the UI thread has finished processing the last task it received
    private AtomicDouble progressBuffer = new AtomicDouble(-1);
    private double progress;
    
    private calculationHolder.calculationType type;
    private JFXProgressBar progressBar;
    private Text progressText;
    private GridView<String> answerGridView;
    private Pane mainPane, progressPane;
    private long mainNumber, lowerNum, upperNum;
    private boolean shouldExport;
    private ExcelWriter.spreadsheetTypes exportType;
    private int excelExportLimit;
    private JFXButton calcButton, cancelButton;
    private Menu selectionMenu;
    
    private static double acceptableAccuracy = 0.004; //The degree of accuracty for pi and phi couples
    
    boolean shouldCancel; //Modified from the outside to cancel the thread. Periodically checked
    
        
    longCalculationThread(threadArgumentBundle bundle){
        progressBar = bundle.progressBar;
        answerGridView = bundle.answerGridView;
        type = bundle.type;
        mainNumber = bundle.primaryNumber;
        upperNum = bundle.upperNum;
        lowerNum = bundle.lowerNum;
        progressText = bundle.progressText;
        mainPane = bundle.mainPane;
        progressPane = bundle.progressPane;
        exportType = bundle.exportType;
        shouldExport = bundle.shouldExport;
        excelExportLimit = bundle.excelExportLimit;   
        calcButton = bundle.button;
        selectionMenu = bundle.selectionMenu;  
        cancelButton = bundle.cancelButton;
        
        excelWriter.cancelButton = cancelButton;
    }
    
    
    @Override
    public void run(){
        System.out.println("Thread started " + Thread.currentThread().getName());
        Platform.runLater(new Runnable() {
        @Override
        public void run() {
            progressBar.isIndeterminate();
            progressBar.progressProperty().setValue(0);
            selectionMenu.setDisable(true);
          }
        });

        switch (type){
            case factors:
                findfactors();
                break;
            case primeFactors:
                findPrimeFactors();
                break;
            case primesUpToX:
                findPrimesUpToX();
                break;
            case XthPrime:
                findUpToXthPrime();
                break;
            case primesWithinRange:
                primesWithinARange();
                break;
            case piRatiosToX:
                piRatiosToX();
                break;
            case XthPiRatio:
                XthPiRatio();
                break;
            case phiRatioToX:
                phiRatiosToX();
                break;
            case XthPhiRatio:
                XthPhiRatio();
                break;           
        }
        try {
            if (!shouldCancel) publishResults();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(longCalculationThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        reEndableInterface();
        System.gc();
        System.out.println("Thread ended " + Thread.currentThread().getName() + "\n");
    }


    //The following comments are for Netbeans code folding...
    // <editor-fold defaultstate="collapsed" desc="These are the different types of calculations">
   
    static private long getSquareRoot (long number){
        //For calculations square root only matters for larger numbers
        //for numbers below 5 it doesn't really matter
        if (number > 5){
            number =  (long) Math.ceil(Math.sqrt(number));
        }
    return number;
    }
    
    
    private void findfactors(){
        progressText.setText("Finding factors...");
        long root = getSquareRoot(mainNumber);       
        double incrementValue = 1/(double)root;
        
          for (long divisor = 1; divisor <= root; divisor++) { 
               progress += incrementValue;  
               callProgressBarUpdate(progress);

               if (mainNumber % divisor == 0) {
                   longResults.add(divisor);
                   longResults.add((mainNumber/divisor));
               }
               if(shouldCancel) return;
           }
    }
    
    
    private void findPrimeFactors(){
        findfactors();
        progressText.setText("Separating prime factors from composite factors...");
        callProgressBarUpdate(0);
        
        double incrementValue = 1/(double)longResults.size();
        progress = 0;
         
        ArrayList <Long> primeResults = new ArrayList<>();
        
        for (long factor: longResults){
           progress += incrementValue;
           if (isPrime(factor)) primeResults.add(factor);
           callProgressBarUpdate(progress);
           
           if(shouldCancel) return;
        }

        longResults = primeResults;
    }
    
    
    private void findPrimesUpToX(){     
        double incrementValue = 1/(double)mainNumber;       
        progressText.setText("Finding primes...");
        
        for (long i = 2; i <= mainNumber; i++){
           progress += incrementValue;
           if (isPrime(i)) longResults.add(i);
           callProgressBarUpdate(progress);
           
           if(shouldCancel) return;
        }  
    }
    
    
    private void findUpToXthPrime(){
        long everIncreasingLimit = 10;
        double incrementValue = 1/(double)mainNumber;
        
        progressText.setText("Finding primes...");
        for (long i = 2; i <= everIncreasingLimit; i++){
            everIncreasingLimit++;
            if (isPrime(i)){
                longResults.add(i);
                progress += incrementValue;
                callProgressBarUpdate(progress);
            }
           if (longResults.size() == mainNumber) break;
           if(shouldCancel) return;
        }
    }
    
    
    private void primesWithinARange(){
        double incrementValue = 1/(double)((upperNum - lowerNum) + 1); // The range is upper - lower, where lower is inclusive
        progressText.setText("Finding primes...");
        for (long i = lowerNum; i <= upperNum; i++){
           progress += incrementValue;
           if (isPrime(i)) longResults.add(i);
           callProgressBarUpdate(progress);
           
           if(shouldCancel) return;
        }  
    }
    
    
    private void phiRatiosToX(){
        double incrementValue = 1/(double)mainNumber;
        progressText.setText("Finding accurate ratios...");
        for (long i = 2; i <= mainNumber; i++){
           progress += incrementValue;
           String ratio = getPhiRatio(i);
           if (!ratio.equals("")) stringResults.add(ratio);
           callProgressBarUpdate(progress);
           
           if(shouldCancel) return;
        }  
    }
    
    
    private void XthPhiRatio(){
        long everIncreasingLimit = 10;
        double incrementValue = 1/(double)mainNumber;
        progressText.setText("Finding phi ratios...");
        
        for (long i = 2; i <= everIncreasingLimit; i++){
            everIncreasingLimit++;
            String ratio = getPhiRatio(i);
            if (!ratio.equals("")){
                stringResults.add(ratio);
                progress += incrementValue;
                callProgressBarUpdate(progress);
            }
           if (stringResults.size() == mainNumber) break;
           if(shouldCancel) return;
        }
    }
    
    
    private void piRatiosToX(){
        double incrementValue = 1/(double)mainNumber;
        progressText.setText("Finding accurate ratios...");
        for (long i = 2; i <= mainNumber; i++){
           String ratio = getPiRatio(i);
           if (!ratio.equals("")){
               stringResults.add(ratio);
               progress += incrementValue;
               callProgressBarUpdate(progress);
           }
            if(shouldCancel) return;
        }  
    }
    
    private void XthPiRatio(){
        long everIncreasingLimit = 10;       
        double incrementValue = 1/(double)mainNumber;       
        progressText.setText("Finding pi ratios...");
        
        for (long i = 2; i <= everIncreasingLimit; i++){
            everIncreasingLimit++;
            String ratio = getPiRatio(i);
            if (!ratio.equals("")){
                stringResults.add(ratio);
                progress += incrementValue;
                callProgressBarUpdate(progress);
            }
           if (stringResults.size() == mainNumber) break;
           if(shouldCancel) return;
        }
    }
 
    static boolean isPrime(long number){
        if (number == 1) return false;
        if (number == 2 || number == 3 || number == 5) return true;
        long root = getSquareRoot(number);
        for (int divisor = 2; divisor <= root; divisor++)
            if (number % divisor == 0) return false;
        return true;
    }
    
    
    //Returns "" if there's no acceptable integer phi ratio
    static String getPhiRatio(long number){
        double closestNumerator = Math.round(number * calculationHolder.PHI);
        double acceptableAnswer;
        double answerAccuracy = Math.abs(((double)closestNumerator/number) - calculationHolder.PHI);
        
        acceptableAnswer = (answerAccuracy > acceptableAccuracy) ? -1 : closestNumerator;
        if (acceptableAnswer == -1) return "";
        
        StringBuilder answer = new StringBuilder();
        answer.append(acceptableAnswer).append(" / ").append(number);
        return answer.toString();
    }
    
    
     static String getPiRatio(long number){
        double closestNumerator = Math.round(number * Math.PI);
        double acceptableAnswer;
        double answerAccuracy = Math.abs(((double)closestNumerator/number) - Math.PI);
        
        acceptableAnswer = (answerAccuracy > acceptableAccuracy) ? -1 : closestNumerator;
        if (acceptableAnswer == -1) return "";
        
        StringBuilder answer = new StringBuilder();
        answer.append(acceptableAnswer).append(" / ").append(number);
        return answer.toString();
    }
    
// </editor-fold>
    
   
    
    private void publishResults() throws IOException, InterruptedException{
        if (!longResults.isEmpty()){
            if (shouldExport) {
                updateProgressText("Exporting to Excel...");
                excelWriter.makeSpreadsheet(longResults, exportType, excelExportLimit, getTitle(), progressBar);
            }
            
            updateProgressText("Printing results...");
            
            ArrayList<String> intermediateArray = new ArrayList<>();
            for (Long x: longResults) intermediateArray.add(Long.toString(x));
            
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    answerGridView.getItems().setAll(intermediateArray);
                  }
                });
            
        }else{
            
            if (shouldExport) {
                updateProgressText("Exporting to Excel...");
                excelWriter.makeSpreadsheet(stringResults, exportType, excelExportLimit, getTitle(), progressBar);
            }
            
            updateProgressText("Printing results...");

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    answerGridView.getItems().setAll(stringResults);
                  }
                });          
        }      
    }
    
    //To make UI interface useable again
    private void reEndableInterface(){
         Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    cancelButton.setVisible(true);
                    progressPane.setDisable(true);
                    progressPane.setVisible(false);
                    mainPane.setDisable(false);
                    mainPane.setVisible(true);
                    calcButton.setDisable(false);
                    System.out.println("Button enabled " + Thread.currentThread().getName());
                    selectionMenu.setDisable(false);
                  }
                });
        
    }
     
    private String getTitle(){
        String title = MainWindowController.setTitle(type);
        title = title.replaceAll("X", Long.toString(mainNumber));
        title = title.replaceAll("range", "range of " + lowerNum + " to " + upperNum);
        title = title.replaceAll("Find", "Finding");
        
        //Adding the calculation date to the title
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();       
        title += "\n  " + dateFormat.format(date);
        return title;
    }
    
    
    //Since this is called repeatedly as small intervals, this has throttling logic
    //So that the UI thread is only called if it has processed its previous update call
    private void callProgressBarUpdate(double value){
        //-1 indicates that the UI thread has finished its last progressbar update task
        if (progressBuffer.getAndSet(value) == -1) 
           updateProgressBar();
    }
    
    private void updateProgressBar(){
         Platform.runLater(new Runnable() {
        @Override
        public void run() {
            progressBar.setProgress(progressBuffer.getAndSet(-1));
          }
        });
    }
    
    private void updateProgressText(String message){
        Platform.runLater(new Runnable() {
        @Override
        public void run() {
            progressText.setText(message);
          }
        });
    }
    
    
    public void cancel(){
        shouldCancel = true;
        excelWriter.shouldCancel = true;
    }    
}
