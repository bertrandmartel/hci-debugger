package fr.bmartel.bluetooth.hcidebugger;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public abstract class PacketHciEventLEMeta extends PacketHciEvent {

    private ValuePair subevent;

    public PacketHciEventLEMeta(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair eventType, ValuePair subevent) {
        super(num, timestamp, dest, type, eventType);
        this.subevent = subevent;
    }

    public ValuePair getSubevent() {
        return subevent;
    }
}
