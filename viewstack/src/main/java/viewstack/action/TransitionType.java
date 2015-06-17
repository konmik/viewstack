package viewstack.action;

/**
 * Action is a unit of work that is done by the {@link viewstack.stack.ViewStack} stack on a child view.
 * This enum's single use is for {@link TransitionDelegate} interface.
 */
public enum TransitionType {

    /**
     * A view is going to be shown with {@link viewstack.stack.ViewStack#push}.
     */
    PUSH_IN(false, false),

    /**
     * A view is going to be frozen with {@link viewstack.stack.ViewStack#push}.
     */
    PUSH_OUT(true, false),

    /**
     * A view is going to be shown with {@link viewstack.stack.ViewStack#pop} method.
     */
    POP_IN(false, false),

    /**
     * A view is going to be removed with {@link viewstack.stack.ViewStack#pop} method.
     */
    POP_OUT(true, true),

    /**
     * A view is going to be shown with {@link viewstack.stack.ViewStack#replace} method.
     */
    REPLACE_IN(false, false),

    /**
     * A view is going to be removed with {@link viewstack.stack.ViewStack#replace} method.
     */
    REPLACE_OUT(true, true);

    /**
     * @return true if an action is out.
     */
    public boolean isOut() {
        return out;
    }

    /**
     * @return true if a view is going to be removed with this action.
     */
    public boolean isExit() {
        return exit;
    }

    private boolean out;
    private boolean exit;

    TransitionType(boolean out, boolean exit) {
        this.out = out;
        this.exit = exit;
    }
}
