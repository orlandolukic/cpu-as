package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class HelpScreenController implements Initializable {

    private static Image iBasic;
    private static Image iInstructions;
    private static Image iAddressing;

    @FXML
    public ImageView ImageBasic;

    @FXML
    public ImageView ImageInstructions;

    @FXML
    public ImageView ImageAddressing;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if ( iBasic == null ) {
            iBasic = new Image( "file:haw-basic.png" );
        }

        if ( iInstructions == null ) {
            iBasic = new Image( "file:haw-instructions.png" );
        }

        if ( iAddressing == null ) {
            iBasic = new Image( "file:haw-addressing.png" );
        }

        //ImageBasic.setImage(iBasic);
        //ImageInstructions.setImage(iInstructions);
        //ImageAddressing.setImage(iAddressing);
    }
}
