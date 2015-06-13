package viewstack.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import viewstack.util.ViewHierarchyFn;

import static java.util.Collections.unmodifiableList;

public class FreezingLayout extends FrameLayout implements FreezingViewGroup {

    private static final String CHILDREN_KEY = "children";
    private static final String PARENT_KEY = "parent";

    private static class ViewStateImpl implements ViewState, Parcelable {
        private FreezingLayout freezingLayout;

        private int layoutId;
        private Class viewClass;
        private int visibility;

        private View view;
        private byte[] frozen;
        private byte[] state;

        public ViewStateImpl(FreezingLayout freezingLayout, View view, int layoutId) {
            this.freezingLayout = freezingLayout;
            this.layoutId = layoutId;
            viewClass = view.getClass();
            visibility = view.getVisibility();
            this.view = view;
        }

        @Override
        public View getView() {
            return view;
        }

        @Override
        public Class getViewClass() {
            return viewClass;
        }

        @Override
        public boolean isFrozen() {
            return frozen != null;
        }

        @Override
        public void freeze() {
            frozen = ViewHierarchyFn.freezeHierarchy(ViewHierarchyFn.getHierarchy(view));
            freezingLayout.removeView(view);
        }

        @Override
        public void unfreeze() {
            view = freezingLayout.inflateAdd(layoutId, ViewHierarchyFn.unfreezeHierarchy(frozen), true);
            frozen = null;
            view.setVisibility(visibility);
        }

        @Override
        public boolean isHidden() {
            return visibility != VISIBLE;
        }

        @Override
        public void hide() {
            visibility = GONE;
            view.setVisibility(GONE);
        }

        @Override
        public void show() {
            visibility = VISIBLE;
            view.setVisibility(VISIBLE);
        }

        @Override
        public void setNonPermanent() {
            freezingLayout.setNonPermanent(this);
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(layoutId);
            dest.writeSerializable(viewClass);
            dest.writeInt(visibility);
            dest.writeByteArray(frozen);
            dest.writeByteArray(view == null ? null : ViewHierarchyFn.freezeHierarchy(ViewHierarchyFn.getHierarchy(view)));
        }

        protected ViewStateImpl(Parcel in) {
            this.layoutId = in.readInt();
            this.viewClass = (Class)in.readSerializable();
            this.visibility = in.readInt();
            this.frozen = in.createByteArray();
            this.state = in.createByteArray();
        }

        private void onRestoreInstanceState(FreezingLayout freezingLayout) {
            this.freezingLayout = freezingLayout;
            if (state != null) {
                view = freezingLayout.inflateAdd(layoutId, ViewHierarchyFn.unfreezeHierarchy(state), true);
                view.setVisibility(visibility);
                state = null;
            }
        }

        public static final Creator<ViewStateImpl> CREATOR = new Creator<ViewStateImpl>() {
            public ViewStateImpl createFromParcel(Parcel source) {
                return new ViewStateImpl(source);
            }

            public ViewStateImpl[] newArray(int size) {
                return new ViewStateImpl[size];
            }
        };
    }

    private ArrayList<ViewState> states = new ArrayList<>();

    public FreezingLayout(Context context) {
        super(context);
    }

    public FreezingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreezingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private View inflateAdd(int layoutId, SparseArray<Parcelable> state, boolean visualTop) {
        View view = LayoutInflater.from(getContext()).inflate(layoutId, this, false);
        if (state != null)
            view.restoreHierarchyState(state);
        addView(view, visualTop ? getChildCount() : 0);
        return view;
    }

    @Override
    public int getFrameId() {
        return getId();
    }

    @Override
    public ViewState inflate(int frameId, int layoutId, boolean visualTop) {
        View view = inflateAdd(layoutId, null, visualTop);
        ViewStateImpl state = new ViewStateImpl(this, view, layoutId);
        states.add(state);
        return state;
    }

    @Override
    public List<ViewState> getViewStates() {
        return unmodifiableList(states);
    }

    @Override
    public void removeNonPermanent() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View childAt = getChildAt(i);
            boolean found = false;
            for (ViewState state : states) {
                if (state.getView() == childAt) {
                    found = true;
                    break;
                }
            }
            if (!found)
                removeViewAt(i);
        }
    }

    private void setNonPermanent(ViewStateImpl viewState) {
        states.remove(viewState);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        ArrayList<Parcelable> parcelables = new ArrayList<>(states.size());
        for (ViewState state : states)
            parcelables.add((Parcelable)state);
        bundle.putParcelableArrayList(CHILDREN_KEY, parcelables);
        bundle.putParcelable(PARENT_KEY, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable(PARENT_KEY));
        ArrayList<Parcelable> parcelables = bundle.getParcelableArrayList(CHILDREN_KEY);
        for (Parcelable parcelable : parcelables) {
            ViewStateImpl s = (ViewStateImpl)parcelable;
            s.onRestoreInstanceState(this);
            states.add(s);
        }
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
