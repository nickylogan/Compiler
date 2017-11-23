package main;

public enum Register implements Operand {
    R0, R1, R2, R3, R4, R5, R6, R7, R8,
    R9, R10, R11, R12, R13, R14, R15;
    public String getCode(){
        return Integer.toHexString(ordinal());
    }
    public static int isRegister(String value){
        return valueOf(value).ordinal();
    }
    public static Register getRegister(int id){
        return values()[id];
    }
}
