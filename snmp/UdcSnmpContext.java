/* *********************************************************************
 * @(#)UdcSnmpContext.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.net.*;
import java.util.*;

import udc.util.*;


/**
 * SNMP-Manager機能は本クラスによって全てが管理されます。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcSnmpContext implements UdcSnmpConstants , UdcThreadMsgId, Runnable
{
	/**
	 * SNMP UDPメッセージ長デフォルト。
	 * @since	UDC1.0
	 */
	static public int	MaxSize_SnmpMessage = 1300;

	/**
	 * SNMPメッセージ送受信結果：成功（PDU内の異常は本結果とは独立です。）
	 * @since	UDC1.0
	 */
	public final static int		Success = 0;

	/**
	 * SNMPメッセージ送受信結果：応答タイムアウト。
	 * @since	UDC1.0
	 */
	public final static int		Error_Timeout = 1;

	/**
	 * タイマ管理クラス。SNMP応答待ち管理に使用する。
	 * @since	UDC1.0
	 */
	protected UdcTimerManager	timerManager;

	/**
	 * SNMP-Manager 要求/応答制御ソケット管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocketControler	opeSocketControler;

	/**
	 * SNMP-Manager TRAP受信ソケット管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocketControler	trapSocketControler;

	/**
	 * SNMP要求等のrequestIdを生成するためシーケンシャル番号管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcGenerateIndex			generateIndex;

	/**
	 * SNMP応答待ちリスト。
	 * @since	UDC1.0
	 */
	protected UdcSnmpResponseWaitList	waitList;

	/**
	 * SNMP応答待ちリスト。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel		trapSendChannel;

	/**
	 * SNMP-Manager応答処理待ちスレッドチャネル。
	 * SNMP応答は opeSocketControler によって受信され、本チャネルに転送されます。
	 * 本クラスではこれを受信して応答受信処理を行う。また、タイムアウトが発生した場合も
	 * 本チャネルに通知され、応答受信処理の１つとしてタイムアウト処理を行う。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel		channel;

	/**
	 * SNMP-Manager応答スレッドチャネル。
	 * SNMP応答は全て本チャネルにリストされる。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel		replyChannel;

	/**
	 * SNMP-Manager応答処理スレッド。
	 * @since	UDC1.0
	 */
	protected Thread		thread;

	/**
	 * SNMP-Manager処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	protected boolean		threadLoop = true;

	/**
	 * startメソッドによってSNMP-Manager処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	protected boolean		isStartComplete = false;

	/**
	 * コンストラクタ。
	 *
	 * @param	timer　	タイマ管理
	 * @param	addr	SNMP-ManagerIPアドレス
	 * @param	opePort	SNMP-Manager制御用IPポート
	 * @param	trapPort SNMP-Managerトラップ受信用IPポート
	 * @param	trapChannel	トラップ転送用チャネル
	 * @since	UDC1.0
	 */
	public UdcSnmpContext(UdcTimerManager timer, InetAddress addr, int opePort, int trapPort, UdcThreadChannel trapChannel)
		throws IOException
	{
		UdcSnmpOpeReceiver opeReceiver = new UdcSnmpOpeReceiver(this);
		UdcSnmpOpeSender opeSender = new UdcSnmpOpeSender(this);
		UdcSnmpTrapReceiver trapReceiver = new UdcSnmpTrapReceiver(this);

		timerManager = timer;
		opeSocketControler = new UdcDatagramSocketControler(addr,opePort,opeReceiver,opeSender);
		trapSocketControler = new UdcDatagramSocketControler(addr,trapPort,trapReceiver,null);
		generateIndex = new UdcGenerateIndex(1,50000);
		waitList = new UdcSnmpResponseWaitList();
		trapSendChannel = trapChannel;
		channel = new UdcThreadChannel();
		replyChannel = new UdcThreadChannel();
	}

	/**
	 * SNMP-Manager処理(応答受信処理)を開始する。
	 * 本メンバ関数を実行することで、Manager機能が開始される。具体的には
	 * SNMP応答受信処理スレッドが開始される。
	 * 本メンバ関数は一度のみ呼び出すことが可能です。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException, InterruptedException
	{
		if (thread == null) {
			UdcMutex mute = new UdcMutex();
			thread = new Thread(this);
			thread.start();
			opeSocketControler.open();
			trapSocketControler.open();
			if (opeSocketControler.getReceiver() != null) {
				opeSocketControler.getReceiver().start();
			}
			if (opeSocketControler.getSender() != null) {
				opeSocketControler.getSender().start();
			}
			if (trapSocketControler.getReceiver() != null) {
				trapSocketControler.getReceiver().start();
			}
			if (trapSocketControler.getSender() != null) {
				trapSocketControler.getSender().start();
			}
			while (! isStartComplete()) {
				try { mute.sleep(20); } catch(InterruptedException exp) {}
			}
		}
	}

	/**
	 * SNMP-Manager処理(応答受信処理)を停止する。
	 * 本メンバ関数を実行することで、Manager機能が停止される。具体的には
	 * SNMP応答受信処理処理スレッドが停止される。
	 * 本メンバ関数は一度のみ呼び出すことが可能です。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		threadLoop = false;
		if (thread != null) {
			opeSocketControler.close();
			trapSocketControler.close();
			if (opeSocketControler.getReceiver() != null) {
				opeSocketControler.getReceiver().stop();
			}
			if (opeSocketControler.getSender() != null) {
				opeSocketControler.getSender().stop();
			}
			if (trapSocketControler.getReceiver() != null) {
				trapSocketControler.getReceiver().stop();
			}
			if (trapSocketControler.getSender() != null) {
				trapSocketControler.getSender().stop();
			}
			thread.stop();
			thread = null;
		}
	}

	/**
	 * SNMP-Manager処理スレッドが起動したか否かを取得する。
	 *
	 * @return	MIB処理スレッドが完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * SNMP要求メッセージを送信し、応答待ちリストに設定する。
	 * 応答待ちリストに設定すると同時にタイマ設定がなされる。
	 *
	 * @return	要求識別子(負値：異常)
	 * @param	destAddr	送信先SNMP-AgentのIPアドレス
	 * @param	destPort	送信先SNMP-AgentのIPポート
	 * @param	request		要求PDU
	 * @param	milisec		応答待ちタイムアウト時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public int send(InetAddress destAddr, int destPort, UdcPdu request, long milisec)
		throws java.io.IOException, InterruptedException, UdcEncodingException
	{
		return send(destAddr,destPort,request,milisec,replyChannel);
	}

	/**
	 * SNMP要求メッセージを送信し、応答待ちリストに設定する。
	 * 応答待ちリストに設定すると同時にタイマ設定がなされ、応答を受信するかタイムアウトすると、指定したチャネルに結果をpushする。
	 *
	 * @return	要求識別子(負値：異常)
	 * @param	destAddr	送信先SNMP-AgentのIPアドレス
	 * @param	destPort	送信先SNMP-AgentのIPポート
	 * @param	request		要求PDU
	 * @param	milisec		応答待ちタイムアウト時間(単位：ミリ秒)
	 * @param	userChannel	結果受信チャネル
	 * @since	UDC1.0
	 */
	public int send(InetAddress destAddr, int destPort, UdcPdu request, long milisec, UdcThreadChannel userChannel)
		throws java.io.IOException, InterruptedException, UdcEncodingException
	{
		if (thread == null) {
			return -1;
		}
			// エンコード
		request.requestId = generateIndex.allocateIndex();
		byte[] data = request.encode();
			// 応答待ち登録
		int invId = replyChannel.newInvokeId();
		UdcSnmpResponseWait elm = new UdcSnmpResponseWait(request,milisec,userChannel,invId);
		elm.sendIndex = invId;
		waitList.lock();
		waitList.add(elm);
		elm.timerIndex = timerManager.registrateTimer(UdcThreadMsgDtrEventTimeout.Timeout,milisec,channel);
		waitList.unlock();
			// 送信
		DatagramPacket packet = new DatagramPacket(data,data.length,destAddr,destPort);
		opeSocketControler.send(packet);
		return invId;
	}

	/**
	 * SNMP-Trapメッセージを送信する。
	 *
	 * @param	destAddr	トラップ送信先IPアドレス
	 * @param	destPort	トラップ送信先IPポート
	 * @param	trap		Trap-PDU
	 * @since	UDC1.0
	 */
	public void sendTrap(InetAddress destAddr, int destPort, UdcPdu trap)
		throws java.io.IOException, InterruptedException, UdcEncodingException
	{
			// エンコード
		trap.requestId = generateIndex.allocateIndex();
		byte[] data = trap.encode();
			// 送信
		DatagramPacket packet = new DatagramPacket(data,data.length,destAddr,destPort);
		opeSocketControler.send(packet);
	}

	/**
	 * 要求識別子(sendIndex)に対応した応答を受信する。
	 * 本メンバ関数は要求識別子に対応する応答を受信するため戻りません。
	 *
	 * @return	応答PDU
	 * @param	sendIndex	要求識別子
	 * @since	UDC1.0
	 */
	public UdcPdu receive(int sendIndex)
		throws InterruptedException
	{
		UdcPdu respPdu = null;
		UdcThreadChannelDtr dtr = replyChannel.pull(sendIndex,-1);
		respPdu = (UdcPdu)dtr.get_data();
		return respPdu;
	}

	/**
	 * 要求識別子(sendIndex)に対応した応答を受信する。
	 * 本メンバ関数では、応答受信リストから要求識別子に対応する応答が
	 * 存在すれば、それを返却し、そうでなければ null を返却する。
	 *
	 * @return	応答PDU
	 * @param	sendIndex	要求識別子
	 * @since	UDC1.0
	 */
	public UdcPdu receiveTry(int sendIndex)
		throws InterruptedException
	{
		UdcPdu respPdu = null;
		UdcThreadChannelDtr dtr = replyChannel.tryPull(sendIndex,-1);
		if (dtr != null) {
			respPdu = (UdcPdu)dtr.get_data();
			respPdu.result = dtr.get_result();
		}
		return respPdu;
	}

	/**
	 * SNMP要求を送信し、応答を受信する。
	 *
	 * @return	応答PDU
	 * @param	destAddr	送信先SNMP-AgentのIPアドレス
	 * @param	destPort	送信先SNMP-AgentのIPポート
	 * @param	request		要求PDU
	 * @param	milisec		応答待ちタイムアウト時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public UdcPdu sendReceive(InetAddress destAddr, int destPort, UdcPdu request, long milisec)
		throws java.io.IOException, InterruptedException, UdcEncodingException
	{
		int sendIndex = send(destAddr, destPort, request, milisec);
		if (sendIndex < 0) {
			return null;
		}
		return receive(sendIndex);
	}

	/**
	 * SNMP応答受信処理を行うスレッド。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpContext.run", " - start.");
		isStartComplete = true;

		try {
			UdcSnmpResponseWait elm;
			UdcThreadChannelDtr dtr,sdtr;

			while (threadLoop) {
				try {
					dtr = channel.pull();
					int dtrtype = dtr.get_dtrType();
					if (dtrtype == Response_Snmp) {
						UdcPdu respPdu = (UdcPdu)dtr.get_data();
						waitList.lock();
						if ((elm=waitList.searchRequestId(respPdu.getRequestId())) != null) {
							waitList.remove(elm);
						}
						waitList.unlock();
						if (elm != null && elm.replyChannel != null) {
							respPdu.result = UdcSnmpContext.Success;
							sdtr = new UdcThreadChannelDtr(elm.replyInvokeId,-1,respPdu);
							sdtr.set_result(0);
							elm.replyChannel.push( sdtr );
						}
					} else if (dtrtype == UdcThreadMsgId.Event_Timeout) {
						UdcThreadMsgDtrEventTimeout event = (UdcThreadMsgDtrEventTimeout)dtr.get_data();
						waitList.lock();
						if ((elm=waitList.searchTimerId(event.get_timerIndex())) != null) {
							waitList.remove(elm);
						}
						waitList.unlock();
						if (elm != null && elm.replyChannel != null) {
							elm.requestPdu.result = UdcSnmpContext.Error_Timeout;
							sdtr = new UdcThreadChannelDtr(elm.replyInvokeId,-1,elm.requestPdu);
							sdtr.set_result(0);
							elm.replyChannel.push( sdtr );
						}
					} else {
					}
				} catch(InterruptedException exp) {
				}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			try {
				opeSocketControler.close();
				trapSocketControler.close();
			} catch(IOException exp) {}
			UdcTrace.trace(UdcTrace.Level,"UdcSnmpContext.run", " - stop.");
			throw death;
		}

		try {
			opeSocketControler.close();
			trapSocketControler.close();
		} catch(IOException exp) {}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpContext.run", " - end.");
	}
}

/**
 * SNMP要求に対する応答待ちリスト要素クラス。
 * SNMP-Managerとして要求を行うと、本クラスに応答待ちとして登録し、応答メッセージ
 * 受信あるいはタイムアウト発生時に要求に対応させる。本クラスはこの要求に対応する情報。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcSnmpResponseWaitList
 * @since	UDC1.0
 */
