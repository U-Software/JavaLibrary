/* *********************************************************************
 * @(#)UdcSnmpAgentMIB.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;


/**
 * SNMP-Agentの操作対象となるMIB管理クラス。
 * SNMP-Agent処理クラス構成は以下のように実装されています。
 * <br>
 *   UdcSnmpAgentContext　　SNMP-Agent処理の統括クラス <br>
 *　　　　　|<br>
 *   UdcSnmpAgentMIB　　　　操作対象MIBの管理クラス<br>
 *　　　　　|<br>
 *   UdcSnmpAgentMIBObject  操作対象MIBオブジェクトクラス（ユーザMIB処理を定義しないGroup/Table対応）<br>
 *   UdcSnmpAgentMIBLeaf　　操作対象MIBオブジェクトクラス（ユーザMIB処理を定義するLeaf対応）<br>
 *<br>
 * Agentで操作対象となるMIB-Objectは全て、本クラスに登録する必要がある。
 * Agent処理では、SNMP要求を受信すると、本クラスに登録されたMIB-Objectから
 * 操作対象を検索し、Agent処理を行う。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcSnmpAgentMIBObject
 * @see		UdcSnmpAgentMIBLeaf
 * @since	UDC1.0
 */
public class UdcSnmpAgentMIB implements UdcSnmpConstants
{
	/**
	 * SNMP-AgentのルートMIB-Object。
	 * @since	UDC1.0
	 */
	protected UdcSnmpAgentMIBObject		root = null;

	/**
	 * SNMP-Agentのバージョン。
	 * @since	UDC1.0
	 */
	protected byte						version = SNMP_VERSION_1;

	/**
	 * SNMP-Agentのコミュニティ。
	 * @since	UDC1.0
	 */
	protected String					community = "public";

	/**
	 * SNMP-Agent処理時のトランザクションインタフェース。
	 * @since	UDC1.0
	 */
	protected UdcSnmpTransactionFace	transaction = null;


	/**
	 * コンストラクタ。
	 *
	 * @param	rootOid	SNMP-AgentのルートMIB-ObjectのOID
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIB(UdcAsnObjectId rootOid)
	{
		root = new UdcSnmpAgentMIBObject(rootOid, UdcSnmpAgentMIBObject.MIBGroup, 0);
	}

	/**
	 * SNMP-Agentのバージョンを取得する。
	 *
	 * @return	SNMP-Agentのバージョン
	 * @since	UDC1.0
	 */
	public byte getVersion()
	{
		return version;
	}

	/**
	 * SNMP-Agentのバージョンを設定する。
	 *
	 * @param	ver	SNMP-Agentのバージョン
	 * @since	UDC1.0
	 */
	public void setVersion(byte ver)
	{
		version = ver;
	}

	/**
	 * SNMP-Agentのコミュニティを取得する。
	 *
	 * @return	SNMP-Agentのコミュニティ
	 * @since	UDC1.0
	 */
	public String getCommunity()
	{
		return community;
	}

	/**
	 * SNMP-Agentのコミュニティを設定する。
	 *
	 * @param	com	SNMP-Agentのコミュニティ
	 * @since	UDC1.0
	 */
	public void setCommunity(String com)
	{
		community = com;
	}

	/**
	 * SNMP-Agentの対象MIB-Objectを設定する。
	 *
	 * @param	childMib	SNMP-Agentの対象MIB-Object
	 * @return 	設定したMIB-Object。OIDが一致しない場合、設定されずに null が返却されます。
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject add(UdcSnmpAgentMIBObject childMib)
	{
		if (root.add(childMib) != null) {
			childMib.setAgentMIB(this);
			return childMib;
		}
		return null;
	}

	/**
	 * SNMP-Agent処理時のトランザクションインタフェースを設定する。
	 * ユーザ処理においてユーザトランザクションを使用する場合のみ本設定を
	 * 行う。
	 *
	 * @param	tran	SNMP-Agent処理時のトランザクションインタフェース
	 * @since	UDC1.0
	 */
	public void setTransactionFace(UdcSnmpTransactionFace tran)
	{
		transaction = tran;
	}

	/**
	 * SNMP-Agent処理時のトランザクションを生成する。
	 *
	 * @return	SNMP-Agent処理時のトランザクション
	 * @since	UDC1.0
	 */
	public UdcSnmpTransactionFace newTransaction() throws CloneNotSupportedException
	{
		if (transaction == null) {
			return null;
		}
		return (UdcSnmpTransactionFace)transaction.clone();
	}

	/**
	 * 登録された対象MIB-ObjectからGET-NEXTの対象となる次MIB-Objectを取得する。
	 *
	 * @return	curの次MIB-Object
	 * @param	cur	基点となるMIB-Object
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject getNextMibObject(UdcSnmpAgentMIBObject cur)
	{
		UdcSnmpAgentMIBObject obj = (UdcSnmpAgentMIBObject)cur.getNextLeaf();
		if (obj != null && obj.getObjectType() != UdcSnmpAgentMIBObject.MIBLeaf) {
			obj = null;
		}
		return obj;
	}

	/**
	 * 登録された対象MIB-Objectから指定されたOIDのMIB-Objectを取得する。
	 *
	 * @return	oidに対応するMIB-Object
	 * @param	oid	取得対象のMIB-ObjectのOID
	 * @since	UDC1.0
	 */
	public UdcSnmpAgentMIBObject getMIBObject(UdcAsnObjectId oid)
	{
		if (root.objectId.compareTo(oid,root.objectId.getValues().length) != 0) {
			return null;
		}
		UdcSnmpAgentMIBObject parent = root;
		UdcSnmpAgentMIBObject child = null;
		for (int i=0; i<parent.sizeBranch(); i++) {
			child = (UdcSnmpAgentMIBObject)parent.get(i);
			if (child.objectId.compareTo(oid,child.objectId.getValues().length) != 0) {
				continue;
			}
			int tmpIndLen = oid.getValues().length - child.objectId.getValues().length;
			if (tmpIndLen <= child.indexLen) {
				return child;
			}
			parent = child;
			i = -1;
		}
		return null;
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		String str = "Agent MIB : version[" + version + "]  community[" + community + "]\n";
		if (root != null) {
			str += root.toString(" ");
		}
		return str;
	}
}

