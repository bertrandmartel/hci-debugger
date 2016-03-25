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

import android.content.Context;

import com.github.akinaru.hcidebugger.R;

/**
 * Filter configuration
 *
 * @author Bertrand Martel
 */
public class Filters {

    private String packetTypeFilter = "";
    private String eventTypeFilter = "";
    private String ogfFilter = "";
    private String subeventFilter = "";
    private String advertizingAddr = "";

    /**
     * Build filter config from existing filters
     *
     * @param context
     * @param packetTypeFilter
     * @param eventTypeFilter
     * @param ogfFilter
     * @param subeventFilter
     * @param advertizingAddr
     */
    public Filters(Context context, String packetTypeFilter, String eventTypeFilter, String ogfFilter, String subeventFilter, String advertizingAddr) {

        if (!packetTypeFilter.equals(context.getResources().getString(R.string.filter_choose)))
            this.packetTypeFilter = packetTypeFilter;

        if (!eventTypeFilter.equals(context.getResources().getString(R.string.filter_choose)))
            this.eventTypeFilter = eventTypeFilter;

        if (!ogfFilter.equals(context.getResources().getString(R.string.filter_choose)))
            this.ogfFilter = ogfFilter;

        if (!subeventFilter.equals(context.getResources().getString(R.string.filter_choose)))
            this.subeventFilter = subeventFilter;

        if (!advertizingAddr.equals(context.getResources().getString(R.string.filter_choose)))
            this.advertizingAddr = advertizingAddr;
    }


    public String getPacketTypeFilter() {
        return packetTypeFilter;
    }

    public String getEventTypeFilter() {
        return eventTypeFilter;
    }

    public String getOgfFilter() {
        return ogfFilter;
    }

    public String getSubeventFilter() {
        return subeventFilter;
    }

    public String getAdvertizingAddr() {
        return advertizingAddr;
    }

    public void setPacketType(String packetType) {
        this.packetTypeFilter = packetType;
    }

    public void setEventType(String eventType) {
        this.eventTypeFilter = eventType;
    }

    public void setOgf(String ogf) {
        this.ogfFilter = ogf;
    }

    public void setSubEventType(String subEventType) {
        this.subeventFilter = subEventType;
    }

    public void setAddress(String address) {
        this.advertizingAddr = address;
    }
}
