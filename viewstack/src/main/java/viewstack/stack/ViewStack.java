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

    public <T extends View> T push(int layoutId) {
        onActionStart();

        T view = inflateAction(container.size(), layoutId, ActionType.PUSH_IN);

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

    public <T extends View> T replace(int layoutId) {
        onActionStart();

        for (int i = 0, size = container.size(); i < size; i++) {
            if (!container.isHidden(i))
                runAction(ActionType.REPLACE_OUT, i);
        }

        T view = inflateAction(0, layoutId, ActionType.REPLACE_IN);

        onActionEnd.run();
        return view;
    }

    private <T extends View> T inflateAction(int index, int layoutId, ActionType actionType) {
        //noinspection unchecked
        T view = (T)container.inflate(index, layoutId);
        runAction(actionType, index);
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
