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
}
