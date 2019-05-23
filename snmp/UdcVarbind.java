/* *********************************************************************
 * @(#)UdcVarbind.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import udc.util.*;


/**
 * VarBindクラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcPdu
 * @since	UDC1.0
 */
public class UdcVarbind implements Cloneable
{
	/**
	 * ASN1 ObjectId
	 * @since	UDC1.0
	 */
	private UdcAsnObjectId 	name;

	/**
	 * ASN1 ObjectValue
	 * @since	UDC1.0
	 */
	private UdcAsnObject 	value;

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcVarbind ()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param	var	VarBind
	 * @since	UDC1.0
	 */
	public UdcVarbind (UdcVarbind var)
	{
		name  = var.name;
		value = var.value;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	oid		ASN1-ObjectId文字列
	 * @since	UDC1.0
	 */
	public UdcVarbind(String oid)
	{
		name = new UdcAsnObjectId(oid);
		value = new UdcAsnNull();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	oid		ASN1-ObjectId
	 * @since	UDC1.0
	 */
	public UdcVarbind(UdcAsnObjectId oid)
	{
		name = oid;
		value = new UdcAsnNull();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	oid		ASN1-ObjectId文字列
	 * @param	val		ASN1-ObjectValue
	 * @since	UDC1.0
	 */
	public UdcVarbind(String oid, UdcAsnObject val)
	{
		name = new UdcAsnObjectId(oid);
		value = val;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	oid		ASN1-ObjectId
	 * @param	val		ASN1-ObjectValue
	 * @since	UDC1.0
	 */
	public UdcVarbind(UdcAsnObjectId oid, UdcAsnObject val)
	{
		name = oid;
		value = val;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	vb	ASN1-ObjectId/Vaklueを含んだSEQUENCE
	 * @since	UDC1.0
	 */
	UdcVarbind(UdcAsnSequence vb)
	{
		name  = (UdcAsnObjectId) vb.getObj(0);
		value = vb.getObj(1);
	}

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcVarbind vb = new UdcVarbind();
		if (name != null) {
			vb.name = (UdcAsnObjectId)name.clone();
		}
		if (value != null) {
			vb.value = (UdcAsnObject)value.clone();
		}
		return vb;
	}

	/**
	 * ASN1-ObjectIdを取得する。
	 *
	 * @return	ASN1-ObjectId
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId getOid()
	{
		return name;
	}

	/**
	 * ASN1-ObjectValueを取得する。
	 *
	 * @return	ASN1-ObjectValue
	 * @since	UDC1.0
	 */
	public UdcAsnObject getValue()
	{
		return value;
	}

	/**
	 * 自インスタンスにASN1-ObjectId/Valueを設定する。
	 *
	 * @param	vb		VarBind
	 * @since	UDC1.0
	 */
	public Object setValue(UdcAsnSequence vb)
	{
		name  = (UdcAsnObjectId)vb.getObj(0);
		value = vb.getObj(1);
		return value;
	}

	/**
	 * 自インスタンスにASN1-ObjectId/Valueを設定する。
	 *
	 * @param	oid		ASN1-ObjectId文字列
	 * @param	val		ASN1-ObjectValue
	 * @since	UDC1.0
	 */
	public void setValue(String oid, UdcAsnObject val)
	{
		if (oid != null) {
			name = new UdcAsnObjectId(oid);
		}
		if (val != null) {
			value = val;
		}
	}

	/**
	 * 自インスタンスにASN1-ObjectId/Valueを設定する。
	 *
	 * @param	oid		ASN1-ObjectId
	 * @param	val		ASN1-ObjectValue
	 * @since	UDC1.0
	 */
	public void setValue(UdcAsnObjectId oid, UdcAsnObject val)
	{
		if (oid != null) {
			name = oid;
		}
		if (val != null) {
			value = val;
		}
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		return (name.toString() + ": " + value.toString());
	}
}
