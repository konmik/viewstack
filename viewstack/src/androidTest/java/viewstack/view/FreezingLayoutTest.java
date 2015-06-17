package viewstack.view;

import android.test.UiThreadTest;
import android.view.View;

import info.android15.viewstack.test.R;
import testkit.BaseActivityTest;
import testkit.TestActivity;

public class FreezingLayoutTest extends BaseActivityTest<TestActivity> {

    public FreezingLayoutTest() {
        super(TestActivity.class);
    }

    FreezingLayout freezingLayout;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                freezingLayout = new FreezingLayout(getActivity());
                getActivity().setContentView(freezingLayout);
            }
        });
    }

    @UiThreadTest
    public void testInflate() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, R.layout.plain_view, true);
        assertEquals(state0.getView(), freezingLayout.getChildAt(0));
        assertNotNull(state0.getView());
        assertEquals(View.class, state0.getViewClass());

        ViewState state2 = freezingLayout.inflate(0, R.layout.plain_view, true);
        assertEquals(state2.getView(), freezingLayout.getChildAt(1));

        assertEquals(2, freezingLayout.getViewStates().size());
    }

    @UiThreadTest
    public void testRemoveNonPermanent() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, R.layout.plain_view, true);
        ViewState state2 = freezingLayout.inflate(0, R.layout.plain_view, true);
        state0.setNonPermanent();
        freezingLayout.removeNonPermanent();
        assertEquals(1, freezingLayout.getViewStates().size());
    }

    @UiThreadTest
    public void testStateGetViewAndClass() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, R.layout.plain_view, true);
        assertNotNull(state0.getClass());
        assertEquals(state0.getView().getClass(), state0.getViewClass());
        state0.freeze();
        assertNotNull(state0.getViewClass());
    }

    @UiThreadTest
    public void testStateHideShow() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, R.layout.plain_view, true);
        ViewState state2 = freezingLayout.inflate(0, R.layout.plain_view, true);

        assertFalse(state0.isHidden());
        assertFalse(state2.isHidden());

        state0.hide();
        assertEquals(View.GONE, state0.getView().getVisibility());
        assertTrue(state0.isHidden());
        assertFalse(state2.isHidden());

        state2.hide();
        assertTrue(state0.isHidden());
        assertTrue(state2.isHidden());
        assertEquals(View.GONE, state2.getView().getVisibility());

        state0.show();
        state2.show();

        assertFalse(state0.isHidden());
        assertFalse(state2.isHidden());

        assertEquals(View.VISIBLE, state0.getView().getVisibility());
        assertEquals(View.VISIBLE, state2.getView().getVisibility());
    }

    @UiThreadTest
    public void testFreezeUnfreeze() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, R.layout.plain_view, true);
        ViewState state2 = freezingLayout.inflate(0, R.layout.plain_view, true);

        assertFalse(state0.isFrozen());
        assertFalse(state2.isFrozen());

        state0.freeze();
        assertNull(state0.getView());
        assertTrue(state0.isFrozen());
        assertFalse(state2.isFrozen());

        state2.hide();
        state2.freeze();
        assertTrue(state0.isFrozen());
        assertTrue(state2.isFrozen());
        assertNull(state2.getView());

        state0.unfreeze();
        state2.unfreeze();

        assertFalse(state0.isFrozen());
        assertFalse(state2.isFrozen());

        assertNotNull(state0.getView());
        assertNotNull(state2.getView());

        assertEquals(state0.getViewClass(), state0.getView().getClass());
        assertEquals(state2.getViewClass(), state2.getView().getClass());

        assertFalse(state0.isHidden());
        assertTrue(state2.isHidden());
    }
}
