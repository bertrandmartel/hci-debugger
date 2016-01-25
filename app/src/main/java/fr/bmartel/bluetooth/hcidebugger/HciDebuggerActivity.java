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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

    private Button bluetoothStateBtn;

    private Runnable decodingTask = new Runnable() {
        @Override
        public void run() {

            String filePath = getHciLogFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "HCI file is specified but is not present in filesystem. Check in Developper Section that btsnoop file log is activated");
                return;
            }

            if (!filePath.equals("")) {
                startHciLogStream(filePath);
            } else {
                Log.e(TAG, "HCI file path not specified in " + BT_CONFIG);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.debugger_activity);

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

        final ArrayList<Packet> list = new ArrayList<>();

        packetAdapter = new PacketAdapter(HciDebuggerActivity.this,
                R.layout.packet_item, list);

        packetListView.setAdapter(packetAdapter);

        packetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Packet item = (Packet) parent.getItemAtPosition(position);

            }
        });

        Button clear_button = (Button) findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "clearing adapter");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.clear();
                        notifyAdapter();
                    }
                });
            }
        });

        Button refresh_button = (Button) findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "refreshing adapter");
                packetAdapter.clear();
                notifyAdapter();
                frameCount = 1;
                stopHciLogStream();
                pool.execute(decodingTask);
            }
        });

        final Button scan_button = (Button) findViewById(R.id.scan_button);
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

        bluetoothStateBtn = (Button) findViewById(R.id.enable_bluetooth);

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

        Button filter_button = (Button) findViewById(R.id.filter_button);
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "setting filter");
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        pool.execute(decodingTask);
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
                                    packetAdapter.insert(new PacketHciLEAdvertizing(frameCount++, timestamp, finalDest, type, eventType, subevent_code_val, reportList), 0);
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
                        packetAdapter.insert(new PacketHciEvent(frameCount++, timestamp, finalDest, type, eventType), 0);
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
                        packetAdapter.insert(new PacketHciCmd(frameCount++, timestamp, finalDest, type, ocf, ogf), 0);
                        notifyAdapter();
                    }
                });
            } else if (type.getCode() == 2) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.insert(new PacketHciAclData(frameCount++, timestamp, finalDest, type), 0);
                        notifyAdapter();
                    }
                });

            } else if (type.getCode() == 3) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.insert(new PacketHciScoData(frameCount++, timestamp, finalDest, type), 0);
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
