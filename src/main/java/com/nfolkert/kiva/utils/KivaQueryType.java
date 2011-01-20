package com.nfolkert.kiva.utils;

/**
 */
public enum KivaQueryType
{
    NewestLenders,
    Lenders,
    Loans,
    RecentLendingActions,
    TeamLenders;

    public static KivaQueryType typeForZipEntry(String zipName)
    {
        final String[] path = zipName.split("/");
        final String root = path[0];
        if (root.equals("lenders")) return Lenders;
        if (root.equals("loan")) return Loans;

        for (KivaQueryType type: KivaQueryType.values())
            if (type.name().equalsIgnoreCase(path[0]))
                return type;
        return null;
    }
}
