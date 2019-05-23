/* *********************************************************************
 * @(#)UdcTelnet.java 1.1, 25 Sep 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.awt.Dimension;

import udc.util.UdcTrace;

/**
 * TelnetクライアントのIOクラス。本クラスではtelnetのソケット
 * 通信等の低レベルインタフェースを提供します。
 *
 * @author  Takayuki Uchida
 * @version 1.1, 25 Sep 2003
 * @see 	UdcTelnetControler
 * @since   UDC1.0
 */
public class UdcTelnet implements UdcTelnetConstants, UdcTelnetStateChangeFace
{
	/**
	 * プロトコル状態。
	 * sinze UDC1.1
	 */
	private final static byte STATE_DATA			= 0;
	private final static byte STATE_IAC				= 1;
	private final static byte STATE_IACSB			= 2;
	private final static byte STATE_IACWILL			= 3;
	private final static byte STATE_IACDO			= 4;
	private final static byte STATE_IACWONT			= 5;
	private final static byte STATE_IACDONT			= 6;
	private final static byte STATE_IACSBIAC		= 7;
	private final static byte STATE_IACSBDATA		= 8;
	private final static byte STATE_IACSBDATAIAC	= 9;

	/**
	 * 現在のプロトコル状態。 
	 * sinze UDC1.1
	 */
	private byte protcolStatus = 0;

	private byte current_sb;	/* IAC SB <xx> 受信時の受信バイト */
	private byte[] receivedDX;	/* IAC DO(NT) 受信時の受信バイト */
	private byte[] receivedWX;	/* IAC WILL/WONT 受信時の受信バイト */
	private byte[] sentDX;		/* IAC DO/DONT 送信時の送信バイト */
	private byte[] sentWX;		/* IAC WILL/WONT 送信時の送信バイト */

	/**
	 * telnetソケット。
	 * @since	UDC1.1
	 */
	private Socket socket;

	/**
	 * telnetソケットの入力ストリーム。
	 * @since	UDC1.1
	 */
	private BufferedInputStream is;

	/**
	 * telnetソケットの出力ストリーム。
	 * @since	UDC1.1
	 */
	private BufferedOutputStream os;

	/**
	 * ターミナル状態変化ハンドラー。
	 * @since	UDC1.1
	 */
	private UdcTelnetStateChangeFace stcHandler = null;


	/**
	 * telnetクライアントソケットをデフォルトポート23で接続します。
	 *
	 * @param	address	接続するサーバアドレス
	 * @since	UDC1.1
	 */
	public void connect(String address)
			throws IOException
	{
		connect(address, WellKnownPort);
	}

	/**
	 * telnetクライアントソケットを接続します。
	 *
	 * @param	address	接続するサーバアドレス
	 * @param	port	接続するサーバのポート番号
	 * @since	UDC1.1
	 */
	public void connect(String address, int port)
			throws IOException
	{
		socket = new Socket(address, port);
		is = new BufferedInputStream(socket.getInputStream());
		os = new BufferedOutputStream(socket.getOutputStream());
		protcolStatus = 0;

		receivedDX = new byte[256];
		sentDX = new byte[256];
		receivedWX = new byte[256];
		sentWX = new byte[256];
	}

	/**
	 * telnetクライアントソケットをクローズします。
	 *
	 * @since UDC1.1
	 */
	public void disconnect()
			throws IOException
	{
		if (socket !=null) {
			socket.close();
		}
	}

