package com.telenor.connect.sms;

import android.support.annotation.NonNull;

import com.telenor.connect.ui.Instruction;
import com.telenor.connect.ui.InstructionHandler;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Config(sdk = 18)
public class HtmlToAndroidInstructionsInterfaceTest {

    private final static String validInstructions = "[  \n" +
            "  {  \n" +
            "    \"name\":\"eval\",\n" +
            "    \"arguments\":[  \n" +
            "      \"\\\"$('#pin').attr('placeholder', 'Waiting for PIN on SMS...');\\\"\"\n" +
            "    ]\n" +
            "  },\n" +
            "  {  \n" +
            "    \"name\":\"eval\",\n" +
            "    \"arguments\":[  \n" +
            "      \"\\\"window['handlePinReceived'] = function(pin) { $('#pin').val(pin); }\\\"\"\n" +
            "    ]\n" +
            "  },\n" +
            "  {  \n" +
            "    \"name\":\"androidSystemCall_getPinFromSms\",\n" +
            "    \"pin_callback_name\":\"handlePinReceived\",\n" +
            "    \"arguments\":[  \n" +
            "      \".* ([0-9]{4}).*\",\n" +
            "      \".*([0-9]{4}) .*\"\n" +
            "    ]\n" +
            "  }\n" +
            "]";

    private static final String brokenJson = "{ bork bork";

    private static final String emptyArgumentsListJs = "[" +
            "  {" +
            "    \"name\": \"foo\"," +
            "    \"arguments\": []" +
            "  }" +
            "]";

    private static final String missingArgumentsListJs = "[" +
            "  {" +
            "    \"name\": \"foo\"," +
            "  }" +
            "]";

    private static final String missingDoubleQuotesOnVarsJson = "[\n" +
            "   {\n" +
            "      name:\"androidJsCall_waitForSmsWithTimeout\"\n" +
            "   },\n" +
            "   {\n" +
            "      name:\"androidJsCall_handlePinReceived\"\n" +
            "   },\n" +
            "   {\n" +
            "      name:\"androidSystemCall_getPinFromSms\",\n" +
            "      pin_callback_name:\"androidJsCall_handlePinReceived\",\n" +
            "      arguments: [\"some arg\"]\n" +
            "   }\n" +
            "]";

    @Test
    public void instructionsArePassedToInstructionHandler() throws Exception {
        InstructionHandler instructionHandler = mock(InstructionHandler.class);

        HtmlToAndroidInstructionsInterface androidInterface
                = new HtmlToAndroidInstructionsInterface(instructionHandler);

        androidInterface.processInstructions(validInstructions);

        verify(instructionHandler).givenInstructions(anyList());
    }

    // http://stackoverflow.com/a/4201631/2148380
    @Test
    public void missingDoubleQuotesOnVarsJsonWorksAsNormal() throws Exception {
        InstructionHandler instructionHandler = mock(InstructionHandler.class);

        HtmlToAndroidInstructionsInterface androidInterface
                = new HtmlToAndroidInstructionsInterface(instructionHandler);

        androidInterface.processInstructions(missingDoubleQuotesOnVarsJson);

        verify(instructionHandler).givenInstructions(anyList());
    }

    @Test
    public void brokenJsonDoesNotCallInstructionHandler() throws Exception {
        InstructionHandler instructionHandler = mock(InstructionHandler.class);

        HtmlToAndroidInstructionsInterface androidInterface =
                new HtmlToAndroidInstructionsInterface(instructionHandler);

        androidInterface.processInstructions(brokenJson);

        verify(instructionHandler, never()).givenInstructions(anyList());
    }

    @Test
    public void emptyInstructionsDoesNotCallInstructionHandler() throws Exception {
        InstructionHandler instructionHandler = mock(InstructionHandler.class);

        HtmlToAndroidInstructionsInterface androidInterface =
                new HtmlToAndroidInstructionsInterface(instructionHandler);

        androidInterface.processInstructions("");
        verify(instructionHandler, never()).givenInstructions(anyList());
    }

    @Test
    public void emptyThenFullInstructionsShouldCallInstructionHandlerOnce() throws Exception {
        InstructionHandler instructionHandler = mock(InstructionHandler.class);

        HtmlToAndroidInstructionsInterface androidInterface =
                new HtmlToAndroidInstructionsInterface(instructionHandler);

        androidInterface.processInstructions("");
        androidInterface.processInstructions(validInstructions);
        verify(instructionHandler, times(1)).givenInstructions(anyList());
    }

    @Test
    public void validJsonEqualsExpectedInstructions() throws Exception {
        List<Instruction> actual
                = HtmlToAndroidInstructionsInterface.getAllValidInstructions(validInstructions);
        List<Instruction> expected = getExpectedInstructions();

        Assert.assertEquals(expected, actual);
    }

    @NonNull
    private static ArrayList<Instruction> getExpectedInstructions() {
        ArrayList<Instruction> instructions = new ArrayList<>();

        Instruction showLoading = new Instruction();
        showLoading.setName("eval");
        ArrayList<Object> showLoadingArguments = new ArrayList<>();
        showLoadingArguments.add("\"$('#pin').attr('placeholder', 'Waiting for PIN on SMS...');\"");
        showLoading.setArguments(showLoadingArguments);
        instructions.add(showLoading);

        Instruction addCallbackMethod = new Instruction();
        addCallbackMethod.setName("eval");
        ArrayList<Object> addCallbackMethodArguments = new ArrayList<>();
        addCallbackMethodArguments.add("\"window['handlePinReceived'] = function(pin) " +
                "{ $('#pin').val(pin); }\"");
        addCallbackMethod.setArguments(addCallbackMethodArguments);
        instructions.add(addCallbackMethod);

        Instruction getPinFromSms = new Instruction();
        getPinFromSms.setName("androidSystemCall_getPinFromSms");
        getPinFromSms.setPinCallbackName("handlePinReceived");
        List<Object> list = new ArrayList<>();
        list.add(".* ([0-9]{4}).*");
        list.add(".*([0-9]{4}) .*");
        getPinFromSms.setArguments(list);
        instructions.add(getPinFromSms);

        return instructions;
    }

    @Test
    public void emptyJsonGivesEmptyList() throws Exception {
        List<Instruction> actual = HtmlToAndroidInstructionsInterface.getAllValidInstructions("");
        List<Instruction> expected = Collections.emptyList();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void brokenJsonGivesEmptyList() throws Exception {
        List<Instruction> actual = HtmlToAndroidInstructionsInterface.getAllValidInstructions(brokenJson);
        List<Instruction> expected = Collections.emptyList();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void emptyJsonArgumentsArrayProducesEmptyArgumentsInstruction() throws Exception {
        List<Instruction> actual = HtmlToAndroidInstructionsInterface.getAllValidInstructions(emptyArgumentsListJs);

        ArrayList<Instruction> expected = new ArrayList<>();
        Instruction instruction = new Instruction();
        instruction.setName("foo");
        instruction.setArguments(Collections.emptyList());
        expected.add(instruction);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void missingJsonArgumentsListGivesNullArgumentsInstruction() throws Exception {
        List<Instruction> actual = HtmlToAndroidInstructionsInterface.getAllValidInstructions(missingArgumentsListJs);

        ArrayList<Instruction> instructions = new ArrayList<>();
        Instruction instruction = new Instruction();
        instruction.setName("foo");
        instruction.setArguments(null);
        List<Instruction> expected = instructions;

        Assert.assertEquals(expected, actual);
    }
}
