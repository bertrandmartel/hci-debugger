package fr.bmartel.bluetooth.hcidebugger;

/**
 * @author Bertrand Martel
 */
public class Filters {

    private String packetTypeFilter = "";
    private String eventTypeFilter = "";
    private String ogfFilter = "";
    private String subeventFilter = "";
    private String advertizingAddr = "";

    public Filters(String packetTypeFilter, String eventTypeFilter, String ogfFilter, String subeventFilter, String advertizingAddr) {

        if (!packetTypeFilter.equals("Choose"))
            this.packetTypeFilter = packetTypeFilter;

        if (!eventTypeFilter.equals("Choose"))
            this.eventTypeFilter = eventTypeFilter;

        if (!ogfFilter.equals("Choose"))
            this.ogfFilter = ogfFilter;

        if (!subeventFilter.equals("Choose"))
            this.subeventFilter = subeventFilter;

        if (!advertizingAddr.equals("Choose"))
            this.advertizingAddr = advertizingAddr;
    }


    public String getPacketTypeFilter() {
        return packetTypeFilter;
    }

    public String getEventTypeFilter() {
        return eventTypeFilter;
    }

    public String getOgfFilter() {
        return ogfFilter;
    }

    public String getSubeventFilter() {
        return subeventFilter;
    }

    public String getAdvertizingAddr() {
        return advertizingAddr;
    }

    public void setPacketType(String packetType) {
        this.packetTypeFilter = packetType;
    }

    public void setEventType(String eventType) {
        this.eventTypeFilter = eventType;
    }

    public void setOgf(String ogf) {
        this.ogfFilter = ogf;
    }

    public void setSubEventType(String subEventType) {
        this.subeventFilter = subEventType;
    }

    public void setAddress(String address) {
        this.advertizingAddr = address;
    }
}
