package viewstack.requirement;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AnnotationRequirementsAnalyzer implements RequirementsAnalyzer {

    @Override
    public int getRequiredCount(List<Class> stack) {
        return getRequiredCount(stack, false);
    }

    @Override
    public int getRequiredVisibleCount(List<Class> stack) {
        return getRequiredCount(stack, true);
    }

    private int getRequiredCount(List<Class> stack, boolean visible) {
        int size = stack.size();
        if (size == 0)
            return 0;
        int index = size - 1;
        List<Class> requirements = getRequiredViewClasses(stack.get(index), visible);
        while (requirements.size() > 0) {
            Class clazz = stack.get(--index);
            removeSatisfiedDependencies(requirements, clazz);
            addDistinct(requirements, getRequiredViewClasses(clazz, visible));
        }
        return size - index;
    }

    /**
     * This algorithm is used instead of {@link java.lang.annotation.Inherited} annotation because
     * there can exist more than one {@link RequiredViews} in a views hierarchy.
     */
    private static List<Class> getRequiredViewClasses(Class clazz, boolean visible) {
        List<Class> requirements = new ArrayList<>();
        while (clazz != null && View.class.isAssignableFrom(clazz)) {
            if (clazz.isAnnotationPresent(RequiredViews.class)) {
                RequiredViews annotation = (RequiredViews)clazz.getAnnotation(RequiredViews.class);
                addDistinct(requirements, Arrays.asList(visible ? annotation.visible() : annotation.dependencies()));
            }
            clazz = clazz.getSuperclass();
        }
        return requirements;
    }

    private static void removeSatisfiedDependencies(List<Class> requirements, Class clazz) {
        for (int i = requirements.size() - 1; i >= 0; i--) {
            if (((Class<?>)requirements.get(i)).isAssignableFrom(clazz))
                requirements.remove(i);
        }
    }

    private static <T> void addDistinct(Collection<T> collection, T item) {
        if (!collection.contains(item))
            collection.add(item);
    }

    private static <T> void addDistinct(Collection<T> collection, Iterable<T> items) {
        for (T item : items)
            addDistinct(collection, item);
    }
}
