/* *********************************************************************
 * @(#)UdcNextMIBObjectException.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;


/**
 * SNMP-GETNEXT時に自身OIDに次要素が存在しない場合のための例外
 * クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcNextMIBObjectException extends Exception
{
	/**
	 * コンストラクタ。
	 *
	 * @since	UDC1.0
	 */
	public UdcNextMIBObjectException()
	{
		super();
	}

	/**
	 * コンストラクタ。
	 *
	 * @param	str		例外文字列
	 * @since	UDC1.0
	 */
	public UdcNextMIBObjectException(String str)
	{
		super(str);
	}
}
