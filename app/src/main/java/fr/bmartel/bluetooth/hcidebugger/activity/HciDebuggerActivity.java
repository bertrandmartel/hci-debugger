/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.bluetooth.hcidebugger.activity;

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
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import fr.bmartel.bluetooth.hcidebugger.IHciDebugger;
import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.SimpleDividerItemDecoration;
import fr.bmartel.bluetooth.hcidebugger.adapter.PacketAdapter;
import fr.bmartel.bluetooth.hcidebugger.common.Constants;
import fr.bmartel.bluetooth.hcidebugger.menu.MenuUtils;
import fr.bmartel.bluetooth.hcidebugger.model.AdvertizingReport;
import fr.bmartel.bluetooth.hcidebugger.model.Filters;
import fr.bmartel.bluetooth.hcidebugger.model.Packet;
import fr.bmartel.bluetooth.hcidebugger.model.PacketDest;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciAclData;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciCmd;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciEvent;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciEventLEMeta;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciLEAdvertizing;
import fr.bmartel.bluetooth.hcidebugger.model.PacketHciScoData;
import fr.bmartel.bluetooth.hcidebugger.model.ValuePair;
import fr.bmartel.bluetooth.hcidebugger.view.CustomRecyclerView;


public class HciDebuggerActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, RecyclerView.OnItemTouchListener, IHciDebugger {

    private final static String BT_CONFIG = "/etc/bluetooth/bt_stack.conf";

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

    private int frameCount = 1;

    private CustomRecyclerView packetListView;

    private PacketAdapter packetAdapter;

    private ExecutorService pool = Executors.newFixedThreadPool(1);

    private BluetoothAdapter mBluetoothAdapter = null;

    private boolean mScanning;

    private boolean isFiltered = false;

    private List<Packet> packetList = new ArrayList<>();
    private List<Packet> packetFilteredList = new ArrayList<>();

    private Filters filters = new Filters("", "", "", "", "");

    private int scanItemCount = 0;

    private SharedPreferences prefs;

    private boolean startScan = false;

    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean floatBtnDown = true;

    FrameLayout mDisplayFrame;
    FrameLayout mWaitingFrame;

    private int mPacketCount = 0;
    private int mMaxPacketCount = Constants.DEFAULT_LAST_PACKET_COUNT;

