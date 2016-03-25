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

import java.util.Date;

/**
 * HCI generic packet
 *
 * @author Bertrand Martel
 */
public abstract class Packet implements IPacket {

    /**
     * packet timestamp
     */
    private Date timestamp = null;

    /**
     * packet destination (sent/received)
     */
    private PacketDest dest;

    /**
     * packet type
     */
    private ValuePair type;

    /**
     * packet sequential number
     */
    private int num;

    /**
     * type to be displayed for this packet (not necessarily the packet type)
     */
    private String displayedType = "";

    /**
     * full hci packet frame in json format
     */
    private String jsonFormattedHciPacket = "";

    /**
     * full snoop packet frame in json format
     */
    private String jsonFormattedSnoopPacket = "";

    /**
     * Build HCI packet
     *
     * @param num                      sequential number
     * @param timestamp                packet timestamp
     * @param dest                     packet sent or received
     * @param type                     packet type
     * @param jsonFormattedHciPacket   hci frame in json format
     * @param jsonFormattedSnoopPacket snoop frame in jons format
     */
    public Packet(int num, Date timestamp, PacketDest dest, ValuePair type, String jsonFormattedHciPacket, String jsonFormattedSnoopPacket) {
        this.timestamp = timestamp;
        this.dest = dest;
        this.type = type;
        this.num = num;
        displayedType = type.getValue().replace("HCI_TYPE_", "");
        this.jsonFormattedHciPacket = jsonFormattedHciPacket;
        this.jsonFormattedSnoopPacket = jsonFormattedSnoopPacket;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public PacketDest getDest() {
        return dest;
    }

    public ValuePair getType() {
        return type;
    }

    public int getNum() {
        return num;
    }

    public String getDisplayedType() {
        return displayedType;
    }

    public String getJsonFormattedHciPacket() {
        return jsonFormattedHciPacket;
    }

    public String getJsonFormattedSnoopPacket() {
        return jsonFormattedSnoopPacket;
    }
}
