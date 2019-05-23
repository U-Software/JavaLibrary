/* *********************************************************************
 * @(#)UdcTelnetServerControler.java 1.2, 25 Mar 2005
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.telnet;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.InetAddress;

import udc.util.UdcMutex;
import udc.util.UdcTrace;


/**
 * telnetサーバソケット(IOクラス)の制御クラス。
 * telnetサーバ処理はは本クラスからユーザ指定された、UdcTelnetServerReceiveProcFaceを通して
 * 実現されます。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 25 Mar 2005
 * @see 	UdcTelnetServer
 * @see 	UdcTelnetServerManager
 * @since   UDC1.2
 */
public class UdcTelnetServerControler implements UdcTelnetConstants, Runnable
{
	/**
	 * telnetサーバのIOクラス。
	 */
	UdcTelnetServer telnet;

	/**
	 * ユーザ処理インタフェース。
	 */
	UdcTelnetServerReceiveProcFace 	userProc;

	/**
	 * クライアントからの入力受付処理を行うスレッド。
	 * @since	UDC1.2
	 */
	protected Thread	thread = null;

	/**
	 * クライアントからの入力受付処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.2
	 */
	protected boolean	threadLoop = true;

	/**
	 * startメソッドによってクライアントからの入力受付処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.2
	 */
	protected boolean 	isStartComplete = false;

	/**
	 * TelnetServer管理インスタンス。
	 * @since	UDC1.2
	 */
	UdcTelnetServerManager	manager;


	/** 
	 * コンストラクタ
	 *
	 * @param	socket	クライアント接続されたソケット	
	 * @param	userproc	ユーザ処理インタフェース	
	 * @param	mgr		TelnetServer管理インスタンス
	 * @since UDC1.1
	 */
	public UdcTelnetServerControler(Socket socket, UdcTelnetServerReceiveProcFace userproc, UdcTelnetServerManager mgr)
			throws IOException
	{
		manager = mgr;
		userProc = userproc;
		telnet = new UdcTelnetServer(socket);
	}


	/**
	 * Telnetクライアント入力監視スレッドが起動したか否かを取得する。
	 *
	 * @return	Telnetクライアント入力監視スレッドが完了したか否か
	 * @since	UDC1.2
	 */
	protected boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * クライアントからの入力受付機能を活性化(起動)する。
	 *
	 * @since	UDC1.2
	 */
	public void start()
		throws IOException
	{
		if (thread != null) {
			return;
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
	 * クライアントからの入力受付処理機能を停止する。
	 *
	 * @since	UDC1.2
	 */
	public void stop()
		throws IOException
	{
		threadLoop = false;
		synchronized (manager.serverCtlList) {
			manager.serverCtlList.remove(this);
		}
		try { telnet.close(); } catch(Exception exp) {}

		if (thread.isAlive()) {
			thread.stop();
		}
		thread = null;
	}

	/**
	 * クライアントからの入力受付処理を行うスレッド。
	 *
	 * @since	UDC1.2
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - start.");
		isStartComplete = true;
		
		byte rec[];
		int ret,pos;
		String str, instr = "";
		try {
			if (userProc.setTelnet(this) != 0) {
				UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - end of connection becase setTelnet failed.");
				try { telnet.close(); } catch(Exception exp) {}
				synchronized (manager.serverCtlList) {
					manager.serverCtlList.remove(this);
				}
				threadLoop = false;
			}

			while (threadLoop) {
				rec = telnet.receive();
				if (rec == null) {
					ret = -10000;
				} else {
					instr += new String(rec);
					ret = 0;
					while (true) {
						instr = instr.replaceAll("\r", "\n");
						instr = instr.replaceAll("\n\n", "\n");
						if ((pos=instr.indexOf("\n")) < 0) {
							break;
						}
						str = instr.substring(0, pos);
						if ((ret=userProc.receive(str)) != 0) {
							break;
						}
						instr = instr.substring(pos+1);
					}
				}
				if (ret != 0) {
					UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - end of connection.");
					try { telnet.close(); } catch(Exception exp) {}
					synchronized (manager.serverCtlList) {
						manager.serverCtlList.remove(this);
					}
					break;
				}
			}
		} catch(IOException ioexp) {
			UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - end of connection becase exception, " + ioexp);
			try { telnet.close(); } catch(Exception exp) {}
			synchronized (manager.serverCtlList) {
				manager.serverCtlList.remove(this);
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - stop.");
			throw death;
		}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcTelnetServerControler.run", " - end.");
	}

	/**
	 * 本インスタンスの文字列情報を取得する。
	 *
	 * @return	本インスタンス情報
	 * @since	UDC1.0
	 */
	public String toString()
	{
		InetAddress src = telnet.socket.getInetAddress();
		String str = " ConnectTime:"
						+ telnet.conntime.get(Calendar.YEAR) + "."
						+ (telnet.conntime.get(Calendar.MONTH)+1) + "."
						+ telnet.conntime.get(Calendar.DATE) + " "
						+ telnet.conntime.get(Calendar.HOUR) + ":"
						+ telnet.conntime.get(Calendar.MINUTE) + "."
						+ telnet.conntime.get(Calendar.SECOND)
						+ "  SrcAddr:" + src.toString() + "\n";
		return str;
	}

	/** 
	 * telnetクライアントに文字列を返信する。
	 *
	 * @param s	送信文字列
	 * @since UDC1.2
	 */
	public void send(String s)
			throws IOException
	{
		byte buf[] = s.getBytes();
		telnet.send(buf);
	}
}