class UdcSnmpResponseWait
{
	/**
	 * SNMP要求PDU。
	 * @since	UDC1.0
	 */
	protected UdcPdu			requestPdu;

	/**
	 * SNMP応答待ち時間。(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	protected long				timeout;

	/**
	 * SNMP応答待ちタイマ識別子。
	 * @since	UDC1.0
	 */
	protected int				timerIndex;

	/**
	 * SNMP応答待ちユーザスレッドの待ちスレッドチャネル。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel 	replyChannel;

	/**
	 * SNMP応答待ちユーザスレッドの待ちスレッドチャネルへの送信情報
	 * の一つで応答識別子。
	 * @since	UDC1.0
	 */
	protected int				replyInvokeId;

	/**
	 * SNMP要求に対する一意の識別子。
	 * @since	UDC1.0
	 */
	protected int 				sendIndex;

	/**
	 * コンストラクタ。
	 *
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWait(UdcPdu request,long milisec, UdcThreadChannel repChannel, int repInvId)
	{
		super();
		requestPdu = request;
		timeout = milisec;
		replyChannel = repChannel;
		replyInvokeId = repInvId;
		timerIndex = -1;
		sendIndex = -1;
	}
}

/**
 * SNMP要求に対する応答待ちリストクラス。
 * SNMP-Managerとして要求を行うと、本クラスに応答待ちとして登録し、応答メッセージ
 * 受信あるいはタイムアウト発生時に要求に対応させる。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcSnmpResponseWait
 * @since	UDC1.0
 */
class UdcSnmpResponseWaitList extends UdcMutex
{
	/**
	 * SNMP応答待ちリスト
	 * @since	UDC1.0
	 */
	LinkedList waitList;

