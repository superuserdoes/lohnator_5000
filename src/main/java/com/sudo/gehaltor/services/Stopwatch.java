package com.sudo.gehaltor.services;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;

import java.util.concurrent.TimeUnit;

public class Stopwatch {
    // Credit: https://stackoverflow.com/a/61333767
    private static long toMillis(long nanos) { return TimeUnit.NANOSECONDS.toMillis(nanos); }

    // value is in milliseconds
    private final ReadOnlyLongWrapper elapsedTime = new ReadOnlyLongWrapper(this, "elapsedTime");

    private void setElapsedTime(long elapsedTime) {
        this.elapsedTime.set(elapsedTime);
    }

    public final long getElapsedTime() {
        return elapsedTime.get();
    }

    public final ReadOnlyLongProperty elapsedTimeProperty() {
        return elapsedTime.getReadOnlyProperty();
    }

    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, "running");

    private void setRunning(boolean running) {
        this.running.set(running);
    }

    public final boolean isRunning() {
        return running.get();
    }

    public final ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    private final Timer timer = new Timer();

    public void start() {
        if (!isRunning()) {
            timer.start();
            setRunning(true);
        }
    }

    public void stop() {
        if (isRunning()) {
            timer.pause();
            setRunning(false);
        }
    }

    public void reset() {
        timer.stopAndReset();
        setElapsedTime(0);
        setRunning(false);
    }

    private class Timer extends AnimationTimer {

        private long originTime = Long.MIN_VALUE;
        private long pauseTime = Long.MIN_VALUE;
        private boolean pausing;

        @Override
        public void handle(long now) {
            if (pausing) {
                pauseTime = toMillis(now);
                pausing = false;
                stop();
            } else {
                if (originTime == Long.MIN_VALUE) {
                    originTime = toMillis(now);
                } else if (pauseTime != Long.MIN_VALUE) {
                    originTime += toMillis(now) - pauseTime;
                    pauseTime = Long.MIN_VALUE;
                }

                setElapsedTime(toMillis(now) - originTime);
            }
        }

        @Override
        public void start() {
            pausing = false;
            super.start();
        }

        void pause() {
            if (originTime != Long.MIN_VALUE) {
                pausing = true;
            } else {
                stop();
            }
        }

        void stopAndReset() {
            stop();
            originTime = Long.MIN_VALUE;
            pauseTime = Long.MIN_VALUE;
            pausing = false;
        }
    }
}