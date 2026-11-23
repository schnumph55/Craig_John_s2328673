package org.me.craig_john_s2328673.model;

import java.util.ArrayList;
import java.util.List;

public class Location {
    private String role;
    private double latitude;
    private double longitude;
    private String roadName;
    private String pointName;
    private String junction;
    private List<String> otherNames = new ArrayList<>();

    public Location(String role) {
        this.role = role;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getRoadName() { return roadName; }
    public void setRoadName(String roadName) { this.roadName = roadName; }

    public String getPointName() { return pointName; }
    public void setPointName(String pointName) { this.pointName = pointName; }

    public String getJunction() { return junction; }
    public void setJunction(String junction) { this.junction = junction; }

    public List<String> getOtherNames() { return otherNames; }
    public void addOtherName(String name) { otherNames.add(name); }

    @Override
    public String toString() {
        return role + ": " + pointName + " (" + latitude + ", " + longitude + ")";
    }

    public void setOtherNames(List<String> otherNames) {
        this.otherNames = otherNames;
    }
}
