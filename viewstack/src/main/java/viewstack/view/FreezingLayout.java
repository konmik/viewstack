package viewstack.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class FreezingLayout extends FrameLayout implements FreezingContainer {

    private static final String CHILDREN_KEY = "children";
    private static final String FREEZER_KEY = "freezer";
    private static final String PARENT_KEY = "parent";

    private FreezerInflater inflater;
    private SparseArray<Parcelable> freezer = new SparseArray<>();

    public FreezingLayout(Context context) {
        super(context);
    }

    public FreezingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreezingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFreezerInflater(FreezerInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View inflate(int index, int layoutId) {
        View view = inflater.inflate(layoutId);
        addView(view, index);
        return view;
    }

    @Override
    public View getView(int index) {
        for (int i = 0, size = getChildCount(); i < size; i++) {
            View view = getChildAt(i);
            if (view.isSaveFromParentEnabled() && index-- == 0)
                return view;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return freezer.size() + permanentCount();
    }

    @Override
    public void clearFrozen() {
        freezer.clear();
    }

    public int frozenCount() {
        return freezer.size();
    }

    @Override
    public boolean isFrozen(int index) {
        return index < freezer.size();
    }

    @Override
    public boolean isHidden(int index) {
        int freezerSize = freezer.size();
        int visibility = index < freezerSize ? inflater.getVisibility(freezer.get(index)) : getView(index - freezerSize).getVisibility();
        return visibility != VISIBLE;
    }

    @Override
    public void hide(int index) {

    }

    @Override
    public void show(int index) {

    }

    @Override
    public void freeze(int index, int freezerIndex) {

    }

    @Override
    public void unfreeze(int index, int freezerIndex) {

    }

    @Override
    public void setNonPermanent(int index) {
        getView(index).setSaveFromParentEnabled(false);
    }

    @Override
    public void removeNonPermanent() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (!getChildAt(i).isSaveFromParentEnabled())
                removeViewAt(i);
        }
    }

    private int permanentCount() {
        int count = 0;
        for (int i = 0, size = getChildCount(); i < size; i++)
            if (getChildAt(i).isSaveFromParentEnabled())
                count++;
        return count;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        ArrayList<Parcelable> children = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.isSaveFromParentEnabled())
                children.add(inflater.freeze(child));
        }
        bundle.putParcelableArrayList(CHILDREN_KEY, children);
        bundle.putSparseParcelableArray(FREEZER_KEY, freezer);
        bundle.putParcelable(PARENT_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_KEY));
        freezer = bundle.getSparseParcelableArray(FREEZER_KEY);

        removeAllViews();
        ArrayList<Parcelable> children = bundle.getParcelableArrayList(CHILDREN_KEY);
        for (Parcelable parcelable : children)
            addView(inflater.unfreeze(parcelable));
    }

    @Override
    protected void dispatchSaveInstanceState(@NonNull SparseArray<Parcelable> container) {
        container.put(getId(), onSaveInstanceState());
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
        onRestoreInstanceState(container.get(getId()));
    }
}
