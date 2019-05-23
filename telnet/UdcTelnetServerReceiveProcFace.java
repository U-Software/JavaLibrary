/* *********************************************************************
 * @(#)UdcTelnetServerReceiveProcFace.java 1.2, 25 Mar 2005
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.util.*;
import java.lang.*;



/**
 * Telnetクライアントからの要求文字列を処理するためのインタフェース。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 25 Mar 2005
 * @see 	UdcTelnetServerControler
 * @since   UDC1.2
 */
public interface UdcTelnetServerReceiveProcFace
{
	/**
	 * サーバTelnet制御インスタンスを設定します。
	 * 本メンバは、クライアント間接続が確立した場合に一度だけ呼び出されます。
	 * ユーザは、本メンバで指定されるサーバTelnet制御インスタンスを保存し、応答返却
	 * 時には、本インスタンスを介して行って下さい。<br>
	 * return で異常を返却すると、本メンバの呼び元であるUdcTelnetServerControlerからtelnetコネクションを切断します。
	 *
	 * @param	telnet サーバTelnet制御インスタンス
	 * @return	0:正常/非0:異常
	 * @since	UDC1.2
	 */
	public int setTelnet(UdcTelnetServerControler telnet);


	/**
	 * 本インスタンスの複製を生成する。
	 * クライアント間リンクが確立されると、UdcTelnetServerControlerインスタンスが生成されます。
	 * 本メンバは、このUdcTelnetServerControlerインスタンス生成の度に呼び出され、引き渡されます。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.2
	 */
	public Object clone() throws CloneNotSupportedException;


	/**
	 * Telnetクライアントからの要求文字列を受信すると、本メンバが呼び出されます。
	 * キックタイミングは、改行文字の入力です。<br>
	 * return で異常を返却すると、本メンバの呼び元であるUdcTelnetServerControlerからtelnetコネクションを切断します。
	 * また、クライアントから強制的にコネクションを切断されると、receveStr を null として本メンバを呼び出します。
	 * クライアントとのコネクションを切断したい場合は、必ず本メンバで異常を返却することによって実現して下さい。
	 *
	 * @return	0:正常/非0:異常
	 * @since	UDC1.2
	 */
	public int receive(String receiveStr);
}
