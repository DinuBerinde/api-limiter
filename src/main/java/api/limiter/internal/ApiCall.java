package api.limiter.internal;

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
