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
    private static final String FREEZER_KEY = "frozen";
    private static final String PARENT_KEY = "parent";

    private FreezerInflater inflater;
    private ArrayList<Parcelable> frozen = new ArrayList<>();

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
        addView(view, getChildIndex(index));
        return view;
    }

    @Override
    public View getView(int index) {
        return getChildAt(getChildIndex(index));
    }

    @Override
    public int frozenCount() {
        return frozen.size();
    }

    @Override
    public int size() {
        return frozenCount() + permanentCount();
    }

    @Override
    public boolean isFrozen(int index) {
        return index < frozen.size();
    }

    @Override
    public boolean isHidden(int index) {
        int freezerSize = frozen.size();
        int visibility = index < freezerSize ? inflater.getVisibility(frozen.get(index)) : getView(index - freezerSize).getVisibility();
        return visibility != VISIBLE;
    }

    @Override
    public void hide(int index) {
        setVisibility(index, GONE);
    }

    @Override
    public void show(int index) {
        setVisibility(index, VISIBLE);
    }

    @Override
    public void freeze(int index, int freezerIndex) {
        frozen.add(freezerIndex, inflater.freeze(getView(index)));
        removeViewAt(index);
    }

    @Override
    public void unfreeze(int index, int freezerIndex) {
        addView(inflater.unfreeze(frozen.remove(freezerIndex)), getChildIndex(index));
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

    @Override
    public void removeFrozen() {
        frozen.clear();
    }

    private int getChildIndex(int index) {
        for (int i = 0, size = getChildCount(); index >= 0; i++) {
            if ((i >= size || getChildAt(i).isSaveFromParentEnabled()) && index-- == 0)
                return i;
        }
        throw new IndexOutOfBoundsException();
    }

    private void setVisibility(int index, int visibility) {
        int freezerSize = frozen.size();
        if (index < freezerSize)
            inflater.setVisibility(frozen.get(index), visibility);
        else
            getView(index - freezerSize).setVisibility(visibility);
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
        bundle.putParcelableArrayList(FREEZER_KEY, frozen);
        bundle.putParcelable(PARENT_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_KEY));
        frozen = bundle.getParcelableArrayList(FREEZER_KEY);
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
