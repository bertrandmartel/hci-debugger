package fr.bmartel.bluetooth.hcidebugger.model;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public class PacketHciCmd extends Packet {

    private ValuePair ocf;
    private ValuePair ogf;

    private String displayedInfo = "";

    public PacketHciCmd(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair ocf, ValuePair ogf) {
        super(num, timestamp, dest, type);

        this.ocf = ocf;
        this.ogf = ogf;

        if (ocf.getCode() != -1)
            displayedInfo = ocf.getValue().replace("HCI_CMD_OCF_", "").replace("_COMMAND", "").replace("CTRL_BSB_", "").replace("INFORMATIONAL_", "");
        else
            displayedInfo = ogf.getValue().replace("HCI_CMD_OGF_", "");
    }

    public ValuePair getOcf() {
        return ocf;
    }

    public ValuePair getOgf() {
        return ogf;
    }

    @Override
    public String getDisplayedInfo() {
        return displayedInfo;
    }
}
