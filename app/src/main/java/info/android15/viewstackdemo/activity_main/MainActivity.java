package info.android15.viewstackdemo.activity_main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import info.android15.viewstackdemo.R;
import viewstack.action.TransitionDelegate;
import viewstack.action.TransitionType;
import viewstack.requirement.AnnotationRequirementsAnalyzer;
import viewstack.stack.FreezingViewGroupGroup;
import viewstack.stack.ViewStack;
import viewstack.view.FreezingLayout;

public class MainActivity extends Activity {

    ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        FreezingLayout mainFrame = (FreezingLayout)findViewById(R.id.mainFrame);
        FreezingLayout popupFrame = (FreezingLayout)findViewById(R.id.popupFrame);

        viewStack = new ViewStack(
            new FreezingViewGroupGroup(mainFrame, popupFrame),
            new AnnotationRequirementsAnalyzer(),
            new TransitionDelegate() {
                @Override
                public void onStackAction(TransitionType transition, View view, Runnable onTransitionEnd) {
                    onTransitionEnd.run();
                }
            }
        );

        if (savedInstanceState == null)
            viewStack.push(R.id.mainFrame, R.layout.screen_list);
    }
}
