package com.nfolkert.kiva.utils;

import com.nfolkert.json.JSONObject;

/**
 */
public class PrintNameHandler extends KivaQueryHandler
{
    public void handle(JSONObject lenderOrLoan)
            throws Exception
    {
        System.out.println(lenderOrLoan.get("name"));
    }

    public static void main(String[] args)
            throws Exception
    {
        new KivaDump().runQuery(KivaQueryType.Lenders, new PrintNameHandler());
        new KivaDump().runQuery(KivaQueryType.Loans, new PrintNameHandler());
    }
}
