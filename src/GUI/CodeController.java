package GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CodeController extends TabPane implements Initializable {
    @FXML
    private TextField codeLine;
    @FXML
    private ListView<Text> pNum;
    @FXML
    private ListView<FlowPane> pseudocode;
    @FXML
    private ListView<Text> adNum;
    @FXML
    private ListView<FlowPane> address;
    @FXML
    private ListView<Text> hNum;
    @FXML
    private ListView<FlowPane> hexa;
    @FXML
    private ListView<Text> decNum;
    @FXML
    private ListView<Text> decimal;
    @FXML
    private FlowPane bundle;

    private ObservableList<FlowPane> pseudoList = FXCollections.observableArrayList();
    private ObservableList<FlowPane> adList = FXCollections.observableArrayList();
    private ObservableList<FlowPane> hexaList = FXCollections.observableArrayList();
    private String code;
    private ColorParser cp;
    private Integer line = 0;
    private static ArrayList<String> rawCode = new ArrayList<>();

    public static ArrayList<String> getRawCode() {
        return rawCode;
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
        pseudocode.setItems(pseudoList);
        address.setItems(adList);
        hexa.setItems(hexaList);

        codeLine.setOnAction(e -> {
            code = codeLine.getText();
            rawCode.add(code);
            addLine(line, code);
            codeLine.setText("");
        });

    }

    public void addLine (Integer lineNum, String code) {
        cp = new ColorParser(code);
        bundle = new FlowPane();
        bundle.getChildren().addAll (cp.getColoredText());
        pseudoList.add(bundle);
        line++;
        Text temp = new Text(line.toString());
        temp.setFill(Color.rgb(255,255,255));
        temp.setFont(Font.font("Monospaced Regular", 14.0));
        pNum.getItems().add(temp);
    }

    public void setAdTable (ArrayList<String> assemblyCode) {
        for (int i = 0; i<assemblyCode.size(); i++) {
            cp = new ColorParser (assemblyCode.get(i).substring(assemblyCode.get(i).indexOf("]")+1,assemblyCode.get(i).length()-1));
            bundle = new FlowPane();
            bundle.getChildren().addAll(cp.getColoredText());
            adList.add(bundle);
            Text temp = new Text(assemblyCode.get(i).substring(0,assemblyCode.get(i).indexOf("]")));
            temp.setFill(Color.rgb(255,255,255));
            temp.setFont(Font.font("Monospaced Regular", 14.0));
            adNum.getItems().add(temp);
        }
    }

    public void setHTable (ArrayList<String> hexaCode) {
        for (int i = 0; i<hexaCode.size(); i++) {
            cp = new ColorParser (hexaCode.get(i).substring(hexaCode.get(i).indexOf("]")+1,hexaCode.get(i).length()-1));
            bundle = new FlowPane();
            bundle.getChildren().addAll(cp.getColoredText());
            hexaList.add(bundle);
            Text temp = new Text(hexaCode.get(i).substring(0,hexaCode.get(i).indexOf("]")));
            temp.setFill(Color.rgb(255,255,255));
            temp.setFont(Font.font("Monospaced Regular", 14.0));
            hNum.getItems().add(temp);
        }
    }

    public void setDecTable (ArrayList<String> hexCode, ArrayList<Long> decCode) {
        for (int i = 0; i<decCode.size(); i++) {
            Text temp = new Text(hexCode.get(i).substring(0,hexCode.get(i).indexOf("]")));
            temp.setFill(Color.rgb(255,255,255));
            decNum.getItems().add(temp);
            temp.setFont(Font.font("Monospaced Regular", 14.0));
            temp = new Text (decCode.get(i).toString());
            temp.setFill(Color.rgb(255,255,255));
            temp.setFont(Font.font("Monospaced Regular", 14.0));
            decimal.getItems().add(temp);
        }
    }

}
