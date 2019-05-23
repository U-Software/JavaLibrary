/* *********************************************************************
 * @(#)UdcAsnTrapPduv1Sequence.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;


/**
 * SNMPv1 Trap-PDU型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcAsnTrapPduv1Sequence extends UdcAsnSequence
{
	/**
	 * コンストラクタ
	 *
	 * @param	oddtype	ASN1識別子
	 * @since	UDC1.0
	 */
	UdcAsnTrapPduv1Sequence(byte oddtype)
	{
		super(oddtype);
	}

	/**
	 * コンストラクタ
	 * 受信情報からデコードによるコンストラクタ。
	 *
	 * @param	in		デコード入力情報(受信メッセージ)
	 * @param	len		デコード長
	 * @param	pos		デコード情報対象格納位置(単位：バイト)
	 * @since	UDC1.0
	 */
	UdcAsnTrapPduv1Sequence(InputStream in, int len, int pos) throws IOException
	{
		super(in,len,pos);
	}

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		UdcAsnObject obj;
		UdcAsnTrapPduv1Sequence seq = new UdcAsnTrapPduv1Sequence(type);
		for (int i=0; i<children.size(); i++) {
			obj = (UdcAsnObject)children.get(i);
			seq.add((UdcAsnObject)obj.clone());
		}
		return seq;
	}

	/**
	 * 自身がTrap-PDUである場合、自身を返却する。
	 * エンコード・デコードした情報からTrap-PDU情報を検索するのに使用する。
	 *
	 * @return	PDU情報(自インスタンス)
	 * @since	UDC1.0
	 */
	UdcAsnObject findTrapPduv1()
	{
		return this;
	}

}
