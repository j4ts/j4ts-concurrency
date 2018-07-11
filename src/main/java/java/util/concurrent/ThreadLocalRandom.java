package java.util.concurrent;

import def.js.Date;

import java.util.Random;
import static def.dom.Globals.self;

public class ThreadLocalRandom extends Random {
    private ThreadLocalRandom() {
        super(self.name.hashCode() ^ (long) new Date().getTime());
    }

    private static ThreadLocalRandom threadLocalRandom;

    static ThreadLocalRandom current() {
        if (threadLocalRandom == null) {
            threadLocalRandom = new ThreadLocalRandom();
        }
        return threadLocalRandom;
    }
}
