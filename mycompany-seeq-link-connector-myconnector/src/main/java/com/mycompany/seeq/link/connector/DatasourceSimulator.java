package com.mycompany.seeq.link.connector;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Random;

import com.seeq.link.sdk.utilities.TimeInstant;

public class DatasourceSimulator {
    private boolean connected;
    private final int tagCount;
    Duration signalPeriod;

    public static class Tag {
        private final int id;
        private final String name;
        private final boolean stepped;

        public Tag(int id, String name, boolean stepped) {
            this.id = id;
            this.name = name;
            this.stepped = stepped;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public boolean getStepped() {
            return this.stepped;
        }
    }

    // This is NOT intended for production use and is solely to model possible
    // datasource response structures.
    public class TagValue {
        private final ZonedDateTime start;
        private final ZonedDateTime end;
        private final double value;

        public TagValue(ZonedDateTime start, ZonedDateTime end, double value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public ZonedDateTime getStart() {
            return start;
        }

        public ZonedDateTime getEnd() {
            return end;
        }

        public double getValue() {
            return value;
        }
    }

    public DatasourceSimulator(int tagCount, Duration signalPeriod) {
        this.tagCount = tagCount;
        this.signalPeriod = signalPeriod;
    }

    public boolean connect() {
        this.connected = true;

        return true;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void disconnect() {}

    public int getTagCount() {
        return this.tagCount;
    }

    public Iterator<Tag> getTags() {
        return new Iterator<Tag>() {
            private int nextTagNumber = 0;

            @Override
            public boolean hasNext() {
                return (this.nextTagNumber < DatasourceSimulator.this.tagCount);
            }

            @Override
            public Tag next() {
                this.nextTagNumber++;
                return new Tag(
                        this.nextTagNumber,
                        String.format("Simulated Tag #%d", this.nextTagNumber),
                        this.nextTagNumber % 2 == 0);
            }
        };
    }

    public enum Waveform {
        SINE
    }

    double query(Waveform waveform, long timestamp) {
        double waveFraction = ((double) timestamp % this.signalPeriod.toNanos()) / this.signalPeriod.toNanos();
        double value;

        switch (waveform) {
        default:
        case SINE:
            value = Math.sin(waveFraction * 2.0d * Math.PI);
            break;
        }

        return value;
    }

    public Iterator<TagValue> query(String dataId, TimeInstant startTimestamp, TimeInstant endTimestamp, int limit) {
        // To be able to yield consistent, reproducible tag values, we need a constant seed. This helps us
        // approximate the behaviour of a real datasource which should be deterministic.
        final int seed = 1_000_000;
        Random random = new Random(seed);
        ZonedDateTime startTime = startTimestamp.toDateTime();
        ZonedDateTime endTime = endTimestamp.toDateTime();
        long timespanInMs = (Duration.between(startTime, endTime)).toMillis();
        long timestampIncrement = timespanInMs / limit;

        return new Iterator<TagValue>() {
            private int index = 1;

            @Override
            public boolean hasNext() {
                return this.index <= limit;
            }

            @Override
            public TagValue next() {
                ZonedDateTime start = startTime.plus(timestampIncrement * this.index, ChronoUnit.MILLIS);
                ZonedDateTime end = start.plus(10, ChronoUnit.MILLIS);
                this.index++;
                return new TagValue(start, end, random.nextDouble());
            }
        };
    }
}
