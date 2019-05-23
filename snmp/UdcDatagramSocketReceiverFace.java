/* *********************************************************************
 * @(#)UdcDatagramSocketReceiverFace.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.net.*;

/**
 * UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェース。
 * UdcDatagramSocketControler.receiveメソッド関数では、内部で DatagramSocket.receive
 * 呼び出します。そのため、呼び出しスレッドはUDPメッセージを受信するまで待ち状態となります。
 * ユーザ側でこのような処理を回避する場合や、受信メッセージ長を自身で制御したい場合等に本クラス
 * を継承して独自の受信フレームワークを構築することが可能です。
 * 本インスタンスをUdcDatagramSocketControlerに引き渡してやることで、UdcDatagramSocketControler.receive
 * 実行時に自動的に本クラスのreceiveメソッド関数が呼び出されます。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcDatagramSocket
 * @see		UdcDatagramSocketControler
 * @since	UDC1.0
 */
public interface UdcDatagramSocketReceiverFace
{
	/**
	 * UdcDatagramSocketを設定する。
	 * UdcDatagramSocketControlerのコンストラクタで、本メンバ関数は自動的に呼び出されます。
	 * @since	UDC1.0
	 */
	public void set_socket(UdcDatagramSocket s);

	/**
	 * 本クラスを継承して独自クラスを定義した場合の起動メンバ関数。
	 * udc.net内では自動的に呼び出されることはありません。
	 * @since	UDC1.0
	 */
	public void start() throws IOException;

	/**
	 * 本クラスを継承して独自クラスを定義した場合の停止メンバ関数。
	 * udc.net内では自動的に呼び出されることはありません。
	 * @since	UDC1.0
	 */
	public void stop() throws IOException;

	/**
	 * startメソッドによって起動が完了したか否かを取得する。
	 *
	 * @return	startメソッドによって起動が完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete();

	/**
	 * UDP通信受信処理を行う。
	 * UdcDatagramSocketControler.receiveメソッド関数では、内部で DatagramSocket.receive
	 * 呼び出します。そのため、呼び出しスレッドはUDPメッセージを受信するまで待ち状態となります。
	 * ユーザ側でこのような処理を回避する場合や、受信メッセージ長を自身で制御したい場合等に本クラス
	 * を継承して独自の受信フレームワークを構築することが可能です。
	 * 本インスタンスをUdcDatagramSocketControlerに引き渡してやることで、UdcDatagramSocketControler.receive
	 * 実行時に自動的に本メンバメソッドが呼び出されます。
	 *
	 * @return	受信したデータグラムパケット
	 * @since	UDC1.0
	 */
	public DatagramPacket receive() throws IOException;
}

