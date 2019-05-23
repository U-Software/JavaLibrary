/* *********************************************************************
 * @(#)UdcThreadChannel.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;

/**
 * スレッド間通信クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadChannelDtr
 * @since	UDC1.0
 */
public class UdcThreadChannel
{
	/**
	 * スレッドチャネル名
	 * @since	UDC1.0
	 */
	private String					channelName;

	/**
	 * 自スレッドチャネルで管理するインボークID管理
	 * @since	UDC1.0
	 */
	private UdcGenerateIndex		useInvokeId = new UdcGenerateIndex();

	/**
	 * 受信メッセージリスト
	 * @since	UDC1.0
	 */
	private UdcThreadChannelDtrList	msgList = new UdcThreadChannelDtrList();

	/**
	 * 受信待ちリスト
	 * @since	UDC1.0
	 */
	private UdcThreadChannelDtrList	waitList = new UdcThreadChannelDtrList();

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcThreadChannel()
	{
		super();
		channelName = "UnNamedChannel";
	}

	/**
	 * コンストラクタ
	 *
	 * @param	name	スレッドチャネル名
	 * @since	UDC1.0
	 */
	public UdcThreadChannel(String name)
	{
		super();
		channelName = name;
	}

	/**
	 * スレッドチャネル名を取得する。
	 *
	 * @return	スレッドチャネル名
	 * @since	UDC1.0
	 */
	public String getChannelName() { return channelName; }

	/**
	 * 新たにインボークIDを確保する。
	 *
	 * @return	新たなインボークID
	 * @since	UDC1.0
	 */
	public int newInvokeId()
		throws InterruptedException
	{
		return useInvokeId.allocateIndex();
	}

	/**
	 * 受信メッセージが存在するかを取得する。
	 *
	 * @return 	真/偽　(真：受信メッセージがない場合)
	 * @since	UDC1.0
	 */
	public boolean isEmpty()
		throws InterruptedException
	{
		int cnt = 0;
		msgList.lock();
		cnt = msgList.size();
		msgList.unlock();
		return (cnt <= 0);
	}

	/**
	 * 応答メッセージ待ちが存在するかを取得する。
	 *
	 * @return 	真/偽　(真：応答メッセージ待ちが存在しない場合)
	 * @since	UDC1.0
	 */
	public boolean isEmptyWaitList()
		throws InterruptedException
	{
		int cnt = 0;
		waitList.lock();
		cnt = waitList.size();
		waitList.unlock();
		return (cnt <= 0);
	}

