/* *********************************************************************
 * @(#)UdcThreadOperation.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.io.*;
import java.sql.*;


/**
 * スレッドを実装したオペレーションスレッド。<br>
 * UdcThreadOperationPool管理下で、処理の分散化を実現します。<br>
 * 内部のスレッドの実装をアプリケーション実装の1例として説明します。 ★印の部分が
 * 本パッケージのサポート範囲。
 *
 *　User-Thread<br>
 *　　　User-1. オペレーションスレッドを確保要求(UdcThreadOperationPool.allocateOperationThread)<br>
 *　　　　・未使用状態のオペレーションスレッドを確保<br>
 *★　　　・確保直後にallocateOperationThread内でトランザクション開始を実行<br>
 *　　　　　(UdcThreadOperation.startTransaction->UdcOperationFace.startTransactionを自動的にコール)<br>
 *　　　User-2. オペレーションスレッドに要求(UdcThreadChannel.push)<br>
 *★UdcThreadOperation - Thread<br>
 *★　　UDC-1. ユーザ要求受信<br>
 *★　　UDC-2. オペレーション処理を実行(UdcOperationFace.action)<br>
 *★　　　・内部処理はユーザで定義<br>
 *★　　　　（要求元に応答を返却する場合は、ユーザ側で行う。返却先情報は、actionの引数として引渡し)<br>
 *　User-Thread<br>
 *　　　User-3. オペレーションスレッドからの応答を受信<br>
 *　　　User-4. オペレーションスレッドを解放(UdcThreadOperation.releaseOperationThread)<br>
 *★　　　・解放前にreleaseOperationThread内でトランザクション終了を実行<br>
 *　　　　　(UdcThreadOperation.stopTransaction->UdcOperationFace.stopTransactionを自動的にコール)<br>
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @see UdcThreadOperationBase
 * @since   UDC1.0
 */
public class UdcThreadOperation extends UdcThreadOperationBase implements Runnable
{
	/**
	 * オペレーション処理を実装するインタフェース。
	 * @since	UDC1.0
	 */
	public UdcOperationFace	operation;

	/**
	 * オペレーション処理を実行するスレッド。
	 * @since	UDC1.0
	 */
	protected Thread		thread = null;

	/**
	 * オペレーション処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	boolean		threadLoop = true;

	/**
	 * startメソッドによってオペレーション処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	boolean		isStartComplete = false;

	/**
	 * startメソッドによってオペレーション処理スレッドの起動が失敗したか否かの状態。
	 * @since	UDC1.1
	 */
	boolean		isStartError = false;

	/**
	 * スレッドに割り当てられるDBコネクション
	 * @since	UDC1.1
	 */
	protected Connection	connection = null;

	/**
	 * スレッドに割り当てられるDBコネクションのURL
	 * @since	UDC1.1
	 */
	protected String 	dbUrl = null;

	/**
	 * スレッドに割り当てられるDBコネクションのユーザ名
	 * @since	UDC1.1
	 */
	protected String 	dbUser = null;

	/**
	 * スレッドに割り当てられるDBコネクションのパスワード
	 * @since	UDC1.1
	 */
	protected String	dbPasswd = null;


	/**
	 * オペレーション処理インタフェースを設定する。
	 *
	 * @param	ope	オペレーション処理インタフェース
	 * @since	UDC1.0
	 */
	public void setOperation(UdcOperationFace ope)
	{
		operation = ope;
		operation.setThreadOperation(this);
	}

	/**
	 * オペレーションスレッドが起動したか否かを取得する。
	 *
	 * @return	オペレーションスレッドが完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * 本インスタンスの複製を作成します。
	 *
	 * @return 	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcThreadOperation th = new UdcThreadOperation();
		th.setDBInfo(dbUrl, dbUser, dbPasswd);
		th.setOperation((UdcOperationFace)operation.clone());
		return th;
	}

	/**
	 * オペレーション処理スレッドを開始する。
	 *
	 * @see		UdcThreadOperationBase#startOperation
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int startOperation()
	{
		return start();
	}

	/**
	 * オペレーション処理スレッドを停止する。
	 *
	 * @see		UdcThreadOperationBase#startOperation
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int stopOperation()
	{
		return stop();
	}

	/**
	 * UdcThreadOperationPoolから本オペレーションスレッド確保時にコールされる。
	 * 本メンバでは、ユーザのオペレーション処理であるUdcOperationFace.startTransaction
	 * をコールする。
	 *
	 * @see		UdcThreadOperationBase#startTransaction
	 * @see		UdcOperationFace#startTransaction
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int startTransaction()
	{
		return operation.startTransaction();
	}

	/**
	 * UdcThreadOperationPoolから本オペレーションスレッド解放時にコールされる。
	 * 本メンバでは、ユーザのオペレーション処理であるUdcOperationFace.startTransaction
	 * をコールする。
	 *
	 * @see		UdcThreadOperationBase#stopTransaction
	 * @see		UdcOperationFace#stopTransaction
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int stopTransaction()
	{
		return operation.stopTransaction();
	}

	/**
	 * オペレーションスレッドを開始する。
	 * 本メンバ関数は、UdcThreadOperationPool.startOperation内で
	 * 自動的に起動されるため、ユーザがコールする必要はありません。
	 *
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int start()
	{
		if (thread == null) {
			int waitbase = 20;
			int waitcnt = 30000 / waitbase + 1;
			isStartError = false;
			thread = new Thread(this);
			thread.start();
			UdcMutex mute = new UdcMutex();
			for (int i=0; ((! isStartComplete()) && i < waitcnt) ; i++) {
				try { mute.sleep(waitbase); } catch(InterruptedException exp) {}
				if (isStartError) {
					return -1;
				}
			}
		}
		return 0;
	}

	/**
	 * オペレーションスレッドを停止する。
	 * 本メンバ関数は、UdcThreadOperationPool.stopOperation内で
	 * 自動的に起動されるため、ユーザがコールする必要はありません。
	 *
	 * @return 	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public int stop()
	{
		if (thread != null) {
			threadLoop = false;
			if (thread.isAlive()) {
				thread.stop();
			}
			thread = null;
		}
		return 0;
	}

	/**
	 * DB-URLを取得する。
	 *
	 * @return	DB-URL文字列
	 * @since	UDC1.2
	 */
	public String 	getDB_URL() { return dbUrl; }