	/**
	 * コンストラクタ。
	 *
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWaitList()
	{
		super();
		waitList = new LinkedList();
	}

	/**
	 * SNMP応答待ちリストに新たに応答待ち要求を登録する。
	 *
	 * @return	SNMP応答待ちリストの登録数。
	 * @since	UDC1.0
	 */
	public int size()
	{
		return waitList.size();
	}

	/**
	 * SNMPリクエストIDから応答待ち要求を検索する。
	 *
	 * @return	SNMP要求に対する応答待ち要素クラス。
	 * @param	index	リストの位置
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWait get(int index)
	{
		if (waitList.size() >= index) {
			return null;
		}
		return (UdcSnmpResponseWait)waitList.get(index);
	}

	/**
	 * SNMP応答待ちリストに新たに応答待ち要求を登録する。
	 *
	 * @param	elm	SNMP要求に対する応答待ち要素クラス。
	 * @since	UDC1.0
	 */
	public void add(UdcSnmpResponseWait elm)
	{
		waitList.add(elm);
	}

	/**
	 * SNMP応答待ちリストから応答待ち要求を削除する。
	 *
	 * @param	elm	SNMP要求に対する応答待ち要素クラス。
	 * @since	UDC1.0
	 */
	public void remove(UdcSnmpResponseWait elm)
	{
		waitList.remove(elm);
	}

