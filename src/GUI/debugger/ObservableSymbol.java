package GUI.debugger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ObservableSymbol {
  private SimpleStringProperty name;
  private SimpleStringProperty scopeID;
  private SimpleStringProperty type;
  private SimpleIntegerProperty size;
  private SimpleIntegerProperty location;
  private SimpleIntegerProperty value;
  private byte[] byteRepresentation;
  private SimpleBooleanProperty changed;

  ObservableSymbol(String name, String scopeID, String type, Integer size, Integer location) {
    this.name = new SimpleStringProperty(name);
    this.scopeID = new SimpleStringProperty(scopeID);
    this.type = new SimpleStringProperty(type);
    this.size = new SimpleIntegerProperty(size);
    this.location = new SimpleIntegerProperty(location);
    this.value = new SimpleIntegerProperty(0);
    this.byteRepresentation = new byte[4];
    this.changed = new SimpleBooleanProperty(false);
  }


  public String getName() {
    return name.get();
  }

  public SimpleStringProperty nameProperty() {
    return name;
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public String getScopeID() {
    return scopeID.get();
  }

  public SimpleStringProperty scopeIDProperty() {
    return scopeID;
  }

  public void setScopeID(String scopeID) {
    this.scopeID.set(scopeID);
  }

  public String getType() {
    return type.get();
  }

  public SimpleStringProperty typeProperty() {
    return type;
  }

  public void setType(String type) {
    this.type.set(type);
  }

  public Integer getSize() {
    return size.get();
  }

  public SimpleIntegerProperty sizeProperty() {
    return size;
  }

  public void setSize(int size) {
    this.size.set(size);
  }

  public Integer getLocation() {
    return location.get();
  }

  public SimpleIntegerProperty locationProperty() {
    return location;
  }

  public void setLocation(Integer location) {
    this.location.set(location);
  }

  public Integer getValue() {
    return value.get();
  }

  public SimpleIntegerProperty valueProperty() {
    return value;
  }

  private void setValue(int value) {
    if(value == this.value.get())
      setChanged(false);
    else
      setChanged(true);
    this.value.set(value);
  }

  public byte[] getBytes() {
    return byteRepresentation;
  }

  public void setBytes(byte[] bytes) {
    int res = 0;
    for (int j = 3; j >= 0; --j) {
//      System.out.println(bytes[j]);
      res = (res << 8) + (bytes[j] & 0xFF);
    }
    setValue(res);
    byteRepresentation = bytes.clone();
  }

  public boolean isChanged() {
    return changed.get();
  }

  public SimpleBooleanProperty changedProperty() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed.set(changed);
  }
}
