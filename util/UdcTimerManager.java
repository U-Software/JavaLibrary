/* *********************************************************************
 * @(#)UdcTimerManager.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.io.*;
import java.lang.*;
import java.util.*;


/**
 * タイマ管理クラス。本クラスではスレッドを１つ有し、登録されたタイマが
 * タイムアウトした場合、指定のチャネルにタイマイベントを通知する。
 * JAVA標準機能にもタイマー機能はあるが、本機能はスレッドチャネルイベントによって共通に処理することを目的として実装している。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcThreadMsgDtrEventTimeout
 * @see		UdcThreadChannel
 * @since	UDC1.0
 */
public class UdcTimerManager implements UdcThreadMsgId , Runnable
{
	/**
	 * タイマ生成ベース周期の下限値(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	static protected long BaseWaitTime = 1000;

	/**
	 * OSシステムコマンドによる時刻変更時の誤差(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	static protected long DifferenceForUpdateDate = 2000;

	/**
	 * タイマ登録内部スレッド間メッセージ種別
	 * @since	UDC1.0
	 */
	static protected int  Request_RegTimer	= 1;

	/**
	 * タイマ生成ベース周期(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	long 				baseWaitTime = UdcTimerManager.BaseWaitTime;

	/**
	 * タイマ処理を行うスレッド。
	 * @since	UDC1.0
	 */
	protected Thread	thread;

	/**
	 * タイマ処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	protected boolean	threadLoop = true;

	/**
	 * startメソッドによってタイマ処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	protected boolean 	isStartComplete = false;

	/**
	 * タイムアウト待ちリスト。
	 * 本リストは、タイムアウト時間が早いもの順に先頭からリスト管理されます。
	 * @since	UDC1.0
	 */
	UdcTimerManagedList waitList = new UdcTimerManagedList();

	/**
	 * 内部処理スレッドチャネル
	 * @since	UDC1.0
	 */
	UdcThreadChannel	mychannel = new UdcThreadChannel("UdcTimerManager");

	/**
	 * コンストラクタ
	 *
	 * @param	basetime	タイマ生成ベース周期(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public UdcTimerManager(long basetime)
	{
		if (basetime >= UdcTimerManager.BaseWaitTime) {
			baseWaitTime = basetime;
		}
		waitList.setTimerManager(this);
	}

	/**
	 * startメソッドによってタイマ処理スレッドの起動が完了したか否か
	 * を取得する。
	 *
	 * @return	startメソッドによって起動が完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * タイマ処理機能を活性化(起動)する。
	 * タイマ機能は専用スレッドにおいて実現されるため、そのスレッドの起動を行う。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException
	{
		if (thread == null) {
			UdcMutex mute = new UdcMutex();
			thread = new Thread(this);
			thread.start();
			while (! isStartComplete()) {
				try { mute.sleep(20); } catch(InterruptedException exp) {}
			}
		}
	}

	/**
	 * タイマ処理機能を停止する。
	 * タイマ機能は専用スレッドにおいて実現されるため、そのスレッドの停止を行う。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		threadLoop = false;
		if (thread.isAlive()) {
			thread.stop();
		}
	}

	/**
	 * タイマ登録を行う。
	 * タイマ登録が正常に実施されるとユニークなタイマ識別子が返却されます。
	 *
	 * @return 	登録したタイマ識別子(異常時は負値)
	 * @param	type	タイマ種別(UdcThreadMsgDtrEventTimeout.Timeout/TimeInterval)
	 * @param	milisec	タイムアウト時間
	 * @param	channel	タイムアウト時の通知先スレッドチャネル
	 * @since	UDC1.0
	 */
	public int registrateTimer(int type, long milisec, UdcThreadChannel channel)
		throws InterruptedException
	{
		int timerIndex = -1;

		waitList.lock();
		UdcTimerManagedElem elm = waitList.addTimerElem(type, milisec, channel);
		timerIndex = elm.getTimerIndex();
		waitList.unlock();

		return timerIndex;
	}

	/**
	 * タイマ登録を行う。
	 * タイマ登録が正常に実施されるとユニークなタイマ識別子が返却されます。
	 *
	 * @return 	登録したタイマ識別子(異常時は負値)
	 * @param	fixedTime	起床日時
	 * @param	channel	タイムアウト時の通知先スレッドチャネル
	 * @since	UDC1.0
	 */
	public int registrateTimer(Date fixedTime, UdcThreadChannel channel)
		throws InterruptedException
	{
		int timerIndex = -1;

		waitList.lock();
		UdcTimerManagedElem elm = waitList.addTimerElem(fixedTime, channel);
		timerIndex = elm.getTimerIndex();
		waitList.unlock();

		return timerIndex;
	}

