/* *********************************************************************
 * @(#)UdcSnmpAgentMIBLeaf.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.util.*;
import java.io.*;
import java.net.*;



/**
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
 * 本クラスはこのMIBオブジェクトクラスの特にLeafオブジェクトに対応します。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcSnmpAgentMIB
 * @see		UdcSnmpAgentMIBObject
 * @since	UDC1.0
 */
public class UdcSnmpAgentMIBLeaf extends UdcSnmpAgentMIBObject
{
	/**
	 * コンストラクタ。
	 *
	 * @param	oid			MIBオブジェクトのOID文字列
	 * @param	indexlen	MIBオブジェクトのインデックス長（単位：バイト）。
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBLeaf(String oid, int indexlen)
	{
		super(oid,MIBLeaf,indexlen);
	}

	/**
	 * コンストラクタ。
	 *
	 * @param	oid			MIBオブジェクトのOID
	 * @param	indexlen	MIBオブジェクトのインデックス長（単位：バイト）。
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBLeaf(UdcAsnObjectId oid, int indexlen)
	{
		super(oid,MIBLeaf,indexlen);
	}

	/**
	 * OID[x.x.・・・.x.0] で指定されるMIBエントリ配下のLeafでない場合のインデックス値の
	 * チェックを行うユーティリティメンバ関数。
	 * MIBエントリ配下のLeafでない場合のLeafのcheckIndexメンバ関数では、本メンバ関数をコール
	 * するだけで済みます。
	 *
	 * @return	正常(true)か否(false)か
	 * @param	oid		チェックするOID
	 * @since	UDC1.0
	 */
	public boolean checkIndexForGroupLeaf(UdcAsnObjectId oid)
	{
		UdcAsnObjectId indexOid = getIndex(oid);
		if (indexOid.getValue((int)0) != 0) {
			return false;
		}
		return true;
	}

	/**
	 * 応答PDU(reply)のVarBindList中で指定したOID(orgOid)のVarBindを再設定する。
	 *
	 * @param	orgOid	再設定するVarBind:ObjectId
	 * @param	reply	再設定する応答PDU
	 * @param	index	再設定するVarBind:ObjectIdのインデックス値
	 * @param	val		再設定するVarBind:ObjectValue
	 * @since	UDC1.0
	 */
	public void replaceReply(UdcAsnObjectId orgOid, UdcPdu reply, long index, UdcAsnObject val)
	{
		UdcAsnObjectId oid = getObjectId();
		oid.addOid(index);
		reply.replaceOid(orgOid,oid,val);
	}

	/**
	 * 応答PDU(reply)のVarBindList中で指定したOID(orgOid)のVarBindを再設定する。
	 *
	 * @param	orgOid	再設定するVarBind:ObjectId
	 * @param	reply	再設定する応答PDU
	 * @param	index	再設定するVarBind:ObjectIdのインデックス値
	 * @param	val		再設定するVarBind:ObjectValue
	 * @since	UDC1.0
	 */
	public void replaceReply(UdcAsnObjectId orgOid, UdcPdu reply, long[] index, UdcAsnObject val)
	{
		UdcAsnObjectId oid = getObjectId();
		for (int i=0; i<index.length; i++) {
			oid.addOid(index[i]);
		}
		reply.replaceOid(orgOid,oid,val);
	}

	/**
	 * UDC-SNMP-Agent機構では、OID[x.x.・・・.x.0] で指定されるMIBエントリ配下のLeafでない場合、要求されたOIDから
	 * 自動的に次OIDを検索し、コールしています。本クラスではデフォルトとしてoperateGetメンバ関数をコールする設定と
	 * し、、OID[x.x.・・・.x.0] で指定されるMIBエントリ配下のLeafでない場合のソース記述量を削減しています。
	 *
	 * @param	reply		要求に対する応答用PDU
	 * @param	replyVb		要求に対する応答用VarBind
	 * @param	reqPdu		要求PDU
	 * @param	reqVbCnt	要求PDUのVarBind格納位置
	 * @param	reqVb		要求PDUのVarBind
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	public int operateNext(UdcPdu reply, UdcVarbind replyVb, UdcPdu reqPdu, int reqVbCnt, UdcVarbind reqVb, UdcSnmpTransactionFace tr)
		throws UdcNextMIBObjectException
	{
		return operateGet(reply,replyVb,reqPdu,reqVbCnt,reqVb,tr);
	}
}

