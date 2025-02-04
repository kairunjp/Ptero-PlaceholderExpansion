package jp.kairun.pteroPlaceholderExpansion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
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
    private static final String DEFAULT_ERROR_MSG = "ERROR:%error%";

    private String apiUrl = DEFAULT_API_URL;
    private String apiKey = DEFAULT_API_KEY;
    private Integer cacheTime = DEFAULT_CACHE_TIME;
    private String errorMsg = DEFAULT_ERROR_MSG;

    private Cache<String, JsonObject> cache;

    private String errorMsg(String detail) {
        return errorMsg.replace("%error%", detail);
    }

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
        return "1.0.4";
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
        if (split.length != 2) return errorMsg("Invalid Format");

        String serverId = split[0];
        String path = split[1];

        JsonObject serverData = cache.getIfPresent(serverId);
        if (serverData == null) {
            // Put Empty JsonObject first to prevent multiple requests for the same server
            cache.put(serverId, new JsonObject());

            // Get Server Details
            String response = fetchDetails(serverId, "");
            if (response == null) return errorMsg("Fetch Failed");
            serverData = parseDetails(response);
            if (serverData == null) return errorMsg("Parse Failed");
            cache.put(serverId, serverData);

            // Get Resources Details
            String resourcesResponse = fetchDetails(serverId, "/resources");
            if (resourcesResponse == null) return errorMsg("Fetch Resources Failed");
            JsonObject resourcesData = parseDetails(resourcesResponse);
            if (resourcesData == null) return errorMsg("Parse Resources Failed");
            serverData.addProperty("current_state", resourcesData.get("current_state").getAsString());
            serverData.add("resources", resourcesData.getAsJsonObject("resources"));
        }

        JsonElement currentElement = serverData;
        String[] pathSplit = path.split("\\.");
        for (String key : pathSplit) {
            if (key.matches(".*\\[\\d+]$")) {
                String arrayKey = key.substring(0, key.indexOf("["));
                int index = Integer.parseInt(key.substring(key.indexOf("[") + 1, key.indexOf("]")));
                if (currentElement.getAsJsonObject().has(arrayKey)) {
                    JsonArray jsonArray = currentElement.getAsJsonObject().get(arrayKey).getAsJsonArray();
                    if (index < jsonArray.size()) {
                        currentElement = jsonArray.get(index);
                    } else {
                        return errorMsg("Index Out of Bound");
                    }
                } else {
                    return errorMsg("Array Not Found");
                }
            } else {
                if (currentElement.getAsJsonObject().has(key)) {
                    currentElement = currentElement.getAsJsonObject().get(key);
                } else {
                    return errorMsg("Key Not Found");
                }
            }
        }

        if (!currentElement.isJsonPrimitive()) {
            return errorMsg("Value is Object");
        }

        return currentElement.getAsString();
    }

    private String fetchDetails(String serverId, String path) {
        try {
            URL url = new URL(apiUrl + "/api/client/servers/" + serverId + path);
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

    private JsonObject parseDetails(String response) {
        try {
            return JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("attributes");
        } catch (Exception e) {
            return null;
        }
    }
}
