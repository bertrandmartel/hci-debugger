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

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.common.Constants;
import com.github.akinaru.hcidebugger.menu.MenuUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Packet description activity
 *
 * @author Bertrand Martel
 */
public class DescriptionActivity extends BaseActivity {

    /**
     * fixed description item table
     */
    private TableLayout tablelayout;

    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.description_activity);
        super.onCreate(savedInstanceState);

        //setup navigation items
        setupDrawerContent(nvDrawer);

        //hide max packet count for this activity
        nvDrawer.getMenu().findItem(R.id.set_max_packet_num).setVisible(false);

        //get information sent via intent to be displayed
        String hciPacket = getIntent().getExtras().getString(Constants.INTENT_HCI_PACKET);
        String snoopPacket = getIntent().getExtras().getString(Constants.INTENT_SNOOP_PACKET);
        int packetNumber = getIntent().getExtras().getInt(Constants.INTENT_PACKET_NUMBER);
        String ts = getIntent().getExtras().getString(Constants.INTENT_PACKET_TS);
        String packet_type = getIntent().getExtras().getString(Constants.INTENT_PACKET_TYPE);
        String destination = getIntent().getExtras().getString(Constants.INTENT_PACKET_DEST);

        //setup description item table
        tablelayout = (TableLayout) findViewById(R.id.tablelayout);
        altTableRow(2);

        //setup json highlishter web page
        WebView lWebView = (WebView) findViewById(R.id.webView);

        TextView number_value = (TextView) findViewById(R.id.number_value);
        TextView ts_value = (TextView) findViewById(R.id.ts_value);
        TextView packet_type_value = (TextView) findViewById(R.id.packet_type_value);
        TextView destination_value = (TextView) findViewById(R.id.dest_value);
        number_value.setText("" + packetNumber);
        ts_value.setText(ts);
        packet_type_value.setText(packet_type);
        destination_value.setText(destination);

        WebSettings webSettings = lWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        int spacesToIndentEachLevel = 2;
        String beautify = "{}";
        try {
            beautify = new JSONObject(hciPacket).toString(spacesToIndentEachLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String html = "<HTML><HEAD><link rel=\"stylesheet\" href=\"styles.css\">" +
                "<script src=\"highlight.js\"></script>" + "<script>hljs.initHighlightingOnLoad();</script>" +
                "</HEAD><body>" +
                "<pre><code class=\"json\">" +
                beautify +
                "</code></pre>" +
                "</body></HTML>";

        lWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        menu.findItem(R.id.packet_number_entry).setVisible(false);
        menu.findItem(R.id.clear_btn).setVisible(false);
        menu.findItem(R.id.scan_btn).setVisible(false);
        menu.findItem(R.id.state_bt_btn).setVisible(false);
        menu.findItem(R.id.reset_snoop_file).setVisible(false);
        menu.findItem(R.id.filter_btn).setVisible(false);
        menu.findItem(R.id.refresh).setVisible(false);

        MenuItem item = menu.findItem(R.id.share);

        if (item != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            setSharedIntent();
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Setup navigation view items
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        MenuUtils.selectDrawerItem(menuItem, mDrawer, DescriptionActivity.this, null);
                        return true;
                    }
                });
    }

    /**
     * alternate between 2 colors for the description item table
     *
     * @param alt_row
     */
    public void altTableRow(int alt_row) {
        int childViewCount = tablelayout.getChildCount();

        for (int i = 0; i < childViewCount; i++) {
            TableRow row = (TableRow) tablelayout.getChildAt(i);

            for (int j = 0; j < row.getChildCount(); j++) {

                TextView tv = (TextView) row.getChildAt(j);
                if (i % alt_row != 0) {
                    tv.setBackground(getResources().getDrawable(
                            R.drawable.alt_row_color));
                } else {
                    tv.setBackground(getResources().getDrawable(
                            R.drawable.row_color));
                }
            }
        }
    }
}