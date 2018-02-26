package com.telenor.connect.utils;

import android.test.AndroidTestCase;

import com.telenor.connect.ui.Instruction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class JavascriptUtilTest extends AndroidTestCase {

    @Test
    public void nameAndArgumentGivesJavascriptFunctionCallString() throws Exception {
        String javascriptString = JavascriptUtil.getJavascriptString("foo", "bar");

        assertThat(javascriptString, is("window[\"foo\"](bar);"));
    }

    @Test
    public void getJavascriptStringHandlesInstruction() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("foo");
        List<Object> list = new ArrayList<>();
        list.add("bar");
        instruction.setArguments(list);

        String javascriptString = JavascriptUtil.getJavascriptString(instruction);

        assertThat(javascriptString, is("window[\"foo\"](bar);"));
    }

    @Test
    public void nullArgumentsGivesZeroJSArguments() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("foo");
        instruction.setArguments(null);

        String javascriptString = JavascriptUtil.getJavascriptString(instruction);

        assertThat(javascriptString, is("window[\"foo\"]();"));
    }

    @Test
    public void differentTypeArgumentsInstructionWorks() throws Exception {
        Instruction instruction = new Instruction();
        instruction.setName("foo");
        ArrayList<Object> list = new ArrayList<>();
        list.add("bar");
        list.add("'foo'");
        list.add("\"magic\"");
        list.add(1);
        list.add(null);
        instruction.setArguments(list);

        String javascriptString = JavascriptUtil.getJavascriptString(instruction);

        assertEquals("window[\"foo\"](bar, 'foo', \"magic\", 1, null);", javascriptString);
    }
}