	/**
	 * SNMPリクエストIDから応答待ち要求を検索する。
	 *
	 * @return	SNMP要求に対する応答待ち要素クラス。
	 * @param	requestId	SNMPリクエストID
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWait searchRequestId(int requestId)
	{
		UdcSnmpResponseWait elm;
		for (int i=0; i<waitList.size(); i++) {
			elm=(UdcSnmpResponseWait)waitList.get(i);
			if (elm.requestPdu.getRequestId() == requestId) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * タイマ識別子から応答待ち要求を検索する。
	 *
	 * @param	timerIndex	タイマ識別子
	 * @return	SNMP要求に対する応答待ち要素クラス。
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWait searchTimerId(int timerIndex)
	{
		UdcSnmpResponseWait elm;
		for (int i=0; i<waitList.size(); i++) {
			elm=(UdcSnmpResponseWait)waitList.get(i);
			if (elm.timerIndex == timerIndex) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * 要求識別子から応答待ち要求を検索する。
	 *
	 * @param	sendIndex	要求識別子
	 * @return	SNMP要求に対する応答待ち要素クラス。
	 * @since	UDC1.0
	 */
	public UdcSnmpResponseWait searchSendId(int sendIndex)
	{
		UdcSnmpResponseWait elm;
		for (int i=0; i<waitList.size(); i++) {
			elm=(UdcSnmpResponseWait)waitList.get(i);
			if (elm.sendIndex == sendIndex) {
				return elm;
			}
		}
		return null;
	}
}

/**
 * SNMP-Trap通知を受信するためのUDP受信クラス。
 * UdcSnmpContext.trapSocketControler の受信インタフェースの実装クラスで、
 * SNMP要求は本クラスのreceiveによって受信される。
 * 本クラスではSNMP要求受信処理においてデコードまで行う。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpOpeReceiver implements UdcDatagramSocketReceiverFace , UdcSnmpConstants, UdcThreadMsgId, Runnable
{
	/**
	 * SNMP-Manager機能管理クラス。
	 * @since	UDC1.0
	 */
	UdcSnmpContext 		context;

