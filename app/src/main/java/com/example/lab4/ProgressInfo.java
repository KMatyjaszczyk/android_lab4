package com.example.lab4;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgressInfo implements Parcelable {
    public int bytesDownloaded;
    public int totalFileSize;
    public String status;

    public ProgressInfo(int bytesDownloaded, int totalFileSize, String status) {
        this.bytesDownloaded = bytesDownloaded;
        this.totalFileSize = totalFileSize;
        this.status = status;
    }

    protected ProgressInfo(Parcel in) {
        bytesDownloaded = in.readInt();
        totalFileSize = in.readInt();
        status = in.readString();
    }

    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }

        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(bytesDownloaded);
        parcel.writeInt(totalFileSize);
        parcel.writeString(status);
    }
}
