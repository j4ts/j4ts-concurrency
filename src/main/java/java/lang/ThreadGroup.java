package java.lang;

import java.util.function.Function;

import static jsweet.util.Lang.*;

public class ThreadGroup {
    static int ID_COUNTER;
    private static ThreadGroup MAIN;
    private final ThreadGroup parent;
    private final String name;
    private final int id;
    public Thread[] childs = new Thread[0];
    private ThreadGroup[] childGroups = new ThreadGroup[0];
    private boolean destroyed = false;

    public ThreadGroup(String name) {
        this(getMain(), name);
    }

    public ThreadGroup(ThreadGroup parent, String name) {
        this.parent = parent;
        this.name = name;
        this.id = ID_COUNTER++;

        if (parent != null) {
            array(parent.childGroups).push(this);
        }
    }

    private ThreadGroup(ThreadGroup parent, String name, int id, boolean destroyed) {
        this.parent = parent;
        this.name = name;
        this.id = id;
        this.destroyed = destroyed;

        if (parent != null) {
            array(parent.childGroups).push(this);
        }
    }

    public static ThreadGroup getMain() {
        if (MAIN == null) {
            MAIN = new ThreadGroup(null, "main");
        }
        return MAIN;
    }

    static void updateWholeTree(def.js.Object treeObject) {
        ThreadGroup naked = any(treeObject);
        getMain().updateTree(naked);
    }

    public int activeCount() {
        int result = childs.length;
        for (ThreadGroup childGroup : childGroups) {
            result += childGroup.activeCount();
        }
        return result;
    }

    public int activeGroupCount() {
        int result = childGroups.length;
        for (ThreadGroup childGroup : childGroups) {
            result += childGroup.activeGroupCount();
        }
        return result;
    }

    public void destroy() {
        ThreadGroup[] cp = new ThreadGroup[childGroups.length];
        arraycopy(childGroups, 0, cp, 0, childGroups.length);

        for (ThreadGroup childGroup : cp) {
            childGroup.destroy();
        }
        for (Thread child : childs) {
            child.destroy();
        }
        if (parent != null) {
            array(parent.childGroups).splice(array(parent.childGroups).indexOf(this), 1);
        }
        this.destroyed = true;
    }

    public int enumerate(Thread[] list) {
        return enumerate(list, true);
    }

    public int enumerate(Thread[] list, boolean recurse) {
        if (recurse) {
            return enumerate_inner(list, 0, t -> t.childs);
        } else {
            int threadsToCopy = Math.min(list.length, childs.length);
            arraycopy(childs, 0, list, 0, threadsToCopy);
            return threadsToCopy;
        }
    }

    public int enumerate(ThreadGroup[] list) {
        return enumerate(list, true);
    }

    public int enumerate(ThreadGroup[] list, boolean recurse) {
        if (recurse) {
            return enumerate_inner(list, 0, t -> t.childGroups);
        } else {
            int threadGroupsToCopy = Math.min(list.length, childGroups.length);
            arraycopy(childGroups, 0, list, 0, threadGroupsToCopy);
            return threadGroupsToCopy;
        }
    }

    private <T> int enumerate_inner(T[] list, int fromIndex, Function<ThreadGroup, T[]> fromArray) {
        if (fromIndex == list.length)
            return fromIndex;

        int threadsToCopy = Math.min(list.length - fromIndex, fromArray.apply(this).length);
        arraycopy(fromArray.apply(this), 0, list, fromIndex, threadsToCopy);

        int result = fromIndex + threadsToCopy;
        for (ThreadGroup childGroup : childGroups) {
            if (result == list.length)
                return result;

            result = childGroup.enumerate_inner(list, result, fromArray);
        }
        return result;
    }

    public int getMaxPriority() {
        return Thread.MAX_PRIORITY;
    }

    public void setMaxPriority(int pri) {
    }

    public String getName() {
        return name;
    }

    public ThreadGroup getParent() {
        return parent;
    }

    public void interrupt() {
        for (ThreadGroup childGroup : childGroups) {
            childGroup.interrupt();
        }
        for (Thread child : childs) {
            child.interrupt();
        }
    }

    public boolean isDaemon() {
        return true;
    }

    public void setDaemon(boolean daemon) {
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void list() {
        list_indented("");
    }

    private void list_indented(String indention) {
        System.out.println(indention + toString());
        String childIndentation = indention + "    ";
        for (Thread child : childs) {
            System.out.println(childIndentation + child.toString());
        }
        for (ThreadGroup childGroup : childGroups) {
            childGroup.list_indented(childIndentation);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[name=" + name + ",maxpri=" + getMaxPriority() + ']';
    }

    public boolean parentOf(ThreadGroup g) {
        return this == g || g.parent != null && this.parentOf(g.parent);
    }

    private void updateTree(ThreadGroup nakedObject) {
        // childgroup
        for (ThreadGroup nakedChildGroup : nakedObject.childGroups) {
            ThreadGroup real = null;

            for (ThreadGroup childGroup : childGroups) {
                if (nakedChildGroup.id == childGroup.id) {
                    real = childGroup;
                    break;
                }
            }

            if (real == null) {
                real = new ThreadGroup(this, nakedChildGroup.name, nakedChildGroup.id, nakedChildGroup.destroyed);
            }
            real.updateTree(nakedChildGroup);
        }

        ThreadGroup[] cpChildGroup = new ThreadGroup[childGroups.length];
        enumerate(cpChildGroup, false);

        for (ThreadGroup childGroup : cpChildGroup) {
            boolean found = false;
            for (ThreadGroup nakedChildGroup : nakedObject.childGroups) {
                if (childGroup.id == nakedChildGroup.id) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                childGroup.destroy();
            }
        }

        // child
        for (Thread nakedChild : nakedObject.childs) {
            Thread real = null;

            for (Thread child : childs) {
                if (nakedChild.id == child.id) {
                    real = child;
                    break;
                }
            }

            if (real == null) {
                real = new Thread(this, new def.js.Function("return " + object(nakedChild).$get("target")).$apply(), nakedChild.name, nakedChild.id);
            }
            real.updateThread(nakedChild);
        }

        Thread[] cpChild = new Thread[childs.length];
        enumerate(cpChild, false);

        for (Thread child : cpChild) {
            boolean found = false;
            for (Thread nakedChild : nakedObject.childs) {
                if (child.id == nakedChild.id) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                child.destroy();
            }
        }

        // destroyed flag
        this.destroyed = nakedObject.destroyed;
    }

    private void arraycopy(Object[] from, int fromStart, Object[] to, int toStart, int length) {
        int deleteCount = Math.min(to.length - toStart, length);
        int fromEnd = Math.min(from.length, fromStart + length);
        $insert("Array.prototype.splice.apply(to, [toStart, deleteCount].concat(from.slice(fromStart, fromEnd)))");
    }
}
