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
        for (int backStackSize = 0; backStackSize < 3; backStackSize++) {
            FreezingViewGroup freezingViewGroup = mock(FreezingViewGroup.class);
            TransitionDelegate transitionDelegate = mock(TransitionDelegate.class);
            RequirementsAnalyzer analyzer = mock(RequirementsAnalyzer.class);
            ViewStack stack = new ViewStack(freezingViewGroup, analyzer, transitionDelegate);

            ViewState viewState = mock(ViewState.class);
            View view = mock(View.class);
            when(freezingViewGroup.inflate(anyInt(), anyInt(), anyBoolean())).thenReturn(viewState);
            when(viewState.getView()).thenReturn(view);
            when(analyzer.analyze(any(List.class))).thenReturn(new RequirementsAnalyzer.Analysis(1, 1));

            List<ViewState> states = new ArrayList<>();
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

            for (int i = 0; i < backStackSize; i++) {
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

    }

    public void testReplace() throws Exception {

    }
}