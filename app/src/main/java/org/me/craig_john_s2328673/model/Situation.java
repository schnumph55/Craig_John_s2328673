package org.me.craig_john_s2328673.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
public class Situation {
    private String id;
    private String recordType;
    private String subType;
    private String comment;
    private String roadName;
    private String direction;
    private String probability;
    private String compliance;
    private boolean roadworks;
    private boolean urgent;
    private OffsetDateTime lastUpdated;

    private Validity validity = new Validity();
    private Impact impact = new Impact();
    private List<Location> locations = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getProbability() { return probability; }
    public void setProbability(String probability) { this.probability = probability; }

    public String getCompliance() { return compliance; }
    public void setCompliance(String compliance) { this.compliance = compliance; }

    public boolean isRoadworks() { return roadworks; }
    public void setRoadworks(boolean roadworks) { this.roadworks = roadworks; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public OffsetDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(OffsetDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Validity getValidity() { return validity; }
    public Impact getImpact() { return impact; }

    public List<Location> getLocations() { return locations; }
    public void addLocation(Location l) { locations.add(l); }


    public Location getMainLocation() {
        for (Location l : locations) {
            if (l.getLatitude() != 0 || l.getLongitude() != 0) return l;
        }
        return null;
    }

    @Override
    public String toString() {
        return recordType + " [" + subType + "] " + roadName + ": " + comment;
    }
}
