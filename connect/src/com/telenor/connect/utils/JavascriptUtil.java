package com.telenor.connect.utils;

import android.text.TextUtils;

import com.telenor.connect.ui.Instruction;

import java.util.List;

public class JavascriptUtil {

    public static String getJavascriptString(Instruction instruction) {
        List<Object> instructionArguments = instruction.getArguments();
        String arguments = instructionArguments != null ?
                TextUtils.join(", ", instructionArguments) :
                "";
        return getJavascriptString(instruction.getName(), arguments);
    }

    public static String getJavascriptString(String function, String argument) {
        return "window[\"" + function + "\"](" + argument + ");";
    }
}
