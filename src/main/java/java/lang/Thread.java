package java.lang;

import def.dom.*;
import def.js.Array;
import def.js.Date;
import def.js.Error;
import def.js.JSON;

import java.util.function.Function;

import static def.dom.Globals.*;
import static def.js.Globals.undefined;
import static jsweet.util.Lang.*;

public class Thread {
    public static final int MIN_PRIORITY = 0;
    public static final int NORM_PRIORITY = 0;
    public static final int MAX_PRIORITY = 0;
    // thread local variable (each one is)
    public static final boolean IS_MAIN_THREAD = typeof(window).equals("object");
    public static final boolean IS_WORKER_THREAD = !IS_MAIN_THREAD && typeof($insert("importScripts")).equals("function");

    public static Thread current;
    public static Thread parent;
    public static Array<String> importables;
    private static int ID_COUNTER = 0;

    static {
        if (IS_MAIN_THREAD) {
            current = new Thread(ThreadGroup.getMain(), null, "main");
            current.alive = true;

            parent = null;
            importables = new Array<>();
            // TODO push all script which need
            HTMLScriptElement element = (HTMLScriptElement) document.$get("currentScript");
            importables.push(element.src);
        }
        if (IS_WORKER_THREAD) {
            onWorkerStart();
        }
    }

    final int id;
    private final ThreadGroup threadGroup;
    private final Runnable target;
    String name;
    private Worker worker;
    private int priority = 0;
    private boolean _interrupted = false;
    private boolean alive = false;
    private boolean daemon = true;

    public Thread() {
        this(ThreadGroup.getMain(), (Runnable) null);
    }

    public Thread(Runnable target) {
        this(ThreadGroup.getMain(), target);
    }

    public Thread(Runnable target, String name) {
        this(ThreadGroup.getMain(), target, name);
    }

    public Thread(String name) {
        this(ThreadGroup.getMain(), null, name);
    }

    public Thread(ThreadGroup group, Runnable target) {
        this(group, target, "Thread-" + ID_COUNTER);
    }

    public Thread(ThreadGroup group, String name) {
        this(group, null, name);
    }

    public Thread(ThreadGroup threadGroup, Runnable target, String name) {
        this.threadGroup = threadGroup;
        this.name = name;
        this.target = target;
        this.id = ID_COUNTER++;

        if (threadGroup != null) {
            array(threadGroup.childs).push(this);
        }
    }

    Thread(ThreadGroup threadGroup, Runnable target, String name, int id) {
        this.threadGroup = threadGroup;
        this.name = name;
        this.target = target;
        this.id = id;

        if (threadGroup != null) {
            array(threadGroup.childs).push(this);
        }
    }


    public static void sleep(long millis) {
        // busy wait
        double to = new Date().getTime() + millis;
        while (new Date().getTime() < to) {
            Thread.yield();
        }
    }

    public static void sleep(long millis, int nanos) {
        // busy wait
        double to = performance.now() + millis + nanos / 1000000d;
        while (performance.now() < to) {
            Thread.yield();
        }
    }

    public static void yield() {
    }

    public static boolean interrupted() {
        if (current == null) return false;

        boolean res = current._interrupted;
        current._interrupted = false;
        return res;
    }

    public static int activeCount() {
        return current == null ? 0 : current.getThreadGroup().activeCount();
    }

    public static Thread currentThread() {
        return current;
    }

    public static int enumerate(Thread[] tarray) {
        return current == null ? 0 : current.getThreadGroup().enumerate(tarray);
    }

    public static void dumpStack() {
        System.err.println(new Error().stack);
    }

    private static def.js.Object createCommand(String command, Object data) {
        def.js.Object commandObj = new def.js.Object();
        commandObj.$set("command", command);
        commandObj.$set("data", data);
        return commandObj;
    }

    public static void onWorkerStart() {
        self.onmessage = Thread::onMessage;
        self.$set("onmessageerror", (Function<MessageEvent, def.js.Object>) (MessageEvent e) -> Thread.onMessageError(e));
    }

    public static def.js.Object onMessage(MessageEvent event) {
        def.js.Object eventData = object(event.data);
        def.js.Object commandData = eventData.$get("data");
        switch (eventData.$get("command").toString()) {
            case "threads": {
                ThreadGroup.updateWholeTree(commandData.$get("tree"));
                ThreadGroup main = ThreadGroup.getMain();
                main.list();
                Thread[] arr = new Thread[main.activeCount()];
                main.enumerate(arr);

                for (Thread thread : arr) {
                    if (thread.id == (int) commandData.$get("current")) {
                        current = thread;
                    }
                    if (thread.id == (int) commandData.$get("parent")) {
                        parent = thread;
                    }
                }

                ThreadGroup.ID_COUNTER = commandData.$get("maxgroup");
                Thread.ID_COUNTER = commandData.$get("maxthread");

                System.out.println("Current: " + currentThread());
                System.out.println("Parent: " + parent);
                break;
            }
            case "start": {
                Thread.currentThread().run();
                break;
            }
            default:
                System.err.println("Got an unrecognized command: " + eventData.$get("command") + " with data: " + commandData);
                break;
        }
        return self;
    }

    public static def.js.Object onMessageError(MessageEvent event) {
        System.err.println("Got a message error :( " + event.data);
        return self;
    }

    public static Object onError(Event event) {
        System.err.println("Got a error :( ");
        return self;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public String toString() {
        return this.getClass().getName() + '[' + name + ',' + getPriority() + ',' + getThreadGroup().getName() + ']';
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInterrupted() {
        return _interrupted;
    }

    public void run() {
        if (target != null) {
            target.run();
        }
    }

    public void interrupt() {
        _interrupted = true;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setDaemon(boolean on) {
    }

    public void destroy() {
        alive = false;
        // TODO call the upper thread to terminate us.
        if (parent != null) {
            // parent.worker.postMessage({""});
        }
    }

    public void start() {
        if (worker != null) {
            return;
        }

        alive = true;

        worker = new Worker(URL.createObjectURL(new Blob(new Object[]{"self.onmessage = e => { importScripts.apply(self, e.data); java.lang.Thread.importables = e.data; }"}, new BlobPropertyBag() {{
            $set("type", "text/javascript");
        }})));
        worker.onmessage = Thread::onMessage;
        worker.onerror = Thread::onError;
        worker.postMessage(importables);

        Object threadGroupCopy = JSON.parse(JSON.stringify(ThreadGroup.getMain(), (key, obj) -> {
            switch (key) {
                case "parent":
                case "threadGroup":
                    return undefined;
                case "target":
                    return obj == null ? obj : obj.toString();
            }
            return obj;
        }));

        int currentId = id;
        int parentId = current.id;
        worker.postMessage(createCommand("threads", new def.js.Object() {{
            $set("tree", threadGroupCopy);
            $set("current", currentId);
            $set("parent", parentId);
            $set("maxgroup", ThreadGroup.ID_COUNTER);
            $set("maxthread", Thread.ID_COUNTER);
        }}));

        worker.postMessage(createCommand("start", undefined));
    }

    void updateThread(Thread nakedThread) {
        this.name = nakedThread.name;
        this.alive = nakedThread.alive;
        this._interrupted = nakedThread._interrupted;
        this.priority = nakedThread.priority;
        this.daemon = nakedThread.daemon;
    }
}
