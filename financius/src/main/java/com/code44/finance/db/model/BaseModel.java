package com.code44.finance.db.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class BaseModel implements Parcelable {
    private long id;
    private ItemState itemState;
    private SyncState syncState;

    protected BaseModel() {
        setId(0);
        setItemState(ItemState.NORMAL);
        setSyncState(SyncState.NONE);
    }

    protected BaseModel(Parcel in) {
        setId(in.readLong());
        setItemState(ItemState.fromInt(in.readInt()));
        setSyncState(SyncState.fromInt(in.readInt()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeInt(getItemState().asInt());
        dest.writeInt(getSyncState().asInt());
    }

    public void checkValues() throws IllegalStateException {
        if (itemState == null) {
            throw new IllegalStateException("ItemState cannot be null.");
        }

        if (syncState == null) {
            throw new IllegalStateException("SyncState cannot be null.");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ItemState getItemState() {
        return itemState;
    }

    public void setItemState(ItemState itemState) {
        this.itemState = itemState;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    public static enum ItemState {
        NORMAL(ItemState.VALUE_NORMAL), DELETED(ItemState.VALUE_DELETED), DELETED_UNDO(ItemState.VALUE_DELETED_UNDO);

        private static final int VALUE_NORMAL = 1;
        private static final int VALUE_DELETED = 2;
        private static final int VALUE_DELETED_UNDO = 3;

        private final int value;

        private ItemState(int value) {
            this.value = value;
        }

        public static ItemState fromInt(int value) {
            switch (value) {
                case VALUE_NORMAL:
                    return NORMAL;

                case VALUE_DELETED:
                    return DELETED;

                case VALUE_DELETED_UNDO:
                    return DELETED_UNDO;

                default:
                    throw new IllegalArgumentException("Value " + value + " is not supported.");
            }
        }

        public int asInt() {
            return value;
        }
    }

    public static enum SyncState {
        NONE(SyncState.VALUE_NONE), IN_PROGRESS(SyncState.VALUE_IN_PROGRESS), SYNCED(SyncState.VALUE_SYNCED), LOCAL_CHANGES(SyncState.VALUE_LOCAL_CHANGES);

        private static final int VALUE_NONE = 1;
        private static final int VALUE_IN_PROGRESS = 2;
        private static final int VALUE_SYNCED = 3;
        private static final int VALUE_LOCAL_CHANGES = 4;

        private final int value;

        private SyncState(int value) {
            this.value = value;
        }

        public static SyncState fromInt(int value) {
            switch (value) {
                case VALUE_NONE:
                    return NONE;

                case VALUE_IN_PROGRESS:
                    return IN_PROGRESS;

                case VALUE_SYNCED:
                    return SYNCED;

                case VALUE_LOCAL_CHANGES:
                    return LOCAL_CHANGES;

                default:
                    throw new IllegalArgumentException("Value " + value + " is not supported.");
            }
        }

        public int asInt() {
            return value;
        }
    }
}
