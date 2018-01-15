package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainWindowController extends BorderPane implements Initializable {
    @FXML private MenuItem newMenu;
    @FXML private MenuItem openMenu;
    @FXML private MenuItem saveMenu;
    @FXML private MenuItem saveAsMenu;
    @FXML private MenuItem exitMenu;
    @FXML private MenuItem cutMenu;
    @FXML private MenuItem copyMenu;
    @FXML private MenuItem pasteMenu;
    @FXML private MenuItem duplicateMenu;
    @FXML private MenuItem deleteMenu;
    @FXML private MenuItem compileMenu;
    @FXML private MenuItem runMenu;
    @FXML private MenuItem toggleInstruction;
    @FXML private MenuItem toggleMachineHex;
    @FXML private MenuItem toggleMachineDec;
    @FXML private MenuItem helpMenu;
    @FXML private MenuItem aboutMenu;
    @FXML private Tab codeTab;
    @FXML private Tab instructionTab;
    @FXML private Tab hexTab;
    @FXML private Tab decTab;
    @FXML private AnchorPane codeTabArea;
    @FXML private AnchorPane instructionTabArea;
    @FXML private AnchorPane hexTabArea;
    @FXML private AnchorPane decTabArea;

    private CodeArea codeArea;
    private static final String[] KEYWORDS = new String[] {
            "if", "endif", "break", "while", "endwhile", "else"
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

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    public MainWindowController(){
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

        //set areas inside of tab pane
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });

        codeArea.getStylesheets().add(getClass().getResource("code-area.css").toExternalForm());

        codeTabArea.getChildren().add(codeArea);
        AnchorPane.setTopAnchor(codeArea, (double) 0);
        AnchorPane.setLeftAnchor(codeArea, (double) 0);
        AnchorPane.setRightAnchor(codeArea, (double) 0);
        AnchorPane.setBottomAnchor(codeArea, (double) 0);
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
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
}
