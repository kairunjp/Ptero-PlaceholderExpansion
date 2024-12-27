package jp.kairun.pteroPlaceholderExpansion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PteroPlaceholderExpansion extends PlaceholderExpansion implements Cacheable, Configurable {
    private static final String DEFAULT_API_URL = "https://panel.example.com";
    private static final String DEFAULT_API_KEY = "ptlc_000000000000000000000000000000000000000000";
    private static final int DEFAULT_CACHE_TIME = 60;
    private static final String DEFAULT_ERROR_MSG = "サーバーデータの取得中にエラーが発生しました";

    private String apiUrl = DEFAULT_API_URL;
    private String apiKey = DEFAULT_API_KEY;
    private Integer cacheTime = DEFAULT_CACHE_TIME;
    private String errorMsg = DEFAULT_ERROR_MSG;

    private Cache<String, JsonObject> cache;

    @Override
    public @NotNull String getIdentifier() {
        return "ptero";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Kairun";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean canRegister() {
        this.apiUrl = getString("pterodactyl.url", DEFAULT_API_URL);
        this.apiKey = getString("pterodactyl.apiKey", DEFAULT_API_KEY);
        this.cacheTime = getInt("cacheTime", DEFAULT_CACHE_TIME);
        this.errorMsg = getString("error_msg", DEFAULT_ERROR_MSG);

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheTime, TimeUnit.SECONDS)
                .build();

        return true;
    }

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
                .put("pterodactyl.url", DEFAULT_API_URL)
                .put("pterodactyl.apiKey", DEFAULT_API_KEY)
                .put("cacheTime", DEFAULT_CACHE_TIME)
                .put("error_msg", DEFAULT_ERROR_MSG)
                .build();
    }

    @Override
    public void clear() {
        if (cache != null) cache.invalidateAll();

        this.apiUrl = null;
        this.apiKey = null;
        this.cacheTime = null;
        this.errorMsg = null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] split = params.split("_", 2);
        if (split.length != 2) return errorMsg;

        String serverId = split[0];
        String path = "attributes." + split[1];

        JsonObject serverData = cache.getIfPresent(serverId);
        if (serverData == null) {
            String logData = getServerDetails(serverId);
            if (logData != null) {
                serverData = JsonParser.parseString(logData).getAsJsonObject();
                cache.put(serverId, serverData);
            } else {
                return errorMsg;
            }
        }

        String[] pathSplit = path.split("\\.");
        JsonObject attributes = serverData;
        for (int i = 0; i < pathSplit.length; i++) {
            if (attributes.has(pathSplit[i])) {
                if (i == pathSplit.length - 1) {
                    return attributes.get(pathSplit[i]).getAsString();
                } else {
                    attributes = attributes.get(pathSplit[i]).getAsJsonObject();
                }
            } else {
                return errorMsg;
            }
        }
        return errorMsg;
    }

    private String getServerDetails(String serverId) {
        try {
            URL url = new URL(apiUrl + "/api/client/servers/" + serverId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            StringBuilder response = new StringBuilder();
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
