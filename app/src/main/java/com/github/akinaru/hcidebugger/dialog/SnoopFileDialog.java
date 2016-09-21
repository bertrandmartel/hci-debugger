package com.github.akinaru.hcidebugger.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.akinaru.hcidebugger.R;
import com.github.akinaru.hcidebugger.inter.IHciDebugger;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class SnoopFileDialog extends AlertDialog {

    private EditText mSnoopFileEditText;

    private IHciDebugger mActivity;

    public SnoopFileDialog(final IHciDebugger activity) {
        super(activity.getContext());

        mActivity = activity;

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.snoopfile_dialog, null);
        setView(dialoglayout);

        mSnoopFileEditText = (EditText) dialoglayout.findViewById(R.id.snoop_file_edit);
        mSnoopFileEditText.setText("" + activity.getBtSnoopFilePath());
        mSnoopFileEditText.setSelection(mSnoopFileEditText.getText().length());

        setTitle(R.string.configuration_snoopfile_title);

        setButton(DialogInterface.BUTTON_POSITIVE, activity.getContext().getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, activity.getContext().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        Button browseBtn = (Button) dialoglayout.findViewById(R.id.browse_file);

        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File("/");
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;
                FilePickerDialog dialog = new FilePickerDialog(getContext(), properties);
                dialog.setTitle("Select a File");
                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {

                        if (files.length > 0) {
                            mSnoopFileEditText.setText(files[0]);
                        }
                    }
                });
                dialog.show();
            }
        });

        Button defaultPathBtn = (Button) dialoglayout.findViewById(R.id.default_btsnoop_path);

        defaultPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSnoopFileEditText.setText(activity.getDefaultBtSnoopPath());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.setBtSnoopFilePath(mSnoopFileEditText.getText().toString());
                dismiss();
            }
        });

    }

}