	/**
	 * 指定した応答メッセージ待ちが存在するかを取得する。
	 *
	 * @return 	真/偽　(真：応答メッセージ待ちが存在しない場合)
	 * @param	invId	応答待ちインボークID
	 * @param 	dtrType	応答待ちメッセージ種別
	 * @since	UDC1.0
	 */
	public  boolean isEmptyWaitList(int invId,int dtrType)
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		boolean bool = true;
		waitList.lock();
		if ((elm=waitList.searchReplyWait(invId,dtrType)) != null) {
			bool = false;
		}
		waitList.unlock();
		return bool;
	}

	/**
	 * 受信メッセージリスト／応答待ちリストをクリアする。
	 *
	 * @since	UDC1.0
	 */
	public void reset()
		throws InterruptedException
	{
		msgList.lock();
		waitList.lock();
		msgList.clear();
		waitList.clear();
		waitList.unlock();
		msgList.unlock();
	}

	/**
	 * メッセージを送信する。
	 *
	 * @param 	data	送信メッセージ
	 * @since	UDC1.0
	 */
	public void push(UdcThreadChannelDtr data)
		throws InterruptedException
	{
		msgList.lock();
		msgList.addPriority(data);
		if (data.get_replyDtrType() >= 0) {
			waitList.lock();
			try {
				waitList.add((UdcThreadChannelDtr)data.clone());
			} catch(java.lang.CloneNotSupportedException ex) {}
			waitList.unlock();
		}
		msgList.unlock();
		msgList.cond_signal();
	}

	/**
	 * 受信メッセージを参照します。
	 * 本メンバは受信メッセージを受信することなく、参照したい場合に使用します。
	 * (注意)
	 *    本メンバによって参照したメッセージが必ずしもtry_pull/pullによって取得
	 * 	  される訳ではありません。これはpushでUdcThreadChannelDtrの優先度による
	 *	  追い越しがあるためです。本メンバによって参照したメッセージを受信したい
	 *    場合は以下のようにします。
	 *       UdcThreadChannelDtr ref = channel.tryReference();
	 *               :
	 *            ユーザ処理 (ここで他スレッドによって優先度の高いメッセージが送信される可能性がある)
	 *               :
	 *       UdcThreadChannelDtr dtr = channel.tryPull(ref.get_invokeId(), ref.get_dtrType());
	 *
	 * @return	受信メッセージ
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr tryReference()
		throws InterruptedException
	{
		return (UdcThreadChannelDtr)msgList.getFirst();
	}

	/**
	 * 受信メッセージを取得する。受信メッセージは受信順に取得されます。
	 * 本メンバ関数では、受信待ちに遷移することなく、受信メッセージを取得するのみです。
	 *
	 * @return	受信メッセージ
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr tryPull()
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		if ((elm=(UdcThreadChannelDtr)msgList.getFirst()) != null) {
			msgList.remove(elm);
			waitList.lock();
			waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
			waitList.unlock();
		}
		msgList.unlock();
		return elm;
	}

	/**
	 * 指定した受信メッセージを取得する。
	 * 本メンバ関数では、受信待ちに遷移することなく、受信メッセージを取得するのみです。
	 *
	 * @return	受信メッセージ
	 * @param	invId	応答待ちインボークID
	 * @param 	dtrtype	応答待ちメッセージ種別
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr tryPull(int invId,int dtrtype)
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		if ((elm=msgList.search(invId,dtrtype)) != null) {
			msgList.remove(elm);
			waitList.lock();
			waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
			waitList.unlock();
		}
		msgList.unlock();
		return elm;
	}

	/**
	 * 受信メッセージを取得する。
	 * 本メンバ関数では、メッセージを受信していない場合、受信するまで待ち状態となります。
	 *
	 * @return	受信メッセージ
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr pull()
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		while (true) {
			if ((elm=(UdcThreadChannelDtr)msgList.getFirst()) != null) {
				msgList.remove(elm);
				waitList.lock();
				waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
				waitList.unlock();
				break;
			}
			msgList.cond_wait();
		}
		msgList.unlock();
		return elm;
	}

	/**
	 * 指定した受信メッセージを取得する。
	 * 本メンバ関数では、メッセージを受信していない場合、受信するまで待ち状態となります。
	 *
	 * @return	受信メッセージ
	 * @param	invId	応答待ちインボークID
	 * @param 	dtrtype	応答待ちメッセージ種別
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr pull(int invId,int dtrtype)
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		while (true) {
			if ((elm=msgList.search(invId,dtrtype)) != null) {
				msgList.remove(elm);
				waitList.lock();
				waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
				waitList.unlock();
				break;
			}
			msgList.cond_wait();
		}
		msgList.unlock();
		return elm;
	}

	/**
	 * 受信メッセージを取得する。
	 * 本メンバ関数では、メッセージを受信していない場合、waittimeで指定された間を上限とし、
	 * 受信するまで待ち状態となります。
	 *
	 * @return	受信メッセージ
	 * @param 	waittime	待ち時間上限(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr pull(long waittime)
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		if ((elm=(UdcThreadChannelDtr)msgList.getFirst()) != null) {
			msgList.remove(elm);
			waitList.lock();
			waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
			waitList.unlock();
			msgList.unlock();
			return elm;
		}
		msgList.cond_timewait(waittime);
		if ((elm=(UdcThreadChannelDtr)msgList.getFirst()) != null) {
			msgList.remove(elm);
			waitList.lock();
			waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
			waitList.unlock();
		}
		msgList.unlock();
		return elm;
	}

	/**
	 * 指定した受信メッセージを取得する。
	 * 本メンバ関数では、メッセージを受信していない場合、waittimeで指定された間を上限とし、
	 * 受信するまで待ち状態となります。
	 *
	 * @return	受信メッセージ
	 * @param	invId		応答待ちインボークID
	 * @param 	dtrtype		応答待ちメッセージ種別
	 * @param 	waittime	待ち時間上限(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr pull(int invId,int dtrtype,long waittime)
		throws InterruptedException
	{
		UdcThreadChannelDtr elm = null;
		msgList.lock();
		if ((elm=msgList.search(invId,dtrtype)) != null) {
			msgList.remove(elm);
			waitList.lock();
			waitList.removeDeleteReplyWait(elm.get_invokeId(),elm.get_dtrType());
			waitList.unlock();
			msgList.unlock();
			return elm;
		}
		long cur = System.currentTimeMillis();
		long wakeup = cur + waittime;
		long old = cur;
		long remain = waittime;
		while (true) {
			old = cur;
			msgList.cond_timewait(waittime);
			if ((elm=msgList.search(invId,dtrtype)) != null) {
				msgList.remove(elm);
				break;
			}
			cur = System.currentTimeMillis();
			if (cur >= wakeup || cur < old || cur > (old+waittime)) {
				break;
			}
			remain = wakeup - cur;
		}
		waitList.lock();
		waitList.removeDeleteReplyWait(invId,dtrtype);
		waitList.unlock();
		msgList.unlock();
		return elm;
	}
}


/**
 * スレッド間通信メッセージリスト
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcThreadChannelDtrList extends UdcMutex
{
	/**
	 * スレッド間通信メッセージリスト
	 * @since	UDC1.0
	 */
	LinkedList dtrList;

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtrList()
	{
		dtrList = new LinkedList();
	}

	/**
	 * メッセージリストの要素数を取得する。
	 *
	 * @return 	メッセージリストの要素数
	 * @since	UDC1.0
	 */
	public int size()
	{
		return dtrList.size();
	}

	/**
	 * メッセージリストの先頭メッセージを検索する。
	 *
	 * @return 	検索されたメッセージ
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr getFirst()
	{
		if (dtrList.size() <= 0) {
			return null;
		}
		return (UdcThreadChannelDtr)dtrList.getFirst();
	}

	/**
	 * メッセージリストをクリアする。
	 *
	 * @return 	検索されたメッセージ
	 * @since	UDC1.0
	 */
	public void clear()
	{
		dtrList.clear();
	}

	/**
	 * 指定されたインボークIDのメッセージを検索する。
	 *
	 * @return 	検索されたメッセージ
	 * @param	invId	インボークID(検索条件)
	 * @since	UDC1.0
	 */
	public UdcThreadChannelDtr search(int invId)
	{
		UdcThreadChannelDtr elm;
		for (int i=0; i<dtrList.size(); i++) {
			elm = (UdcThreadChannelDtr)dtrList.get(i);
			if (elm.get_invokeId() == invId) {
				return elm;
			}
		}	
		return null;
	}

	/**
	 * 指定されたインボークID・メッセージ種別のメッセージを検索する。
	 *
	 * @return 	検索されたメッセージ
	 * @param	invId	インボークID(検索条件)
	 * @param	dtrtype	メッセージ種別(検索条件)
	 * @since	UDC1.0
	 */
	public  UdcThreadChannelDtr search(int invId,int dtrtype)
	{
		UdcThreadChannelDtr elm;
		for (int i=0; i<dtrList.size(); i++) {
			elm = (UdcThreadChannelDtr)dtrList.get(i);
			if (elm.get_invokeId() == invId && elm.get_dtrType() == dtrtype) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * 指定された応答待ちの要求メッセージを検索する。
	 *
	 * @return 	検索された応答待ちの要求メッセージ
	 * @param	invId		インボークID(検索条件)
	 * @param	repdtrtype	メッセージ種別(検索条件)
	 * @since	UDC1.0
	 */
	public  UdcThreadChannelDtr searchReplyWait(int invId,int repdtrtype)
	{
		UdcThreadChannelDtr elm;
		for (int i=0; i<dtrList.size(); i++) {
			elm = (UdcThreadChannelDtr)dtrList.get(i);
			if (elm.get_invokeId() == invId && elm.get_replyDtrType() == repdtrtype) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * 要求されたメッセージを受信メッセージリストに追加する。
	 *
	 * @param	dtr	送信メッセージ
	 * @since	UDC1.0
	 */
	public void add(UdcThreadChannelDtr dtr)
	{
		dtrList.add(dtr);
	}

	/**
	 * 要求されたメッセージを受信メッセージリストに追加する。
	 * 受信メッセージリストに追加する際には、優先度の高いものをリストの
	 * 先頭からリストする。
	 *
	 * @param	dtr 送信メッセージ
	 * @since	UDC1.0
	 */
	public void addPriority(UdcThreadChannelDtr dtr)
	{
		UdcThreadChannelDtr elm;
		for (int i=0; i<dtrList.size(); i++) {
			elm = (UdcThreadChannelDtr)dtrList.get(i);
			if (dtr.get_dtrLevel() > elm.get_dtrLevel()) {
				dtrList.add(i,dtr);
				return;
			}
		}
		dtrList.add(dtr);
	}

	/**
	 * 指定されたメッセージを受信メッセージリストから削除する。
	 *
	 * @since	UDC1.0
	 */
	public void remove(UdcThreadChannelDtr dtr)
	{
		dtrList.remove(dtr);
	}

	/**
	 * 指定されたメッセージを受信メッセージリストから削除する。
	 *
	 * @param	invId		インボークID(検索条件)
	 * @param	repdtrtype	メッセージ種別(検索条件)
	 * @since	UDC1.0
	 */
	public void removeDeleteReplyWait(int invId,int repdtrtype)
	{
		UdcThreadChannelDtr waitElem = searchReplyWait(invId,repdtrtype);
		if (waitElem != null) {
			dtrList.remove(waitElem);
		}
	}
}

