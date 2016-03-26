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
package com.github.akinaru.hcidebugger.service;

/**
 * Created by akinaru on 26/03/16.
 */
public interface IDecodingService {

    /**
     * start streaming hci log file
     */
    void startHciLogStream(String filePath, int lastPacketCount);

    /**
     * stop streaming hci log file
     */
    void stopHciLogStream();

    /**
     * set packet reception cb
     *
     * @param cb
     */
    void setPacketReceptionCb(IPacketReceptionCallback cb);
}
