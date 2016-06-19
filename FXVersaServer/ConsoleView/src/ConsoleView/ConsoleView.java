/**
 * VersaCheckers
 * ConsoleView - ConsoleView
 * @author Mikhail Kotlik, Sam Xu
 * @version 1
 * Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 */

package ConsoleView;

import java.io.IOException;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class ConsoleView extends AnchorPane{
    //FXML variables
    @FXML private TextArea userOut; //Console output/history
    @FXML private TextField userIn; //User input line
    //Back-end variables
    //Maybe this should be just a ListProperty? Or an ObservableArrayList?
    private SimpleListProperty<String> inputLines; //List of latest commands, observed by a higher class

    //Constructor
    public ConsoleView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConsoleView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        inputLines = new SimpleListProperty<String>();
    }

    public StringProperty getInputProperty() {
        return userIn.textProperty();
    }

    public String getInputText() {
        return getInputProperty().get();
    }

    public String readInput() {
        String ret = getInputText();
        userIn.clear();
        return ret;
    }

    public void print(String text) {
        userOut.appendText(text);
    }

    public void println(String text) {
        userOut.appendText(text + "\n");
    }

    //Event methods
    @FXML
    protected void onEnter(KeyEvent event) { //Bind this to the textField
        if (event.getCode() == KeyCode.ENTER) {
            inputLines.add(readInput());
        }
    }

    //List methods
    public SimpleListProperty getInputList() {
        return inputLines;
    }
    //Maybe I should add direct methods for the list, but for now, assume calling class saves reference

}
