package GUI.debugger;

import com.sun.istack.internal.NotNull;
import compiler.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.*;
import utils.StringUTILS;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static compiler.SymbolType.ARRAY;
import static compiler.SymbolType.VAR;

public class DebuggerWindowController extends BorderPane implements Initializable {

  // register panel
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

  // symbol table
  @FXML
  private TreeTableView<ObservableSymbol> symbolTableViewer;
  @FXML
  private TreeTableColumn<ObservableSymbol, String> nameCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, String> scopeIDCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, String> typeCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, Number> sizeCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, Number> locationCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, Number> valueCol;
  @FXML
  private TreeTableColumn<ObservableSymbol, Boolean> changedCol;
  private TreeItem<ObservableSymbol> root;

  // instruction viewer
  @FXML
  private TextField currentInstruction;
  @FXML
  private Button nextButton;
  @FXML
  private ListView<ObservableInstruction> codeViewer;

  @FXML
  private VBox middleColumn;
  @FXML
  private GridPane memoryViewer;

  @FXML
  private ScrollBar memoryScrollBar;
  @FXML
  private TextField locator;
  @FXML
  private Button goTo;

  private int MAX_PAGES;
  private static final int COLUMN_SIZE = 16;
  private static final int ROW_SIZE = 8;
  private SimpleIntegerProperty page = new SimpleIntegerProperty(0);
  private Label[] pageLabels = new Label[ROW_SIZE];
  private Label[][] memoryContentLabels = new Label[ROW_SIZE][COLUMN_SIZE];

  private Program program;
  private ObservableList<ObservableInstruction> instructionList = FXCollections.observableArrayList();
  private HashMap<Integer, ObservableSymbol> symbolHashMap = new HashMap<>();
  private ObservableList<Integer> changedList = FXCollections.observableArrayList();

  private int index = 0;
  private int index2 = 0;

  private SimpleIntegerProperty selected = new SimpleIntegerProperty(-1);