	/**
	 * SNMP-Manager要求を受け付けるソケットクラス。
	 * @since	UDC1.0
	 */
	UdcDatagramSocket 	socket;

	/**
	 * SNMPメッセージ受信スレッド。
	 * @since	UDC1.0
	 */
	Thread				thread;

	/**
	 * SNMPメッセージ受信処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	protected boolean	threadLoop = true;

	/**
	 * startメソッドによってSNMPメッセージ受信スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	boolean				isStartComplete = false;


	/**
	 * コンストラクタ。
	 *
	 * @param	cont	SNMP-Manager機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpOpeReceiver(UdcSnmpContext cont)
	{
		context = cont;
	}

	/**
	 * UdcSocketを設定する。
	 *
	 * @since	UDC1.0
	 */
	public void set_socket(UdcDatagramSocket s)
	{
		socket = s;
	}

	/**
	 * startメソッドによってSNMPメッセージ受信スレッドの起動が完了したか否か
	 * を取得する。
	 *
	 * @return	startメソッドによって起動が完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * SNMPメッセージ受信機能を活性化(起動)する。
	 * 受信機能は受信専用スレッドにおいて実現されるため、
	 * そのスレッドの起動を行う。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			UdcMutex mute = new UdcMutex();
			while (! isStartComplete()) {
				try { mute.sleep(20); } catch(InterruptedException exp) {}
			}
		}
	}

	/**
	 * SNMPメッセージ受信機能を停止する。
	 * 受信機能は受信専用スレッドにおいて実現されるため、
	 * そのスレッドの停止を行う。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		threadLoop = false;
		if (thread != null) {
			thread.stop();
			thread = null;
		}
	}

	/**
	 * UDPメッセージの受信を行う。
	 * 受信メッセージ長は UdcSnmpContext.MaxSize_SnmpMessage 分。
	 *
	 * @return 	UDP受信メッセージ
	 * @since	UDC1.0
	 */
	public DatagramPacket receive()
		throws IOException
	{
		DatagramPacket packet = null;
		DatagramSocket soc = socket.get_socket();
		if (soc != null) {
			byte [] data = new byte[UdcSnmpContext.MaxSize_SnmpMessage];
			packet = new DatagramPacket(data,UdcSnmpContext.MaxSize_SnmpMessage);
			soc.receive(packet);
		}
		return packet;
	}

