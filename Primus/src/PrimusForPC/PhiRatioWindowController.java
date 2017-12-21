
package PrimusForPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 *This is the controller class for the golden ratio window that has the visual aids (VisualPhiGenerator.fxml)
 * @author ADDO_a
 */
public class PhiRatioWindowController implements Initializable{

    int decimalPrecision; //The number of decimal digits an answer will be rounded to
    
    BigDecimal phi = new BigDecimal("1.6180339887498948482045868343656381177203091798057650" +
                                    "2862135448622705260462818902449707207204893911374100" +
                                    "84754088075386891752126633862223536931793180060766" +
                                    "7263543338908659593958290563832266131992829026788");
    
    MathContext context = new MathContext(200, RoundingMode.HALF_UP);

    
    //<editor-fold defaultstate="collapsed" desc="FXML varibales">
    @FXML
    private BorderPane parentBorderPane;

    @FXML
    private JFXTextField ABInput;

    @FXML
    private JFXTextField AInputLeft;

    @FXML
    private JFXTextField AInputRight;

    @FXML
    private JFXTextField BInput;

    @FXML
    private ComboBox<String> decimalDropdown;

    @FXML
    private JFXButton clearButton;

    @FXML
    private JFXButton calculateButton;

    @FXML
    private Text answerText;

    @FXML
    private TextArea mainRatioAB;

    @FXML
    private TextArea mainRatioA;

    @FXML
    private TextArea mainRatioB;

    @FXML
    private Text largerRatioTitle;

    @FXML
    private TextArea largeRatioAB;

    @FXML
    private TextArea largeRatioA;

    @FXML
    private TextArea largeRatioB;

    @FXML
    private Text smallerRatioTitle;

    @FXML
    private TextArea smallRatioAB;

    @FXML
    private TextArea smallRatioA;

    @FXML
    private TextArea smallRatioB;

//</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc="Minor FXML methods">
    @FXML
            void clearInputs(ActionEvent event) {
                ABInput.setText("");
                BInput.setText("");
                AInputLeft.setText("");
                AInputRight.setText("");
            }
            
            @FXML
            void goToWebsite(ActionEvent event) throws IOException {
                URI myUri = URI.create("https://www.loadingdeveloper.com/");
                java.awt.Desktop.getDesktop().browse(myUri);
            }
            
            @FXML
            void setDecimalPrecision(ActionEvent event) {
                decimalPrecision = Integer.parseInt(decimalDropdown.getValue());
            }
            
            
            @FXML
            void goToMainInterface(ActionEvent event) {
                PrimusForPC.Instance.switchScenes("MainWindow.fxml");
            }
//</editor-fold>

