package viewstack.stack;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import viewstack.view.FreezingViewGroup;
import viewstack.view.ViewState;

/**
 * This class allows to manipulate on a group of FreezingContainer as if it would be a single container.
 */
public class FreezingViewGroupGroup implements FreezingViewGroup {

    private final List<FreezingViewGroup> containers;
    private final SparseArray<FreezingViewGroup> frameToContainer = new SparseArray<>();

    public FreezingViewGroupGroup(FreezingViewGroup... containers) {
        this.containers = Arrays.asList(containers);
        for (FreezingViewGroup layout : containers)
            frameToContainer.put(layout.getFrameId(), layout);
    }

    @Override
    public ViewState inflate(int frameId, int layoutId, boolean visualTop) {
        return frameToContainer.get(frameId).inflate(frameId, layoutId, visualTop);
    }

    @Override
    public List<ViewState> getViewStates() {
        List<ViewState> list = new ArrayList<>();
        for (FreezingViewGroup container : containers)
            list.addAll(container.getViewStates());
        return list;
    }

    @Override
    public void removeNonPermanent() {
        for (FreezingViewGroup container : containers)
            container.removeNonPermanent();
    }

    @Override
    public int getFrameId() {
        return 0;
    }
}
