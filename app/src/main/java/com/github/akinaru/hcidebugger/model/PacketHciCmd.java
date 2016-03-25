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
 * BLuetooth HCI command packet
 *
 * @author Bertrand Martel
 */
public class PacketHciCmd extends Packet {

    private ValuePair ocf;
    private ValuePair ogf;

    private String displayedInfo = "";

    public PacketHciCmd(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair ocf, ValuePair ogf, String jsonFormattedHciPacket, String jsonFormattedSnoopPacket) {
        super(num, timestamp, dest, type, jsonFormattedHciPacket, jsonFormattedSnoopPacket);

        this.ocf = ocf;
        this.ogf = ogf;

        if (ocf.getCode() != -1)
            displayedInfo = ocf.getValue().replace("HCI_CMD_OCF_", "").replace("_COMMAND", "").replace("CTRL_BSB_", "").replace("INFORMATIONAL_", "");
        else
            displayedInfo = ogf.getValue().replace("HCI_CMD_OGF_", "");
    }

    public ValuePair getOcf() {
        return ocf;
    }

    public ValuePair getOgf() {
        return ogf;
    }

    @Override
    public String getDisplayedInfo() {
        return displayedInfo;
    }
}
