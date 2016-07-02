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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.akinaru.hcidebugger.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Abstract base activity for all activities in HCI Debugger app
 *
 * @author Bertrand Martel
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar = null;

    /**
     * navigation drawer
     */
    protected DrawerLayout mDrawer = null;

    protected ActionBarDrawerToggle drawerToggle;

    /**
     * navigation view
     */
    protected NavigationView nvDrawer;

    /**
     * activity layout
     */
    protected int layoutId;

    /**
     * define activity layout (called before super.onCreate())
     *
     * @param layoutId layout ressource id
     */
    protected void setLayout(int layoutId) {
        this.layoutId = layoutId;
    }


    /**
     * Retrieve btsnoop file absolute path from bt_stack.conf file
     *
     * @return btsnoop file absolute path
     */
    public String getHciLogFilePath() {

        try {
            FileInputStream fis = new FileInputStream(getResources().getString(R.string.bluetooth_config));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (line.contains(getResources().getString(R.string.bt_config_file_name_filter))) {
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        // setup toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_item);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.drawer_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        //setup navigation view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.setDrawerListener(drawerToggle);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
    }

    /**
     * setup open/close action for navigation view
     *
     * @return
     */
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    /**
     * necessary for the hamburger button
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    /**
     * close the drawer if open when back is pressed
     */
    @Override
    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
