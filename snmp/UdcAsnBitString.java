/* *********************************************************************
 * @(#)UdcAsnBitString.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;

/**
 * ASN1 OCTET型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcAsnBitString extends UdcAsnObject
{
	public static String HEX_PREFIX = "0x";
	public static byte mask[] = { (byte)0xFF, (byte)0xFE, (byte)0xFC, (byte)0xF8, (byte)0xF0, (byte)0xE0, (byte)0xC0, (byte)0x80};

	/**
	 * BIT-OCTET値。
	 * @since	UDC1.0
	 */
	byte value[];

	/**
	 * 最後のバイトの未使用ビット数
	 * @since	UDC1.0
	 */
	byte nouseLen;

	/**
	 * コンストラクタ
	 *
	 * @param	s	オクテット列
	 * param	nouselen	最後のバイトの未使用ビット数
	 * @since	UDC1.0
	 */
	public UdcAsnBitString(byte s[], byte nouselen) throws IllegalArgumentException
	{
		if (s == null || s.length < 1 || nouselen >= 8) { 
			throw new IllegalArgumentException("Value is illegal");
		}
		type = ASN_BIT_STR;
		value = new byte[s.length+1];
		for (int i=0; i<s.length; i++) { value[i+1] = s[i]; }
		value[s.length] &= mask[nouselen];
		value[0] = nouselen;
	}

	/**
     * コンストラクタ
     * 受信情報からデコードによるコンストラクタ。
     *
     * @param   in      デコード入力情報(受信メッセージ)
     * @param   len     デコード長
     * @since   UDC1.0
     */
    public UdcAsnBitString(InputStream in, int len) throws IOException
    {
        read(in,len);
    }

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		byte[] vals = new byte[value.length-1];
		for (int i=1; i<value.length; i++) {
			vals[i-1] = value[i];
		}
		return new UdcAsnBitString(vals, value[0]);
	}

	/**
	 * OCTET値を取得する。
	 *
	 * @return	OCTET値
	 * @since	UDC1.0
	 */
	public byte[] getBytes()
	{
		byte val[] = new byte[value.length-1];
		for (int i=1; i<value.length; i++) {
			val[i-1] = value[i];
		}
		return val;
	}

	/**
	 * 値のASN1エンコード時のバイト長を取得する。
	 *
	 * @return	値のASN1エンコード時のバイト長
	 * @since	UDC1.0
	 */
	int size()
	{
		return value.length;
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
			/* Asn1Type , Asn1Length */
		AsnBuildHeader(out, type, value.length);
			/* Asn1Value */
		for(int idx=0; idx<value.length; idx++) {
			out.write(value[idx]);
		}
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
		value = new byte[len];
		if (len != 0) {
			if (len==in.read(value,0,len)) {
				value[len-1] &= mask[value[0]];
			} else {
				throw new IOException("UdcAsnBitString(): Not enough data");
			}
		}
	}

	/**
	 * インスタンス情報文字列を取得する。
	 * フォーマットは、[0xXX:0xXX:0xXX: ...]
	 *
	 * @return	ヘキサ文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		String str = "";
		if (value.length >= 2) {
			for (int i=1; i<value.length; i++) { str += UdcCoder.byte2HexString(value[i]) + ":"; }
		}
		return str;
	}
}