	/**
	 * ターミナル状態変化ハンドラーを設定する。
	 *
	 * @param handler	
	 * @since UDC1.1
	 */
	public void setStateChangeHandler(UdcTelnetStateChangeFace handler)
	{
		stcHandler = handler;
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
	 * @return 受信バイト長
	 * @exception IOException ソケット例外
	 * @since UDC1.1
	 */
	public byte[] receive()
			throws IOException
	{
		int len = is.available();
		byte buf[] = new byte[len];
		len = is.read(buf);
		if (len < 0) {
			throw new IOException("Connection closed.");
		}
		return handleProtcol(buf,len);
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
	 * IAC-SB受信時のプロトコルハンドリングを行う。
	 * (*)IAC-SBは以下のような形式でネゴシエイトされます。 <br>
	 * 	⇒ IAC SB <type> <bytes> IAC SE
	 *
	 * @param type SB種別
	 * @param sbata SBデータ
	 * @param sbcount SBデータ長
	 */
	private void handleSB(byte type, byte[] sbdata, int sbcount)
		throws IOException
	{
		byte[] b;
		switch (type) {
		case TELOPT_TTYPE:
			if (sbcount>0 && sbdata[0]==TELQUAL_SEND) {
				b = new byte[2];
				b[0] = IAC;
				b[1] = SB;
				send(b);
				send(TELOPT_TTYPE);
				send(TELQUAL_IS);
				/* 
				 * FIXME: need more logic here if we use
				 * more than one terminal type
				 */
				String ttype = (String)handleStateChange(STC_TTYPE);
				if(ttype == null) {
					ttype = "dumb";
				}
				byte[] bttype = new byte[ttype.length()];
				ttype.getBytes(0,ttype.length(), bttype, 0);
				send(bttype);
				b = new byte[2];
				b[0] = IAC;
				b[1] = SE;
				send(b);
			}

		}
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
				case WILL:
					protcolStatus = STATE_IACWILL;
					break;
				case WONT:
					protcolStatus = STATE_IACWONT;
					break;
				case DONT:
					protcolStatus = STATE_IACDONT;
					break;
				case DO:
					protcolStatus = STATE_IACDO;
					break;
				case EOR:
					protcolStatus = STATE_DATA;
					break;
				case SB:
					protcolStatus = STATE_IACSB;
					sbcount = 0;
					break;
				default:
					UdcTrace.trace(UdcTrace.Level, "UdcTelnet.handleProtcol - Status STATE_IAC , Unknown code [" + b +"]");
					protcolStatus = STATE_DATA;
					break;
				}
				break;
			case STATE_IACWILL:
				switch(b) {
				case TELOPT_ECHO:
					reply = DO;
					handleStateChange(STC_NOLOCALECHO);
					break;
				case TELOPT_EOR:
					reply = DO;
					break;
				default:
					reply = DONT;
					break;
				}
				if (reply != sentDX[b+128] || WILL != receivedWX[b+128]) {
					sendIAC(reply,b);
					sentDX[b+128] = reply;
					receivedWX[b+128] = WILL;
				}
				protcolStatus = STATE_DATA;
				break;
			case STATE_IACWONT:
				switch(b) {
				case TELOPT_ECHO:
					handleStateChange(STC_LOCALECHO);
					reply = DONT;
					break;
				case TELOPT_EOR:
					reply = DONT;
					break;
				default:
					reply = DONT;
					break;
				}
				if (reply != sentDX[b+128] || WONT != receivedWX[b+128]) {
					sendIAC(reply,b);
					sentDX[b+128] = reply;
					receivedWX[b+128] = WILL;
				}
				protcolStatus = STATE_DATA;
				break;
			case STATE_IACDO:
				switch (b) {
				case TELOPT_ECHO:	// ECHO
					reply = WILL;
					handleStateChange(STC_LOCALECHO);
					break;
				case TELOPT_TTYPE:	// TTYPE
					reply = WILL;
					break;
				case TELOPT_NAWS: 	// NAWS
					Dimension size = (Dimension)handleStateChange(STC_NAWS);
					receivedDX[b] = DO;
					if(size == null) { // this shouldn't happen
						send(IAC);
						send(WONT);
						send(TELOPT_NAWS);
						reply = WONT;
						sentWX[b] = WONT;
						break;
					}
					reply = WILL;
					sentWX[b] = WILL;
					sendIAC(WILL,TELOPT_NAWS);
					send(IAC);
					send(SB);
					send(TELOPT_NAWS);
					send((byte) (size.width >> 8));
					send((byte) (size.width & 0xff));
					send((byte) (size.height >> 8));
					send((byte) (size.height & 0xff));
					send(IAC);send(SE);
					break;
				default:
					UdcTrace.trace(UdcTrace.Level, "UdcTelnet.handleProtcol - Status IAC_DO , Unknown code [" + b + "]");
					reply = WONT;
					break;
				}
				if (reply != sentWX[128+b] || DO != receivedDX[128+b]) {
					sendIAC(reply,b);
					sentWX[b+128] = reply;
					receivedDX[b+128] = DO;
				}
				protcolStatus = STATE_DATA;
				break;
			case STATE_IACDONT:
				switch (b) {
				case TELOPT_ECHO:	// NOLOCAL ECHO
					reply = WONT;
					handleStateChange(STC_NOLOCALECHO);
					break;
				case TELOPT_NAWS:	// NAWS
					reply = WONT;
					break;
				default:
					UdcTrace.trace(UdcTrace.Level, "UdcTelnet.handleProtcol - Status IAC_DONT , Unknown code [" + b + "]");
					reply	= WONT;
					break;
				}
				if (reply != sentWX[b+128] || DONT != receivedDX[b+128]) {
					send(IAC);
					send(reply);
					send(b);
					sentWX[b+128]		= reply;
					receivedDX[b+128]	= DONT;
				}
				protcolStatus = STATE_DATA;
				break;
			case STATE_IACSBIAC:
				if (b == IAC) {
					sbcount		= 0;
					current_sb	= b;
					protcolStatus	= STATE_IACSBDATA;
				} else {
					UdcTrace.trace(UdcTrace.Level, "UdcTelnet.handleProtcol - Status IACSBIAC , bad code [" + b + "]");
					protcolStatus = STATE_DATA;
				}
				break;
			case STATE_IACSB:
				switch (b) {
				case IAC:
					protcolStatus = STATE_IACSBIAC;
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
					protcolStatus = STATE_IACSBDATAIAC;
					break;
				default:
					sbbuf[sbcount++] = b;
					break;
				}
				break;
			case STATE_IACSBDATAIAC:
				switch (b) {
				case IAC:
					protcolStatus = STATE_IACSBDATA;
					sbbuf[sbcount++] = IAC;
					break;
				case SE:
					handleSB(current_sb,sbbuf,sbcount);
					current_sb	= 0;
					protcolStatus	= STATE_DATA;
					break;
				case SB:
					handleSB(current_sb,sbbuf,sbcount);
					protcolStatus	= STATE_IACSB;
					break;
				default:
					protcolStatus	= STATE_DATA;
					break;
				}
				break;
			default:
				UdcTrace.trace(UdcTrace.Level, "UdcTelnet.handleProtcol - connectStatus unknown [" + protcolStatus +"]");
				protcolStatus = STATE_DATA;
				break;
			}
		}

		buf	= new byte[noffset];
		System.arraycopy(nbuf,0, buf,0, noffset);
		return buf;
	}

	/**
	 * ハンドリングされたターミナル状態変化を処理する。
	 * 本メンバメソッドから UdcTelnetStateChangeFace(ターミナル状態変化通知ハンドラー)
	 * のhandleStateChangeがコールされる。
	 *
	 * @param state 変化した状態
	 * @see	UdcTelnetStateChangeFace
	 * @since UDC1.1
	 */
	public Object handleStateChange(String state)
	{
		if(stcHandler != null) {
			return stcHandler.handleStateChange(state);
		}
		return null;
	}
}
