package org.me.craig_john_s2328673.model;

import java.time.Duration;
import java.time.OffsetDateTime;

public class Validity {
    private OffsetDateTime start;
    private OffsetDateTime end;

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public boolean hasEnd() { return end != null; }


    public long getDurationDays() {
        if (start == null || end == null) return -1;
        return Duration.between(start, end).toDays();
    }
}
