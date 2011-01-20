package com.nfolkert.kiva.utils;

import com.nfolkert.collections.ListOrderedMap;
import com.nfolkert.kiva.properties.KivaProperties;
import com.nfolkert.utils.FileUtilsExt;
import com.nfolkert.utils.URLUtils;
import com.nfolkert.utils.JSONUtils;
import com.nfolkert.json.JSONArray;
import com.nfolkert.json.JSONObject;
import com.nfolkert.exception.BaseRuntimeException;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 */
public class KivaResultManager
{
    private static final File kKivaQueryCacheList = new File(KivaProperties._kivaProps.getProperty("queryCache"));
    private static final File kKivaQueryCache = new File(KivaProperties._kivaProps.getProperty("queryCacheDir"));

    Map<KivaQueryType, Map<String, File>> _map = new ListOrderedMap<KivaQueryType, Map<String, File>>();
    int _queries;

    private static KivaResultManager _instance;

    public static KivaResultManager getInstance()
    {
        if (_instance == null)
            _instance = readInstance();
        return _instance;
    }

    private void addQueryFile(KivaQueryType type, String queryURL, File file)
    {
        Map<String, File> subMap = _map.get(type);
        if (subMap == null)
        {
            subMap = new ListOrderedMap<String, File>();
            _map.put(type, subMap);
        }
        subMap.put(queryURL, file);
    }

    public static String getResultsAsString(KivaQueryType type, String queryURL)
            throws Exception
    {
        return FileUtilsExt.readFileAsString(getResultFile(type, queryURL));
    }

    public static File getResultFile(KivaQueryType queryType, String queryURL)
            throws Exception
    {
        final KivaResultManager krm = getInstance();
        final Map<String, File> subMap = krm._map.get(queryType);
        File res = subMap == null ? null : subMap.get(queryURL);
        if (res != null && res.exists()) 
            return res;

        res = krm.queryAndCache(queryType, queryURL);

        return res;
    }

    public File queryAndCache(KivaQueryType type, String queryURL)
            throws Exception
    {
        _queries++;

        final String result = URLUtils.getURL(queryURL);
        File dir = new File(kKivaQueryCache, type.name());
        dir.mkdirs();
        final Map<String, File> typeMap = _map.get(type);
        String fName = "queryCache_" + (typeMap == null ? 1 : typeMap.size()+1) + ".json";
        final File file = new File(dir, fName);
        JSONUtils.FormattedPrinter printer = new JSONUtils.FormattedPrinter(JSONObject.parseJSON(result));
        FileUtilsExt.writeFile(file,  printer.formatPrint());
        addQueryFile(type, queryURL, file);
        return file;
    }

    private static KivaResultManager readInstance()
    {
        try
        {
            final KivaResultManager krm = new KivaResultManager();
            if (!kKivaQueryCacheList.exists())
                return krm;
            final String jsonCache = FileUtilsExt.readFileAsString(kKivaQueryCacheList);
            JSONArray jarr = new JSONArray(jsonCache);
            for (int i = 0; i < jarr.length(); i++)
            {
                final JSONObject jobj = jarr.getJSONObject(i);
                String queryURL = jobj.getString("query");
                String type = jobj.getString("type");
                String path = jobj.getString("file");
                krm.addQueryFile(KivaQueryType.valueOf(type), queryURL, new File(path));
            }
            return krm;
        }
        catch (Exception e)
        {
            throw new BaseRuntimeException(e);
        }
    }

    public static void dumpCache(boolean verbose)
    {
        KivaResultManager instance = getInstance();
        System.out.println("Kiva query cache contains " + instance._map.size() + " entries:");
        for (KivaQueryType type: instance._map.keySet())
            System.out.println("\t" + type.name() + ": " + instance._map.get(type).size());
        System.out.println("Required " + instance._queries + " queries for cache misses");

        if (verbose)
        {
            for (KivaQueryType type : instance._map.keySet())
            {
                for (String query : instance._map.get(type).keySet())
                {
                    final File file = instance._map.get(type).get(query);
                    System.out.println(query + "\t" + file.getAbsolutePath());
                }
            }
        }
    }

    public static void saveCache()
            throws Exception
    {
        KivaResultManager instance = getInstance();
        JSONArray jarr = new JSONArray();
        for (KivaQueryType type: instance._map.keySet())
        {
            for (String query: instance._map.get(type).keySet())
            {
                JSONObject jobj = new JSONObject();
                jobj.put("type", type.name());
                jobj.put("query", query);
                final File file = instance._map.get(type).get(query);
                jobj.put("file", file.getAbsolutePath());
                jarr.put(jobj);
            }

        }
        JSONUtils.FormattedPrinter printer = new JSONUtils.FormattedPrinter(jarr);
        FileUtilsExt.writeFile(kKivaQueryCacheList, printer.formatPrint());
    }

    public static void clearCache(KivaQueryType... typesToClear)
            throws Exception
    {
        KivaResultManager instance = getInstance();

        Set<KivaQueryType> toClear =
                new HashSet<KivaQueryType>(Arrays.asList(typesToClear == null ? KivaQueryType.values() : typesToClear));
        for (KivaQueryType type: toClear)
        {
            final Map<String, File> subMap = instance._map.get(type);
            if (subMap != null)
                for (File f: subMap.values())
                    f.delete();
            instance._map.remove(type);
        }
        saveCache();
    }
}
