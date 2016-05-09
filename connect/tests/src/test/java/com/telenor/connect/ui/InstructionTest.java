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
    public void wrongNameReturnsFalseIsValidPinInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("something not $getPinFromSms");

        assertFalse(instruction.isValidPinInstruction());
    }
}
