package fr.bmartel.bluetooth.hcidebugger;

import android.content.Context;

/**
 * Created by akinaru on 24/03/16.
 */
public interface IHciDebugger {

    void refresh();

    void setMaxPacketValue(int maxPacketValue);

    Context getContext();
}
