/**
 * This file is part of RFdroid.
 * <p/>
 * Copyright (C) 2016  Bertrand Martel
 * <p/>
 * Foobar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.bmartel.bluetooth.hcidebugger.menu;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import fr.bmartel.bluetooth.hcidebugger.IHciDebugger;
import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.dialog.AboutDialog;
import fr.bmartel.bluetooth.hcidebugger.dialog.LastPacketCountDialog;
import fr.bmartel.bluetooth.hcidebugger.dialog.OpenSourceItemsDialog;

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
            case R.id.set_last_packet_num: {
                if (activity != null) {
                    LastPacketCountDialog dialog = new LastPacketCountDialog(activity);
                    dialog.show();
                }
                break;
            }
            case R.id.report_bugs: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "kiruazoldik92@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "HCI Debugger Issue");
                intent.putExtra(Intent.EXTRA_TEXT, "Your error report here...");
                context.startActivity(Intent.createChooser(intent, "Report a problem"));
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
