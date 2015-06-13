package viewstack.stack;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import viewstack.action.ActionHandler;
import viewstack.action.ActionType;
import viewstack.requirement.RequirementsAnalyzer;
import viewstack.view.FreezingViewGroup;
import viewstack.view.ViewState;

public class ViewStack {

    private FreezingViewGroup container;
    private RequirementsAnalyzer analyzer;
    private ActionHandler handler;
    private int actionCounter;

    public ViewStack(FreezingViewGroup container, RequirementsAnalyzer analyzer, ActionHandler handler) {
        this.container = container;
        this.analyzer = analyzer;
        this.handler = handler;
    }

    public <T extends View> T push(int frameId, int layoutId) {
        onActionStart();

        ViewState state = container.inflate(frameId, layoutId, true);
        runAction(ActionType.PUSH_IN, state);

        int required = analyzer.getRequiredVisibleCount(getViewClasses(container.getViewStates()));
        List<ViewState> states = container.getViewStates();
        for (int i = 0, size = states.size(); i < size - required; i++) {
            ViewState viewState = states.get(i);
            if (!viewState.isHidden())
                runAction(ActionType.PUSH_OUT, viewState);
        }

        onActionEnd.run();
        //noinspection unchecked
        return (T)state.getView();
    }

    public void pop() {
        onActionStart();

        List<ViewState> states = container.getViewStates();
        runAction(ActionType.POP_OUT, states.get(states.size() - 1));

        List<Class> classes = getViewClasses(container.getViewStates());
        classes.remove(classes.size() - 1);

        int size = classes.size();
        int startVisible = size - analyzer.getRequiredVisibleCount(classes);
        int startRequired = size - analyzer.getRequiredCount(classes);
        for (int i = 0; i < size; i++) {
            ViewState viewState = states.get(i);
            if (startRequired <= i && viewState.isFrozen())
                viewState.unfreeze();
            if (startVisible <= i && viewState.isHidden()) {
                viewState.show();
                runAction(ActionType.POP_IN, viewState);
            }
        }

        onActionEnd.run();
    }

    public <T extends View> T replace(int frameId, int layoutId) {
        onActionStart();

        List<ViewState> states = container.getViewStates();
        for (ViewState state : states) {
            if (!state.isHidden())
                runAction(ActionType.REPLACE_OUT, state);
            state.setNonPermanent();
        }

        ViewState viewState = container.inflate(frameId, layoutId, false);
        runAction(ActionType.REPLACE_IN, viewState);

        onActionEnd.run();
        //noinspection unchecked
        return (T)viewState.getView();
    }

    private void runAction(ActionType actionType, final ViewState viewState) {
        onActionStart();
        if (actionType.isExit())
            viewState.setNonPermanent();
        handler.onStackAction(actionType, viewState.getView(), !actionType.isOut() ? onActionEnd : new Runnable() {
            @Override
            public void run() {
                viewState.hide();
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
        List<Class> classes = getViewClasses(container.getViewStates());
        int size = classes.size();
        int startVisible = size - analyzer.getRequiredVisibleCount(classes);
        int startRequired = size - analyzer.getRequiredCount(classes);
        List<ViewState> states = container.getViewStates();
        for (int i = 0; i < size; i++) {
            ViewState state = states.get(i);
            if (i < startVisible && !state.isHidden())
                state.hide();
            if (i < startRequired && !state.isFrozen())
                state.freeze();
        }
        container.removeNonPermanent();
    }

    private List<Class> getViewClasses(List<ViewState> states) {
        List<Class> list = new ArrayList<>(states.size());
        for (ViewState state : states)
            list.add(state.getViewClass());
        return list;
    }
}
