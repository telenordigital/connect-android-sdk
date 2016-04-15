package com.telenor.connect.sms;

import com.telenor.connect.ui.Instruction;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SmsPinParseUtilTest {

    @Test
    public void findsPinInEnglishSms() {
        String body = "Your verification code for ''HipstaGram'' is 3456 - CONNECT by Telenor Digital.";

        Instruction.Config config = new Instruction.Config("", "Your verification code for ''{0}'' is {1} - CONNECT by Telenor Digital.", "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertEquals("3456", actual);

        assertThat(actual, is("3456"));
    }


    @Test
    public void findsPinInThaiSms() {
        String body = "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a ''HipstaGram'' \u0e04\u0e37\u0e2d 3456 - CONNECT \u0e42\u0e14\u0e22 Telenor Digital";
        // same as: "รหัสยืนยันของคุณสำหรับ ''HipstaGram'' คือ 3456 - CONNECT โดย Telenor Digital";

        Instruction.Config config = new Instruction.Config("",
                "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a ''{0}'' \u0e04\u0e37\u0e2d {1} - CONNECT \u0e42\u0e14\u0e22 Telenor Digital",
                "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void lookingForMissingPinInEnglishSmsReturnsNull() {
        String body = "Telenor are currently having connectivity issues. Expected fix time" +
                " is by 20:00 tonight.";

        Instruction.Config config = new Instruction.Config("",
                "Your verification code for ''{0}'' is {1} - CONNECT by Telenor Digital.",
                "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertNull(actual);
    }

    @Test
    public void lookingForMissingPinInThaiSmsReturnsNull() {
        String body = "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a \u0e04\u0e37\u0e2d  - CONNECT \u0e42\u0e14\u0e22 Telenor Digital";
        // same as: "รหัสยืนยันของคุณสำหรับ คือ  - CONNECT โดย Telenor Digital"

        Instruction.Config config = new Instruction.Config("",
                "\u0e23\u0e2b\u0e31\u0e2a\u0e22\u0e37\u0e19\u0e22\u0e31\u0e19\u0e02\u0e2d\u0e07\u0e04\u0e38\u0e13\u0e2a\u0e33\u0e2b\u0e23\u0e31\u0e1a ''{0}'' \u0e04\u0e37\u0e2d {1} - CONNECT \u0e42\u0e14\u0e22 Telenor Digital",
                "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertNull(actual);
    }

    @Test
    public void getRegexStringForValueKeyGivesRegexMatchOnValueKey() throws Exception {
        String actual = SmsPinParseUtil.getRegexStringForValueKey(
                "Your verification code for ''{0}'' is {1} - CONNECT by Telenor Digital.",
                "{1}");

        assertThat(
                actual,
                is("Your verification code for ''.*'' is (\\w*) - CONNECT by Telenor Digital."));
    }

    @Test
    public void findsPinInRightToLeftLanguages() {
        String body = "''Hipstagram'' \u06a9\u06d2 \u0644\u06cc\u06d2 \u0627\u0653\u067e \u06a9\u0627 \u062a\u0635\u062f\u06cc\u0642\u06cc \u06a9\u0648\u0688 3456 \u06c1\u06d2 - Telenor Digital \u06a9\u0627 \u067e\u06cc\u0634 \u06a9\u0631\u062f\u06c1 CONNECT\u06d4";
        // ''Hipstagram'' کے لیے آپ کا تصدیقی کوڈ 3456 ہے - Telenor Digital کا پیش کردہ CONNECT۔

        Instruction.Config config = new Instruction.Config("",
                "''{0}'' \u06a9\u06d2 \u0644\u06cc\u06d2 \u0627\u0653\u067e \u06a9\u0627 \u062a\u0635\u062f\u06cc\u0642\u06cc \u06a9\u0648\u0688 {1} \u06c1\u06d2 - Telenor Digital \u06a9\u0627 \u067e\u06cc\u0634 \u06a9\u0631\u062f\u06c1 CONNECT\u06d4",
                "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertThat(actual, is("3456"));
    }

    @Test
    public void lookingForMissingPinInRightToLeftLanguageReturnsNull() {
        String body = "\u0627\u0653\u067e \u0646\u06d2 \u063a\u0644\u0637 \u067e\u0627\u0633 \u0648\u0631\u0688 \u062f\u0631\u062c \u06a9\u06cc\u0627 \u06c1\u06d2\u06d4 \u0628\u0631\u0627\u06c1\u0650 \u0645\u06c1\u0631\u0628\u0627\u0646\u06cc \u062f\u0648\u0628\u0627\u0631\u06c1 \u06a9\u0648\u0634\u0634 \u06a9\u0631\u06cc\u06ba \u06cc\u0627 <a href=\"{0}\">\u0627\u067e\u0646\u0627 \u067e\u0627\u0633 \u0648\u0631\u0688 \u062f\u0648\u0628\u0627\u0631\u06c1 \u062a\u0631\u062a\u06cc\u0628 \u062f\u06cc\u06ba</a>\u06d4";

        Instruction.Config config = new Instruction.Config("",
                "''{0}'' \u06a9\u06d2 \u0644\u06cc\u06d2 \u0627\u0653\u067e \u06a9\u0627 \u062a\u0635\u062f\u06cc\u0642\u06cc \u06a9\u0648\u0688 {1} \u06c1\u06d2 - Telenor Digital \u06a9\u0627 \u067e\u06cc\u0634 \u06a9\u0631\u062f\u06c1 CONNECT\u06d4",
                "{1}");

        Instruction instruction = new Instruction();
        instruction.setConfig(config);

        String actual = SmsPinParseUtil.findPin(body, instruction);
        assertNull(actual);
    }
}
