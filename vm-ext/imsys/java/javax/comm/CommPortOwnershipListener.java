/*
 * CommPortOwnershipListener.java	
 *
 * @author Imsys Technologies AB, Copyright (C) 2003-2007
 *
 */

package javax.comm;
import java.util.*;

public interface CommPortOwnershipListener extends EventListener
{
	/**
	Propagates various communications port ownership events. When a port is opened, a CommPortOwnership event of type PORT_OWNED will be propagated. When a port is closed, a CommPortOwnership event of type PORT_UNOWNED will be propagated. 
	Multiple applications that are seeking ownership of a communications port can resolve their differences as follows: 

	ABCapp calls open and takes ownership of port. 
	XYZapp calls open sometime later. 
	While processing XYZapp's open, CommPortIdentifier will propagate a CommPortOwnership event with the event type PORT_OWNERSHIP_REQUESTED. 
	If ABCapp is registered to listen to these events and if it is willing to give up ownership, it calls close from within the event callback. 
	After the event has been fired, CommPortIdentifier checks to see if ownership was given up, and if so, turns over ownership of the port to XYZapp by returning success from open. 
	Note: When a close is called from within a CommPortOwnership event callback, a new CommPortOwnership event will not be generated.
    */
	public static final int PORT_OWNED=1;
	/*The port just went from unowned to owned state,
	when an application successfully called CommPortIdentifier.open.
    */
	public static final int PORT_UNOWNED=2;
	/*The port just went from owned to unowned state,
	when the port's owner called CommPort.close.
    */
	public static final int PORT_OWNERSHIP_REQUESTED=3;
	/*Ownership contention.
	The port is owned by one application and
	another application wants ownership.
	If the owner of this port is listening to this event,
	it can call CommPort.close during the processing
	of this event and thereby give up ownership of the port.
    */
	
	public abstract void ownershipChange(int type);
	/*Propagates a CommPortOwnership event.
	This method will be called with the type set
	to one of the variables PORT_OWNED, PORT_UNOWNED,
	or PORT_OWNERSHIP_REQUESTED.
    */
}

