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
package com.github.akinaru.hcidebugger.common;

/**
 * Some constants for shared preferences, parsing, intent params
 *
 * @author Bertrand Martel
 */
public class Constants {

    public final static String PREFERENCES = "filters";
    public final static int DEFAULT_LAST_PACKET_COUNT = 1000;
    public final static String PREFERENCES_SUBEVENT_FILTERS = "subeventFilter";
    public final static String PREFERENCES_OGF_FILTER = "ogfFilter";
    public final static String PREFERENCES_EVENT_TYPE_FILTER = "eventTypeFilter";
    public final static String PREFERENCES_PACKET_TYPE_FILTER = "packetTypeFilter";
    public final static String PREFERENCES_ADVERTISING_ADDR = "advertizingAddr";
    public final static String PREFERENCES_MAX_PACKET_COUNT = "lastPacketCount";
    public final static String HCI_COMMAND = "COMMAND";
    public final static String HCI_EVENT = "EVENT";
    public final static String HCI_LE_META = "LE_META";
    public final static String HCI_ADVERTISING_REPORT = "ADVERTISING_REPORT";
    public final static String INTENT_HCI_PACKET = "hci_packet";
    public final static String INTENT_SNOOP_PACKET = "snoop_packet";
    public final static String INTENT_PACKET_NUMBER = "packet_number";
    public final static String INTENT_PACKET_TS = "packet_ts";
    public final static String INTENT_PACKET_TYPE = "packet_type";
    public final static String INTENT_PACKET_DEST = "packet_dest";
}
