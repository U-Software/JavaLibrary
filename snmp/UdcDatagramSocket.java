/* *********************************************************************
 * @(#)UdcDatagramSocket.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.net.*;


/**
 * DatagramSocketのカプセルクラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcDatagramSocketControler
 * @since	UDC1.0
 */
public class UdcDatagramSocket
{
	/**
	 * 受信時に一度の読み出すバイト長のデフォルト。
	 * @since	UDC1.0
	 */
	final static int MaxMsgLen = 8192;

	/**
	 * java.net.DatagramSocketインスタンス。
	 * 本インスタンスはopenメソッド関数にて生成される。
	 * @since	UDC1.0
	 */
	protected DatagramSocket	soc = null;

	/**
	 * socketアドレス。
	 * @since	UDC1.0
	 */
	protected InetAddress		socAddr;

	/**
	 * socketポート。
	 * @since	UDC1.0
	 */
	protected int				socPort;

	/**
	 * デフォルト通信先アドレス。
	 * @since	UDC1.0
	 */
	protected InetAddress		defaultDestAddr;

	/**
	 * デフォルト通信先ポート。
	 * @since	UDC1.0
	 */
	protected int				defaultDestPort;

	/**
	 * 受信時に読み出すバイト長。
	 * @since	UDC1.0
	 */
	protected int				readLen = MaxMsgLen;

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcDatagramSocket()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param	addr		socketアドレス
	 * @param	port		socketポート
	 * @since	UDC1.0
	 */
	public UdcDatagramSocket(InetAddress addr, int port)
	{
		socAddr = addr;
		socPort = port;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	addr			socketアドレス
	 * @param	port			socketポート
	 * @param	defDestAddr		デフォルト通信先アドレス
	 * @param	defDestPort		デフォルト通信先ポート
	 * @since	UDC1.0
	 */
	public UdcDatagramSocket(InetAddress addr, int port,InetAddress defDestAddr, int defDestPort)
	{
		socAddr = addr;
		socPort = port;
		defaultDestAddr = defDestAddr;
		defaultDestPort = defDestPort;
	}

	/**
	 * データグラムソケットを取得する。
	 *
	 * @return 	java.net.DatagramSocket
	 * @since	UDC1.0
	 */
	public DatagramSocket	get_socket() { return soc; }

	/**
	 * socketアドレスを取得する。
	 *
	 * @return	socketアドレス
	 * @since	UDC1.0
	 */
	public InetAddress		get_socAddr() { return socAddr; }

	/**
	 * socketポートを取得する。
	 *
	 * @return	socketポート
	 * @since	UDC1.0
	 */
	public int				get_socPort() { return socPort; }

	/**
	 * 受信時に読み出すバイト長を取得する。
	 *
	 * @return 	受信時に読み出すバイト長。
	 * @since	UDC1.0
	 */
	public int 				get_readLen() { return readLen; }

	/**
	 * 受信時に読み出すバイト長を設定する。
	 *
	 * @param 	reclen	受信時に読み出すバイト長。
	 * @since	UDC1.0
	 */
	public void 			set_readLen(int reclen) { readLen = reclen; }

	/**
	 * DatagramSocketクラスを生成する。
	 * もし、既にDatagramSocketクラスが生成されている場合には、一度closeして
	 * 再生成します。
	 *
	 * @since	UDC1.0
	 */
	public void open() throws IOException
	{
		close();
		try {
			if (socAddr == null) {
				soc = new DatagramSocket(socPort);
			} else {
				soc = new DatagramSocket(socPort,socAddr);
			}
		} catch (SocketException exc) {
			String str = "Socket problem - " + exc.getMessage();
			throw (new IOException(str));
		}
	}

	/**
	 * DatagramSocketクラスを生成する。
	 * もし、既にDatagramSocketクラスが生成されている場合には、一度closeして
	 * 再生成します。
	 *
	 * @param	port	socketポート
	 * @since	UDC1.0
	 */
	public void open(int port) throws IOException
	{
		close();
		socPort = port;
		try {
			soc = new DatagramSocket(socPort);
		} catch (SocketException exc) {
			String str = "Socket problem - " + exc.getMessage();
			throw (new IOException(str));
		}
	}

	/**
	 * DatagramSocketクラスを生成する。
	 * もし、既にDatagramSocketクラスが生成されている場合には、一度closeして
	 * 再生成します。
	 *
	 * @param	host	socketアドレス
	 * @param	port	socketポート
	 * @since	UDC1.0
	 */
	public void open(String host, int port) throws IOException
	{
		close();
		socPort = port;
		try {
			socAddr = InetAddress.getByName(host);
			soc = new DatagramSocket(socPort,socAddr);
		} catch (SocketException exc) {
			String str = "Socket problem - " + exc.getMessage();
			throw (new IOException(str));
		}
	}

	/**
	 * socketをクローズする。
	 *
	 * @since	UDC1.0
	 */
	public void close()
	{
		if (soc != null) {
			soc.close();
			soc = null;
		}
	}

	/**
	 * socketから情報を受信する。
	 * 受信するまで本メソッド内で待ち状態となります。
	 *
	 * @return	受信したデータグラムパケット
	 * @since	UDC1.0
	 */
	public DatagramPacket receive() throws IOException
	{
		DatagramPacket p = null;
		if (soc != null) {
			byte [] data = new byte[readLen];
			p = new DatagramPacket(data,readLen);
			soc.receive(p);
		}
		return p;
	}

	/**
	 * socketから情報を送信する。
	 * 送信するまで本メソッド内で待ち状態となります。
	 *
	 * @param	p	送信データグラムパケット
	 * @since	UDC1.0
	 */
	public void send(DatagramPacket p) throws IOException
	{
		if (soc != null) {
			String str = "sock problem - not open ";
			throw (new IOException(str));
		}
		soc.send(p);
	}

	/**
	 * socketから情報を送信する。送信先は、デフォルト通信先で設定したものとなる。
	 * 送信するまで本メソッド内で待ち状態となります。
	 *
	 * @param	data	送信バイト列
	 * @since	UDC1.0
	 */
	public void send(byte[] data) throws IOException
	{
		if (defaultDestAddr == null) {
			String str = "send problem - not set DestInetAddress/Port ";
			throw (new IOException(str));
		}
		DatagramPacket p = new DatagramPacket(data, data.length, defaultDestAddr, defaultDestPort);
		send(p);
	}

	/**
	 * socketから情報を送信する。
	 * 送信するまで本メソッド内で待ち状態となります。
	 *
	 * @param	data		送信バイト列
	 * @param	destAddr	送信先アドレス
	 * @param	destPort	送信先ポート
	 * @since	UDC1.0
	 */
	public void send(byte[] data, InetAddress destAddr, int destPort) throws IOException
	{
		DatagramPacket p = new DatagramPacket(data, data.length, destAddr, destPort);
		send(p);
	}

}

