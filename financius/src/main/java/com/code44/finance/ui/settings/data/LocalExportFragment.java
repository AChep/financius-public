package com.code44.finance.ui.settings.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.code44.finance.R;
import com.code44.finance.data.backup.BackupDataExporter;
import com.code44.finance.data.backup.DataExporter;
import com.code44.finance.ui.FilePickerActivity;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;

import java.io.File;

public class LocalExportFragment extends BaseExportFragment {
    private static final String ARG_TYPE = "ARG_TYPE";

    private static final int REQUEST_DIRECTORY = 1;

    private Type type;

    public static LocalExportFragment newInstance(Type type) {
        final Bundle args = new Bundle();
        args.putSerializable(ARG_TYPE, type);

        final LocalExportFragment fragment = new LocalExportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        type = (Type) getArguments().getSerializable(ARG_TYPE);

        // Show directory selector if necessary
        if (savedInstanceState == null) {
            FilePickerActivity.startDir(this, REQUEST_DIRECTORY);
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DIRECTORY:
                if (resultCode == Activity.RESULT_OK) {
                    onDirectorySelected(new File(data.getData().getPath()));
                } else {
                    cancel();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onDirectorySelected(File directory) {
        final File file = getFile(directory);
        final DataExporter dataExporter = getDataExporter(file);
        exportData(dataExporter);
    }

    private File getFile(File directory) {
        return new File(directory, getFileTitle());
    }

    private String getFileTitle() {
        return getString(R.string.app_name) + " " + DateUtils.formatDateTime(getActivity(), new DateTime(), DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
    }

    private DataExporter getDataExporter(File file) {
        switch (type) {
            case BACKUP:
                return new BackupDataExporter(file, getActivity());
//            case CSV:
//                break;
            default:
                throw new IllegalStateException("Type " + type + " is not supported.");
        }
    }

    public static enum Type {
        BACKUP, CSV
    }
}
