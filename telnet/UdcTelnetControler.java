/* *********************************************************************
 * @(#)UdcTelnetControler.java 1.1, 25 Sep 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.io.*;
import java.util.*;

/**
 * telnetクライアントソケット(IOクラス)の制御クラス。
 * telnetアクセスは本クラスを使用して行います。
 *
 * @author  Takayuki Uchida
 * @version 1.1, 25 Sep 2003
 * @see 	UdcTelnet
 * @since   UDC1.1
 */
public class UdcTelnetControler implements UdcTelnetConstants
{
	/**
	 * telnetクライアントのIOクラス。
	 */
	UdcTelnet telnet;

	/**
	 * ログインホスト名。
	 */
	private String loginHost = null;

	/**
	 * ログインホスト名。
	 */
	private int loginPort = WellKnownPort;

	/**
	 * ログイン名。
	 */
	private String loginName = null;

	/**
	 * ログインパスワード。
	 */
	private String loginPasswd = null;

	/**
	 * ログイン入力待ちプロンプト
	 */
	private String promptLoginWait = "login:";

	/**
	 * パスワード入力待ちプロンプト
	 */
	private String promptPasswdWait = "Password:";

	/**
	 * 文字コード設定
	 */
	private String charset = null;


	/** 
	 * コンストラクタ
	 *
	 * @since UDC1.1
	 */
	public UdcTelnetControler()
	{
	}

	/** 
	 * コンストラクタ
	 *
	 * @param chrset	文字コード設定
	 * @since UDC1.1
	 */
	public UdcTelnetControler(String chrset)
	{
		charset = chrset;
	}

	/** 
	 * ログイン入力待ちプロンプトの設定を変更する。<br>
	 * デフォルトは[login:]です。
	 *
	 * @param pmt	ログイン入力待ちプロンプト
	 * @since UDC1.1
	 */
	public void setPromptLoginWait(String pmt)
	{
		promptLoginWait = pmt;
	}

	/** 
	 * パスワード入力待ちプロンプトの設定を変更する。<br>
	 * デフォルトは[Password:]です。
	 *
	 * @param pmt	パスワード入力待ちプロンプト
	 * @since UDC1.1
	 */
	public void setPromptPasswdWait(String pmt)
	{
		promptPasswdWait = pmt;
	}

	/** 
	 * telnetコネクションを接続し、ログインする。
	 *
	 * @param host	接続先ホスト名
	 * @param loginname ログイン名
	 * @param loginpasswd ログインパスワード
	 * @since UDC1.1
	 */
	public void open(String host, String loginname, String loginpasswd)
			throws IOException , UnsupportedEncodingException
	{
		open(host,WellKnownPort,loginname,loginpasswd);
	}

	/** 
	 * telnetコネクションを接続し、ログインする。本メンバはログインプロンプトが
	 * 表示されずにパスワードプロンプトが出力されるサーバに対して使用します。
	 * 本メソッドでは、パスワードの入力までを行います。ログイン結果については、
	 * 本メソッド終了後、receive/waitメソッドによって行ってください。<br>
	 * <p>本メソッド内でログイン結果の確認を行わないのは、telnetサーバによってログイン
	 * 完了後に出力されるプロンプトが異なるためです。
	 *
	 * @param host	接続先ホスト名
	 * @param port	接続先ポート番号
	 * @param loginpasswd ログインパスワード
	 * @since UDC1.1
	 */
	public void open(String host, int port, String loginpasswd)
			throws IOException , UnsupportedEncodingException
	{
		open(host,port,null,loginpasswd);
	}

	/** 
	 * telnetコネクションを接続し、ログインする。
	 * 本メソッドでは、パスワードの入力までを行います。ログイン結果については、
	 * 本メソッド終了後、receive/waitメソッドによって行ってください。<br>
	 * <p>本メソッド内でログイン結果の確認を行わないのは、telnetサーバによってログイン
	 * 完了後に出力されるプロンプトが異なるためです。
	 *
	 * @param host	接続先ホスト名
	 * @param port	接続先ポート番号
	 * @param loginname ログイン名
	 * @param loginpasswd ログインパスワード
	 * @since UDC1.1
	 */
	public void open(String host, int port, String loginname, String loginpasswd)
			throws IOException , UnsupportedEncodingException
	{
		if (telnet != null) {
			throw new IOException("already open.");
		}
		loginHost = host;
		loginPort = port;
		loginName = loginname;
		loginPasswd = loginpasswd;
			// telnet connection open
		telnet = new UdcTelnet();
		telnet.connect(loginHost,loginPort);
			// telnet login
		if (loginName != null) {
			if (wait( promptLoginWait, 10*1000) == null) {
				throw new IOException("login prompt is mismatch?");
			}
			send(loginName + "\n");
		}
			// telnet passwd
		if (loginPasswd != null) {
			if (wait( promptPasswdWait, 10*1000) == null) {
				throw new IOException("password prompt is mismatch?");
			}
			send(loginPasswd + "\n");
		}
	}

	/** 
	 * telnetコネクションを切断する。
	 *
	 * @since UDC1.1
	 */
	public void close()
			throws IOException
	{
		if(telnet != null) {
			telnet.disconnect();
		}
		telnet = null;
	}

