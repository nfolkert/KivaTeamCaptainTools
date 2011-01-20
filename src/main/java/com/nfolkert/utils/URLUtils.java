package com.nfolkert.utils;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 */
public class URLUtils
{
    public static String getURL(String urlStr)
            throws IOException
    {
        String result;
        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null)
        {
            sb.append(line);
        }
        rd.close();
        result = sb.toString();
        return result;
    }
}
