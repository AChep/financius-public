package com.code44.finance.api.requests;

import android.content.ContentValues;
import android.net.Uri;

import com.code44.finance.api.Request;
import com.code44.finance.api.User;
import com.code44.finance.data.DataStore;
import com.code44.finance.data.db.model.BaseModel;
import com.google.api.client.json.GenericJson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class GetRequest<T extends GenericJson> extends Request {
    @Inject User user;

    @Override protected void performRequest() throws Exception {
        long timestamp = getLastTimestamp(user);
        final List<T> entities = performRequest(timestamp);
        final List<ContentValues> valuesList = new ArrayList<>();
        for (T entity : entities) {
            final BaseModel model = getModelFrom(entity);
            final ContentValues values = model.asContentValues();
            onValuesCreated(values);
            valuesList.add(values);
            final long entityEditTimestamp = (Long) entity.get("edit_ts");
            if (timestamp < entityEditTimestamp) {
                timestamp = entityEditTimestamp;
            }
        }

        DataStore.bulkInsert().values(valuesList).into(getSaveUri());
        saveNewTimestamp(user, timestamp);
    }

    protected abstract long getLastTimestamp(User user);

    protected abstract List<T> performRequest(long timestamp) throws Exception;

    protected abstract BaseModel getModelFrom(T entity);

    protected abstract void saveNewTimestamp(User user, long newTimestamp);

    protected abstract Uri getSaveUri();

    protected void onValuesCreated(ContentValues values) {
    }
}
