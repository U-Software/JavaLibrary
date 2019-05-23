/* *********************************************************************
 * @(#)UdcThreadOperationPool.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.io.*;


/**
 * オペレーションスレッドの管理クラス。
 * UdcThreadOperationBaseオペレーションスレッドクラスの稼動状態を管理し、
 * 空きスレッド等の管理を行う。本クラスでは、処理する度にスレッドを生成
 * する処理負荷を軽減すると共に、スレッドリソースの上限を制限し、システム
 * 全体でのリソース制限を同時に実現します。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadOperationBase
 * @since	UDC1.0
 */
public class UdcThreadOperationPool implements UdcThreadMsgId
{
	/**
	 * タイマ管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcTimerManager	timerManager;

	/**
	 * スレッドプール数
	 * @since	UDC1.0
	 */
	protected int				threadNum = 0;

	/**
	 * 本クラスが管理するスレッド管理クラス全ての稼動状態の論理和。
	 * 本クラスをインスタンス化し、直後 isStartComplete メンバ関数で管理スレッド
	 * が全て起動されたか否かに使用する。
	 * @since	UDC1.0
	 */
	protected int				status = 0;

	/**
	 * 管理スレッド確保時に、全ての管理スレッドが使用中である場合、一定時間(ユーザ指定)
	 * 待ち状態にに遷移する。本スレッドチャネルは、この待ち状態時に、いづれかのスレッド解放
	 * あるいはタイムアウトの待合せを行うもの。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel	channel = new UdcThreadChannel();

	/**
	 * 未使用状態の管理スレッドリスト。
	 * @since	UDC1.0
	 */
	protected LinkedList		emptyList = new LinkedList();

	/**
	 * 使用状態の管理スレッドリスト。
	 * @since	UDC1.0
	 */
	protected LinkedList		usedList = new LinkedList();

	/**
	 * 使用状態から未使用状態への状態遷移中の管理スレッドリスト。
	 * @since	UDC1.0
	 */
	protected LinkedList		usedToEmptyList = new LinkedList();

	/**
	 * 管理スレッド確保時に、全ての管理スレッドが使用中である場合の確保待ちリスト。
	 * @since	UDC1.0
	 */
	protected LinkedList		waitList = new LinkedList();


	/**
	 * コンストラクタ
	 *
	 * @param	poolnum	スレッドプール数
	 * @param	thread	スレッド管理クラス
	 * @param	timer	タイマ管理クラス
	 * @since	UDC1.0
	 */
	public UdcThreadOperationPool(int poolnum, UdcThreadOperationBase thread, UdcTimerManager timer)
				throws CloneNotSupportedException
	{
		threadNum = poolnum;
		timerManager = timer;
		for (int i=0; i<threadNum; i++) {
			UdcThreadOperationBase th = (UdcThreadOperationBase)thread.clone();
			if (th == null) {
				String str = "Not definition clone method.";
				throw (new CloneNotSupportedException(str) );
			}
			th.setPool(this);
			th.threadIndex = i + 1;
			emptyList.add( th );
		}
	}

	/**
	 * スレッド数を取得する。
	 *
	 * @return スレッド数
	 * @since   UDC1.0
	 */
	public int getSize()
	{
		synchronized (emptyList) {
			return emptyList.size() + usedList.size() + usedToEmptyList.size();
		}
	}

	/**
	 * 未使用状態のスレッド数を取得する。
	 *
	 * @return 未使用状態のスレッド数
	 * @since   UDC1.0
	 */
	public int getEmptySize()
	{
		synchronized (emptyList) {
			return emptyList.size();
		}
	}

	/**
	 * 使用中状態のスレッド数を取得する。
	 *
	 * @return 使用中状態のスレッド数
	 * @since   UDC1.0
	 */
	public int getUsedSize()
	{
		synchronized (emptyList) {
			return usedList.size();
		}
	}

	/**
	 * 使用中状態から未使用状態に遷移中のスレッド数を取得する。
	 *
	 * @return 使用中状態から空き状態に遷移中のスレッド数
	 * @since   UDC1.0
	 */
	public int getUsedToEmptySize()
	{
		synchronized (emptyList) {
			return usedToEmptyList.size();
		}
	}

