/**************************************************************************
* Copyright (c) 2003 by Acunia N.V. All rights reserved.                  *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package java.beans;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The common base class for bean descriptors
 */
public class FeatureDescriptor
{
    protected String name;
    protected String displayName;
    protected String shortDescription;
    protected boolean expert;
    protected boolean hidden;
    protected boolean preferred;
    protected Hashtable values;
    
    /**
     * Create a default FeatureDescriptor
     */
    public FeatureDescriptor()
    {
        name = null;
        displayName = null;
        shortDescription = null;
        expert = false;
        hidden = false;
        preferred = false;
        values = new Hashtable();
    }
    
    /**
     * Get the feature name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Set the feature name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Get the localized display name
     * TODO: Should default to programmatic name from getName
     */
    public String getDisplayName()
    {
        return displayName;
    }
    
    /**
     * Set the localized display name
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    
    /**
     * Indicate if this is a feature for expert users
     */
    public boolean isExpert()
    {
        return expert;
    }
    /**
     * Set if this is a feature for expert users
     */
    public void setExpert(boolean expert)
    {
        this.expert = expert;
    }
    
    /**
     * Indicate if this is a feature for tool use only
     */
    public boolean isHidden()
    {
        return hidden;
    }
    
    /**
     * Set if this is a feature for tool use only
     */
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }
    
    /**
     * Indicate if this is an important feature for humans
     */
    public boolean isPreferred()
    {
        return preferred;
    }
    
    /**
     * Set if this is an important feature for humans
     */
    public void setPreferred(boolean preferred)
    {
        this.preferred = preferred;
    }
    
    /**
     * Get a short description of this feature
     */
    public String getShortDescription()
    {
        return shortDescription;
    }
    
    /**
     * Set the short description of this feature
     */
    public void setShortDescription(String text)
    {
        this.shortDescription = text;
    }
    
    /**
     * Associate a named attribute with this feature
     */
    public void setValue(String attributeName, Object value)
    {
        values.put(attributeName, value);
    }
    
    /**
     * Get a named attribute from this feature
     */
    public Object getValue(String attributeName)
    {
        if (values.containsKey(attributeName))
            return values.get(attributeName);
        else
            return null;
    }
    
    /**
     * Return the names of all attribute associated with this feature
     */
    public Enumeration attributeNames()
    {
        return values.keys();
    }
}
