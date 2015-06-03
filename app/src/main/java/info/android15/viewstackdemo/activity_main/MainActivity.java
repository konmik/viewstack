package info.android15.viewstackdemo.activity_main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import info.android15.viewstackdemo.R;
import viewstack.action.ActionHandler;
import viewstack.action.ActionType;
import viewstack.requirement.AnnotationRequirementsAnalyzer;
import viewstack.stack.FreezingContainerGroup;
import viewstack.stack.ViewStack;
import viewstack.view.FreezerInflater;
import viewstack.view.FreezingLayout;

public class MainActivity extends Activity {

    ViewStack viewStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        FreezingLayout mainFrame = (FreezingLayout)findViewById(R.id.mainFrame);
        mainFrame.setFreezerInflater(new FreezerInflater(getLayoutInflater()));
        FreezingLayout popupFrame = (FreezingLayout)findViewById(R.id.popupFrame);
        popupFrame.setFreezerInflater(new FreezerInflater(getLayoutInflater()));

        viewStack = new ViewStack(
            new FreezingContainerGroup(mainFrame, popupFrame),
            new AnnotationRequirementsAnalyzer(),
            new ActionHandler() {
                @Override
                public void onStackAction(ActionType action, View view, Runnable onActionEnd) {
                    onActionEnd.run();
                }
            }
        );

        if (savedInstanceState == null)
            viewStack.push(R.id.mainFrame, R.layout.screen_list);
    }
}
