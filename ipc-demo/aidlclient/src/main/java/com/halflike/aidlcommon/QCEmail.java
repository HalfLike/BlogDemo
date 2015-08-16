package com.halflike.aidlcommon;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by halflike on 15/8/16.
 */
public final class QCEmail implements Parcelable {

    public String fromWho = "";
    public String toWho = "";
    public String content = "";
    public long creatTime = 0;

    public static final Creator<QCEmail> CREATOR = new
            Creator<QCEmail>() {

                @Override
                public QCEmail createFromParcel(Parcel source) {
                    return new QCEmail(source);
                }

                @Override
                public QCEmail[] newArray(int size) {
                    return new QCEmail[size];
                }
            };

    public QCEmail() {}

    private QCEmail(Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fromWho);
        dest.writeString(toWho);
        dest.writeString(content);
        dest.writeLong(creatTime);
    }

    public void readFromParcel(Parcel source) {
        fromWho = source.readString();
        toWho = source.readString();
        content = source.readString();
        creatTime = source.readInt();
    }


}