	/**
	 * SNMP要求メッセージの受信／デコードを行うスレッド。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpOpeReceiver.run", " - start.");
		isStartComplete = true;

		try {
			int dtrType = -1;
			UdcPdu pdu = null;
			DatagramPacket packet;
			while (threadLoop) {
				try {
					packet = socket.receive();
					if (packet == null) {
						socket.open();
						continue;
					}
				} catch(IOException exp) {
					continue;
				}
					// decoding
				pdu = null;
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
					UdcAsnSequence dummy = new UdcAsnSequence();
					UdcAsnSequence asnTopSeq = (UdcAsnSequence)dummy.AsnReadHeader(in);
					byte version = (byte)((UdcAsnInteger)asnTopSeq.getObj(0)).getValue();
					if (version == SNMP_VERSION_1 || version == SNMP_VERSION_2c) {
						pdu = new UdcPdu();
						pdu.decode(asnTopSeq);
						byte ty = pdu.getMsgType();
						if (ty != GET_RSP_MSG && ty != GET_RPRT_MSG) {
							pdu = null;
						} else {
							dtrType = Response_Snmp;
						}
					} else if (version == SNMP_VERSION_3) {
						//
						// Farther Study
						//
					} else {
					}
				} catch(IOException exp) {
					UdcTrace.trace(UdcTrace.Level, "UdcSnmpOpeReceiver.run : ", "io exception : " + exp);
					continue;
				} catch(UdcDecodingException dexp) {
					UdcTrace.trace(UdcTrace.Level, "UdcSnmpOpeReceiver.run : ", "decode exception : " + dexp);
					continue;
				}
					// wakeup response wait
				try {
					if (pdu != null) {
UdcTrace.trace(UdcTrace.Level, "UdcSnmpOpeReceiver.run : ", "<<reply-receive>>\n" + pdu.toString()); // uchiiiii
						pdu.setSourceAddress( packet.getAddress() );
						pdu.setSourcePort( packet.getPort() );
						context.channel.push( new UdcThreadChannelDtr(-1,dtrType,pdu) );
					}
				} catch(InterruptedException exp) {
					UdcTrace.trace(UdcTrace.Level, "UdcSnmpOpeReceiver.run : ", "cannot wake up for responce wait thread : " + exp);
					continue;
				}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcSnmpOpeReceiver.run", " - stop.");
			throw death;
		}

		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpOpeReceiver.run", " - end.");
	}
}

/**
 * SNMP要求を送信するためのUDP送信クラス。
 * UdcSnmpAgentContext.opeSocketControler　の送信インタフェースの実装クラスで、
 * SNMP要求は全て本クラスのsendから送信される。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpOpeSender implements UdcDatagramSocketSenderFace , UdcSnmpConstants, UdcThreadMsgId
{
	/**
	 * SNMP-Manager機能管理クラス。
	 * @since	UDC1.0
	 */
	UdcSnmpContext 		context;

