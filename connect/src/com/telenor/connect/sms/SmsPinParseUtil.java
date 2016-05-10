package com.telenor.connect.sms;

import com.telenor.connect.ui.Instruction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPinParseUtil {

    private static final String MUST_CONTAIN = "CONNECT";
    // For security reasons all SMS that are going to be checked for PIN codes needs to be
    // checked for the keyword `CONNECT`. Otherwise a malicious person might use a regex that
    // grabs the entire sms, from all senders.

    public static String findPin(String body, Instruction instruction) {
        if (body == null || body.isEmpty() || !body.contains(MUST_CONTAIN)) {
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
