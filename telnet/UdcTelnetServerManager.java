/* *********************************************************************
 * @(#)UdcTelnetServerManager.java 1.2, 25 Mar 2005
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.lang.*;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.awt.Dimension;

import udc.util.UdcMutex;
import udc.util.UdcTrace;


/**
 * TelnetサーバーのListenポート管理クラス。本クラスではtelnetサーバの
 * Listenソケット通信等の低レベルインタフェースを提供し、クライアントとの
 * 接続が確立したらUdcTeletnetServerクラスを生成します。<br>
 * UdcTeletnetServerクラスはTelnetクライアントからの要求を受信し、
 * 本クラスのコンストラクタで指定したUdcTelnetServerReceiveProcFaceで受信情報に対応した処理を
 * 実装します。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 25 Mar 2005
 * @see 	UdcTelnetServer
 * @see 	UdcTelnetServerControler
 * @see 	UdcTelnetServerReceiveProcFace
 * @since   UDC1.2
 */
public class UdcTelnetServerManager implements UdcTelnetConstants , Runnable
{
	/**
	 * クライアントからの接続受付処理を行うスレッド。
	 * @since	UDC1.2
	 */
	protected Thread	thread = null;

	/**
	 * クライアントからの接続受付処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.2
	 */
	protected boolean	threadLoop = true;

	/**
	 * startメソッドによってクライアントからの接続受付処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.2
	 */
	protected boolean 	isStartComplete = false;

	/**
	 * telnetサーバーソケット。
	 * @since	UDC1.2
	 */
	protected ServerSocket socket = null;

	/**
	 * telnet接続最大数
	 * @since	UDC1.2
	 */
	protected int serverCtlListMax = -1;

	/**
	 * telnet接続リスト。
	 * @since	UDC1.2
	 */
	protected LinkedList serverCtlList;

	/**
	 * telnet接続クラスのユーザ処理インタフェース。
	 * @since	UDC1.2
	 */
	protected UdcTelnetServerReceiveProcFace userProc;


	/**
	 * コンストラクタ。
	 *
	 * @param	userproc	Telnetサーバのユーザ処理インタフェース
	 * @since	UDC1.2
	 */
	public UdcTelnetServerManager(UdcTelnetServerReceiveProcFace userproc)
	{
		serverCtlList = new LinkedList();
		userProc = userproc;
	}
	
	/**
	 * コンストラクタ。
	 *
	 * @param	userproc	Telnetサーバのユーザ処理インタフェース
	 * @param	connMax		Telnet接続最大数
	 * @since	UDC1.2
	 */
	public UdcTelnetServerManager(UdcTelnetServerReceiveProcFace userproc, int connMax)
	{
		this(userproc);

		serverCtlListMax = connMax;
	}


	/**
	 * サーバListenポート監視スレッドが起動したか否かを取得する。
	 *
	 * @return	サーバListenポート監視スレッドが完了したか否か
	 * @since	UDC1.2
	 */
	protected boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * クライアントからの接続受付機能を活性化(起動)する。
	 * クライアントからの接続受付機能は専用スレッドにおいて実現されるため、
	 * サーバソケットをオープンし、スレッドの起動を行う。
	 *
	 * @param	port	サーバのポート番号
	 * @since	UDC1.2
	 */
	public void start(int port)
		throws IOException
	{
		start(null, port, 0);
	}

	/**
	 * クライアントからの接続受付機能を活性化(起動)する。
	 * クライアントからの接続受付機能は専用スレッドにおいて実現されるため、
	 * サーバソケットをオープンし、スレッドの起動を行う。
	 *
	 * @param	address	サーバアドレス
	 * @param	port	サーバのポート番号
	 * @param	listen	同時接続要求受付キュー長(ServerSocketのbacklog)
	 * @since	UDC1.2
	 */
	public void start(String address, int port, int listen)
		throws IOException
	{
		if (thread != null) {
			return;
		}

		if (address != null) {
			open(address, port, listen);
		} else {
			open(port);
		}

		UdcMutex mute = new UdcMutex();
		threadLoop = true;
		thread = new Thread(this);
		thread.start();
		while (! isStartComplete) {
			try { mute.sleep(20); } catch(InterruptedException exp) {}
		}
	}

