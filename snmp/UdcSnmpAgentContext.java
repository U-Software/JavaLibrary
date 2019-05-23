/* *********************************************************************
 * @(#)UdcSnmpAgentContext.java 1.0, 18 Jan 2003
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
 * SNMP-Agent機能は本クラスによって全てが管理されます。
 * SNMP-Agent処理クラス構成は以下のように実装されています。
 * <br>
 *   UdcSnmpAgentContext　　SNMP-Agent処理の統括クラス <br>
 *　　　　　|<br>
 *   UdcSnmpAgentMIB　　　　操作対象MIBの管理クラス<br>
 *　　　　　|<br>
 *   UdcSnmpAgentMIBObject  操作対象MIBオブジェクトクラス（ユーザMIB処理を定義しないGroup/Table対応）<br>
 *   UdcSnmpAgentMIBLeaf　　操作対象MIBオブジェクトクラス（ユーザMIB処理を定義するLeaf対応）<br>
 *<br>
 * UdcSnmpAgentContextクラスでは、SNMP要求を受信すると、受信したメッセージから
 * 要求されたOIDをに対応するUdcSnmpAgentMIBObject/LeafをUdcSnmpAgentMIBから検索し、
 * 対応するUdcSnmpAgentMIBObject/LeafのoperateXXメンバメソッドをコールします。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcSnmpAgentContext implements Runnable , UdcSnmpConstants , UdcThreadMsgId
{
	/**
	 * SNMP UDPメッセージ長デフォルト。
	 * @since	UDC1.0
	 */
	final static int	MaxSize_SnmpMessage = 1300;

	/**
	 * タイマ管理クラス。SNMP-Agent-MIB処理をスレッドで行う場合のスレッド確保待ち
	 * 管理に使用する。
	 * @since	UDC1.0
	 */
	protected UdcTimerManager	timerManager;

	/**
	 * Trap等のrequestIdを生成するためシーケンシャル番号管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcGenerateIndex	generateIndex;

	/**
	 * SNMP-Agent 要求受付ソケット管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcDatagramSocketControler	opeSocketControler;

	/**
	 * SNMP-Agent のアドレス。
	 * @since	UDC1.0
	 */
	InetAddress agentAddress;

	/**
	 * SNMP-Agent の制御ポート番号。
	 * @since	UDC1.0
	 */
	int 	agentPort;

	/**
	 * SNMP-Agentの操作対象となるMIB管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcSnmpAgentMIB	agentMib;

	/**
	 * SNMP-Agent処理受信スレッドチャネル。
	 * SNMP要求はopeSocketControlerによて受信され、MIB処理を行うために本チャネルに送信される。
	 * 本クラスではこれを受信してMIB処理を行う。
	 * @since	UDC1.0
	 */
	protected UdcThreadChannel	channel;

	/**
	 * SNMP-Agent MIB処理を行うスレッド。
	 * @since	UDC1.0
	 */
	protected Thread	thread;

	/**
	 * startメソッドによってSNMP-Agent MIB受信処理スレッドの起動が完了したか否かの状態。
	 * @since	UDC1.0
	 */
	protected boolean	isStartComplete = false;

	/**
	 * SNMP-Agent MIB処理を行うスレッドの処理継続フラグ
	 * @since	UDC1.0
	 */
	protected boolean	threadLoop = true;

	/**
	 * SNMP要求をパラレルにスレッド処理するか否かのフラグ。
	 * @since	UDC1.0
	 */
	protected boolean	isPararel = false;

	/**
	 * SNMP要求をパラレルにスレッド処理する場合の、スレッドプールクラス。
	 * @since	UDC1.0
	 */
	protected UdcThreadOperationPool	pararelThreadPool = null;

	/**
	 * SNMP要求をパラレルにスレッド処理する場合の、スレッドプールから空きスレッド
	 * 確保待ち時間。(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	protected long		pararelThreadWaitTime = 60000;


	/**
	 * コンストラクタ。
	 * SNMP要求を順にシーケンシャルの処理するモード。
	 *
	 * @param	timer　	タイマ管理
	 * @param	addr	SNMP-Agent 要求受付IPアドレス
	 * @param	opePort SNMP-Agent 要求受付IPポート
	 * @param	mib		SNMP-Agentの操作対象となるMIB管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentContext(UdcTimerManager timer, InetAddress addr, int opePort, UdcSnmpAgentMIB mib)
		throws IOException
	{
		UdcSnmpAgentOpeReceiver opeReceiver = new UdcSnmpAgentOpeReceiver(this);
		UdcSnmpAgentOpeSender opeSender = new UdcSnmpAgentOpeSender(this);

		timerManager = timer;
		generateIndex = new UdcGenerateIndex(1,50000);
		agentAddress = addr;
		agentPort = opePort;
		opeSocketControler = new UdcDatagramSocketControler(addr,opePort,opeReceiver,opeSender);
		agentMib = mib;
		channel = new UdcThreadChannel();
	}

	/**
	 * コンストラクタ。
	 * SNMP要求をパラレルにスレッド処理するモード。
	 *
	 * @param	timer　	タイマ管理
	 * @param	addr	SNMP-Agent 要求受付IPアドレス
	 * @param	opePort SNMP-Agent 要求受付IPポート
	 * @param	mib		SNMP-Agentの操作対象となるMIB管理クラス
	 * @param	pararelThreadNum SNMP要求をパラレルにスレッド処理する場合の、スレッドプール数
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentContext(UdcTimerManager timer, InetAddress addr, int opePort, UdcSnmpAgentMIB mib, int pararelThreadNum)
		throws IOException, CloneNotSupportedException
	{
		this(timer,addr,opePort,mib);
		isPararel = true;
		UdcSnmpRequestReceiver rec = new UdcSnmpRequestReceiver();
		rec.setContext(this);
		UdcThreadOperation ope = new UdcThreadOperation();
		ope.setOperation(rec);
		pararelThreadPool = new UdcThreadOperationPool(pararelThreadNum,ope,timerManager);
	}

	/**
	 * SNMP-Agent のアドレスを取得する。
	 *
	 * @return SNMP-Agent のアドレス
	 * @since	UDC1.2
	 */
	public InetAddress getAddress()
	{
		return agentAddress;
	}

	/**
	 * SNMP-Agent の制御ポート番号を取得する。
	 *
	 * @return SNMP-Agent の制御ポート番号
	 * @since	UDC1.2
	 */
	public int 	getPort()
	{
		return agentPort;
	}

	/**
	 * SNMP要求をパラレルにスレッド処理する場合の、スレッドプールから空きスレッド
	 * 確保待ち時間を取得する。
	 *
	 * @return	スレッド確保待ち時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public long	getPararelThreadWaitTime()
	{
		return pararelThreadWaitTime;
	}

	/**
	 * SNMP要求をパラレルにスレッド処理する場合の、スレッドプールから空きスレッド
	 * 確保待ち時間を設定する。
	 *
	 * @param	milsec	スレッド確保待ち時間(単位：ミリ秒)
	 * @since	UDC1.0
	 */
	public void	setPararelThreadWaitTime(int milsec)
	{
		pararelThreadWaitTime = milsec;
	}

	/**
	 * SNMP-Agent処理を開始する。
	 * 本メンバ関数を実行することで、Agent機能が開始される。具体的には
	 * SNMP要求受信処理・MIB処理スレッドが開始される。
	 * 本メンバ関数は一度のみ呼び出すことが可能です。
	 *
	 * @since	UDC1.0
	 */
	public void start()
		throws IOException, InterruptedException
	{
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			opeSocketControler.open();
			if (opeSocketControler.getReceiver() != null) {
				opeSocketControler.getReceiver().start();
			}
			if (opeSocketControler.getSender() != null) {
				opeSocketControler.getSender().start();
			}
			if (isPararel == true) {
				pararelThreadPool.startOperation();
			}
			UdcMutex mute = new UdcMutex();
			while (! isStartComplete()) {
				try { mute.sleep(20); } catch(InterruptedException exp) {}
			}
		}
	}

	/**
	 * SNMP-Agent処理を停止する。
	 * 本メンバ関数を実行することで、Agent機能が停止される。具体的には
	 * SNMP要求受信処理・MIB処理スレッドが停止される。
	 * 本メンバ関数は一度のみ呼び出すことが可能です。
	 *
	 * @since	UDC1.0
	 */
	public void stop()
		throws IOException
	{
		if (thread != null) {
			threadLoop = false;
			if (opeSocketControler.getReceiver() != null) {
				opeSocketControler.getReceiver().stop();
			}
			if (opeSocketControler.getSender() != null) {
				opeSocketControler.getSender().stop();
			}
			thread.stop();
			thread = null;
		}
	}

	/**
	 * SNMP-Agent受信処理スレッドが起動したか否かを取得する。
	 *
	 * @return	MIB処理スレッドが完了したか否か
	 * @since	UDC1.0
	 */
	public boolean isStartComplete()
	{
		return isStartComplete;
	}

	/**
	 * SNMP応答メッセージを送信する。
	 *
	 * @param	reply	応答PDU
	 * @since	UDC1.0
	 */
	public void sendReply(UdcPdu reply)
		throws java.io.IOException, InterruptedException, UdcEncodingException
	{
			// エンコード
		InetAddress destAddr = reply.getSourceAddress();
		int destPort = reply.getSourcePort();
		byte[] data = reply.encode();
			// 送信
		DatagramPacket packet = new DatagramPacket(data,data.length,destAddr,destPort);
		opeSocketControler.send(packet);
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
	 * SNMP-MIB処理の一つGET-NEXT処理を行う。
	 *
	 * @return 	正常:SNMP_ERR_NOSUCHNAME／異常:SNMP_ERR_NOSUCHNAME以外
	 * @param	reply		応答PDU
	 * @param	repVb		GET-NEXTによる応答VarBind格納エリア
	 * @param	reqPdu		要求PDU
	 * @param	vbcnt		要求PDUのGET-NEXT処理対象VarBaind格納位置
	 * @param	reqVb 		要求PDUのGET-NEXT処理対象VarBaind
	 * @param	mibObj		要求PDUのGET-NEXT処理対象MIBオブジェクト
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	protected int operateNext(UdcPdu reply, UdcVarbind repVb,
					UdcPdu reqPdu, int vbcnt, UdcVarbind reqVb, UdcSnmpAgentMIBObject mibObj, UdcSnmpTransactionFace tr)
	{
		int ret = 0;
		UdcSnmpAgentMIBObject trunk = (UdcSnmpAgentMIBObject)mibObj.getTrunk();
		if (trunk.getObjectType() == UdcSnmpAgentMIBObject.MIBGroup) {
			mibObj = agentMib.getNextMibObject(mibObj);
		}
		while (mibObj != null) {
			try {
				ret = mibObj.operateNext(reply,repVb,reqPdu,vbcnt,reqVb,tr);
				break;
			} catch(UdcNextMIBObjectException ex) {
				mibObj = agentMib.getNextMibObject(mibObj);
			}
		}
		if (mibObj == null) {
			if (agentMib.getVersion() == SNMP_VERSION_1) {
				return reply.setError(SNMP_ERR_NOSUCHNAME, 0);
			} else {
				return reply.setError(SNMP_VAR_ENDOFMIBVIEW, 0);
			}
		}
		if (ret != SNMP_ERR_NOERROR) {
			if (reply.getErrorStatus() == SNMP_ERR_NOERROR) {
				reply.setErrorStatus(ret);
			}
		}
		return ret;
	}

	/**
	 * SNMP-MIB処理の一つGET-BULK処理を行う。
	 * non-repeat objectsでされたVarBind分GET-NEXT処理を行い、以降のVarBind
	 * に対しては、max-repetitions回繰り返しGET-NEXT処理を行う。
	 *
	 * @return 	正常:SNMP_ERR_NOSUCHNAME／異常:SNMP_ERR_NOSUCHNAME以外
	 * @param	reply		応答PDU
	 * @param	pdu			要求PDU
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	protected int operateGetBulk(UdcPdu reply, UdcPdu pdu, UdcSnmpTransactionFace tr)
	{
		int i;
		int ret = SNMP_ERR_NOERROR;

		LinkedList nonVBList = pdu.getVarbind_NonRepeaters();
		LinkedList maxVBList = pdu.getVarbind_MaxRepetions();
		if (nonVBList == null || maxVBList == null) {
			return reply.setError(SNMP_ERR_DECODING_EXC, 0);
		}

		UdcVarbind vb,repVb;
		UdcSnmpAgentMIBObject mibObj;
			// GET for non-repeat objects
		for (i=0; i<nonVBList.size(); i++) {	
			vb = (UdcVarbind)nonVBList.get(i);
				// 対応MIBオブジェクトを検索
			if ((mibObj=agentMib.getMIBObject(vb.getOid())) == null) {
				return reply.setError(SNMP_ERR_NOSUCHNAME, 0);
			}
				// Indexチェック
			if (mibObj.checkIndex(vb.getOid()) == false) {
				return reply.setError(SNMP_ERR_NOSUCHNAME, 0);
			}
					// オペレーション
			repVb = new UdcVarbind();
			if ((ret=operateNext(reply,repVb,pdu,i,vb,mibObj,tr)) != SNMP_ERR_NOERROR) {
				if (reply.getErrorStatus() == SNMP_ERR_NOERROR) {
					reply.setErrorStatus(ret);
				}
				return ret;
			}
			reply.addOid(repVb);
		}
			// GETNEXT for repeat objects
		UdcVarbind tmp;
		int non = pdu.getNonRepeaters();
		int max = pdu.getMaxRepetitions();
		for (int j=0; j<max; j++) {
			for (i=0; i<maxVBList.size(); i++) {	
				vb = (UdcVarbind)maxVBList.get(i);
					// 対応MIBオブジェクトを検索
				if ((mibObj=agentMib.getMIBObject(vb.getOid())) == null) {
					return reply.setError(SNMP_ERR_NOSUCHNAME, 0);
				}
				//	// Indexチェック
				//if (mibObj.checkIndex(vb.getOid()) == false) {
				//	return reply.setError(SNMP_ERR_NOSUCHNAME, 0);
				//}
					// オペレーション
				repVb = new UdcVarbind();
				if ((ret=operateNext(reply,repVb,pdu,i,vb,mibObj,tr)) != SNMP_ERR_NOERROR) {
					if (reply.getErrorStatus() == SNMP_ERR_NOERROR) {
						reply.setErrorStatus(ret);
					}
					return ret;
				}
				try {
					tmp = (UdcVarbind)repVb.clone();
				} catch(CloneNotSupportedException exp) {
					return reply.setError(SNMP_ERR_DECODING_EXC, 0);	// 有り得ない
				}
				vb.setValue(tmp.getOid(),tmp.getValue());
				reply.addOid(repVb);
			}
		}
		return SNMP_ERR_NOERROR;
	}

	/**
	 * SNMP要求に対応するMIB処理を行う。
	 * SNMP要求受信処理は以下のように行われ、本メンバ関数がコールされる。
	 *   ・SNMP要求受信
	 *   ・MIB処理
	 *      ・トランザクション開始
	 *      ・MIB処理（本メンバ関数）
	 *      ・トランザクション終了
	 *      ・SNM応答返却
	 *
	 * @return	応答PDU
	 * @param	pdu			要求PDU
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	public UdcPdu receiveRequestSub(UdcPdu pdu, UdcSnmpTransactionFace tr)
	{
		UdcPdu reply = null;
		byte msgType = pdu.getMsgType();
		byte version = pdu.getVersion();

			// Varbind処理
		UdcVarbind vb;
		LinkedList vblist;
		int i;
		int ret = SNMP_ERR_NOERROR;
		UdcSnmpAgentMIBObject mibObj;
		vblist = pdu.getVarbindlist();
		if (msgType == GET_REQ_MSG || msgType == SET_REQ_MSG || msgType == GETNEXT_REQ_MSG) {
					// 応答PDUの初期化
			try {
				reply = (UdcPdu)pdu.clone();
			} catch (CloneNotSupportedException exp) {
				return null; // エラーは有り得ない
			}
			reply.setMsgType(GET_RSP_MSG);
			vblist = reply.getVarbindlist();
			for (i=0; i<vblist.size(); i++) {
				vb = (UdcVarbind)vblist.get(i);
				vb.setValue((String)null, new UdcAsnNull());
			}
					// Varbindリストを順に処理
			vblist = pdu.getVarbindlist();
			for (i=0; i<vblist.size(); i++) {
				vb = (UdcVarbind)vblist.get(i);
					// 対応MIBオブジェクトを検索
				if ((mibObj=agentMib.getMIBObject(vb.getOid())) == null) {
					reply.setError(SNMP_ERR_NOSUCHNAME, 0);
					break;
				}
					// Indexチェック
				if (msgType != GETNEXT_REQ_MSG) {
					if (mibObj.checkIndex(vb.getOid()) == false) {
						reply.setError(SNMP_ERR_NOSUCHNAME, 0);
						break;
					}
				}
					// オペレーション
				UdcVarbind repVb = new UdcVarbind();
				if (msgType == GET_REQ_MSG) {				// GET
					UdcTrace.trace(UdcTrace.Level,"receiveRequestSub : GET - operation.");
					ret = mibObj.operateGet(reply,repVb,pdu,i,vb,tr);
				} else if (msgType == GETNEXT_REQ_MSG) { 	// GET-NEXT
					UdcTrace.trace(UdcTrace.Level,"receiveRequestSub : GET-Next - operation.");
					ret = operateNext(reply,repVb,pdu,i,vb,mibObj,tr);
				} else if (msgType == SET_REQ_MSG) { 		// SET
					UdcTrace.trace(UdcTrace.Level,"receiveRequestSub : SET-Next - operation.");
					ret = mibObj.operateSet(reply,repVb,pdu,i,vb,tr);
				}
				if (ret != SNMP_ERR_NOERROR) {
					if (reply.getErrorStatus() == SNMP_ERR_NOERROR) {
						reply.setErrorStatus(ret);
					}
					break;
				}
				reply.replaceOid(i, repVb.getOid(),repVb.getValue());
				//reply.replaceOid(vb.getOid(),repVb.getOid(),repVb.getValue());
			}
		} else if (msgType == GETBULK_REQ_MSG) {			// BULK-GET
			if (version == SNMP_VERSION_1) {
				return null;
			}
					// 応答PDUの初期化
			UdcTrace.trace(UdcTrace.Level,"receiveRequestSub : GET-Bulk - operation.");
			reply = pdu.cloneBase();
			reply.setMsgType(GET_RSP_MSG);
					// オペレーション
			operateGetBulk(reply,pdu,tr);
		} else {
			return null;
		}

		//UdcTrace.trace(UdcTrace.Level,"<<reply>>\n" + reply.toString());
		return reply;
	}

	/**
	 * SNMP要求に対応するMIB処理を行う。
	 * SNMP要求受信処理は以下のように行われ、本メンバ関数がコールされる。
	 *   ・SNMP要求受信
	 *   ・MIB処理(本メンバ関数)
	 *      ・トランザクション開始
	 *      ・MIB処理
	 *      ・トランザクション終了
	 *      ・SNM応答返却
	 *
	 * @param	pdu		要求PDU
	 * @since	UDC1.0
	 */
	public void receiveRequest(UdcPdu pdu)
	{
		int ret;
			// トランザクション開始
		UdcSnmpTransactionFace tr;
		try {
			tr = agentMib.newTransaction();
		} catch (CloneNotSupportedException exp) {
			return; // エラーは有り得ない
		}
		if (tr != null) {
			if (tr.prepare(pdu) != 0) {
				return;
			}
		}
			// オペレーション処理
		UdcPdu reply = receiveRequestSub(pdu,tr);
			// トランザクション終了
		if (tr != null) {
			tr.commit(pdu,reply);
		}
			// 応答処理
				// 処理結果が異常の場合は、応答を返却しない
		if (reply == null) {
			return;
		}
			// 応答返却
		try {
			sendReply(reply);
		} catch (IOException ioexp) {
			;
		} catch (InterruptedException iexp) {
			;
		} catch (UdcEncodingException enexp) {
			;
		}
	}

	/**
	 * SNMP要求に対応するMIB処理をスレッドで行う。
	 * SNMP要求受信処理は以下のように行われ、本メンバ関数がコールされる。
	 *   ・SNMP要求受信
	 *   ・スレッドプールからMIB処理用スレッドを確保し、MIB処理要求（本メンバ関数）
	 *   <スレッド処理>
	 *   ・MIB処理
	 *      ・トランザクション開始
	 *      ・MIB処理
	 *      ・トランザクション終了
	 *      ・SNM応答返却
	 *
	 * @param	pdu		要求PDU
	 * @since	UDC1.0
	 */
	public void receiveRequestPararel(UdcPdu pdu)
	{
			// 空きスレッドの確保
		UdcThreadOperation paraOpeThread = (UdcThreadOperation)pararelThreadPool.allocateOperationThread(pararelThreadWaitTime);
		if (paraOpeThread == null) {
			receiveRequest(pdu);	// スレッドが確保できない場合は、自スレッドで行う。
			return;
		}
			// 空きスレッドに要求(応答の同期は取らない)
		UdcThreadChannel opeChannel = paraOpeThread.getChannel();
		int invId = 1;	// 同期通信はしないため、固定値 invId = opeChannel.newInvokeId();
		UdcThreadChannelDtr dtr = new UdcThreadChannelDtr(invId, Request_Snmp, pdu);
		try {
			opeChannel.push(dtr);
		} catch (InterruptedException iexp) {
			UdcTrace.trace(UdcTrace.Level,"receiveRequestPararel : " + iexp);
		}
	}

	/**
	 * SNMP要求受信処理を行うスレッド。
	 * Socketレベルでの受信処理は、opeSocketControlerによって行われ、この受信メッセージから
	 * 要求PDUの正常性確認(community/versionチェック)を行い、MIB処理を行う。
	 *
	 * @since	UDC1.0
	 */
	public void run()
	{
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentContext.run", " - start.");
		isStartComplete = true;

		try {
			byte msgType,version;
			UdcThreadChannelDtr dtr;
			while (threadLoop) {
				try {
					dtr = channel.pull();
					int dtrtype = dtr.get_dtrType();
					if (dtrtype == Request_Snmp) {
						UdcPdu pdu = (UdcPdu)dtr.get_data();
							// versionチェック
						if (pdu.getVersion() != agentMib.getVersion()) {
							UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentContext.run", " - unmatch version.");
							continue;
						}
							// communityチェック
						if (pdu.getCommunity().equals(agentMib.getCommunity()) == false) {
							UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentContext.run", " - unmatch community.");
							continue;
						}
							// Agentオペレーション
						if (isPararel) {
							receiveRequestPararel(pdu);
						} else {
							receiveRequest(pdu);
						}
					} else {
					}
				} catch(InterruptedException exp) {
				}
			}
		} catch(ThreadDeath death) {
			try { opeSocketControler.close(); } catch(IOException exp) {}
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentContext.run", " - stop.");
			throw death;
		}
		try { opeSocketControler.close(); } catch(IOException exp) {}
		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentContext.run", " - end.");
	}
}

