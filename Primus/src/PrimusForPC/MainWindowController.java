package PrimusForPC;

import PrimusForPC.ExcelWriter.spreadsheetTypes;
import PrimusForPC.calculationHolder.calculationType;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URI;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.util.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.controlsfx.control.GridView;


/**
 *
 * @author ADDO_a
 */
public class MainWindowController implements Initializable {
    
    public static longCalculationThread calculationThread;
    calculationHolder.calculationType currentType = calculationType.XthPrime;
    

    //<editor-fold defaultstate="collapsed" desc="JavaFX variables">
    @FXML
    private BorderPane parentBorderPane;

    @FXML
    private Text titleText;

    @FXML
    private HBox rangeInputHBox;

    @FXML
    private JFXTextField lowerRangeInput;

    @FXML
    private JFXTextField upperRangeInput;

    @FXML
    private JFXTextField singularNumberInput;

    @FXML
    private HBox buttonHBox;
    
    @FXML
    private JFXButton calculateButton;
    
    @FXML
    private JFXButton cancelButton;

    @FXML
    private VBox basicTextPane;

    @FXML
    private Text largeAnswerText;

    @FXML
    private Text smallAnswerText;

    @FXML
    private Pane excelPane;

    @FXML
    private JFXCheckBox spreadsheetOption;

    @FXML
    private VBox MaxColomn;

    @FXML
    private JFXRadioButton numberOfColumnsRadio;

    @FXML
    private ToggleGroup excelRowORColumn;

    @FXML
    private JFXTextField columnNumber;

    @FXML
    private VBox MaxRow;

    @FXML
    private JFXRadioButton numberOfROwsRadio;

    @FXML
    private JFXTextField rowNumber;

    @FXML
    private VBox outputRightVbox;

    @FXML
    private JFXRadioButton leftToRIghtExcel;

    @FXML
    private ToggleGroup excelExportType;

    @FXML
    private VBox outputTopVBox;

    @FXML
    private JFXRadioButton outputTopToBottom;

    @FXML
    private Pane progressPane;

    @FXML
    private JFXProgressBar progressBar;

    @FXML
    private Text progressText;

    @FXML
    private GridView<String> answerGridView;

    @FXML
    private Menu calculationTypeMenuItem;


//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Menu Item Methods">
    @FXML
    void setCalcDeterminePrime(ActionEvent event) {
        chooseCalculationType(calculationType.determineIfPrime);
    }
    
    @FXML
    void setCalcFactors(ActionEvent event) {
        chooseCalculationType(calculationType.factors);
    }

    @FXML
    void setCalcPhiForX(ActionEvent event) {
        chooseCalculationType(calculationType.findPhiRatioWithX);
    }

    @FXML
    void setCalcPhiToX(ActionEvent event) {
        chooseCalculationType(calculationType.phiRatioToX);
    }

    @FXML
    void setCalcPiRatioForX(ActionEvent event) {
        chooseCalculationType(calculationType.findPiRatioWithX);
    }

    @FXML
    void setCalcPiRatiosToX(ActionEvent event) {
        chooseCalculationType(calculationType.piRatiosToX);
    }

    @FXML
    void setCalcPrimeFactors(ActionEvent event) {
        chooseCalculationType(calculationType.primeFactors);
    }

    @FXML
    void setCalcPrimesInRange(ActionEvent event) {
        chooseCalculationType(calculationType.primesWithinRange);
    }

    @FXML
    void setCalcXthPhi(ActionEvent event) {
        chooseCalculationType(calculationType.XthPhiRatio);
    }

    @FXML
    void setCalcXthPiRatio(ActionEvent event) {
        chooseCalculationType(calculationType.XthPiRatio);
    }

    @FXML
    void setCalcXthPrime(ActionEvent event) {
        chooseCalculationType(calculationType.XthPrime);
    }
    
    @FXML
    void goToWebsite(ActionEvent event) throws IOException {
        URI myUri = URI.create("https://www.loadingdeveloper.com/");
        java.awt.Desktop.getDesktop().browse(myUri);
    }
    
    @FXML
    void switchToPhiInterface(ActionEvent event) {
        PrimusForPC.Instance.switchScenes("VisualPhiGenerator.fxml");
    }
    
     
    @FXML
    void cancelThread(ActionEvent event) {
        if (calculationThread == null) return;
        calculationThread.cancel();
    }

            
//</editor-fold>
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PrimusForPC.applicationStage.setMinWidth(734);
        PrimusForPC.applicationStage.setMinHeight(614);
        
        makeNumericOnly(lowerRangeInput);
        makeNumericOnly(upperRangeInput);
        makeNumericOnly(singularNumberInput);
        makeNumericOnly(rowNumber);
        makeNumericOnly(columnNumber);
        
        //This makes the Vbox that contains most of the control options dynamic
        //This means it won't allow invisible objets to take up UI whitespace
        excelPane.managedProperty().bind(excelPane.visibleProperty());
        rangeInputHBox.managedProperty().bind(rangeInputHBox.visibleProperty());
        singularNumberInput.managedProperty().bind(singularNumberInput.visibleProperty());
        progressPane.managedProperty().bind(progressPane.visibleProperty());
        basicTextPane.managedProperty().bind(basicTextPane.visibleProperty());
        
