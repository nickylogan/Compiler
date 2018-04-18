package GUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BuildMessageController extends AnchorPane implements Initializable {
    @FXML
    private ListView<FlowPane> msg;

    @FXML
    private ScrollPane s1;

    @FXML
    private FlowPane bundle;

    private ObservableList<FlowPane> msgList = FXCollections.observableArrayList();

    public BuildMessageController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BuildMessage.fxml"));
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
        msg.setItems(msgList);
    }

    public void setBuildMessage (ArrayList<String> buildMessage) {
//        System.out.println(assemblyCode);
        int i = 0;
        for (String aBuildMessage : buildMessage) {
            ArrayList<Text> arr = new ArrayList<>();
            Text ad = new Text("["+(i*4/10)+(i*4%10)+"] " );
            Text t = new Text(aBuildMessage);
            ad.setFill(Color.rgb(210,148,93));
            ad.setFont(Font.font("Courier New", 14.0));
            t.setFill(Color.WHITE);
            t.setFont(Font.font("Courier New", 14.0));
            arr.add(ad);
            arr.add(t);
            bundle = new FlowPane();
            bundle.getChildren().addAll(arr);
            msgList.add(bundle);
            ++i;
        }
    }

}
