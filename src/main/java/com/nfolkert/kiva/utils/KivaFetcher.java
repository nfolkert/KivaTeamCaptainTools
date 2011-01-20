package com.nfolkert.kiva.utils;

import com.nfolkert.json.JSONObject;

import org.apache.commons.lang.StringUtils;

/**
 */
public class KivaFetcher
{
    private static final String gLendersRoot = "http://api.kivaws.org/v1/lenders/search.json";
    private static final String gNewestLendersRoot = "http://api.kivaws.org/v1/lenders/newest.json";
    private static final String gLenderByIDRoot = "http://api.kivaws.org/v1/lenders/";

    private static final String gTeamLendersRoot = "http://api.kivaws.org/v1/teams/$/lenders.json";
    private static final String gRecentLendingActionsRoot = "http://api.kivaws.org/v1/lending_actions/recent.json";

    private static final String api_key = "app_id=com.kivanewyork.query";

    public void fetchLenders(KivaFetchHandler handler, int startAtPage)
            throws Exception
    {
        for (int i = startAtPage ;; i++)
        {
            final String url = gLendersRoot + "?country_code=us&sort_by=oldest&page=" + i + "&" + api_key;
            JSONObject file = new JSONObject(KivaResultManager.getResultsAsString(KivaQueryType.Lenders, url));
            boolean more = handler.handleFile(KivaQueryType.Lenders, file);
            if (!more || !handler.continueQuery(file))
                break;
        }
    }

    public void fetchNewestLenders(KivaFetchHandler handler, int startAtPage)
            throws Exception
    {
        for (int i = startAtPage ;; i++)
        {
            final String url = gNewestLendersRoot + "?page=" + i + "&" + api_key;
            JSONObject file = new JSONObject(KivaResultManager.getResultsAsString(KivaQueryType.NewestLenders, url));
            boolean more = handler.handleFile(KivaQueryType.NewestLenders, file);
            if (!more || !handler.continueQuery(file))
                break;
        }
    }

    public JSONObject getLenderById(String id)
            throws Exception
    {
        final JSONObject[] ret = new JSONObject[1];
        fetchLendersByIds(new KivaFetchHandler.Complete()
        {
            @Override
            public void handle(JSONObject object)
                    throws Exception
            {
                ret[0] = object;
            }
        }, id);
        return ret[0];
    }

    public void fetchLenderById(KivaFetchHandler handler, String id)
            throws Exception
    {
        fetchLendersByIds(handler, id);
    }

    public void fetchLendersByIds(KivaFetchHandler handler, String... ids)
            throws Exception
    {
        for (int i = 0; i < ids.length; i += 50)
        {
            int numThisBatch = Math.min(50, ids.length-i);
            String[] theseIds = new String[numThisBatch];
            System.arraycopy(ids, i, theseIds, 0, theseIds.length);
            String query = StringUtils.join(theseIds, ",") + ".json";
            final String url = gLenderByIDRoot + query + "?" + api_key;
            JSONObject file = new JSONObject(KivaResultManager.getResultsAsString(KivaQueryType.Lenders, url));
            boolean more = handler.handleFile(KivaQueryType.Lenders, file);
            if (!more || !handler.continueQuery(file))
                break;
        }
    }

    public void fetchLatestLendingActions(KivaFetchHandler handler)
            throws Exception
    {
        final String url = gRecentLendingActionsRoot + "?" + api_key;
        JSONObject file = new JSONObject(KivaResultManager.getResultsAsString(KivaQueryType.RecentLendingActions, url));
        handler.handleFile(KivaQueryType.RecentLendingActions, file);
    }

    public void fetchTeamLenders(KivaFetchHandler handler, int teamId)
            throws Exception
    {
        for (int i = 1 ;; i++)
        {
            final String teamUrl = gTeamLendersRoot.replace("$", String.valueOf(teamId));
            final String url = teamUrl + "?sort_by=oldest&page=" + i + "&" + api_key;
            JSONObject file = new JSONObject(KivaResultManager.getResultsAsString(KivaQueryType.TeamLenders, url));
            boolean more = handler.handleFile(KivaQueryType.TeamLenders, file);
            if (!more || !handler.continueQuery(file))
                break;
        }
    }
}

