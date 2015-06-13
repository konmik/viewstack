package viewstack.view;

import java.util.List;

/**
 * This interface provides access to views and their state.
 */
public interface FreezingViewGroup {
    int getFrameId();
    ViewState inflate(int frameId, int layoutId, boolean visualTop);
    List<ViewState> getViewStates();
    void removeNonPermanent();
}
