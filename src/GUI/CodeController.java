package GUI;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CodeController extends TabPane implements Initializable {
    @FXML
    private TextField codeLine;
    @FXML
    private TableColumn pNum;
    @FXML
    private TableColumn pseudocode;
    @FXML
    private TableColumn adNum;
    @FXML
    private TableColumn address;
    @FXML
    private TableColumn hNum;
    @FXML
    private TableColumn hexa;
    @FXML
    private TableColumn decNum;
    @FXML
    private TableColumn decimal;
    @FXML
    private TableView pTable;
    @FXML
    private TableView adTable;
    @FXML
    private TableView hTable;
    @FXML
    private TableView decTable;

    private String code;
    private ColorParser cp;
    private int line = 0;
    private ObservableList<ArrayList<Text>> codes;

    public CodeController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Code.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        codeLine.setOnAction(e -> {
            code = codeLine.getText();
            cp = new ColorParser(code);
            ArrayList<Text> tempAL;
            tempAL = new ArrayList<>(cp.getColoredText());
            Text temp = new Text();
            line++;
            // TODO: 12-Dec-17 masukkin data ke table..
        });
    }
}