	/**
	 * SNMP-Manager要求を受け付けるソケットクラス。
	 * @since	UDC1.0
	 */
	UdcDatagramSocket 	socket;

	/**
	 * コンストラクタ。
	 *
	 * @param	cont	SNMP-Manager機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpOpeSender(UdcSnmpContext cont)
	{
		context = cont;
	}

	/**
	 * UdcSocketを設定する。
	 *
	 * @since	UDC1.0
	 */
	public void set_socket(UdcDatagramSocket s)
	{
		socket = s;
	}

	/**
	 * SNMPメッセージ送信機能を活性化(起動)する。
	 * 送信機能はソケットが活性化されれば即可能であるため、
	 * 現在は何も行っていない。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException
	{
	}

	/**
	 * SNMPメッセージ送信機能を停止する。
	 * 送信機能はソケットが活性化されれば即可能であるため、
	 * 現在は何も行っていない。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		return;
	}

	/**
	 * startメソッドによって起動が完了したか否かを取得する。
	 * 送信はスレッド処理ではなく、ユーザスレッドから実施するため、
	 * コンストラクトされた時点で常に完了状態となる。
	 *
	 * @return	startメソッドによって起動が完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return true;
	}

	/**
	 * UDP通信送信処理を行う。
	 *
	 * @since	UDC1.0
	 */
	public void send(DatagramPacket data)
		throws IOException
	{
		DatagramSocket soc = socket.get_socket();
		soc.send(data);
	}
}

