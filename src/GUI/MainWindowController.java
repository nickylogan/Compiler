package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.Instruction;
import main.Mapper;
import main.Parser;
import main.ParserException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainWindowController extends VBox implements Initializable {
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
    private MenuItem compile;
    @FXML
    private MenuItem about;

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
        about.setOnAction(e->{
            try {
                File file = new File("src/GUI/about.txt");
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                ScrollPane scrollPane = new ScrollPane();
                VBox vBox = new VBox();
                vBox.setPadding(new Insets(10,10,10,10));
                Stage stage = new Stage();
                while ((line = bufferedReader.readLine()) != null) {
                    Text text = new Text(line);
                    text.setFont(Font.font("Courier New", 14.0));
                    vBox.getChildren().add(text);
                }
                scrollPane.setContent(vBox);
                stage.setScene(new Scene(scrollPane, 800,600));
                stage.show();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        });
    }

    public void writeToFile(File file) {
        try {
            PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
            ArrayList<String> rawCode = CodeController.getRawCode();
            for (String aRawCode : rawCode) {
                printWriter.println(aRawCode);
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
            int i = 1;
            while ((line = bufferedReader.readLine()) != null) {
                CodeController.getRawCode().add(line);
                cc.addLine(i++, line);
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

    public void compile(File file) {
        try {
            ArrayList<Instruction> ins = Parser.compile(CodeController.getRawCode());
            ArrayList<String> insStr = Parser.convertInstructionsToString(ins); //nanti didisplay di tab assmebly code
            ArrayList<String> hex = Mapper.convertToHexString(ins); //display di tab machine code (hex)
            ArrayList<Long> machineCode = Mapper.convertToMachineCode(ins); //display di tab machine code (dec)
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
        } catch (ParserException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.setHeaderText(null);
            alert.setTitle("Compile error");
            alert.showAndWait();
        }
    }
}
