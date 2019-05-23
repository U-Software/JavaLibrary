/* *********************************************************************
 * @(#)UdcThreadChannelDtr.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

/**
 * スレッド間通信のための通信情報クラス。
 * UDCではスレッド間通信のために ThreadChannel クラスを提供する。本クラス
 * このクラスとの組合せで使用することを原則とします。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadChannel
 * @since	UDC1.0
 */
public class UdcThreadChannelDtr implements Cloneable
{
	/**
	 * スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)
	 * @since	UDC1.0
	 */
	private int					invokeId = -1;

	/**
	 * スレッド間通信メッセージ種別
	 * @since	UDC1.0
	 */
	private int					dtrType = -1;

	/**
	 * 処理結果(スレッド間通信の応答返却時の要求に対する結果)
	 * @since	UDC1.0
	 */
	private int					result = -1;

	/**
	 * スレッド間通信ユーザ情報
	 * @since	UDC1.0
	 */
	private Object				data = null;

	/**
	 * スレッド間通信の要求に対する応答の返却先スレッドチャンネル
	 * @since	UDC1.0
	 */
	private UdcThreadChannel	replyChannel = null;

	/**
	 * スレッド間通信の応答メッセージ種別
	 * @since	UDC1.0
	 */
	private int					replyDtrType = -1;

	/**
	 * スレッド間通信の優先度
	 * @since	UDC1.0
	 */
	private int					dtrLevel = 50;


	/**
	 * コンストラクタ
	 *
	 * @param	invid	スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)
	 * @param	type	スレッド間通信メッセージ種別
	 * @see		UdcThreadMsgId
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr(int invid, int type)
	{
		invokeId = invid;
		dtrType = type;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	invid	スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)
	 * @param	type	スレッド間通信メッセージ種別
	 * @param	d		スレッド間通信ユーザ情報
	 * @see		UdcThreadMsgId
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr(int invid, int type, Object d)
	{
		invokeId = invid;
		dtrType = type;
		data = d;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	repch	スレッド間通信の要求に対する応答の返却先スレッドチャンネル
	 * @param	invid	スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)
	 * @param	type	スレッド間通信種別
	 * @param	res		処理結果(スレッド間通信の応答返却時の要求に対する結果)
	 * @param	d		スレッド間通信ユーザ情報
	 * @see		UdcThreadMsgId
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr(UdcThreadChannel repch, int invid, int type, int res, Object d)
	{
		replyChannel = repch;
		invokeId = invid;
		dtrType = type;
		result = res;
		data = d;
	}

	/**
	 * スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)を取得する。
	 *
	 * @return 	スレッド間通信インボークID(スレッド間通信の要求に対する応答の対応識別子)
	 * @since	UDC1.0
	 */
	public int get_invokeId() { return invokeId; }

	/**
	 * スレッド間通信メッセージ種別を取得する。
	 *
	 * @return	スレッド間通信メッセージ種別
	 * @since	UDC1.0
	 */
	public int get_dtrType() { return dtrType; }

	/**
	 * 処理結果(スレッド間通信の応答返却時の要求に対する結果)を取得する。
	 *
	 * @return	処理結果(スレッド間通信の応答返却時の要求に対する結果)
	 * @since	UDC1.0
	 */
	public int get_result() { return result; }

	/**
	 * 処理結果(スレッド間通信の応答返却時の要求に対する結果)を設定する。
	 *
	 * @param	res	処理結果(スレッド間通信の応答返却時の要求に対する結果)
	 * @since	UDC1.0
	 */
	public void set_result(int res) { result = res; }

	/**
	 * スレッド間通信の要求に対する応答の返却先スレッドチャンネルを取得する。
	 *
	 * @return	スレッド間通信の要求に対する応答の返却先スレッドチャンネル
	 * @since	UDC1.0
	 */
	public UdcThreadChannel get_replyChannel() { return replyChannel; }

	/**
	 * スレッド間通信の要求に対する応答の返却先スレッドチャンネルを設定する。
	 *
	 * @param	repch	スレッド間通信の要求に対する応答の返却先スレッドチャンネル
	 * @since	UDC1.0
	 */
	public void set_replyChannel(UdcThreadChannel repch) { replyChannel = repch; }

	/**
	 * スレッド間通信ユーザ情報を取得する。
	 *
	 * @return	スレッド間通信ユーザ情報
	 * @since	UDC1.0
	 */
	public Object get_data() { return data; }

	/**
	 * スレッド間通信ユーザ情報を設定する。
	 *
	 * @param	d	スレッド間通信ユーザ情報
	 * @since	UDC1.0
	 */
	public void set_data(Object d) { data = d; }

	/**
	 * スレッド間通信の応答メッセージ種別を取得する。
	 *
	 * @return	スレッド間通信の応答メッセージ種別
	 * @since	UDC1.0
	 */
	public int get_replyDtrType() { return replyDtrType; }

	/**
	 * スレッド間通信の応答メッセージ種別を設定する。
	 *
	 * @param	repdtrtype	スレッド間通信の応答メッセージ種別
	 * @since	UDC1.0
	 */
	public void set_replyDtrType(int repdtrtype) { replyDtrType = repdtrtype; }

	/**
	 * スレッド間通信の優先度を取得する。
	 *
	 * @return 	スレッド間通信の優先度
	 * @since	UDC1.0
	 */
	public int get_dtrLevel() { return dtrLevel; }

	/**
	 * スレッド間通信の優先度を設定する。
	 * 本設定はUdcThreadChannelに送信(push)する前に行う必要があります。本設定を行わないと
	 * 通信優先度レベルのデフォルトは[50]です。
	 *
	 * @param 	level	スレッド間通信の優先度
	 * @since	UDC1.0
	 */
	public void set_dtrLevel(int level) { dtrLevel = level; }

	/**
	 * 本インスタンスの複製を作成します。
	 * スレッド間通信ユーザ情報は複製を作成せずにもとの情報をそのまま設定します。
	 *
	 * @return 	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcThreadChannelDtr obj = new UdcThreadChannelDtr(replyChannel, invokeId, dtrType, result, data);
		obj.set_replyDtrType(replyDtrType);
		return obj;
	}
}