	/**
	 * DB-Userを取得する。
	 *
	 * @return	DB-User文字列
	 * @since	UDC1.2
	 */
	public String 	getDB_USER() { return dbUser; }

	/**
	 * DB-Passwdを取得する。
	 *
	 * @return	DB-Passwd文字列
	 * @since	UDC1.2
	 */
	public String 	getDB_PASSWD() { return dbPasswd; }

	/**
	 * DB接続情報を設定する。
	 *
	 * @param	url		DB-URL文字列
	 * @param	user	DB-User文字列
	 * @param	passwd	DB-Passwd文字列
	 * @since	UDC1.2
	 */
	public void	setDBInfo(String url, String user, String passwd)	
	{
		dbUrl = (url != null) ? new String(url) : null;
		dbUser = (user != null) ? new String(user) : null;
		dbPasswd = (passwd != null) ? new String(passwd) : null;
	}

	/**
	 * 接続中のDBコネクションを取得する。
	 *
	 * @return	接続中のDBコネクション
	 * @since	UDC1.2
	 */
	public Connection	getDBConnection()
	{
		return connection;
	}	

	/**
	 * DBコネクションを確立する。<br>
	 * 内部では、JDBCインタフェース DriverManager.getConnection がコールされます。
	 *
	 * @return	確立したDBコネクション。失敗すると null が返却されます。
	 * @since	UDC1.2
	 */
	public Connection	connectDBConnection()	
			throws SQLException
	{
		if (dbUrl == null || dbUser == null || dbPasswd == null) {
			return null;
		}

		disconnectDBConnection();
		connection = DriverManager.getConnection(dbUrl,dbUser,dbPasswd);
		connection.setAutoCommit(false);
		return connection;
	}

	/**
	 * DBコネクションを切断する。<br>
	 * 内部では、JDBCインタフェース Connection.close がコールされます。
	 *
	 * @since	UDC1.2
	 */
	public void	disconnectDBConnection()	
			throws SQLException
	{
		if (connection == null) {
			return;
		}

		Connection conn = connection;
		connection = null;
		conn.close();
	}

	/**
	 * オペレーション処理を行うスレッド。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcThreadOperation.run", "(" + getThreadIndex() +  ") - start.");
		if (dbUrl != null && dbUser != null && dbPasswd != null) {
			try {
				if (connectDBConnection() == null) {
					UdcTrace.trace(UdcTrace.Level,"UdcThreadOperation.run", "(" + getThreadIndex() +  ") - failed connect db, " 
									+ "URL[" + dbUrl + "] User[" + dbUser + "] Passwd[" + dbPasswd + "]");
					isStartComplete = false;
					isStartError = true;
					return;
				}
			} catch(Exception exp) {
				UdcTrace.trace(UdcTrace.Level,"UdcThreadOperation.run", "(" + getThreadIndex() +  ") - failed connect db, " 
								+ "URL[" + dbUrl + "] User[" + dbUser + "] Passwd[" + dbPasswd + "]  exception - " + exp);
				isStartComplete = false;
				isStartError = true;
				return;
			}
		}
		isStartError = false;
		isStartComplete = true;

		try {
			UdcThreadChannelDtr dtr;
			while (threadLoop) {
				try {
						// オペレーション要求受信
					if ((dtr=channel.pull()) == null) {
						continue;
					}
						// オペレーション処理実行
						//	(*) 要求元に応答を返却する場合は、ユーザ処理内で行う。
					operation.action(dtr);

				} catch (InterruptedException exp) {}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			try { disconnectDBConnection(); } catch(Exception exp) {}
			UdcTrace.trace(UdcTrace.Level,"UdcThreadOperation.run", "(" + getThreadIndex() +  ") - stop.");
			throw death;
		}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcThreadOperation.run", "(" + getThreadIndex() +  ") - end.");
	}
}
