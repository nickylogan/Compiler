package GUI;

import com.sun.tools.javac.comp.Todo;
import com.sun.xml.internal.bind.v2.TODO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CodeDebuggerController extends BorderPane implements Initializable {
    @FXML
    private AnchorPane aaa;

    @FXML
    private Button next;

    public CodeDebuggerController () {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CodeDebugger.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize (URL location, ResourceBundle resources) {
        next.setOnAction( e -> {
            //TODO make the 'next' button change to the next line of code
        });
    }
}
