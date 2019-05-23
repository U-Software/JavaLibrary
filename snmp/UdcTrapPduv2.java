/* *********************************************************************
 * @(#)UdcTrapPduv2.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.util.*;
import java.io.*;
import java.net.*;



/**
 * SNMPv2c TRAP-PDU型クラス。
 * TRAPを送信する場合、本インスタンスを生成・設定し、
 * UdcSnmpContext/UdcSnmpAgentContextに引き渡す。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcPdu
 * @since	UDC1.0
 */
public class UdcTrapPduv2 extends UdcPdu
{
	/**
	 * SNMP-TrapV2 - generic-trapの文字列表現リスト
	 * @since	UDC1.0
	 */
	public final static String [] genericTrapStrings = {
		"Cold Start",
		"Warm Start",
		"Link Down",
		"Link Up",
		"Authentication Failure"
	};

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcTrapPduv2()
	{
		super();
		setMsgTypeTRAPV2();
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		return super.toString();
	}
}
