// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import com.sun.image.codec.jpeg.*;
import javax.activation.DataSource;

/**
 * This class is responsible for initializing, starting, stopping and
 * terminating the application.
 */

public class Plotter
{
    private BufferedImage bi;
    private Graphics2D g; 

    public Plotter(int aWidth, int aHeight)
    {
        bi = new BufferedImage(aWidth, aHeight,
            BufferedImage.TYPE_3BYTE_BGR);
        g = bi.createGraphics();
    }

    public void drawImage(Image anImage, int x, int y, ImageObserver anObserver)
    {
        g.drawImage(anImage, x, y, anObserver);
    }

    public byte[] jpegEncoding()
        throws Exception
    {
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        JPEGEncodeParam parms = JPEGCodec.getDefaultJPEGEncodeParam(bi);
        parms.setQuality(0.5f, true);
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(f,
            parms);
        encoder.encode(bi);
        byte[] r = f.toByteArray();
        f.close();
        return r;
    }

    public DataSource createDataSource()
        throws Exception
    {
        return new RawDataSource("image/jpeg", jpegEncoding());
    }
} class RawDataSource
    implements DataSource
{
    String type;
    byte[] buffer;

    public RawDataSource(String type, byte[] buffer)
    {
        this.type = type;
        this.buffer = buffer;
    }

    public String getContentType() 
    {
        return type;
    }

    public InputStream getInputStream() 
    {
        return new ByteArrayInputStream(buffer);
    }

    public String getName() 
    {
        return toString();
    }

    public OutputStream getOutputStream()
    {
        throw new RuntimeException("Not supported");
    }
}
