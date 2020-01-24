package com.wajahbesar.pembacameter.Database;

public class TableUpdate {
    private String Nopel;
    private String Type;
    private String Value;
    private String Status;

    TableUpdate() {}

    public TableUpdate(String Nopel, String Type, String Value, String Status) {
        this.Nopel = Nopel;
        this.Type = Type;
        this.Value = Value;
    }

    public String getNopel() {
        return Nopel;
    }

    public void setNopel(String Nopel) {
        this.Nopel = Nopel;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String Value) {
        this.Value = Value;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
}
