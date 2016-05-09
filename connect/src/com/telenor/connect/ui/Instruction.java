package com.telenor.connect.ui;

import java.util.List;

public class Instruction {

    public static final String PIN_INSTRUCTION_NAME = "androidSystemCall_getPinFromSms";

    private String name;
    private List<Object> arguments;
    private String pinCallbackName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public String getPinCallbackName() {
        return pinCallbackName;
    }

    public void setPinCallbackName(String pinCallbackName) {
        this.pinCallbackName = pinCallbackName;
    }

    public boolean isValid() {
        return isValidJavaScript() || isValidPinInstruction();
    }

    public boolean isValidJavaScript() {
        return name != null && !name.equals(Instruction.PIN_INSTRUCTION_NAME);
    }

    public boolean isValidPinInstruction() {
        return name != null && name.equals(Instruction.PIN_INSTRUCTION_NAME);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instruction that = (Instruction) o;

        if (!name.equals(that.name)) return false;
        if (arguments != null ? !arguments.equals(that.arguments) : that.arguments != null)
            return false;
        return pinCallbackName != null ? pinCallbackName.equals(that.pinCallbackName) : that.pinCallbackName == null;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        result = 31 * result + (pinCallbackName != null ? pinCallbackName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                ", pinCallbackName='" + pinCallbackName + '\'' +
                '}';
    }

}
