/**************************************************************************
 * This file is part of HCI Debugger                                      *
 * <p/>                                                                   *
 * Copyright (C) 2016  Bertrand Martel                                    *
 * <p/>                                                                   *
 * Foobar is free software: you can redistribute it and/or modify         *
 * it under the terms of the GNU General Public License as published by   *
 * the Free Software Foundation, either version 3 of the License, or      *
 * (at your option) any later version.                                    *
 * <p/>                                                                   *
 * Foobar is distributed in the hope that it will be useful,              *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 * GNU General Public License for more details.                           *
 * <p/>                                                                   *
 * You should have received a copy of the GNU General Public License      *
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.        *
 */
package com.github.akinaru.hcidebugger.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.inter.IViewHolderClickListener;
import com.github.akinaru.hcidebugger.model.Packet;
import com.github.akinaru.hcidebugger.model.PacketDest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * HCI Packet Adapter
 *
 * @author Bertrand Martel
 */
public class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ViewHolder> {

    /**
     * list of HCI packet displayed in recyclerview
     */
    List<Packet> packetFilteredList = new ArrayList<>();

    /**
     * format for date
     */
    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * ANdroid context
     */
    private Context context = null;

    /**
     * click listener
     */
    private IViewHolderClickListener mListener;

    /**
     * Build packet adapter
     *
     * @param list     HCI packet list
     * @param context  Android context
     * @param listener item click listener
     */
    public PacketAdapter(List<Packet> list, Context context, IViewHolderClickListener listener) {
        this.packetFilteredList = list;
        this.context = context;
        this.mListener = listener;
    }

    @Override
    public PacketAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.packet_item_portrait, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.packet_item, parent, false);
        }
        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Packet item = packetFilteredList.get(position);

        holder.packet_num.setText("" + item.getNum());
        holder.packet_timestamp.setText(timestampFormat.format(item.getTimestamp()));
        holder.packet_type.setText(item.getDisplayedType());
        holder.packet_info.setText(item.getDisplayedInfo());
        if (item.getDest() == PacketDest.PACKET_RECEIVED)
            holder.packet_dest.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_trending_down));
        else
            holder.packet_dest.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_trending_up));
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

    /**
     * ViewHolder for HCI packet
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /**
         * packet item layout
         */
        public LinearLayout layout;

        /**
         * packet seq number
         */
        public TextView packet_num;

        /**
         * packet timestamp
         */
        public TextView packet_timestamp;

        /**
         * packet type name
         */
        public TextView packet_type;

        /**
         * packet information (this is OGF for HCI command & hci event name or subevent for HCI event type)
         */
        public TextView packet_info;

        /**
         * direction of packet (send or receive)
         */
        public ImageView packet_dest;

        /**
         * click listener
         */
        public IViewHolderClickListener mListener;

        /**
         * ViewHolder for HCI packet
         *
         * @param v
         * @param listener
         */
        public ViewHolder(View v, IViewHolderClickListener listener) {
            super(v);
            mListener = listener;
            packet_num = (TextView) v.findViewById(R.id.packet_num);
            packet_timestamp = (TextView) v.findViewById(R.id.packet_timestamp);
            packet_type = (TextView) v.findViewById(R.id.packet_type);
            packet_info = (TextView) v.findViewById(R.id.packet_info);
            packet_dest = (ImageView) v.findViewById(R.id.packet_dest);
            layout = (LinearLayout) v.findViewById(R.id.packet_item);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

}
