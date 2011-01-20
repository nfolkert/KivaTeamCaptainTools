package com.nfolkert.kiva.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KivaProperties
{
    public static Properties _kivaProps = loadProperties();

    private static Properties loadProperties()
    {
        final String resName = "kiva.properties";
        Properties props = new Properties();
        final InputStream inStream = KivaProperties.class.getClassLoader().getResourceAsStream(resName);
        if ( inStream == null )
            throw new IllegalStateException( "Failed to find resource: " + resName );
        try
        {
            props.load( inStream);
            return props;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load resource: "+ resName, e );

        }
    }
}
