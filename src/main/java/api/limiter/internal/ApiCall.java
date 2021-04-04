package api.limiter.internal;

import net.jcip.annotations.Immutable;

/**
 * Class to keep track of the number of calls and time for an API.
 */
@Immutable
class ApiCall {
    private final int numberOfCalls;
    private final long time;

    ApiCall(int numberOfCalls, long time) {
        this.numberOfCalls = numberOfCalls;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }
}
