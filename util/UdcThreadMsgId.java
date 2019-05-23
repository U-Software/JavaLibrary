/* *********************************************************************
 * @(#)UdcThreadMsgId.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;


/**
 * スレッド間通信で使用するスレッド間通信メッセージID定義クラス。
 * 本メッセージID定義は、UDCが予約するメッセージIDです。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadChannel
 * @see		UdcThreadChannelDtr
 * @since	UDC1.0
 */
public interface UdcThreadMsgId
{
	/**
	 * UDC内部処理で使用するスレッド間通信メッセージID。要求時に使用する、ベース値。
	 * @since UDC1.0
	 */
	public static final int	Request_Base 			= 0x00000000;

	/**
	 * UDC内部処理で使用するスレッド間通信メッセージID。応答時に使用する、ベース値。
	 * @since UDC1.0
	 */
	public static final int	Response_Base 			= 0x10000000;

	/**
	 * UDC内部処理で使用するスレッド間通信メッセージID。イベント通知時に使用する、ベース値。
	 * @since UDC1.0
	 */
	public static final int	Event_Base 				= 0x20000000;


	//--------------------------------------------------------------
	// SNMP関連
	//--------------------------------------------------------------
	/**
	 * UDC内部のSNMP処理で使用するスレッド間通信メッセージID(SNMP Request)
	 * @since UDC1.0
	 */
	public static final int	Request_Snmp			= 0x00000100;

	/**
	 * UDC内部のSNMP処理で使用するスレッド間通信メッセージID(SNMP Response)
	 * @since UDC1.0
	 */
	public static final int	Response_Snmp			= 0x10000100;	/* SNMP Response */

	/**
	 * UDC内部のSNMP処理で使用するスレッド間通信メッセージID(SNMP V1 Trap)
	 * udc.snmp.UdcSnmpContextで登録するトラップ受信チャンネルには本値が設定されます。
	 * @since UDC1.0
	 */
	public static final int	Event_SnmpTrapV1		= 0x20000101;

	/**
	 * UDC内部のSNMP処理で使用するスレッド間通信メッセージID(SNMP V2c Trap)
	 * udc.snmp.UdcSnmpContextで登録するトラップ受信チャンネルには本値が設定されます。
	 * @since UDC1.0
	 */
	public static final int	Event_SnmpTrapV2		= 0x20000102;

	/**
	 * UDC内部のSNMP処理で使用するスレッド間通信メッセージID(SNMP V2c InformRequest)
	 * udc.snmp.UdcSnmpContextで登録するトラップ受信チャンネルには本値が設定されます。
	 * @since UDC1.0
	 */
	public static final int	Event_SnmpInformRequest	= 0x20000103;


	//--------------------------------------------------------------
	// タイマ関連
	//--------------------------------------------------------------
	/**
	 * タイマスレッドからの指定時間超過通知
	 * @since UDC1.0
	 */
	public static final int	Event_Timeout 			= 0x20010001;

}

