package com.nfolkert.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.nfolkert.exception.BaseException;
import com.nfolkert.exception.BaseRuntimeException;
import com.nfolkert.exception.AssertExt;

public class FileUtilsExt
{
    private FileUtilsExt()
    { // static only
    }

    public static List<String> loadFileContentsIntoArrayList(final File f, final boolean includeBlankLines, final boolean trimLines)
            throws IOException
    {
        final LineAccumulator lp = new LineAccumulator(trimLines, includeBlankLines);
        processFileByLine(f, lp);
        return lp.res;
    }

    public static List<String> getLines(final Reader r, final boolean includeBlankLines, final boolean trimLines)
            throws IOException
    {
        final LineAccumulator lp = new LineAccumulator(trimLines, includeBlankLines);
        processReaderByLine(r,lp);
        return lp.res;
    }

    public static String loadFileContents(final File f)
            throws IOException
    {
        return loadFileContents(f, 0);
    }

    public static String loadEndOfFileContents(final File f, long maxToLoad)
            throws IOException
    {
        long skip = f.length() - maxToLoad;
        if (skip < 0) skip = 0;
        return loadFileContents(f, skip);
    }

    public static String loadFileContents(final File f, long skip)
            throws IOException
    {
        final StringBuffer res = new StringBuffer((int) f.length());
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(f);
            in.skip(skip);

            final byte[] buf = new byte[5000];
            for (; ;)
            {
                final int num = in.read(buf);
                if (num < 0)
                {
                    break;
                }
                res.append(new String(buf, 0, num));
            }
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
        return res.toString();
    }

