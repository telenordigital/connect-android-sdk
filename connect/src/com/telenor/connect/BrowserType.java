package com.telenor.connect;

public enum BrowserType {
    EXTERNAL_BROWSER("external-browser"),
    CHROME_CUSTOM_TAB("chrome-custom-tab");

    private String versionString;

    BrowserType(String versionString) {
        this.versionString = versionString;
    }

    public String getVersionString() {
        return versionString;
    }
}
