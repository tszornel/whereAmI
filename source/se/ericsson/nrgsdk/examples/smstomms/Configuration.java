// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import java.io.*;
import java.util.*;
import com.ericsson.hosasdk.utility.configuration.NestedProperties;
import com.ericsson.hosasdk.utility.configuration.NotFoundException;

/**
 * This class is responsible for parsing the configuration file and
 * presenting the configuration data using convenient data types.
 */

public class Configuration extends NestedProperties
{
    public static final Configuration INSTANCE = new Configuration();

    private static String itsBillingInformation;

    /**
     * Private constructor to prevent multiple instances.
     */
    private Configuration()
    {}

    /**
     * Initializes an instance.
     * @param aSource the path of file that holds the configuration
     * @exception IOException if the file could not be loaded
     * @exception NotFoundException if an expected parameter could not
     *      be found
     */

    public void load(String aSource)
        throws IOException
    {
        super.load(aSource);

       itsBillingInformation = loadString("billingInformation");
    }

    private String loadString(String aParm)
    {
        String s = getProperty(aParm);

        if (s == null)
        {
            throw new NotFoundException(aParm);
        }
        return s;
    }

    public String getBillingInformation()
    {
        return itsBillingInformation;
    }

    private String[] loadFriends(String aParm)
    {
        List friends = new ArrayList();

        for (int i = 0;; i++)
        {
            try
            {
                friends.add(loadString(aParm + "." + i));
            }
            catch (NotFoundException e)
            {
                break;
            }
        }
        return (String[]) friends.toArray(new String[0]);
    }

    /**
     * A convenience class for grouping Subscriber-releated information
     */
    public static class Subscriber
    {
        public String phoneNumber;
        public String[] friends;
        public String contentDir = "content/";
        public Integer assignmentId;
    }

}
