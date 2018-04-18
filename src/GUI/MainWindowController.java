package GUI;

import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import main.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainWindowController extends BorderPane implements Initializable {
  //FX Components
  //Menu items
  //File menu items
  @FXML
  private MenuItem newMenu;
  @FXML
  private MenuItem openMenu;
  @FXML
  private MenuItem saveMenu;
  @FXML
  private MenuItem saveAsMenu;
  @FXML
  private MenuItem exitMenu;

  //Edit menu items
  @FXML
  private MenuItem cutMenu;
  @FXML
  private MenuItem copyMenu;
  @FXML
  private MenuItem pasteMenu;
  @FXML
  private MenuItem duplicateMenu;
  @FXML
  private MenuItem deleteMenu;

  //Compile menu items
  @FXML
  private MenuItem compileMenu;
  @FXML
  private MenuItem runMenu;

  //View menu items
  @FXML
  private CheckMenuItem toggleInstruction;
  @FXML
  private CheckMenuItem toggleMachineHex;
  @FXML
  private CheckMenuItem toggleMachineDec;

  //Help menu items
  @FXML
  private MenuItem helpMenu;
  @FXML
  private MenuItem aboutMenu;

  //Tab components
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab codeTab;
  @FXML
  private Tab instructionTab;
  @FXML
  private Tab hexTab;
  @FXML
  private Tab decTab;
  @FXML
  private AnchorPane codeTabArea;
  @FXML
  private AnchorPane instructionTabArea;
  @FXML
  private AnchorPane hexTabArea;
  @FXML
  private AnchorPane decTabArea;

  //Other FX components
  private CodeArea codeArea;
  private CodeArea instructionCodeArea;
  private CodeArea hexCodeArea;
  private CodeArea decCodeArea;

  //STRING PATTERNS
  //Pseudocode patterns
  private static final String[] KEYWORDS = new String[]{
      "if", "endif", "break", "while", "endwhile", "else", "continue", "var"
  };
  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
  private static final String OPERATOR_PATTERN = "[-+*/<>=]|==";
  private static final String NUMBER_PATTERN = "[0-9]+|true|false";

  //Instruction patterns
  private static final String[] INSTRUCTION_KEYWORDS = new String[]{
      "HALT", "ADD", "ADDI", "MOVR", "MOVI", "MOV", "MOVM", "MOVB",
      "MOVH", "MOVMH", "MOVL", "MOVML", "MOVD", "MOVMD", "PUSH", "POP",
      "PUSHF", "POPF", "INTR", "IN", "OUT", "JMP", "JMPR", "JE",
      "JNE", "JLT", "JGT", "SUB", "SUBI", "MOVMHW", "MUL", "MULI",
      "DIV", "DIVI", "CALL", "RET"
  };
  private static final String INSTRUCTION_OP_PATTERN = "\\b(" + String.join("|", INSTRUCTION_KEYWORDS) +")\\b";
  private static final String REGISTER_PATTERN = "\\b([Rr]([0-9]|1[0-5]))\\b";
  private static final String MEMORY_PATTERN = "\\[(" + REGISTER_PATTERN + ")\\+(" + NUMBER_PATTERN + ")\\]";

  //Hex patterns
  private static final String HEX_NUMBER_PATTERN = "[0-9A-Fa-f]+";
  private static final String HEX_PREFIX_PATTERN = "0x";

  private static final Pattern CODE_PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<REGISTER>" + REGISTER_PATTERN + ")"
          + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
          + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );
  private static final Pattern INSTRUCTION_PATTERN = Pattern.compile(
      "(?<OPERATOR>" + INSTRUCTION_OP_PATTERN + ")"
          + "|(?<MEMORY>" + MEMORY_PATTERN + ")"
          + "|(?<REGISTER>" + REGISTER_PATTERN + ")"
          + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
  );
  private static final Pattern HEX_PATTERN = Pattern.compile(
      "(?<HEXNUMBER>"+HEX_NUMBER_PATTERN+")" +
          "|(?<HEXPREFIX>"+HEX_PREFIX_PATTERN+")"
  );


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
    //set icons
    {
      newMenu.setGraphic(new ImageView("GUI/assets/text.png"));
      openMenu.setGraphic(new ImageView("GUI/assets/menu-open.png"));
      saveMenu.setGraphic(new ImageView("GUI/assets/menu-saveall.png"));
      exitMenu.setGraphic(new ImageView("GUI/assets/exit.png"));
      cutMenu.setGraphic(new ImageView("GUI/assets/menu-cut.png"));
      copyMenu.setGraphic(new ImageView("GUI/assets/copy.png"));
      pasteMenu.setGraphic(new ImageView("GUI/assets/menu-paste.png"));
      deleteMenu.setGraphic(new ImageView("GUI/assets/delete.png"));
      compileMenu.setGraphic(new ImageView("GUI/assets/compile.png"));
      runMenu.setGraphic(new ImageView("GUI/assets/run.png"));
      toggleInstruction.setGraphic(new ImageView("GUI/assets/gear.png"));
      toggleMachineHex.setGraphic(new ImageView("GUI/assets/hex.png"));
      toggleMachineDec.setGraphic(new ImageView("GUI/assets/dec.png"));
      helpMenu.setGraphic(new ImageView("GUI/assets/help.png"));
      codeTab.setGraphic(new ImageView("GUI/assets/brace.png"));
      instructionTab.setGraphic(new ImageView("GUI/assets/gear-dark.png"));
      hexTab.setGraphic(new ImageView("GUI/assets/hex-dark.png"));
      decTab.setGraphic(new ImageView("GUI/assets/dec-dark.png"));
    }

    //set code area
    codeArea = new CodeArea();
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    codeArea.richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
        .subscribe(change -> {
          codeArea.setStyleSpans(0, computeCodeHighlighting(codeArea.getText()));
        });

    codeArea.getStylesheets().add(getClass().getResource("code-area.css").toExternalForm());
    codeArea.setOnKeyTyped(e -> {
      if(e.getCharacter().equals("\t")) {
        codeArea.replaceText(codeArea.getText().substring(0, codeArea.getText().length() - 1));
        codeArea.replaceText(codeArea.getText() + "    ");
      }
    });
    codeTabArea.getChildren().add(codeArea);
    AnchorPane.setTopAnchor(codeArea, (double) 0);
    AnchorPane.setLeftAnchor(codeArea, (double) 0);
    AnchorPane.setRightAnchor(codeArea, (double) 0);
    AnchorPane.setBottomAnchor(codeArea, (double) 0);

    //set instruction area
    instructionCodeArea = new CodeArea();
    instructionCodeArea.richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(change -> {
          instructionCodeArea.setStyleSpans(
              0, computeInstructionHighlighting(instructionCodeArea.getText()));
        });
    instructionCodeArea.getStylesheets().add(getClass().getResource("instruction-area.css").toExternalForm());
    instructionCodeArea.setEditable(false);

    instructionTabArea.getChildren().add(instructionCodeArea);
    AnchorPane.setTopAnchor(instructionCodeArea, (double) 0);
    AnchorPane.setLeftAnchor(instructionCodeArea, (double) 0);
    AnchorPane.setRightAnchor(instructionCodeArea, (double) 0);
    AnchorPane.setBottomAnchor(instructionCodeArea, (double) 0);

    //set machine hex code area
    hexCodeArea = new CodeArea();
    hexCodeArea.richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(change -> {
          hexCodeArea.setStyleSpans(
              0, computeHexHighlighting(hexCodeArea.getText()));
        });
    hexCodeArea.getStylesheets().add(getClass().getResource("machine-area.css").toExternalForm());
    hexCodeArea.setEditable(false);

    hexTabArea.getChildren().add(hexCodeArea);
    AnchorPane.setTopAnchor(hexCodeArea, (double) 0);
    AnchorPane.setLeftAnchor(hexCodeArea, (double) 0);
    AnchorPane.setRightAnchor(hexCodeArea, (double) 0);
    AnchorPane.setBottomAnchor(hexCodeArea, (double) 0);
    
    //set machine decimal code area
    decCodeArea = new CodeArea();
    decCodeArea.richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(change -> {
          decCodeArea.setStyleSpans(
              0, computeInstructionHighlighting(decCodeArea.getText()));
        });
    decCodeArea.getStylesheets().add(getClass().getResource("machine-area.css").toExternalForm());
    decCodeArea.setEditable(false);

    decTabArea.getChildren().add(decCodeArea);
    AnchorPane.setTopAnchor(decCodeArea, (double) 0);
    AnchorPane.setLeftAnchor(decCodeArea, (double) 0);
    AnchorPane.setRightAnchor(decCodeArea, (double) 0);
    AnchorPane.setBottomAnchor(decCodeArea, (double) 0);

    //bind tab visibility to selected property of each CheckMenuItem
    toggleInstruction.selectedProperty().addListener((e, o, n) -> {
      if (n) tabPane.getTabs().add(1, instructionTab);
      else tabPane.getTabs().remove(instructionTab);
    });
    toggleMachineHex.selectedProperty().addListener((e, o, n) -> {
      if (n) tabPane.getTabs().add(Math.min(tabPane.getTabs().size(), 2), hexTab);
      else tabPane.getTabs().remove(hexTab);
    });
    toggleMachineDec.selectedProperty().addListener((e, o, n) -> {
      if (n) tabPane.getTabs().add(Math.min(tabPane.getTabs().size(), 3), decTab);
      else tabPane.getTabs().remove(decTab);
    });

    //handle close tab requests
    instructionTab.setOnCloseRequest(e -> {
      e.consume();
      toggleInstruction.setSelected(false);
    });
    hexTab.setOnCloseRequest(e -> {
      e.consume();
      toggleMachineHex.setSelected(false);
    });
    decTab.setOnCloseRequest(e -> {
      e.consume();
      toggleMachineDec.setSelected(false);
    });
  }

  private static StyleSpans<Collection<String>> computeCodeHighlighting(String text) {
    Matcher matcher = CODE_PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
          matcher.group("REGISTER") != null ? "number" :
          matcher.group("PAREN") != null ? "paren" :
          matcher.group("OPERATOR") != null ? "operator" :
          matcher.group("NUMBER") != null ? "number" :
          matcher.group("BRACE") != null ? "brace" :
          matcher.group("BRACKET") != null ? "bracket" :
          matcher.group("SEMICOLON") != null ? "semicolon" :
          matcher.group("STRING") != null ? "string" :
          matcher.group("COMMENT") != null ? "comment" :
          null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  private static StyleSpans<Collection<String>> computeInstructionHighlighting(String text) {
    Matcher matcher = INSTRUCTION_PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("OPERATOR") != null ? "operator" :
              matcher.group("MEMORY") != null ? "memory" :
              matcher.group("REGISTER") != null ? "register" :
              matcher.group("NUMBER") != null ? "number" :
                                              null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  private static StyleSpans<Collection<String>> computeHexHighlighting(String text) {
    Matcher matcher = HEX_PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
              matcher.group("HEXPREFIX") != null ? "hex-prefix" :
                  matcher.group("HEXNUMBER") != null ? "hex-number" :
                      null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public void writeToFile(File file) {
    System.out.println("Write file "+file.getName());
    try {
      PrintWriter printWriter = new PrintWriter(file.getAbsolutePath());
      ArrayList<String> rawCode = getCodeAsList();
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
    System.out.println("Load file " + file.getName());
    try {
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String line;
      ArrayList<String> text = new ArrayList<>();
      while ((line = bufferedReader.readLine()) != null) text.add(line);
      setCodeContent(text);
      setTitle(file.getName());
    } catch (FileNotFoundException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText("File not found!");
      alert.setContentText("Error loading " + file.getName());
      alert.showAndWait();
    } catch (IOException ignored) {
    }
  }

  public void reset() {
    codeArea.clear();
    codeArea.getUndoManager().forgetHistory();
    codeArea.getUndoManager().mark();
    setTitle("Untitled");
  }

  public void markSaved() {
    codeArea.getUndoManager().mark();
  }

  public void setTitle(String title) {
    codeTab.setText(title);
  }

  public void setCodeContent(ArrayList<String> codeContent) {
    StringBuilder sb = new StringBuilder();
    for (String s : codeContent) sb.append(s).append("\n");
    codeArea.replaceText(sb.toString());
  }

  private void setInstructionContent(ArrayList<Instruction> instructions) {
    StringBuilder sb = new StringBuilder();
    int len = instructions.size();
    int mx = (int) (Math.log10(len * 4)) + 1;
    for (int i = 0; i < len; ++i) {
      String num = "[" + (i <= 2 ? "0" + i * 4 : i * 4) + "]";
      String ins = instructions.get(i).toString() + "\n";
      for(int j = 0; j<mx-num.length()+2; ++j) sb.append(" ");
      sb.append(num);
      sb.append("  ");
      sb.append(ins);
    }
    instructionCodeArea.replaceText(sb.toString());
  }

  private void setMachineHexContent(ArrayList<String> hexContent) {
    StringBuilder sb = new StringBuilder();
    int len = hexContent.size();
    int mx = (int) (Math.log10(len * 4)) + 1;
    for (int i = 0; i < len; ++i) {
      String num = "[" + (i <= 2 ? "0" + i * 4 : i * 4) + "]";
      for(int j = 0; j<mx-num.length()+2; ++j) sb.append(" ");
      sb.append(num).append("  ").append(hexContent.get(i)).append("\n");
    }
    hexCodeArea.replaceText(sb.toString());
  }

  private void setMachineDecContent(ArrayList<Long> decContent) {
    StringBuilder sb = new StringBuilder();
    for(Long l : decContent) sb.append(l).append("\n");
    decCodeArea.replaceText(sb.toString());
  }

  public void compileToFile(File file) {
    try {
      ArrayList<Instruction> ins = Parser.compile(getCodeAsList());
      ArrayList<String> hex = Mapper.convertToHexString(ins);
      ArrayList<Long> machineCode = Mapper.convertToMachineCode(ins);
      setInstructionContent(ins);
      setMachineHexContent(hex);
      setMachineDecContent(machineCode);
      PrintWriter printWriter;
      try {
        printWriter = new PrintWriter(file.getAbsolutePath());
        for (Long l : machineCode) printWriter.println(l);
        printWriter.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      Alert alert = new Alert(Alert.AlertType.INFORMATION, "Compilation successful!");
      alert.setHeaderText(null);
      alert.setTitle("Compilation successful!");
      alert.showAndWait();
    } catch (ParserException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
      alert.setHeaderText(null);
      alert.setTitle("Compile error");
      alert.showAndWait();
    }
  }

  //Other getters
  public ObservableBooleanValue observableAtMarkedPosition() {
    return codeArea.getUndoManager().atMarkedPositionProperty();
  }
  public ArrayList<String> getCodeAsList() {
      return new ArrayList<>(Arrays.asList(codeArea.getText().split("\r?\n")));
  }

  //Menu getters
  public MenuItem getNewMenu() {
    return newMenu;
  }
  public MenuItem getOpenMenu() {
    return openMenu;
  }
  public MenuItem getSaveMenu() {
    return saveMenu;
  }
  public MenuItem getSaveAsMenu() {
    return saveAsMenu;
  }
  public MenuItem getCompileMenu() {
    return compileMenu;
  }
  public MenuItem getExitMenu() {
    return exitMenu;
  }
  public MenuItem getCutMenu() {
    return cutMenu;
  }
  public MenuItem getCopyMenu() {
    return copyMenu;
  }
  public MenuItem getPasteMenu() {
    return pasteMenu;
  }
  public MenuItem getDuplicateMenu() {
    return duplicateMenu;
  }
  public MenuItem getDeleteMenu() {
    return deleteMenu;
  }
  public MenuItem getRunMenu() {
    return runMenu;
  }
  public CheckMenuItem getToggleInstruction() {
    return toggleInstruction;
  }
  public CheckMenuItem getToggleMachineHex() {
    return toggleMachineHex;
  }
  public CheckMenuItem getToggleMachineDec() {
    return toggleMachineDec;
  }
  public MenuItem getHelpMenu() {
    return helpMenu;
  }
  public MenuItem getAboutMenu() {
    return aboutMenu;
  }


}
