package com.nfolkert.kiva.utils;

import com.nfolkert.json.JSONObject;

/**
 */
public abstract class KivaFetchHandler extends KivaQueryHandler
{
    public abstract boolean continueQuery(JSONObject jobj);

    public abstract static class Complete extends KivaFetchHandler
    {
        final public boolean continueQuery(JSONObject jobj)
        {
            return true;
        }
    }
}
