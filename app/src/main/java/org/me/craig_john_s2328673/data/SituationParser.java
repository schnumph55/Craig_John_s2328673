package org.me.craig_john_s2328673.data;

import android.util.Log;

import org.me.craig_john_s2328673.model.Location;
import org.me.craig_john_s2328673.model.Situation;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class SituationParser {

    private static final String TAG = "SituationParser";

    public ArrayList<Situation> parse(String xml, boolean isRoadworks) {
        ArrayList<Situation> situations = new ArrayList<>();
        if (xml == null || xml.isEmpty()) return situations;

        Situation current = null;
        Location currentLoc = null;
        boolean inComment = false;
        String pendingName = null;
        String text = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xml));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    text = "";

                    if (tag.equalsIgnoreCase("situation")) {
                        current = new Situation();
                        current.setId(xpp.getAttributeValue(null, "id"));
                        current.setRoadworks(isRoadworks);

                    } else if (tag.equalsIgnoreCase("situationRecord") && current != null) {
                        current.setRecordType(getTypeAttribute(xpp));

                    } else if (tag.equalsIgnoreCase("generalPublicComment")) {
                        inComment = true;

                    } else if ((tag.equalsIgnoreCase("from") || tag.equalsIgnoreCase("to"))
                            && current != null) {
                        currentLoc = new Location(tag.toLowerCase());
                    }

                } else if (eventType == XmlPullParser.TEXT) {
                    text = xpp.getText().trim();

                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();
                    if (current != null) {
                        handleEndTag(tag, text, current, currentLoc, inComment, pendingName);
                    }
                    if (tag.equalsIgnoreCase("value") && !inComment && currentLoc != null) {
                        pendingName = text;
                    } else if (tag.equalsIgnoreCase("generalPublicComment")) {
                        inComment = false;
                    } else if ((tag.equalsIgnoreCase("from") || tag.equalsIgnoreCase("to"))
                            && current != null && currentLoc != null) {
                        current.addLocation(currentLoc);
                        currentLoc = null;
                    } else if (tag.equalsIgnoreCase("situation") && current != null) {
                        finishSituation(current);
                        situations.add(current);
                        current = null;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Parsing error – returning what was parsed so far", e);
        }

        Log.d(TAG, "Parsed " + situations.size()
                + (isRoadworks ? " roadworks" : " incidents"));
        return situations;
    }


    private void handleEndTag(String tag, String text, Situation s, Location loc,
                              boolean inComment, String pendingName) {
        switch (tag) {

            case "value":
                if (inComment) s.setComment(text);
                break;
            case "situationRecordVersionTime":
                s.setLastUpdated(parseDate(text));
                break;
            case "probabilityOfOccurrence":
                s.setProbability(text);
                break;
            case "complianceOption":
                s.setCompliance(text);
                break;
            case "tpegDirection":
                s.setDirection(text);
                break;
            case "urgentRoadworks":
                s.setUrgent(Boolean.parseBoolean(text));
                break;

            case "overallStartTime":
                s.getValidity().setStart(parseDate(text));
                break;
            case "overallEndTime":
                s.getValidity().setEnd(parseDate(text));
                break;

            case "numberOfLanesRestricted":
                s.getImpact().setLanesRestricted(parseInteger(text));
                break;
            case "numberOfOperationalLanes":
                s.getImpact().setOperationalLanes(parseInteger(text));
                break;
            case "delaysType":
                s.getImpact().setDelaysType(text);
                break;


            case "roadOrCarriagewayOrLaneManagementType":
            case "abnormalTrafficType":
            case "vehicleObstructionType":
            case "obstructionType":
            case "roadMaintenanceType":
                s.setSubType(text);
                break;


            case "latitude":
                if (loc != null) loc.setLatitude(parseDouble(text));
                break;
            case "longitude":
                if (loc != null) loc.setLongitude(parseDouble(text));
                break;


            case "tpegIlcPointDescriptorType":
                if (loc != null && pendingName != null) {
                    if (text.equals("tpegIlcName1")) loc.setRoadName(pendingName);
                    else loc.addOtherName(pendingName);
                }
                break;
            case "tpegOtherPointDescriptorType":
                if (loc != null && pendingName != null) {
                    if (text.equals("linkName")) loc.setRoadName(pendingName);
                    else if (text.equals("intersectionName") || text.equals("nonLinkedPointName"))
                        loc.setPointName(pendingName);
                    else loc.addOtherName(pendingName);
                }
                break;
            case "tpegJunctionPointDescriptorType":
                if (loc != null && pendingName != null) loc.setJunction(pendingName);
                break;

            default:
                break;
        }
    }


    private void finishSituation(Situation s) {
        for (Location l : s.getLocations()) {
            if (l.getRoadName() != null && !l.getRoadName().isEmpty()) {
                s.setRoadName(l.getRoadName());
                return;
            }
        }
        s.setRoadName(extractRoadFromComment(s.getComment()));
    }


    static String extractRoadFromComment(String comment) {
        if (comment == null) return "Unknown";
        String[] words = comment.split("\\s+");
        for (String w : words) {
            String clean = w.replace(",", "").replace(".", "");
            if (clean.length() >= 2
                    && (clean.charAt(0) == 'M' || clean.charAt(0) == 'A' || clean.charAt(0) == 'B')
                    && Character.isDigit(clean.charAt(1))) {
                return clean;
            }
        }
        return "Unknown";
    }


    private String getTypeAttribute(XmlPullParser xpp) {
        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals("type")) {
                return xpp.getAttributeValue(i);
            }
        }
        return xpp.getAttributeCount() > 0 ? xpp.getAttributeValue(0) : "Unknown";
    }



    private OffsetDateTime parseDate(String s) {
        try { return OffsetDateTime.parse(s); }
        catch (DateTimeParseException | NullPointerException e) { return null; }
    }

    private Integer parseInteger(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
