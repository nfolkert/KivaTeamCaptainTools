package com.nfolkert.kiva.utils;

import com.nfolkert.json.JSONObject;
import com.nfolkert.json.JSONArray;

/**
 */
public abstract class KivaQueryHandler
{
    public abstract void handle(JSONObject object)
            throws Exception;

    public final boolean handleFile(KivaQueryType type, JSONObject file)
            throws Exception
    {
        if (type == KivaQueryType.Lenders ||
            type == KivaQueryType.NewestLenders ||
            type == KivaQueryType.TeamLenders)
        {
            final JSONArray lenders = file.getJSONArray("lenders");
            for (int i = 0; i < lenders.length(); i++)
                handle(lenders.getJSONObject(i));
            return lenders.length() > 0;
        }
        else if (type == KivaQueryType.Loans)
        {
            final JSONArray loans = file.getJSONArray("loans");
            for (int i = 0; i < loans.length(); i++)
                handle(loans.getJSONObject(i));
            return loans.length() > 0;
        }
        else if (type == KivaQueryType.RecentLendingActions)
        {
            final JSONArray actions = file.getJSONArray("lending_actions");
            for (int i = 0; i < actions.length(); i++)
                handle(actions.getJSONObject(i));
            return actions.length() > 0;
        }
        else
            throw new UnsupportedOperationException();
    }
}