    private Runnable decodingTask = new Runnable() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDisplayFrame.setVisibility(View.GONE);
                    mWaitingFrame.setVisibility(View.VISIBLE);
                }
            });

            String filePath = getHciLogFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                showWarningDialog("HCI file is specified but is not present in filesystem. Check in Developper Section that btsnoop file log is activated");
                return;
            }

            if (!filePath.equals("")) {
                startHciLogStream(filePath, prefs.getInt("lastPacketCount", Constants.DEFAULT_LAST_PACKET_COUNT));
            } else {
                showWarningDialog("HCI file path not specified in " + BT_CONFIG);
            }
        }
    };

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
                builder.setMessage(message).setPositiveButton("exit", dialogClickListener)
                        .setNegativeButton("retry", dialogClickListener).show();
            }
        });
    }

    private GestureDetector mGestureDetector;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.debugger_activity);
        super.onCreate(savedInstanceState);

        mDisplayFrame = (FrameLayout) findViewById(R.id.display_frame);
        mWaitingFrame = (FrameLayout) findViewById(R.id.waiting_frame);


        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar.setTitle("");
        }

        prefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);

        filters.setPacketType(prefs.getString("packetTypeFilter", ""));
        filters.setEventType(prefs.getString("eventTypeFilter", ""));
        filters.setOgf(prefs.getString("ogfFilter", ""));
        filters.setSubEventType(prefs.getString("subeventFilter", ""));
        filters.setAddress(prefs.getString("advertizingAddr", ""));

        mMaxPacketCount = prefs.getInt("lastPacketCount", Constants.DEFAULT_LAST_PACKET_COUNT);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        packetListView = (CustomRecyclerView) findViewById(R.id.packet_list);

        packetList = new ArrayList<>();

        packetAdapter = new PacketAdapter(packetList, this);

        packetListView.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false));

        packetListView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));

        packetListView.setAdapter(packetAdapter);
        packetListView.addOnItemTouchListener(this);

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

        pool.execute(decodingTask);
    }

    private void clearAdapter() {
        Log.v(TAG, "clearing adapter");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packetList.clear();
                packetFilteredList.clear();
                //packetListView.clearChoices();
                notifyAdapter();
            }
        });
    }

    private void toggleScan(MenuItem item) {
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
            //scan_button.setText("start scan");
            item.setIcon(R.drawable.ic_action_scanning);
            item.setTitle("enable bluetooth");
            Toast.makeText(HciDebuggerActivity.this, "scan has stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScan() {
        mScanning = true;
        scanItemCount = 0;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        //scan_button.setText("stop scan");
        MenuItem item = toolbar.getMenu().findItem(R.id.scan_btn);
        item.setIcon(R.drawable.ic_portable_wifi_off);
        item.setTitle("disable bluetooth");
        Toast.makeText(HciDebuggerActivity.this, "scan has started", Toast.LENGTH_SHORT).show();
    }

    private void toggleBtState() {
        if (mBluetoothAdapter.isEnabled()) {
            setBluetooth(false);
        } else {
            setBluetooth(true);
        }
    }

    private void filter() {
        Log.v(TAG, "setting filter");
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HciDebuggerActivity.this);
        //dialogBuilder.setCancelable(false);

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
                editor.putString("advertizingAddr", s.toString());
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

                filters = new Filters(packet_type_filter.getSelectedItem().toString(),
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
                Log.i(TAG, "new size : " + packetFilteredList.size());

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

    public void refresh() {
        Log.v(TAG, "refreshing adapter");
        packetList.clear();
        packetFilteredList.clear();
        //packetListView.clearChoices();
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

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = toolbar.getMenu().findItem(R.id.scan_btn);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_action_scanning);
                            item.setTitle("start scan");
                            Toast.makeText(HciDebuggerActivity.this, "scan has stopped", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = toolbar.getMenu().findItem(R.id.scan_btn);
                        if (item != null) {
                            item.setIcon(R.drawable.ic_portable_wifi_off);
                            item.setTitle("stop scan");
                            Toast.makeText(HciDebuggerActivity.this, "scan has started", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MenuItem item = toolbar.getMenu().findItem(R.id.state_bt_btn);
                            if (item != null) {
                                item.setIcon(R.drawable.ic_bluetooth_disabled);
                                item.setTitle("enable bluetooth");
                                Toast.makeText(HciDebuggerActivity.this, "bluetooth disabled", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (state == BluetoothAdapter.STATE_ON) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MenuItem item = toolbar.getMenu().findItem(R.id.state_bt_btn);
                            if (item != null) {
                                item.setIcon(R.drawable.ic_bluetooth);
                                item.setTitle("disable bluetooth");
                                Toast.makeText(HciDebuggerActivity.this, "bluetooth enabled", Toast.LENGTH_SHORT).show();
                            }
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

    private boolean matchFilter(Packet packet) {

        if (!filters.getPacketTypeFilter().equals("")) {

            if (packet.getType().getValue().contains(filters.getPacketTypeFilter())) {

                if (filters.getPacketTypeFilter().equals("COMMAND")) {

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
                } else if (filters.getPacketTypeFilter().equals("EVENT")) {

                    PacketHciEvent event = (PacketHciEvent) packet;

                    if (!filters.getEventTypeFilter().equals("")) {

                        if (event.getEventType().getValue().contains(filters.getEventTypeFilter())) {

                            if (!filters.getSubeventFilter().equals("") && filters.getEventTypeFilter().equals("LE_META")) {

                                if (packet instanceof PacketHciEventLEMeta) {

                                    PacketHciEventLEMeta leMeta = (PacketHciEventLEMeta) packet;

                                    if (!filters.getSubeventFilter().equals("")) {

                                        if (leMeta.getSubevent().getValue().contains(filters.getSubeventFilter())) {

                                            if (filters.getSubeventFilter().equals("ADVERTISING_REPORT")) {

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

                Log.i(TAG, "position : " + sItems.getSelectedItem());

                if (!sItems.getSelectedItem().toString().equals("Choose")) {

                    SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();

                    if (ressourceId == R.array.packet_type_array) {

                        if (sItems.getSelectedItem().toString().equals("EVENT")) {
                            displayEventSpinner(view);
                        } else {
                            displayCmdSpinner(view);
                        }
                        filters.setPacketType(sItems.getSelectedItem().toString());

                        editor.putString("packetTypeFilter", sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.event_type_array) {

                        if (sItems.getSelectedItem().toString().equals("LE_META")) {
                            displaySubEventSpinner(view);
                        }

                        filters.setEventType(sItems.getSelectedItem().toString());

                        editor.putString("eventTypeFilter", sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.ogf_array) {

                        filters.setOgf(sItems.getSelectedItem().toString());

                        editor.putString("ogfFilter", sItems.getSelectedItem().toString());
                        editor.commit();

                    } else if (ressourceId == R.array.subevent_array) {

                        if (sItems.getSelectedItem().toString().equals("ADVERTISING_REPORT")) {
                            displayAdvertizingReportFilter(view);
                        }

                        filters.setSubEventType(sItems.getSelectedItem().toString());

                        editor.putString("subeventFilter", sItems.getSelectedItem().toString());
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

    private void displayEventSpinner(View view) {

        Spinner ogf_filter = (Spinner) view.findViewById(R.id.cmd_ogf_filter);
        ogf_filter.setVisibility(View.GONE);
        Spinner event_type_filter = (Spinner) view.findViewById(R.id.event_type_filter);
        event_type_filter.setVisibility(View.VISIBLE);
    }

    private void displaySubEventSpinner(View view) {
        displayEventSpinner(view);
        Spinner subevent_type_filter = (Spinner) view.findViewById(R.id.subevent_type_filter);
        subevent_type_filter.setVisibility(View.VISIBLE);
    }

    private void displayAdvertizingReportFilter(View view) {
        displaySubEventSpinner(view);
        TextView device_address_label = (TextView) view.findViewById(R.id.device_address_label);
        device_address_label.setVisibility(View.VISIBLE);
        EditText device_address_edit = (EditText) view.findViewById(R.id.device_address_edit);
        device_address_edit.setVisibility(View.VISIBLE);
    }

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

    private boolean setBluetooth(boolean state) {

        boolean isEnabled = mBluetoothAdapter.isEnabled();
        Log.i(TAG, "Setting bluetooth " + isEnabled + " : " + state);
        if (!isEnabled && state) {
            return mBluetoothAdapter.enable();
        } else if (isEnabled && !state) {
            return mBluetoothAdapter.disable();
        }
        return false;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (device != null && device.getAddress() != null && device.getAddress().equals("F2:34:A1:39:16:AA")) {

                scanItemCount++;
                Log.i(TAG, timestampFormat.format(new Date().getTime()) + " detected NIU : " + scanItemCount);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        frameCount = 1;
        unregisterReceiver(mBroadcastReceiver);
        stopHciLogStream();
    }

    public String getHciLogFilePath() {

        try {

            FileInputStream fis = new FileInputStream(BT_CONFIG);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (line.contains("BtSnoopFileName")) {
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

    public void onFinishedPacketCount(int packetCount) {
        Log.i(TAG, "onFinishedPacketCount " + packetCount);
        mPacketCount = packetCount;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWaitingFrame.setVisibility(View.GONE);
                mDisplayFrame.setVisibility(View.VISIBLE);
            }
        });

    }

    public void onHciFrameReceived(final String snoopFrame, final String hciFrame) {

        if (frameCount > mMaxPacketCount) {
            mPacketCount++;
        }
        //Log.v(TAG, "new frame | SNOOP : " + snoopFrame + " | HCI : " + hciFrame);

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
            //Log.v(TAG, "new frame | SNOOP : " + snoopFrame + " | HCI : " + hciFrame);
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
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());

        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            int index = rv.getChildAdapterPosition(childView);

            Intent intent = new Intent(this, DescriptionActivity.class);

            if (packetFilteredList.size() != 0) {
                intent.putExtra("hci_packet", packetFilteredList.get(index).getJsonFormattedHciPacket());
                intent.putExtra("snoop_packet", packetFilteredList.get(index).getJsonFormattedSnoopPacket());
                intent.putExtra("packet_number", packetFilteredList.get(index).getNum());
                intent.putExtra("packet_ts", timestampFormat.format(packetFilteredList.get(index).getTimestamp().getTime()));
                intent.putExtra("packet_type", packetFilteredList.get(index).getDisplayedType());
                intent.putExtra("packet_dest", packetFilteredList.get(index).getDest().toString());
            } else {
                intent.putExtra("hci_packet", packetList.get(index).getJsonFormattedHciPacket());
                intent.putExtra("snoop_packet", packetList.get(index).getJsonFormattedSnoopPacket());
                intent.putExtra("packet_number", packetList.get(index).getNum());
                intent.putExtra("packet_ts", timestampFormat.format(packetList.get(index).getTimestamp().getTime()));
                intent.putExtra("packet_type", packetList.get(index).getDisplayedType());
                intent.putExtra("packet_dest", packetList.get(index).getDest().toString());
            }
            startActivity(intent);
            return true;
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void setMaxPacketValue(int maxPacketValue) {
        mMaxPacketCount = maxPacketValue;
    }
}
