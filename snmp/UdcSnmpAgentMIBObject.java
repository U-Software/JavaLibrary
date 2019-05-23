/* *********************************************************************
 * @(#)UdcSnmpAgentMIBObject.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.lang.*;

import udc.util.*;

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
 * 本クラスはこのMIBオブジェクトクラスに対応します。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcSnmpAgentMIB
 * @see		UdcSnmpAgentMIBLeaf
 * @since	UDC1.0
 */
public class UdcSnmpAgentMIBObject extends UdcBranchElem implements UdcSnmpConstants
{
	/**
	 * MIB-Object種別の一つで、グループオブジェクトを表す。
	 * 自身の子ObjectにLeafオブジェクトを持つ、Table/Entryでないオブジェクト種別。
	 * @since	UDC1.0
	 */
	public static final int	MIBGroup	= 1;

	/**
	 * MIB-Object種別の一つで、テーブルオブジェクトを表す。
	 * 自身の子ObjectにEntryオブジェクトを持つ、オブジェクト種別。
	 * @since	UDC1.0
	 */
	public static final int	MIBTable	= 2;

	/**
	 * MIB-Object種別の一つで、エントリオブジェクトを表す。
	 * 自身の子ObjectにLeafオブジェクトを持つ、オブジェクト種別。
	 * @since	UDC1.0
	 */
	public static final int	MIBEntry	= 3;

	/**
	 * MIB-Object種別の一つで、リーフオブジェクトを表す。
	 * 本種別に対応するMIBオブジェクトは本クラスを継承したUdcSnmpAgentMIBLead。
	 * @see		UdcSnmpAgentMIBLeaf
	 * @since	UDC1.0
	 */
	public static final int	MIBLeaf		= 4;

	/**
	 * SNMP-Agentの操作対象となる本クラスあるいはUdcSnmpAgentMIBLeaf
	 * を管理するMIB管理クラス。
	 * @since	UDC1.0
	 */
	protected UdcSnmpAgentMIB			mib;

	/**
	 * MIBオブジェクトID。
	 * @since	UDC1.0
	 */
	protected UdcAsnObjectId			objectId;

	/**
	 * MIBオブジェクトのASN1型。
	 * @since	UDC1.0
	 */
	protected int						objectType = -1;

	/**
	 * MIBオブジェクトのインデックス長（単位：バイト）。
	 * @since	UDC1.0
	 */
	protected int						indexLen = 0;

	/**
	 * コンストラクタ。
	 *
	 * @param	oid			MIBオブジェクトのOID文字列
	 * @param	mibType		MIBオブジェクトのASN1型
	 * @param	indexlen	MIBオブジェクトのインデックス長（単位：バイト）。
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject(String oid, int mibType, int indexlen)
	{
		this(new UdcAsnObjectId(oid), mibType,indexlen);
	}

	/**
	 * コンストラクタ。
	 *
	 * @param	oid			MIBオブジェクトのOID
	 * @param	mibType		MIBオブジェクトのASN1型
	 * @param	indexlen	MIBオブジェクトのインデックス長（単位：バイト）。
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject(UdcAsnObjectId oid, int mibType, int indexlen)
	{
		objectId = oid;
		objectType = mibType;
		indexLen = indexlen;
	}

	/**
	 * MIB管理クラスを設定する。
	 * 本メンバ関数は、UdcSnmpAgentMIB.addによって登録する際に内部で
	 * 自動的に設定されます。但し、addで登録するMIBオブジェクトのみ設定
	 * するため、その子オブジェクトへの設定は行っていないことに注意。
	 * また、本インスタンスの子オブジェクトとして本クラスメンバ関数addで
	 * 登録する際も同様に自身のMIB管理クラスが子オブジェクトに設定されます。
	 *
	 * @param	m	MIB管理クラス
	 * @since	UDC1.0
	 */
	public void setAgentMIB(UdcSnmpAgentMIB m)
	{
		mib = m;
	}