        //Doing the same for the cancel button now
        cancelButton.managedProperty().bind(cancelButton.visibleProperty());
    } 
    
    //Called by the "export to spreadsheet checkbox"
    @FXML
    void enableExcel(ActionEvent event) {
        MaxRow.setDisable(!spreadsheetOption.isSelected());
        MaxColomn.setDisable(!spreadsheetOption.isSelected());
        outputRightVbox.setDisable(!spreadsheetOption.isSelected());
        outputTopVBox.setDisable(!spreadsheetOption.isSelected());
    }


    //Called by the calculate button
    @FXML
    void callCalculation(ActionEvent event) throws IOException, InterruptedException {
        
        Long number = 0L;
        Long lowerBound = 0L;
        Long higherBound = 0L;

        try {
            //Populating the three variables based off the TextFields
            String intermediate = singularNumberInput.getText();            
            if (intermediate.equals("")) {
                intermediate = "0";
            }            
            number = Long.parseLong(intermediate);
            
            intermediate = lowerRangeInput.getText();            
            if (intermediate.equals("")) {
                intermediate = "0";
            }            
            lowerBound = Long.parseLong(intermediate);
            
            intermediate = upperRangeInput.getText();            
            if (intermediate.equals("")) {
                intermediate = "0";
            }            
            higherBound = Long.parseLong(intermediate);
        } catch (Exception exception) {
            warnUserInvalidInput("Please enter a number larger than 0 and less than 500,000,000");
            return;
        }
        
        //Checking if the current calculation even needs a longCalculationThread...
        if (currentType == calculationType.determineIfPrime || currentType == calculationType.findPiRatioWithX||
            currentType == calculationType.findPhiRatioWithX){
            performMinorCalculation(number);
        }else{           
            
            //Input error checking...
            if (currentType == calculationType.primesWithinRange &&
                    (higherBound < 2 || lowerBound < 1 || lowerBound >= higherBound || lowerBound > 500000000  || higherBound > 500000000)){
                warnUserInvalidInput("Please let your higher number be higher than your lower number! Both should be more than 0 and less than 500,000,000.");
                return;
            }else if (currentType != calculationType.primesWithinRange && (number == 0 || number > 500000000)){
                warnUserInvalidInput("Please enter a number larger than 0 and less than 500,000,000");
                return;
            }
            
            //Does the user want an Excel export? what Type does he want?
            boolean shouldExport = spreadsheetOption.isSelected();
            int limit = 0;
            ExcelWriter.spreadsheetTypes exportType = spreadsheetTypes.rightToLeftColumnLimit;
            
            if (shouldExport){
                if (numberOfColumnsRadio.isSelected()){
                    String userColumnLimit = columnNumber.getText();
                    if (userColumnLimit.equals("")) userColumnLimit = "0";
                    int i = Integer.parseInt(userColumnLimit);
                    if (i == 0 || i > 1000000) {
                        warnUserInvalidInput("Please choose a column number between 1 and 1000000");
                        return;
                    }
                    limit = i;
                }else{
                    String userRowLimit = rowNumber.getText();
                    if (userRowLimit.equals("")) userRowLimit = "0";
                    int i = Integer.parseInt(userRowLimit);
                    if (i == 0 || i > 1000000) {
                        warnUserInvalidInput("Please choose a row number between 1 and 1000000");
                        return;
                    }
                    limit = i;
                }
                
                if (numberOfColumnsRadio.isSelected())
                    exportType = (leftToRIghtExcel.isSelected()) ? spreadsheetTypes.rightToLeftColumnLimit : spreadsheetTypes.topToBottomColumnLimit;
                else
                    exportType = (leftToRIghtExcel.isSelected()) ? spreadsheetTypes.topToBottomRowLimit : spreadsheetTypes.topToBottomRowLimit;
            }
            
            //Enabling progress UI since no errors were encoundered...
            excelPane.setVisible(false); 
            progressPane.setDisable(false);
            progressPane.setVisible(true);

            
            //Preparing and starting thread.
            threadArgumentBundle threadBundle = new threadArgumentBundle();
            threadBundle.primaryNumber = number;
            threadBundle.answerGridView = answerGridView;
            threadBundle.shouldExport = shouldExport;
            threadBundle.type = currentType;
            threadBundle.exportType = exportType;
            threadBundle.progressText = progressText; 
            threadBundle.progressBar = progressBar;
            threadBundle.mainPane = excelPane;
            threadBundle.progressPane = progressPane;
            threadBundle.excelExportLimit = limit;
            threadBundle.lowerNum = lowerBound;
            threadBundle.upperNum = higherBound;
            threadBundle.button = calculateButton;
            threadBundle.selectionMenu = calculationTypeMenuItem;
            threadBundle.cancelButton = cancelButton;

            calculateButton.setDisable(true); //Will be re-enabled when thread finishes execution
            calculationThread = new longCalculationThread(threadBundle);
            calculationThread.start();
            System.gc(); //For the objects of the previous calculationThread
        }
    }   
    
    
    //This method contains the logic for calculations that don't requre a longCalculationThread
    void performMinorCalculation(Long number){
        
        if (number == 0){
            warnUserInvalidInput("Please enter a number larger than 0!");
            return;
            
        }
        if (currentType == calculationType.determineIfPrime){
            
            boolean isPrime = longCalculationThread.isPrime(number);
            largeAnswerText.setText(Long.toString(number));
            smallAnswerText.setText((isPrime)? "This is a prime number!" : "This isn't a prime number.");
            
        }else if (currentType == calculationType.findPhiRatioWithX){
            
            double phi = calculationHolder.PHI;
            String fractionXDenominator = longCalculationThread.getPhiRatio(number);
            String fractionXNumerator = longCalculationThread.getPhiRatio(Math.round(number / phi));
            largeAnswerText.setText(Long.toString(number));
            
            String answer = "";
            answer += (!fractionXDenominator.equals("")) ? ("Fraction with number as denominator: " + fractionXDenominator) 
                    : "There is no accurate fraction with this number as its denominator";
            answer += "\n\n";
            answer += (!fractionXNumerator.equals("")) ? ("Closest fraction with number as numerator: " + fractionXNumerator) 
                    : "There is no accurate fraction with this number as its numerator";
            smallAnswerText.setText(answer);
            
        }else{
                        
            String fractionXDenominator = longCalculationThread.getPiRatio(number);
            String fractionXNumerator = longCalculationThread.getPiRatio(Math.round(number / Math.PI));
            largeAnswerText.setText(Long.toString(number));
            
            String answer = "";
            answer += (!fractionXDenominator.equals("")) ? ("Fraction with number as denominator: " + fractionXDenominator) 
                    : "There is no accurate fraction with this number as its denominator";
            answer += "\n\n";
            answer += (!fractionXNumerator.equals("")) ? ("Closest fraction with number as numerator: " + fractionXNumerator) 
                    : "There is no accurate fraction with this number as its numerator";
            smallAnswerText.setText(answer);
        }
    }
    
    
    
    //Makes JFXTextFields only accept numeric input 
    void makeNumericOnly(JFXTextField textInput){
        textInput.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, 
        String newValue) {
            if (!newValue.matches("\\d*")) {
                textInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
        }
        });
    }
    
    //Called whenever a new calculation mode is chosen from the menu bar
    //Sets the UI accordingly
    void chooseCalculationType(calculationHolder.calculationType type){
        currentType = type;
        titleText.setText(setTitle(type));

        
        if (type == calculationType.determineIfPrime || type == calculationType.findPiRatioWithX||
            type == calculationType.findPhiRatioWithX){
            
            excelPane.setVisible(false);
            basicTextPane.setVisible(true);
            singularNumberInput.setVisible(true);
            rangeInputHBox.setVisible(false);
            largeAnswerText.setText("Your answer"); //Resetting Text UI to original strings
            smallAnswerText.setText("will appear here!");
            cancelButton.setVisible(false);
            
        }else{
            cancelButton.setVisible(true);
            excelPane.setVisible(true);
            basicTextPane.setVisible(false);
            if (type == calculationType.primesWithinRange){
                singularNumberInput.setVisible(false);
                rangeInputHBox.setVisible(true);
            }else{
                singularNumberInput.setVisible(true);
                rangeInputHBox.setVisible(false);
            }
            
            //Try to GC any leftover stuff that may have been dropped by a previous calculation thread
            System.gc();
        }
    
    }
       
    //Sets the title text FMXL object to reflect the calulation type Primus is in
    static String setTitle(calculationType type){
        String title = "";
        switch (type){
            case factors:
                title = "Find the factors of X";
                break;
            case primeFactors:
                title = "Find the prime factors of X";
                break;
            case primesUpToX:
                title = "Find the prime numbers up to X";
                break;
            case XthPrime:
                title = "Find up to the Xth prime number";
                break;
            case primesWithinRange:
                title = "Find the prime numbers within a range";
                break;
            case piRatiosToX:
                title = "Find all the pi fractions with denominators up to X";
                break;
            case XthPiRatio:
                title = "Find up to the Xth pi fraction";
                break;
            case phiRatioToX:
                title = "Find all the golden ratio fractions with denominators up to X";
                break;
            case XthPhiRatio:
                title = "Find up to the Xth golden ratio number";
                break;  
            case determineIfPrime:
                title = "Determine if X is prime";
                break;
            case findPhiRatioWithX:
                title = "Find any golden ratio fraction that includes X";
                break;
            case findPiRatioWithX:
                title = "Find any pi fraction that includes X";
                break;
        }
        return title;
    }
    
    void warnUserInvalidInput(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        
        alert.setContentText(message);
        
        alert.showAndWait();
    }
}


