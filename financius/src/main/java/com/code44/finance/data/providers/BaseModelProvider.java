package com.code44.finance.data.providers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.code44.finance.data.db.Tables;
import com.code44.finance.data.db.model.BaseModel;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedParameters")
public abstract class BaseModelProvider extends BaseProvider {
    private static final int URI_ITEMS = 1;
    private static final int URI_ITEMS_ID = 2;

    public static Uri uriModels(Class<? extends BaseModelProvider> providerClass, String modelTable) {
        return Uri.parse(CONTENT_URI_BASE + getAuthority(providerClass) + "/" + modelTable);
    }

    public static Uri uriModel(Class<? extends BaseModelProvider> providerClass, String modelTable, long modelId) {
        return ContentUris.withAppendedId(uriModels(providerClass, modelTable), modelId);
    }

    @Override
    public boolean onCreate() {
        final boolean result = super.onCreate();

        final String authority = getAuthority();
        final String mainTable = getModelTable();
        uriMatcher.addURI(authority, mainTable, URI_ITEMS);
        uriMatcher.addURI(authority, mainTable + "/#", URI_ITEMS_ID);

        return result;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                return TYPE_LIST_BASE + getModelTable();
            case URI_ITEMS_ID:
                return TYPE_ITEM_BASE + getModelTable();
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor cursor;

        final int uriId = uriMatcher.match(uri);
        switch (uriId) {
            case URI_ITEMS:
                cursor = queryItems(projection, selection, selectionArgs, sortOrder);
                break;

            case URI_ITEMS_ID:
                cursor = queryItem(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        final Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        cursor.moveToFirst();

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long newId;
        final int uriId = uriMatcher.match(uri);
        switch (uriId) {
            case URI_ITEMS:
                final Map<String, Object> extras = new HashMap<>();
                onBeforeInsertItem(uri, values, extras);
                newId = insertItem(uri, values);
                onAfterInsertItem(uri, values, extras);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        ProviderUtils.notifyChangeIfNecessary(getContext(), uri);
        ProviderUtils.notifyUris(getContext(), getOtherUrisToNotify());

        return ContentUris.withAppendedId(uri, newId);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        final int uriId = uriMatcher.match(uri);
        switch (uriId) {
            case URI_ITEMS:
                final Map<String, Object> extras = new HashMap<>();
                onBeforeUpdateItems(uri, values, selection, selectionArgs, extras);
                count = updateItems(uri, values, selection, selectionArgs);
                onAfterUpdateItems(uri, values, selection, selectionArgs, extras);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        ProviderUtils.notifyChangeIfNecessary(getContext(), uri);
        ProviderUtils.notifyUris(getContext(), getOtherUrisToNotify());

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        final int uriId = uriMatcher.match(uri);
        switch (uriId) {
            case URI_ITEMS:
                final String deleteMode = uri.getQueryParameter(ProviderUtils.QueryParameterKey.DELETE_MODE.getKeyName());
                if (TextUtils.isEmpty(deleteMode)) {
                    throw new IllegalArgumentException("Uri " + uri + " must have query parameter " + ProviderUtils.QueryParameterKey.DELETE_MODE.getKeyName());
                }

                final BaseModel.ItemState itemState;
                switch (deleteMode) {
                    case "undo":
                        itemState = BaseModel.ItemState.DELETED_UNDO;
                        break;
                    case "commit":
                        itemState = BaseModel.ItemState.DELETED;
                        break;
                    default:
                        throw new IllegalArgumentException(ProviderUtils.QueryParameterKey.DELETE_MODE.getKeyName() + "=" + deleteMode + " is not supported.");
                }

                final Map<String, Object> extras = new HashMap<>();
                onBeforeDeleteItems(uri, selection, selectionArgs, itemState, extras);
                count = deleteItems(uri, selection, selectionArgs, itemState);
                onAfterDeleteItems(uri, selection, selectionArgs, itemState, extras);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        ProviderUtils.notifyChangeIfNecessary(getContext(), uri);
        ProviderUtils.notifyUris(getContext(), getOtherUrisToNotify());

        return count;
    }

    @Override
    public int bulkInsert(Uri uri, @SuppressWarnings("NullableProblems") ContentValues[] valuesArray) {
        int count;
        final int uriId = uriMatcher.match(uri);
        switch (uriId) {
            case URI_ITEMS:
                final Map<String, Object> extras = new HashMap<>();
                onBeforeBulkInsertItems(uri, valuesArray, extras);
                count = bulkInsertItems(uri, valuesArray);
                onAfterBulkInsertItems(uri, valuesArray, extras);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        ProviderUtils.notifyChangeIfNecessary(getContext(), uri);
        ProviderUtils.notifyUris(getContext(), getOtherUrisToNotify());

        return count;
    }

    protected abstract String getModelTable();

    protected abstract String getQueryTables();

    protected Cursor queryItems(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(getQueryTables());

        return qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
    }

    protected Cursor queryItem(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(getQueryTables());
        //noinspection ConstantConditions
        qb.appendWhere(getModelTable() + "." + BaseColumns._ID + "=" + uri.getPathSegments().get(1));

        return qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
    }

    protected void onBeforeInsertItem(Uri uri, ContentValues values, Map<String, Object> outExtras) {
    }

    protected long insertItem(Uri uri, ContentValues values) {
        return ProviderUtils.doUpdateOrInsert(database, getModelTable(), values, true);
    }

    protected void onAfterInsertItem(Uri uri, ContentValues values, Map<String, Object> extras) {
    }

    protected void onBeforeUpdateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs, Map<String, Object> outExtras) {
    }

    protected int updateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(getModelTable(), values, selection, selectionArgs);
    }

    protected void onAfterUpdateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs, Map<String, Object> extras) {
    }

    protected void onBeforeDeleteItems(Uri uri, String selection, String[] selectionArgs, BaseModel.ItemState itemState, Map<String, Object> outExtras) {
    }

    protected int deleteItems(Uri uri, String selection, String[] selectionArgs, BaseModel.ItemState itemState) {
        final ContentValues values = new ContentValues();
        values.put(getModelTable() + "_" + Tables.SUFFIX_ITEM_STATE, itemState.asInt());
        values.put(getModelTable() + "_" + Tables.SUFFIX_SYNC_STATE, BaseModel.SyncState.LOCAL_CHANGES.asInt());

        return database.update(getModelTable(), values, selection, selectionArgs);
    }

    protected void onAfterDeleteItems(Uri uri, String selection, String[] selectionArgs, BaseModel.ItemState itemState, Map<String, Object> extras) {
    }

    protected void onBeforeBulkInsertItems(Uri uri, ContentValues[] valuesArray, Map<String, Object> outExtras) {
    }

    protected int bulkInsertItems(Uri uri, ContentValues[] valuesArray) {
        return ProviderUtils.doArrayReplaceInTransaction(database, getModelTable(), valuesArray);
    }

    protected void onAfterBulkInsertItems(Uri uri, ContentValues[] valuesArray, Map<String, Object> extras) {
    }

    protected Uri[] getOtherUrisToNotify() {
        return null;
    }
}
