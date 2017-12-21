package PrimusForPC;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author ADDO_a
 */
public class PrimusForPC extends Application {
    
    static public Stage applicationStage;
    static public PrimusForPC Instance; //Useful singleton logic here for scene switching
    
    public static void main(String[] args) {
        launch(args);
    }
        
    
    @Override
    public void start(Stage stage) throws Exception {
        Instance = this;
        applicationStage = stage;
        
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        
        Scene scene = new Scene(root);        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("Primus logo.png")));
        stage.setTitle("Primus");
        stage.setScene(scene);
        stage.show();        
    }

    //This is why singleton logic was used
    public void switchScenes(String fxmlName){
        try {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlName));
        Scene scene = new Scene(root);
        applicationStage.setScene(scene);
        applicationStage.show();   
        } catch (Exception e) {
        }
    }

}
