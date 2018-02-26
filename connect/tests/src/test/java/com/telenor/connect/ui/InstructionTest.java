package com.telenor.connect.ui;

import org.junit.Test;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@Config(sdk = 18)
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
    public void wrongNameReturnsFalseIsValidPinInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("something not $getPinFromSms");

        assertFalse(instruction.isValidPinInstruction());
    }
}
