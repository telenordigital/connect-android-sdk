package com.telenor.connect.ui;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class InstructionTest {

    @Test
    public void isValidJavaScriptRequiresName() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("abc");

        assertTrue(instruction.isValidJavaScript());
    }

    @Test
    public void missingInstructionNameIsValidReturnsFalse() throws Exception {
        Instruction instruction = new Instruction();

        assertFalse(instruction.isValidJavaScript());
    }

    @Test
    public void isValidPinInstructionRequiresCorrectNameAndNonNullConfig() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("$getPinFromSms");
        Instruction.Config config = new Instruction.Config("not null", "not null", "not null");
        instruction.setConfig(config);

        assertTrue(instruction.isValidPinInstruction());
    }

    @Test
    public void wrongNameReturnsFalseIsValidPinInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("something not $getPinFromSms");

        assertFalse(instruction.isValidPinInstruction());
    }

    @Test
    public void cantHaveConfigIfNotPinInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("something not $getPinFromSms");
        instruction.setConfig(new Instruction.Config());

        assertFalse(instruction.isValidPinInstruction());
        assertFalse(instruction.isValidJavaScript());
        assertFalse(instruction.isValid());
    }

    @Test
    public void mustHaveFullConfigIfPinInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("$getPinFromSms");
        Instruction.Config config = new Instruction.Config();
        config.setSender("");
        config.setTemplate("");
        config.setValueKey("");
        instruction.setConfig(config);

        assertFalse(instruction.isValidJavaScript());
        assertTrue(instruction.isValidPinInstruction());
        assertTrue(instruction.isValid());
    }

    public static class ConfigTest {

        @Test
        public void validConfigHasAllNonNullFields() throws Exception {
            Instruction.Config config1 = new Instruction.Config("", "", "");
            Instruction.Config config2 = new Instruction.Config("Telenor",
                    "Your verification code for ''{0}'' is {1} - CONNECT by Telenor Digital.",
                    "{1}");

            assertTrue(config1.isValid());
            assertTrue(config2.isValid());
        }

        @Test
        public void nullConfigFieldsGiveFalseIsValid() throws Exception {
            Instruction.Config config1 = new Instruction.Config(null, "", "");
            Instruction.Config config2 = new Instruction.Config("", null, "");
            Instruction.Config config3 = new Instruction.Config("", "", null);
            Instruction.Config config4 = new Instruction.Config(null, null, "");
            Instruction.Config config5 = new Instruction.Config(null, "", null);
            Instruction.Config config6 = new Instruction.Config("", null, null);
            Instruction.Config config7 = new Instruction.Config(null, null, null);

            assertFalse(config1.isValid());
            assertFalse(config2.isValid());
            assertFalse(config3.isValid());
            assertFalse(config4.isValid());
            assertFalse(config5.isValid());
            assertFalse(config6.isValid());
            assertFalse(config7.isValid());
        }

    }
}
