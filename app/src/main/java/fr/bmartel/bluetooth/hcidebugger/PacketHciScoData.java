package fr.bmartel.bluetooth.hcidebugger;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public class PacketHciScoData extends  Packet {

    public PacketHciScoData(int num, Date timestamp, PacketDest dest, ValuePair type) {
        super(num, timestamp, dest, type);
    }

    @Override
    public String getDisplayedInfo() {
        return "";
    }
}
