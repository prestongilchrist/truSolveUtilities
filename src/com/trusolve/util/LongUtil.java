/**
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 
 * 1.1 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is truSolve Utilities.
 * 
 * The Initial Developer of the Original Code is
 * truSolve.com.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 *
 * $Id$
 *
 */
package com.trusolve.util;

/**
 * This class provides some simple utilities for use in manipulation of long values.  Most methods will be static.
 * 
 * @author pwg
 *
 */
public class LongUtil
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	
	/**
	 * Breaks a long value down into a byte array.
	 * 
	 * @param data a long value that will be broken down into a byte array.
	 * @return a byte array that contains the byte decomposition of the provided long.
	 */
	public static byte[] asByteArray( long data )
	{
		return(
				new byte[]{
						(byte)((data >> 56) & 0xff),
						(byte)((data >> 48) & 0xff),
						(byte)((data >> 40) & 0xff),
						(byte)((data >> 32) & 0xff),
						(byte)((data >> 24) & 0xff),
						(byte)((data >> 16) & 0xff),
						(byte)((data >> 8) & 0xff),
						(byte)(data & 0xff)
						}
		);
	}
	/**
	 * Convert 8 bytes to a corresponding long.
	 * 
	 * @param data8 Most significant byte
	 * @param data7 byte 7
	 * @param data6 byte 6
	 * @param data5 byte 5
	 * @param data4 byte 4
	 * @param data3 byte 3
	 * @param data2 byte 2
	 * @param data1 Least significant byte
	 * @return
	 */
	public static long fromBytes( byte data8, byte data7, byte data6, byte data5, byte data4, byte data3, byte data2, byte data1 )
	{
		long v = data8;
		v = v << 8;
		v &= data7;
		v = v << 8;
		v &= data6;
		v = v << 8;
		v &= data5;
		v = v << 8;
		v &= data4;
		v = v << 8;
		v &= data3;
		v = v << 8;
		v &= data2;
		v = v << 8;
		v &= data1;
		return( v );
	}
	
	/**
	 * Create a long value from a corresponding byte array.  The 7th byte is the most significant byte and the 0th is the least significant.
	 * @param byteArray
	 * @return a long value
	 */
	public static long fromBytes( byte[] byteArray )
	{
		long v = byteArray[7];
		v = v << 8;
		v &= byteArray[6];
		v = v << 8;
		v &= byteArray[5];
		v = v << 8;
		v &= byteArray[4];
		v = v << 8;
		v &= byteArray[3];
		v = v << 8;
		v &= byteArray[2];
		v = v << 8;
		v &= byteArray[1];
		v = v << 8;
		v &= byteArray[0];
		return( v );
	}
}
