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
package fr.bmartel.bluetooth.hcidebugger;

import android.app.Activity;
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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HciDebuggerActivity extends Activity {

    private final static String BT_CONFIG = "/etc/bluetooth/bt_stack.conf";

    static {
        System.loadLibrary("hciviewer");
    }

    /**
     * start streaming hci log file
     */
    public native void startHciLogStream(String filePath);

    /**
     * stop streaming hci log file
     */
    public native void stopHciLogStream();

    private static String TAG = HciDebuggerActivity.class.getSimpleName();

    private int frameCount = 1;

    private ListView packetListView;

    private PacketAdapter packetAdapter;

    private ExecutorService pool = Executors.newFixedThreadPool(1);

    private BluetoothAdapter mBluetoothAdapter = null;

    private boolean mScanning;

    private final static int REQUEST_ENABLE_BT = 1;

    private BootstrapButton bluetoothStateBtn;

    private boolean isFiltered = false;

    private List<Packet> packetList = new ArrayList<>();
    private List<Packet> packetFilteredList = new ArrayList<>();

    private Filters filters = new Filters("", "", "", "", "");

    private final static String PREFERENCES = "filters";

    private int selectedPacket = -1;

    private TextView snoop_frame_text;
    private TextView hci_frame_text;

    private View lastSelectedView = null;

    public static int mSelectedItem;

    private Animation fadein;
    private Animation fadeout;

    private Runnable decodingTask = new Runnable() {
        @Override
        public void run() {

            String filePath = getHciLogFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                showWarningDialog("HCI file is specified but is not present in filesystem. Check in Developper Section that btsnoop file log is activated");
                return;
            }

            if (!filePath.equals("")) {
                startHciLogStream(filePath);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.debugger_activity);

        fadein = AnimationUtils.loadAnimation(HciDebuggerActivity.this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(HciDebuggerActivity.this, R.anim.fadeout);


        snoop_frame_text = (TextView) findViewById(R.id.snoop_frame_text);
        hci_frame_text = (TextView) findViewById(R.id.hci_frame_text);

        SharedPreferences prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        filters.setPacketType(prefs.getString("packetTypeFilter", ""));
        filters.setEventType(prefs.getString("eventTypeFilter", ""));
        filters.setOgf(prefs.getString("ogfFilter", ""));
        filters.setSubEventType(prefs.getString("subeventFilter", ""));
        filters.setAddress(prefs.getString("advertizingAddr", ""));

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        packetListView = (ListView) findViewById(R.id.packet_list);

        packetList = new ArrayList<>();

        packetAdapter = new PacketAdapter(HciDebuggerActivity.this,
                R.layout.packet_item, packetList);

        packetListView.setAdapter(packetAdapter);
        packetListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        packetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Packet item = (Packet) parent.getItemAtPosition(position);

                if (selectedPacket != item.getNum()) {

                    selectedPacket = item.getNum();

                    displayOrbs();

                    /*
                    view.setBackgroundColor(Color.CYAN);

                    if (lastSelectedView != null && lastSelectedView != view) {
                        lastSelectedView.setBackgroundColor(Color.parseColor("#e6e6e6"));
                    }
                    lastSelectedView = view;
                    */

                } else {

                    packetListView.clearChoices();

                    selectedPacket = -1;

                    hideOrbs();

                    //view.setBackgroundColor(Color.parseColor("#e6e6e6"));
                    packetAdapter.notifyDataSetChanged();
                }

                Log.i(TAG, "item clicked !");
            }
        });

        BootstrapButton clear_button = (BootstrapButton) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "clearing adapter");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetList.clear();
                        packetFilteredList.clear();
                        packetAdapter.clear();
                        packetListView.clearChoices();
                        notifyAdapter();

                        hideOrbs();
                    }
                });
            }
        });

        BootstrapButton refresh_button = (BootstrapButton) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "refreshing adapter");
                packetList.clear();
                packetFilteredList.clear();
                packetAdapter.clear();
                packetListView.clearChoices();
                notifyAdapter();
                frameCount = 1;
                stopHciLogStream();
                pool.execute(decodingTask);

                hideOrbs();
            }
        });

        final BootstrapButton scan_button = (BootstrapButton) findViewById(R.id.scan_button);
        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScanning) {
                    Log.v(TAG, "starting scan");
                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    scan_button.setText("stop scan");
                } else {
                    Log.v(TAG, "stopping scan");
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                    scan_button.setText("start scan");
                }
            }
        });

        bluetoothStateBtn = (BootstrapButton) findViewById(R.id.enable_bluetooth);

        if (mBluetoothAdapter.isEnabled()) {
            bluetoothStateBtn.setText("disable BT");
        } else {
            bluetoothStateBtn.setText("enable BT");
        }

        bluetoothStateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mBluetoothAdapter.isEnabled()) {
                    setBluetooth(false);
                } else {
                    setBluetooth(true);
                }
            }
        });

        BootstrapButton filter_button = (BootstrapButton) findViewById(R.id.filter_button);

        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v(TAG, "setting filter");
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HciDebuggerActivity.this);
                dialogBuilder.setCancelable(false);

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

                        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
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
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        pool.execute(decodingTask);
    }

    private void displayOrbs() {

        if ((snoop_frame_text.getVisibility() == View.GONE) &&
                (hci_frame_text.getVisibility() == View.GONE)) {

            snoop_frame_text.setVisibility(View.VISIBLE);
            hci_frame_text.setVisibility(View.VISIBLE);
            snoop_frame_text.startAnimation(fadein);
            hci_frame_text.startAnimation(fadein);
        }
    }

    private void hideOrbs() {

        if ((snoop_frame_text.getVisibility() == View.VISIBLE) &&
                (hci_frame_text.getVisibility() == View.VISIBLE)) {
            snoop_frame_text.startAnimation(fadeout);
            hci_frame_text.startAnimation(fadeout);
            snoop_frame_text.setVisibility(View.GONE);
            hci_frame_text.setVisibility(View.GONE);
        }

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

                    SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();

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

    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF) {

                    Log.e(TAG, "Bluetooth state change to STATE_OFF");
                    bluetoothStateBtn.setText("enable BT");

                } else if (state == BluetoothAdapter.STATE_ON) {
                    Log.e(TAG, "Bluetooth state change to STATE_ON");
                    bluetoothStateBtn.setText("disable BT");

                }
            }
        }
    };

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

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");
        frameCount = 1;
        unregisterReceiver(bluetoothBroadcastReceiver);
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

    public void onHciFrameReceived(String snoopFrame, String hciFrame) {

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

                                    Packet packet = new PacketHciLEAdvertizing(frameCount++, timestamp, finalDest, type, eventType, subevent_code_val, reportList);

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

                        Packet packet = new PacketHciEvent(frameCount++, timestamp, finalDest, type, eventType);

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

                        Packet packet = new PacketHciCmd(frameCount++, timestamp, finalDest, type, ocf, ogf);

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

                        Packet packet = new PacketHciAclData(frameCount++, timestamp, finalDest, type);

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

                        Packet packet = new PacketHciScoData(frameCount++, timestamp, finalDest, type);

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

        TextView frameCountView = (TextView) findViewById(R.id.filter_count);
        frameCountView.setText(packetAdapter.getCount() + " /" + (frameCount - 1));
        packetAdapter.notifyDataSetChanged();
    }

}
