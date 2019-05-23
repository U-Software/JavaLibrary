/* *********************************************************************
 * @(#)UdcThreadMsgDtrEventTimeout.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.Date;



/**
 * スレッド間通信でタイマ管理スレッドからのタイムアウトイベント通知時の
 * タイムアウト情報クラス。
 * 本クラスは、UdcThreadChannelDtrに設定されます。
 *		UdcThreadChannelDtr.dtrType = UdcThreadMsgId.Event_Timeout
 *		UdcThreadChannelDtr.data 	= 本インスタンス
 *		UdcThreadChannelDtr.result	= 0;
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadChannelDtr
 * @since	UDC1.0
 */
public class UdcThreadMsgDtrEventTimeout implements Cloneable
{
	/**
	 * タイマ種別　: タイムアウト
	 * @since	UDC1.0
	 */
	public static int	Timeout			= 1;

	/**
	 * タイマ種別　: タイムインターバル
	 * @since	UDC1.0
	 */
	public static int	TimeInterval	= 2;

	/**
	 * タイマ種別　: 定時刻通知
	 * @since	UDC1.0
	 */
	public static int	FixedTime		= 3;

	/**
	 * タイマ識別子管理
	 * @since	UDC1.0
	 */
	private	static 		UdcGenerateIndex	gen = null;

	/**
	 * タイマ識別子
	 * @since	UDC1.0
	 */
	protected int		timerIndex = -1;

	/**
	 * タイマ種別
	 * @since	UDC1.k0
	 */
	protected int		timerType = UdcThreadMsgDtrEventTimeout.Timeout;

	/**
	 * 起床待ち時間(ミリ秒)
	 * @since	UDC1.0
	 */
	protected long		waitTime = -1;

	/**
	 * 起床時刻。
	 * @since	UDC1.0
	 */
	protected Date		fixedTime;

	/**
	 * コンストラクタ
	 * タイマ種別がタイムアウト/タイムインターバル時のコンストラクタ。
	 *
	 * @param	type	タイマ種別(タイムアウト/タイムインターバル)
	 * @param	milisec	起床までの時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public UdcThreadMsgDtrEventTimeout(int type, long milisec)
	{
		timerType = type;
		waitTime = milisec;
	}

	/**
	 * コンストラクタ
	 * タイマ種別が定時刻のコンストラクタ。
	 *
	 * @param	fixed 起床日時
	 * @since	UDC1.0
	 */
	public UdcThreadMsgDtrEventTimeout(Date fixed)
	{
		timerType = UdcThreadMsgDtrEventTimeout.FixedTime;
		fixedTime = fixed;
		waitTime = -1;
	}

	/**
	 * タイマ識別子を取得する。
	 *
	 * @return 	タイマ識別子
	 * @since	UDC1.0
	 */
	public int get_timerIndex() { return timerIndex; }

	/**
	 * タイマ種別を取得する。
	 *
	 * @return 	タイマ種別
	 * @since	UDC1.0
	 */
	public int get_timerType() { return timerType; }

	/**
	 * 起床までの時間を取得する。
	 *
	 * @return 	起床までの時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public long get_waitTime() { return waitTime; }

	/**
	 * 起床日時を取得する。
	 *
	 * @return 	起床日時
	 * @since	UDC1.0
	 */
	public Date get_fixedTime() { return fixedTime; }

	/**
	 * 本インスタンスの複製を作成します。
	 *
	 * @return 	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcThreadMsgDtrEventTimeout obj;
		if (timerType == UdcThreadMsgDtrEventTimeout.FixedTime) {
			obj = new UdcThreadMsgDtrEventTimeout(fixedTime);
		} else {
			obj = new UdcThreadMsgDtrEventTimeout(timerType,waitTime);
		}
		obj.set_timerIndex(timerIndex);
		return obj;
	}

	/**
	 * タイマ識別子を設定する。
	 *
	 * @param	index タイマ識別子
	 * @since	UDC1.0
	 */
	protected void set_timerIndex(int index)
	{
		timerIndex = index;
	}

	/**
	 * タイマ識別子を確保・設定する。
	 *
	 * @since	UDC1.0
	 */
	protected int set_timerIndex()
	{
		try {
			timerIndex = allocateTimerIndex();
		} catch (InterruptedException exp) {
			return -1;
		}
		return 0;
	}

	/**
	 * タイマ識別子を確保する。
	 *
	 * @return 	確保したタイマ識別子
	 * @since	UDC1.0
	 */
	protected int allocateTimerIndex() throws InterruptedException
	{
		if (gen == null) {
			gen = new UdcGenerateIndex();
		}
		return gen.allocateIndex();
	}

}

