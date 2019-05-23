/* *********************************************************************
 * @(#)UdcDBConnectionPool.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.sql.*;


/**
 * JDBCのDBコネクションプールクラス。
 * 本クラスでは、ユーザが確保したDBコネクションをリスト管理する。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcDBConnectionPool extends UdcMutex
{
	public final static String 	Cache_url		= "url";
	public final static String 	Cache_usr		= "usr";
	public final static String 	Cache_passwd	= "passwd";
	public final static String 	Cache_connName	= "connName";
	public final static String 	Cache_conn		= "conn";
	public final static String 	Cache_channel	= "channel";
	public final static String 	Cache_timerId	= "timerId";

	/**
	 * 未使用状態のDBコネクションリスト。
	 * @since   UDC1.0
	 */
	protected Vector emptyList = new Vector();

	/**
	 * 使用状態のDBコネクションリスト。
	 * @since   UDC1.0
	 */
	protected Vector usedList = new Vector();

	/**
	 * DBコネクション空き待ちリスト。
	 * @since   UDC1.0
	 */
	protected Vector waitList = new Vector();

	/**
	 *  空きスレッド確保待ちタイマー。
	 * @since   UDC1.0
	 */
	protected UdcTimerManager timerManager;


	/**
	 * コンストラクタ。
	 *
	 * @since   UDC1.0
	 */
	public UdcDBConnectionPool(UdcTimerManager timer)
	{
		super();
		timerManager = timer;
	}

	/**
	 * プールされているDBコネクション数を取得する。
	 *
	 * @return プールされているDBコネクション数
	 * @since   UDC1.0
	 */
	public int getSize()
	{
		return emptyList.size() + usedList.size();
	}

	/**
	 * プールされている未使用状態のDBコネクション数を取得する。
	 *
	 * @return プールされている未使用状態のDBコネクション数
	 * @since   UDC1.0
	 */
	public int getEmptySize()
	{
		return emptyList.size();
	}

	/**
	 * プールされている使用状態のDBコネクション数を取得する。
	 *
	 * @return プールされている使用状態のDBコネクション数
	 * @since   UDC1.0
	 */
	public int getUsedSize()
	{
		return usedList.size();
	}

	/**
	 * ユーザが確保したDBコネクションをプールする。プール時は、未使用状態
	 * としてプールされるので注意。
	 *
	 * @return プールしたDBコネクション
	 * @param	conn		DBコネクション
	 * @param	connName	DBコネクション名。本名はプールされるDBコネクションでユニークであること。
	 * @since   UDC1.0
	 */
	public Connection add(Connection conn, String connName)
	{
		if (UdcCache.searchCacheList(emptyList, Cache_connName, connName) != null
		  || UdcCache.searchCacheList(usedList, Cache_connName, connName) != null) {
			return null;
		}
		UdcCache c = new UdcCache();
		c.setAttr(Cache_conn, conn);
		c.setAttr(Cache_connName, new String(connName));
		synchronized (emptyList) { emptyList.add(c); }
		return conn;
	}

	/**
	 * 新規でDBコネクションをオープンし、プールする。プール時は、未使用状態
	 * としてプールされるので注意。
	 *
	 * @return オープンしたDBコネクション
	 * @param	url			DriverManager.getConnectionの第一引数(DB名)
	 * @param	user		DriverManager.getConnectionの第二引数(DBアクセス時のユーザ名)
	 * @param	passwd 		DriverManager.getConnectionの第三引数(DBアクセス時のパスワード)
	 * @param	connName	DBコネクション名
	 * @since   UDC1.0
	 */
	public Connection add(String url, String user, String passwd, String connName)
		throws SQLException
	{
		if (UdcCache.searchCacheList(emptyList, Cache_connName, connName) != null
		  || UdcCache.searchCacheList(usedList, Cache_connName, connName) != null) {
			return null;
		}

		Connection conn = DriverManager.getConnection(url,user,passwd);

		UdcCache c = new UdcCache();
		c.setAttr(Cache_conn, conn);
		c.setAttr(Cache_connName, new String(connName));
		c.setAttr(Cache_url, new String(url));
		c.setAttr(Cache_usr, new String(user));
		c.setAttr(Cache_passwd, new String(passwd));
		synchronized (emptyList) { emptyList.add(c); }
		return conn;
	}

	/**
	 * 指定したDBコネクションをプールから削除する。本メンバ関数では
	 * 使用/未使用状態に関わらず無条件にプールから削除する。
	 * DBコネクションとしてはcloseされない。
	 *
	 * @return プールから削除したDBコネクション
	 * @param	conn		DBコネクション
	 * @since   UDC1.0
	 */
	public Connection remove(Connection conn)
	{
		UdcCache c;
		synchronized (emptyList) {
			if ((c=searchList(emptyList, conn)) != null) {
				emptyList.remove(c);
				return conn;
			}
			if ((c=searchList(usedList, conn)) != null) {
				usedList.remove(c);
				return conn;
			}
		}
		return null;
	}

	/**
	 * プール内の全てのDBコネクションを削除する。本メンバ関数では
	 * 使用/未使用状態に関わらず無条件にプールから削除する。
	 * DBコネクションとしてもcloseする。
	 *
	 * @since   UDC1.0
	 */
	public void removeAndClose()
	{
		int i;
		UdcCache c;
		Connection conn;
		synchronized (emptyList) {
			for (i=0; i<emptyList.size(); i++) {
				c = (UdcCache)emptyList.get(i);
				if ((conn=(Connection)c.getAttr(Cache_conn)) != null) {
					try { conn.close(); } catch(Exception ex) {}
				}
			}
			for (i=0; i<usedList.size(); i++) {
				c = (UdcCache)usedList.get(i);
				if ((conn=(Connection)c.getAttr(Cache_conn)) != null) {
					try { conn.close(); } catch(Exception ex) {}
				}
			}
			emptyList.clear();
			usedList.clear();
		}
	}

	/**
	 * 未使用状態のDBコネクションをプールから確保する。
	 * 確保されたDBコネクションは使用状態に状態遷移する。本メンバでは、空きがなければ null を返却する。
	 *
	 * @return	確保したDBコネクション
	 * @since   UDC1.0
	 */
	public Connection try_allocate()
	{
		int ecnt;
		synchronized (emptyList) { ecnt = emptyList.size(); }
		if (ecnt <= 0) {
			return null;
		}

		UdcCache c;
		synchronized (emptyList) {
			c = (UdcCache)emptyList.get(0);
			emptyList.remove(c);
			usedList.add(c);
		}
		return (Connection)c.getAttr(Cache_conn);
	}

	/**
	 * 未使用状態のDBコネクションをプールから確保する。
	 * 確保されたDBコネクションは使用状態に状態遷移する。本メンバでは、空きがなければ空きが確保できるまで待ちます。
	 *
	 * @return	確保したDBコネクション
	 * @since   UDC1.0
	 */
	public Connection allocate(long waittime)
	{
		int ecnt;
		synchronized (emptyList) { ecnt = emptyList.size(); }
		if (ecnt <= 0) {
			allocateWait(waittime);
			synchronized (emptyList) { ecnt = emptyList.size(); }
			if (ecnt <= 0) {
				return null;
			}
		}

		UdcCache c;
		synchronized (emptyList) {
			c = (UdcCache)emptyList.get(0);
			emptyList.remove(c);
			usedList.add(c);
		}
		return (Connection)c.getAttr(Cache_conn);
	}

	/**
	 * allocateによって確保されたDBコネクションを解放する。
	 * 解放されたDBコネクションは未使用状態に状態遷移する。
	 *
	 * @return 解放したか否か（解放できない場合は指定したDBコネクションが確保されていない場合）
	 * @param	conn	DBコネクション
	 * @since   UDC1.0
	 */
	public boolean release(Connection conn)
	{
		boolean res = false;
		synchronized (emptyList) {
			UdcCache c = searchList(usedList, conn);
			if (c != null) {
				usedList.remove(c);
				emptyList.add(c);
				resumeWait();
				res = true;
			}
		}
		return res;
	}

	/**
	 * 空DB-Connectionが存在しないため、waittime時間の間にDB-Connectionが
	 * 空くのを待って確保する。本メンバ関数は、この空き待ち登録を行ないます。
	 *
	 * @param	waittime	空DB-connection無し時の確保待ち時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	protected void allocateWait(long waittime)
	{
		int index = -1;
		UdcThreadChannel channel = new UdcThreadChannel();
		try {	
			index = timerManager.registrateTimer(UdcThreadMsgDtrEventTimeout.Timeout, waittime, channel);
		} catch (InterruptedException exp) {
			return;
		}
		UdcCache wc = new UdcCache();
		wc.setAttr(Cache_channel, channel);
		wc.setAttr(Cache_timerId, new Integer(index));
		synchronized (waitList) {
			waitList.add(wc);
		}
		// T.O|起床待ち 
		try { channel.pull(); } catch(Exception exp) {}
	}

	/**
	 * 空DB-Connectionが存在しないため、waittime時間の間にDB-Connectionが
	 * 空くのを待って確保する。本メンバ関数は、DB-Connection解放時に待ち中の確保処理を起床する。
	 *
	 * @since	UDC1.0
	 */
	protected void resumeWait()
	{
		int wcnt;
		UdcCache wc;
		synchronized (waitList) { wcnt = waitList.size(); }
		if (wcnt <= 0) {
			return;
		}
		synchronized (waitList) {
			wc = (UdcCache)waitList.get(0);
			waitList.remove(0);
		}
		UdcThreadChannel channel = (UdcThreadChannel)wc.getAttr(Cache_channel);
		try { channel.push(new UdcThreadChannelDtr(0,0xFFFFFFFF)); } catch (InterruptedException exp) {}
	}

	/**
	 * DBコネクションリストからDBコネクションで検索する。
	 *
	 * @return 検索されたDBコネクション
	 * @param	list	DBコネクションリスト
	 * @param	con		DBコネクション
	 * @since   UDC1.0
	 */
	protected UdcCache searchList(Vector list, Connection con)
	{
		if (con == null) {
			return null;
		}
		for (int i=0; i<list.size(); i++) {
			if (con == (Connection)((UdcCache)list.get(i)).getAttr(Cache_conn)) {
				return (UdcCache)list.get(i);
			}
		}
		return null;
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
		UdcCache c;
		Connection conn;
		StringBuffer str = new StringBuffer();
		str.append("All Connection Number = " + getSize());		str.append("\n");

		str.append("  EmptyConnection = " + getEmptySize());	str.append("\n");
		for (i=0; i<emptyList.size(); i++) {
			c = (UdcCache)emptyList.get(i);
			str.append("    Name[");	str.append((String)c.getAttr(Cache_connName));	str.append("] ");
			if ((conn=(Connection)c.getAttr(Cache_conn)) != null) {
				str.append(conn.toString());		str.append("\n");
			} else {
				str.append("null\n");
			}
		}

		str.append("  UsedConnection = " + getUsedSize());		str.append("\n");
		for (i=0; i<usedList.size(); i++) {
			c = (UdcCache)usedList.get(i);
			str.append("    Name[");	str.append((String)c.getAttr(Cache_connName));	str.append("] ");
			if ((conn=(Connection)c.getAttr(Cache_conn)) != null) {
				str.append(conn.toString());		str.append("\n");
			} else {
				str.append("null\n");
			}
		}
		return str.toString();
	}
}

