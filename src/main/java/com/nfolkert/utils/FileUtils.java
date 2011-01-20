package com.nfolkert.utils;

import java.io.File;
import java.util.regex.Pattern;

/**
 */
public class FileUtils
{
    public static File getLastModifiedFile(File inDirectory, Pattern optionalFileNamePattern)
    {
        if (!inDirectory.isDirectory()) throw new IllegalArgumentException();
        final File[] allFiles = inDirectory.listFiles();

        long latestLastModified = 0;
        File latestFile = null;
        for (File f: allFiles)
        {
            final String fileName = f.getName();
            if (optionalFileNamePattern != null &&
                !optionalFileNamePattern.matcher(fileName).matches()) continue;
            if (f.lastModified() > latestLastModified)
            {
                latestLastModified = f.lastModified();
                latestFile = f;
            }
        }
        return latestFile;
    }
}