	/**
	 * タイマ登録の解除を行う。
	 * 登録されいないタイマ識別子を指定した場合、何も行われません。
	 *
	 * @param 	timerIndex	登録したタイマ識別子
	 * @since	UDC1.0
	 */
	public void unregistrateTimer(int timerIndex)
		throws InterruptedException
	{
		waitList.lock();
		waitList.removeTimerElem(timerIndex);
		waitList.unlock();
	}

	/**
	 * タイマ周期スレッドで就寝していた時間を算出します。
	 * 以下の場合、システムコマンド等によって時刻変更されたと見なします。この場合
	 * 就寝時間は無条件に waittime 値となります。
	 *	・(就寝後時刻ー就寝前時刻) < 0
	 *	・(就寝後時刻ー就寝前時刻) > (就寝予定時刻+OSシステムコマンドによる時刻変更時の誤差)
	 *
	 * @return	タイマ周期スレッドで就寝していた時間(単位：ミリ秒)
	 * @param	waittime	就寝予定時間(単位：ミリ秒)
	 * @param	beforetime	就寝前時刻
	 * @param	aftertime	起床後時刻
	 * @param	dtr			タイマ周期スレッド要求メッセージ(要求がない場合はnull)
	 * @since	UDC1.0
	 */
	protected long getPassedTime(long waittime, long beforetime, long aftertime, UdcThreadChannelDtr dtr)
	{
		/*
		 * 時刻変更の可能性はあるが、これを考慮し、就寝ベース時間機能を導入しているため
		 * 新たな登録要求によって起動されていない場合は原則として waittime を就寝時間とする。
		 */

		long sleeptime = aftertime - beforetime;
			/* 時刻変更され、就寝時間が不明となっていることを考慮し、無条件に指定時間を就寝時間とする */
		if (sleeptime < 0) {
			sleeptime = waittime;
		}

			/* 新たな要求があった場合 */
				/* 規定の就寝時間を就寝していないため、最大でも就寝時間分しか就寝していないはず。 */
		if (dtr != null) {
			if (sleeptime > waittime) {
				sleeptime = waittime;
			}
			return sleeptime;
		}

			/* 新たな要求がない場合 */
				/*
				 * 処理遅延で遅れることは十分あるが、時刻変更も考慮する必要があるため
				 * 大きく時間差がある場合のみ時刻変更として扱う
				 *		大きな時間差 = DifferenceForUpdateDateミリ秒
				 * とする。
				 */
		if (sleeptime > (waittime + UdcTimerManager.DifferenceForUpdateDate) ) {
			sleeptime = waittime;
		}
		return sleeptime;
	}

	/**
	 * タイムアウト待ちリスト要素
	 *
	 * @param 	passed	就寝時間
	 * @since	UDC1.0
	 */
	protected void updateWaitList(long passed, long currentTime)
	{
		UdcTimerManagedElem elm;
		boolean sortFlag = false;
		long svtime = -1000000000;
		for (int i=0; i<waitList.size(); i++) {
			elm = (UdcTimerManagedElem)waitList.get(i);
				/* タイマ要求された直後の要素はタイマ処理の対象外 */
			if (elm.init == true) {
				elm.init = false;
				continue;
			}
				/* 経過時間を更新 */
			if (elm.getTimerType() == UdcThreadMsgDtrEventTimeout.FixedTime) {
				long ft = elm.elem.fixedTime.getTime();
				elm.remainTime = ft - currentTime;
			} else {
				elm.remainTime -= passed;
			}
			if (svtime > elm.remainTime) {
				sortFlag = true;
			}
			svtime = elm.remainTime;
		}
			// 起床順に最整列
		if (sortFlag) {
			waitList.sortRemainTime();
		}
	}

