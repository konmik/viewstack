package viewstack.stack;

import android.util.SparseArray;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import viewstack.util.Lazy;
import viewstack.view.FreezingContainer;

/**
 * This class allows to manipulate on a group of FreezingContainer as if it would be a single container.
 */
public class FreezingContainerGroup implements FreezingContainer {

    private final List<FreezingContainer> containers;
    private final SparseArray<FreezingContainer> frameToContainer = new SparseArray<>();

    private class Coordinates {
        final FreezingContainer container;
        final int localIndex;

        public Coordinates(FreezingContainer container, int localIndex) {
            this.container = container;
            this.localIndex = localIndex;
        }
    }

    private Lazy<List<Coordinates>> coords = new Lazy<>(new Lazy.Factory<List<Coordinates>>() {
        @Override
        public List<Coordinates> call() {
            List<Coordinates> coordinates = new ArrayList<>(size());
            for (FreezingContainer container : containers) {
                for (int i = 0, size = container.size(); i < size; i++)
                    coordinates.add(new Coordinates(container, i));
            }
            return coordinates;
        }
    });

    public FreezingContainerGroup(FreezingContainer... containers) {
        this.containers = Arrays.asList(containers);
        for (FreezingContainer layout : containers)
            frameToContainer.put(layout.getFrameId(), layout);
    }

    @Override
    public View inflateTop(int frameId, int layoutId) {
        coords.invalidate();
        return frameToContainer.get(frameId).inflateTop(frameId, layoutId);
    }

    @Override
    public View inflateBottom(int frameId, int layoutId) {
        coords.invalidate();
        return frameToContainer.get(frameId).inflateBottom(frameId, layoutId);
    }

    @Override
    public View getView(int index) {
        Coordinates coordinates = coords.get().get(index);
        return coordinates.container.getView(coordinates.localIndex);
    }

    @Override
    public int size() {
        int size = 0;
        for (FreezingContainer container : containers)
            size += container.size();
        return size;
    }

    @Override
    public void freeze(int index) {
        Coordinates coordinates = coords.get().get(index);
        coordinates.container.freeze(coordinates.localIndex);
    }

    @Override
    public boolean isHidden(int index) {
        Coordinates coordinates = coords.get().get(index);
        return coordinates.container.isHidden(coordinates.localIndex);
    }

    @Override
    public void hide(int index) {
        Coordinates coordinates = coords.get().get(index);
        coordinates.container.hide(coordinates.localIndex);
    }

    @Override
    public void show(int index) {
        Coordinates coordinates = coords.get().get(index);
        coordinates.container.show(coordinates.localIndex);
    }

    @Override
    public void setNonPermanent(int index) {
        Coordinates coordinates = coords.get().get(index);
        coordinates.container.setNonPermanent(coordinates.localIndex);
        coords.invalidate();
    }

    @Override
    public void removeNonPermanent() {
        for (FreezingContainer container : containers)
            container.removeNonPermanent();
    }

    @Override
    public void removeFrozen() {
        for (FreezingContainer container : containers)
            container.removeFrozen();
        coords.invalidate();
    }

    @Override
    public List<Class> getClasses() {
        List<Class> classes = new ArrayList<>();
        for (FreezingContainer container : containers)
            classes.addAll(container.getClasses());
        return classes;
    }

    @Override
    public int getFrameId() {
        return 0;
    }
}
