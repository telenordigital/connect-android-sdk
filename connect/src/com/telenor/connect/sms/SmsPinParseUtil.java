package com.telenor.connect.sms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPinParseUtil {

    private static final String TEMPLATE_TO_MATCH
            = "(?: (\\d+) .*(?:Connect|CONNECT).*)|(?:.*(?:Connect|CONNECT).* (\\d+) )"
            + "|(?:.*(?:Connect|CONNECT).* (\\d+)$)|(?:^(\\d+) .*(?:Connect|CONNECT).*)";

    public static String findPin(String body) {
        Pattern patternToMatch = Pattern.compile(TEMPLATE_TO_MATCH);
        Matcher matcher = patternToMatch.matcher(body);
        if (!matcher.find()) {
            return null;
        }

        for (int i = 0; i < matcher.groupCount(); i++) {
            String group = matcher.group(i);
            if (isNumber(group)) {
                return group;
            }
        }

        throw new RuntimeException("Found match, but then couldn't find it.");
    }

    private static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
