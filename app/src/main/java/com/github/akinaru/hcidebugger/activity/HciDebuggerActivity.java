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
package com.github.akinaru.hcidebugger.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.adapter.PacketAdapter;
import com.github.akinaru.hcidebugger.common.Constants;
import com.github.akinaru.hcidebugger.common.SimpleDividerItemDecoration;
import com.github.akinaru.hcidebugger.inter.IHciDebugger;
import com.github.akinaru.hcidebugger.inter.IViewHolderClickListener;
import com.github.akinaru.hcidebugger.menu.MenuUtils;
import com.github.akinaru.hcidebugger.model.AdvertizingReport;
import com.github.akinaru.hcidebugger.model.Filters;
import com.github.akinaru.hcidebugger.model.Packet;
import com.github.akinaru.hcidebugger.model.PacketDest;
import com.github.akinaru.hcidebugger.model.PacketHciAclData;
import com.github.akinaru.hcidebugger.model.PacketHciCmd;
import com.github.akinaru.hcidebugger.model.PacketHciEvent;
import com.github.akinaru.hcidebugger.model.PacketHciEventLEMeta;
import com.github.akinaru.hcidebugger.model.PacketHciLEAdvertizing;
import com.github.akinaru.hcidebugger.model.PacketHciScoData;
import com.github.akinaru.hcidebugger.model.ValuePair;
import com.github.akinaru.hcidebugger.view.CustomRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity
 *
 * @author Bertrand Martel
 */
public class HciDebuggerActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, IHciDebugger {

    /**
     * load native module entry point
     */
    static {
        System.loadLibrary("hciviewer");
    }

    /**
     * start streaming hci log file
     */
    public native void startHciLogStream(String filePath, int lastPacketCount);

    /**
     * stop streaming hci log file
     */
    public native void stopHciLogStream();

    private static String TAG = HciDebuggerActivity.class.getSimpleName();

    /**
     * number of frame decoded via the callback onHciFrameReceived
     */
    private int frameCount = 1;

    /**
     * HCI packet recycler view (customized for scroll speed issue)
     */
    private CustomRecyclerView packetListView;

    /**
     * adapter for recyclerview
     */
    private PacketAdapter packetAdapter;

    /**
     * thread pool of 1 thread used to execute btsnoop file decoding task
     */
    private ExecutorService pool = Executors.newFixedThreadPool(1);

    /**
     * bluetooth adapter for setting bluetooth on/off, scanning
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * define if bluetooth is scanning or not
     */
    private boolean mScanning;

    /**
     * define if current configuration is the result of a filter operation or not
     */
    private boolean isFiltered = false;

    /**
     * total list of HCI packet decoded
     */
    private List<Packet> packetList = new ArrayList<>();

    /**
     * list of filtered HCI packet (this is cleared when filter is cancelled)
     */
    private List<Packet> packetFilteredList = new ArrayList<>();

    /**
     * filter management object
     */
    private Filters filters;

    /**
     * shared prefenrence object
     */
    private SharedPreferences prefs;

    /**
     * permit to start a scan AFTER enabling bluetooth
     */
    private boolean startScan = false;

    /**
     * format for date
     */
    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * define if float button is targetting a scroll down (false for scroll up)
     */
    private boolean floatBtnDown = true;

    /**
     * frame used for the swipe + recyclerview
     */
    FrameLayout mDisplayFrame;

    /**
     * frame used for displaying a textview to wait for packet count resolution
     */
    FrameLayout mWaitingFrame;

    /**
     * total HCI packet count (even those not decoded)
     */
    private int mPacketCount = 0;

    /**
     * max packet count to be displayed
     */
    private int mMaxPacketCount = Constants.DEFAULT_LAST_PACKET_COUNT;

    private boolean mAllPacketInit = false;

