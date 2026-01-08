/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import java.io.Externalizable;

/**
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
