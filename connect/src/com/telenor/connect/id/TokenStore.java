package com.telenor.connect.id;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TokenStore {

    private static final String PREFERENCE_KEY_CONNECT_TOKENS = "CONNECT_TOKENS";
    private static final String PREFERENCE_KEY_ID_TOKEN = "ID_TOKEN";
    private static final String PREFERENCES_FILE = "com.telenor.connect.PREFERENCES_FILE";
    private static final Gson preferencesGson =
            new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .registerTypeAdapter(Date.class, new DateDeserializer())
                    .create();

    private static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            String date = je.getAsString();

            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(date);
            } catch (ParseException e) {}

            try {
                return new SimpleDateFormat("MMM d, y h:mm:ss").parse(date);
            } catch (ParseException e) {}

            try {
                return new SimpleDateFormat("MMM d, y h:mm:ss", Locale.ENGLISH).parse(date);
            } catch (ParseException e) {}

            try {
                return DateFormat
                        .getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT).parse(date);
            } catch (ParseException e) {}

            try {
                return DateFormat.getDateTimeInstance(
                        DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.ENGLISH).parse(date);
            } catch (ParseException e) {}

            try {
                return DateFormat.getDateTimeInstance(
                        DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US).parse(date);
            } catch (ParseException e) {}

            throw new JsonParseException("Invalid date:" + date);
        }
    }

    private final Context context;

    public TokenStore(Context context) {
        this.context = context;
    }

    public void set(ConnectTokens connectTokens) {
        String jsonConnectTokens = preferencesGson.toJson(connectTokens);
        String jsonIdToken = preferencesGson.toJson(connectTokens.getIdToken());
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
