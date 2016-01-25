package fr.bmartel.bluetooth.hcidebugger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bertrand Martel
 */
public class PacketHciLEAdvertizing extends PacketHciEventLEMeta {

    private List<AdvertizingReport> reports = new ArrayList<>();

    public PacketHciLEAdvertizing(int num, Date timestamp, PacketDest dest, ValuePair type, ValuePair eventType, ValuePair subevent, List<AdvertizingReport> reports) {
        super(num, timestamp, dest, type, eventType, subevent);
        this.reports = reports;
    }

    public List<AdvertizingReport> getReports() {
        return reports;
    }
}
