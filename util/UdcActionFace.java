/* *********************************************************************
 * @(#)UdcActionFace.java 1.0, 30 Jun 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;
import java.sql.*;

/**
 * アクション処理のインタフェースクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 30 Jun 2006
 * @see UdcThreadOperation
 * @since   UDC1.2
 */
public class UdcActionFace
{
	/**
	 * アクション処理のスレッド間通信タイムアウト時間のデフォルト。
	 */	
	public static long	timeout	= 180*1000;

	/**
	 * UGBViewが管理する負荷分散スレッド
	 */
	public UdcThreadOperation 	myThread;

	/**
	 * UGBViewが管理する負荷分散スレッドが所有するDB-Connection。
	 */
	public Connection	dbconn;

	/**
	 * アクションパラメータ
	 */
	public UdcCache		args = null;

	/**
	 * アクション結果
	 */
	public int 			result;

	/**
	 * アクション結果情報
	 */
	public UdcCache		resultData = null;

	/**
	 * アクション結果文字列
	 */
	public String 		err = null;


	/**
	 * コンストラクタ
	 */
	public UdcActionFace()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param	param	アクションパラメータ
	 */
	public UdcActionFace(UdcCache param)
	{
		args = param;
	}

	/**
  	 * アクション処理のスレッド間通信タイムアウト時間を取得する。
  	 *	@return アクションのスレッド間通信タイムアウト時間
	 */
	public long get_timeout()
	{
		return UdcActionFace.timeout;
	}

	/**
  	 * UGBViewが管理する分散スレッド情報をアクションクラスに設定するインタフェース関数。
	 *	@param	ope [in]	UGBViewが管理する分散スレッド情報。	
	 */
	public void setThreadOperation(UdcThreadOperation ope)
	{
		myThread = ope;
		dbconn = myThread.getDBConnection();
	}

	/**
  	 * アクション処理を実行するインタフェース関数。
	 *	@param	request [in]	アクション要求のスレッド間チャネル情報。
  	 *	@return アクション応答のためのスレッド間チャネル情報。
	 */
    public UdcThreadChannelDtr execute(UdcThreadChannelDtr request)
	{
		result = -1;
		err = "Not implementation action.";
		return request;
	}

	/**
  	 * Statement.executeQueryで取得したResultSetをキャッシュリストに設定する。
	 *	@param	rs	Statement.executeQueryで取得したResultSet
  	 *	@return ResultSetをキャッシュリストに変換した情報
	 */
    public Vector ResultSet2CacheList(ResultSet rs)
			throws SQLException
	{
		UdcCache ca;
		Vector clist = new Vector();
		while (rs.next()) {
			clist.add(ResultSet2Cache(rs));
		}
		return clist;
	}

	/**
  	 * Statement.executeQueryで取得したResultSetをキャッシュに設定する。
	 *	@param	rs	Statement.executeQueryで取得したResultSet
  	 *	@return ResultSetをキャッシュに変換した情報
	 */
    public UdcCache ResultSet2Cache(ResultSet rs)
			throws SQLException
	{
		UdcCache ca = new UdcCache();
		ResultSetMetaData meta = rs.getMetaData();

		Object obj;
		for (int i=1; i<=meta.getColumnCount(); i++) {
			if ((obj=rs.getObject(i)) == null) { continue; }
			ca.setAttr(meta.getColumnName(i), obj);
		}
		return ca;
	}

}
