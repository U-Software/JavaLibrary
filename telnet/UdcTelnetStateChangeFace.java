/* *********************************************************************
 * @(#)UdcTelnetStateChangeFace.java 1.1, 25 Sep 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;


/**
 * ターミナル状態変化時のハンドリングインタフェース。
 * 
 * @author  Takayuki Uchida
 * @version 1.1, 25 Sep 2003
 * @see 	UdcTelnetControler
 * @since   UDC1.1
 */
public interface UdcTelnetStateChangeFace
{
	public final static String STC_LOCALECHO	= "LOCALECHO";
	public final static String STC_NOLOCALECHO	= "NOLOCALECHO";
	public final static String STC_NAWS			= "NAWS";
	public final static String STC_TTYPE		= "TTYPE";

	/**
	 * ターミナル状態変化時のハンドリングメンバ。
	 *
	 * @since UDC1.1
	 */
	public Object handleStateChange(String status);
}
