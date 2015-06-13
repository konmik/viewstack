package viewstack.view;

import android.view.View;

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

    public void testInflate() throws Exception {
        ViewState state0 = freezingLayout.inflate(0, info.android15.viewstack.test.R.layout.plain_view, true);
        assertEquals(state0.getView(), freezingLayout.getChildAt(0));
        assertNotNull(state0.getView());
        assertEquals(View.class, state0.getViewClass());

        ViewState state2 = freezingLayout.inflate(0, info.android15.viewstack.test.R.layout.plain_view, true);
        assertEquals(state2.getView(), freezingLayout.getChildAt(1));
    }
}
