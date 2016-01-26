package fr.bmartel.bluetooth.hcidebugger;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

/**
 * @author Bertrand Martel
 */
public class HciApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }

}
