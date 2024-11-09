// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import javax.activation.MimetypesFileTypeMap;
import javax.activation.DataHandler;
import javax.activation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * This class holds the content of an MMS message, it is responsible for
 * composing the message and has the ability to return the content as a byte[]
 * so it can be used as input for a TpMessage.
 */
public class MMSMessageContent extends MimeMultipart
{
    public MMSMessageContent()
    {}

    /**
     * Adds a part (String) to the message content.
     * It is expected to be us-ascii text.
     * @param aTextPart The String to be added.
     */
    public void addTextMedia(String aTextPart)
    {
        try
        {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(aTextPart, "us-ascii");
            mbp.addHeader("Content-Type", mbp.getContentType());
            addBodyPart(mbp);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Adds a part (mediaobject) to the message content.
     * @param aFilename A filename refering to a file of which a
     * MimeBodyPart is created and added to the MMSContent
     */
    public void addMedia(String aFilename)
    {
        try
        {
            FileDataSource fds = new FileDataSource(aFilename);
            MimetypesFileTypeMap map = new MimetypesFileTypeMap();

            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setDataHandler(new DataHandler(fds));
            mbp.setHeader("Content-Type",
                map.getContentType(aFilename));
            mbp.setHeader("Content-Transfer-Encoding", "base64");
            mbp.setContentID("CONT" + getCount());
            addBodyPart(mbp);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void addMedia(DataSource aSource)
    {
        try
        {
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setDataHandler(new DataHandler(aSource));
            mbp.setHeader("Content-Type", aSource.getContentType());
            mbp.setHeader("Content-Transfer-Encoding", "base64");
            mbp.setContentID("CONT" + getCount());
            addBodyPart(mbp);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Adds a Byte Array bodypart
     * @param aByteBodyPart the Byte bodypart that has to be added
     * @param aContentType the content type of the bodypart
     */
    public void addByteArrayBodyPart(byte[] aByteBodyPart, String aContentType)
    {
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(aByteBodyPart);
            MimeBodyPart mbp = new MimeBodyPart(bis);

            mbp.setHeader("Content-Type", aContentType);
            mbp.setContentID("CONT" + getCount());
            addBodyPart(mbp);
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * @return A byte array of the complete(including the content-type of the
     * whole MMSMessage Content) content.
     */
    public byte[] getBinaryContent()

    {
        try
        {
            ByteArrayOutputStream byteArrayBuffer = new ByteArrayOutputStream();
            // The content type of the whole content is requested and added to the output
            String contentType = "Content-Type: "
                + this.getContentType() + "\r\n\r\n";
            byteArrayBuffer.write(contentType.getBytes());
            byteArrayBuffer.flush();
            writeTo(byteArrayBuffer);
            return byteArrayBuffer.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.getMessage());
        }
        catch (MessagingException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }
}
