package org.me.craig_john_s2328673.data;

import org.me.craig_john_s2328673.model.Situation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;


public class FeedDownloader {
    private static final int TIMEOUT_MS = 15000;
    private final String clientId;
    private final String clientKey;

    public FeedDownloader(String clientId, String clientKey) {
        this.clientId = clientId;
        this.clientKey = clientKey;
    }

    public String download(String urlString) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            String auth = Base64.getEncoder().encodeToString(
                    (clientId + ":" + clientKey).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", auth);

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned HTTP " + code);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) sb.append(line).append('\n');
            }

            String result = sb.toString();
            int start = result.indexOf("<?");
            if (start == -1) throw new IOException("Response was not XML");
            return result.substring(start);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}