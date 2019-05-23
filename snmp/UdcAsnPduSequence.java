/* *********************************************************************
 * @(#)UdcAsnPduSequence.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;

/**
 * ASN1 SEQUENCE(PDU情報時の)型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcAsnPduSequence extends UdcAsnSequence implements Cloneable
{
	/**
	 * コンストラクタ
	 *
	 * @param	oddtype	ASN1識別子
	 * @since	UDC1.0
	 */
	public UdcAsnPduSequence(byte oddtype)
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
	public UdcAsnPduSequence(InputStream in, int len, int pos) throws IOException
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
		UdcAsnPduSequence seq = new UdcAsnPduSequence(type);

		for (int i=0; i<children.size(); i++) {
			obj = (UdcAsnObject)children.get(i);
			seq.add((UdcAsnObject)obj.clone());
		}
		return seq;
	}

	/**
	 * 自身がPDUである場合、自身を返却する。
	 * エンコード・デコードした情報からPDU情報を検索するのに使用する。
	 *
	 * @return	PDU情報(自インスタンス)
	 * @since	UDC1.0
	 */
	UdcAsnObject findPdu()
	{
		return this;
	}
}
