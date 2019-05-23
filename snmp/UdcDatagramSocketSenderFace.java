/* *********************************************************************
 * @(#)UdcDatagramSocketSender.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.net.*;


/**
 * UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェース。
 * UdcDatagramSocketControler.sendメソッド関数では、内部で DatagramSocket.sendを呼び出します。
 * そのため、呼び出しスレッドはTCPメッセージを送信するまで待ち状態となります。
 * ユーザ側でこのような処理を回避する場合等に本クラスを継承して独自の送信フレームワーク
 * を構築することが可能です。
 * 本インスタンスをUdcDatagramSocketControlerに引き渡してやることで、UdcDatagramSocketControler.send
 * 実行時に自動的に本クラスのsendメンバ関数が呼び出されます。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcDatagramSocket
 * @see		UdcDatagramSocketControler
 * @since	UDC1.0
 */
public interface UdcDatagramSocketSenderFace
{
	/**
	 * UdcSocketを設定する。
	 * UdcSocketControlerのコンストラクタで、本メンバ関数は自動的に呼び出されます。
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
	 * UDP通信送信処理を行う。
	 * UdcDatagramSocketControler.sendメソッド関数では、内部で Socket.sendを呼び出します。
	 * そのため、呼び出しスレッドはUDPメッセージを送信するまで待ち状態となります。
	 * ユーザ側でこのような処理を回避する場合等に本クラスを継承して独自の送信フレームワーク
	 * を構築することが可能です。
	 * 本インスタンスをUdcDatagramSocketControlerに引き渡してやることで、UdcDatagramSocketControler.send
	 * 実行時に自動的に本メンバメソッドが呼び出されます。
	 *
	 * @param	data	送信したいデータグラムパケット
	 * @since	UDC1.0
	 */
	public void send(DatagramPacket data) throws IOException;
}

