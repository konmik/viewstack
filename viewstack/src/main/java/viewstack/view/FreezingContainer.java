package viewstack.view;

import android.view.View;

import java.util.List;

public interface FreezingContainer {
    int getFrameId();
    View inflateTop(int frameId, int layoutId);
    View inflateBottom(int frameId, int layoutId);
    View getView(int index);

    int size();
    void freeze(int index);

    boolean isHidden(int index);
    void hide(int count);
    void show(int index);

    void setNonPermanent(int index);
    void removeNonPermanent();
    void removeFrozen();
    List<Class> getClasses();
}