/**
 * SNMP-Trap通知を受信するためのUDP受信クラス。
 * UdcSnmpContext.trapSocketControler の受信インタフェースの実装クラスで、
 * SNMP要求は本クラスのreceiveによって受信される。
 * 本クラスではSNMP要求受信処理においてデコードまで行う。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpTrapReceiver implements UdcDatagramSocketReceiverFace , UdcSnmpConstants , UdcThreadMsgId, Runnable
{
	/**
	 * SNMP-Manager機能管理クラス。
	 * @since	UDC1.0
	 */
	UdcSnmpContext 		context;

	/**
	 * SNMP-Trapを受け付けるソケットクラス。
	 * @since	UDC1.0
	 */
	UdcDatagramSocket 	socket;

	/**
	 * SNMP-Trapメッセージ受信スレッド。
	 * @since	UDC1.0
	 */
	Thread				thread;

	/**
	 * SNMP-Trapメッセージ受信処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	protected boolean		threadLoop = true;

	/**
	 * startメソッドによってSNMP-Trapメッセージ受信スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	boolean				isStartComplete = false;

	/**
	 * コンストラクタ。
	 *
	 * @param	cont	SNMP-Manager機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpTrapReceiver(UdcSnmpContext cont)
	{
		context = cont;
	}

	/**
	 * UdcSocketを設定する。
	 *
	 * @since	UDC1.0
	 */
	public void set_socket(UdcDatagramSocket s)
	{
		socket = s;
	}

	/**
	 * startメソッドによってSNMP-Trap受信スレッドの起動が完了したか否か
	 * を取得する。
	 *
	 * @return	startメソッドによって起動が完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * SNMP-Trapメッセージ受信機能を活性化(起動)する。
	 * 受信機能は受信専用スレッドにおいて実現されるため、
	 * そのスレッドの起動を行う。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			UdcMutex mute = new UdcMutex();
			while (! isStartComplete()) {
				try { mute.sleep(20); } catch(InterruptedException exp) {}
			}
		}
	}

	/**
	 * SNMP-Trapメッセージ受信機能を停止する。
	 * 受信機能は受信専用スレッドにおいて実現されるため、
	 * そのスレッドの停止を行う。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		if (thread != null) {
			threadLoop = false;
			thread.stop();
			thread = null;
		}
	}

	/**
	 * UDPメッセージの受信を行う。
	 * 受信メッセージ長は UdcSnmpContext.MaxSize_SnmpMessage 分。
	 *
	 * @return 	UDP受信メッセージ
	 * @since	UDC1.0
	 */
	public DatagramPacket receive()
		throws IOException
	{
		DatagramPacket packet = null;
		DatagramSocket soc = socket.get_socket();
		if (soc != null) {
			byte [] data = new byte[UdcSnmpContext.MaxSize_SnmpMessage];
			packet = new DatagramPacket(data,UdcSnmpContext.MaxSize_SnmpMessage);
			soc.receive(packet);
		}
		return packet;
	}

	/**
	 * SNMP-Trapメッセージの受信／デコードを行い、ユーザ指定の
	 * スレッドチャネルに通知するスレッド。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpTrapReceiver.run", " - start.");
		isStartComplete = true;

		try {
			int dtrType = -1;
			UdcPdu trap = null;
			byte msgType;
			DatagramPacket packet;
			UdcAsnObject obj;

			while (threadLoop) {
				try {
					packet = socket.receive();
					if (packet == null) {
						socket.open();
						continue;
					}
				} catch(IOException exp) {

					continue;
				}
					// decoding
				trap = null;
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(packet.getData());
					UdcAsnSequence dummy = new UdcAsnSequence();
					UdcAsnSequence asnTopSeq = (UdcAsnSequence)dummy.AsnReadHeader(in);
					byte version = (byte)((UdcAsnInteger)asnTopSeq.getObj(0)).getValue();
					if (version == SNMP_VERSION_1) {
						if ((obj=asnTopSeq.findTrapPduv1()) == null || !(asnTopSeq.findTrapPduv1() instanceof UdcAsnTrapPduv1Sequence)) {
							continue;
						}
						UdcTrapPduv1 pdu = new UdcTrapPduv1();
						pdu.decode(asnTopSeq);
						dtrType = Event_SnmpTrapV1;
						trap = pdu;
					} else if (version == SNMP_VERSION_2c) {
						if ((obj=asnTopSeq.findPdu()) == null || ! (asnTopSeq.findPdu() instanceof UdcAsnPduSequence)) {
							continue;
						}
						UdcAsnPduSequence p = (UdcAsnPduSequence)asnTopSeq.findPdu();
						msgType = p.getType();
						if (msgType == INFORM_REQ_MSG) {
							UdcPdu pdu = new UdcPdu();
							pdu.decode(asnTopSeq);
							dtrType = Event_SnmpInformRequest;
							trap = pdu;
						} else {
							UdcTrapPduv2 pdu = new UdcTrapPduv2();
							pdu.decode(asnTopSeq);
							dtrType = Event_SnmpTrapV2;
							trap = pdu;
						}
					} else if (version == SNMP_VERSION_3) {
						//
						// Farther Study
						//
					} else {
					}
				} catch(IOException exp) {
					UdcTrace.trace(UdcTrace.Level, "UdcSnmpTrapReceiver::run : ", "io exception : " + exp);
					continue;
				} catch(UdcDecodingException dexp) {
					UdcTrace.trace(UdcTrace.Level, "UdcSnmpTrapReceiver::run : ", "decode exception : " + dexp);
					continue;
				}
					// push trap to trapChannel
				try {
					if (trap != null) {
						trap.setSourceAddress( packet.getAddress() );
						trap.setSourcePort( packet.getPort() );
						context.trapSendChannel.push( new UdcThreadChannelDtr(-1,dtrType,trap) );
					}
				} catch(InterruptedException exp) {
					continue;
				}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcSnmpTrapReceiver.run", " - stop.");
			throw death;
		}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpTrapReceiver.run", " - end.");
	}
}

