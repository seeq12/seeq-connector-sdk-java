package com.mycompany.seeq.link.connector;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.common.math.LongMath;
import com.seeq.link.sdk.utilities.TimeInstant;


public class DatasourceSimulator {
    // To be able to yield consistent, reproducible tag values, we need a constant seed. This helps us
    // approximate the behaviour of a real datasource which should be deterministic.
    private static final int RANDOMNESS_SEED = 1_000_000;

    private static final Random RNG = new Random(RANDOMNESS_SEED);

    private boolean connected;
    private Duration samplePeriod;
    private Duration signalPeriod;

    public DatasourceSimulator(Duration samplePeriod, Duration signalPeriod) {
        this.samplePeriod = samplePeriod;
        this.signalPeriod = signalPeriod;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public boolean connect() {
        this.connected = true;

        return true;
    }

    public void disconnect() {}

    public Iterable<Element> getDatabases() {
        int databaseCount = RNG.nextInt(10);
        return () -> IntStream.rangeClosed(1, databaseCount)
                .mapToObj(Element::new)
                .iterator();
    }

    public Iterable<Alarm> getAlarmsForDatabase(String elementId) {
        int alarmCount = RNG.nextInt(10);
        return () -> IntStream.rangeClosed(1, alarmCount)
                .mapToObj(alarmId -> new Alarm(elementId, alarmId))
                .iterator();
    }

    public Iterable<Tag> getTagsForDatabase(String elementId) {
        int tagCount = RNG.nextInt(10);
        return () -> IntStream.rangeClosed(1, tagCount)
                .mapToObj(tagId -> new Tag(elementId, tagId, tagId % 2 == 0))
                .iterator();
    }

    public Iterable<Constant> getConstantsForDatabase(String elementId) {
        int constantCount = RNG.nextInt(10);
        return () -> IntStream.rangeClosed(1, constantCount)
                .mapToObj(constId -> new Constant(elementId, constId, "°C", constId * 10))
                .iterator();
    }

    public enum Waveform {
        SINE
    }

    public Stream<Tag.Value> getTagValues(String dataId, TimeInstant startTimestamp, TimeInstant endTimestamp,
            int limit) {
        long samplePeriodInNanos = this.samplePeriod.toNanos();
        return LongStream.rangeClosed(
                        LongMath.divide(startTimestamp.getTimestamp(), samplePeriodInNanos, RoundingMode.FLOOR),
                        LongMath.divide(endTimestamp.getTimestamp(), samplePeriodInNanos, RoundingMode.CEILING)
                )
                .mapToObj(index -> {
                    TimeInstant key = new TimeInstant(index * samplePeriodInNanos);
                    double value = getWaveformValue(Waveform.SINE, key.getTimestamp());
                    return new Tag.Value(key, value);
                })
                .limit(limit);
    }

    public Stream<Alarm.Event> getAlarmEvents(String dataId, TimeInstant startTimestamp, TimeInstant endTimestamp,
            int limit) {
        long capsulePeriodInNanos = this.samplePeriod.toNanos();
        return LongStream.rangeClosed(
                        LongMath.divide(startTimestamp.getTimestamp(), capsulePeriodInNanos, RoundingMode.FLOOR),
                        LongMath.divide(endTimestamp.getTimestamp(), capsulePeriodInNanos, RoundingMode.CEILING)
                )
                .mapToObj(index -> {
                    ZonedDateTime start = new TimeInstant(index * capsulePeriodInNanos).toDateTime();
                    ZonedDateTime end = ChronoUnit.MILLIS.addTo(start, 10L);
                    return new Alarm.Event(start, end, RNG.nextDouble());
                })
                .limit(limit);
    }

    private double getWaveformValue(Waveform waveform, long timestamp) {
        long signalPeriodInNanos = this.signalPeriod.toNanos();
        double waveFraction = ((double) timestamp % signalPeriodInNanos) / signalPeriodInNanos;
        double value;

        switch (waveform) {
        default:
        case SINE:
            value = Math.sin(waveFraction * 2.0d * Math.PI);
            break;
        }

        return value;
    }

    // NOTE: the data structures in this file are purely for illustration purposes only
    // and are here solely to approximate datasource response structures for syncing

    /**
     * This class defines an element that can be used for syncing assets
     */
    public static class Element {
        private final String id;
        private final String name;

        public Element(int elementId) {
            this.id = Integer.toString(elementId);
            this.name = String.format("Simulated Element #%d", elementId);
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * This class defines an alarm that can be used for syncing conditions
     */
    public static class Alarm {
        private final String id;
        private final String name;

        public Alarm(String elementId, int alarmId) {
            this.id = String.format("Element=%s;Alarm=%d", elementId, alarmId);
            this.name = String.format("Simulated Alarm #%d", alarmId);
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static class Event {
            private final ZonedDateTime start;
            private final ZonedDateTime end;
            private final Object intensity;

            public Event(ZonedDateTime start, ZonedDateTime end, Object intensity) {
                this.start = start;
                this.end = end;
                this.intensity = intensity;
            }

            public ZonedDateTime getStart() {
                return this.start;
            }

            public ZonedDateTime getEnd() {
                return this.end;
            }

            public Object getIntensity() {
                return this.intensity;
            }
        }
    }

    /**
     * This class defines a tag that can be used for syncing signals
     */
    public static class Tag {
        private final String id;
        private final String name;
        private final boolean stepped;

        public Tag(String elementId, int tagId, boolean stepped) {
            this.id = String.format("Element=%s;Tag=%d", elementId, tagId);
            this.name = String.format("Simulated Tag #%d", tagId);
            this.stepped = stepped;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public boolean getStepped() {
            return this.stepped;
        }

        public static class Value {
            private final TimeInstant timestamp;
            private final Object measure;

            public Value(TimeInstant timestamp, Object value) {
                this.timestamp = timestamp;
                this.measure = value;
            }

            public TimeInstant getTimestamp() {
                return this.timestamp;
            }

            public Object getMeasure() {
                return this.measure;
            }
        }
    }

    /**
     * This class defines a constant that can be used for syncing scalars
     */
    public static class Constant {
        private final String id;
        private final String name;
        private final String unitOfMeasure;
        private final Object value;

        public Constant(String elementId, int constantId, String unitOfMeasure, Object value) {
            this.id = String.format("Element=%s;Constant=%d", elementId, constantId);
            this.name = String.format("Simulated Constant #%d", constantId);
            this.unitOfMeasure = unitOfMeasure;
            this.value = value;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getUnitOfMeasure() {
            return unitOfMeasure;
        }

        public Object getValue() {
            return value;
        }
    }
}
