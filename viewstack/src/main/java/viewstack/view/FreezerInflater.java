package viewstack.view;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.android15.viewstack.R;

// TODO: keep serialized parcels to prevent parceled data modification during runtime
public class FreezerInflater {
    private static final String STATE_KEY = "state";
    private static final String LAYOUT_ID_KEY = "layout_id";
    private static final String CLASS_KEY = "class";
    private static final String VISIBILITY_KEY = "visibility";

    private ViewGroup viewGroup;
    private LayoutInflater inflater;

    public FreezerInflater(ViewGroup viewGroup, LayoutInflater inflater) {
        this.viewGroup = viewGroup;
        this.inflater = inflater;
    }

    public View inflate(int layoutId) {
        View view = inflater.inflate(layoutId, viewGroup, false);
        view.setTag(R.id.view_layout_id, layoutId);
        return view;
    }

    public Parcelable freeze(View view) {
        SparseArray<Parcelable> states = new SparseArray<>();
        view.saveHierarchyState(states);
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(STATE_KEY, states);
        bundle.putInt(LAYOUT_ID_KEY, (int)view.getTag(R.id.view_layout_id));
        bundle.putSerializable(CLASS_KEY, view.getClass());
        bundle.putInt(VISIBILITY_KEY, view.getVisibility());
        return bundle;
    }

    public View unfreeze(Parcelable parcelable) {
        Bundle bundle = (Bundle)parcelable;
        View view = inflate(bundle.getInt(LAYOUT_ID_KEY));
        view.restoreHierarchyState(bundle.getSparseParcelableArray(STATE_KEY));
        //noinspection ResourceType
        view.setVisibility(bundle.getInt(VISIBILITY_KEY));
        return view;
    }

    public int getVisibility(Parcelable parcelable) {
        return ((Bundle)parcelable).getInt(VISIBILITY_KEY);
    }

    public Class<? extends View> getClass(Parcelable parcelable) {
        //noinspection unchecked
        return (Class<? extends View>)((Bundle)parcelable).getSerializable(CLASS_KEY);
    }
}
