package com.telenor.connect.sms;

import android.webkit.JavascriptInterface;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.telenor.connect.ui.Instruction;
import com.telenor.connect.ui.InstructionHandler;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * This class works as the interface between the Javascript in a {@link InstructionHandler} and
 * the Connect SDK.
 *
 * The function {@code processInstructions} is a {@link @JavascriptInterface}, and is meant to
 * receive a String which hopefully is valid json of a list of {@link Instruction}. If not
 * an empty list is passed on to the {@link InstructionHandler}.
 */
public class HtmlToAndroidInstructionsInterface {

    private final InstructionHandler instructionHandler;

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private static final Type listOfInstructionsType =
            new TypeToken<List<Instruction>>() {}.getType();
    private static final List<Instruction> emptyList = Collections.emptyList();

    /**
     * @param instructionHandler the {@code InstructionHandler} that will be given a list of
     *                           all valid instructions or an empty list, after the
     *                           {@code @JavascriptInterface} {@code processInstructions} has
     *                           been called.
     */
    public HtmlToAndroidInstructionsInterface(InstructionHandler instructionHandler) {
        this.instructionHandler = instructionHandler;
    }

    /**
     * Interface between HTML and Java code of this SDK.
     * @param content Valid or invalid JSON of a list of {@link Instruction}s.
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void processInstructions(String content) {
        List<Instruction> instructions = getAllValidInstructions(content);
        instructionHandler.givenInstructions(instructions);
    }

    /**
     *
     * @param potentialInstructionJson either valid JSON String of a list of {@link Instruction}s,
     *                                 null, or invalid/broken JSON.
     * @return a parsed list of {@code List<Instruction>}s or an empty list.
     */
    public static List<Instruction> getAllValidInstructions(String potentialInstructionJson) {
        if (potentialInstructionJson == null || potentialInstructionJson.isEmpty()) {
            return emptyList;
        }

        List<Instruction> instructions;
        try {
            instructions = gson.fromJson(potentialInstructionJson, listOfInstructionsType);
        } catch (JsonSyntaxException e) {
            return emptyList;
        }

        return allValid(instructions) ? instructions : emptyList;
    }

    private static boolean allValid(List<Instruction> instructions) {
        for (Instruction i : instructions) {
            if (!i.isValid()) {
                return false;
            }
        }
        return true;
    }
}
