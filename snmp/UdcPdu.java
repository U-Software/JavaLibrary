/* *********************************************************************
 * @(#)UdcPdu.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.util.*;
import java.io.*;
import java.net.*;

import udc.util.*;


/**
 * SNMP-Request/ResponseのPDUクラス。本クラスでV1/V2を
 * サポートします。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcPdu implements UdcSnmpConstants, Cloneable
{
	/**
	 * SNMP応答のエラー文字列。
	 * @since	UDC1.0
	 */
	private final static String [] errorStrings = {
		"No error",
		"Value too big error",
		"No such name error",
		"Bad value error",
		"Read only error",
		"General error",
		"No access error",
		"Wrong type error",
		"Wrong length error",
		"Wrong encoding error",
		"Wrong value error",
		"No creation error",
		"Inconsistent value error",
		"Resource unavailable error",
		"Commit failed error",
		"Undo failed error",
		"Authorization error",
		"Not writable error",
		"Inconsistent name error",
	};

	/**
	 * 受信時の送信元IPアドレス。
	 * @since	UDC1.0
	 */
	protected InetAddress	srcAddr;

	/**
	 * 受信時の送信元IPポート。
	 * @since	UDC1.0
	 */
	protected int			srcPort;

	/**
	 * SNMP処理結果。
	 * @since	UDC1.0
	 */
	protected int			result = UdcSnmpContext.Success;

	/**
	 * SNMPバージョン。
	 * @since	UDC1.0
	 */
	protected byte			version = SNMP_VERSION_1;

	/**
	 * SNMPコミュニティ。
	 * @since	UDC1.0
	 */
	protected String		community = "public";

	/**
	 * SNMPメッセージ種別。
	 * @see		UdcSnmpConstants#GET_REQ_MSG
	 * @see		UdcSnmpConstants#GETNEXT_REQ_MSG
	 * @see		UdcSnmpConstants#SET_REQ_MSG
	 * @see		UdcSnmpConstants#GETBULK_REQ_MSG
	 * @see		UdcSnmpConstants#GET_RSP_MSG
	 * @see		UdcSnmpConstants#TRP_REQ_MSG
	 * @see		UdcSnmpConstants#TRPV2_REQ_MSG
	 * @see		UdcSnmpConstants#INFORM_REQ_MSG
	 * @see		UdcSnmpConstants#GET_RPRT_MSG
	 * @since	UDC1.0
	 */
	protected byte     		msgType;

	/**
	 * SNMPリクエストID。
	 * @since	UDC1.0
	 */
	protected int 			requestId;

	/**
	 * SNMP - errorStatus
	 * @since	UDC1.0
	 */
	protected int       	errorStatus;

	/**
	 * SNMP - errorIndex
	 * @since	UDC1.0
	 */
	protected int       	errorIndex;

	/**
	 * SNMP - VarBindList
	 * @since	UDC1.0
	 */
	protected LinkedList	varbindlist = null;

	/**
	 * SNMP - GETBULK時のnon-repeaters
	 * @since	UDC1.0
	 */
	protected int non_repeaters = 0;

	/**
	 * SNMP - GETBULK時のmax-repetitions
	 * @since	UDC1.0
	 */
	protected int max_repetitions = 0;

	/**
	 * コンストラクタ
	 * 初期化時は
	 *   メッセージ種別: GET_REQ_MSG
	 *   errorStatus:    SNMP_ERR_NOERROR
	 * が設定されます。
	 *
	 * @since	UDC1.0
	 */
	public UdcPdu()
	{
		msgType = GET_REQ_MSG;
		requestId  = -1;
		errorStatus = SNMP_ERR_NOERROR;
		errorIndex  = 0x00;
		varbindlist = new LinkedList();
	}

	/**
	 * SNMP処理結果を取得する。
	 *
	 * @return SNMP処理結果
	 * @since	UDC1.0
	 */
	public int getResult() { return result; }

	/**
	 * SNMP処理結果を上書きする。 
	 *
	 * @param	res SNMP処理結果
	 * @since	UDC1.1
	 */
	public void setResult(int res) { result = res; }

	/**
	 * SNMPバージョンを取得する。
	 *
	 * @return	SNMPバージョン
	 * @since	UDC1.0
	 */
	public byte getVersion() { return version; }

	/**
	 * SNMPバージョンを設定する。
	 *
	 * @param	ver		SNMPバージョン
	 * @since	UDC1.0
	 */
	public void setVersion(byte ver) { version = ver; }

	/**
	 * SNMPバージョンをversion1に設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setVersion1()	{ version = SNMP_VERSION_1; }

	/**
	 * SNMPバージョンをversionr2cに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setVersion2c()	{ version = SNMP_VERSION_2c; }

	/**
	 * SNMPバージョンをversionr3に設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setVersion3()	{ version = SNMP_VERSION_3; }

	/**
	 * SNMPコミュニティを取得する。
	 *
	 * @return	SNMPコミュニティ
	 * @since	UDC1.0
	 */
	public String getCommunity() { return community; }

	/**
	 * SNMPコミュニティを設定する。
	 *
	 * @param	newCommunity	SNMPコミュニティ
	 * @since	UDC1.0
	 */
	public void setCommunity(String newCommunity) { community = new String(newCommunity); }

	/**
	 * SNMPメッセージ種別を取得する。
	 *
	 * @since	UDC1.0
	 */
	public byte getMsgType() { return msgType; }

	/**
	 * SNMPメッセージ種別を設定する。
	 *
	 * @param	type	SNMPメッセージ種別
	 * @see		UdcSnmpConstants#GET_REQ_MSG
	 * @see		UdcSnmpConstants#GETNEXT_REQ_MSG
	 * @see		UdcSnmpConstants#SET_REQ_MSG
	 * @see		UdcSnmpConstants#GETBULK_REQ_MSG
	 * @see		UdcSnmpConstants#GET_RSP_MSG
	 * @see		UdcSnmpConstants#TRP_REQ_MSG
	 * @see		UdcSnmpConstants#TRPV2_REQ_MSG
	 * @see		UdcSnmpConstants#INFORM_REQ_MSG
	 * @see		UdcSnmpConstants#GET_RPRT_MSG
	 * @since	UDC1.0
	 */
	public void setMsgType(byte type) { msgType = type; }

	/**
	 * SNMPメッセージ種別をGET_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeGET() { msgType = GET_REQ_MSG; }

	/**
	 * SNMPメッセージ種別をGETNEXT_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeGETNEXT() { msgType = GETNEXT_REQ_MSG; }

	/**
	 * SNMPメッセージ種別をGETBULK_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeGETBULK() { msgType = GETBULK_REQ_MSG; }

	/**
	 * SNMPメッセージ種別をSET_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeSET() { msgType = SET_REQ_MSG; }

	/**
	 * SNMPメッセージ種別をTRAP_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeTRAPV1() { msgType = TRP_REQ_MSG; }

	/**
	 * SNMPメッセージ種別をTRAPV2_REQ_MSGに設定する。
	 *
	 * @since	UDC1.0
	 */
	public void setMsgTypeTRAPV2() { msgType = TRPV2_REQ_MSG; }

	/**
	 * SNMPリクエストIDを取得する。
	 *
	 * @return	SNMPリクエストID
	 * @since	UDC1.0
	 */
	public int getRequestId() { return requestId; }

	/**
	 * SNMP errorStatusを取得する。
	 *
	 * @return	SNMP errorStatus
	 * @since	UDC1.0
	 */
	public int getErrorStatus() { return errorStatus; }

	/**
	 * SNMP errorIndexを取得する。
	 *
	 * @return	SNMP errorIndex
	 * @since	UDC1.0
	 */
	public int getErrorIndex() { return errorIndex; }

	/**
	 * SNMP errorStatusを設定する。
	 *
	 * @param	errstat	SNMP errorStatus
	 * @since	UDC1.0
	 */
	public void setErrorStatus(int errstat) { errorStatus = errstat; }

	/**
	 * SNMP errorIndexを設定する。
	 *
	 * @param	errind	SNMP errorIndex
	 * @since	UDC1.0
	 */
	public void setErrorIndex(int errind) { errorIndex = errind; }

	/**
	 * SNMP errorStatus/errorIndexを設定する。
	 *
	 * @param	errstat	SNMP errorStatus
	 * @param	errind	SNMP errorIndex
	 * @since	UDC1.0
	 */
	public int setError(int errstat, int errind)
	{
		errorStatus = errstat;
		errorIndex = errind;
		return errorStatus;
	}

	/**
	 * SNMP-BULKGETのnon-repeatersを取得する。
	 *
	 * @return	SNMP-BULKGETのnon-repeaters
	 * @since	UDC1.0
	 */
	public int getNonRepeaters() { return non_repeaters; }

	/**
	 * SNMP-BULKGETのnon-repeatersを設定する。
	 *
	 * @param	no	SNMP-BULKGETのnon-repeaters
	 * @since	UDC1.0
	 */
	public void setNonRepeaters(int no) { non_repeaters = no; }

	/**
	 * SNMP-BULKGETのmax-repetitionsを取得する。
	 *
	 * @return	SNMP-BULKGETのmax-repetitions
	 * @since	UDC1.0
	 */
	public int getMaxRepetitions() { return max_repetitions; }

	/**
	 * SNMP-BULKGETのmax-repetitionsを設定する。
	 *
	 * @param	no	SNMP-BULKGETのmax-repetitions
	 * @since	UDC1.0
	 */
	public void setMaxRepetitions(int no) { max_repetitions = no; }

	/**
	 * 本インスタンスの複製を生成するための共通処理を行う。
	 *
	 * @return	共通情報がコピー設定された本インスタンスの複製
	 * @since	UDC1.0
	 */
	protected UdcPdu cloneBase()
	{
		UdcPdu pdu = new UdcPdu();
		pdu.community = new String( community );
		pdu.msgType = msgType;
		pdu.version = version;
		pdu.requestId = requestId;
		pdu.errorStatus = errorStatus;
		pdu.errorIndex = errorIndex;
		pdu.non_repeaters = non_repeaters;
		pdu.max_repetitions = max_repetitions;
		pdu.result = result;
		pdu.srcAddr = srcAddr;
		pdu.srcPort = srcPort;
		return pdu;
	}

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone()
			throws CloneNotSupportedException
	{
		UdcPdu pdu = cloneBase();
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			pdu.addOid((UdcVarbind)vb.clone());
		}
		return pdu;
	}

	/**
	 * VarBindListにOIDのみのVarBindを追加設定する。
	 *
	 * @param	oid	VarBindListに追加するOID
	 * @since	UDC1.0
	 */
	public void addOid(String oid)
	{
		varbindlist.add(new UdcVarbind(oid));
	}

	/**
	 * VarBindListにOIDのみのVarBindを追加設定する。
	 *
	 * @param	oid	VarBindListに追加するOID
	 * @since	UDC1.0
	 */
	public void addOid(UdcAsnObjectId oid)
	{
		varbindlist.add(new UdcVarbind(oid));
	}

	/**
	 * VarBindListにVarBindを追加設定する。
	 *
	 * @param	var	VarBindListに追加するVarBind
	 * @since	UDC1.0
	 */
	public void addOid(UdcVarbind var)
	{
		varbindlist.add(var);
	}

	/**
	 * VarBindList中の指定VarBindを再設定する。
	 *
	 * @return 	再設定結果(0:正常/-1:異常)
	 * @param	vbcnt	VarBindListの再設位置
	 * @param	newOid	VarBindListに再設定するVarBind:ObjectId
	 * @param	newVal	VarBindListに再設定するVarBind:ObjectValue
	 * @since	UDC1.0
	 */
	public int replaceOid(int vbcnt, UdcAsnObjectId newOid, UdcAsnObject newVal)
	{
		if (varbindlist.size() <= vbcnt) {
			return -1;
		}
		UdcVarbind vb = (UdcVarbind)varbindlist.get(vbcnt);
		vb.setValue(newOid,newVal);
		return 0;
	}

	/**
	 * VarBindList中の指定VarBindを再設定する。
	 *
	 * @return 	再設定結果(0:正常/-1:異常)
	 * @param	oid		VarBindListの再設定対象VarBindのOID
	 * @param	newOid	VarBindListに再設定するVarBind:ObjectId
	 * @param	newVal	VarBindListに再設定するVarBind:ObjectValue
	 * @since	UDC1.0
	 */
	public int replaceOid(UdcAsnObjectId oid, UdcAsnObjectId newOid, UdcAsnObject newVal)
	{
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			if ( oid.compareTo(vb.getOid()) == 0) {
				vb.setValue(newOid,newVal);
				return 0;
			}
		}
		return -1;
	}

	/**
	 * VarBindListの設定要素数を取得する。
	 *
	 * @return	VarBindListの設定要素数
	 * @since	UDC1.0
	 */
	public int getVarbindlistSize()
	{
		return varbindlist.size();
	}

	/**
	 * VarBindListを取得する。
	 *
	 * @return	VarBindList
	 * @since	UDC1.0
	 */
	public LinkedList getVarbindlist()
	{
		return varbindlist;
	}

	/**
	 * VarBindListから指定されたOIDのVarBindを取得する。
	 *
	 * @return	VarBind
	 * @param	oid	取得するVarBindのOID
	 * @since	UDC1.0
	 */
	public UdcVarbind getVarbind(String oid)
	{
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			if ( oid.equals(vb.getOid().getValue()) ) {
				return vb;
			}
		}
		return null;
	}

	/**
	 * VarBindListから指定されたOIDのVarBindを取得する。
	 *
	 * @return	VarBind
	 * @param	oid	取得するVarBindのOID
	 * @since	UDC1.0
	 */
	public UdcVarbind getVarbind(UdcAsnObjectId oid)
	{
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			if (oid.isSame(vb.getOid())) {
				return vb;
			}
		}
		return null;
	}

	/**
	 * VarBindListから指定されたOIDを含むのVarBindを取得する。
	 *
	 * @return	VarBind
	 * @param	oid	取得するVarBindのOID
	 * @since	UDC1.0
	 */
	public UdcVarbind getVarbind_contain(UdcAsnObjectId oid)
	{
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			if (vb.getOid().contain(oid)) {
				return vb;
			}
		}
		return null;
	}

	/**
	 * VarBindListから指定された位置のVarBindを取得する。
	 *
	 * @return	VarBind
	 * @param	i	取得するVarBindの格納位置
	 * @since	UDC1.0
	 */
	public UdcVarbind getVarbind(int i)
	{
		return (UdcVarbind)varbindlist.get(i);
	}

	/**
	 * GETBULK要求時にVarBindListからnon-repeaters対象のVarBindリストを取得する。
	 *
	 * @return	non-repeaters対象のVarBindのリスト
	 * @since	UDC1.0
	 */
	public LinkedList getVarbind_NonRepeaters()
	{
		if (msgType != GETBULK_REQ_MSG) {
			return null;
		}
		int size = varbindlist.size();
		if (size <= non_repeaters) {
			return null;
		}
		LinkedList nonRepVBList = new LinkedList();
		UdcVarbind last = (UdcVarbind)varbindlist.get(non_repeaters);

		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			if (vb == last) {
				break;
			}
			try {
				nonRepVBList.add( (UdcVarbind)vb.clone() );
			} catch(CloneNotSupportedException exp) {
				return null;
			}
		}
		return nonRepVBList;
	}

	/**
	 * GETBULK要求時にVarBindListからmax-repetitions対応のVarBindリストを取得する。
	 *
	 * @return	max-repetitions対応のVarBindリスト
	 * @since	UDC1.0
	 */
	public LinkedList getVarbind_MaxRepetions()
	{
		if (msgType != GETBULK_REQ_MSG) {
			return null;
		}
		int size = varbindlist.size();
		if (size <= non_repeaters) {
			return null;
		}
		LinkedList maxRepVBList = new LinkedList();
		UdcVarbind vb;
		for (int i=non_repeaters; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			try {
				maxRepVBList.add( (UdcVarbind)vb.clone() );
			} catch(CloneNotSupportedException exp) {
				return null;
			}
		}
		return maxRepVBList;
	}

	/**
	 * 受信時の送信元IPアドレスを取得する。
	 *
	 * @return	受信時の送信元IPアドレス
	 * @since	UDC1.0
	 */
	public InetAddress	getSourceAddress() { return srcAddr; }

	/**
	 * 受信時の送信元IPアドレスを設定する。
	 *
	 * @param	addr	受信時の送信元IPアドレス
	 * @since	UDC1.0
	 */
	public void			setSourceAddress(InetAddress addr) { srcAddr = addr; }

	/**
	 * 受信時の送信元IPポートを取得する。
	 *
	 * @return	受信時の送信元IPポート
	 * @since	UDC1.0
	 */
	public int			getSourcePort() { return srcPort; }

	/**
	 * 受信時の送信元IPポートを設定する。
	 *
	 * @param	port	受信時の送信元IPポート
	 * @since	UDC1.0
	 */
	public void			setSourcePort(int port) { srcPort = port; }

	/**
	 * 自PDUをエンコードする。
	 *
	 * @return	エンコードしたバイト列
	 * @since	UDC1.0
	 */
	public byte[] encode()
		throws java.io.IOException, UdcEncodingException
	{
		byte[] encodedata = null;
		if (version == SNMP_VERSION_1 || version == SNMP_VERSION_2c) {
			encodedata = encodeForVersion1or2c();
		} else if (version == SNMP_VERSION_3) {
			//
			// Farther study
			//
		} else {
			throw new UdcEncodingException("illegal version");
		}
		return encodedata;
	}

	/**
	 * 自PDUをSNMPv1/v2cでエンコードする。
	 *
	 * @return	エンコードしたバイト列
	 * @since	UDC1.0
	 */
	public byte[] encodeForVersion1or2c()
		throws java.io.IOException, UdcEncodingException
	{
			// create msg
		UdcAsnSequence	msg = new UdcAsnSequence();
				// version
		msg.add(new UdcAsnInteger(version));
				// community
		msg.add(new UdcAsnOctets(getCommunity()));
				// pdu
		UdcAsnObject pdu = new UdcAsnSequence(msgType);	// msgtype (choice)
		pdu.add(new UdcAsnInteger(requestId));			// reruest_id
		if (msgType == GETBULK_REQ_MSG) {
			pdu.add(new UdcAsnInteger(non_repeaters));		// non_repeaters
			pdu.add(new UdcAsnInteger(max_repetitions));	// max_repetitions
		} else {
			pdu.add(new UdcAsnInteger(errorStatus));		// error_status
			pdu.add(new UdcAsnInteger(errorIndex));			// error_index
		}
					// varbindlist
		UdcAsnObject vbl = pdu.add(new UdcAsnSequence());
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			UdcAsnObject vbObj = vbl.add(new UdcAsnSequence());
			vbObj.add(vb.getOid());
			vbObj.add(vb.getValue());
		}
		msg.add(pdu);
			// encoding
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.write(out);
		return out.toByteArray();
	}

	/**
	 * デコードする。
	 *
	 * @param	in	デコード対象のストリーム
	 * @since	UDC1.0
	 */
	public void decode(InputStream in)
		throws IOException, UdcDecodingException
	{
			// decoding
		UdcAsnSequence dummy = new UdcAsnSequence();
		UdcAsnSequence asnTopSeq = (UdcAsnSequence)dummy.AsnReadHeader(in);
		decode(asnTopSeq);
	}

	/**
	 * デコードする。
	 *
	 * @param	asnTopSeq	デコード対象のSEQUENCE情報
	 * @since	UDC1.0
	 */
	public void decode(UdcAsnSequence asnTopSeq)
		throws IOException, UdcDecodingException
	{
			// version
		version = (byte)((UdcAsnInteger)asnTopSeq.getObj(0)).getValue();
		if (version != SNMP_VERSION_1 && version != SNMP_VERSION_2c && version != SNMP_VERSION_3) {
			throw new UdcDecodingException("Wrong version, [" + version + "]");
		}
			// community
		if (asnTopSeq.getObj(1) != null) {
			community = ((UdcAsnOctets)asnTopSeq.getObj(1)).getValue();
		}
			// pdu
		UdcAsnPduSequence pdu = (UdcAsnPduSequence)asnTopSeq.findPdu();
		msgType = pdu.getType();								// msgType
		requestId = ((UdcAsnInteger)pdu.getObj(0)).getValue();		// request_id
		if (msgType == GETBULK_REQ_MSG) {
			non_repeaters = ((UdcAsnInteger)pdu.getObj(1)).getValue();	// non_repeaters
			max_repetitions = ((UdcAsnInteger)pdu.getObj(2)).getValue();// max_repetitions
		} else {
			errorStatus = ((UdcAsnInteger)pdu.getObj(1)).getValue();	// error_status
			errorIndex = ((UdcAsnInteger)pdu.getObj(2)).getValue();		// error_index
		}
				// varbindlist
		UdcAsnSequence varbindings = (UdcAsnSequence)pdu.getObj(3);
		int size = varbindings.getObjCount();
		for (int i=0; i<size; i++) {
			UdcAsnSequence vbObj = (UdcAsnSequence)varbindings.getObj(i);
			varbindlist.add( new UdcVarbind(vbObj) );
		}
	}

	/**
	 * SNMP-errorStatuをエラー文字列に変換する。
	 *
	 * @return	SNMP-errorStatuをエラー文字列に変換したもの
	 * @since	UDC1.0
	 */
	public String getErrorStatusString()
	{
		String err = "";
		if (errorStatus >= 0 && errorStatus < errorStrings.length) {
			err = errorStrings[errorStatus];
		}
		return err;
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		String res = getClass().getName() + " [" + "Udc result =" + result + "] "
			+ "version=" + version + ", reqId=" + requestId + ", msgType=0x" + UdcCoder.byte2HexString(msgType)
			+ ", errSt=" + errorStatus + ", errInd="  + errorIndex
			+ "\n  vblist[" + varbindlist.size() + "]\n";

		if (varbindlist != null) {
			UdcVarbind vb;
			for (int i=0; i<varbindlist.size(); i++) {
				vb = (UdcVarbind)varbindlist.get(i);
				res += "    [" + vb.toString() + "]\n";
			}
		} else {
			res += "null";
		}
		return res;
	}


	/**
	 * SNMP-GET応答PDUの正常性チェックを行う。
	 * 	@param	respPdu SNMP-GET応答PDU
	 *	@param	err	エラー情報格納エリア
	 *	@return	0:正常/非0:異常(SnmpRespCheck_XXXXX)
	 */
	public static int checkResponse(UdcPdu respPdu, StringBuffer err)
	{
		int result = SnmpRespCheck_OK;
		if (respPdu == null) {
			err.append("Unkonwn Error");
			result = SnmpRespCheck_UnknonwErr;
		} else if (respPdu.getResult() != UdcSnmpContext.Success) {
			err.append("TimeOut Error");
			result = SnmpRespCheck_Timeout;
		} else if (respPdu.getErrorStatus() != UdcSnmpConstants.SNMP_ERR_NOERROR) {
			err.append("PDU Error, ErrorStatus[" + respPdu.getErrorStatus() + "] ErrorIndex[" + respPdu.getErrorIndex() + "]");
			result = SnmpRespCheck_ResError;
		}

		if (result == SnmpRespCheck_OK) {
			UdcVarbind vb;
			LinkedList vblist = respPdu.getVarbindlist();
			for (int i=0; i<vblist.size(); i++) {
				vb = (UdcVarbind)vblist.get(i);	
				if (vb.getValue() instanceof UdcAsnPrimitive) {
					UdcAsnPrimitive pri = (UdcAsnPrimitive)vb.getValue();
					err.append("PDU include Primitive, type = " + ((pri.getType() & 0x7F) + 128));
					result = SnmpRespCheck_PrimitiveError;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * SNMP-GETNEXT応答PDUの正常性チェックを行う。
	 * 	@param	respPdu SNMP-GET応答PDU
	 *	@param	err	エラー情報格納エリア
	 *	@return	0:正常/非0:異常(SnmpRespCheck_XXXXX)
	 */
	public static int checkResponse_next(UdcPdu respPdu, StringBuffer err)
	{
		int result = SnmpRespCheck_OK;
		if (respPdu == null) {
			err.append("Unkonwn Error");
			result = SnmpRespCheck_UnknonwErr;
		} else if (respPdu.getResult() != UdcSnmpContext.Success) {
			err.append("TimeOut Error");
			result = SnmpRespCheck_Timeout;
		} else if (respPdu.getErrorStatus() != UdcSnmpConstants.SNMP_ERR_NOERROR) {
			if (respPdu.getErrorStatus() != UdcSnmpConstants.SNMP_ERR_NOSUCHNAME
			  && respPdu.getErrorStatus() != UdcSnmpConstants.SNMP_VAR_NOSUCHINSTANCE
			  && respPdu.getErrorStatus() != UdcSnmpConstants.SNMP_VAR_ENDOFMIBVIEW) {
				err.append("PDU Error, ErrorStatus[" + respPdu.getErrorStatus() + "] ErrorIndex[" + respPdu.getErrorIndex() + "]");
				result = SnmpRespCheck_ResError;
			}
		}

		if (result == SnmpRespCheck_OK) {
			UdcVarbind vb;
			LinkedList vblist = respPdu.getVarbindlist();
			for (int i=0; i<vblist.size(); i++) {
				vb = (UdcVarbind)vblist.get(i);	
				if (vb.getValue() instanceof UdcAsnPrimitive) {
					UdcAsnPrimitive pri = (UdcAsnPrimitive)vb.getValue();
					if (pri.getType() != UdcSnmpConstants.SNMP_VAR_NOSUCHINSTANCE && pri.getType() != UdcSnmpConstants.SNMP_VAR_ENDOFMIBVIEW) {
						err.append("PDU include Primitive, type = " + ((pri.getType() & 0x7F) + 128));
						result = SnmpRespCheck_PrimitiveError;
						break;
					}
				}
			}
		}
		return result;
	}
}

