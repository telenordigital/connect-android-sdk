package com.telenor.connect.sms;

import com.telenor.connect.ui.Instruction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPinParseUtil {

    public static String findPin(String body, Instruction instruction) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        for (Object pattern: instruction.getArguments()) {
            Pattern patternToMatch = Pattern.compile((String) pattern);
            Matcher matcher = patternToMatch.matcher(body);
            if (!matcher.find()) {
                continue;
            }

            return matcher.group(1);
        }
        return null;
    }
}
