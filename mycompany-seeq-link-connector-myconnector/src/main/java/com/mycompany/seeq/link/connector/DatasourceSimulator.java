package com.mycompany.seeq.link.connector;

import java.time.Duration;
import java.util.Iterator;

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
}
