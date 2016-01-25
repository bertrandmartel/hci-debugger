package fr.bmartel.bluetooth.hcidebugger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PacketAdapter extends ArrayAdapter<Packet> {

    List<Packet> packetFilteredList = new ArrayList<>();

    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static LayoutInflater inflater = null;

    public PacketAdapter(Context context, int textViewResourceId,
                         List<Packet> objects) {
        super(context, textViewResourceId, objects);

        this.packetFilteredList = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;

        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.packet_item, null);
                holder = new ViewHolder();
                holder.packet_num = (TextView) vi.findViewById(R.id.packet_num);
                holder.packet_timestamp = (TextView) vi.findViewById(R.id.packet_timestamp);
                holder.packet_type = (TextView) vi.findViewById(R.id.packet_type);
                holder.packet_info = (TextView) vi.findViewById(R.id.packet_info);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.packet_num.setText("" + packetFilteredList.get(position).getNum());
            holder.packet_timestamp.setText(timestampFormat.format(packetFilteredList.get(position).getTimestamp()));
            holder.packet_type.setText(packetFilteredList.get(position).getDisplayedType());
            holder.packet_info.setText(packetFilteredList.get(position).getDisplayedInfo());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    public List<Packet> getPacketList() {
        return packetFilteredList;
    }

    public void setPacketList(List<Packet> list) {
        packetFilteredList = list;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return packetFilteredList.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public TextView packet_num;
        public TextView packet_timestamp;
        public TextView packet_type;
        public TextView packet_info;
    }

}
