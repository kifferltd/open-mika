package org.bouncycastle.i18n;

import java.util.Locale;
import java.util.TimeZone;

public class ErrorBundle extends MessageBundle 
{

    /**
     * summary entry key
     */
    public static final String SUMMARY_ENTRY = "summary";
    
    /**
     * detail entry key
     */
    public static final String DETAIL_ENTRY = "details";
    
    /**
     * Constructs a new ErrorBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public ErrorBundle(String resource, String id) throws NullPointerException
    {
        super(resource, id);
    }

    /**
     * Constructs a new ErrorBundle using <code>resource</code> as the base name for the 
     * RessourceBundle and <code>id</code> as the message bundle id the resource file. 
     * @param resource base name of the resource file 
     * @param id the id of the corresponding bundle in the resource file
     * @param arguments an array containing the arguments for the message
     * @throws NullPointerException if <code>resource</code> or <code>id</code> is <code>null</code>
     */
    public ErrorBundle(String resource, String id, Object[] arguments) throws NullPointerException
    {
        super(resource, id, arguments);
    }
    
    /**
     * Returns the summary message in the given locale and timezone.
     * @param loc the {@link Locale}
     * @param timezone the {@link TimeZone}
     * @return the summary message.
     * @throws MissingEntryException if the message is not available
     */
    public String getSummary(Locale loc, TimeZone timezone) throws MissingEntryException
    {
        return getEntry(SUMMARY_ENTRY,loc,timezone);
    }
    
    /**
     * Returns the summary message in the given locale and the default timezone.
     * @param loc the {@link Locale}
     * @return the summary message.
     * @throws MissingEntryException if the message is not available
     */
    public String getSummary(Locale loc) throws MissingEntryException
    {
        return getEntry(SUMMARY_ENTRY,loc,TimeZone.getDefault());
    }
    
    /**
     * Returns the detail message in the given locale and timezone.
     * @param loc the {@link Locale}
     * @param timezone the {@link TimeZone}
     * @return the detail message.
     * @throws MissingEntryException if the message is not available
     */
    public String getDetail(Locale loc, TimeZone timezone) throws MissingEntryException
    {
        return getEntry(DETAIL_ENTRY,loc,timezone);
    }
    
    /**
     * Returns the detail message in the given locale and the default timezone.
     * @param loc the {@link Locale}
     * @return the detail message.
     * @throws MissingEntryException if the message is not available
     */
    public String getDetail(Locale loc) throws MissingEntryException
    {
        return getEntry(DETAIL_ENTRY,loc,TimeZone.getDefault());
    }

}
