package com.telenor.connect.id;

import android.content.Context;

import com.google.gson.Gson;

public class TokenStore {

    private static final String PREFERENCE_KEY_CONNECT_TOKENS = "CONNECT_TOKENS";
    private static final String PREFERENCE_KEY_ID_TOKEN = "ID_TOKEN";
    private static final String PREFERENCES_FILE = "com.telenor.connect.PREFERENCES_FILE";
    private static final Gson preferencesGson = new Gson();

    private final Context context;

    public TokenStore(Context context) {
        this.context = context;
    }

    public void set(ConnectTokens connectTokens) {
        String jsonConnectTokens = preferencesGson.toJson(connectTokens);
        String jsonIdToken = preferencesGson.toJson(connectTokens.idToken);
        context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_CONNECT_TOKENS, jsonConnectTokens)
                .putString(PREFERENCE_KEY_ID_TOKEN, jsonIdToken)
                .apply();
    }

    public void update(ConnectTokens connectTokens) {
        String jsonConnectTokens = preferencesGson.toJson(connectTokens);
        context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .putString(PREFERENCE_KEY_CONNECT_TOKENS, jsonConnectTokens)
                .apply();
    }

    public ConnectTokens get() {
        String connectTokensJson = context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString(PREFERENCE_KEY_CONNECT_TOKENS, null);

        return preferencesGson.fromJson(connectTokensJson, ConnectTokens.class);
    }

    public void clear() {
        context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public IdToken getIdToken() {
        String idTokenJson = context
                .getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
                .getString(PREFERENCE_KEY_ID_TOKEN, null);
        return preferencesGson.fromJson(idTokenJson, IdToken.class);
    }
}
