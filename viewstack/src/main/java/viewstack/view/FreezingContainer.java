package viewstack.view;

import android.view.View;

import java.util.List;

public interface FreezingContainer {
    View inflate(int index, int layoutId);
    View getView(int index);

    int size();
    void freeze(int count);

    boolean isHidden(int index);
    void hide(int count);
    void show(int index);

    void setNonPermanent(int index);
    void removeNonPermanent();
    List<Class> getClasses();
}
