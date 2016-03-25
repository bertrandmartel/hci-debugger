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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * BLuetooth HCI advertising reports packet (subevent)
 *
 * @author Bertrand Martel
 */
public class PacketHciLEAdvertizing extends PacketHciEventLEMeta {

    private List<AdvertizingReport> reports = new ArrayList<>();

    public PacketHciLEAdvertizing(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair eventType, ValuePair subevent, List<AdvertizingReport> reports, String jsonFormattedHciPacket, String jsonFormattedSnoopPacket) {
        super(num, timestamp, dest, type, eventType, subevent, jsonFormattedHciPacket, jsonFormattedSnoopPacket);
        this.reports = reports;
    }

    public List<AdvertizingReport> getReports() {
        return reports;
    }
}
