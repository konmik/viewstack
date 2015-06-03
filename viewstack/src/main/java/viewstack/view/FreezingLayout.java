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
import java.util.Collections;
import java.util.List;

import viewstack.util.Lazy;

public class FreezingLayout extends FrameLayout implements FreezingContainer {

    private static final String CHILDREN_KEY = "children";
    private static final String FREEZER_KEY = "frozen";
    private static final String PARENT_KEY = "parent";

    private FreezerInflater inflater;
    private SparseArray<Parcelable> frozen = new SparseArray<>();

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
    public int getFrameId() {
        return getId();
    }

    @Override
    public View inflateTop(int frameId, int layoutId) {
        View view = inflater.inflate(this, layoutId);
        addView(view, getChildCount());
        classes.invalidate();
        return view;
    }

    @Override
    public View inflateBottom(int frameId, int layoutId) {
        View view = inflater.inflate(this, layoutId);
        addView(view, 0);
        classes.invalidate();
        return view;
    }

    @Override
    public View getView(int index) {
        return getChildAt(getChildIndex(index));
    }

    @Override
    public int size() {
        return classes.get().size();
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
        if (index < frozen.size())
            unfreeze(index);
        setVisibility(index, VISIBLE);
    }

    @Override
    public void freeze(int index) {
        frozen.put(index, inflater.freeze(getView(index)));
        removeViewAt(index);
    }

    @Override
    public void setNonPermanent(int index) {
        getView(index).setSaveFromParentEnabled(false);
        classes.invalidate();
    }

    @Override
    public void removeNonPermanent() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (!getChildAt(i).isSaveFromParentEnabled())
                removeViewAt(i);
        }
    }

    @Override
    public List<Class> getClasses() {
        return classes.get();
    }

    @Override
    public void removeFrozen() {
        frozen.clear();
        classes.invalidate();
    }

    private Lazy<List<Class>> classes = new Lazy<>(new Lazy.Factory<List<Class>>() {
        @Override
        public List<Class> call() {
            int childCount = getChildCount();
            List<Class> classes = new ArrayList<>(frozen.size() + childCount);

            for (int i = 0; i < frozen.size(); i++)
                classes.add(inflater.getClass(frozen.get(i)));

            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if (view.isSaveFromParentEnabled())
                    classes.add(view.getClass());
            }

            return Collections.unmodifiableList(classes);
        }
    });

    private void unfreeze(int index) {
        Parcelable parcelable = frozen.get(index);
        addView(inflater.unfreeze(this, parcelable), getChildIndex(index));
        frozen.remove(index);
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
        bundle.putSparseParcelableArray(FREEZER_KEY, frozen);
        bundle.putParcelable(PARENT_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_KEY));
        frozen = bundle.getSparseParcelableArray(FREEZER_KEY);
        ArrayList<Parcelable> children = bundle.getParcelableArrayList(CHILDREN_KEY);
        for (Parcelable parcelable : children)
            addView(inflater.unfreeze(this, parcelable));
        classes.invalidate();
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
