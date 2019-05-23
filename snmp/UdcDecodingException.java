/* *********************************************************************
 * @(#)UdcException.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

/**
 * UDCでのデコード例外クラス。

 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcDecodingException extends Exception
{
	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcDecodingException()
	{
		super();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	str		例外文字列
	 * @since	UDC1.0
	 */
	public UdcDecodingException(String str)
	{
		super(str);
	}
}
