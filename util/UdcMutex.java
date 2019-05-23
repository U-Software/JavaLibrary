/* *********************************************************************
 * @(#)UdcMutex.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

/**
 * スレッド間の排他制御を行うmutexユーティリティ。
 * 本クラスは、syncronized/wait/notifyAllの組合せによってunix等のmutex
 * で知られるmutex機能の一部を実現しています。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcMutex
{
	/**
	 * 排他獲得状態。
	 *
	 * @since	UDC1.0
	 */
	private boolean	locked = false;

	/**
	 * cond_wait起床状態。
	 *
	 * @since	UDC1.0
	 */
	private boolean condition = false;

	/**
	 * 排他制御権獲得中スレッド。
	 *
	 * @since	UDC1.0
	 */
	private Thread	owner = null;

	/**
	 * 他スレッドとの排他権獲得を行います。既に他スレッドによって排他権獲得中の場合、
	 * 排他権獲得スレッドが解放するまで待ち状態となります。
	 * (注意）
	 *   排他権の解放(unlock)は、排他権を獲得したスレッドと同一でないと解放できません。
	 *
	 * @since	UDC1.0
	 */
	public synchronized void lock() throws InterruptedException
	{
		while (locked) {
			wait();
		}
		locked = true;
		owner = Thread.currentThread();
	}

	/**
	 * 他スレッドとの排他権解放を行います。自スレッドで排他権獲得中ならば、排他権を解放する
	 * と同時に他スレッドで排他権獲得待ち状態のスレッド起床します。
	 * 自スレッドで排他権獲得中でなければ何も行いません。
	 * (注意）
	 *   排他権の解放(unlock)は、排他権を獲得したスレッドと同一でないと解放できません。
	 *
	 * @since	UDC1.0
	 */
	public synchronized void unlock()
	{
		if (Thread.currentThread() == owner) {
			locked = false;
			owner = null;
			notifyAll();
		}
	}

	/**
	 * cond_wait状態のスレッドを起床します。
	 * cond_wait状態のスレッドが複数する場合、いづれか一つが起床復帰します。
	 * 起床優先度は、JAVAスレッド優先度に依存します。
	 *
	 * @since	UDC1.0
	 */
	public synchronized void cond_signal()
	{
		condition = true;
		notifyAll();
	}

	/**
	 * 自スレッドをcond_wait状態(cond_signalによる起床待ち)にします。
	 * 注意）
	 * 　本メンバは、自スレッドで排他権獲得中でなければcond_wait状態に遷移しません。
	 *   内部的には、一次的に排他権が解放されますが、起床と同時に排他権が確保されます。
	 *   そのため、以下の使用例のように使用します。
	 * 使用例）
	 *   mutex.lock();
	 *   mutex.cond_wait();
	 *   mutex.unlock();
	 * @since	UDC1.0
	 */
	public synchronized void cond_wait() throws InterruptedException
	{
		if (Thread.currentThread() != owner) {
			return;
		}
		unlock();
		while (! condition) {
			wait();
		}
		lock();
		condition = false;
	}

	/**
	 * 自スレッドをcond_wait状態(cond_signalによる起床待ち)にします。
	 * cond_waitメンバとの違いは、指定したmilisecミリ秒待って起床されなかったら
	 * 自動的に起床・復帰します。
	 * 注意）
	 * 　本メンバは、自スレッドで排他権獲得中でなければcond_wait状態に遷移しません。
	 *   内部的には、一次的に排他権が解放されますが、起床と同時に排他権が確保されます。
	 *   そのため、以下の使用例のように使用します。
	 * 使用例）
	 *   mutex.lock();
	 *   mutex.cond_timewait(60*1000);	60*1000⇒60秒
	 *   mutex.unlock();
	 *
	 * @param	milisec		cond_wait状態から復帰するまでの最大待ち時間(ミリ秒)
	 * @since	UDC1.0
	 */
	public synchronized void cond_timewait(long milisec) throws InterruptedException
	{
		if (Thread.currentThread() != owner) {
			return;
		}
		long cur = System.currentTimeMillis();
		long wakeup = cur + milisec;
		long old = cur;
		long remain = milisec;
		unlock();
		while (! condition) {
			old = cur;
			wait(remain);
			cur = System.currentTimeMillis();
			if (cur >= wakeup || cur < old || cur > (old+remain)) {
				break;
			}
			remain = wakeup - cur;
		}
		lock();
		condition = false;
	}

	/**
	 * 自スレッドでmilisecミリ秒間就寝します。他スレッドからcond_signalがコール
	 * されるとmilisecで指定した時間に満たなくとも起床されます。
	 *
	 * @param	milisec	就寝時間(ミリ秒)
	 * @since	UDC1.0
	 */
	public synchronized void sleep(long milisec) throws InterruptedException
	{
		lock();
		cond_timewait(milisec);
		unlock();
	}
}

