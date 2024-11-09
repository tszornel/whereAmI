// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import com.ericsson.hosasdk.api.*;
import com.ericsson.hosasdk.utility.framework.FWproxy;
import com.ericsson.hosasdk.api.ui.TpUIEventCriteria;
import com.ericsson.hosasdk.api.hui.IpHosaUIManager;
import com.ericsson.hosasdk.utility.log.*;
import com.ericsson.hosasdk.api.HOSAMonitor;
import com.ericsson.hosasdk.api.mm.ul.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This class implements the logic of the application.
 * It uses processors to interact with NRG.
 */
public class Feature
{
    private Main itsParent;
    private FWproxy itsFramework;
    private IpHosaUIManager itsHosaUIManager;
    private IpUserLocation itsOsaULManager;

    private SMSProcessor itsSMSProcessor;
    private MMSProcessor itsMMSProcessor;
    private LocationProcessor itsLocationProcessor;
    private GUI theGUI;

    Integer assignmentId;

    /**
     * Initializes a new instance, without starting interaction with NRG
     * (see start)
     *
     * @param aGUI the GUI of the application
     */
    public Feature(GUI aGUI)
    {
        theGUI = aGUI;
        aGUI.setTitle("whereAmI");
        aGUI.addTab("Description", getDescription());
    }

    /**
     * Starts interaction with the NRG.
     * Note: this method is intended to be called at most once.
     */
    protected void start()
    {
        System.out.println("Starting Parlay tracing");
        SimpleTracer.SINGLETON.PRINT_STACKTRACES = false;
        HOSAMonitor.addListener(SimpleTracer.SINGLETON);

        System.out.println("Getting framework");
        itsFramework = new FWproxy(Configuration.INSTANCE);

        System.out.println("Getting service manager");
        itsHosaUIManager = (IpHosaUIManager) itsFramework.obtainSCF("SP_HOSA_USER_INTERACTION");
        itsOsaULManager = (IpUserLocation) itsFramework.obtainSCF("P_USER_LOCATION");

        System.out.println("Creating processor");
        itsSMSProcessor = new SMSProcessor(itsHosaUIManager, this);
        itsMMSProcessor = new MMSProcessor(itsHosaUIManager, this);
        itsLocationProcessor = new LocationProcessor(itsOsaULManager,
            this);

        System.out.println("Starting SMS notification");
        assignmentId = new Integer(itsSMSProcessor.startNotifications("444"));

    }

    /**
     * Stops interaction with the NRG and disposes of all resources
     * allocated by this instance.
     * Note: this method is intended to be called at most once.
     */
    public void stop()
    {
        System.out.println("Stopping SMS notification");
        if (assignmentId != null)
        {
            itsSMSProcessor.stopNotifications(assignmentId.intValue());
        }
        assignmentId = null;

        System.out.println("Disposing processor");
        if (itsSMSProcessor != null)
        {
            itsSMSProcessor.dispose();
        }

        if (itsMMSProcessor != null)
        {
            itsMMSProcessor.dispose();
        }

        if (itsLocationProcessor != null)
        {
            itsLocationProcessor.dispose();
        }

        System.out.println("Disposing service manager");
        if (itsHosaUIManager != null)
        { itsFramework.releaseSCF(itsHosaUIManager);
        }
        if (itsOsaULManager != null)
        { itsFramework.releaseSCF(itsOsaULManager);
        }

        System.out.println("Disposing framework");
        if (itsFramework != null)
        {
            itsFramework.endAccess();
            itsFramework.dispose();
        }

        System.out.println("Stopping Parlay tracing");
        HOSAMonitor.removeListener(SimpleTracer.SINGLETON);
        System.exit(0);
    }

    /**
     * Invoked by the SMSProcessor, when a notification is received.
     */
    protected void smsReceived(String aSender, String aReceiver, String aMessageContent)
    {
        itsLocationProcessor.requestLocation(aSender);
    }

    public void locationReceived(String user, float latitude, float longitude)
    {
        try
        {
            Image map = new ImageIcon("content/map.gif").getImage();
            int wm = map.getWidth(theGUI);
            int hm = map.getHeight(theGUI);
            Image phone = new ImageIcon("content/phone.png").getImage();
            int wp = phone.getWidth(theGUI);
            int hp = phone.getHeight(theGUI);
            if (latitude < 0)
            {
                latitude = 0;
            }
            if (latitude > 1)
            {
                latitude = 1;
            }
            if (longitude < 0)
            {
                longitude = 0;
            }
            if (longitude > 1)
            {
                longitude = 1;
            }
            int x = (int) (latitude * wm - wp / 2);
            int y = (int) (longitude * hm - hp / 2);
            Plotter plotter = new Plotter(wm, hm);
            plotter.drawImage(map, 0, 0, theGUI);
            plotter.drawImage(phone, x, y, theGUI);

            MMSMessageContent messageContent = new MMSMessageContent();
            messageContent.addMedia(plotter.createDataSource());
            itsMMSProcessor.sendMMS("444", user,
                messageContent.getBinaryContent(), "Current location");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns a descriptive text that explains the application and its configuration.
     */
    private String getDescription()
    {
        String s = "Press START to connect to the Framework";
        s += " and request the H-OSA Messaging (HUI) services from the Framework.\n";
        s += "\n";
        s += "When the user sends an SMS towards service number 444, ";
        s += "the application returns an MMS showing the current location of the user.\n";
        s += "\n";
        s += "Press STOP to release resources in the NRG and the application.\n";
        return s;
    }
}
