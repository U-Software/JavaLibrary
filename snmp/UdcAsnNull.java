/* *********************************************************************
 * @(#)UdcAsnNull.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;


/**
 * ASN1 NULL型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcAsnNull extends UdcAsnObject
{
	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcAsnNull()
	{
	}

	/**
	 * コンストラクタ
	 * 受信情報からデコードによるコンストラクタ。
	 *
	 * @param	in		デコード入力情報(受信メッセージ)
	 * @param	len		デコード長
	 * @since	UDC1.0
	 */
	public UdcAsnNull(InputStream in, int len)
	{
		this();
	}

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		return new UdcAsnNull();
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
		AsnBuildHeader(out, ASN_NULL, 0);
	}

	/**
	 * ASN1デコードを行う。
	 *
	 * @param	in	デコード情報格納ストリーム
	 * @param	len	デコード対象情報長
	 * @since	UDC1.0
	 */
	void read(InputStream in, int len) throws IOException
	{
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		return "AsnNull";
	}

}
