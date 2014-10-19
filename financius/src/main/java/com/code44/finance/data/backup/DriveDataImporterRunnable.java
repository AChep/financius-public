package com.code44.finance.data.backup;

import android.content.Context;

import com.code44.finance.data.db.DBHelper;
import com.code44.finance.ui.settings.data.ImportActivity;
import com.code44.finance.utils.EventBus;
import com.code44.finance.utils.errors.ImportError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

public class DriveDataImporterRunnable implements Runnable {
    private final GoogleApiClient googleApiClient;
    private final DriveId driveId;
    private final ImportActivity.ImportType importType;
    private final Context context;
    private final DBHelper dbHelper;
    private final EventBus eventBus;

    public DriveDataImporterRunnable(GoogleApiClient googleApiClient, DriveId driveId, ImportActivity.ImportType importType, Context context, DBHelper dbHelper, EventBus eventBus) {
        this.googleApiClient = googleApiClient;
        this.driveId = driveId;
        this.importType = importType;
        this.context = context;
        this.dbHelper = dbHelper;
        this.eventBus = eventBus;
    }

    @Override public void run() {
        final DriveFile driveFile = Drive.DriveApi.getFile(googleApiClient, driveId);
        final DriveApi.ContentsResult result = driveFile.openContents(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();

        if (!result.getStatus().isSuccess()) {
            throw new ImportError("Import failed.");
        }

        final Contents contents = result.getContents();
        final DataImporterRunnable dataImporterRunnable = new DataImporterRunnable(eventBus, importType.getDataImporter(contents.getInputStream(), context, dbHelper));
        dataImporterRunnable.run();
        driveFile.discardContents(googleApiClient, contents);
    }
}
