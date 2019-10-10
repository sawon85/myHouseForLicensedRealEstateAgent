package com.example.myhouseforlicensedrealestateagent;


public class AppManager {

    private static AppManager instance = null;

    private AppManager() {
    }

    public static AppManager getInstance() {
        if (instance == null)
            instance = new AppManager();
        return instance;
    }

    String excelPath = null;
    String location = null;

    public String getExcelPath() {return excelPath; }

    public void setExcelPath(String path){ this.excelPath = path; }

    public String getLocation() {return location;}
    public void setLocation(String location) { this.location = location ;}

}



