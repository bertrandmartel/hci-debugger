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
package com.github.akinaru.hcidebugger.menu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.dialog.AboutDialog;
import com.github.akinaru.hcidebugger.dialog.MaxPacketCountDialog;
import com.github.akinaru.hcidebugger.dialog.OpenSourceItemsDialog;
import com.github.akinaru.hcidebugger.inter.IHciDebugger;

/**
 * Some functions used to manage Menu
 *
 * @author Bertrand Martel
 */
public class MenuUtils {

    /**
     * Execute actions according to selected menu item
     *
     * @param menuItem MenuItem object
     * @param mDrawer  navigation drawer
     */
    public static void selectDrawerItem(MenuItem menuItem, DrawerLayout mDrawer, Context context, IHciDebugger activity) {

        switch (menuItem.getItemId()) {
            case R.id.set_max_packet_num: {
                if (activity != null) {
                    MaxPacketCountDialog dialog = new MaxPacketCountDialog(activity);
                    dialog.show();
                }
                break;
            }
            case R.id.report_bugs: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", context.getResources().getString(R.string.developper_mail), null));
                intent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.issue_object));
                intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.issue_message));
                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.issue_title)));
                break;
            }
            case R.id.scan_btn_nv: {
                activity.toggleScan(menuItem);
                break;
            }
            case R.id.reset_snoop_file_nv: {
                activity.resetSnoopFile();
                activity.refresh();
                break;
            }
            case R.id.state_bt_btn_nv: {
                activity.toggleBtState();
                break;
            }
            case R.id.open_source_components: {
                OpenSourceItemsDialog d = new OpenSourceItemsDialog(context);
                d.show();
                break;
            }
            case R.id.about_app: {
                AboutDialog dialog = new AboutDialog(context);
                dialog.show();
                break;
            }
        }
        mDrawer.closeDrawers();
    }
}
