package com.telenor.connect.ui;

import java.util.List;

public class Instruction {

    public static final String PIN_INSTRUCTION_NAME = "$getPinFromSms";

    private String name;
    private List<Object> arguments;
    private Config config;
    private String pinCallbackName;
    private int timeout;

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

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getPinCallbackName() {
        return pinCallbackName;
    }

    public void setPinCallbackName(String pinCallbackName) {
        this.pinCallbackName = pinCallbackName;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isValid() {
        return isValidJavaScript() || isValidPinInstruction();
    }

    public boolean isValidJavaScript() {
        return name != null && config == null;
    }

    public boolean isValidPinInstruction() {
        return name.equals(Instruction.PIN_INSTRUCTION_NAME) &&
                config.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instruction that = (Instruction) o;

        if (timeout != that.timeout) return false;
        if (!name.equals(that.name)) return false;
        if (arguments != null ? !arguments.equals(that.arguments) : that.arguments != null)
            return false;
        if (config != null ? !config.equals(that.config) : that.config != null) return false;
        return pinCallbackName != null ? pinCallbackName.equals(that.pinCallbackName) : that.pinCallbackName == null;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        result = 31 * result + (config != null ? config.hashCode() : 0);
        result = 31 * result + (pinCallbackName != null ? pinCallbackName.hashCode() : 0);
        result = 31 * result + timeout;
        return result;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                ", config=" + config +
                ", pinCallbackName='" + pinCallbackName + '\'' +
                ", timeout=" + timeout +
                '}';
    }

    public static class Config {

        private String sender;
        private String template;
        private String valueKey;

        public Config() {
        }

        public Config(String sender, String template, String valueKey) {
            this.sender = sender;
            this.template = template;
            this.valueKey = valueKey;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public void setValueKey(String valueKey) {
            this.valueKey = valueKey;
        }

        public String getSender() {
            return sender;
        }

        public String getTemplate() {
            return template;
        }

        public String getValueKey() {
            return valueKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Config config = (Config) o;

            if (sender != null ? !sender.equals(config.sender) : config.sender != null)
                return false;
            if (template != null ? !template.equals(config.template) : config.template != null)
                return false;
            return valueKey != null ? valueKey.equals(config.valueKey) : config.valueKey == null;

        }

        @Override
        public int hashCode() {
            int result = sender != null ? sender.hashCode() : 0;
            result = 31 * result + (template != null ? template.hashCode() : 0);
            result = 31 * result + (valueKey != null ? valueKey.hashCode() : 0);
            return result;
        }

        public boolean isValid() {
            return sender != null && template != null && valueKey != null;
        }
    }
}