    public static void processFileByLine(final File f, final LineProcessor proc)
            throws IOException
    {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(f);
            processStreamByLine(in, proc);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Processes the stream _and_ closes it.
     */
    public static void processStreamByLine(final InputStream in, final LineProcessor proc)
            throws IOException
    {
        Reader reader = null;
        try
        {
            reader = new InputStreamReader(in);
            processReaderByLine(reader, proc);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(in);
        }
    }

    public static void processReaderByLine(Reader reader, LineProcessor proc)
            throws IOException
    {
        BufferedReader breader = null;
        try
        {
            breader = new BufferedReader(reader);
            for (; ;)
            {
                final String line = breader.readLine();
                if (line == null)
                {
                    break;
                }
                proc.processLine(line);
            }
        }
        finally
        {
            IOUtils.closeQuietly(breader);
        }
    }

    public static boolean filesEqual(final File one, final File two)
            throws IOException
    {
        if (one.length() != two.length())
        {
            return false;
        }

        FileInputStream oneIn = null;
        FileInputStream twoIn = null;

        try
        {
            oneIn = new FileInputStream(one);
            twoIn = new FileInputStream(two);

            final byte[] arrOne = new byte[1000];
            final byte[] arrTwo = new byte[1000];
            for (; ;)
            {
                final int o = oneIn.read(arrOne);
                final int t = twoIn.read(arrTwo);
                if (o != t) // not quite right (we could have had a slow read on one of them) but this will work
                {
                    return false;
                }
                if (o == -1)
                {
                    return true;
                }

                for (int i = 0; i < o; i++)
                {
                    if (arrOne[i] != arrTwo[i])
                    {
                        return false;
                    }
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(oneIn);
            IOUtils.closeQuietly(twoIn);
        }
    }

    public static void copyFile(final File src, final File dest)
            throws IOException
    {
        FileUtilsExt.delete(dest);

        FileOutputStream out = null;
        FileInputStream in = null;

        try
        {
            out = new FileOutputStream(dest);
            in = new FileInputStream(src);

            transferStream(in, out);
        }
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    public static void writeFileIntoZip(final File src, final ZipOutputStream out)
            throws IOException
    {
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(src);
            transferStream(in, out);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Reads all data from src and appends to dest.  Closes src as well.
     *
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copy(final InputStream src, final File dest)
            throws IOException
    {
        FileUtilsExt.delete(dest);

        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(dest);

            transferStream(src, out);
        }
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(src);
        }
    }

    /**
     * Same as transferring using three-arg version, with a chunkSize of 5000
     * @param in
     * @param out
     * @throws IOException
     */
    public static void transferStream(InputStream in, OutputStream out)
            throws IOException
    {
        transferStream(in,out,5000);
    }

    public static void transferStream(InputStream in, OutputStream out, final int chunkSize)
            throws IOException
    {
        final byte[] buf = new byte[chunkSize];
        for (; ;)
        {
            final int num = in.read(buf);
            if (num < 0)
            {
                break;
            }
            else if (num == 0)
            {
                continue;
            }
            out.write(buf, 0, num);
        }
        out.flush();
    }

    public static File writeStringToTempFile(final String toWrite, final String suffix) throws BaseException
    {
        FileOutputStream os = null;
        final File file;
        try
        {
            file = File.createTempFile("mftemp_" + System.currentTimeMillis(), suffix);
            FileUtilsExt.writeFile(file, toWrite);
        }
        catch (Exception e)
        {
            throw new BaseException(e);
        }
        finally
        {
            IOUtils.closeQuietly(os);
        }

        return file;
    }

    public static File writeStreamToTempFile(final InputStream inStream,
                                             final String fileExtension,
                                             final File parentDir)
            throws IOException
    {
        FileOutputStream os = null;
        final File file;
        try
        {
            file = File.createTempFile("mftemp_" + System.currentTimeMillis(), fileExtension, parentDir);
            os = new FileOutputStream(file);
            FileUtilsExt.transferStream(inStream, os);
        }
        finally
        {
            IOUtils.closeQuietly(os);
        }

        return file;
    }

    public static void deleteQuietly(final File file)
    {
        if (file != null)
        {
            if (!FileUtilsExt.delete(file))
            {
                file.deleteOnExit();
            }
        }
    }

    public static void moveFile(final File src, final File dest)
            throws IOException
    {
        FileUtilsExt.delete(dest);
        if (src.renameTo(dest))
        {
            return;
        }

        FileOutputStream out = null;
        FileInputStream in = null;
        final byte[] buf = new byte[5000];

        try
        {
            out = new FileOutputStream(dest);
            in = new FileInputStream(src);

            for (; ;)
            {
                final int num = in.read(buf);
                if (num < 0)
                {
                    break;
                }
                else if (num == 0)
                {
                    continue;
                }
                out.write(buf, 0, num);
            }
            out.flush();
        }
        finally
        {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    public static String removeExtension(final String name)
    {
        final int index = name.lastIndexOf(".");
        return index == -1 ? name : name.substring(0, index);
    }

    /**
     * @param name
     * @return dot + the extension, or "" if there is no extension
     */
    public static String getExtension(final String name)
    {
        final int index = name.lastIndexOf(".");
        return index == -1 ? "" : name.substring(index);
    }

    public static byte[] readFileAsBytes(final File file)
            throws IOException
    {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream((int) file.length() + 1);
        FileInputStream in = null;

        try
        {
            in = new FileInputStream(file);
            final byte[] arr = new byte[2000];
            for (; ;)
            {
                final int numRead = in.read(arr);
                if (numRead == -1)
                {
                    break;
                }
                bout.write(arr, 0, numRead);
            }
            return bout.toByteArray();
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * @param file
     * @return
     * @throws java.io.IOException thrown if a true IOException occurs when scanning the parent directory OR if more than 1 file
     *                             is found matching the given file.  If no files match, null is returned
     */
    public static File findMatchingFileIgnoreCase(final File file)
            throws IOException
    {
        final File[] files = file.getParentFile().listFiles(new FilenameFilter()
        {
            public boolean accept(final File dir, final String name)
            {
                return name.equalsIgnoreCase(file.getName());
            }
        });
        if (files == null || files.length == 0)
        {
            return null;
        }
        else if (files.length > 1)
        {
            throw new IOException("Attempted to do case-insensitive match but found multiple for : \"" + file + "\"");
        }
        else
        {
            return files[0];
        }
    }

    public static String removeIllegalFileChars(String filename)
    {
        if (filename == null)
        {
            return null;
        }

        final String[] illegalChars = new String[]{"\\", "/", ":", "?", "*", "\"", ">", "<", "|"};
        for (String illegalChar : illegalChars)
        {
            filename = StringUtils.replace(filename, illegalChar, "");
        }

        return filename;
    }

    public static void unzipFile(File zipFile)
            throws BaseException
    {
        if ( !zipFile.getName().toLowerCase().endsWith(".zip"))
            return;
        boolean successful =false;
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            while ( (entry = zin.getNextEntry()) !=null )
            {
                File newFile = new File(zipFile.getParentFile().getAbsolutePath(), entry.getName());
                if (newFile.exists())
                    FileUtilsExt.delete(newFile);

                FileOutputStream out = null;
                try
                {
                    newFile.getParentFile().mkdirs();
                    out = new FileOutputStream(newFile);
                    transferStream(zin, out);
                }
                finally
                {
                    IOUtils.closeQuietly(out);
                }
            }
            successful = true;
        }
        catch (IOException e)
        {
            throw new BaseException(e);
        }
        finally
        {
            IOUtils.closeQuietly(zin);
            if (successful)
                zipFile.delete();
        }
    }

    public interface LineProcessor
    {
        public void processLine(String line);
    }

    public static Map<String, String> loadMyPropertyFile(final File f)
            throws IOException
    {
        final PropertyFileProcessor prop = new PropertyFileProcessor();
        processFileByLine(f, prop);
        prop.finishProcessing();

        return prop._res;
    }

    private static class PropertyFileProcessor implements LineProcessor
    {
        private Map<String,String> _res = new Hashtable<String, String>();
        private StringBuffer _curLine = new StringBuffer(1000);
        private String _curSection;

        public void processLine(final String line)
        {
            if (line.startsWith("#"))
            {
                finishCurSection();
                _curSection = line.substring(1).trim();
            }
            else
            {
                _curLine.append(line);
            }
        }

        public void finishProcessing()
        {
            finishCurSection();
        }

        private void finishCurSection()
        {
            if (_curSection != null)
            {
                _res.put(_curSection, _curLine.toString());
                _curLine.setLength(0);
                _curSection = null;
            }
        }
    }

    public static String[] getAllFilesAndSubFiles(final String dir)
    {
        final List< String > res = new ArrayList< String >(400);
        getAllFilesAndSubFiles(new File(dir), "", res);

        return res.toArray(new String[res.size()]);
    }

    private static void getAllFilesAndSubFiles(final File dir, final String curPathWithTrailer, final List<String>res)
    {
        final String[] names = dir.list();
        for (String name : names)
        {
            final File inner = new File(dir, name);
            final String nextPath = curPathWithTrailer + name;
            if (inner.isDirectory())
            {
                getAllFilesAndSubFiles(inner, nextPath + File.separator, res);
            }
            else
            {
                res.add(nextPath);
            }
        }
    }

    /**
     * @param out
     * @param contents
     * @throws IOException
     */
    public static void writeFile(final File out, final String contents)
            throws IOException
    {
        final File tmpFile = File.createTempFile(out.getName(), null, out.getParentFile());

        final FileOutputStream fout = new FileOutputStream(tmpFile);
        try
        {
            final PrintWriter pw = new PrintWriter(fout);
            pw.write(contents);

            pw.flush();
            fout.flush();
        }
        finally
        {
            IOUtils.closeQuietly(fout);
        }

        renameTmpFile(tmpFile, out);
    }

    private static void renameTmpFile(final File tmpFile, final File out)
            throws IOException
    {
        if (out.exists())
        {
            FileUtilsExt.delete(out);
        }
        if (!tmpFile.renameTo(out))
        {
            throw new IOException("Failed to rename \"" + tmpFile + "\" to \"" + out + "\"");
        }
    }

    public static void writeFile(final File out, final List<String> stringContentsByLine)
    {
        writeFile(out, stringContentsByLine, false);
    }

    public static void writeFile(final File out, final List<String> stringContentsByLine, final boolean appendToExisting)
    {
        try
        {
            final File toWrite = appendToExisting ? out : File.createTempFile(out.getName(), null, out.getParentFile());

            boolean isSuccessful = false;
            FileOutputStream fout = null;
            BufferedOutputStream bout = null;
            PrintWriter pw = null;
            try
            {
                fout = new FileOutputStream(toWrite.getAbsolutePath(), appendToExisting);
                bout = new BufferedOutputStream(fout);
                pw = new PrintWriter(bout);
                final int size = stringContentsByLine.size();
                for (int i = 0; i < size; i++)
                {
                    final String s = stringContentsByLine.get(i);
                    pw.println(s);
                }
                isSuccessful = true;
            }
            finally
            {
                IOException toThrow = null;
                if (pw != null)
                {
                    pw.close();
                }
                try
                {
                    if (bout != null)
                    {
                        bout.close();
                    }
                }
                catch (IOException ex)
                {
                    if (isSuccessful)
                    {
                        toThrow = ex;
                        isSuccessful = false;
                    }
                }
                try
                {
                    if (fout != null)
                    {
                        fout.close();
                    }
                }
                catch (IOException ex)
                {
                    if (isSuccessful)
                    {
                        toThrow = ex;
                        isSuccessful = false;
                    }
                }

                if (toThrow != null)
                {
                    throw toThrow;
                }
            }

            if (toWrite != out)
            {
                if (out.exists())
                {
                    FileUtilsExt.delete(out);
                }
                toWrite.renameTo(out);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * only accepts files or directories with the given extensions.
     */
    public static class FilenameFilterByExtension implements FilenameFilter
    {
        private final String[] _extsByRef;

        /**
         * @param extsByReference do not put the dots on the extensions (so pass, for example, <code>new String[] { "txt", "dat" }</code>)
         */
        public FilenameFilterByExtension(final String[] extsByReference)
        {
            _extsByRef = extsByReference;
        }

        public boolean accept(final File f, final String name)
        {
            final int lastDot = name.lastIndexOf(".");
            if (lastDot == -1 || lastDot == name.length() - 1)
            {
                return false;
            }

            final String ext = name.substring(lastDot + 1);
            for (String possibleExt : _extsByRef)
            {
                if (ext.equalsIgnoreCase(possibleExt))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public static String makePathReadyToConcat(final String s)
    {
        final int backIndex = s.lastIndexOf('\\');
        final int forwardIndex = s.lastIndexOf('/');
        if (backIndex == s.length() - 1 || forwardIndex == s.length() - 1)
        {
            return s;
        }

        if (backIndex == -1 && forwardIndex == -1)
        {
            return s + File.separator;
        }
        else if (backIndex == -1)
        {
            return s + '/';
        }
        else if (forwardIndex == -1)
        {
            return s + '\\';
        }
        else
        {
            // we have both...don't know what to do here so just use file separator
            return s + File.separator;
        }
    }

    /**
     * Replace forward and back slashes with File.separatorChar
     *
     * @param path
     */
    public static String pathSeparatorReplace(String path)
    {
        path = path.replace('/', File.separatorChar);
        path = path.replace('\\', File.separatorChar);
        return path;
    }

    public static String forwardSlashReplace(String path)
    {
        path = path.replace('\\', '/');
        return path;
    }

    public static String readFirstLine(final File file)
            throws BaseException
    {
        try
        {
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            final String line = reader.readLine();
            reader.close();

            return line;
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
    }

    public static List<String> readLines(File file)
            throws BaseException
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            return readLines(reader);
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
    }

    public static List<String> readLines(Reader reader)
            throws BaseException
    {
        try
        {
            BufferedReader breader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
            List<String> ret = new ArrayList<String>(1000);
            for (;;)
            {
                String line = breader.readLine();
                if (line == null) return ret;
                ret.add(line);
            }
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
    }

    public static boolean deleteDirectory(File f)
    {
        boolean result;
        try
        {
            FileUtils.deleteDirectory(f);
            result = true;
        }
        catch (IOException ioex)
        {
            result = false;
        }
        AssertExt.assertCond(result || ! f.exists() , "Tried to delete " + f.getAbsolutePath() + " but could not." ); // delete will fail if it doesn't already exist.
        return result;
    }

    public static File createTempDir(String name)
            throws IOException
    {
        // todo: better way to do this?  temp on client?
        File tmpDir = File.createTempFile("dummy", ".txt").getParentFile();

        File file;
        for (int seq = 0; ; seq++)
        {
            file = new File(tmpDir, name + System.currentTimeMillis() + "_" + seq);
            if (!file.exists())
            {
                file.mkdir();
                break;
            }
        }
        return file;
    }

    public static boolean delete(File f)
    {
        boolean result = f.delete();
        AssertExt.assertCond( result || ! f.exists() , "Tried to delete " + f.getAbsolutePath() + " but could not." ); // delete will fail if it doesn't already exist.
        return result;
    }

    private static class LineAccumulator implements LineProcessor
    {
        final List<String> res;
        private final boolean _trimLines;
        private final boolean _includeBlankLines;

        public LineAccumulator(boolean trimLines, boolean includeBlankLines)
        {
            _trimLines = trimLines;
            _includeBlankLines = includeBlankLines;
            res = new ArrayList<String>(100);
        }

        public void processLine(String line)
        {
            if (_trimLines)
            {
                line = line.trim();
            }
            if (!_includeBlankLines && line.length() == 0)
            {
                return;
            }

            res.add(line);
        }
    }

    /**
     * Serialize to XML string. Warning, specifing a method name in both methodsToExclude and methodsToInclude sets could
     * cause unexpected behaviour.
     * @param bean the object to be serialized
     * @param methodsToExclude: If the methods to Exclude is specified than ALL the methods other than the given set are
     * serialized.
     * @param methodsToInclude If this is specified then only the specified methods are used for serialization.
     * @return xml representation of the object
     * @throws BaseException
     */
    public static StringBuilder objectToXML(Object bean, Set<String> methodsToInclude, Set<String> methodsToExclude)
            throws BaseException
    {
        StringBuilder sb = new StringBuilder();
        String className = bean.getClass().getName();
        className = className.replace("$","_dollar_");
        sb.append("<").append(className).append(" ");

        Method[] methods = bean.getClass().getMethods();

        final List<Method> methodNames = new ArrayList<Method>();
        for (final Method m : methods)
        {
            final String methodName = m.getName();
            if (methodsToInclude.size() > 0 && !methodsToInclude.contains(methodName))
                continue;

            if (methodsToExclude.size() > 0 && methodsToExclude.contains(methodName))
                continue;

            if (methodName.startsWith("get"))
            {
                methodNames.add(m);
            }
        }

        final Method[] sortedMethods = methodNames.toArray(new Method[methodNames.size()]);
        Arrays.sort(sortedMethods, new Comparator<Method>(){

            public int compare(Method o1, Method o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (final Method m : sortedMethods)
        {
            if (m.getParameterTypes().length == 0)
            {
                try
                {
                    Object res = m.invoke(bean);
                    if (res != null)
                        sb.append(m.getName().substring(3)).append("=\"").
                                append(TextUtils.escapeForXML(res.toString())).append("\"").append(" ");
                }
                catch (Exception e)
                {
                    throw new BaseException(e);
                }
            }
        }
        sb.append(" />\n");

        return sb;
    }

    public static String readFileAsString(File file)
            throws BaseException
    {
        try
        {
            return FileUtils.readFileToString(file);
        }
        catch (IOException ioex)
        {
            throw new BaseException(ioex);
        }
    }

    private static String regex(BufferedReader in, Properties keys)
            throws IOException
    {
        String line = null;
        final StringBuffer out = new StringBuffer();
        while ( (line = in.readLine()) != null) {
            String substituted = regex(line, keys);
            out.append(substituted).append("\n");
        }
        return out.toString();
    }

    /**
     * Substitutes all ${<key>} expressions in the specified file with the keys found in the Properties object.
     * @param file the file gets overwritten with the new, substituted file.
     * @param keys the key-value pairs.
     */
    public static String regex(File file, Properties keys) throws IOException {
        if (file == null || !file.exists()) return null;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            return regex(in, keys);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String regex(InputStream in, Properties keys) throws IOException
    {

        if (in == null) return null;
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(in));
            return regex(input, keys);
        }
        finally {
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * Returns a string with all parameterized values substituted for the keys specified in the {@link Properties}
     * object.  e.g. if the line = "asdf${key}" and 'key'-->'value' in the Properties object, then this method
     * would return "asdfvalue".
     * @param line the line to replace
     * @param keys a map full of the patterns and what they match to.
     * @return a substituted line.
     */
    public static String regex(String line, Properties keys) {

        if (keys == null || keys.size() == 0) {
            return line;
        }

        // this is probably a slow method.  compiling regular expressions takes some time.
        for (Iterator it = keys.keySet().iterator(); it.hasNext(); ) {
            String stringPattern = (String)it.next();
            String replace = keys.getProperty(stringPattern);

            Pattern pattern = Pattern.compile(stringPattern);
            Matcher matcher = pattern.matcher(line);
            line = matcher.replaceAll(replace);
        }

        return line;
    }

}
