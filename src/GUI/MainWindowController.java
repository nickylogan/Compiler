package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import main.Instruction;
import main.Mapper;
import main.Parser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainWindowController extends VBox implements Initializable{
    @FXML
    private AnchorPane codeArea;
    @FXML
    private MenuItem newFile;
    @FXML
    private MenuItem save;
    @FXML
    private MenuItem open;
    @FXML
    private MenuItem close;
    @FXML
    private MenuItem undo;
    @FXML
    private MenuItem redo;
    @FXML
    private MenuItem compile;
    private CodeController cc;
    public MainWindowController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
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
        cc = new CodeController();
        codeArea.getChildren().add(cc);
        AnchorPane.setTopAnchor(cc, 0.0);
        AnchorPane.setBottomAnchor(cc, 0.0);
        AnchorPane.setLeftAnchor(cc, 0.0);
        AnchorPane.setRightAnchor(cc, 0.0);
    }

    public void writeToFile(File file) {
        try {
            PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
            ArrayList<String> rawCode = CodeController.getRawCode();
            for (String aRawCode : rawCode) {
                printWriter.println(aRawCode + "\n");
            }
            printWriter.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error loading file");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void loadFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            ArrayList<String> rawCode = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                rawCode.add(line);
            }
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("File not found!");
            alert.setContentText("Error loading " + file.getName());
            alert.showAndWait();
        } catch (IOException ignored) {
        }
    }

    public void newDocument() {

    }

    public AnchorPane getCodeArea() {
        return codeArea;
    }

    public MenuItem getNewFile() {
        return newFile;
    }

    public MenuItem getSave() {
        return save;
    }

    public MenuItem getOpen() {
        return open;
    }

    public MenuItem getClose() {
        return close;
    }

    public MenuItem getUndo() {
        return undo;
    }

    public MenuItem getRedo() {
        return redo;
    }

    public MenuItem getCompile() {
        return compile;
    }

    public CodeController getCc() {
        return cc;
    }

//    public static void writeFile(File file, ArrayList<Long> machineCode) throws IOException {
//        PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
//        for(Long l : machineCode){
//            printWriter.println(l);
//        }
//        printWriter.close();
//    }

    public void compile(File file){
        ArrayList<Instruction> ins = Parser.compile(CodeController.getRawCode());
        ArrayList<String> insStr = Parser.convertInstructionsToString(ins); //nanti didisplay di tab assmebly code
        ArrayList<String> hex = Mapper.convertToHexString(ins); //display di tab machine code (hex)
        ArrayList<Long> machineCode =  Mapper.convertToMachineCode(ins); //display di tab machine code (dec)
        cc.setAdTable(insStr);
        cc.setHTable(hex);
        cc.setDecTable(hex, machineCode);
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(file.getAbsolutePath());
            for (Long l : machineCode) {
                printWriter.println(l);
            }
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
