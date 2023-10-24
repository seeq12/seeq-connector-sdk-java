package com.mycompany.seeq.link.connector;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.seeq.link.sdk.utilities.TimeInstant;


public class DatasourceSimulator {
    public class Element {
        private String id;
        private String name;

        public Element(int elementId) {
            this.id = Integer.toString(elementId);
            this.name = "Simulated Element #" + elementId;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }
    }

    public class Alarm {
        private String id;
        private String name;

        public Alarm(String elementId, int alarmId) {
            this.id = String.format("Element=%s;Alarm=%d", elementId, alarmId);
            this.name = "Simulated Alarm #" + alarmId;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static class Event {
            private ZonedDateTime start;
            private ZonedDateTime end;
            private double intensity;

            public Event(ZonedDateTime start, ZonedDateTime end, double intensity) {
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

            public double getIntensity() {
                return this.intensity;
            }
        }
    }

    public class Tag {
        private String id;
        private String name;
        private boolean stepped;

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
            private TimeInstant timestamp;
            private double measure;

            public Value(TimeInstant timestamp, double value) {
                this.timestamp = timestamp;
                this.measure = value;
            }

            public TimeInstant getTimestamp() {
                return this.timestamp;
            }

            public double getMeasure() {
                return this.measure;
            }
        }
    }
    public class Constant {
        private String id;
        private String name;
        private  String unitOfMeasure;
        private  Object value;

        public Constant(int elementId, int constantId, String unitOfMeasure, Object value) {
            this.id = String.format("Element=%d;Constant=%d", elementId, constantId);
            this.name = String.format("Simulated Constant #%d", elementId);
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

    // To be able to yield consistent, reproducible tag values, we need a constant seed. This helps us
    // approximate the behaviour of a real datasource which should be deterministic.
    private final int RandomnessSeed = 1_000_000;

    private final Random RNG = new Random(RandomnessSeed);

    private boolean connected;
    private Duration signalPeriod;

    public DatasourceSimulator(Duration signalPeriod) {
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
        return IntStream.range(1, databaseCount + 1)
                .mapToObj(Element::new)
                .collect(Collectors.toList());
    }

    public Iterable<Alarm> getAlarmsForDatabase(String elementId) {
        int alarmCount = RNG.nextInt(10);
        return IntStream.range(1, alarmCount + 1)
                .mapToObj(alarmId -> new Alarm(elementId, alarmId))
                .collect(Collectors.toList());
    }

    public Iterable<Tag> getTagsForDatabase(String elementId) {
        int tagCount = RNG.nextInt(10);
        return IntStream.range(1, tagCount + 1)
                .mapToObj(tagId -> new Tag(elementId, tagId, tagId % 2 == 0))
                .collect(Collectors.toList());
    }

    public enum Waveform {
        SINE
    }

    public Iterable<Tag.Value> getTagValues(String dataId, TimeInstant startTimestamp, TimeInstant endTimestamp, int limit) {
        long samplePeriodInNanos = this.signalPeriod.toNanos();
        long leftBoundTimestamp = startTimestamp.getTimestamp() / samplePeriodInNanos;
        long rightBoundTimestamp = (endTimestamp.getTimestamp() + samplePeriodInNanos - 1) / samplePeriodInNanos;

        return () -> new Iterator<>() {
            private long sampleIndex = leftBoundTimestamp;
            private long count = 0;

            @Override
            public boolean hasNext() {
                return sampleIndex <= rightBoundTimestamp && count < limit;
            }

            @Override
            public Tag.Value next() {
                if (!hasNext()) {
                    throw new RuntimeException();
                }

                TimeInstant key = new TimeInstant(sampleIndex * samplePeriodInNanos);
                double value = getWaveformValue(Waveform.SINE, key.getTimestamp());
                sampleIndex++;
                count++;
                return new Tag.Value(key, value);
            }
        };
    }

    public Iterable<Alarm.Event> getAlarmEvents(String dataId, TimeInstant startTimestamp, TimeInstant endTimestamp, int limit) {
        ZonedDateTime startTime = startTimestamp.toDateTime();
        ZonedDateTime endTime = endTimestamp.toDateTime();
        long timespanInMs = ChronoUnit.MILLIS.between(startTime, endTime);
        long timestampIncrement = timespanInMs / limit;

        return () -> new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < limit;
            }

            @Override
            public Alarm.Event next() {
                if (!hasNext()) {
                    throw new RuntimeException();
                }

                ZonedDateTime start = ChronoUnit.MILLIS.addTo(startTime, timestampIncrement * i);
                ZonedDateTime end = ChronoUnit.MILLIS.addTo(start, 10L);
                double randomValue = Math.random();
                i++;

                return new Alarm.Event(start, end, randomValue);
            }
        };
    }

    private double getWaveformValue(Waveform waveform, long timestamp) {
        long signalPeriodInNanos = this.signalPeriod.toNanos();
        double waveFraction = ((double)timestamp % signalPeriodInNanos) / signalPeriodInNanos;
        double value;

        switch (waveform) {
        default:
        case SINE:
            value = Math.sin(waveFraction * 2.0d * Math.PI);
            break;
        }

        return value;
    }
}