	/**
	 * 本インスタンスが管理する全てのスレッド管理インスタンスの稼動状態の論理和
	 * を取得する。
	 *
	 * @see		UdcThreadOperationBase#isStartComplete()
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		if (status != 1) {
			return false;
		}
		UdcThreadOperationBase th;
		for (int i=0; i<emptyList.size(); i++) {
			th = (UdcThreadOperationBase)emptyList.get(i);
			if (!th.isStartComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 本インスタンスが管理する全てのスレッド管理インスタンスを全て開始する。
	 * スレッド管理インスタンス中で１つでも開始失敗したら、その時点で処理を終了
	 * する。
	 *
	 * @return	管理スレッドが起動されたか否か(負値：異常)
	 * @see		UdcThreadOperationBase#startOperation()
	 * @since	UDC1.0
	 */
	public int startOperation()
	{
		synchronized (emptyList) {
			if (status != 0) {
				return -1;
			}
			status = 1;
			UdcThreadOperationBase th;
			for (int i=0; i<emptyList.size(); i++) {
				th = (UdcThreadOperationBase)emptyList.get(i);
				if (th.startOperation() < 0) {
					return -1;
				}
			}
			UdcMutex mute = new UdcMutex();
			while (! isStartComplete()) {
				try { mute.sleep(30); } catch(InterruptedException exp) {}
			}
		}
		return 0;
	}

	/**
	 * 本インスタンスが管理する全てのスレッド管理インスタンスを全て停止する。
	 * スレッド管理インスタンス中で１つでも停止失敗したら、その時点で処理を終了
	 * する。
	 *
	 * @return	管理スレッドが停止されたか否か(負値：異常)
	 * @see		UdcThreadOperationBase#stopOperation()
	 * @since	UDC1.0
	 */
	public int stopOperation()
	{
		int ret = 0;
		UdcThreadOperationBase th;

		synchronized (emptyList) {
			if (status != 0) {
				return -1;
			}
			for (int i=0; i<usedToEmptyList.size(); i++) {
				th = (UdcThreadOperationBase)usedToEmptyList.get(i);
				if (th.stopOperation() < 0) {
					ret = -1;
				}
			}
			for (int i=0; i<usedList.size(); i++) {
				th = (UdcThreadOperationBase)usedList.get(i);
				if (th.stopOperation() < 0) {
					ret = -1;
				}
			}
			for (int i=0; i<emptyList.size(); i++) {
				th = (UdcThreadOperationBase)emptyList.get(i);
				if (th.stopOperation() < 0) {
					ret = -1;
				}
			}
			status = -1;
		}
		return ret;
	}

	/**
	 * 空きスレッド管理を１つ確保する。
	 * もし、空きスレッド管理が１つもない場合、waittimeで指定した時間を上限として
	 * 空スレッド確保待ちとし、この間にいずれかのスレッド管理が解放されればそれを
	 * 割り当てる。しかし、指定時間内に１つもスレッド管理が解放されなければ、確保
	 * 失敗とする。
	 *
	 * @return	確保スレッド管理インスタンス(確保失敗時：null)
	 * @param	waittime	空スレッド無し時の確保待ち時間(単位：ミリ秒)
	 * @see		UdcThreadOperationBase#startTransaction()
	 * @since	UDC1.0
	 */
	public UdcThreadOperationBase allocateOperationThread(long waittime)
	{
		if (status != 1) {
			return null;
		}

		/* 空きスレッドリストから使用中スレッドリストにスレッドを移す */
		UdcThreadOperationBase thread;
		thread = allocate();
		/* 空きスレッドがない場合には、確保待ちに遷移する */
		if (thread == null) {
			if (waittime <= 0) {
				return null;
			}
			int index = -1;
			UdcAllocateWait waitElem = newAllocateWait(waittime);
			waitElem.waitToAllocate();
			deleteAllocateWait(waitElem.timerIndex);
			if (waitElem.status != UdcAllocateWait.Enable) {
				return null;
			}
			if ((thread=allocate()) == null) {
				return null;
			}
		}
			/* トランザクション開始 */
		int ret = thread.startTransaction();
				/* トランザクションを開始できなければ、このスレッドは強制的に削除し、新規にスレッドを補完しておく */
		if (ret != 0) {
			releaseOperationThread(thread.threadIndex);
			return null;
		}
		return thread;
	}

