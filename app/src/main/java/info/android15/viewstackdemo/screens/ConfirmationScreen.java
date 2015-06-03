package info.android15.viewstackdemo.screens;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import viewstack.requirement.RequiredViews;

@RequiredViews(visible = View.class, dependencies = ConfirmationScreen.DialogScreenOwner.class)
public class ConfirmationScreen extends FrameLayout {

    public interface DialogScreenOwner {

    }

    public ConfirmationScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
