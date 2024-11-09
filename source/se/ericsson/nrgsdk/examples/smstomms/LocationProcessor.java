// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import com.ericsson.hosasdk.api.TpAddress;
import com.ericsson.hosasdk.api.TpAddressPlan;
import com.ericsson.hosasdk.api.TpAddressPresentation;
import com.ericsson.hosasdk.api.TpAddressScreening;
import com.ericsson.hosasdk.api.mm.*;
import com.ericsson.hosasdk.api.mm.ul.*;
import java.util.Properties;

/**
 * This class is responsible for all NRG interation regarding
 * User Location needed for this application
 */

public class LocationProcessor extends IpAppUserLocationAdapter
    implements IpAppUserLocation
{
    private IpUserLocation itsULManager;
    private Feature itsParent;

    /**
     * Creates a new instance.
     * @param anULManager a User Location manager
     * @param aParent the Feature that will receive location updates
     */

    public LocationProcessor(IpUserLocation anULManager, Feature aParent)
    {
        itsULManager = anULManager;
        itsParent = aParent;
    }

    /**
     * Requests a location update for a set of users.
     * Location updates will be returned asynchronously, 
     * using Feature.locationReceived.
     * @param aUserList a set of users.
     */

    public void requestLocation(String aUser)
    {
        TpAddress[] users = new TpAddress[] {createE164Address(aUser)};
        TpLocationResponseTime responseTime = new TpLocationResponseTime(TpLocationResponseIndicator.P_M_NO_DELAY,
            -1); // timer value, not applicable
        float accuracy = 100f; // in meters
        boolean altitudeRequested = false;
        String locationMethod = "NETWORK";
        TpLocationRequest request = new TpLocationRequest(accuracy,
            responseTime, altitudeRequested,
            TpLocationType.P_M_CURRENT, TpLocationPriority.P_M_NORMAL,
            locationMethod);

        itsULManager.extendedLocationReportReq(this, users, request);
    }

    /**
     * Invoked by NRG to present the application with location reports.
     * The geographical position for each user for which location information
     * is available is reported to the callback.
     */

    public void extendedLocationReportRes(int anAssignmentId, TpUserLocationExtended[] reports)
    {
        for (int i = 0; i != reports.length; i++)
        {
            String user = reports[i].UserID.AddrString;
            if (reports[i].StatusCode == TpMobilityError.P_M_OK)
            {
                float latitude = reports[i].Locations[0].GeographicalPosition.Latitude;
                float longtitude = reports[i].Locations[0].GeographicalPosition.Longitude;
                itsParent.locationReceived(user, latitude, longtitude);
            }
        }
    }

    private static TpAddress createE164Address(String aNumber)
    {
        return new TpAddress(TpAddressPlan.P_ADDRESS_PLAN_E164,
            aNumber, "",
            TpAddressPresentation.P_ADDRESS_PRESENTATION_UNDEFINED,
            TpAddressScreening.P_ADDRESS_SCREENING_UNDEFINED, "");
    }
}
