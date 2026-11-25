package org.me.craig_john_s2328673.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.model.Situation;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TrafficRepository {
    private static final String TAG = "TrafficRepository";

    public static final String INCIDENTS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    public static final String ROADWORKS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    private static final String INCIDENTS_CACHE = "incidents_cache.xml";
    private static final String ROADWORKS_CACHE = "roadworks_cache.xml";

    private final Context context;
    private final FeedDownloader downloader;
    private final SituationParser parser = new SituationParser();

    public TrafficRepository(Context context) {
        this.context = context.getApplicationContext();
        String id = "", key = "";
        try {
            Bundle meta = this.context.getPackageManager()
                    .getApplicationInfo(this.context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            id = meta.getString("org.me.craig_john_s2328673.TS_CLIENT_ID");
            key = meta.getString("org.me.craig_john_s2328673.TS_CLIENT_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not read API keys", e);
        }
        downloader = new FeedDownloader(id, key);
    }

    public FeedResult loadAll() {
        FeedResult result = new FeedResult();
        result.incidents = loadFeed(INCIDENTS_URL, INCIDENTS_CACHE, R.raw.unplanned_example3, false, result);
        result.roadworks = loadFeed(ROADWORKS_URL, ROADWORKS_CACHE, R.raw.roadworks_example3, true, result);
        return result;
    }

    private ArrayList<Situation> loadFeed(String url, String cacheFile, int sampleRes,
                                          boolean roadworks, FeedResult result) {
        String xml;
        try {
            xml = downloader.download(url);
            Log.d(TAG, "Raw feed " + url.substring(url.indexOf("publications/") + 13)
                    + " contains " + (xml.split("<situation ").length - 1) + " <situation> elements");
            writeFile(cacheFile, xml);
        } catch (IOException e) {
            Log.w(TAG, "Download failed (" + e.getMessage() + "), using offline copy");
            result.live = false;
            xml = readFile(cacheFile);
            if (xml == null) {
                xml = readRaw(sampleRes);
                result.usedSample = true;
            }
        }
        return parser.parse(xml, roadworks);
    }
    private void writeFile(String name, String data) {
        try (FileOutputStream out = context.openFileOutput(name, Context.MODE_PRIVATE)) {
            out.write(data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "Could not save cache " + name, e);
        }
    }

    private String readFile(String name) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                context.openFileInput(name), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private String readRaw(int resId) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(resId), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }
}