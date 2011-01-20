package com.nfolkert.googlemaps;

import com.nfolkert.kiva.properties.KivaProperties;
import com.nfolkert.utils.*;
import com.nfolkert.exception.BaseRuntimeException;
import com.nfolkert.json.JSONObject;
import com.nfolkert.json.JSONArray;
import com.nfolkert.collections.ListOrderedMap;

import java.io.File;
import java.util.Map;
import java.net.URLEncoder;

/**
 */
public class GeoCodeManager
{
    private static final File kGeoCodeCache = new File(KivaProperties._kivaProps.getProperty("geoCodeCache"));

    Map<String, Pair<Double, Double>> _map = new ListOrderedMap<String, Pair<Double, Double>>();
    int _lookups;

    private static GeoCodeManager _instance;

    public static GeoCodeManager getInstance()
    {
        if (_instance == null)
            _instance = readInstance();
        return _instance;
    }

    private void addGeoCode(String location, double latitude, double longitude)
    {
        _map.put(location, new Pair<Double, Double>(latitude, longitude));
    }

    public static Pair<Double, Double> getGeoCode(String location)
            throws Exception
    {
        final GeoCodeManager gcm = getInstance();
        Pair<Double, Double> res = gcm._map.get(location);
        if (res != null) return res;

        res = gcm.lookupAndCache(location);

        return res;
    }

    private static final String gMapsRootUrl = "http://maps.google.com/maps/geo";

    private static final String kGoogleAPIKey = KivaProperties._kivaProps.getProperty("googleGeoCodeAPIKey");
    public Pair<Double, Double> lookupAndCache(String location)
            throws Exception
    {
        _lookups++;
        final JSONObject json = lookup(location);

        final double lat;
        final double lon;

        if (json == null)
            lat = lon = 0;
        else
        {
            final JSONObject mark = json.getJSONArray("Placemark").getJSONObject(0);
            final JSONArray coords = mark.getJSONObject("Point").getJSONArray("coordinates");

            lat = coords.getDouble(1);
            lon = coords.getDouble(0);
        }

        addGeoCode(location, lat, lon);

        return new Pair<Double, Double>(lat, lon);
    }

    public static JSONObject lookup(String location)
            throws Exception
    {
        location = URLEncoder.encode(location, StringUtilsExt.kCHARSET_UTF8);
        String url = gMapsRootUrl + "?q=" + location + "&output=json&sensor=false&key=" + kGoogleAPIKey;
        final JSONObject json = new JSONObject(URLUtils.getURL(url));

        final int status = json.getJSONObject("Status").getInt("code");
        if (status != 200) return null;
        return json;
    }

    private static GeoCodeManager readInstance()
    {
        try
        {
            final GeoCodeManager gcm = new GeoCodeManager();
            if (!kGeoCodeCache.exists())
                return gcm;
            final String jsonCache = FileUtilsExt.readFileAsString(kGeoCodeCache);
            JSONArray jarr = new JSONArray(jsonCache);
            for (int i = 0; i < jarr.length(); i++)
            {
                final JSONObject jobj = jarr.getJSONObject(i);
                String loc = jobj.getString("loc");
                double lat = jobj.getDouble("lat");
                double lon = jobj.getDouble("long");
                gcm.addGeoCode(loc, lat, lon);
            }
            return gcm;
        }
        catch (Exception e)
        {
            throw new BaseRuntimeException(e);
        }
    }

    public static double toRadians(double value)
    {
        return value * Math.PI / 180.0;
    }

    public static double toMiles(double km)
    {
        return km / 1.609344;
    }

    public static double distanceBetween(double lat1, double long1, double lat2, double long2)
    {
        // Haversine formula
        lat1 = toRadians(lat1);
        lat2 = toRadians(lat2);
        long1 = toRadians(long1);
        long2 = toRadians(long2);

        double deltaLat = lat2 - lat1;
        double deltaLong = long2 - long1;

        double a =
                Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLong/2) * Math.sin(deltaLong/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        final double radiusOfEarth = 6371; // Mean radius in km
        return radiusOfEarth * c; // in km
    }

    public static void dumpCache(boolean verbose)
    {
        GeoCodeManager instance = getInstance();
        System.out.println("Google maps geocode cache contains " + instance._map.size() + " entries");
        System.out.println("Required " + instance._lookups + " lookups for cache misses");

        if (verbose)
        {
            for (String loc : instance._map.keySet())
            {
                final Pair<Double, Double> latLong = instance._map.get(loc);
                System.out.println(loc + "\t" + latLong.getHead() + "\t" + latLong.getTail());
            }
        }
    }

    public static void saveCache()
            throws Exception
    {
        GeoCodeManager instance = getInstance();
        JSONArray jarr = new JSONArray();
        for (String loc: instance._map.keySet())
        {
            JSONObject jobj = new JSONObject();
            jobj.put("loc", loc);
            final Pair<Double, Double> latLong = instance._map.get(loc);
            jobj.put("lat", latLong.getHead());
            jobj.put("long", latLong.getTail());
            jarr.put(jobj);
        }
        JSONUtils.FormattedPrinter printer = new JSONUtils.FormattedPrinter(jarr);
        FileUtilsExt.writeFile(kGeoCodeCache, printer.formatPrint());
    }

    public static void main(String[] args)
            throws Exception
    {
        dumpCache(false);
    }

    /*
    private static void test()
            throws Exception
    {
        String[] places = new String[] {
                "New York, NY",
                "San Francisco, CA",
                "Stamford, CT",
                "Yonkers, NY",
                "Jersey City, NJ",
                "Bronx, NY",
                "Brooklyn, NY",
                "Staten Island, NY",
                "Hoboken, NJ",
                "White Plains, NY",
                "Queens, NY"
        };

        double[][] latAndLong = new double[places.length][];
        for (int i = 0; i < places.length; i++)
        {
            final Pair<Double, Double> latLong = GeoCodeManager.getGeoCode(places[i]);
            latAndLong[i] = new double[] {latLong.getHead(), latLong.getTail()};
        }

        for (int i = 0; i < places.length; i++)
            System.out.println(places[i] + ": " + latAndLong[i][0] + ", " + latAndLong[i][1]);

        for (int i = 0; i < places.length; i++)
            for (int j = 0; j < places.length; j++)
            {
                final double km = distanceBetween(latAndLong[i][0], latAndLong[i][1], latAndLong[j][0], latAndLong[j][1]);
                System.out.println("Distance from " + places[i] + " to " + places[j] + " = " +
                toMiles(km) + "(" + km + " km)");
            }

        saveCache();
    }
    */
}
