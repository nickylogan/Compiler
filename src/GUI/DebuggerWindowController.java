package GUI;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import main.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import static main.SymbolType.ARRAY;
import static main.SymbolType.VAR;

public class DebuggerWindowController extends BorderPane implements Initializable {
  @FXML
  private TextField PC;
  @FXML
  private TextField FLAG;
  @FXML
  private TextField BREG;
  @FXML
  private TextField LREG;
  @FXML
  private TextField SP;
  @FXML
  private TextField BP;
  @FXML
  private TextField R0, R1, R2, R3, R4, R5, R6, R7, R8,
      R9, R10, R11, R12, R13, R14, R15;

  @FXML
  private TreeTableView<SimpleSymbol> symbolTableViewer;
  @FXML
  private TreeTableColumn<SimpleSymbol, String> nameCol;
  @FXML
  private TreeTableColumn<SimpleSymbol, String> scopeIDCol;
  @FXML
  private TreeTableColumn<SimpleSymbol, String> typeCol;
  @FXML
  private TreeTableColumn<SimpleSymbol, Number> sizeCol;
  @FXML
  private TreeTableColumn<SimpleSymbol, String> locationCol;
  @FXML
  private TreeTableColumn<SimpleSymbol, Number> valueCol;
  @FXML
  private ListView<String> codeViewer;
  @FXML
  private TextField currentInstruction;
  @FXML
  private Button nextButton;

  private TreeItem<SimpleSymbol> root;

  private Program program;

  private ObservableList<String> instructionList = FXCollections.observableArrayList();

  private HashMap<Integer, SimpleSymbol> symbolHashMap = new HashMap<>();

  int index = 0;

  public DebuggerWindowController(Program program) {
    this.root = new TreeItem<>(new SimpleSymbol("", "", "", 0, 0));
    this.program = program;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("DebuggerWindow.fxml"));
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
    SymbolTable symbolTable = program.getSymbolTable();
    // initialize symbol table viewer
    for (String symbolName : symbolTable.keySet()) {
      HashMap<String, Symbol> scopes = symbolTable.get(symbolName);
      for (String scopeID : scopes.keySet()) {
        Symbol symbol = scopes.get(scopeID);
        SymbolType type = symbol.getType();
        int size = symbol.getSize();
        int loc = symbol.getLocation().getIntValue();
        System.out.println(loc);
        SimpleSymbol simpleSymbol =
            new SimpleSymbol(
                symbolName, scopeID, type.toString(), size, loc
            );
        TreeItem<SimpleSymbol> simpleSymbolTreeItem = new TreeItem<>(simpleSymbol);
        if (type == ARRAY) {
          for (int i = 0; i < size / 4; ++i) {
            int loc1 = loc + i*4;
            SimpleSymbol element = new SimpleSymbol(
                "[" + i + "]", scopeID, VAR.toString(), 4, loc1
            );
            TreeItem<SimpleSymbol> elementTreeItem = new TreeItem<>(element);
            simpleSymbolTreeItem.getChildren().add(elementTreeItem);
            symbolHashMap.put(loc1, element);
          }
        } else {
          symbolHashMap.put(loc, simpleSymbol);
        }
        root.getChildren().add(simpleSymbolTreeItem);
      }
    }

    nameCol.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
    scopeIDCol.setCellValueFactory(param -> param.getValue().getValue().scopeIDProperty());
    typeCol.setCellValueFactory(param -> param.getValue().getValue().typeProperty());
    sizeCol.setCellValueFactory(param -> param.getValue().getValue().sizeProperty());
    locationCol.setCellValueFactory(param -> param.getValue().getValue().locationProperty());
    valueCol.setCellValueFactory(param -> param.getValue().getValue().valueProperty());

    this.symbolTableViewer.setRoot(root);
    this.symbolTableViewer.setShowRoot(false);
    program.getInstructions().forEach(ins -> instructionList.add(ins.toString()));
    this.codeViewer.setItems(instructionList);

    nextButton.setOnAction(e -> next());
  }

  private void next() {
    nextButton.setText("Next");
    // refresh value on symbol table
    for (Integer i : symbolHashMap.keySet()) {
      byte[] val = program.accessMemory(i);
      SimpleSymbol simpleSymbol = symbolHashMap.get(i);
      int res = 0;
//      System.out.println(i + ": ");
      for(int j = 3; j>=0; --j) res = (res << 8) + val[j];
//      System.out.println("\n");
      simpleSymbol.setValue(res);
    }

    // refresh registers
    R0.setText(Integer.toString(program.getRegisterValue(0)));
    R1.setText(Integer.toString(program.getRegisterValue(1)));
    R2.setText(Integer.toString(program.getRegisterValue(2)));
    R3.setText(Integer.toString(program.getRegisterValue(3)));
    R4.setText(Integer.toString(program.getRegisterValue(4)));
    R5.setText(Integer.toString(program.getRegisterValue(5)));
    R6.setText(Integer.toString(program.getRegisterValue(6)));
    R7.setText(Integer.toString(program.getRegisterValue(7)));
    R8.setText(Integer.toString(program.getRegisterValue(8)));
    R9.setText(Integer.toString(program.getRegisterValue(9)));
    R10.setText(Integer.toString(program.getRegisterValue(10)));
    R11.setText(Integer.toString(program.getRegisterValue(11)));
    R12.setText(Integer.toString(program.getRegisterValue(12)));
    R13.setText(Integer.toString(program.getRegisterValue(13)));
    R14.setText(Integer.toString(program.getRegisterValue(14)));
    R15.setText(Integer.toString(program.getRegisterValue(15)));

    PC.setText(Integer.toString(program.getProgramCounter()));
    FLAG.setText(Integer.toString(program.getFlagRegister()));
    BREG.setText(Integer.toString(program.getBaseRegister()));
    LREG.setText(Integer.toString(program.getLimitRegister()));
    BP.setText(Integer.toString(program.getBasePointer()));
    SP.setText(Integer.toString(program.getStackPointer()));

    index = program.executeNext();

    if(index == -1) {
      nextButton.setDisable(true);
      nextButton.setText("Execution finished");
    } else {
      currentInstruction.setText(instructionList.get(index));
      // highlight code viewer
      codeViewer.getSelectionModel().clearSelection();
      codeViewer.getSelectionModel().select(index);
    }
  }
}
