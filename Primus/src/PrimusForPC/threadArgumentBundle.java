package PrimusForPC;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.controlsfx.control.GridView;

/**
 * This thread just holds the arguments for the creation of a longCalculationThread
 * @author ADDO_a
 */
public class threadArgumentBundle {
    
    calculationHolder.calculationType type;
    JFXProgressBar progressBar;
    Text progressText;
    GridView<String> answerGridView;
    Pane mainPane, progressPane;
    long primaryNumber;
    long lowerNum, upperNum;
    boolean shouldExport;
    ExcelWriter.spreadsheetTypes exportType;
    int excelExportLimit;
    JFXButton button, cancelButton;
    Menu selectionMenu;

    public threadArgumentBundle() {
        type = calculationHolder.calculationType.XthPrime;
        exportType = ExcelWriter.spreadsheetTypes.rightToLeftColumnLimit;      
    }
    
    
}
