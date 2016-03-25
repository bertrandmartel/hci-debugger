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
package com.github.akinaru.hcidebugger.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.common.Constants;
import com.github.akinaru.hcidebugger.inter.IHciDebugger;

/**
 * Max packet count dialog
 *
 * @author Bertrand Martel
 */
public class MaxPacketCountDialog extends AlertDialog {

    public MaxPacketCountDialog(final IHciDebugger activity) {
        super(activity.getContext());

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.last_packet_count_dialog, null);
        setView(dialoglayout);

        final EditText last_packet_val = (EditText) dialoglayout.findViewById(R.id.last_packet_count_value);

        SharedPreferences prefs = activity.getContext().getSharedPreferences(Constants.PREFERENCES, activity.getContext().MODE_PRIVATE);
        last_packet_val.setText("" + prefs.getInt(Constants.PREFERENCES_MAX_PACKET_COUNT, Constants.DEFAULT_LAST_PACKET_COUNT));

        setTitle(R.string.last_packet_count);
        setButton(DialogInterface.BUTTON_POSITIVE, activity.getContext().getResources().getString(R.string.dialog_ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int maxPacketValue = Integer.parseInt(last_packet_val.getText().toString());
                SharedPreferences.Editor editor = activity.getContext().getSharedPreferences(Constants.PREFERENCES, activity.getContext().MODE_PRIVATE).edit();
                editor.putInt(Constants.PREFERENCES_MAX_PACKET_COUNT, maxPacketValue);
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