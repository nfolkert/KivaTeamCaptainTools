package com.nfolkert.exception;

import org.apache.commons.lang.exception.NestableException;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nfolkert.utils.TextUtils;

public class BaseException extends NestableException
{
    private transient boolean _hasBeenLogged;

    /**
     * Create a new exception, with a message describing the immediate context of the error.
     *
     * @param message A message describing the immediate context of the error.
     */
    public BaseException(final String message)
    {
        super(message);
    }

    /**
     * Create a new exception, with a message describing the immediate context of the error, and the
     * <code>Throwable</code> that caused this exception.
     *
     * @param message A message describing the immediate context of the error.
     * @param cause   The <code>Throwable</code> that caused this exception.
     */
    public BaseException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public BaseException( final Throwable cause )
    {
        super( cause );
    }

    public String toString()
    {
        return getLocalizedMessage();
    }

    public String getMessage()
    {
        final String[] messages = getMessages();
        final int numMessages = messages.length;
        String message = null;
        for (int i = 0; i < numMessages; i++)
        {
            if (messages[i] != null)
                message = (message == null) ? messages[i] : (message + TextUtils.kLINE_SEP + messages[i]);
        }
        return message;
    }

    public void printStackTrace()
    {
        final Throwable c = getCause();
        if ( c != null )
            c.printStackTrace();
        else
            super.printStackTrace();
    }

    public void printStackTrace(final PrintStream printStream)
    {
        final Throwable c = getCause();
        if ( c != null )
            c.printStackTrace( printStream);
        else
            super.printStackTrace( printStream);
    }

    public void printStackTrace(final PrintWriter printWriter)
    {
        final Throwable c = getCause();
        if ( c != null )
            c.printStackTrace( printWriter );
        else
            super.printStackTrace(printWriter);
    }

    public static String getStackTraceAsString(Throwable thr)
    {
        final StringWriter sWriter = new StringWriter();
        thr.printStackTrace(new PrintWriter(sWriter));
        return sWriter.getBuffer().toString();
    }

    /**
     * Has this BaseException already been logged?
     */
    public boolean hasBeenLogged()
    {
        return _hasBeenLogged;
    }

    /**
     * Sets the flag indicating whether this BaseException has already been logged.
     */
    public void setHasBeenLogged(final boolean hasBeenLogged)
    {
        _hasBeenLogged = hasBeenLogged;
    }

    /**
     * Makes and returns a BaseRuntimeException class comprised of this exception.
     */
    public BaseRuntimeException asRuntimeException()
    {
        return new BaseRuntimeException(this);
    }

    /**
     *
     * @param e
     * @return e if e is a BaseException, otherwise construct a new BaseException from e and throw the new exception.
     */
    public static BaseException castOrCreate(Throwable e)
    {
        return e instanceof BaseException ? (BaseException) e : new BaseException(e);
    }
}
