/* *********************************************************************
 * @(#)UdcOperationFace.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;


/**
 * UdcThreadOperation下でオペレーション処理を行うためのインタフェース
 * クラス。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @see UdcThreadOperation
 * @since   UDC1.0
 */
public interface UdcOperationFace
{
	/**
	 * 自身を制御するオペレーションスレッドを設定する。
	 *
	 * @param	ope	オペレーションスレッド
	 * @since	UDC1.0
	 */
	public void setThreadOperation(UdcThreadOperation ope);

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException;

	/**
	 * 本インタフェースを動作させるオペレーションスレッド確保時にコールされる
	 * インタフェース。
	 *
	 * @return  正常：0／異常：0以外
	 * @since	UDC1.0
	 */
	public int startTransaction();

	/**
	 * 本インタフェースを動作させるオペレーションスレッド解放時にコールされる
	 * インタフェース。
	 *
	 * @return  正常：0／異常：0以外
	 * @since	UDC1.0
	 */
	public int stopTransaction();

	/**
	 * オペレーション処理を行うインタフェース。
	 *
	 * @return  正常：0／異常：0以外
	 * @param	request	オペレーション処理要求情報
	 * @since	UDC1.0
	 */
	public int action(UdcThreadChannelDtr request);
}
