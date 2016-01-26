package com.telenor.connect.sms;

import com.telenor.connect.ui.Instruction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPinParseUtil {

    public static String findPin(String body, Instruction instruction) {
        Instruction.Config config = instruction.getConfig();
        String templateToMatch
                = getRegexStringForValueKey(config.getTemplate(), config.getValueKey());

        Pattern pattern = Pattern.compile(templateToMatch);
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * This function will turn a {@code template} and {@code valueKey} into a regex string
     * that matches on the valueKey.
     *
     * An example
     * {@code
     * getRegexStringForValueKey("Your verification code for ''{0}'' +
     * is {1} - CONNECT by Telenor Digital.", "{1}");
     * }
     * will make a new string with the following transformation:
     * "Your verification code for ''{0}'' is {1} - CONNECT by Telenor Digital." ->
     * "Your verification code for ''{0}'' is (\w*) - CONNECT by Telenor Digital." ->
     * "Your verification code for ''.*'' is (\w*) - CONNECT by Telenor Digital."
     *
     * @param template the template to turn into a regex string
     * @param valueKey the key inside the {@code template} in which the value to extract
     *                 is located.
     * @return a regex string which will match on the {@code valueKey} inside the {@code template}.
     */
    public static String getRegexStringForValueKey(String template, String valueKey) {
        return template
                .replace(valueKey, "(\\w*)")
                .replaceAll("\\{\\d\\}", ".*");
    }
}
