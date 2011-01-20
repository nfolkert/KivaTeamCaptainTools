package com.nfolkert.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.nfolkert.utils.Pair;
import com.nfolkert.exception.BaseException;

/**
 */
public class ZipUtils
{
    private static void getAllFilesRelativeAndAbsolute_rec(File absoluteFile, File parentFileRelative, List<Pair<File, File>> filesInOut)
    {
        File relativeFile = parentFileRelative == null ? new File(absoluteFile.getName()) : new File(parentFileRelative, absoluteFile.getName());
        if (absoluteFile.isDirectory())
        {
            File[] childFiles = absoluteFile.listFiles();
            for (File child: childFiles)
                getAllFilesRelativeAndAbsolute_rec(child, relativeFile, filesInOut);
        }
        else
            filesInOut.add(new Pair<File, File>(relativeFile, absoluteFile));
    }

    private static List<Pair<File, File>> getAllFilesRelativeAndAbsolute(File fileOrDirectory)
            throws BaseException
    {
        List<Pair<File, File>> ret = new ArrayList<Pair<File, File>>();
        Set<String> namesSeen = new HashSet<String>();
        if (fileOrDirectory.isDirectory())
        {
            for (File f : fileOrDirectory.listFiles())
            {
                if (namesSeen.contains(f.getName()))
                    throw new BaseException("Root level files to zip must be uniquely named");
                namesSeen.add(f.getName());
                getAllFilesRelativeAndAbsolute_rec(f, null, ret);
            }
        }
        else
            ret.add(new Pair<File, File>(new File(fileOrDirectory.getName()), fileOrDirectory));
        return ret;
    }

    /**
     * Files should have a common root, alternatively, root directories should be uniquely named
     * @param fileOrDirectory
     * @throws BaseException
     */
    public static void zipFileOrDirectoryOverwritingExistingZip(final File zipTarget,
                                                                final boolean deleteFilesAfterZipping,
                                                                final File fileOrDirectory)
            throws BaseException
    {
        zipFileOrDirectory(zipTarget, false, deleteFilesAfterZipping, fileOrDirectory);
    }

    private static void zipFileOrDirectory(final File zipTarget,
                                 final boolean mergeIntoExistingZip,
                                 final boolean deleteFilesAfterZipping,
                                 final File fileOrDirectory)
            throws BaseException
    {
        List<Pair<File, File>> leafFiles = getAllFilesRelativeAndAbsolute(fileOrDirectory);
        ZipOutputStream zout = null;
        OutputStreamWriter zwriter = null;
        ZipInputStream zin = null;

        File unzipDir = null;
        try
        {
            if (zipTarget.exists() && mergeIntoExistingZip)
            {
                unzipDir = FileUtilsExt.createTempDir("unzipForMerge");
                unzipFiles(zipTarget, false, unzipDir);
                List<Pair<File, File>> unzippedFilesRelAndAbs = getAllFilesRelativeAndAbsolute(unzipDir);

                Map<File, File> relativeFiles = new HashMap<File, File>(leafFiles.size());
                for (Pair<File, File> relAndAbsFile: leafFiles)
                    relativeFiles.put(relAndAbsFile.getHead(), relAndAbsFile.getTail());

                for (Pair<File, File> unzipPair: unzippedFilesRelAndAbs)
                {
                    File mergeFile = relativeFiles.get(unzipPair.getHead());
                    if (mergeFile == null)
                        leafFiles.add(unzipPair);
                }
            }
            else if (zipTarget.exists())
                zipTarget.delete();

            zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipTarget)));
            zwriter = new OutputStreamWriter(zout, StringUtilsExt.kCHARSET_UTF8);

            for (Pair<File, File> relAndAbsFile: leafFiles)
            {
                File inFile = relAndAbsFile.getTail();
                File writePath = relAndAbsFile.getHead();

                ZipEntry zentry = new ZipEntry(writePath.getPath());
                zentry.setTime(0);
                zout.putNextEntry(zentry);
                String sessionXML = FileUtils.readFileToString(inFile, StringUtilsExt.kCHARSET_UTF8);
                zwriter.write(sessionXML);
                zwriter.flush();
            }
            zout.close();

            if (deleteFilesAfterZipping)
                for (Pair<File, File> relAndAbsFile: leafFiles)
                    relAndAbsFile.getTail().delete();
            if (unzipDir != null)
                FileUtils.deleteDirectory(unzipDir);
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
        finally
        {
            IOUtils.closeQuietly(zout);
            IOUtils.closeQuietly(zin);
            IOUtils.closeQuietly(zwriter);
        }
    }

    public static void unzipFiles(final File zipFile,
                                  final boolean deleteZipFile,
                                  final File dirWhereToUnzip)
            throws BaseException
    {
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), 8092));
            ZipEntry zipEntry;
            while (null != (zipEntry = zin.getNextEntry()))
            {
                String name = zipEntry.getName();
                File toRead = new File(dirWhereToUnzip, name);
                toRead.getParentFile().mkdirs();

                if (toRead.exists())
                    FileUtilsExt.delete(toRead);

                FileOutputStream out = null;
                try
                {
                    out = new FileOutputStream(toRead);
                    FileUtilsExt.transferStream(zin, out);
                }
                finally
                {
                    IOUtils.closeQuietly(out);
                }
            }
            zin.close();
            if (deleteZipFile)
                zipFile.delete();
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
        finally
        {
            IOUtils.closeQuietly(zin);
        }
    }

    public static abstract class ProcessZipFile
    {
        public abstract void processFile(String zipName, BufferedInputStream zippedFileStream)
                throws Exception;
    }

    public static void readZippedDirectory(final File zipFile,
                                           final ProcessZipFile callback)
            throws BaseException
    {
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), 8092));
            ZipEntry zipEntry;
            while (null != (zipEntry = zin.getNextEntry()))
            {
                String name = zipEntry.getName();
                callback.processFile(name, new BufferedInputStream(zin));
            }
            zin.close();
        }
        catch (Exception ioex)
        {
            throw new BaseException(ioex);
        }
        finally
        {
            IOUtils.closeQuietly(zin);
        }
    }

    public static void writeFileOrDirectoryMergingWithExistingZip(final File zipFile,
                                                                  final boolean deleteFilesAfterZipping,
                                                                  final File fileOrDirectory)
            throws BaseException
    {
        zipFileOrDirectory(zipFile, true, deleteFilesAfterZipping, fileOrDirectory);
    }
}
