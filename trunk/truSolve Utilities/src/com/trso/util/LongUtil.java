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
package com.trso.util;

public class LongUtil
{
	@SuppressWarnings("unused")
	private static final String CLASS_ID = "$Id$";

	
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
}
