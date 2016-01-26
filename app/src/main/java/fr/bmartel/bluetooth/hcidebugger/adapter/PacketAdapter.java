package fr.bmartel.bluetooth.hcidebugger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.model.Packet;

public class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ViewHolder> {

    List<Packet> packetFilteredList = new ArrayList<>();

    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static LayoutInflater inflater = null;

    private int selectedPacket = -1;

    private Context context = null;

    public PacketAdapter(List<Packet> list, Context context) {
        this.packetFilteredList = list;
        this.context = context;
    }

    @Override
    public PacketAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.packet_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Packet item = packetFilteredList.get(position);

        holder.packet_num.setText("" + item.getNum());
        holder.packet_timestamp.setText(timestampFormat.format(item.getTimestamp()));
        holder.packet_type.setText(item.getDisplayedType());
        holder.packet_info.setText(item.getDisplayedInfo());

        if (item.getNum() != selectedPacket)
            holder.layout.setBackgroundColor(context.getResources().getColor((R.color.background)));
        else
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.highlight));
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

    @Override
    public int getItemCount() {
        return packetFilteredList.size();
    }

    public int getSelectedPacket() {
        return selectedPacket;
    }

    public void setSelectedPacket(int selectedPacket) {
        this.selectedPacket = selectedPacket;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layout;
        public TextView packet_num;
        public TextView packet_timestamp;
        public TextView packet_type;
        public TextView packet_info;

        public ViewHolder(View v) {
            super(v);
            packet_num = (TextView) v.findViewById(R.id.packet_num);
            packet_timestamp = (TextView) v.findViewById(R.id.packet_timestamp);
            packet_type = (TextView) v.findViewById(R.id.packet_type);
            packet_info = (TextView) v.findViewById(R.id.packet_info);
            layout = (LinearLayout) v.findViewById(R.id.packet_item);
        }
    }

}
