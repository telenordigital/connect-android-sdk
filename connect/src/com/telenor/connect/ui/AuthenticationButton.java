package com.telenor.connect.ui;

import com.telenor.connect.id.Claims;

import java.util.ArrayList;
import java.util.Map;

public interface AuthenticationButton {
    public ArrayList<String> getAcrValues();

    public Map<String, String> getLoginParameters();

    public ArrayList<String> getLoginScopeTokens();

    public int getRequestCode();

    public Claims getClaims();

    public int getCustomLoadingLayout();

    public void setAcrValues(String... acrValues);

    public void setAcrValues(ArrayList<String> acrValues);

    public void setLoginScopeTokens(String... scopeTokens);

    public void setLoginScopeTokens(ArrayList<String> scopeTokens);

    public void addLoginParameters(Map<String, String> parameters);

    public void setRequestCode(int requestCode);

    public void setClaims(Claims claims);

    public void setCustomLoadingLayout(int customLoadingLayout);
}
