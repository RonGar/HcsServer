/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  com.google.common.reflect.TypeToken
 *  com.google.gson.Gson
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 */
package hcsmod.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import hcsmod.server.SPacketHandler;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapMarkersServer {
    public static Map<String, MarkerGroup> markerGroups = new LinkedHashMap<String, MarkerGroup>();

    public static void reloadMapMarkers() {
        MapMarkersServer.readMapMarkers();
        SPacketHandler.sendMapMarkers(null);
    }

    public static void readMapMarkers() {
        try (InputStreamReader in = new InputStreamReader((InputStream)new FileInputStream("hcsConfig/map/markers.json"), StandardCharsets.UTF_8);){
            Gson gson = new Gson();
            markerGroups.clear();
            markerGroups = (Map)gson.fromJson((Reader)in, new TypeToken<Map<String, MarkerGroup>>(){}.getType());
        }
        catch (JsonSyntaxException e) {
            throw new JsonParseException("\u041e\u0448\u0438\u0431\u043a\u0430 \u0432 hcsConfig/map/markers.json");
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static class MarkerData {
        int x;
        int z;
        int shiftX;
        int shiftY;
        String[] description;
    }

    public static class MarkerGroup {
        public String groupName;
        String iconName;
        String[] description;
        int iconSize;
        int iconShiftX;
        int iconShiftY;
        public List<MarkerData> markers;
    }
}

