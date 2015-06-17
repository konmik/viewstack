package viewstack.action;

import android.view.View;

/**
 * This interface is used by {@link viewstack.stack.ViewStack} to provide control over
 * animations for stack operations.
 */
public interface TransitionDelegate {
    /**
     * This method is called by {@link viewstack.stack.ViewStack} when an transition is requested.
     * If the transition implies that a view will be removed or frozen then this will be delayed until onTransitionEnd
     * is called for each called onStackAction.
     *
     * In example, if StackLayout has two children, and {@link viewstack.stack.ViewStack#replace} is
     * called then actual child removal will be done after each view's onTransitionEnd is called.
     *
     * @param transition      a type of transition that is being executing
     * @param view        a target view of transition
     * @param onTransitionEnd a callback that must be called when animation is done.
     *                    This callback can also can be called immediately if no animation required.
     */
    void onStackAction(TransitionType transition, View view, Runnable onTransitionEnd);
}