    @FXML
    void calculateRatios(ActionEvent event) {
        try {
            //The numbers for calculating the phi ratio (a > b, a+b:a = a:b)
            BigDecimal a = new BigDecimal(BigInteger.ZERO);         
            BigDecimal b = new BigDecimal(BigInteger.ZERO);            
            BigDecimal aPlusB = new BigDecimal(BigInteger.ZERO);
            
            //Verification
            int numberOfInputs = getNumberOfInputs();
            if (numberOfInputs == 0){
                warnUserInvalidInput("Please enter a number in one of the input fields.");
                return;
            }else if (numberOfInputs > 1){
                warnUserInvalidInput("Please enter a number in only one of the input fields.");
                return;
            }
            
            //Simple calculations to get the ratios
            JFXTextField field = fetchInputField();
            if (field == BInput) {
                b = new BigDecimal(field.getText());
                a = b.multiply(phi, context);
                aPlusB = a.add(b, context);
            }else if (field == AInputRight || field == AInputLeft){
                a = new BigDecimal(field.getText());
                b = a.divide(phi, context);
                aPlusB = a.add(b,context);
            }else if (field == ABInput){
                aPlusB = new BigDecimal(field.getText());
                a = aPlusB.divide(phi, context);
                b = a.divide(phi, context);
            }
            
            //This is for the decimal presision. I'm using regex formatting
            String append = new String(new char[decimalPrecision]).replace("\0", "#");
            DecimalFormat formatter = new DecimalFormat("#." + append);
            formatter.setRoundingMode(RoundingMode.HALF_UP);
            
            
            //If any of the numbers are really large, notfy the user that the UI could get ugly
            BigDecimal limit = new BigDecimal("1000000000000");
            boolean aIsLarge = a.compareTo(limit) >= 0;
            boolean bIsLarge = b.compareTo(limit) >= 0;
            boolean aPlusBIsLarge = aPlusB.compareTo(limit) >= 0;
            if (aIsLarge || bIsLarge || aPlusBIsLarge)
                informUser("The results of this calculation will be quite large!"
                        + " Because of this, the UI will bet a bit messy");
            
            
            //Outputting main answer, using the formatter made
            mainRatioA.setText(formatter.format(a));
            mainRatioAB.setText(formatter.format(aPlusB));
            mainRatioB.setText(formatter.format(b));
            answerText.setText(String.format("%s    :    %s    =    %s    :    %s",
                    formatter.format(aPlusB), formatter.format(a), formatter.format(a), formatter.format(b)));
            
            
            //Outputting the results for the larger or smaller ratio section
            if (field == BInput) {
                smallRatioA.setText(formatter.format(b));
                smallRatioB.setText(formatter.format(b.divide(phi, context)));
                smallRatioAB.setText(formatter.format(b.multiply(phi, context)));
                smallerRatioTitle.setText("Smaller Ratio: substituting B into A");

                largeRatioA.setText(formatter.format(aPlusB));
                largeRatioAB.setText(formatter.format(aPlusB.multiply(phi, context)));
                largeRatioB.setText(formatter.format(a));
                largerRatioTitle.setText("Larger Ratio: substituting A+B into A");
                
            }else if (field == AInputRight || field == AInputLeft){
                smallRatioA.setText(formatter.format(b));
                smallRatioB.setText(formatter.format(a.divide(phi.pow(2, context), context)));
                smallRatioAB.setText(formatter.format(a));
                smallerRatioTitle.setText("Smaller Ratio: substituting B into A");

                largeRatioA.setText(formatter.format(aPlusB));
                largeRatioAB.setText(formatter.format(a.multiply(phi.pow(2, context), context)));
                largeRatioB.setText(formatter.format(a));
                largerRatioTitle.setText("Larger Ratio: substituting A+B into A");
                
            }else if (field == ABInput){
                smallRatioB.setText(formatter.format(a.divide(phi.pow(2, context), context)));
                smallRatioA.setText(formatter.format(a.divide(phi, context)));
                smallRatioAB.setText(formatter.format(a));
                smallerRatioTitle.setText("Smaller Ratio: substituting A into A+B");

                largeRatioA.setText(formatter.format(aPlusB));
                largeRatioAB.setText(formatter.format(aPlusB.multiply(phi, context)));
                largeRatioB.setText(formatter.format(a));
                largerRatioTitle.setText("Larger Ratio: substituting A into B");
          }
            
            
        } catch (Exception e) {
            warnUserInvalidInput("Please enter some valid input!");
        }       
    }

    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PrimusForPC.applicationStage.setMinWidth(617);
        PrimusForPC.applicationStage.setMinHeight(637);
        
        makeNumericOnly(ABInput);
        makeNumericOnly(AInputLeft);
        makeNumericOnly(AInputRight);
        makeNumericOnly(BInput);
        
        decimalDropdown.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        decimalDropdown.setValue("3");
        decimalPrecision = 3;
    }
    
    
    //Makes JFXTextFields only accept numeric input with a max of one decimal place
    void makeNumericOnly(JFXTextField textInput){
        textInput.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, 
        String newValue) {
            if (!newValue.matches("\\d* (\\.\\d*)? ")) {
                
                if (newValue.contains(".")){
                    //Reconstructing the string by splitting it in two, one part before and the other after the first decimal point
                    String beforeString = newValue.substring(0, newValue.indexOf(".")); 
                    String afterString = "";
                    
                    //This prevents OutOfBounds error if the string is just an "." or ends with an "."
                    if (newValue.indexOf(".") != newValue.length()-1){
                        afterString = newValue.substring(newValue.indexOf(".") + 1, newValue.length());
                    }

                    afterString = afterString.replaceAll("[^0-9]", "");
                    beforeString = beforeString.replaceAll("[^0-9]", ""); 
                    textInput.setText(beforeString + "." + afterString);
                }else{
                    textInput.setText(newValue.replaceAll("[^0-9]", ""));
                }
            }
        }
        });
    }
    
    //This returns how many textFields have input in them
    private int getNumberOfInputs(){
        int count = 0;
        JFXTextField[] listOfFields = new JFXTextField[]{ABInput, AInputLeft, AInputRight, BInput};
        for (JFXTextField field : listOfFields) 
            if (!field.getText().equals("")) count++;
        return count;
    }
    
    void warnUserInvalidInput(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);       
        alert.setContentText(message);      
        alert.showAndWait();
    }
    
    void informUser(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Something to note");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //Since there are lots of input fields, this simplifies code for getting the one the user
    //Has populated. Needs prior verification to be flawless, eg from getNumberOfInputs().
    private JFXTextField fetchInputField(){
        JFXTextField[] listOfFields = new JFXTextField[]{ABInput, AInputLeft, AInputRight, BInput};
        for (JFXTextField field : listOfFields) 
            if (!field.getText().equals("")) return field;
        return ABInput;
    }
}
