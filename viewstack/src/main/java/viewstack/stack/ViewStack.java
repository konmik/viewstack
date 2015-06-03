package viewstack.stack;

import android.view.View;

import java.util.List;

import viewstack.action.ActionHandler;
import viewstack.action.ActionType;
import viewstack.requirement.RequirementsAnalyzer;
import viewstack.view.FreezingContainer;

public class ViewStack {

    private FreezingContainer container;
    private RequirementsAnalyzer analyzer;
    private ActionHandler handler;
    private int actionCounter;

    public ViewStack(FreezingContainer container, RequirementsAnalyzer analyzer, ActionHandler handler) {
        this.container = container;
        this.analyzer = analyzer;
        this.handler = handler;
    }

    public <T extends View> T push(int frameId, int layoutId) {
        onActionStart();

        //noinspection unchecked
        T view = (T)container.inflateTop(frameId, layoutId);
        runAction(ActionType.PUSH_IN, container.size() - 1);

        int required = analyzer.getRequiredVisibleCount(container.getClasses());
        for (int i = 0, size = container.size(); i < size - required; i++) {
            if (!container.isHidden(i))
                runAction(ActionType.PUSH_OUT, i);
        }

        onActionEnd.run();
        return view;
    }

    public void pop() {
        onActionStart();

        runAction(ActionType.POP_OUT, container.size() - 1);

        int size = container.size();
        int required = analyzer.getRequiredVisibleCount(container.getClasses());
        for (int i = size - required; i < size; i++) {
            if (container.isHidden(i)) {
                container.show(i);
                runAction(ActionType.POP_IN, i);
            }
        }

        onActionEnd.run();
    }

    public <T extends View> T replace(int frameId, int layoutId) {
        onActionStart();

        container.removeFrozen();
        for (int i = 0, size = container.size(); i < size; i++) {
            if (!container.isHidden(i))
                runAction(ActionType.REPLACE_OUT, i);
        }

        //noinspection unchecked
        T view = (T)container.inflateBottom(frameId, layoutId);
        runAction(ActionType.REPLACE_IN, 0);

        onActionEnd.run();
        return view;
    }

    private void runAction(ActionType actionType, int index) {
        onActionStart();

        final View view = container.getView(index);
        if (actionType.isExit())
            container.setNonPermanent(index);
        handler.onStackAction(actionType, view, !actionType.isOut() ? onActionEnd : new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);

                onActionEnd.run();
            }
        });
    }

    private void onActionStart() {
        actionCounter++;
    }

    private Runnable onActionEnd = new Runnable() {
        @Override
        public void run() {
            actionCounter--;
            if (actionCounter == 0)
                onAllActionsEnd();
        }
    };

    private void onAllActionsEnd() {
        List<Class> classes = container.getClasses();
        int size = classes.size();
        container.hide(size - analyzer.getRequiredVisibleCount(classes));
        container.freeze(size - analyzer.getRequiredCount(classes));
        container.removeNonPermanent();
    }
}
