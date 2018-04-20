package GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import main.Main;

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
    private ListView<FlowPane> address;
    @FXML
    private ListView<FlowPane> hexa;
    @FXML
    private ListView<FlowPane> decimal;
    @FXML
    private FlowPane bundle;
    @FXML
    private ScrollPane s1;
    @FXML
    private ScrollPane s2;
    @FXML
    private ScrollPane s3;
    @FXML
    private ScrollPane s4;
    @FXML
    private ScrollPane s5;

    private ObservableList<FlowPane> pseudoList = FXCollections.observableArrayList();
    private ObservableList<FlowPane> adList = FXCollections.observableArrayList();
    private ObservableList<FlowPane> hexaList = FXCollections.observableArrayList();
    private ObservableList<FlowPane> decList = FXCollections.observableArrayList();
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
        decimal.setItems(decList);

        codeLine.setOnAction(e -> {
            Main.mainWindow.setSaved(false);
            code = codeLine.getText();
            rawCode.add(code);
            addLine(line, code);
            codeLine.setText("");
        });
    }

    public void addLine (Integer lineNumber, String code) {
        cp = new ColorParser(code);
        bundle = new FlowPane();
        bundle.getChildren().addAll (cp.getColoredText());
        pseudoList.add(bundle);
        line++;
        Text temp = new Text(line.toString());
        temp.setFill(Color.rgb(255,255,255));
        temp.setFont(Font.font("Courier New", 14.0));
        pNum.getItems().add(temp);
    }

    public void setAdTable (ArrayList<String> assemblyCode) {
//        System.out.println(assemblyCode);
        int i = 0;
        for (String anAssemblyCode : assemblyCode) {
            ArrayList<Text> arr = new ArrayList<>();
            Text ad = new Text("["+(i*4/10)+(i*4%10)+"] " );
            Text t = new Text(anAssemblyCode);
            ad.setFill(Color.rgb(210,148,93));
            ad.setFont(Font.font("Courier New", 14.0));
            t.setFill(Color.WHITE);
            t.setFont(Font.font("Courier New", 14.0));
            arr.add(ad);
            arr.add(t);
            bundle = new FlowPane();
            bundle.getChildren().addAll(arr);
            adList.add(bundle);
            ++i;
        }
    }

    public void setHTable (ArrayList<String> hexaCode) {
        int i = 0;
        for (String aHexaCode : hexaCode) {
            ArrayList<Text> t = new ArrayList<>();
            Text ad = new Text("["+(i*4/10)+(i*4%10)+"] ");
            Text co = new Text(aHexaCode);
            ad.setFill(Color.rgb(210,148,93));
            ad.setFont(Font.font("Courier New", 14.0));
            co.setFill(Color.WHITE);
            co.setFont(Font.font("Courier New", 14.0));
            t.add(ad);
            t.add(co);
            bundle = new FlowPane();
            bundle.getChildren().addAll(t);
            hexaList.add(bundle);
            i++;
        }
    }

    public void setDecTable (ArrayList<String> hexCode, ArrayList<Long> decCode) {
        for (int i = 0; i<decCode.size(); i++) {
            ArrayList<Text> t = new ArrayList<>();
            Text ad = new Text("["+(i*4/10)+(i*4%10)+"] ");
            ad.setFill(Color.rgb(210,148,93));
            ad.setFont(Font.font("Courier New", 14.0));
            Text temp = new Text (decCode.get(i).toString());
            temp.setFill(Color.rgb(255,255,255));
            temp.setFont(Font.font("Courier New", 14.0));
            t.add(ad);
            t.add(temp);
            bundle = new FlowPane();
            bundle.getChildren().setAll(t);
            decList.add(bundle);
        }
    }

}
