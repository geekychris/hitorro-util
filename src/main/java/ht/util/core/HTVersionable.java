package ht.util.core;

import java.io.Externalizable;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 23, 2006 Time: 4:07:52 PM
 * <p/>
 * Serializable object that supports versions
 * <p/>
 * Example:
 * <p/>
 * <pre>
 * public class C implements HTVersionable
 * {
 * 	protected static final long serialVersionUID = 1L;
 * 	private static final short codeVersion = 1;
 *
 * 	int bar = 1;
 *
 * 	public AExternalizable ()
 *     {
 *     }
 *
 * 	public AExternalizable (int bar)
 *     {
 * 		this.bar = bar;
 *     }
 *
 * 	public void writeExternal (ObjectOutput out) throws IOException
 *     {
 * 		out.writeInt(codeVersionUID);
 * 		out.writeXXX(fooo);
 *     }
 *
 * 	public void readExternal (ObjectInput in) throws IOException,
 * 			ClassNotFoundException
 *     {
 * 		short objectVersionUID = in.readShort();
 *
 * 		if (codeVersionUID &lt; objectVersionUID)
 * 			throw new IOException( Fmt.S(&quot;Version %s is not supported. Can read upto %s&quot;,
 * 			      objectVersionUID, codeVersion));
 *
 * 		switch (objectVersionUID)
 *         {
 * 		    case 1:
 * 		    case 2:
 * 			    fooo = in.readXXX();
 * 			    break;
 *         }
 * 		;
 *     }
 *
 * 	public short getVersionUID ()
 *     {
 * 		return codeVersion;
 *     }
 * }
 */
public interface HTVersionable
        extends Externalizable {
    /**
     * Get the object's code version number.
     *
     * @return Objects version number
     */
    int getCodeVersion();
}
