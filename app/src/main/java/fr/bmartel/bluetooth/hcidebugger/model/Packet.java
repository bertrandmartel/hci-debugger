package fr.bmartel.bluetooth.hcidebugger.model;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public abstract class Packet implements IPacket {

    private Date timestamp = null;
    private PacketDest dest;
    private ValuePair type;
    private int num;
    private String displayedType = "";

    public Packet(int num, Date timestamp, PacketDest dest, ValuePair type) {
        this.timestamp = timestamp;
        this.dest = dest;
        this.type = type;
        this.num = num;
        displayedType = type.getValue().replace("HCI_TYPE_", "");
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public PacketDest getDest() {
        return dest;
    }

    public ValuePair getType() {
        return type;
    }

    public int getNum() {
        return num;
    }

    public String getDisplayedType() {
        return displayedType;
    }
}
