package fr.bmartel.bluetooth.hcidebugger;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public class PacketHciEvent extends Packet {

    private ValuePair eventType;

    private String displayedInfo = "";

    public PacketHciEvent(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair eventType) {
        super(num, timestamp, dest, type);

        this.eventType = eventType;
        this.displayedInfo = eventType.getValue().replace("HCI_EVENT_", "");
    }

    public ValuePair getEventType() {
        return eventType;
    }

    @Override
    public String getDisplayedInfo() {
        return displayedInfo;
    }
}
