package viewstack.stack;

import android.view.View;

import java.util.Collections;
import java.util.List;

import viewstack.view.FreezingContainer;

public class FreezingGroup implements FreezingContainer {

    private final List<FreezingContainer> containers;

    public FreezingGroup(List<FreezingContainer> containers) {
        this.containers = Collections.unmodifiableList(containers);
    }

    @Override
    public View inflate(int index, int layoutId) {
        return null;
    }

    @Override
    public View getView(int index) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void freeze(int count) {

    }

    @Override
    public boolean isHidden(int index) {
        return false;
    }

    @Override
    public void hide(int count) {

    }

    @Override
    public void show(int index) {

    }

    @Override
    public void setNonPermanent(int index) {

    }

    @Override
    public void removeNonPermanent() {

    }

    @Override
    public List<Class> getClasses() {
        return null;
    }
}
