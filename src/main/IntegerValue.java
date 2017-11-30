package main;

public class IntegerValue {
    private Integer value;
    public IntegerValue(Integer value){
        setValue(value);
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
