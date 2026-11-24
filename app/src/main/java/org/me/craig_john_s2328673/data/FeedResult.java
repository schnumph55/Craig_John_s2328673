package org.me.craig_john_s2328673.data;

import org.me.craig_john_s2328673.model.Situation;
import java.util.ArrayList;

public class FeedResult {
    public ArrayList<Situation> incidents = new ArrayList<>();
    public ArrayList<Situation> roadworks = new ArrayList<>();
    public boolean live = true;
    public boolean usedSample = false;
}