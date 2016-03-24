package fr.bmartel.bluetooth.hcidebugger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bertrand Martel
 */
public class PacketHciLEAdvertizing extends PacketHciEventLEMeta {

    private List<AdvertizingReport> reports = new ArrayList<>();

    public PacketHciLEAdvertizing(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair eventType, ValuePair subevent, List<AdvertizingReport> reports, String jsonFormattedHciPacket, String jsonFormattedSnoopPacket) {
        super(num, timestamp, dest, type, eventType, subevent, jsonFormattedHciPacket, jsonFormattedSnoopPacket);
        this.reports = reports;
    }

    public List<AdvertizingReport> getReports() {
        return reports;
    }
}
