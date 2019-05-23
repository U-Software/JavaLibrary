/* *********************************************************************
 * @(#)UdcTelnetServer.java 1.2, 25 Mar 2005
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.lang.*;
import java.util.*;
import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import udc.util.UdcTrace;

/**
 * TelnetサーバーのIOクラス。本クラスではtelnetのソケット
 * 通信等の低レベルインタフェースを提供します。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 25 Mar 2005
 * @see 	UdcTelnetControler
 * @since   UDC1.2
 */
public class UdcTelnetServer implements UdcTelnetConstants
{
	/**
	 * プロトコル状態。
	 * sinze UDC1.2
	 */
	private final static byte STATE_DATA			= 0;
	private final static byte STATE_IAC				= 1;
	private final static byte STATE_IACSB			= 2;
	private final static byte STATE_IACSBDATA		= 3;

	/**
	 * 現在のプロトコル状態。 
	 * sinze UDC1.2
	 */
	private byte protcolStatus = 0;

	private byte current_sb;		/* IAC SB <xx> 受信時の受信バイト */
	private byte[] received_sb;		/* IAC SB 受信情報 */
	private byte[] received_buf;	/* 受信情報 */

	/**
	 * クライアントターミナルのWidth/Height。
	 * IAC SB TELOPT_NAWS xx width xx height IAC SE のやり取りを行った場合にのみ
	 * 本メンバに値が反映されます。
	 * @since	UDC1.2
	 */
	int 	width = 0;
	int 	height = 0;

	/**
	 * telnet接続時刻
	 * @since	UDC1.2
	 */
	GregorianCalendar	conntime;	

	/**
	 * telnetサーバーソケット。
	 * @since	UDC1.2
	 */
	protected Socket socket;

	/**
	 * telnetソケットの入力ストリーム。
	 * @since	UDC1.2
	 */
	protected BufferedInputStream is;

	/**
	 * telnetソケットの出力ストリーム。
	 * @since	UDC1.2
	 */
	protected BufferedOutputStream os;



	/**
	 * コンストラクタ。
	 *
	 * @since	UDC1.2
	 */
	public UdcTelnetServer(Socket soc)
			throws IOException
	{
		protcolStatus = 0;
		current_sb = 0;	
		received_sb = new byte[256];
		received_buf = new byte[2048];

		conntime = new GregorianCalendar();
		socket = soc;
		is = new BufferedInputStream(socket.getInputStream());
		os = new BufferedOutputStream(socket.getOutputStream());
	}

	/**
	 * クライアントとネゴシエイトしたWidthを返却します。
	 * ネゴしていない場合は０が返却されます。
	 *
	 * @return クライアントとネゴシエイトしたWidth
	 * @since	UDC1.2
	 */
	public int getWidth() 
	{
		return width;
	}

	/**
	 * クライアントとネゴシエイトしたHeightを返却します。
	 * ネゴしていない場合は０が返却されます。
	 *
	 * @return クライアントとネゴシエイトしたHeight
	 * @since	UDC1.2
	 */
	public int getHeight() 
	{
		return height;
	}

	/**
	 * telnetクライアントソケットをクローズします。
	 *
	 * @since UDC1.1
	 */
	public void close()
			throws IOException
	{
		if (socket !=null) {
			socket.close();
			socket = null;
		}
	}

	/**
	 * telnet入力ストリームに受信されている受信データ長を取得する。
	 *
	 * @return telnet入力ストリームに受信されている受信データ長
	 * @exception IOException ソケット例外
	 * @since UDC1.1
	 */
	public int available()
			throws IOException
	{
		return is.available();
	}

	/**
	 * telnet入力ストリームから受信されているデータを読み出す。
	 *
	 * @return 受信バイト列
	 * @exception IOException ソケット例外
	 * @since UDC1.1
	 */
	public byte[] receive()
			throws IOException
	{
		int len = is.read(received_buf);
		if (len < 0) {
			return null;	
		}
		return handleProtcol(received_buf,len);
	}