  DebuggerWindowController(Program program) {
    this.root = new TreeItem<>(new ObservableSymbol("", "", "", 0, 0));
    this.program = program;
    MAX_PAGES = program.getMemorySize() / 16;

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
    // initialize symbol table viewer
    SymbolTable symbolTable = program.getSymbolTable();
    for (String symbolName : symbolTable.keySet()) {
      HashMap<String, Symbol> scopes = symbolTable.get(symbolName);
      for (String scopeID : scopes.keySet()) {
        Symbol symbol = scopes.get(scopeID);
        SymbolType type = symbol.getType();
        int size = symbol.getSize();
        int loc = symbol.getLocation().getIntValue();
        System.out.println(loc);
        ObservableSymbol observableSymbol =
            new ObservableSymbol(
                symbolName, scopeID, type.toString(), size, loc
            );
        TreeItem<ObservableSymbol> simpleSymbolTreeItem = new TreeItem<>(observableSymbol);
        if (type == ARRAY) {
          for (int i = 0; i < size / 4; ++i) {
            int loc1 = loc + i * 4;
            ObservableSymbol element = new ObservableSymbol(
                "[" + i + "]", scopeID, VAR.toString(), 4, loc1
            );
            TreeItem<ObservableSymbol> elementTreeItem = new TreeItem<>(element);
            simpleSymbolTreeItem.getChildren().add(elementTreeItem);
            symbolHashMap.put(loc1, element);
          }
        } else {
          symbolHashMap.put(loc, observableSymbol);
        }
        root.getChildren().add(simpleSymbolTreeItem);
      }
    }
    nameCol.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
    nameCol.setCellFactory(new SymbolTableCellFactory<>());
    nameCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.25));
    scopeIDCol.setCellValueFactory(param -> param.getValue().getValue().scopeIDProperty());
    scopeIDCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.15));
    scopeIDCol.setCellFactory(new SymbolTableCellFactory<>());
    typeCol.setCellValueFactory(param -> param.getValue().getValue().typeProperty());
    typeCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.15));
    typeCol.setCellFactory(new SymbolTableCellFactory<>());
    sizeCol.setCellValueFactory(param -> param.getValue().getValue().sizeProperty());
    sizeCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.10));
    sizeCol.setCellFactory(new SymbolTableCellFactory<>());
    locationCol.setCellValueFactory(param -> param.getValue().getValue().locationProperty());
    locationCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.15));
    locationCol.setCellFactory(new SymbolTableCellFactory<>());
    valueCol.setCellValueFactory(param -> param.getValue().getValue().valueProperty());
    valueCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(.20));
    valueCol.setCellFactory(new SymbolTableCellFactory<>());
    changedCol.setCellValueFactory(param -> param.getValue().getValue().changedProperty());
    changedCol.prefWidthProperty().bind(middleColumn.widthProperty().multiply(0));
    changedCol.setCellFactory(new SymbolTableCellFactory<>());
    this.symbolTableViewer.setRoot(root);

    // initialize code viewer
    ArrayList<Instruction> instructions = program.getInstructions();
    ObservableInstruction.setMaxLocation((instructions.size() - 1) * 4);
    for (int i = 0; i < instructions.size(); i++) {
      Instruction ins = instructions.get(i);
      ObservableInstruction observableInstruction =
          new ObservableInstruction(i * 4, ins.getOperator().toString(), ins.getStringOperands());
      instructionList.add(observableInstruction);
    }
    codeViewer.setItems(instructionList);
    codeViewer.setOnMousePressed(e -> {
      if (index != -1) {
        codeViewer.getSelectionModel().clearSelection();
        codeViewer.getSelectionModel().select(index);
      }
    });

    // initialize next button
    nextButton.setOnAction(e -> next());
    // initialize memory viewer
    for (int i = 0; i < ROW_SIZE; ++i) {
      Label label = new Label();
      memoryViewer.add(label, 0, i + 1);
      GridPane.setHgrow(label, Priority.ALWAYS);
      GridPane.setHalignment(label, HPos.CENTER);
      GridPane.setValignment(label, VPos.CENTER);
      label.getStyleClass().add("bold");
      pageLabels[i] = label;
    }
    for (int i = 0; i < ROW_SIZE; ++i) {
      for (int j = 0; j < COLUMN_SIZE; ++j) {
        Label label = new Label();
        memoryViewer.add(label, j + 1, i + 1);
        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setVgrow(label, Priority.ALWAYS);
        GridPane.setHalignment(label, HPos.CENTER);
        GridPane.setValignment(label, VPos.CENTER);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        int finalJ = j, finalI = i;
        label.setOnMouseClicked(e -> selected.set(page.get() * COLUMN_SIZE + finalI * COLUMN_SIZE + finalJ));
        label.getStyleClass().add("memory");
        memoryContentLabels[i][j] = label;
      }
    }
    updateMemoryLabels();
    page.addListener((observable, oldValue, newValue) -> {
      updateMemoryLabels();
      updateMemoryHighlights(oldValue.intValue(), newValue.intValue(), selected.get(), selected.get(), changedList, changedList);
    });
    
    memoryScrollBar.setMax(MAX_PAGES - ROW_SIZE);
    memoryScrollBar.setVisibleAmount(MAX_PAGES / 10.0);
    memoryScrollBar.valueProperty().bindBidirectional(page);
    memoryViewer.setOnScroll(e -> {
      int newPage = Math.min(Math.max(0, page.get() + (e.getDeltaY() >= 0 ? -1 : 1)), MAX_PAGES - ROW_SIZE);
      page.set(newPage);
    });
    selected.addListener((observable, oldValue, newValue) -> {
      updateMemoryHighlights(page.get(), page.get(), oldValue.intValue(), newValue.intValue(), changedList, changedList);
      locator.setText(String.valueOf(newValue.intValue()));
    });
    locator.setOnAction(e -> {
      if (locator.getText().matches("\\d+")) {
        int i = Integer.valueOf(locator.getText());
        if (i < program.getMemorySize()) {
          selected.set(i);
          page.set(i / COLUMN_SIZE);
        } else {
          Alert alert = new Alert(Alert.AlertType.ERROR, "Out of bounds!");
          alert.setHeaderText(null);
          alert.setTitle("Out of bounds!");
          alert.showAndWait();
        }
      }
    });
    locator.setOnKeyTyped(e -> {
      if (!e.getCharacter().matches("\\d")) e.consume();
    });
    goTo.setOnAction(e -> locator.fireEvent(new ActionEvent()));

    symbolHashMap.forEach((integer, observableSymbol) -> {
      observableSymbol.changedProperty().addListener((obs, oldV, newV) -> {
        ObservableList<Integer> oldList = FXCollections.observableArrayList(changedList);
        if(newV) changedList.add(observableSymbol.getLocation());
        else changedList.remove(observableSymbol.getLocation());
        updateMemoryHighlights(page.get(), page.get(), selected.get(), selected.get(), oldList, changedList);
      });
    });
  }

  private void updateMemoryLabels() {
    int pageInt = page.get();
    for (int i = 0; i < ROW_SIZE; ++i) {
      pageLabels[i].setText(
          "0x" + StringUTILS.toHexStringWithLength(pageInt * COLUMN_SIZE + i * COLUMN_SIZE, 4).toUpperCase()
      );
    }
    Set<Integer> keyset = symbolHashMap.keySet();
    for (int i = 0; i < ROW_SIZE * COLUMN_SIZE; ++i) {
      int loc = pageInt * COLUMN_SIZE + i;
      if (keyset.contains(loc)) {
        byte[] bytes = symbolHashMap.get(loc).getBytes();
        memoryContentLabels[i / COLUMN_SIZE][i++ % COLUMN_SIZE].setText(
            StringUTILS.toHexStringWithLength(bytes[0], 2).toUpperCase());
        memoryContentLabels[i / COLUMN_SIZE][i++ % COLUMN_SIZE].setText(
            StringUTILS.toHexStringWithLength(bytes[1], 2).toUpperCase());
        memoryContentLabels[i / COLUMN_SIZE][i++ % COLUMN_SIZE].setText(
            StringUTILS.toHexStringWithLength(bytes[2], 2).toUpperCase());
        memoryContentLabels[i / COLUMN_SIZE][i % COLUMN_SIZE].setText(
            StringUTILS.toHexStringWithLength(bytes[3], 2).toUpperCase());
      } else {
        memoryContentLabels[i / COLUMN_SIZE][i % COLUMN_SIZE].setText(
            StringUTILS.toHexStringWithLength(program.accessByte(loc), 2).toUpperCase());
      }
    }

  }

  private void updateMemoryHighlights(int oldPage, int newPage, int oldSelected, int newSelected, @NotNull ObservableList<Integer> oldChangedList, @NotNull ObservableList<Integer> newChangedList) {
    int oldFocusedRow = (oldSelected - oldPage * COLUMN_SIZE) / COLUMN_SIZE;
    int oldFocusedCol = (oldSelected - oldPage * COLUMN_SIZE) % COLUMN_SIZE;
    int newFocusedRow = (newSelected - newPage * COLUMN_SIZE) / COLUMN_SIZE;
    int newFocusedCol = (newSelected - newPage * COLUMN_SIZE) % COLUMN_SIZE;
    
    if (0 <= oldFocusedRow && oldFocusedRow < ROW_SIZE && 0 <= oldFocusedCol && oldFocusedCol < COLUMN_SIZE)
      memoryContentLabels[oldFocusedRow][oldFocusedCol].getStyleClass().remove("focused");
    if (0 <= newFocusedRow && newFocusedRow < ROW_SIZE && 0 <= newFocusedCol && newFocusedCol < COLUMN_SIZE)
      memoryContentLabels[newFocusedRow][newFocusedCol].getStyleClass().add("focused");
    
    for(int i = 0; i<oldChangedList.size(); ++i){
      int location = oldChangedList.get(i);
      for(int j = 0; j < 4; ++j){
        int oldChangedRow = (location + j - oldPage * COLUMN_SIZE) / COLUMN_SIZE;
        int oldChangedCol = (location + j - oldPage * COLUMN_SIZE) % COLUMN_SIZE;
        if (0 <= oldChangedRow && oldChangedRow < ROW_SIZE && 0 <= oldChangedCol && oldChangedCol < COLUMN_SIZE)
          memoryContentLabels[oldChangedRow][oldChangedCol].getStyleClass().remove("changed");
      }
    }
    for(int i = 0; i<newChangedList.size(); ++i){
      int location = newChangedList.get(i);
      for(int j = 0; j < 4; ++j) {
        int newChangedRow = (location + j - newPage * COLUMN_SIZE) / COLUMN_SIZE;
        int newChangedCol = (location + j - newPage * COLUMN_SIZE) % COLUMN_SIZE;
        if (0 <= newChangedRow && newChangedRow < ROW_SIZE && 0 <= newChangedCol && newChangedCol < COLUMN_SIZE)
          memoryContentLabels[newChangedRow][newChangedCol].getStyleClass().add("changed");
      }
    }
  }

  private void next() {
    nextButton.requestFocus();
    nextButton.setText("Next");
    if (index2 % 2 == 0) {
      R0.getStyleClass().remove("changed");
      R1.getStyleClass().remove("changed");
      R2.getStyleClass().remove("changed");
      R3.getStyleClass().remove("changed");
      R4.getStyleClass().remove("changed");
      R5.getStyleClass().remove("changed");
      R6.getStyleClass().remove("changed");
      R7.getStyleClass().remove("changed");
      R8.getStyleClass().remove("changed");
      R9.getStyleClass().remove("changed");
      R10.getStyleClass().remove("changed");
      R11.getStyleClass().remove("changed");
      R12.getStyleClass().remove("changed");
      R13.getStyleClass().remove("changed");
      R14.getStyleClass().remove("changed");
      R15.getStyleClass().remove("changed");
      FLAG.getStyleClass().remove("changed");
      BREG.getStyleClass().remove("changed");
      LREG.getStyleClass().remove("changed");
      BP.getStyleClass().remove("changed");
      SP.getStyleClass().remove("changed");

      String pc = Integer.toString(program.getProgramCounter());
      if (!PC.getText().equals(pc)) {
        if (!PC.getText().isEmpty()) {
          PC.getStyleClass().add("changed");
        }
        PC.setText(pc);
      } else {
        PC.getStyleClass().remove("changed");
      }
      index = program.executeNext();

      if (index == -1) {
        nextButton.setDisable(true);
        nextButton.setText("Execution finished");
      } else {
        currentInstruction.setText(instructionList.get(index).shortString());
        // highlight code viewer
        codeViewer.getSelectionModel().clearSelection();
        codeViewer.getSelectionModel().select(index);
      }
    } else {
      PC.getStyleClass().remove("changed");

      // refresh value on symbol table
      for (Integer i : symbolHashMap.keySet()) {
        byte[] val = program.accessMemory(i);
        ObservableSymbol observableSymbol = symbolHashMap.get(i);
        observableSymbol.setBytes(val);
      }

      // refresh registers
      String r0 = Integer.toString(program.getRegisterValue(0));
      if (!R0.getText().equals(r0)) {
        if (!R0.getText().isEmpty()) {
          R0.getStyleClass().add("changed");
        }
        R0.setText(r0);
      } else {
        R0.getStyleClass().remove("changed");
      }
      String r1 = Integer.toString(program.getRegisterValue(1));
      if (!R1.getText().equals(r1)) {
        if (!R1.getText().isEmpty()) R1.getStyleClass().add("changed");
        R1.setText(r1);
      } else {
        R1.getStyleClass().remove("changed");
      }
      String r2 = Integer.toString(program.getRegisterValue(2));
      if (!R2.getText().equals(r2)) {
        if (!R2.getText().isEmpty())
          R2.getStyleClass().add("changed");
        R2.setText(r2);
      } else {
        R2.getStyleClass().remove("changed");
      }
      String r3 = Integer.toString(program.getRegisterValue(3));
      if (!R3.getText().equals(r3)) {
        if (!R3.getText().isEmpty())
          R3.getStyleClass().add("changed");
        R3.setText(r3);
      } else {
        R3.getStyleClass().remove("changed");
      }
      String r4 = Integer.toString(program.getRegisterValue(4));
      if (!R4.getText().equals(r4)) {
        if (!R4.getText().isEmpty())
          R4.getStyleClass().add("changed");
        R4.setText(r4);
      } else {
        R4.getStyleClass().remove("changed");
      }
      String r5 = Integer.toString(program.getRegisterValue(5));
      if (!R5.getText().equals(r5)) {
        if (!R5.getText().isEmpty())
          R5.getStyleClass().add("changed");
        R5.setText(r5);
      } else {
        R5.getStyleClass().remove("changed");
      }
      String r6 = Integer.toString(program.getRegisterValue(6));
      if (!R6.getText().equals(r0)) {
        if (!R6.getText().isEmpty())
          R6.getStyleClass().add("changed");
        R6.setText(r6);
      } else {
        R6.getStyleClass().remove("changed");
      }
      String r7 = Integer.toString(program.getRegisterValue(7));
      if (!R7.getText().equals(r7)) {
        if (!R7.getText().isEmpty())
          R7.getStyleClass().add("changed");
        R7.setText(r7);
      } else {
        R7.getStyleClass().remove("changed");
      }
      String r8 = Integer.toString(program.getRegisterValue(8));
      if (!R8.getText().equals(r8)) {
        if (!R8.getText().isEmpty())
          R8.getStyleClass().add("changed");
        R8.setText(r8);
      } else {
        R8.getStyleClass().remove("changed");
      }
      String r9 = Integer.toString(program.getRegisterValue(9));
      if (!R9.getText().equals(r9)) {
        if (!R9.getText().isEmpty())
          R9.getStyleClass().add("changed");
        R9.setText(r9);
      } else {
        R9.getStyleClass().remove("changed");
      }
      String r10 = Integer.toString(program.getRegisterValue(10));
      if (!R10.getText().equals(r10)) {
        if (!R10.getText().isEmpty())
          R10.getStyleClass().add("changed");
        R10.setText(r10);
      } else {
        R10.getStyleClass().remove("changed");
      }
      String r11 = Integer.toString(program.getRegisterValue(11));
      if (!R11.getText().equals(r11)) {
        if (!R11.getText().isEmpty())
          R11.getStyleClass().add("changed");
        R11.setText(r11);
      } else {
        R11.getStyleClass().remove("changed");
      }
      String r12 = Integer.toString(program.getRegisterValue(12));
      if (!R12.getText().equals(r12)) {
        if (!R12.getText().isEmpty())
          R12.getStyleClass().add("changed");
        R12.setText(r12);
      } else {
        R12.getStyleClass().remove("changed");
      }
      String r13 = Integer.toString(program.getRegisterValue(13));
      if (!R13.getText().equals(r13)) {
        if (!R13.getText().isEmpty())
          R13.getStyleClass().add("changed");
        R13.setText(r13);
      } else {
        R13.getStyleClass().remove("changed");
      }
      String r14 = Integer.toString(program.getRegisterValue(14));
      if (!R14.getText().equals(r14)) {
        if (!R14.getText().isEmpty()) {
          R14.getStyleClass().add("changed");
        }
        R14.setText(r14);
      } else {
        R14.getStyleClass().remove("changed");
      }
      String r15 = Integer.toString(program.getRegisterValue(15));
      if (!R15.getText().equals(r15)) {
        if (!R15.getText().isEmpty())
          R15.getStyleClass().add("changed");
        R15.setText(r15);
      } else {
        R15.getStyleClass().remove("changed");
      }

      String flag = Integer.toString(program.getFlagRegister());
      if (!FLAG.getText().equals(flag)) {
        if (!FLAG.getText().isEmpty())
          FLAG.getStyleClass().add("changed");
        FLAG.setText(flag);
      } else {
        FLAG.getStyleClass().remove("changed");
      }
      String breg = Integer.toString(program.getBaseRegister());
      if (!BREG.getText().equals(breg)) {
        if (!BREG.getText().isEmpty())
          BREG.getStyleClass().add("changed");
        BREG.setText(breg);
      } else {
        BREG.getStyleClass().remove("changed");
      }
      String lreg = Integer.toString(program.getLimitRegister());
      if (!LREG.getText().equals(lreg)) {
        if (!LREG.getText().isEmpty())
          LREG.getStyleClass().add("changed");
        LREG.setText(lreg);
      } else {
        LREG.getStyleClass().remove("changed");
      }
      String bp = Integer.toString(program.getBasePointer());
      if (!BP.getText().equals(bp)) {
        if (!BP.getText().isEmpty())
          BP.getStyleClass().add("changed");
        BP.setText(bp);
      } else {
        BP.getStyleClass().remove("changed");
      }
      String sp = Integer.toString(program.getStackPointer());
      if (!SP.getText().equals(sp)) {
        if (!SP.getText().isEmpty())
          SP.getStyleClass().add("changed");
        SP.setText(sp);
      } else {
        SP.getStyleClass().remove("changed");
      }


      updateMemoryLabels();

    }
    ++index2;
  }
}
