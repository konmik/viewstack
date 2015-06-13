package viewstack.view;

import android.view.View;

public interface ViewState {
    View getView();
    Class getViewClass();

    boolean isFrozen();
    void freeze();
    void unfreeze();

    boolean isHidden();
    void hide();
    void show();

    void setNonPermanent();
}
