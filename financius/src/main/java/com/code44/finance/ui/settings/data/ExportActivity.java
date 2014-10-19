package com.code44.finance.ui.settings.data;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import com.code44.finance.R;
import com.code44.finance.data.backup.BackupDataExporter;
import com.code44.finance.data.backup.CsvDataExporter;
import com.code44.finance.data.backup.DataExporter;
import com.code44.finance.data.backup.DataExporterRunnable;
import com.code44.finance.data.backup.DriveDataExporterRunnable;
import com.code44.finance.qualifiers.Local;
import com.code44.finance.ui.BaseActivity;
import com.code44.finance.ui.FilePickerActivity;
import com.code44.finance.ui.GoogleApiFragment;
import com.code44.finance.utils.GeneralPrefs;
import com.code44.finance.utils.errors.AppError;
import com.code44.finance.utils.errors.ExportError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.inject.Inject;

public class ExportActivity extends BaseActivity {
    private static final String EXTRA_EXPORT_TYPE = "EXTRA_EXPORT_TYPE";
    private static final String EXTRA_DESTINATION = "EXTRA_DESTINATION";

    private static final int REQUEST_LOCAL_DIRECTORY = 1;
    private static final int REQUEST_DRIVE_DIRECTORY = 2;

    private static final String FRAGMENT_GOOGLE_API = "FRAGMENT_GOOGLE_API";
    private static final String UNIQUE_GOOGLE_API_ID = ExportActivity.class.getName();

    @Inject @Local Executor localExecutor;
    @Inject GeneralPrefs generalPrefs;

    private ExportType exportType;
    private GoogleApiClient googleApiClient;

    public static void start(Context context, ExportType exportType, Destination destination) {
        final Intent intent = makeIntent(context, ExportActivity.class);
        intent.putExtra(EXTRA_EXPORT_TYPE, exportType);
        intent.putExtra(EXTRA_DESTINATION, destination);
        start(context, intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Get extras
        exportType = (ExportType) getIntent().getSerializableExtra(EXTRA_EXPORT_TYPE);
        final Destination destination = (Destination) getIntent().getSerializableExtra(EXTRA_DESTINATION);

        // Setup
        getEventBus().register(this);
        if (savedInstanceState == null) {
            destination.startExportProcess(this, generalPrefs);
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        switch (requestCode) {
            case REQUEST_LOCAL_DIRECTORY:
                final String path = data.getData().getPath();
                generalPrefs.setLastFileExportPath(path);
                onLocalDirectorySelected(new File(path));
                break;

            case REQUEST_DRIVE_DIRECTORY:
                final DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                // TODO Maybe store this driveId to open Google Drive in this folder next time.
                onDriveDirectorySelected(driveId);
                break;
        }

        final GoogleApiFragment googleApi_F = (GoogleApiFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_GOOGLE_API);
        if (googleApi_F != null) {
            googleApi_F.handleOnActivityResult(requestCode, resultCode);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        getEventBus().unregister(this);
    }

    @Override protected void onHandleError(AppError error) {
        super.onHandleError(error);
        if (error instanceof ExportError) {
            finish();
        }
    }

    @Subscribe public void onDataExporterFinished(DataExporter dataExporter) {
        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Subscribe public void onGoogleApiClientConnected(GoogleApiFragment.GoogleApiConnectedEvent connectedEvent) {
        if (!UNIQUE_GOOGLE_API_ID.equals(connectedEvent.getUniqueClientId())) {
            return;
        }

        googleApiClient = connectedEvent.getClient();
        final IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{"application/vnd.google-apps.folder"})
                .build(googleApiClient);

        try {
            startIntentSenderForResult(intentSender, REQUEST_DRIVE_DIRECTORY, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            throw new ExportError("Unable to show Google Drive.", e);
        }
    }

    private void onDriveDirectorySelected(DriveId driveId) {
        exportData(new DriveDataExporterRunnable(googleApiClient, driveId, exportType, this, getEventBus(), getFileTitle()));
    }

    private void onLocalDirectorySelected(File directory) {
        final File file = getFile(directory);

        final OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new ExportError("Data export has failed.", e);
        }
        final DataExporter dataExporter = exportType.getDataExporter(outputStream, this);
        exportData(new DataExporterRunnable(getEventBus(), dataExporter));
    }

    private File getFile(File directory) {
        return new File(directory, getFileTitle());
    }

    private String getFileTitle() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return getString(R.string.app_name) + " " + dateFormat.format(new Date()) + exportType.getExtension();
    }

    private void exportData(Runnable exportRunnable) {
        localExecutor.execute(exportRunnable);
    }

    public static enum ExportType {
        Backup {
            @Override public DataExporter getDataExporter(OutputStream outputStream, Context context) {
                return new BackupDataExporter(outputStream, context);
            }

            @Override public String getExtension() {
                return ".json";
            }

            @Override public String getMimeType() {
                return "application/json";
            }
        },

        CSV {
            @Override public DataExporter getDataExporter(OutputStream outputStream, Context context) {
                return new CsvDataExporter(outputStream, context);
            }

            @Override public String getExtension() {
                return ".csv";
            }

            @Override public String getMimeType() {
                return "text/csv";
            }
        };

        public abstract DataExporter getDataExporter(OutputStream outputStream, Context context);

        public abstract String getExtension();

        public abstract String getMimeType();
    }

    public static enum Destination {
        File {
            @Override public void startExportProcess(BaseActivity activity, GeneralPrefs generalPrefs) {
                FilePickerActivity.startDir(activity, REQUEST_LOCAL_DIRECTORY, generalPrefs.getLastFileExportPath());
            }
        },

        GoogleDrive {
            @Override public void startExportProcess(BaseActivity activity, GeneralPrefs generalPrefs) {
                GoogleApiFragment googleApi_F = (GoogleApiFragment) activity.getSupportFragmentManager().findFragmentByTag(FRAGMENT_GOOGLE_API);
                if (googleApi_F == null) {
                    googleApi_F = GoogleApiFragment.with(UNIQUE_GOOGLE_API_ID).setUseDrive(true).build();
                    activity.getSupportFragmentManager().beginTransaction().add(android.R.id.content, googleApi_F, FRAGMENT_GOOGLE_API).commit();
                }
                googleApi_F.connect();
            }
        };

        public abstract void startExportProcess(BaseActivity activity, GeneralPrefs generalPrefs);
    }
}
