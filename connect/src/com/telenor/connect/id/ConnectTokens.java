package com.telenor.connect.id;

import androidx.annotation.Nullable;

import com.telenor.connect.ConnectException;
import com.telenor.connect.utils.Validator;

import java.util.Calendar;
import java.util.Date;

public class ConnectTokens {

    private final String accessToken;
    private final Date expirationDate;
    private final IdToken idToken;
    private final String refreshToken;
    private final String scope;
    private final String tokenType;

    public ConnectTokens(ConnectTokensTO connectTokensTO, @Nullable Date serverTimestamp) throws ConnectException {
        Validator.validateTokens(connectTokensTO, serverTimestamp);

        accessToken = connectTokensTO.getAccessToken();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, (int) connectTokensTO.getExpiresIn());
        expirationDate = instance.getTime();
        idToken = connectTokensTO.getIdToken();
        refreshToken = connectTokensTO.getRefreshToken();
        scope = connectTokensTO.getScope();
        tokenType = connectTokensTO.getTokenType();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public IdToken getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public boolean accessTokenHasExpired() {
        return Calendar.getInstance().getTime().after(expirationDate);
    }

}
