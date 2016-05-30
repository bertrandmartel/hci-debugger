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
package com.github.akinaru.hcidebugger.inter;

import android.content.Context;
import android.view.MenuItem;

import com.github.akinaru.hcidebugger.model.ScanType;

/**
 * interface for HCI Debugger main activity
 *
 * @author Bertrand Martel
 */
public interface IHciDebugger {

    /**
     * refresh adapter & launch a new decoding task (and stop the former one)
     */
    void refresh();

    /**
     * set max packet value
     *
     * @param maxPacketValue
     */
    void setMaxPacketValue(int maxPacketValue);

    /**
     * retrieve android context
     *
     * @return
     */
    Context getContext();

    /**
     * Change bluetooth state On/Off
     */
    void toggleBtState();

    /**
     * start/stop ble scan
     *
     * @param menuItem
     */
    void toggleScan(MenuItem menuItem);

    /**
     * reset Snoop file
     */
    void resetSnoopFile();

    /**
     * get bluetooth scan type
     *
     * @return
     */
    ScanType getScanType();

    /**
     * set bluetooth scan type
     *
     * @param bleScan
     */
    void setScanType(ScanType bleScan);
}