	/**
	 * MIBオブジェクトのOIDを取得する。
	 * 返却されるOIDは複製であることに注意。
	 *
	 * @return	MIBオブジェクトのOID
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId getObjectId()
	{
		UdcAsnObjectId oid;
		try {
			oid = (UdcAsnObjectId)objectId.clone();
		} catch(CloneNotSupportedException exp) {
			oid = null;
		}
		return oid;
	}

	/**
	 * MIBオブジェクトのASN1型を取得する。
	 *
	 * @return	MIBオブジェクトのASN1型
	 * @since	UDC1.0
	 */
	public int getObjectType()
	{
		return objectType;
	}

	/**
	 * MIBオブジェクトのインデックス長（単位：バイト）を取得する。
	 *
	 * @return	MIBオブジェクトのインデックス長（単位：バイト）
	 * @since	UDC1.0
	 */
	public int getIndexLen()
	{
		return indexLen;
	}

	/**
	 * 自身の子オブジェクトとしてMIBオブジェクトを登録する。
	 *
	 * @return	自身の子オブジェクトとして登録したMIBオブジェクト
	 * @param	childMib	自身の子オブジェクトとして登録するMIBオブジェクト
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject add(UdcSnmpAgentMIBObject childMib)
	{
		int ret = 0;
		int mylen = this.objectId.getValues().length;
		int len = childMib.objectId.getValues().length;
		if (len <= mylen || (ret=this.objectId.compareTo(childMib.objectId,mylen)) != 0) {
			return null;
		}

		UdcSnmpAgentMIBObject elm;
		for (int i=0; i<sizeBranch(); i++) {
			elm = (UdcSnmpAgentMIBObject)get(i);
			int elmlen = elm.objectId.getValues().length;
			if (elmlen == len) {
				ret = childMib.objectId.compareTo(elm.objectId);
			} else {
				if (elmlen > len) {
					ret = elm.objectId.compareTo(childMib.objectId,len);
				} else if (elmlen < len) {
					ret = childMib.objectId.compareTo(elm.objectId,elmlen);
				}
			}
			if (ret == 0) {
				return null;
			} else if (ret < 0) {
				addBranch(i,elm);
				childMib.setAgentMIB(mib);
				return childMib;
			}
		}
		addBranch(childMib);
		childMib.setAgentMIB(mib);
		return childMib;
	}

	/**
	 * 要求されたOIDからインデックスを取り出す。
	 *
	 * @return	インデックスOID
	 * @param	oid		OID
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId getIndex(UdcAsnObjectId oid)
	{
		int len = oid.getSize() - objectId.getSize();
		if (len < 0) { len = 0; }
		return oid.trimOid(len);
	}

	/**
	 * ユーザオーバライド関数。
	 * SNMP-Requestを受信すると、対応するMIBオブジェクトをUdcSnmpAgentMIBから
	 * 検索し、operateXXX　メンバ関数をコールします。本メンバ関数は operateXXX
	 * をコールする直前で、要求されたOIDのインデックス値の正常を確認するために
	 * ユーザに提供されます。
	 * 通常Leaf以外のOIDがSNMPオペレーションの対象になることはないため、
	 * デフォルトは false　としています。
	 *
	 * @return	正常(true)か否(false)か
	 * @param	oid		チェックするOID
	 * @since	UDC1.0
	 */
	public boolean checkIndex(UdcAsnObjectId oid)
	{
		return false;
	}