	/**
	 * タイムアウト待ちリスト中でタイムアウトした要素について通知を行う。
	 * また、タイムアウト通知を行った要素中でタイマ種別がタイムインターバルの場合、
	 * 再度タイムアウト待ちリストに登録し直す。
	 *
	 * @since	UDC1.0
	 */
	protected void operateWaitList()
	{
		/* タイムアウトしている要素を検索し、タイムアウトしていればタイムアウトイベントを通知 */
		UdcTimerManagedElem elm;
		for (int i=0; i<waitList.size(); i++) {
			elm = waitList.get(i);
			/* タイムアウトしている要素のタイムアウトイベント通知 */
			if (elm.remainTime <= 0) {
				if (elm.getTimerType() == UdcThreadMsgDtrEventTimeout.TimeInterval) {
					elm.remainTime = elm.elem.get_waitTime();
				} else {
					waitList.removeTimerElem(elm.getTimerIndex());
					i --;
				}
				Object obj = null;
				try {
					obj = elm.elem.clone();
					elm.eventChannel.push( new UdcThreadChannelDtr(0,Event_Timeout, obj) );
				} catch (CloneNotSupportedException exp_c) {
				} catch (InterruptedException exp_i) {
				}
			} else {
				/* waitListはタイムアウト順に整列しているため、以降の要素は全てタイムアウト対象外 */
				break;
			}
		}
	}

	/**
	 * タイマースレッド。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcTimerManager.run", " - start.");
		isStartComplete = true;

		try {
			long beforetime;
			long aftertime;
			long sleeptime;
			long waittime;
			UdcThreadChannelDtr dtr;

			while ( threadLoop ) {
				/* タイマ前処理 */
				waittime = -1;
				try {
					waitList.lock();
					/* 就寝時間の決定 */
					UdcTimerManagedElem elm = (UdcTimerManagedElem)waitList.get(0);
					if (elm != null) {
						waittime = elm.remainTime;
							/* 時刻変更監視を行うため、定期的に起床させるため固定値で初期化 */
						if (waittime > baseWaitTime) {
							waittime = baseWaitTime;
						}
							/* システムクロックが10msec程度であるため、処理上この分を減算しておく */
						if (waittime > 10) {
							waittime -= 10;
						}
					}
					waitList.unlock();
				} catch (InterruptedException exp) {}

					/* 就寝 */
				Date date = new Date();
				beforetime = date.getTime();
				dtr = null;
				try {
					if (waittime < 0) { dtr = mychannel.pull(); }
					else 			  { dtr = mychannel.pull(waittime); }
				} catch (InterruptedException exp) {}
				date = new Date();
				aftertime = date.getTime();

					/* タイマ処理 */
				try {
					waitList.lock();
						/* 就寝していた時間の決定 */
					sleeptime = getPassedTime(waittime,beforetime,aftertime,dtr);
						/* タイマ要素全ての残起床時間の更新 */
					updateWaitList( sleeptime , aftertime );
						/* タイムアウトしている要素を検索し、タイムアウトしていればタイムアウトイベントを通知 */
					operateWaitList();
					waitList.unlock();
				} catch (InterruptedException exp) {}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcTimerManager.run", " - stop.");
			throw death;
		}

		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcTimerManager.run", " - end.");
	}

}

/**
 * タイマ管理クラスで管理されるタイムアウトイベント待ち要素
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 *
 */
class UdcTimerManagedElem implements Comparable
{
	/**
	 * タイマ登録要求情報
	 * @since	UDC1.0
	 */
	protected UdcThreadMsgDtrEventTimeout	elem = null;

	/**
	 * タイマ処理登録中フラグ
	 * @since	UDC1.0
	 */
	protected boolean						init = true;

	/**
	 * タイムアウトまでの残就寝時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	protected long							remainTime = 0;

	/**
	 * タイムアウト通知先チャネル
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel				eventChannel = null;

	/**
	 * コンストラクタ
	 *
	 * @param	request		タイマ登録要求情報
	 * @param	channel		タイムアウト通知先チャネル
	 * @since	UDC1.0
	 */
	public UdcTimerManagedElem(UdcThreadMsgDtrEventTimeout request, UdcThreadChannel channel)
	{
		elem = request;
		eventChannel = channel;
		if (elem.timerType == UdcThreadMsgDtrEventTimeout.FixedTime) {
			Date cur = new Date();
			remainTime = elem.fixedTime.getTime() - cur.getTime();
		} else {
			remainTime = elem.get_waitTime();
		}
	}

	/**
	 * タイマー管理識別子を取得する。
	 *
	 * @return	タイマ管理識別子
	 * @since	UDC1.0
	 */
	public int getTimerIndex()
	{
		return elem.get_timerIndex();
	}

	/**
	 * タイマー種別を取得する。
	 *
	 * @return	タイマ種別
	 * @since	UDC1.0
	 */
	public int getTimerType()
	{
		return elem.get_timerType();
	}

