package GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CodeController extends TabPane implements Initializable {
    @FXML
    private TextField codeLine;
    @FXML
    private TableColumn<Integer, String> pNum;
    @FXML
    private TableColumn<ArrayList<Text>, String> pseudocode;
    @FXML
    private TableColumn<Integer, String> adNum;
    @FXML
    private TableColumn<ArrayList<Text>, String> address;
    @FXML
    private TableColumn<Integer, String> hNum;
    @FXML
    private TableColumn<ArrayList<Text>, String> hexa;
    @FXML
    private TableColumn<Integer, String> decNum;
    @FXML
    private TableColumn<ArrayList<Text>, String> decimal;
    @FXML
    private TableView<CodeLine> pTable;
    @FXML
    private TableView<CodeLine> adTable;
    @FXML
    private TableView<CodeLine> hTable;
    @FXML
    private TableView<CodeLine> decTable;

    private String code;
    private ColorParser cp;
    private Integer line = 0;
    private static ArrayList<String> rawCode;
    private ObservableList<CodeLine> codes;
    private ObservableList<CodeLine> cAddress;
    private ObservableList<CodeLine> cHexa;
    private ObservableList<CodeLine> cDec;

    public static ArrayList<String> getRawCode() {
        return rawCode;
    }

    public void setcAddress() {
        // TODO: 12-Dec-17 panggil fungsi parser yang ubah ke assembly buat dimasukin ke sini 
    }

    public void setcHexa(ObservableList<CodeLine> cHexa) {
        // TODO: 12-Dec-17 same as above
    }

    public void setcDec(ObservableList<CodeLine> cDec) {
        // TODO: 12-Dec-17 same as above
    }

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
            rawCode.add(code);
            cp = new ColorParser(code);
            codes.add(new CodeLine(line, cp.getColoredText()));
            setPTable();
        });
    }

    public void setPTable () {
        pNum.setCellValueFactory(new PropertyValueFactory<>("pNum"));
        pseudocode.setCellValueFactory(new PropertyValueFactory<>("pseudocode"));
        pTable.getItems().clear();
        pTable.getItems().addAll(codes);
    }

    public void setAdTable () {
        adNum.setCellValueFactory(new PropertyValueFactory<>("adNum"));
        address.setCellValueFactory(new PropertyValueFactory<>("address"));
        adTable.getItems().clear();
        adTable.getItems().addAll(cAddress);
    }

    public void setHTable () {
        hNum.setCellValueFactory(new PropertyValueFactory<>("hNum"));
        hexa.setCellValueFactory(new PropertyValueFactory<>("hexa"));
        hTable.getItems().clear();
        hTable.getItems().addAll(cHexa);
    }

    public void setDecTable () {
        decNum.setCellValueFactory(new PropertyValueFactory<>("decNum"));
        decimal.setCellValueFactory(new PropertyValueFactory<>("decimal"));
        decTable.getItems().clear();
        decTable.getItems().addAll(cDec);
    }

}