    /**
     * task run in a thread to decoded btsnoop file, HCI packets
     */
    private Runnable decodingTask = new Runnable() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //when decoding task is launched hide recyclerview to wait for packet count callback
                    mDisplayFrame.setVisibility(View.GONE);
                    mWaitingFrame.setVisibility(View.VISIBLE);
                }
            });

            //get btsnoop file
            String filePath = getHciLogFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                showWarningDialog(getResources().getString(R.string.hci_warning));
                return;
            }

            mAllPacketInit = false;
            if (!filePath.equals("")) {
                startHciLogStream(filePath, prefs.getInt(Constants.PREFERENCES_MAX_PACKET_COUNT, Constants.DEFAULT_LAST_PACKET_COUNT));
            } else {
                showWarningDialog("HCI file path not specified in " + getResources().getString(R.string.bluetooth_config));
            }
        }
    };

    /**
     * show a dialog when problem occur
     *
     * @param message
     */
    private void showWarningDialog(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                dialog.cancel();
                                dialog.dismiss();
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                pool.execute(decodingTask);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(HciDebuggerActivity.this);

                builder.setCancelable(false);
                builder.setMessage(message).setPositiveButton(getResources().getString(R.string.warning_dialog_exit), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.warning_dialog_retry), dialogClickListener).show();
            }
        });
    }

    /**
     * swipe refresh layout used to make refresh animation when scrolling top of recyclerview
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.debugger_activity);
        super.onCreate(savedInstanceState);

        //setup frames
        mDisplayFrame = (FrameLayout) findViewById(R.id.display_frame);
        mWaitingFrame = (FrameLayout) findViewById(R.id.waiting_frame);

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        filters = new Filters(this, "", "", "", "", "");
        //get shared preferences
        prefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
        filters.setPacketType(prefs.getString(Constants.PREFERENCES_PACKET_TYPE_FILTER, ""));
        filters.setEventType(prefs.getString(Constants.PREFERENCES_EVENT_TYPE_FILTER, ""));
        filters.setOgf(prefs.getString(Constants.PREFERENCES_OGF_FILTER, ""));
        filters.setSubEventType(prefs.getString(Constants.PREFERENCES_SUBEVENT_FILTERS, ""));
        filters.setAddress(prefs.getString(Constants.PREFERENCES_ADVERTISING_ADDR, ""));
        mMaxPacketCount = prefs.getInt(Constants.PREFERENCES_MAX_PACKET_COUNT, Constants.DEFAULT_LAST_PACKET_COUNT);

        // init Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //init recycler view
        packetListView = (CustomRecyclerView) findViewById(R.id.packet_list);
        packetList = new ArrayList<>();
        packetAdapter = new PacketAdapter(packetList, this, new IViewHolderClickListener() {
            @Override
            public void onClick(View view) {
                int index = packetListView.getChildAdapterPosition(view);

                //launch packet description activity
                Intent intent = new Intent(HciDebuggerActivity.this, DescriptionActivity.class);

                if (packetFilteredList.size() != 0) {
                    intent.putExtra(Constants.INTENT_HCI_PACKET, packetFilteredList.get(index).getJsonFormattedHciPacket());
                    intent.putExtra(Constants.INTENT_SNOOP_PACKET, packetFilteredList.get(index).getJsonFormattedSnoopPacket());
                    intent.putExtra(Constants.INTENT_PACKET_NUMBER, packetFilteredList.get(index).getNum());
                    intent.putExtra(Constants.INTENT_PACKET_TS, timestampFormat.format(packetFilteredList.get(index).getTimestamp().getTime()));
                    intent.putExtra(Constants.INTENT_PACKET_TYPE, packetFilteredList.get(index).getDisplayedType());
                    intent.putExtra(Constants.INTENT_PACKET_DEST, packetFilteredList.get(index).getDest().toString());
                } else {
                    intent.putExtra(Constants.INTENT_HCI_PACKET, packetList.get(index).getJsonFormattedHciPacket());
                    intent.putExtra(Constants.INTENT_SNOOP_PACKET, packetList.get(index).getJsonFormattedSnoopPacket());
                    intent.putExtra(Constants.INTENT_PACKET_NUMBER, packetList.get(index).getNum());
                    intent.putExtra(Constants.INTENT_PACKET_TS, timestampFormat.format(packetList.get(index).getTimestamp().getTime()));
                    intent.putExtra(Constants.INTENT_PACKET_TYPE, packetList.get(index).getDisplayedType());
                    intent.putExtra(Constants.INTENT_PACKET_DEST, packetList.get(index).getDest().toString());
                }
                startActivity(intent);
            }
        });

        //set layout manager
        packetListView.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false));

        //set line decoration
        packetListView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));

        packetListView.setAdapter(packetAdapter);

        //setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //register bluetooth receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        registerReceiver(mBroadcastReceiver, intentFilter);

        //setup floating button
        final FloatingActionButton upFloatingBtn = (FloatingActionButton) findViewById(R.id.updown_btn);
        upFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                if (floatBtnDown) {
                    packetListView.scrollToPosition(packetListView.getAdapter().getItemCount() - 1);
                    floatBtnDown = false;
                    upFloatingBtn.setImageResource(R.drawable.ic_arrow_upward);
                } else {
                    packetListView.scrollToPosition(0);
                    floatBtnDown = true;
                    upFloatingBtn.setImageResource(R.drawable.ic_arrow_downward);
                }

            }
        });

        //laucnh btsnoop file decoding
        pool.execute(decodingTask);
    }

    /**
     * clear adapter
     */
    private void clearAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packetList.clear();
                packetFilteredList.clear();
                notifyAdapter();
            }
        });
    }

    /**
     * start/stop bluetooth scan
     *
     * @param item scan menu item
     */
    @Override
    public void toggleScan(MenuItem item) {
        if (!mScanning) {
            Log.v(TAG, "starting scan");
            if (!mBluetoothAdapter.isEnabled()) {
                startScan = true;
                setBluetooth(true);
            } else {
                startScan();
            }
        } else {
            Log.v(TAG, "stopping scan");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                item.setIcon(R.drawable.ic_looks);
            } else {
                item.setIcon(R.drawable.ic_action_scanning);
            }
            item.setTitle(getResources().getString(R.string.menu_item_title_start_scan));
            Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_scan_stop), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar.getMenu().findItem(R.id.scan_btn).setVisible(false);
            toolbar.getMenu().findItem(R.id.state_bt_btn).setVisible(false);
            nvDrawer.getMenu().findItem(R.id.scan_btn_nv).setVisible(true);
            MenuItem stateBtn = nvDrawer.getMenu().findItem(R.id.state_bt_btn_nv);
            stateBtn.setVisible(true);
            if (mBluetoothAdapter.isEnabled()) {
                stateBtn.setIcon(R.drawable.ic_bluetooth);
            } else {
                stateBtn.setIcon(R.drawable.ic_bluetooth_disabled);
            }
            stateBtn.setTitle(getResources().getString(R.string.menu_item_title_enable_bluetooth_portrait));
        }
        return ret;
    }

    /**
     * start bluetooth scan (launch from menu or when bluetooth is activated if previous start scan was launched)
     */
    private void startScan() {
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);

        MenuItem item;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            item = nvDrawer.getMenu().findItem(R.id.scan_btn_nv);
        } else {
            item = toolbar.getMenu().findItem(R.id.scan_btn);
        }
        if (item != null) {
            item.setIcon(R.drawable.ic_portable_wifi_off);
            item.setTitle(getResources().getString(R.string.menu_item_title_stop_scan));
        }
        Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_scan_start), Toast.LENGTH_SHORT).show();
    }

    /**
     * switch on/off bluetooth
     */
    @Override
    public void toggleBtState() {
        if (mBluetoothAdapter.isEnabled()) {
            setBluetooth(false);
        } else {
            setBluetooth(true);
        }
    }

    /**
     * setup a filter
     */
    private void filter() {
        Log.v(TAG, "setting filter");
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HciDebuggerActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.filter_dialog, null);
        dialogBuilder.setView(dialogView);

        //packet type
        setupSpinnerAdapter(R.array.packet_type_array, dialogView, R.id.packet_type_filter, filters.getPacketTypeFilter());

        //event type
        setupSpinnerAdapter(R.array.event_type_array, dialogView, R.id.event_type_filter, filters.getEventTypeFilter());

        //ogf
        setupSpinnerAdapter(R.array.ogf_array, dialogView, R.id.cmd_ogf_filter, filters.getOgfFilter());

        //subevent_type_filter
        setupSpinnerAdapter(R.array.subevent_array, dialogView, R.id.subevent_type_filter, filters.getSubeventFilter());

        EditText addressText = (EditText) dialogView.findViewById(R.id.device_address_edit);
        addressText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filters.setAddress(s.toString());

                SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();
                editor.putString(Constants.PREFERENCES_ADVERTISING_ADDR, s.toString());
                editor.commit();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addressText.setText(filters.getAdvertizingAddr());

        final AlertDialog alertDialog = dialogBuilder.create();

        final Button button_withdraw_filter = (Button) dialogView.findViewById(R.id.button_withdraw_filter);

        if (isFiltered)
            button_withdraw_filter.setVisibility(View.VISIBLE);
        else
            button_withdraw_filter.setVisibility(View.GONE);

        button_withdraw_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isFiltered = false;
                packetAdapter.setPacketList(packetList);
                notifyAdapter();

                alertDialog.cancel();
                alertDialog.dismiss();
                packetFilteredList.clear();
            }
        });

        Button button_apply = (Button) dialogView.findViewById(R.id.button_apply_filter);

        button_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Spinner packet_type_filter = (Spinner) dialogView.findViewById(R.id.packet_type_filter);
                Spinner ogf_filter = (Spinner) dialogView.findViewById(R.id.cmd_ogf_filter);
                Spinner event_type_filter = (Spinner) dialogView.findViewById(R.id.event_type_filter);
                Spinner subevent_type_filter = (Spinner) dialogView.findViewById(R.id.subevent_type_filter);
                EditText device_address_edit = (EditText) dialogView.findViewById(R.id.device_address_edit);

                filters = new Filters(HciDebuggerActivity.this, packet_type_filter.getSelectedItem().toString(),
                        event_type_filter.getSelectedItem().toString(),
                        ogf_filter.getSelectedItem().toString(),
                        subevent_type_filter.getSelectedItem().toString(),
                        device_address_edit.getText().toString());

                packetFilteredList = new ArrayList<Packet>();
                for (int i = 0; i < packetList.size(); i++) {

                    if (matchFilter(packetList.get(i))) {
                        packetFilteredList.add(packetList.get(i));
                    }
                }

                isFiltered = true;

                packetAdapter.setPacketList(packetFilteredList);
                notifyAdapter();

                alertDialog.cancel();
                alertDialog.dismiss();
            }
        });

        Button button_cancel = (Button) dialogView.findViewById(R.id.button_cancel_filter);

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * refresh adapter
     */
    public void refresh() {
        Log.v(TAG, "refreshing adapter");
        packetList.clear();
        packetFilteredList.clear();
        notifyAdapter();
        frameCount = 1;
        stopHciLogStream();

        pool.execute(decodingTask);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Context getContext() {
        return this;
    }

    /**
     * broadcast receiver for receiver callback when bluetooth is set on/off or external bluetooth discoverey started/stopped
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            //stop discovery detected
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = toolbar.getMenu().findItem(R.id.scan_btn);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_action_scanning);
                            item.setTitle(getResources().getString(R.string.menu_item_title_start_scan));
                            Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_scan_stop), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //start discovery detected
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = toolbar.getMenu().findItem(R.id.scan_btn);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_portable_wifi_off);
                            item.setTitle(getResources().getString(R.string.menu_item_title_stop_scan));
                            Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_scan_start), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //bluetooth state change detected
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                MenuItem stateBtn = nvDrawer.getMenu().findItem(R.id.state_bt_btn_nv);
                                if (stateBtn != null) {
                                    stateBtn.setIcon(R.drawable.ic_bluetooth_disabled);
                                    stateBtn.setTitle(getResources().getString(R.string.menu_item_title_enable_bluetooth_portrait));
                                }
                            } else {
                                MenuItem item = toolbar.getMenu().findItem(R.id.state_bt_btn);
                                if (item != null) {
                                    item.setIcon(R.drawable.ic_bluetooth_disabled);
                                    item.setTitle(getResources().getString(R.string.menu_item_title_enable_bluetooth));
                                }
                            }
                            Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_bluetooth_disabled), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (state == BluetoothAdapter.STATE_ON) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                MenuItem stateBtn = nvDrawer.getMenu().findItem(R.id.state_bt_btn_nv);
                                if (stateBtn != null) {
                                    stateBtn.setIcon(R.drawable.ic_bluetooth);
                                    stateBtn.setTitle(getResources().getString(R.string.menu_item_title_enable_bluetooth_portrait));
                                }
                            } else {
                                MenuItem item = toolbar.getMenu().findItem(R.id.state_bt_btn);
                                if (item != null) {
                                    item.setIcon(R.drawable.ic_bluetooth);
                                    item.setTitle(getResources().getString(R.string.menu_item_title_disable_bluetooth));
                                }
                            }
                            Toast.makeText(HciDebuggerActivity.this, getResources().getString(R.string.toast_bluetooth_enabled), Toast.LENGTH_SHORT).show();
                            if (startScan) {
                                startScan();
                                startScan = false;
                            }
                        }
                    });
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        //clear button
        MenuItem item = menu.findItem(R.id.clear_btn);
        if (item != null) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    clearAdapter();
                    return true;
                }
            });
        }

        //bluetoooth scan button
        item = menu.findItem(R.id.scan_btn);
        if (item != null) {
            item.setIcon(R.drawable.ic_action_scanning);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    toggleScan(item);
                    return true;
                }
            });
        }

        //bluetooth state button
        item = menu.findItem(R.id.state_bt_btn);
        if (item != null) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    toggleBtState();
                    return true;
                }
            });
            if (mBluetoothAdapter.isEnabled()) {
                item.setIcon(R.drawable.ic_bluetooth);
            } else {
                item.setIcon(R.drawable.ic_bluetooth_disabled);
            }
        }

        //filter button
        item = menu.findItem(R.id.filter_btn);
        if (item != null) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    filter();
                    return true;
                }
            });
        }

        //refresh button
        item = menu.findItem(R.id.refresh);
        if (item != null) {
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {

                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        refresh();
                        return true;
                    }
                });
            } else {
                item.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * setup navigation view
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        MenuUtils.selectDrawerItem(menuItem, mDrawer, HciDebuggerActivity.this, HciDebuggerActivity.this);
                        return true;
                    }
                });
    }

    /**
     * determine if a packet match current filter or not
     *
     * @param packet generic HCI packet
     * @return true if packet match filter
     */
    private boolean matchFilter(Packet packet) {

        if (!filters.getPacketTypeFilter().equals("")) {

            if (packet.getType().getValue().contains(filters.getPacketTypeFilter())) {

                if (filters.getPacketTypeFilter().equals(Constants.HCI_COMMAND)) {

                    PacketHciCmd cmd = (PacketHciCmd) packet;

                    if (!filters.getOgfFilter().equals("")) {

                        if (cmd.getOgf().getValue().contains(filters.getOgfFilter())) {
                            return true;
                        } else {
                            return false;
                        }

                    } else {
                        return true;
                    }
                } else if (filters.getPacketTypeFilter().equals(Constants.HCI_EVENT)) {

                    PacketHciEvent event = (PacketHciEvent) packet;

                    if (!filters.getEventTypeFilter().equals("")) {

                        if (event.getEventType().getValue().contains(filters.getEventTypeFilter())) {

                            if (!filters.getSubeventFilter().equals("") && filters.getEventTypeFilter().equals(Constants.HCI_LE_META)) {

                                if (packet instanceof PacketHciEventLEMeta) {

                                    PacketHciEventLEMeta leMeta = (PacketHciEventLEMeta) packet;

                                    if (!filters.getSubeventFilter().equals("")) {

                                        if (leMeta.getSubevent().getValue().contains(filters.getSubeventFilter())) {

                                            if (filters.getSubeventFilter().equals(Constants.HCI_ADVERTISING_REPORT)) {

                                                if (!filters.getAdvertizingAddr().equals("")) {

                                                    PacketHciLEAdvertizing adReportFrame = (PacketHciLEAdvertizing) packet;

                                                    for (int i = 0; i < adReportFrame.getReports().size(); i++) {
                                                        if (adReportFrame.getReports().get(i).getAddress().toLowerCase().equals(filters.getAdvertizingAddr().toLowerCase())) {
                                                            return true;
                                                        }
                                                    }
                                                    return false;

                                                } else {
                                                    return true;
                                                }

                                            } else {
                                                return true;
                                            }

                                        } else {
                                            return false;
                                        }

                                    } else {
                                        return true;
                                    }
                                } else {
                                    return false;
                                }

                            } else {
                                return true;
                            }

                        } else {
                            return false;
                        }

                    } else {
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * setup filter spinner
     *
     * @param ressourceId
     * @param view
     * @param spinnerId
     * @param value
     */
    private void setupSpinnerAdapter(final int ressourceId, final View view, int spinnerId, String value) {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(HciDebuggerActivity.this,
                ressourceId, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner sItems = (Spinner) view.findViewById(spinnerId);
        sItems.setAdapter(adapter);

        sItems.setSelection(getIndex(sItems, value));

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view2, int position, long id) {

                if (!sItems.getSelectedItem().toString().equals(getResources().getString(R.string.filter_choose))) {

                    SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();

                    if (ressourceId == R.array.packet_type_array) {

                        if (sItems.getSelectedItem().toString().equals(Constants.HCI_EVENT)) {
                            displayEventSpinner(view);
                        } else {
                            displayCmdSpinner(view);
                        }
                        filters.setPacketType(sItems.getSelectedItem().toString());

                        editor.putString(Constants.PREFERENCES_PACKET_TYPE_FILTER, sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.event_type_array) {

                        if (sItems.getSelectedItem().toString().equals(Constants.HCI_LE_META)) {
                            displaySubEventSpinner(view);
                        }

                        filters.setEventType(sItems.getSelectedItem().toString());

                        editor.putString(Constants.PREFERENCES_EVENT_TYPE_FILTER, sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.ogf_array) {

                        filters.setOgf(sItems.getSelectedItem().toString());

                        editor.putString(Constants.PREFERENCES_OGF_FILTER, sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.subevent_array) {

                        if (sItems.getSelectedItem().toString().equals(Constants.HCI_ADVERTISING_REPORT)) {
                            displayAdvertizingReportFilter(view);
                        }

                        filters.setSubEventType(sItems.getSelectedItem().toString());

                        editor.putString(Constants.PREFERENCES_SUBEVENT_FILTERS, sItems.getSelectedItem().toString());
                        editor.commit();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    /**
     * display event filter spinner
     *
     * @param view
     */
    private void displayEventSpinner(View view) {

        Spinner ogf_filter = (Spinner) view.findViewById(R.id.cmd_ogf_filter);
        ogf_filter.setVisibility(View.GONE);
        Spinner event_type_filter = (Spinner) view.findViewById(R.id.event_type_filter);
        event_type_filter.setVisibility(View.VISIBLE);
    }

    /**
     * display sub event spinner
     *
     * @param view
     */
    private void displaySubEventSpinner(View view) {
        displayEventSpinner(view);
        Spinner subevent_type_filter = (Spinner) view.findViewById(R.id.subevent_type_filter);
        subevent_type_filter.setVisibility(View.VISIBLE);
    }

    /**
     * display advertising report filter views
     *
     * @param view
     */
    private void displayAdvertizingReportFilter(View view) {
        displaySubEventSpinner(view);
        TextView device_address_label = (TextView) view.findViewById(R.id.device_address_label);
        device_address_label.setVisibility(View.VISIBLE);
        EditText device_address_edit = (EditText) view.findViewById(R.id.device_address_edit);
        device_address_edit.setVisibility(View.VISIBLE);
    }

    /**
     * setup visibility of all filter spinner
     *
     * @param view
     */
    private void displayCmdSpinner(View view) {

        Spinner ogf_filter = (Spinner) view.findViewById(R.id.cmd_ogf_filter);
        ogf_filter.setVisibility(View.VISIBLE);
        Spinner event_type_filter = (Spinner) view.findViewById(R.id.event_type_filter);
        event_type_filter.setVisibility(View.GONE);
        Spinner subevent_type_filter = (Spinner) view.findViewById(R.id.subevent_type_filter);
        subevent_type_filter.setVisibility(View.GONE);
        TextView device_address_label = (TextView) view.findViewById(R.id.device_address_label);
        device_address_label.setVisibility(View.GONE);
        EditText device_address_edit = (EditText) view.findViewById(R.id.device_address_edit);
        device_address_edit.setVisibility(View.GONE);
    }

    /**
     * set bluetooth state
     *
     * @param state bluetooth state
     * @return success if true / failure if false
     */
    private boolean setBluetooth(boolean state) {

        boolean isEnabled = mBluetoothAdapter.isEnabled();
        Log.v(TAG, "Setting bluetooth " + isEnabled + " : " + state);
        if (!isEnabled && state) {
            return mBluetoothAdapter.enable();
        } else if (isEnabled && !state) {
            return mBluetoothAdapter.disable();
        }
        return false;
    }

    /**
     * Callback for BLE scan
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        frameCount = 1;
        unregisterReceiver(mBroadcastReceiver);
        stopHciLogStream();
    }

    /**
     * Retrieve btsnoop file absolute path from bt_stack.conf file
     *
     * @return btsnoop file absolute path
     */
    public String getHciLogFilePath() {

        try {
            FileInputStream fis = new FileInputStream(getResources().getString(R.string.bluetooth_config));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (line.contains(getResources().getString(R.string.bt_config_file_name_filter))) {
                    if (line.indexOf("=") != -1) {
                        fis.close();
                        isr.close();
                        bufferedReader.close();
                        return line.substring(line.indexOf("=") + 1);
                    }
                }
            }

            fis.close();
            isr.close();
            bufferedReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * callback called from native function when packet count is finished
     *
     * @param packetCount total number of HCI packet available
     */
    public void onFinishedPacketCount(int packetCount) {
        Log.v(TAG, "onFinishedPacketCount " + packetCount);
        mPacketCount = packetCount;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //display recyclerview + swipe refresh view
                mWaitingFrame.setVisibility(View.GONE);
                mDisplayFrame.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * callback called from native function when a HCI pakcet has been decoded
     *
     * @param snoopFrame snoop frame part
     * @param hciFrame   HCI packet part
     */
    public void onHciFrameReceived(final String snoopFrame, final String hciFrame) {

        if (!mAllPacketInit && ((frameCount >= mPacketCount) || (frameCount >= mMaxPacketCount))) {
            mAllPacketInit = true;
        }
        if (mAllPacketInit)
            mPacketCount++;

        try {
            JSONObject snoopJson = new JSONObject(snoopFrame);

            final Date timestamp = new Date(snoopJson.getLong("timestamp_microseconds") / 1000);

            PacketDest dest = PacketDest.PACKET_SENT;

            if (snoopJson.getBoolean("packet_received")) {
                dest = PacketDest.PACKET_RECEIVED;
            }

            JSONObject hciJson = new JSONObject(hciFrame);

            JSONObject packet_type = hciJson.getJSONObject("packet_type");
            final ValuePair type = new ValuePair(packet_type.getInt("code"), packet_type.getString("value"));

            JSONObject parameters = null;

            if (hciJson.has("parameters"))
                parameters = hciJson.getJSONObject("parameters");

            final PacketDest finalDest = dest;

            if (type.getCode() == 4) {

                JSONObject event_code = hciJson.getJSONObject("event_code");
                final ValuePair eventType = new ValuePair(event_code.getInt("code"), event_code.getString("value"));

                if (hciJson.has("subevent_code")) {

                    JSONObject subevent_code = hciJson.getJSONObject("subevent_code");
                    final ValuePair subevent_code_val = new ValuePair(subevent_code.getInt("code"), subevent_code.getString("value"));

                    if (subevent_code_val.getCode() == 2) {

                        if (parameters != null && parameters.has("reports")) {

                            JSONArray reports = parameters.getJSONArray("reports");

                            final List<AdvertizingReport> reportList = new ArrayList<>();

                            for (int i = 0; i < reports.length(); i++) {

                                JSONObject reportItem = reports.getJSONObject(i);
                                reportList.add(new AdvertizingReport(reportItem.getString("address"),
                                        reportItem.getInt("address_type"),
                                        reportItem.getJSONArray("data"),
                                        reportItem.getInt("data_length"),
                                        reportItem.getInt("event_type"),
                                        reportItem.getInt("rssi")));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Packet packet = new PacketHciLEAdvertizing(frameCount++, timestamp, finalDest, type, eventType, subevent_code_val, reportList, hciFrame, snoopFrame);

                                    packetList.add(0, packet);

                                    if (isFiltered && matchFilter(packet))
                                        packetFilteredList.add(0, packet);

                                    notifyAdapter();
                                }
                            });

                            return;
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Packet packet = new PacketHciEvent(frameCount++, timestamp, finalDest, type, eventType, hciFrame, snoopFrame);

                        packetList.add(0, packet);

                        if (isFiltered && matchFilter(packet))
                            packetFilteredList.add(0, packet);

                        notifyAdapter();
                    }
                });

            } else if (type.getCode() == 1) {


                JSONObject ogf_obj = hciJson.getJSONObject("ogf");
                final ValuePair ogf = new ValuePair(ogf_obj.getInt("code"), ogf_obj.getString("value"));

                final ValuePair ocf;

                if (hciJson.has("ocf")) {
                    JSONObject ocf_obj = hciJson.getJSONObject("ocf");
                    ocf = new ValuePair(ocf_obj.getInt("code"), ocf_obj.getString("value"));
                } else {
                    ocf = new ValuePair(-1, "");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Packet packet = new PacketHciCmd(frameCount++, timestamp, finalDest, type, ocf, ogf, hciFrame, snoopFrame);

                        packetList.add(0, packet);

                        if (isFiltered && matchFilter(packet))
                            packetFilteredList.add(0, packet);

                        notifyAdapter();
                    }
                });
            } else if (type.getCode() == 2) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Packet packet = new PacketHciAclData(frameCount++, timestamp, finalDest, type, hciFrame, snoopFrame);

                        packetList.add(0, packet);

                        if (isFiltered && matchFilter(packet))
                            packetFilteredList.add(0, packet);

                        notifyAdapter();
                    }
                });

            } else if (type.getCode() == 3) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Packet packet = new PacketHciScoData(frameCount++, timestamp, finalDest, type, hciFrame, snoopFrame);

                        packetList.add(0, packet);

                        if (isFiltered && matchFilter(packet))
                            packetFilteredList.add(0, packet);

                        notifyAdapter();
                    }
                });

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Edit frame count text view and refresh adapter
     */
    private void notifyAdapter() {
        MenuItem item = toolbar.getMenu().findItem(R.id.packet_number_entry);
        if (item != null) {
            item.setTitle(packetAdapter.getItemCount() + "/" + mPacketCount);
        }
        packetAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void setMaxPacketValue(int maxPacketValue) {
        mMaxPacketCount = maxPacketValue;
    }
}
