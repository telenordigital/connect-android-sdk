package com.telenor.connect.id;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class IdTokenDeserializer implements JsonDeserializer<IdToken> {
    @Override
    public IdToken deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String jsonString = json.getAsString();
        return new IdToken(jsonString);
    }
}