	/**
	 * telnet出力ストリームに送信バイト列を書き出す。
	 *
	 * @param buf 送信バイト列
	 * @since UDC1.1
	 */
	public void send(byte[] buf)
			throws IOException
	{
		os.write(buf);
		os.flush();
	}

	/**
	 * telnet出力ストリームに送信バイトを書き出す。
	 *
	 * @param b 送信バイト
	 * @since UDC1.1
	 */
	public void send(byte b)
			throws IOException
	{
		os.write(b);
		os.flush();
	}

	/**
	 * telnet出力ストリームにプロトコル情報を書き出す。
	 *
	 * @param para1 IAC要求情報１
	 * @param para2 IAC要求情報２
	 * @exception IOException ソケット例外
	 * @since UDC1.1
	 */
	public void sendIAC(byte para1, byte para2)
			throws IOException
	{
		byte[] b = new byte[3];
		b[0] = IAC;
		b[1] = para1;
		b[2] = para2;
		send(b);
	}

	/**
	 * ソケットから受信情報からプロトコルハンドリングを行う。
	 *
	 * @param buf	受信バイト列
	 * @param count	受信バイト長
	 * @since UDC1.1
	 */
	private byte[] handleProtcol(byte buf[], int count)
			throws IOException
	{
		byte nbuf[] = new byte[count];
		byte sbbuf[] = new byte[count];
		int  sbcount = 0;

		byte b,reply;
		int noffset = 0;
		int boffset = 0;
		for (boffset=0; boffset<count; boffset++) {
				// バイト補正
			if ((b=buf[boffset]) >= 128) {
				b = (byte)((int)b-256);
			}
				// telnetプロトコル状態に応じた受信処理
			switch (protcolStatus) {
			case STATE_DATA:
				if (b == IAC) { protcolStatus = STATE_IAC; }
				else 		  { nbuf[noffset++] = b; }
				break;
			case STATE_IAC:
				switch (b) {
				case IAC:
					protcolStatus = STATE_DATA;
					nbuf[noffset++] = IAC;
					break;
				case SB:
					protcolStatus = STATE_IACSB;
					sbcount = 0;
					break;
				case WILL:
				case WONT:
				case DONT:
				case DO:
				case EOR:
				default:
					UdcTrace.trace(UdcTrace.Level, "UdcTelnetServer.handleProtcol - Status STATE_IAC , no support [" + b + "]");
					protcolStatus = STATE_DATA;
					break;
				}
				break;
			case STATE_IACSB:
				switch (b) {
				case IAC:
					current_sb	= 0;
					sbcount = 0;
					protcolStatus = STATE_IAC;
					break;
				case SB:
					protcolStatus = STATE_IACSB;
					current_sb	= 0;
					sbcount = 0;
					break;
				default:
					current_sb	= b;
					sbcount		= 0;
					protcolStatus	= STATE_IACSBDATA;
					break;
				}
				break;
			case STATE_IACSBDATA:
				switch (b) {
				case IAC:
					current_sb	= 0;
					sbcount = 0;
					protcolStatus = STATE_IAC;
					break;
				case SE:
					switch (current_sb) {
					case TELOPT_NAWS:
						if (sbcount < 4) { break; }
						width = sbbuf[1];	// width
						height = sbbuf[3];	// height
						break;
					}
					protcolStatus = STATE_DATA;
					break;
				default:
					sbbuf[sbcount++] = b;
					break;
				}
				break;
			default:
				UdcTrace.trace(UdcTrace.Level, "UdcTelnetServer.handleProtcol - connectStatus unknown [" + protcolStatus + "]");
				protcolStatus = STATE_DATA;
				break;
			}
		}

		buf	= new byte[noffset];
		System.arraycopy(nbuf,0, buf,0, noffset);
		return buf;
	}
}
