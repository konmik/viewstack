package viewstack.stack;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import viewstack.action.TransitionDelegate;
import viewstack.action.TransitionType;
import viewstack.requirement.RequirementsAnalyzer;
import viewstack.view.FreezingViewGroup;
import viewstack.view.ViewState;

public class ViewStack {

    private FreezingViewGroup container;
    private RequirementsAnalyzer analyzer;
    private TransitionDelegate transitionDelegate;
    private int actionCounter;

    public ViewStack(FreezingViewGroup container, RequirementsAnalyzer analyzer, TransitionDelegate transitionDelegate) {
        this.container = container;
        this.analyzer = analyzer;
        this.transitionDelegate = transitionDelegate;
    }

    public <T extends View> T push(int frameId, int layoutId) {
        onActionStart();

        ViewState state = container.inflate(frameId, layoutId, true);
        runAction(TransitionType.PUSH_IN, state);

        RequirementsAnalyzer.Analysis analysis = analyzer.analyze(getViewClasses());
        List<ViewState> states = container.getViewStates();
        for (int i = 0, size = states.size(); i < size - analysis.visible; i++) {
            ViewState viewState = states.get(i);
            if (!viewState.isHidden())
                runAction(TransitionType.PUSH_OUT, viewState);
        }

        onActionEnd.run();
        //noinspection unchecked
        return (T)state.getView();
    }

    public void pop() {
        onActionStart();

        List<ViewState> states = container.getViewStates();
        runAction(TransitionType.POP_OUT, states.get(states.size() - 1));

        List<Class> classes = getViewClasses();
        classes.remove(classes.size() - 1);

        int size = classes.size();
        RequirementsAnalyzer.Analysis analysis = analyzer.analyze(classes);
        int startVisible = size - analysis.required;
        int startRequired = size - analysis.visible;
        for (int i = 0; i < size; i++) {
            ViewState viewState = states.get(i);
            if (startRequired <= i && viewState.isFrozen())
                viewState.unfreeze();
            if (startVisible <= i && viewState.isHidden()) {
                viewState.show();
                runAction(TransitionType.POP_IN, viewState);
            }
        }

        onActionEnd.run();
    }

    public <T extends View> T replace(int frameId, int layoutId) {
        onActionStart();

        List<ViewState> states = container.getViewStates();
        for (ViewState state : states) {
            if (!state.isHidden())
                runAction(TransitionType.REPLACE_OUT, state);
            state.setNonPermanent();
        }

        ViewState viewState = container.inflate(frameId, layoutId, false);
        runAction(TransitionType.REPLACE_IN, viewState);

        onActionEnd.run();
        //noinspection unchecked
        return (T)viewState.getView();
    }

    private void runAction(TransitionType transitionType, final ViewState viewState) {
        onActionStart();
        if (transitionType.isExit())
            viewState.setNonPermanent();
        transitionDelegate.onStackAction(transitionType, viewState.getView(), !transitionType.isOut() ? onActionEnd : new Runnable() {
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
        List<Class> classes = getViewClasses();
        int size = classes.size();
        RequirementsAnalyzer.Analysis analysis = analyzer.analyze(classes);
        int startVisible = size - analysis.required;
        int startRequired = size - analysis.visible;
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

    private List<Class> getViewClasses() {
        List<ViewState> states = container.getViewStates();
        List<Class> list = new ArrayList<>(states.size());
        for (ViewState state : states)
            list.add(state.getViewClass());
        return list;
    }
}
