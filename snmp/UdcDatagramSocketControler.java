/* *********************************************************************
 * @(#)UdcDatagramSocketControler.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.net.*;

/**
 * UdcDatagramSocketの制御クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcDatagramSocket
 * @see		UdcDatagramSocketSenderFace
 * @see		UdcDatagramSocketReceiverFace
 * @since	UDC1.0
 */
public class UdcDatagramSocketControler
{
	/**
	 * データグラムソケットクラス。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocket 				socket = null;

	/**
	 * UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェース。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocketReceiverFace		receiver = null;

	/**
	 * UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェース。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocketSenderFace		sender = null;


	/**
	 * コンストラクタ
	 *
	 * @param	addr		socketアドレス
	 * @param	port		socketポート
	 * @param	rec 		UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェース
	 * @param	snd			UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェース
	 * @since	UDC1.0
	 */
	public UdcDatagramSocketControler(InetAddress addr, int port,
									UdcDatagramSocketReceiverFace rec, UdcDatagramSocketSenderFace snd)
	{
		socket = new UdcDatagramSocket(addr,port);
		receiver = rec;
		sender = snd;
		if (receiver != null) {
			receiver.set_socket(socket);
		}
		if (sender != null) {
			sender.set_socket(socket);
		}
	}

	/**
	 * コンストラクタ
	 *
	 * @param	s			データグラムソケットクラス
	 * @param	rec 		UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェース
	 * @param	snd			UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェース
	 * @since	UDC1.0
	 */
	public UdcDatagramSocketControler(UdcDatagramSocket s, UdcDatagramSocketReceiverFace rec, UdcDatagramSocketSenderFace snd)
	{
		socket = s;
		receiver = rec;
		sender = snd;
		if (receiver != null) {
			receiver.set_socket(socket);
		}
		if (sender != null) {
			sender.set_socket(socket);
		}
	}

	/**
	 * UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェースを取得する。
	 *
	 * @return	UdcDatagramSocketControler下でUDPメッセージ受信をユーザ独自処理とするためのインタフェース
	 * @since	UDC1.0
	 */
	public UdcDatagramSocketReceiverFace getReceiver() { return receiver; }

	/**
	 * UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェースを取得する。
	 *
	 * @return	UdcDatagramSocketControler下でUDPメッセージ送信をユーザ独自処理とするためのインタフェース
	 * @since	UDC1.0
	 */
	public UdcDatagramSocketSenderFace getSender() { return sender; }

	/**
	 * DatagramSocketクラスを生成する。
	 * 具体的には、UdcDatagramSocket.openメンバ関数を実行する。
	 *
	 * @since	UDC1.0
	 */
	public void open() throws IOException
	{
		if (socket == null) {
			String str = "socket problem - not instanciate";
			throw (new IOException(str));
		}
		socket.open();
	}

	/**
	 * socketをクローズする。
	 * 具体的には、UdcDatagramSocket.closeメンバ関数を実行する。
	 *
	 * @since	UDC1.0
	 */
	public void close() throws IOException
	{
		if (socket == null) {
			String str = "socket problem - not instanciate";
			throw (new IOException(str));
		}
		socket.close();
	}

	/**
	 * UDP通信受信処理を行う。
	 * UdcDatagramSocketReceiverFaceが設定されている場合は、そのインタフェースのreceiveメンバ関数が実行され、
	 * 設定されていない場合は UdcDatagramSocket.receive が実行される。
	 *
	 * @return	受信したデータグラムパケット
	 * @since	UDC1.0
	 */
	public DatagramPacket receive() throws IOException
	{
		if (receiver == null) {
			if (socket == null) {
				String str = "socket problem - not instanciate";
				throw (new IOException(str));
			}
			return socket.receive();
		}
		return receiver.receive();
	}

	/**
	 * UDP通信送信処理を行う。
	 * UdcDatagramSocketSenderFaceが設定されている場合は、そのインタフェースのsendメンバ関数が実行され、
	 * 設定されていない場合は UdcDatagramSocket.send が実行される。
	 *
	 * @param	data	送信したいデータグラムパケット
	 * @since	UDC1.0
	 */
	public void send(DatagramPacket data) throws IOException
	{
		if (sender == null) {
			if (socket == null) {
				String str = "socket problem - not instanciate";
				throw (new IOException(str));
			}
			socket.send(data);
			return;
		}
		sender.send(data);
		return;
	}

}