	/**
	 * 使用中スレッドを解放する。
	 * もし、allocateOperationThreadメンバ関数で確保待ち状態のものがいれば、解放と同時に
	 * これを通知する。
	 *
	 * @param	index	使用中スレッド管理インスタンスの識別子
	 * @see		UdcThreadOperationBase#stopTransaction()
	 * @since	UDC1.0
	 */
	public void releaseOperationThread(int index)
	{
			/* 使用中スレッドリストから空きスレッドリスト移行リストにスレッドを移す */
		UdcThreadOperationBase thread = prerelease(index);
		if (thread == null) {
			return;
		}
			/* トランザクション停止 */
		int ret = thread.stopTransaction();
				/* トランザクションを停止できなければ、このスレッドは強制的に削除し、新規にスレッドを補完しておく */
		if (ret != 0) {
			UdcTrace.trace(UdcTrace.Level,"UdcThreadOperationBase.releaseOperationThread", " - Cannot stop transaction, thIndex[" + index + "]");
			UdcThreadOperationBase th = null;
			try {
				th = (UdcThreadOperationBase)thread.clone();
				if (th != null) {
					if (th.startOperation() == 0) {
						th.setPool(this);
						UdcMutex mute = new UdcMutex();
						while (! th.isStartComplete()) {
							try { mute.sleep(20); } catch(InterruptedException exp) {}
						}
					} else {
						UdcTrace.trace(UdcTrace.Level,"UdcThreadOperationBase.releaseOperationThread", " - Cannot recover thread.");
						th = null;
					}
				}
			} catch (CloneNotSupportedException exp) {}	/* ここでのれ以外はありえない */
			release(th,thread);
			return;
		}
			/* 空きスレッドリスト移行リストから空きスレッドリストにスレッドを移す */
		release(thread);
		return;
	}

	/**
	 * 空きスレッド管理を１つ確保する。
	 *
	 * @return	確保スレッド管理インスタンス(確保失敗時：null)
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationBase allocate()
	{
		synchronized (emptyList) {
			if (emptyList.size() <= 0) {
				return null;
			}
			UdcThreadOperationBase thread = (UdcThreadOperationBase)emptyList.get(0);
			thread.setStatus(UdcThreadOperationBase.Status_Used);
			emptyList.remove(thread);
			usedList.add(thread);
			return thread;
		}
	}

	/**
	 * 空スレッド管理が存在しないため、waittime時間の間にスレッド管理が
	 * 空くのを待ってスレッド管理を確保する。waittime時間待っても空スレッド
	 * 管理が確保できなれば確保失敗となります。
	 *
	 * @param	waittime	空スレッド無し時の確保待ち時間(単位：ミリ秒)
	 * @return	確保スレッド管理インスタンス(確保失敗時：null)
	 * @since	UDC1.0
	 */
	protected UdcAllocateWait newAllocateWait(long waittime)
	{
		int index = -1;
		UdcAllocateWait waitElem = new UdcAllocateWait();
		try {
			index = timerManager.registrateTimer(UdcThreadMsgDtrEventTimeout.Timeout, waittime, waitElem.channel);
		} catch (InterruptedException exp) {
			return null;
		}
		waitElem.timerIndex = index;
		waitElem.status = 1;
		synchronized (waitList) {
			waitList.add(waitElem);
		}
		return waitElem;
	}

	/**
	 * 空スレッド管理確保待ちタイムアウト設定の解除をする。
	 *
	 * @param	timerIndex	空スレッド管理確保待ちタイマ識別子
	 * @since	UDC1.0
	 */
	protected void deleteAllocateWait(int timerIndex)
	{
		synchronized (waitList) {
			UdcAllocateWait elm = null;
			for (int i=0; i<waitList.size(); i++) {
				elm = (UdcAllocateWait)waitList.get(i);
				if (elm.getTimerIndex() == timerIndex) {
					break;
				}
				elm = null;
			}
			if (elm != null) {
				waitList.remove(elm);
				if (elm.status == UdcAllocateWait.Wait || elm.status == UdcAllocateWait.Enable) {
					try {
						timerManager.unregistrateTimer(timerIndex);
					} catch (InterruptedException exp) {
						return;
					}
					elm.status = UdcAllocateWait.Disable;
				}
			}
		}
	}

