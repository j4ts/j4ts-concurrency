package java.util.concurrent;

public enum TimeUnit {
    NANOSECONDS(1L),
    MICROSECONDS(1000L),
    MILLISECONDS(1000L*1000L),
    SECONDS(1000L*1000L*1000L),
    MINUTES(1000L*1000L*1000L*60L),
    HOURS(1000L*1000L*1000L*60L*60L),
    DAYS(1000L*1000L*1000L*60L*60L*24L);

    private final long toNano;

    TimeUnit(long toNano) {
        this.toNano = toNano;
    }

    public long convert(long duration, TimeUnit toUnit) {
        return (long) ((double) duration / (double) toUnit.toNano * (double) toNano);
    }

    public long toNanos(long duration) {
        return convert(duration, NANOSECONDS);
    }

    public long toMicros(long duration) {
        return convert(duration, MICROSECONDS);
    }

    public long toMillis(long duration) {
        return convert(duration, MILLISECONDS);
    }

    public long toSeconds(long duration) {
        return convert(duration, SECONDS);
    }

    public long toMinutes(long duration) {
        return convert(duration, MINUTES);
    }

    public long toHours(long duration) {
        return convert(duration, HOURS);
    }

    public long toDays(long duration) {
        return convert(duration, DAYS);
    }

    public void sleep(long duration) throws InterruptedException {
        if (duration > 0L) {
            long millisecs = this.toMillis(duration);
            long nanosecs = this == NANOSECONDS ? duration % 1000000 :
                    this == MICROSECONDS ? duration % 1000 * 1000 : 0;
            Thread.sleep(millisecs, (int) nanosecs);
        }
    }

}
