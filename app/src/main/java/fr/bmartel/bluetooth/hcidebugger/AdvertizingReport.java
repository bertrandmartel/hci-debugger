package fr.bmartel.bluetooth.hcidebugger;

import org.json.JSONArray;

/**
 * @author Bertrand Martel
 */
public class AdvertizingReport {

    private String address = "";
    private int addressType = -1;
    private JSONArray data;
    private int dataLength = 0;
    private int eventType = 0;
    private int rssi = 0;

    public AdvertizingReport(String address, int addressType, JSONArray data, int dataLength, int eventType, int rssi) {
        this.address = address;
        this.addressType = addressType;
        this.data = data;
        this.dataLength = dataLength;
        this.eventType = eventType;
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public int getAddressType() {
        return addressType;
    }

    public JSONArray getData() {
        return data;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getEventType() {
        return eventType;
    }

    public int getRssi() {
        return rssi;
    }
}