package viewstack.stack;

import android.view.View;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import viewstack.action.TransitionDelegate;
import viewstack.action.TransitionType;
import viewstack.requirement.RequirementsAnalyzer;
import viewstack.view.FreezingViewGroup;
import viewstack.view.ViewState;

import static java.lang.Math.max;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ViewStackTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testPush() throws Exception {
        for (int backStackSize = 0; backStackSize < 5; backStackSize++) {
            FreezingViewGroup freezingViewGroup = mock(FreezingViewGroup.class);
            TransitionDelegate transitionDelegate = mock(TransitionDelegate.class);
            RequirementsAnalyzer analyzer = mock(RequirementsAnalyzer.class);
            ViewStack stack = new ViewStack(freezingViewGroup, analyzer, transitionDelegate);

            ViewState viewState = mock(ViewState.class);
            View view = mock(View.class);
            when(freezingViewGroup.inflate(anyInt(), anyInt(), anyBoolean())).thenReturn(viewState);
            when(viewState.getView()).thenReturn(view);

            RequirementsAnalyzer.Analysis analysis = new RequirementsAnalyzer.Analysis(1, max(1, backStackSize / 2));
            when(analyzer.analyze(any(List.class))).thenReturn(analysis);

            List<ViewState> states = new ArrayList<>(backStackSize);
            for (int i = 0; i < backStackSize; i++) {
                View v = mock(View.class);
                ViewState mock = mock(ViewState.class);
                when(mock.isHidden()).thenReturn(false);
                when(mock.getView()).thenReturn(v);
                states.add(mock);
            }
            states.add(viewState);
            when(freezingViewGroup.getViewStates()).thenReturn(states);

            View returnedView = stack.push(0, info.android15.viewstack.test.R.layout.plain_view);

            assertEquals(view, returnedView);

            verify(freezingViewGroup, times(1)).inflate(0, info.android15.viewstack.test.R.layout.plain_view, true);
            verify(freezingViewGroup, atLeastOnce()).getViewStates();
            verifyNoMoreInteractions(freezingViewGroup);

            int shouldBePushedOut = backStackSize - analysis.visible + 1;
            for (int i = 0; i < shouldBePushedOut; i++) {
                View view1 = states.get(i).getView();
                verify(transitionDelegate, times(1)).onStackAction(eq(TransitionType.PUSH_OUT), eq(view1), any(Runnable.class));
            }
            verify(transitionDelegate, times(1)).onStackAction(eq(TransitionType.PUSH_IN), eq(view), any(Runnable.class));
            verifyNoMoreInteractions(transitionDelegate);

            verify(analyzer, atLeastOnce()).analyze(anyList());
            verifyNoMoreInteractions(analyzer);
        }
    }

    public void testPop() throws Exception {
        for (int frozen = 0; frozen < 5; frozen++)
            for (int hidden = 0; hidden < 5; hidden++)
                for (int visible = frozen + hidden == 0 ? 1 : 0; visible < 5; visible++)
                    testPop(frozen, hidden, visible);
    }

    public ViewState mockViewState(View view, boolean isFrozen, boolean isHidden) {
        ViewState mock = mock(ViewState.class);
        when(mock.isFrozen()).thenReturn(isFrozen);
        when(mock.isHidden()).thenReturn(isHidden);
        when(mock.getView()).thenReturn(view);
        if (view != null)
            when(mock.getViewClass()).thenReturn(view.getClass());
        return mock;
    }

    private void testPop(int frozen, int hidden, int visible) throws Exception {
        FreezingViewGroup freezingViewGroup = mock(FreezingViewGroup.class);
        TransitionDelegate transitionDelegate = mock(TransitionDelegate.class);
        RequirementsAnalyzer analyzer = mock(RequirementsAnalyzer.class);

        int required = 1 + frozen / 2;
        int visible1 = visible + hidden / 2;
        when(analyzer.analyze(anyList())).thenReturn(new RequirementsAnalyzer.Analysis(required, visible1));

        List<ViewState> states = new ArrayList<>(frozen + hidden + 1);
        for (int f = 0; f < frozen; f++)
            states.add(mockViewState(mock(View.class), true, true));
        for (int h = 0; h < hidden; h++)
            states.add(mockViewState(mock(View.class), false, true));
        for (int v = 0; v < visible; v++)
            states.add(mockViewState(mock(View.class), false, false));
        states.add(mockViewState(mock(View.class), false, false));
        when(freezingViewGroup.getViewStates()).thenReturn(states);

        ViewStack stack = new ViewStack(freezingViewGroup, analyzer, transitionDelegate);
        stack.pop();


    }

    public void testReplace() throws Exception {

    }
}