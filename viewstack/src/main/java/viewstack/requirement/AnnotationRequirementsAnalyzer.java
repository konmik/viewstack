package viewstack.requirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AnnotationRequirementsAnalyzer implements RequirementsAnalyzer {

    @Override
    public Analysis analyze(List<Class> stack) {
        int count0 = 1, count1 = 1;
        synchronized (requirements0) {
            int size = stack.size();
            int index = size - 1;
            boolean done0 = false, done1 = false;
            do {
                Class clazz = stack.get(index--);
                removeSatisfiedDependencies(requirements0, clazz);
                removeSatisfiedDependencies(requirements1, clazz);
                getRequiredViewClasses(clazz, requirements0, requirements1);
                done0 |= requirements0.size() == 0;
                done1 |= requirements1.size() == 0;
                if (!done0)
                    count0++;
                if (!done1)
                    count1++;
            }
            while (!done0 || !done1);
            requirements0.clear();
            requirements1.clear();
        }
        return new Analysis(count0, count1);
    }

    private static final ArrayList<Class> requirements0 = new ArrayList<>();
    private static final ArrayList<Class> requirements1 = new ArrayList<>();

    private static HashMap<Class, List<Class>> requirements0Cache = new HashMap<>();
    private static HashMap<Class, List<Class>> requirements1Cache = new HashMap<>();

    /**
     * This algorithm is used instead of {@link java.lang.annotation.Inherited} annotation because
     * there can exist more than one {@link RequiredViews} annotation in a views hierarchy.
     */
    private static void getRequiredViewClasses(Class clazz, List<Class> requirements0, List<Class> requirements1) {
        List<Class> list0 = requirements0Cache.get(clazz);
        List<Class> list1 = requirements1Cache.get(clazz);
        if (list0 == null) {
            requirements0Cache.put(clazz, list0 = new ArrayList<>());
            requirements1Cache.put(clazz, list1 = new ArrayList<>());
            while (clazz != null) {
                if (clazz.isAnnotationPresent(RequiredViews.class)) {
                    RequiredViews annotation = (RequiredViews)clazz.getAnnotation(RequiredViews.class);
                    addDistinct(list0, annotation.required());
                    addDistinct(list1, annotation.visible());
                }
                clazz = clazz.getSuperclass();
            }
        }
        requirements0.addAll(list0);
        requirements1.addAll(list1);
    }

    private static void removeSatisfiedDependencies(List<Class> requirements, Class clazz) {
        for (int i = requirements.size() - 1; i >= 0; i--) {
            if (((Class<?>)requirements.get(i)).isAssignableFrom(clazz))
                requirements.remove(i);
        }
    }

    private static <T> void addDistinct(Collection<T> collection, T[] items) {
        for (T item : items)
            if (!collection.contains(item))
                collection.add(item);
    }
}