	/** 
	 * telnetサーバからのメッセージを受信します。
	 * 内部ではソケット受信でブロック状態となります。
	 *
	 * @return 応答メッセージ文字列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String receive()
			throws IOException , UnsupportedEncodingException
	{
		String s = new String(receiveBytes(-1));
		return s;
	}

	/** 
	 * telnetサーバからのメッセージを受信します。
	 * 内部ではソケット受信でブロック状態となります。
	 *
	 * @return 応答メッセージ文字列
	 * @param timeout token文字列を受信しなかった場合の受信タイムアウト時間(単位：ミリ秒)
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String receive(long timeout)
			throws IOException , UnsupportedEncodingException
	{
		byte[] recdata;
		if (charset == null) {
			recdata = receiveBytes(timeout);
		} else {
			recdata = receiveBytes(timeout);
		}
		if (recdata == null) {
			return null;
		}	
		return new String(recdata,charset);
	}

	/** 
	 * telnetサーバからのメッセージを受信します。
	 * 内部ではソケット受信でブロック状態となります。
	 *
	 * @return 応答メッセージバイト列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public byte[] receiveBytes()
			throws IOException
	{
		return telnet.receive();
	}

	/** 
	 * telnetサーバからのメッセージを受信します。
	 * 内部ではソケット受信でブロック状態となります。
	 *
	 * @return 応答メッセージバイト列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public byte[] receiveBytes(long timeout)
			throws IOException
	{
		long deadline = 0;
		if(timeout >= 0) {
			deadline = new Date().getTime() + timeout;
		}

		boolean waiton = true;
		while (waiton) {
			if(timeout >= 0) {
				while(available() <= 0) {
					if (telnet == null) {
						return null;
					}
					if(new Date().getTime() > deadline) {
						return null;
					}
					try{
						Thread.currentThread().sleep(100);
					} catch(InterruptedException ignored) {}
				}
			}
			break;
		}
		return telnet.receive();
	}

	/**
	 * telnetサーバからの応答メッセージ中にパラメータで指定する
	 * token文字列が含まれるまで応答を待ちます。
	 *
	 * @return 応答文字列
	 * @param token telnetサーバからの期待応答属性
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String wait(String token)
			throws IOException , UnsupportedEncodingException
	{
		String[] tokens = new String[1];	
		tokens[0] = token;
		return wait(tokens,-1);
	}

	/**
	 * telnetサーバからの応答メッセージ中にパラメータで指定する
	 * token文字列配列のいづれが含まれるまで応答を待ちます。
	 *
	 * @return 応答文字列
	 * @param token telnetサーバからの期待応答属性列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String wait(String[] token)
			throws IOException , UnsupportedEncodingException
	{
		return wait(token,-1);
	}

	/**
	 * telnetサーバからの応答メッセージ中にパラメータで指定する
	 * token文字列が含まれるまで応答を待ちます。timoutで指定した
	 * 時間内にtokenを含む文字列を受信できない場合には、nullを返却
	 * する。
	 *
	 * @return 応答文字列
	 * @param token telnetサーバからの期待応答属性
	 * @param timeout token文字列を受信しなかった場合の受信タイムアウト時間(単位：ミリ秒)
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String wait(String token, long timeout)
			throws IOException , UnsupportedEncodingException
	{
		String[] tokens = new String[1];	
		tokens[0] = token;
		return wait(tokens,timeout);
	}

	/** 
	 * telnetサーバからの応答メッセージ中にパラメータで指定する
	 * token文字列配列のいづれかが含まれるまで応答を待ちます。timoutで指定した
	 * 時間内にtokenを含む文字列を受信できない場合には、nullを返却
	 * する。
	 *
	 * @return 応答文字列
	 * @param token telnetサーバからの期待応答属性列
	 * @param timeout token文字列を受信しなかった場合の受信タイムアウト時間(単位：ミリ秒)
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public String wait(String[] token, long timeout)
			throws IOException, UnsupportedEncodingException
	{
		String res = "";
		long deadline = 0;
		if(timeout >= 0) {
			deadline = new Date().getTime() + timeout;
		}

		int i;
		boolean waiton = true;
		while (waiton) {
			if (timeout >= 0) {
				while(available() <= 0) {
					if (telnet == null) {
						return null;
					}
					if(new Date().getTime() > deadline) {
						return null;
					}
					try{ Thread.currentThread().sleep(100); } catch(InterruptedException ignored) {}
				}
			}
			res += receive();
			for (i=0; i<token.length; i++) {
				if (res.indexOf(token[i]) != -1) {
					waiton = false;
					break;
				}
			}
		}
		return res;
	}

	/** 
	 * telnetソケットでメッセージを受信しているか否かを取得する。
	 *
	 * @return telnetソケットでメッセージを受信しているか否かを取得する。
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public int available()
			throws IOException
	{
		if (telnet == null) {
			return -1;
		}
		return telnet.available();
	}

	/** 
	 * telnetサーバに文字列を送信する。
	 *
	 * @param s	送信文字列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public void send(String s)
			throws IOException , UnsupportedEncodingException
	{
		byte[] buf;
		if (charset != null) {
			buf = s.getBytes(charset);
		} else {
			buf = s.getBytes();
		}
		send(buf);
	}

	/** 
	 * telnetサーバにバイト列を送信する。
	 *
	 * @param s	送信バイト列
	 * @exception IOExceptelnet telnet用ソケットに異常が発生する。
	 * @since UDC1.1
	 */
	public void send(byte[] s)
			throws IOException
	{
		telnet.send(s);
	}

}

