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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Standalone service used by main activity to decode snoop HCI file. <br/>
 * This is useful to catch user swipe with onTaskStop=false in manifest to be sure to call stopHciLogStream() when process is killed
 *
 * @author Bertrand Martel
 */
public class HciDebuggerService extends Service implements IDecodingService {

    private final static String TAG = HciDebuggerService.class.getSimpleName();

    /**
     * load native module entry point
     */
    static {
        System.loadLibrary("hciviewer");
    }

    /**
     * start streaming hci log file
     */
    @Override
    public native void startHciLogStream(String filePath, int lastPacketCount);

    /**
     * stop streaming hci log file
     */
    @Override
    public native void stopHciLogStream();

    /**
     * Service binder
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * this is the callback called in activity when packets are received
     */
    private IPacketReceptionCallback callback;

    /**
     * set packet reception cb
     *
     * @param cb
     */
    @Override
    public void setPacketReceptionCb(IPacketReceptionCallback cb) {
        callback = cb;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "'onCreate HciDebuggerService");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v(TAG, "onTaskRemoved");
        stopHciLogStream();
        Log.v(TAG, "exit");
        stopSelf();
    }

    /**
     * callback called from native function when a HCI pakcet has been decoded
     *
     * @param snoopFrame snoop frame part
     * @param hciFrame   HCI packet part
     */
    public void onHciFrameReceived(final String snoopFrame, final String hciFrame) {
        if (callback != null) {
            callback.onHciFrameReceived(snoopFrame, hciFrame);
        }
    }

    /**
     * callback called from native function when packet count is finished
     *
     * @param packetCount total number of HCI packet available
     */
    public void onFinishedPacketCount(int packetCount) {
        if (callback != null) {
            callback.onFinishedPacketCount(packetCount);
        }
    }

    /*
     * LocalBInder that render public getService() for public access
     */
    public class LocalBinder extends Binder {
        public HciDebuggerService getService() {
            return HciDebuggerService.this;
        }
    }
}
