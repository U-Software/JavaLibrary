/* *********************************************************************
 * @(#)UdcAsnSequence.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;


/**
 * ASN1 SEQUENCE型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
class UdcAsnSequence extends UdcAsnObject
{
	/**
	 * SEQUENCEの構成要素列。
	 * @since	UDC1.0
	 */
	protected LinkedList children;

	/**
	 * コンストラクタ
	 * ASN1識別子は CONS_SEQ で設定される。
	 *
	 * @since	UDC1.0
	 */
	UdcAsnSequence()
	{
		this(CONS_SEQ);
	}

	/**
	 * コンストラクタ
	 *
	 * @param	oddtype	ASN1識別子
	 * @since	UDC1.0
	 */
	UdcAsnSequence(byte oddtype)
	{
		type     = oddtype;
		children = new LinkedList();
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
	UdcAsnSequence(InputStream in, int len, int pos) throws IOException
	{
		this();
		UdcAsnObject a = null;
		while (true) {
			a = AsnReadHeader(in, pos);
			if (a != null) {
				pos += (a.headerLength + a.contentsLength);
				add(a);
			} else {
				break; // all done
			}
		}
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
		UdcAsnSequence seq = new UdcAsnSequence(type);
		for (int i=0; i<children.size(); i++) {
			obj = (UdcAsnObject)children.get(i);
			seq.add((UdcAsnObject)obj.clone());
		}
		return seq;
	}

	/**
	 * SEQUENCEの構成要素に追加する。
	 *
	 * @return	追加したASN1構成要素情報
	 * @param	child 	追加するASN1構成要素情報
	 * @since	UDC1.0
	 */
	UdcAsnObject add(UdcAsnObject child)
	{
		if (child.isCorrect == true) {
			children.add(child);
		}
		return child;
	}

	/**
	 * Replaces one child by the other.
	 * This is used by v3 to insert the security / auth fingerprint.
	 */
	/**
	 * SEQUENCEの構成要素を再設定する。
	 *
	 * @return	再設定したASN1構成要素情報
	 * @param	oldChild	再設定されるASN1構成要素情報
	 * @param	newChild	再設定するASN1構成要素情報
	 * @since	UDC1.0
	 */
	UdcAsnObject replaceChild(UdcAsnObject oldChild, UdcAsnObject newChild)
	{
		UdcAsnObject obj;
		UdcAsnObject ret = oldChild;
		for (int i=0; i<children.size(); i++) {
			obj = (UdcAsnObject)children.get(i);
			if (obj == oldChild) {
				children.add(i, newChild);
				children.remove(obj);
				ret = newChild;
				break;
			}
		}
		return ret;
	}

	/**
	 * 値のASN1エンコード時のバイト長を取得する。
	 *
	 * @return	値のASN1エンコード時のバイト長
	 * @since	UDC1.0
	 */
	int size()
	{
		int sz = 0;
		int tmp;
		UdcAsnObject obj;
		for (int i=0; i<children.size(); i++) {
			obj = (UdcAsnObject)children.get(i);
			tmp = obj.size();
			tmp += (1+getLengthBytes(tmp));
			sz += tmp;
		}
		return sz;
	}

	/**
	 * ASN1エンコードを行う。
	 *
	 * @param	out	エンコード情報の出力先ストリーム
	 * @since	UDC1.0
	 */
	void write(OutputStream out) throws IOException
	{
		write(out, 0);
	}

	/**
	 * ASN1エンコードを行う。
	 *
	 * @param	out	エンコード情報の出力先ストリーム
	 * @param	pos	エンコード情報の出力位置(本クラスでは未使用)
	 * @since	UDC1.0
	 */
	void write(OutputStream out, int pos) throws IOException
	{
			// Output header
		int length = size();
		startPos = pos;
		AsnBuildHeader(out, type, length);
			// Output children
		pos += headerLength;
		UdcAsnObject child;
		for (int i=0; i<children.size(); i++) {
			child = (UdcAsnObject)children.get(i);
			child.write(out, pos);
			child.startPos = pos;
			pos += child.headerLength + child.contentsLength;
		}
	}

	/**
	 * 自身の構成要素がPDUである場合、PDUを返却する。
	 * エンコード・デコードした情報からPDU情報を検索するのに使用する。
	 *
	 * @return	PDU情報
	 * @since	UDC1.0
	 */
	UdcAsnObject findPdu()
	{
		UdcAsnObject res = null;
		UdcAsnObject child;
		for (int i=0; i<children.size(); i++) {
			child = (UdcAsnObject)children.get(i);
			res = child.findPdu();
			if (res != null) {
				break;
			}
		}
		if (this.isCorrect == false && res != null) {
			res.isCorrect = false;
		}
		return res;
	}

	/**
	 * 自身の構成要素がTrap-PDUである場合、Trap-PDUを返却する。
	 * エンコード・デコードした情報からTrap-PDU情報を検索するのに使用する。
	 *
	 * @return	PDU情報
	 * @since	UDC1.0
	 */
	UdcAsnObject findTrapPduv1()
	{
		UdcAsnObject res = null;
		UdcAsnObject child;
		for (int i=0; i<children.size(); i++) {
			child = (UdcAsnObject)children.get(i);
			res = child.findTrapPduv1();
			if (res != null) {
				break;
			}
		}
		if (this.isCorrect == false && res != null) {
			res.isCorrect = false;
		}
		return res;
	}

	/**
	 * 格納位置を指定してASN1構成要素情報を取得する。
	 *
	 * @return	ASN1構成要素情報
	 * @param	offset	取得する構成要素の格納位置
	 * @since	UDC1.0
	 */
	UdcAsnObject getObj(int offset)
	{
		return (UdcAsnObject)children.get(offset);
	}

	/**
	 * ASN1構成要素数を取得する。
	 *
	 * @return	ASN1構成要素数
	 * @since	UDC1.0
	 */
	int getObjCount()
	{
		return children.size();
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		return "";
	}

}
