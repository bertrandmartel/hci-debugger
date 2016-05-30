package com.github.akinaru.hcidebugger.model;

/**
 * Created by akinaru on 29/05/16.
 */
public enum ScanType {

    CLASSIC_SCAN("classic_scan"),
    BLE_SCAN("ble_scan");

    private String mValue;

    ScanType(String value) {
        this.mValue = value;
    }

    public static ScanType getScanType(String value) {

        if (value != null) {
            for (ScanType action : ScanType.values()) {
                if (value.equals(action.mValue))
                    return action;
            }
        }
        return CLASSIC_SCAN;
    }

    public String getValue() {
        return mValue;
    }
}