	/**
	 * ユーザオーバライド関数。
	 * SNMP-Request(GET)を受信すると、対応するMIBオブジェクトをUdcSnmpAgentMIBから
	 * 検索し、本メンバ関数をコールします。
	 * ユーザでオーバライドする場合、本メンバ関数では以下の処理を行ってください。
	 *  <return について>
	 * 	  正常時は、SNMP_ERR_NOERRORを返却する。
	 *    異常時は、UdcSnmpConstantsに定義されるSNMP-errorStatusに対応する値を返却する。
	 *  <GET-応答情報について>
	 *　　replyVbに対応する情報を設定する。
	 *    tr(トランザクション)はユーザ定義によるものです。一つのGET要求で同一情報を何度も
	 *    取得すると非効率であるため、内部でキャッシングする等の処理を行うために提供されます。
	 *
	 * @param	reply		要求に対する応答用PDU
	 * @param	replyVb		要求に対する応答用VarBind
	 * @param	reqPdu		要求PDU
	 * @param	reqVbCnt	要求PDUのVarBind格納位置
	 * @param	reqVb		要求PDUのVarBind
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	public int operateGet(UdcPdu reply, UdcVarbind replyVb, UdcPdu reqPdu, int reqVbCnt, UdcVarbind reqVb, UdcSnmpTransactionFace tr)
	{
		return reply.setError(SNMP_ERR_NOSUCHNAME,0);
	}

	/**
	 * ユーザオーバライド関数。
	 * SNMP-Request(GET-NEXT)を受信すると、対応するMIBオブジェクトをUdcSnmpAgentMIBから
	 * 検索し、本メンバ関数をコールします。
	 * ユーザでオーバライドする場合、本メンバ関数では以下の処理を行ってください。
	 *  <return について>
	 * 	  正常時は、SNMP_ERR_NOERRORを返却する。
	 *    異常時は、UdcSnmpConstantsに定義されるSNMP-errorStatusに対応する値を返却する。
	 *  <GETNEXT-応答情報について>
	 *　　replyVbに対応する情報を設定する。
	 *	  GET-NEXTでは reqVb で指定されるNEXT情報を取得する必要があります。
	 *　　　・reqVbのOIDが自身のOIDと同一であれば、同一OIDの次の情報を取得・設定する。
	 *        もし、同一OIDに次の情報が存在しない場合は、次のOIDから情報を取得する必要があるため
	 *		  UdcNextMIBObjectException例外を発生させ、UDC-SNMP-Agent機構で次OID処理に遷移させて下さい。
	 *      ・reqVbのOIDが自身と異なる場合、前OIDの情報がなかったためにコールされているため、自身のOID
	 *        の先頭情報をreplyVbに設定する。この時replyVbのObjectIdも更新することを忘れないで下さい。
	 *　注意）
	 *    OID[x.x.・・・.x.0] で指定されるMIBエントリ配下のLeafでない場合、operateNextでは、自身のOIDに対応した情報
	 *    replyVbに設定してください。UDC-SNMP-Agent機構で要求されたOIDから自動的に次OIDを検索し、コール
	 *    しています。
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
		return reply.setError(SNMP_ERR_NOSUCHNAME,0);
	}

	/**
	 * ユーザオーバライド関数。
	 * SNMP-Request(SET)を受信すると、対応するMIBオブジェクトをUdcSnmpAgentMIBから
	 * 検索し、本メンバ関数をコールします。
	 * ユーザでオーバライドする場合、本メンバ関数では以下の処理を行ってください。
	 *  <return について>
	 * 	  正常時は、SNMP_ERR_NOERRORを返却する。
	 *    異常時は、UdcSnmpConstantsに定義されるSNMP-errorStatusに対応する値を返却する。
	 *  <SET-応答情報について>
	 *　　replyVbに設定した情報を設定する。reqVbと同一情報であるため、reqVb情報の複製を設定
	 *    するのが通常です。
	 *    tr(トランザクション)はユーザ定義によるものです。一つのGET要求で同一情報を何度も
	 *    取得すると非効率であるため、内部でキャッシングする等の処理を行うために提供されます。
	 *
	 * @param	reply		要求に対する応答用PDU
	 * @param	replyVb		要求に対する応答用VarBind
	 * @param	reqPdu		要求PDU
	 * @param	reqVbCnt	要求PDUのVarBind格納位置
	 * @param	reqVb		要求PDUのVarBind
	 * @param	tr			トランザクション
	 * @since	UDC1.0
	 */
	public int operateSet(UdcPdu reply, UdcVarbind replyVb, UdcPdu reqPdu, int reqVbCnt, UdcVarbind reqVb, UdcSnmpTransactionFace tr)
	{
		return reply.setError(SNMP_ERR_NOSUCHNAME,0);
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString(String indent)
	{
		String str = indent + objectId.toString();
		if (indexLen > 0) {
			str += "   (indexLen:"+ indexLen + ")\n";
		} else {
			str += "\n";
		}
		indent += " ";
		UdcSnmpAgentMIBObject child;
		for (int i=0; i<sizeBranch(); i++) {
			child = (UdcSnmpAgentMIBObject)get(i);
			str += child.toString(indent);
		}
		return str;
	}
}

