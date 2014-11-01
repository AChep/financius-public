package com.code44.finance.ui.tags;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.util.Pair;
import android.view.Menu;
import android.widget.TextView;

import com.code44.finance.R;
import com.code44.finance.data.db.Tables;
import com.code44.finance.data.model.Tag;
import com.code44.finance.data.providers.TagsProvider;
import com.code44.finance.ui.common.ModelActivity;

public class TagActivity extends ModelActivity<Tag> {
    private TextView titleTextView;

    public static void start(Context context, String tagServerId) {
        final Intent intent = makeIntent(context, TagActivity.class, tagServerId);
        startActivity(context, intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        // Get views
        titleTextView = (TextView) findViewById(R.id.titleTextView);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override protected CursorLoader getModelCursorLoader(String modelId) {
        return Tables.Tags.getQuery().asCursorLoader(this, TagsProvider.uriTag(modelId));
    }

    @Override protected Tag getModelFrom(Cursor cursor) {
        return Tag.from(cursor);
    }

    @Override protected void onModelLoaded(Tag model) {
        titleTextView.setText(model.getTitle());
    }

    @Override protected Uri getDeleteUri() {
        return TagsProvider.uriTags();
    }

    @Override protected Pair<String, String[]> getDeleteSelection() {
        return Pair.create(Tables.Tags.ID + "=?", new String[]{String.valueOf(modelId)});
    }

    @Override protected void startModelEdit(String modelId) {
        TagEditActivity.start(this, modelId);
    }
}
