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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.debugger_activity);

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

        ExecutorService pool = Executors.newFixedThreadPool(1);

        pool.execute(new Runnable() {
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
        });
    }

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
                hciJson.getJSONObject("parameters");

            final PacketDest finalDest = dest;

            if (type.getCode() == 4) {

                JSONObject event_code = hciJson.getJSONObject("event_code");
                final ValuePair eventType = new ValuePair(event_code.getInt("code"), event_code.getString("value"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.insert(new PacketHciEvent(frameCount++, timestamp, finalDest, type, eventType), 0);
                        packetAdapter.notifyDataSetChanged();
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
                        packetAdapter.notifyDataSetChanged();
                    }
                });
            } else if (type.getCode() == 2) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.insert(new PacketHciAclData(frameCount++, timestamp, finalDest, type), 0);
                        packetAdapter.notifyDataSetChanged();
                    }
                });

            } else if (type.getCode() == 3) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetAdapter.insert(new PacketHciScoData(frameCount++, timestamp, finalDest, type), 0);
                        packetAdapter.notifyDataSetChanged();
                    }
                });

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG, "new frame | SNOOP : " + snoopFrame + " | HCI : " + hciFrame);
        }

    }
}
