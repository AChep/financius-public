package com.code44.finance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.code44.finance.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends BaseAdapter {
    public static final long ID_CURRENCIES = 1;
    public static final long ID_CATEGORIES = 2;

    private final Context context;
    private final List<SettingsItem> settingsItems;

    public SettingsAdapter(Context context) {
        this.context = context;
        settingsItems = new ArrayList<>();
        settingsItems.add(new SettingsItem(ID_CURRENCIES, ViewType.SettingsItem, context.getString(R.string.currencies)));
        settingsItems.add(new SettingsItem(ID_CATEGORIES, ViewType.SettingsItem, context.getString(R.string.categories)));
    }

    @Override
    public int getCount() {
        return settingsItems.size();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Object getItem(int position) {
        return settingsItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return settingsItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SettingsItem settingsItem = settingsItems.get(position);
        final ViewType viewType = settingsItem.getViewType();
        final SettingsItemViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(viewType.getLayoutId(), parent, false);
            viewHolder = viewType.createViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SettingsItemViewHolder) convertView.getTag();
        }
        viewHolder.bind(settingsItem);

        return convertView;
    }

    private static enum ViewType {
        SettingsItem(R.layout.li_settings) {
            @Override
            public SettingsItemViewHolder createViewHolder(View itemView) {
                return new SettingsItemViewHolder(itemView);
            }
        };

        private final int layoutId;

        private ViewType(int layoutId) {
            this.layoutId = layoutId;
        }

        public int getLayoutId() {
            return layoutId;
        }

        public abstract SettingsItemViewHolder createViewHolder(View itemView);
    }

    private static class SettingsItem {
        private final long id;
        private final ViewType viewType;
        private final String title;

        protected SettingsItem(long id, ViewType viewType, String title) {
            this.id = id;
            this.viewType = viewType;
            this.title = title;
        }

        public long getId() {
            return id;
        }

        public ViewType getViewType() {
            return viewType;
        }

        public String getTitle() {
            return title;
        }
    }

    private static class SettingsItemViewHolder {
        private final TextView title_TV;

        private SettingsItemViewHolder(View itemView) {
            title_TV = (TextView) itemView.findViewById(R.id.title_TV);
        }

        private void bind(SettingsItem settingsItem) {
            title_TV.setText(settingsItem.getTitle());
        }
    }
}
