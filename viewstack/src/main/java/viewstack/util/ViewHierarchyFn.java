package viewstack.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;

public class ViewHierarchyFn {
    public static byte[] freezeHierarchy(SparseArray<Parcelable> hierarchy) {
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(hierarchy.size());
        for (int i = 0; i < hierarchy.size(); i++) {
            parcel.writeInt(hierarchy.keyAt(i));
            parcel.writeParcelable(hierarchy.valueAt(i), 0);
        }
        byte[] r = parcel.marshall();
        parcel.recycle();
        return r;
    }

    public static SparseArray<Parcelable> unfreezeHierarchy(byte[] bytes) {
        SparseArray<Parcelable> hierarchy = new SparseArray<>();
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        int size = parcel.readInt();
        for (int i = 0; i < size; i++)
            hierarchy.put(parcel.readInt(), parcel.readParcelable(null));
        parcel.recycle();
        return hierarchy;
    }

    public static SparseArray<Parcelable> getHierarchy(View view) {
        SparseArray<Parcelable> states = new SparseArray<>();
        view.saveHierarchyState(states);
        return states;
    }
}
