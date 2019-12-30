package com.wajahbesar.pembacameter.Database;

public class TableSetting {
    private String URLAPI;

    TableSetting() {}

    public TableSetting(String URLAPI) {
        this.URLAPI = URLAPI;
    }

    public String getURLAPI() {
        return URLAPI;
    }

    void setURLAPI(String URLAPI) {
        this.URLAPI = URLAPI;
    }
}
