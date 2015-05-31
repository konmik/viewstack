package viewstack.view;

import android.view.View;

public interface FreezingContainer {
    View inflate(int index, int layoutId);
    View getView(int index);
    int size();
    void clearFrozen();
    boolean isFrozen(int index);
    boolean isHidden(int index);
    void hide(int index);
    void show(int index);
    void freeze(int index, int freezerIndex);
    void unfreeze(int index, int freezerIndex);
    void setNonPermanent(int index);
    void removeNonPermanent();
}
