package fr.bmartel.bluetooth.hcidebugger.model;

/**
 * @author Bertrand Martel
 */
public class ValuePair {

    private int code = -1;
    private String value = "";

    public ValuePair(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

}
