// #copyright
package se.ericsson.nrgsdk.examples.smstomms;

import com.ericsson.hosasdk.api.hui.IpAppHosaUIManagerAdapter;
import com.ericsson.hosasdk.api.hui.IpAppHosaUIManager;
import com.ericsson.hosasdk.api.hui.IpHosaUIManager;

import com.ericsson.hosasdk.api.ui.P_UI_RESPONSE_REQUIRED;

import com.ericsson.hosasdk.api.TpAddress;
import com.ericsson.hosasdk.api.TpAddressPlan;
import com.ericsson.hosasdk.api.TpAddressPresentation;
import com.ericsson.hosasdk.api.TpAddressScreening;
import com.ericsson.hosasdk.api.TpHosaSendMessageError;
import com.ericsson.hosasdk.api.TpHosaSendMessageReport;
import com.ericsson.hosasdk.api.TpHosaTerminatingAddressList;
import com.ericsson.hosasdk.api.TpHosaMessage;
import com.ericsson.hosasdk.api.TpHosaUIMessageDeliveryType;
import com.ericsson.hosasdk.api.TpHosaDeliveryTime;

import java.util.Properties;

/**
 * This class is responsible for:
 * <ul>
 * <li>Sending an MMS message.</li>
 * <li>Logging success or failure of sending an MMS message.</li>
 * </ul>
 */
public class MMSProcessor extends IpAppHosaUIManagerAdapter
    implements IpAppHosaUIManager
{
    private IpHosaUIManager itsHosaUIManager;
    private Feature itsParent;

    /**
     * @param aHosaUIManager manager used to talk to the NRG
     * @param aParent the Parent to which this class can callback to
     */
    public MMSProcessor(IpHosaUIManager aHosaUIManager, Feature aParent)
    {
        itsHosaUIManager = aHosaUIManager;
        itsParent = aParent;
    }

    /**
     * Send an MMS.
     * @param aSender sender of the MMS
     * @param aReceiver receiver of the MMS
     * @param aMessageContent message as a byte array
     * @param aMessageSubject subject of the MMS
     */
    protected void sendMMS(String aSender, String aReceiver, byte[] aMessageContent, String aMessageSubject)
    {
        IpAppHosaUIManager appHosaUIManager = this;

        TpHosaUIMessageDeliveryType deliveryType = TpHosaUIMessageDeliveryType.P_HUI_MMS;

        // Create a dummy delivery time (send immediately)
        TpHosaDeliveryTime deliveryTime = new TpHosaDeliveryTime();

        deliveryTime.Dummy((short) 0);

        TpAddress originatingAddress = createE164Address(aSender);
        TpAddress destinationAddress = createE164Address(aReceiver);

        TpHosaTerminatingAddressList recipients = new TpHosaTerminatingAddressList();
        recipients.ToAddressList = new TpAddress[] {destinationAddress};

        TpHosaMessage message = new TpHosaMessage();
        message.BinaryData(aMessageContent);

        TpHosaDeliveryTime t = new TpHosaDeliveryTime();
        // t.Dummy((short)0);
        t.DeliveryTime("2005-01-02 01:02:03.045");

        // Send message
        itsHosaUIManager.hosaSendMessageReq(appHosaUIManager, // callback
            originatingAddress, recipients, aMessageSubject, message,
            deliveryType,
            Configuration.INSTANCE.getBillingInformation(), // billingID (operator defined)
            P_UI_RESPONSE_REQUIRED.value, true, // deliveryNotificationRequested
            t, // deliveryTime (not applicable)
            "2006-01-02 01:02:03.045"); // validityTime (not applicable)
    }

    /**
     * Called by the NRG when something went wrong sending the message.
     * @see com.ericsson.hosasdk.api.hui.IpAppHosaUIManager
     */
    public void hosaSendMessageErr(int anAssignmentID,
        TpHosaSendMessageError[] anErrorList)
    {
        System.out.println("\nError sending the MMS to "
            + anErrorList[0].UserAddress.AddrString + "(ErrorCode "
            + anErrorList[0].Error.value() + ")");
    }

    /**
     * Called by the NRG when sending the message is a succes.
     * @see com.ericsson.hosasdk.api.hui.IpAppHosaUIManager
     */
    public void hosaSendMessageRes(int anAssignmentID,
        TpHosaSendMessageReport[] aResponseList)
    {
        System.out.println("\nMMS Message sent to "
            + aResponseList[0].UserAddress.AddrString);
    }

    /**
     * @param aNumber A String from which a TpAddress is created with a
     * TpAdddressPlan: P_ADDRESS_PLAN_E164.
     * @return A default TpAddress, based on an address string.
     */
    private static TpAddress createE164Address(String aNumber)
    {
        return new TpAddress(TpAddressPlan.P_ADDRESS_PLAN_E164,
            aNumber, // address
            "", // name
            TpAddressPresentation.P_ADDRESS_PRESENTATION_UNDEFINED,
            TpAddressScreening.P_ADDRESS_SCREENING_UNDEFINED, ""); // subaddress
    }

}
