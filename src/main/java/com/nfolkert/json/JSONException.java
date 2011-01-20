package com.nfolkert.json;

import java.io.Serializable;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 *
 * @author JSON.org
 * @version 2
 */
public class JSONException extends Exception implements Serializable
{
    /**
     * Constructs a JSONException with an explanatory message.
     *
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message)
    {
        super(message);
    }

    public JSONException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public JSONException(Throwable t)
    {
        super(t.getMessage(), t );
    }
}
