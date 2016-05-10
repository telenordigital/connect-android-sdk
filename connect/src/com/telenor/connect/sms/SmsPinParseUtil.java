package com.telenor.connect.sms;

import com.telenor.connect.ui.Instruction;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPinParseUtil {

    private static final String MUST_CONTAIN = "CONNECT";

    public static String findPin(String body, Instruction instruction) {
        if (body == null || body.isEmpty() || !body.contains(MUST_CONTAIN)) {
            return null;
        }

        List<Object> patterns = instruction.getArguments();

        for (Object pattern: patterns) {
            if (!(pattern instanceof String)) {
                continue;
            }
            String p = (String) pattern;

            Pattern patternToMatch = Pattern.compile(p);
            Matcher matcher = patternToMatch.matcher(body);
            if (!matcher.find()) {
                continue;
            }

            return matcher.group(1);
        }

        return null;
    }
}