	/**
	 * クライアントからの接続受付処理機能を停止する。
	 * クライアントからの接続受付機能は専用スレッドにおいて実現されるため、そのスレッドの停止を行う。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		threadLoop = false;
		try { close(); } catch(Exception exp) {}
		if (thread.isAlive()) {
			thread.stop();
		}
		thread = null;
	}

	/**
	 * クライアントからの接続受付処理を行うスレッド。
	 *
	 * @since	UDC1.1
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - start.");
		isStartComplete = true;
		
		try {
			UdcTelnetServerControler ctl;
			UdcTelnetServerReceiveProcFace userproc;

			while (threadLoop) {
				Socket conn = socket.accept();
				if (conn == null) {
					UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - disconnect.");
					try { conn.close(); } catch(Exception exp) {}
					break;
				}
				if (serverCtlListMax > 0) {
					synchronized (serverCtlList) {
						if (serverCtlListMax <= serverCtlList.size()) {
							UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - max connection.");
							try { conn.close(); } catch(Exception exp) {}
							break;
						}
					}
				}

				try {
					userproc = (UdcTelnetServerReceiveProcFace)userProc.clone();
				} catch(Exception exp) {
					UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - user procedure is undefine.");
					try { conn.close(); } catch(Exception connexp) {}
					break;
				}

				ctl = null;
				try {
					ctl = new UdcTelnetServerControler(conn, userproc, this);
					ctl.start();
				} catch(Exception exp) {
					if (ctl != null) {
						try { ctl.stop(); } catch(Exception ctlexp) {}
					}
					UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - user procedure cannot start.");
					continue;	
				}
				synchronized (serverCtlList) {
					serverCtlList.add(ctl);
				}
			}
		} catch(IOException ioexp) {
			UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - ioexception , " + ioexp);
			try { socket.close(); } catch(Exception sexp) {}

		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - stop.");
			throw death;
		}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerManager.run", " - end.");
	}

	/**
	 * 本インスタンスの文字列情報を取得する。
	 *
	 * @return	本インスタンス情報
	 * @since	UDC1.0
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("ServerSocket: ");	
		if (isStartComplete) {
			buf.append("Enable\n");	
			UdcTelnetServerControler ctl;
			synchronized (serverCtlList) {
				for (int i=0; i<serverCtlList.size(); i++) {
					ctl = (UdcTelnetServerControler)serverCtlList.get(i);
					buf.append("(" + i + ") " + ctl.toString());	
				}
			}
		} else {
			buf.append("Disable\n");	
		}
		return buf.toString();
	}

	/**
	 * telnetサーバのListenポートを確立します。
	 * ソケットをデフォルトポートは23です。
	 *
	 * @param	port	サーバのポート番号
	 * @since	UDC1.2
	 */
	void open(int port)
			throws IOException
	{
		try {
			socket = new ServerSocket(port);
		} catch (Exception exp) {
			String str = "Socket problem - " + exp.getMessage();
			throw (new IOException(str));
		}
	}

	/**
	 * telnetサーバのListenポートを確立します。
	 *
	 * @param	address	サーバアドレス
	 * @param	port	サーバのポート番号
	 * @param	listen	同時接続要求受付キュー長(ServerSocketのbacklog)
	 * @since	UDC1.2
	 */
	void open(String address, int port, int listen)
			throws IOException
	{
		try {
			InetAddress addr = InetAddress.getByName(address);
			socket = new ServerSocket(port, listen, addr);
		} catch (Exception exp) {
			String str = "Socket problem - " + exp.getMessage();
			throw (new IOException(str));
		}
	}

	/**
	 * telnetサーバのListenポートをクローズします。
	 *
	 * @since UDC1.2
	 */
	void close()
			throws IOException
	{
		if (socket !=null) {
			socket.close();
			socket = null;
		}
	}

	/**
	 * telnetサーバへの接続要求をacceptします。
	 *
	 * @since UDC1.2
	 */
	Socket accept() 
			throws IOException
	{
		if (socket == null) {
			return null;
		}
		return socket.accept();
	}
}