	/**
	 * 使用中スレッド管理を使用状態から未使用状態への状態遷移中に状態遷移させる。
	 *
	 * @return	解放スレッド管理インスタンス
	 * @param	index	使用中スレッド管理インスタンスの識別子
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationBase prerelease(int index)
	{
		synchronized (emptyList) {
			UdcThreadOperationBase thread = getThread(usedList, index);
			if (thread == null) {
				thread = getThread(usedToEmptyList, index);
				if (thread != null) {
					thread.setStatus(UdcThreadOperationBase.Status_UsedToEmpty);
					return thread;
				}
			}

			thread.setStatus(UdcThreadOperationBase.Status_UsedToEmpty);
			usedList.remove(thread);
			usedToEmptyList.add(thread);
			return thread;
		}
	}

	/**
	 * 使用状態から未使用状態への状態遷移中に状態のスレッド管理を正式に解放する。
	 * もしスレッド確保待ち状態のものが存在するならそれを起床する。
	 *
	 * @return	解放スレッド管理インスタンス
	 * @param	thread	解放スレッド管理インスタンス
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationBase release(UdcThreadOperationBase thread)
	{
		synchronized (emptyList) {
			thread.setStatus(UdcThreadOperationBase.Status_Empty);
			usedToEmptyList.remove(thread);
			emptyList.add(thread);
				/* allocate待ちが存在すれば起床 */
			UdcAllocateWait elm;
			synchronized (waitList) {
				for (int i=0; i<waitList.size(); i++) {
					elm = (UdcAllocateWait)waitList.get(i);
					if (elm.status == UdcAllocateWait.Wait) {
						elm.wakeupToAllocate();
						break;
					}
				}
			}
			return thread;
		}
	}

	/**
	 * 使用状態から未使用状態への状態遷移中に状態のスレッド管理を正式に解放する。
	 * 本メンバ関数の場合、解放するスレッドが使用不能状態になっているため、解放スレッドを削除し、
	 * newthreadで指定された新規生成スレッド管理と置き換える。
	 * もしスレッド確保待ち状態のものが存在するならそれを起床する。
	 *
	 * @return	解放スレッド管理インスタンス
	 * @param	newthread	新規追加スレッド管理インスタンス
	 * @param	oldthread 	解放スレッド管理インスタンス
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationBase release(UdcThreadOperationBase newthread,UdcThreadOperationBase oldthread)
	{
		synchronized (emptyList) {
			newthread.setStatus(UdcThreadOperationBase.Status_Empty);
			if (oldthread != null) {
				usedToEmptyList.remove(oldthread);
			}
			emptyList.add(newthread);
				/* allocate待ちが存在すれば起床 */
			synchronized (waitList) {
				UdcAllocateWait elm;
				for (int i=0; i<waitList.size(); i++) {
					elm = (UdcAllocateWait)waitList.get(i);
					if (elm.status == UdcAllocateWait.Wait) {
						elm.wakeupToAllocate();
						break;
					}
				}
			}
		}
		return newthread;
	}

	/**
	 * 本インスタンスの文字列情報を取得する。
	 *
	 * @return	本インスタンス情報
	 * @since	UDC1.0
	 */
	 public String toString()
	 {
		int i;
		UdcThreadOperationBase th;
		String str = "All Thread Number = " + getSize() + "\n";

		str += "  EmptyThread = " + getEmptySize() + "\n";
		for (i=0; i<emptyList.size(); i++) {
			th = (UdcThreadOperationBase)emptyList.get(i);
			str += "    Index[" + th.getThreadIndex() + "]  startComplete:" + th.isStartComplete() + " status:" + th.getStatus() + "\n";
		}

		str += "  UsedThread = " + getUsedSize() + "\n";
		for (i=0; i<usedList.size(); i++) {
			th = (UdcThreadOperationBase)usedList.get(i);
			str += "    Index[" + th.getThreadIndex() + "]  startComplete:" + th.isStartComplete() + " status:" + th.getStatus() + "\n";
		}

		str += "  UsedToEmptyThread = " + getUsedToEmptySize() + "\n";
		for (i=0; i<usedToEmptyList.size(); i++) {
			th = (UdcThreadOperationBase)usedToEmptyList.get(i);
			str += "    Index[" + th.getThreadIndex() + "]  startComplete:" + th.isStartComplete() + " status:" + th.getStatus() + "\n";
		}
		return str;
	 }

	/**
	 * スレッド管理リストから指定された識別子のスレッドを取得する。
	 *
	 * @return	スレッド
	 * @param	thrlist	スレッドリスト
	 * @param	index	スレッド識別子
	 * @since	UDC1.0
	 */
	UdcThreadOperationBase getThread(LinkedList thrlist, int index)
	{
		UdcThreadOperationBase thread = null;
		for (int i=0; i<thrlist.size(); i++) {
			thread = (UdcThreadOperationBase)thrlist.get(i);
			if (thread.getThreadIndex() == index) {
				return thread;
			}
		}
		return null;
	}	
}

