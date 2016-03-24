package fr.bmartel.bluetooth.hcidebugger.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import fr.bmartel.bluetooth.hcidebugger.IHciDebugger;
import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.common.Constants;

public class LastPacketCountDialog extends AlertDialog {

    public LastPacketCountDialog(final IHciDebugger activity) {
        super(activity.getContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.last_packet_count_dialog, null);
        setView(dialoglayout);

        final EditText last_packet_val = (EditText) dialoglayout.findViewById(R.id.last_packet_count_value);

        SharedPreferences prefs = activity.getContext().getSharedPreferences(Constants.PREFERENCES, activity.getContext().MODE_PRIVATE);
        last_packet_val.setText("" + prefs.getInt("lastPacketCount", Constants.DEFAULT_LAST_PACKET_COUNT));

        setTitle(R.string.last_packet_count);
        setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int maxPacketValue = Integer.parseInt(last_packet_val.getText().toString());
                SharedPreferences.Editor editor = activity.getContext().getSharedPreferences(Constants.PREFERENCES, activity.getContext().MODE_PRIVATE).edit();
                editor.putInt("lastPacketCount", maxPacketValue);
                editor.commit();
                activity.refresh();
                activity.setMaxPacketValue(maxPacketValue);
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }
}