/**
 * SNMP要求を受信するためのUDP受信クラス。
 * UdcSnmpAgentContext.opeSocketControler　の受信インタフェースの実装クラスで、
 * SNMP要求は本クラスのreceiveによって受信される。
 * 本クラスではSNMP要求受信処理においてデコードまで行う。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpAgentOpeReceiver implements UdcDatagramSocketReceiverFace , UdcSnmpConstants, UdcThreadMsgId, Runnable
{
	/**
	 * SNMP-Agent機能管理クラス。
	 * @since	UDC1.0
	 */
	UdcSnmpAgentContext context;

	/**
	 * SNMP-Agent要求を受け付けるソケットクラス。
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
	boolean 			isStartComplete = false;

	/**
	 * コンストラクタ。
	 *
	 * @param	cont	SNMP-Agent機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentOpeReceiver(UdcSnmpAgentContext cont)
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
	 * SNMPメッセージ受信機能を活性化(起動)する。
	 * 受信機能は受信専用スレッドにおいて実現されるため、そのスレッドの起動を行う。
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
	 * 受信機能は受信専用スレッドにおいて実現されるため、そのスレッドの停止を行う。
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
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentOpeReceiver.run", " - start.");
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
						if (ty != GET_REQ_MSG && ty != SET_REQ_MSG && ty != GETNEXT_REQ_MSG
						  && ty != GETBULK_REQ_MSG && ty != INFORM_REQ_MSG) {
							pdu = null;
						} else {
							dtrType = Request_Snmp;
						}
					} else if (version == SNMP_VERSION_3) {
						//
						// Farther Study
						//
					} else {

					}
				} catch(IOException exp) {
					UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentOpeReceiver.run", " - io exception : " + exp);
					continue;
				} catch(UdcDecodingException dexp) {
					UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentOpeReceiver.run", " - decode exception : " + dexp);
					continue;
				}
					// wakeup request wait
				try {
					if (pdu != null) {
						pdu.setSourceAddress( packet.getAddress() );
						pdu.setSourcePort( packet.getPort() );
						context.channel.push( new UdcThreadChannelDtr(-1,dtrType,pdu) );
					}
				} catch(InterruptedException exp) {
					continue;
				}
			}
		} catch(ThreadDeath death) {
			isStartComplete = false;
			UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentOpeReceiver.run", " - stop.");
			throw death;
		}

		isStartComplete = false;
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpAgentOpeReceiver.run", " - start.");
	}
}

/**
 * SNMP応答やトラップを送信するためのUDP送信クラス。
 * UdcSnmpAgentContext.opeSocketControler　の送信インタフェースの実装クラスで、
 * SNMP応答やトラップは全て本クラスのsendから送信される。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpAgentOpeSender implements UdcDatagramSocketSenderFace , UdcSnmpConstants, UdcThreadMsgId
{
	/**
	 * SNMP-Agent機能管理クラス。
	 * @since	UDC1.0
	 */
	UdcSnmpAgentContext	context;

	/**
	 * SNMP-Agent要求を受け付けるソケットクラス。
	 * @since	UDC1.0
	 */
	UdcDatagramSocket 	socket;

	/**
	 * コンストラクタ。
	 *
	 * @param	cont	SNMP-Agent機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentOpeSender(UdcSnmpAgentContext cont)
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
	 * @param	data	送信情報
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
 * SNMP要求をパラレルにスレッド処理するためのスレッドオペレーションクラス。
 * 本クラスは、UdcThreadPoolクラスによってプール管理される。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcSnmpRequestReceiver implements UdcOperationFace, UdcThreadMsgId
{
	/**
	 * SNMP-Agent機能管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcSnmpAgentContext	context = null;

	/**
	 * オペレーションスレッド。本クラスは、このスレッド下で動作するインタフェース実装。
	 * @since	UDC1.0
	 */
	protected UdcThreadOperation	opeTh = null;


	/**
	 * 自身を制御するオペレーションスレッドを設定する。
	 *
	 * @param	ope	オペレーションスレッド
	 * @since	UDC1.0
	 */
	public void setThreadOperation(UdcThreadOperation ope)
	{
		opeTh = ope;
	}

	/**
	 * SNMP-Agent機能管理クラスを取得する。
	 *
	 * @return	SNMP-Agent機能管理クラス
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentContext getContext()
	{
		return context;
	}

	/**
	 * SNMP-Agent機能管理クラスを設定する。
	 *
	 * @param	cont	SNMP-Agent機能管理クラス。
	 * @since	UDC1.0
	 */
	public void setContext(UdcSnmpAgentContext cont)
	{
		context = cont;
	}

	/**
	 * 自インスタンスの複製を取得する。
	 *
	 * @return  自インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcSnmpRequestReceiver ope = new UdcSnmpRequestReceiver();
		ope.setContext(context);
		return ope;
	}

	/**
	 * 本インタフェースを動作させるオペレーションスレッド確保時にコールされる
	 * インタフェース。本クラスでは何も行わない。
	 *
	 * @return  正常：0／異常：0以外
	 * @since	UDC1.0
	 */
	public int startTransaction()
	{
		return 0;
	}

	/**
	 * 本インタフェースを動作させるオペレーションスレッド解放時にコールされる
	 * インタフェース。本クラスでは何も行わない。
	 *
	 * @return  正常：0／異常：0以外
	 * @since	UDC1.0
	 */
	public int stopTransaction()
	{
		return 0;
	}

	/**
	 * SNMP-Agent　MIB処理を行う。
	 *
	 * @return  正常：0／異常：0以外
	 * @param	request	SNMP-MIB処理要求情報
	 * @since	UDC1.0
	 */
	public int action(UdcThreadChannelDtr request)
	{
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpRequestReceiver.action", " - start.");

		int ret = 0;
		if (request.get_dtrType() != Request_Snmp) {
			return -1;
		}
			// SNMP-Agentオペレーション
		UdcPdu pdu = (UdcPdu)request.get_data();
		context.receiveRequest(pdu);
			// 自身でスレッド解放
		if (context.pararelThreadPool != null) {
			context.pararelThreadPool.releaseOperationThread(opeTh.getThreadIndex());
		}
		UdcTrace.trace(UdcTrace.Level,"UdcSnmpRequestReceiver.action", " - end.");
		return 0;
	}
}
