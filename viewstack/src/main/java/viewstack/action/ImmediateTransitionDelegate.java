package viewstack.action;

import android.view.View;

/**
 * A no-op TransitionDelegate.
 */
public class ImmediateTransitionDelegate implements TransitionDelegate {
    @Override
    public void onStackAction(TransitionType transition, View view, Runnable onTransitionEnd) {
        onTransitionEnd.run();
    }
}
