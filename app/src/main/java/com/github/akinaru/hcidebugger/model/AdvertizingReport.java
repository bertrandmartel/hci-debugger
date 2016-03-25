/**************************************************************************
 * This file is part of HCI Debugger                                      *
 * <p/>                                                                   *
 * Copyright (C) 2016  Bertrand Martel                                    *
 * <p/>                                                                   *
 * Foobar is free software: you can redistribute it and/or modify         *
 * it under the terms of the GNU General Public License as published by   *
 * the Free Software Foundation, either version 3 of the License, or      *
 * (at your option) any later version.                                    *
 * <p/>                                                                   *
 * Foobar is distributed in the hope that it will be useful,              *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 * GNU General Public License for more details.                           *
 * <p/>                                                                   *
 * You should have received a copy of the GNU General Public License      *
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.        *
 */
package com.github.akinaru.hcidebugger.model;

import org.json.JSONArray;

/**
 * Advertising report packet object
 *
 * @author Bertrand Martel
 */
public class AdvertizingReport {

    /**
     * bluetooth device address
     */
    private String address = "";
    private int addressType = -1;

    /**
     * advertising data
     */
    private JSONArray data;
    /**
     * advertising data length
     */
    private int dataLength = 0;

    /**
     * event type
     */
    private int eventType = 0;

    /**
     * rssi value
     */
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