/**
 * スレッド管理確保待ち情報クラス。
 * 本クラスは、UdcThreadOperationPool.waitListの線形リスト要素です。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcAllocateWait implements Comparable
{
	/**
	 * スレッド管理確保待ち状態：空状態
	 * @since	UDC1.0
	 */
	final public static int	Empty 	= 0;

	/**
	 * スレッド管理確保待ち状態：スレッド空き・タイムアウト待ち状態
	 * @since	UDC1.0
	 */
	final public static int	Wait 	= 1;

	/**
	 * スレッド管理確保待ち状態：スレッド空き検出状態
	 * @since	UDC1.0
	 */
	final public static int	Enable	= 2;

	/**
	 * スレッド管理確保待ち状態：タイムアウト検出状態
	 * @since	UDC1.0
	 */
	final public static int	Disable	= 3;

	/**
	 * スレッド確保トリガ受信チャネル(タイムアウト時のタイムアウト受信も本チャネルで行う)
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel	channel = new UdcThreadChannel();

	/**
	 * タイマ識別子
	 * @since	UDC1.0
	 */
	protected int	timerIndex;

	/**
	 * スレッド管理確保待ち状態
	 * @since	UDC1.0
	 */
	protected int	status = Empty;

	/**
	 * スレッド確保待ちリストからタイマ識別子で検索するための検索キーを取得する。
	 *
	 * @return 	タイマ識別子
	 * @since	UDC1.0
	 */
	public int getTimerIndex()
	{
		return timerIndex;
	}

	/**
	 * スレッド管理確保を待合せ、確保可能か否かを取得する。
	 *
	 * @return  1:確保可能/0:確保不能/負値:異常
	 * @since	UDC1.0
	 */
	public int waitToAllocate()
	{
		UdcThreadChannelDtr dtr = null;
		try {
			status = UdcAllocateWait.Wait;
			dtr = channel.pull();
		} catch(InterruptedException exp) {
			return -1;
		}
		if (dtr == null) {
			return -1;
		}
		if (dtr.get_dtrType() == UdcThreadMsgId.Event_Timeout) {
			status = UdcAllocateWait.Disable;
			return 0;
		}
		status = UdcAllocateWait.Enable;
		return 1;
	}

	/**
	 * スレッド管理確保待ち状態のものに確保可能通知を送信する。
	 *
	 * @since	UDC1.0
	 */
	public void wakeupToAllocate()
	{
		UdcThreadChannelDtr dtr = new UdcThreadChannelDtr(0,0xFFFFFFFF);
		try {
			channel.push(dtr);
		} catch (InterruptedException exp) {
		}
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
		UdcAllocateWait elm = (UdcAllocateWait)obj;
		
		if (timerIndex > elm.getTimerIndex()) {
			return 1;
		} else if (timerIndex < elm.getTimerIndex()) {
			return -1;
		}
		return 0;
	}



}

