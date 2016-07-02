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
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.akinaru.hcidebugger.BuildConfig;
import com.github.akinaru.hcidebugger.R;

/**
 * About dialog
 *
 * @author Bertrand Martel
 */
public class AboutDialog extends AlertDialog {

    public AboutDialog(Context context) {
        super(context);

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.about_dialog, null);
        setView(dialoglayout);

        TextView name = (TextView) dialoglayout.findViewById(R.id.name);
        TextView copyright = (TextView) dialoglayout.findViewById(R.id.copyright);
        TextView github_link = (TextView) dialoglayout.findViewById(R.id.github_link);

        name.setText(context.getResources().getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        copyright.setText(R.string.copyright);
        github_link.setText(R.string.github_link);

        setTitle(R.string.about);
        setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.dialog_ok),
                (OnClickListener) null);
    }
}