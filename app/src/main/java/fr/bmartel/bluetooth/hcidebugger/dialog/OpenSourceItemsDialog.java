package fr.bmartel.bluetooth.hcidebugger.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ListView;

import fr.bmartel.bluetooth.hcidebugger.R;
import fr.bmartel.bluetooth.hcidebugger.adapter.OpenSourceItemAdapter;

public class OpenSourceItemsDialog extends AlertDialog {

    public OpenSourceItemsDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        ListView listview = (ListView) inflater.inflate(R.layout.open_source_list, null);
        listview.setAdapter(new OpenSourceItemAdapter(context));

        setView(listview);
        setTitle(R.string.open_source_items);
        setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
                (OnClickListener) null);
    }
}
