/* *********************************************************************
 * @(#)UdcTelnetConstants.java 1.1, 25 Sep 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;


/**
 * telnetプロトコルでのプロトコル制御コード定義インタフェース。
 *
 * @author  Takayuki Uchida
 * @version 1.1, 25 Sep 2003
 * @see 	UdcTelnet
 * @since   UDC1.0
 */
public interface UdcTelnetConstants
{
	/**
	 * IAC - telnetプロトコルネゴシエーション時の先頭シーケンス。
	 */
	public final static byte IAC  = (byte)255;

	/**
	 * EOR - telnetプロトコルネゴシエーション時の最終レコード。
	 */
	public final static byte EOR  = (byte)239;

	/**
	 * IAC WILL - 
	 */
	public final static byte WILL  = (byte)251;

	/**
	 * IAC WONT -
	 */
	public final static byte WONT  = (byte)252;

	/**
	 * IAC DO -
	 */
	public final static byte DO    = (byte)253;

	/**
	 * IAC DONT -
	 */
	public final static byte DONT  = (byte)254;

	/**
	 * IAC Sub Begin -
	 */
	public final static byte SB  = (byte)250;

	/**
	 * IAC Sub End -
	 */
	public final static byte SE  = (byte)240;

	/**
	 * Telnetオプション - echo text
	 */
	public final static byte TELOPT_ECHO  = (byte)1;  /* echo on/off */

	/**
	 * Telnetオプション - End Of Record
	 */
	public final static byte TELOPT_EOR   = (byte)25;  /* end of record */

	/**
	 * Telnetオプション - Negotiate About Window Size
	 */
	public final static byte TELOPT_NAWS  = (byte)31;  /* NA-WindowSize*/

	/**
	 * Telnetオプション - Terminal Type
	 */
	public final static byte TELOPT_TTYPE  = (byte)24;  /* terminal type */

	/**
	 * Telnetオプション - qualifier 'IS'
	 */
	public final static byte TELQUAL_IS = (byte)0;

	/**
	 * Telnetオプション - qualifier 'SEND'
	 */
	public final static byte TELQUAL_SEND = (byte)1;

	/**
	 * telnetサーバポート番号。
	 */
	public final static int WellKnownPort  = 23;
}
