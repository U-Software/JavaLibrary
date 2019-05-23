/* *********************************************************************
 * @(#)UdcThreadOperationBase.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.io.*;


/**
 * UdcThreadOperationPoolクラスで管理されるスレッド管理クラスのベースクラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadOperationPool
 * @since	UDC1.0
 */
public class UdcThreadOperationBase implements UdcThreadMsgId, Cloneable, Comparable
{
	/**
	 * スレッド使用状態　: 未使用状態
	 * @since	UDC1.0
	 */
	final public static int	Status_Empty	= 0;

	/**
	 * スレッド使用状態　: 使用中状態
	 * @since	UDC1.0
	 */
	final public static int	Status_Used		= 1;

	/**
	 * スレッド使用状態　: 使用中状態から未使用状態への遷移過程状態
	 * @since	UDC1.0
	 */
	final public static int	Status_UsedToEmpty	= 2;

	/**
	 * スレッド使用状態
	 * @since	UDC1.0
	 */
	protected int	status = UdcThreadOperationBase.Status_Empty;

	/**
	 * 本インスタンスが管理するスレッドID
	 * @since	UDC1.0
	 */
	protected int 	threadIndex;

	/**
	 * 本インスタンスが管理するスレッドチャネル
	 * 本チャネルの使用はユーザにゆだねられます。UDC内部では使用していません。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel	channel = new UdcThreadChannel();

	/**
	 * 本インスタンスを管理するスレッドプールクラス。
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationPool 	pool = null;


	/**
	 * 本インスタンスを管理するスレッドプールクラスを取得する。
	 *
	 * @return 	本インスタンスを管理するスレッドプールクラス
	 * @since	UDC1.0
	 */
	public UdcThreadOperationPool getPool()
	{
		return pool;
	}

	/**
	 * 本インスタンスを管理するスレッドプールクラスを設定する。
	 *
	 * @param	p	本インスタンスを管理するスレッドプールクラス
	 * @since	UDC1.0
	 */
	public void setPool(UdcThreadOperationPool p)
	{
		pool = p;
	}

	/**
	 * 本インスタンスが管理するスレッドIDを取得する。
	 *
	 * @return  本インスタンスが管理するスレッドID
	 * @since	UDC1.0
	 */
	public int getThreadIndex()
	{
		return threadIndex;
	}

	/**
	 * 本インスタンスが管理するスレッドチャネルを取得する。
	 *
	 * @return  本インスタンスが管理するスレッドチャネル
	 * @since	UDC1.0
	 */
	public UdcThreadChannel getChannel()
	{
		return channel;
	}

	/**
	 * 本インスタンスの使用状態を取得する。
	 *
	 * @return  本インスタンスの使用状態を取得する。
	 * @since	UDC1.0
	 */
	public synchronized int getStatus()
	{
		return status;
	}

	/**
	 * 本インスタンスの使用状態を設定する。
	 *
	 * @param  st	本インスタンスの使用状態
	 * @since	UDC1.0
	 */
	public synchronized void setStatus(int st)
	{
		status = st;
	}

	/**
	 * 本インスタンスが管理するスレッドの稼動状態を取得する。
	 * (ユーザオーバライド・メソッド関数）
	 *	 本メソッドは、UdcThreadOperationPool初期化時に、本インスタンスが使用可能
	 *   状態となった否かの判定に使用しています。
	 *   UdcThreadOperationPool.isStartComplete では、管理するスレッド管理クラス全ての
	 *   本メソッドを呼び出し、その論理和を返却するため、必ずオーバライドする必要があります。
	 *   デフォルトは false になっているので注意して下さい。
	 *
	 * @return  本インスタンスが管理するスレッドの稼動状態(true:稼動中/false:初期化中等）
	 * @see		UdcThreadOperationPool#isStartComplete()
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return false;
	}

	/**
	 * 本インスタンスが管理するスレッドを開始する。
	 * (ユーザオーバライド・メソッド関数）
	 *   本メソッドは、UdcThreadOperationPool.startOperationメンバ関数で管理する全ての
	 *   スレッド管理クラスの起動に使用しています。
	 *   本メンバメソッド内で、thread.start() を使用するのが一般的な使用方法です。
	 *   通常Thread.start/stopは一回しかコールできないため、ユーザにおいて別管理を行う
	 *   ための余地を残すためにこのように枠組みを具備しています。
	 *
	 * @return	本クラスが管理するスレッドを起動したか否か(0:正常／負値：異常)
	 * @see		UdcThreadOperationPool#startOperation()
	 * @since	UDC1.0
	 */
	public int startOperation()
	{
		return 0;
	}

	/**
	 * 本インスタンスが管理するスレッドを停止する。
	 * (ユーザオーバライド・メソッド関数）
	 *   本メソッドは、UdcThreadOperationPool.stopOperationメンバ関数で管理する全ての
	 *   スレッド管理クラスの停止に使用しています。
	 *   本メンバメソッド内で、thread.start() を使用するのが一般的な使用方法です。
	 *   通常Thread.start/stopは一回しかコールできないため、ユーザにおいて別管理を行う
	 *   ための余地を残すためにこのように枠組みを具備しています。
	 *
	 * @return	本クラスが管理するスレッドを停止したか否か(0:正常／負値：異常)
	 * @see		UdcThreadOperationPool#stopOperation()
	 * @since	UDC1.0
	 */
	public int stopOperation()
	{
		return 0;
	}

	/**
	 * 本インスタンスの複製を生成する。
	 * (ユーザオーバライド・メソッド関数）
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		return null;
	}

	/**
	 * Comparable.compareToの実装。
	 *
	 * @return	このオブジェクトが指定されたオブジェクトより小さい場合は負の整数、等しい場合はゼロ、大きい場合は正の整数
	 * @param	obj	比較対象オブジェクト
	 * @since	UDC1.0
	 */
	public int compareTo(Object obj)
	{
		UdcThreadOperationBase elm = (UdcThreadOperationBase)obj;
		
		if (threadIndex > elm.getThreadIndex()) {
			return 1;
		} else if (threadIndex < elm.getThreadIndex()) {
			return -1;
		}
		return 0;
	}

	/**
	 * UdcThreadOperationPool.allocateOperationThreadによって本インスタンス
	 * 確保時にコールされる。
	 * (ユーザオーバライド・メソッド関数）
	 *
	 * @return	0:正常／0以外:異常
	 * @since	UDC1.0
	 */
	public int startTransaction()
	{
		return 0;
	}

	/**
	 * UdcThreadOperationPool.releaseOperationThreadによって本インスタンス
	 * 解放時にコールされる。
	 * (ユーザオーバライド・メソッド関数）
	 *
	 * @return	0:正常／0以外:異常
	 * @since	UDC1.0
	 */
	public int stopTransaction()
	{
		return 0;
	}
}

