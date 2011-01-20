package com.nfolkert.kiva.utils;

import com.nfolkert.kiva.properties.KivaProperties;
import com.nfolkert.utils.FileUtils;
import com.nfolkert.utils.ZipUtils;
import com.nfolkert.exception.BaseException;
import com.nfolkert.json.JSONObject;

import java.io.File;
import java.io.BufferedInputStream;

import org.apache.commons.io.IOUtils;

/**
 */
public class KivaDump
{
    public static File kDefaultKivaDirectory = new File(KivaProperties._kivaProps.getProperty("kivaDumpDir"));
    private File _zipFile;

    public KivaDump()
            throws Exception
    {
        _zipFile = FileUtils.getLastModifiedFile(kDefaultKivaDirectory, null);
    }

    public void runQuery(final KivaQueryType queryType, final KivaQueryHandler searcher)
            throws BaseException
    {
        ZipUtils.readZippedDirectory(_zipFile, new ZipUtils.ProcessZipFile() {

            public void processFile(String zipName, BufferedInputStream zippedFileStream)
                    throws Exception
            {
                final KivaQueryType type = KivaQueryType.typeForZipEntry(zipName);
                if (type == queryType)
                {
                    JSONObject fileObject = new JSONObject(IOUtils.toString(zippedFileStream));
                    searcher.handleFile(queryType, fileObject);
                }
            }
        });
    }
}
