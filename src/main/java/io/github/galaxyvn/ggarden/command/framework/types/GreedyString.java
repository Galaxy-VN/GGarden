package io.github.galaxyvn.ggarden.command.framework.types;

public class GreedyString {

    private final String value;

    public GreedyString(String value) {
        this.value = value;
    }

    /**
     * @return the String value
     */
    public String get() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