	/**
	 * 起床までの時間を取得する。
	 *
	 * @return	起床までの時間（ミリ秒）
	 * @since	UDC1.0
	 */
	public long getRemainTime()
	{
		return remainTime;
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
		UdcTimerManagedElem elm = (UdcTimerManagedElem)obj;
		if (getRemainTime() > elm.getRemainTime()) {
			return 1;
		} else if (getRemainTime() < elm.getRemainTime()) {
			return -1;
		}
		return 0;
	}
}

/**
 * タイマ管理クラスで管理されるタイムアウトイベント待ち要素管理リスト
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 *
 */
class UdcTimerManagedList extends UdcMutex
{
	/**
	 * イベント待ちリスト
	 * @since	UDC1.0
	 */
	LinkedList timerList;

	/**
	 * タイマ管理クラス
	 * @since	UDC1.0
	 */
	protected UdcTimerManager timerManager = null;

	/**
	 * コンストラクタ
	 * @since	UDC1.0
	 */
	public UdcTimerManagedList()
	{
		timerList = new LinkedList();
	}

	/**
	 * タイマ管理クラスを内部に保存する。
	 * @param	timer 	タイマ管理クラス
	 * @since	UDC1.0
	 */
	public void setTimerManager(UdcTimerManager timer)
	{
		timerManager = timer;
	}

	/**
	 * タイマ登録要素数を取得する。
	 * @return	タイマ登録要素数
	 * @since	UDC1.0
	 */
	public int size()
	{
		return timerList.size();
	}

	/**
	 * タイマ登録要素数を取得する。
	 * @return	タイマ登録要素
	 * @param	index	項目の位置
	 * @since	UDC1.0
	 */
	public UdcTimerManagedElem get(int index)
	{
		if (timerList.size() <= index) {
			return null;
		}
		return (UdcTimerManagedElem)timerList.get(index);
	}

	/**
	 * タイマ登録を行う。
	 * タイマ登録を行うと同時に、タイマ処理スレッドに対して新規登録があったことを通知する。
	 *
	 * @return	登録したタイマ登録情報
	 * @param	type 	タイマ種別
	 * @param	milisec	タイムアウト時間(単位：ミリ秒)
	 * @param	channel	タイムアウト通知先チャネル
	 * @since	UDC1.0
	 */
	public UdcTimerManagedElem addTimerElem(int type, long milisec, UdcThreadChannel channel)
	{
		UdcTimerManagedElem addelm = new UdcTimerManagedElem( new UdcThreadMsgDtrEventTimeout(type,milisec), channel);
		return addTimerElem(addelm);
	}

	/**
	 * タイマ登録を行う。
	 * タイマ登録を行うと同時に、タイマ処理スレッドに対して新規登録があったことを通知する。
	 *
	 * @return	登録したタイマ登録情報
	 * @param	fixedTime	定時刻起床日時
	 * @param	channel	タイムアウト通知先チャネル
	 * @since	UDC1.0
	 */
	public UdcTimerManagedElem addTimerElem(Date fixedTime, UdcThreadChannel channel)
	{
		UdcTimerManagedElem addelm = new UdcTimerManagedElem( new UdcThreadMsgDtrEventTimeout(fixedTime), channel);
		return addTimerElem(addelm);
	}

	/**
	 * タイマ登録を行う。
	 * タイマ登録を行うと同時に、タイマ処理スレッドに対して新規登録があったことを通知する。
	 *
	 * @return	登録したタイマ登録情報
	 * @param	addelm	登録するタイマ情報
	 * @since	UDC1.0
	 */
	protected UdcTimerManagedElem addTimerElem(UdcTimerManagedElem addelm)
	{
		if (addelm.elem.set_timerIndex() < 0) {
			addelm = null;
			return null;
		}
		timerList.add(addelm);
		try {
			timerManager.mychannel.push( new UdcThreadChannelDtr(0,UdcTimerManager.Request_RegTimer) );
		} catch (InterruptedException exp) {
			return null;
		}
		return addelm;
	}

	/**
	 * タイマ登録の解除を行う。
	 *
	 * @return	解除したタイマ登録情報
	 * @param	index 	タイマ識別子
	 * @since	UDC1.0
	 */
	public UdcTimerManagedElem removeTimerElem(int index)
	{
		UdcTimerManagedElem elm;
		for (int i=0; i<timerList.size(); i++) {
			elm = (UdcTimerManagedElem)timerList.get(i);
			if (elm.getTimerIndex() == index) {
				timerList.remove(elm);
				return elm;
			}
		}
		return null;
	}

	/**
	 * 起床待ち要素を起床時間順に整列する。
	 *
	 * @since	UDC1.0
	 */
	void sortRemainTime()
	{
		Collections.sort(timerList);
	}
}

