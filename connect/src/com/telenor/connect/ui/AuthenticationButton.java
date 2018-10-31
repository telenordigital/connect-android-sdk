package com.telenor.connect.ui;

import com.telenor.connect.id.Claims;

import java.util.ArrayList;
import java.util.Map;

public interface AuthenticationButton {
    ArrayList<String> getAcrValues();

    Map<String, String> getLoginParameters();

    ArrayList<String> getLoginScopeTokens();

    int getRequestCode();

    Claims getClaims();

    int getCustomLoadingLayout();

    void setAcrValues(String... acrValues);

    void setAcrValues(ArrayList<String> acrValues);

    void setLoginScopeTokens(String... scopeTokens);

    void setLoginScopeTokens(ArrayList<String> scopeTokens);

    void addLoginParameters(Map<String, String> parameters);

    void setRequestCode(int requestCode);

    void setClaims(Claims claims);

    void setCustomLoadingLayout(int customLoadingLayout);
}
