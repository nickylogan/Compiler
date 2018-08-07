package GUI.debugger;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class SymbolTableCellFactory<S, T> implements Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
  @Override
  public TreeTableCell<S, T> call(TreeTableColumn<S, T> param) {
    return new TreeTableCell<S, T>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        String rowChanged = "row-changed";

        ObservableSymbol observableSymbol = null;
        if(getTreeTableRow() != null) {
          observableSymbol = (ObservableSymbol) this.getTreeTableRow().getItem();
          getTreeTableRow().getStyleClass().remove(rowChanged);
        }

        super.updateItem(item, empty);

        if(observableSymbol != null && getTreeTableRow() != null) {
          if(observableSymbol.isChanged()) {
            getTreeTableRow().getStyleClass().add(rowChanged);
          }
        }

        if(item != null) setText(item.toString());
        else setText("");
      }
    };
  }
}
