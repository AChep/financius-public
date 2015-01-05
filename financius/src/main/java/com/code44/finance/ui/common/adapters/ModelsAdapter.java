package com.code44.finance.ui.common.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.code44.finance.data.model.Model;
import com.code44.finance.ui.common.presenters.ModelsPresenter;

import java.util.HashSet;
import java.util.Set;

public abstract class ModelsAdapter<M extends Model> extends RecyclerView.Adapter<ModelsAdapter.ViewHolder<M>> {
    private final Set<M> selectedModels = new HashSet<>();
    private final OnModelClickListener<M> onModelClickListener;

    private Cursor cursor;
    private ModelsPresenter.Mode mode = ModelsPresenter.Mode.View;

    public ModelsAdapter(OnModelClickListener<M> onModelClickListener) {
        this.onModelClickListener = onModelClickListener;
    }

    @Override public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override public ViewHolder<M> onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder<M> viewHolder = createModelViewHolder(parent, viewType);
        viewHolder.setOnModelClickListener(onModelClickListener);
        return viewHolder;
    }

    @Override public void onBindViewHolder(ViewHolder<M> holder, int position) {
        cursor.moveToPosition(position);
        final M model = modelFromCursor(cursor);
        holder.bindViewHolder(model, cursor, position, mode, mode != ModelsPresenter.Mode.View && selectedModels.contains(model));
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        if (cursor != null) {
            cursor.moveToFirst();
        }
        notifyDataSetChanged();
    }

    public void setMode(ModelsPresenter.Mode mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public void toggleModelSelected(M model) {
        if (!selectedModels.add(model)) {
            selectedModels.remove(model);
        }
        notifyDataSetChanged();
    }

    public Set<M> getSelectedModels() {
        return selectedModels;
    }

    public void setSelectedModels(Set<M> selectedModels) {
        this.selectedModels.clear();
        if (selectedModels != null) {
            this.selectedModels.addAll(selectedModels);
        }
        notifyDataSetChanged();
    }

    protected abstract ViewHolder<M> createModelViewHolder(ViewGroup parent, int viewType);

    protected abstract M modelFromCursor(Cursor cursor);

    public static interface OnModelClickListener<M extends Model> {
        public void onModelClick(View view, M model, Cursor cursor, int position, ModelsPresenter.Mode mode, boolean isSelected);
    }

    public static abstract class ViewHolder<M extends Model> extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnModelClickListener<M> onModelClickListener;

        private M model;
        private Cursor cursor;
        private int position;
        private ModelsPresenter.Mode mode;
        private boolean isSelected;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindViewHolder(M model, Cursor cursor, int position, ModelsPresenter.Mode mode, boolean isSelected) {
            this.model = model;
            this.cursor = cursor;
            this.position = position;
            this.mode = mode;
            this.isSelected = isSelected;
            bind(model, cursor, position, mode, isSelected);
        }

        protected abstract void bind(M model, Cursor cursor, int position, ModelsPresenter.Mode mode, boolean isSelected);

        private void setOnModelClickListener(OnModelClickListener<M> onModelClickListener) {
            this.onModelClickListener = onModelClickListener;
            if (onModelClickListener != null) {
                itemView.setOnClickListener(this);
            } else {
                itemView.setOnClickListener(null);
            }
        }

        @Override public void onClick(View v) {
            cursor.moveToPosition(position);
            onModelClickListener.onModelClick(v, model, cursor, position, mode, isSelected);
        }
    }
}
