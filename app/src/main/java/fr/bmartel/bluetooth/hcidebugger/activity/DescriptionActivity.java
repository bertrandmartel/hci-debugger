package fr.bmartel.bluetooth.hcidebugger.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import fr.bmartel.bluetooth.hcidebugger.MyWebClient;
import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.menu.MenuUtils;

/**
 * Created by akinaru on 24/03/16.
 */
public class DescriptionActivity extends BaseActivity {

    private TableLayout tablelayout;

    protected void onCreate(Bundle savedInstanceState) {

        setLayout(R.layout.description_activity);
        super.onCreate(savedInstanceState);

        setupDrawerContent(nvDrawer);

        nvDrawer.getMenu().findItem(R.id.set_last_packet_num).setVisible(false);

        String hciPacket = getIntent().getExtras().getString("hci_packet");
        String snoopPacket = getIntent().getExtras().getString("snoop_packet");
        int packetNumber = getIntent().getExtras().getInt("packet_number");
        String ts = getIntent().getExtras().getString("packet_ts");
        String packet_type = getIntent().getExtras().getString("packet_type");
        String destination = getIntent().getExtras().getString("packet_dest");

        tablelayout = (TableLayout) findViewById(R.id.tablelayout);

        altTableRow(2);

        WebView lWebView = (WebView) findViewById(R.id.webView);
        lWebView.setWebChromeClient(new MyWebClient());

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
        String hciJsonBeautified = "{}";
        try {
            hciJsonBeautified = new JSONObject(hciPacket).toString(spacesToIndentEachLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String html = "<HTML><HEAD><link rel=\"stylesheet\" href=\"styles.css\">" +
                "<script src=\"highlight.js\"></script>" + "<script>hljs.initHighlightingOnLoad();</script>" +
                "</HEAD><body>" +
                "<pre><code class=\"json\">" +
                hciJsonBeautified +
                "</code></pre>" +
                "</body></HTML>";

        lWebView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
    }


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