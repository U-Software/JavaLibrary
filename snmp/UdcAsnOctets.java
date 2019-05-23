/* *********************************************************************
 * @(#)UdcAsnOctets.java 1.0, 18 Jan 2003
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
public class UdcAsnOctets extends UdcAsnObject
{
	public static String HEX_PREFIX = "0x";

	/**
	 * OCTET値。
	 * @since	UDC1.0
	 */
	byte value[];

	/**
	 * コンストラクタ
	 *
	 * @param	s	キャラクタ列
	 * @since	UDC1.0
	 */
	public UdcAsnOctets(char s[])
	{
		value = new byte[s.length];
		type = ASN_OCTET_STR;
		for(int idx=0; idx<s.length; idx++) {
			value[idx] = (byte)s[idx];
		}
	}

	/**
	 * コンストラクタ
	 *
	 * @param	s	文字列
	 * @since	UDC1.0
	 */
	public UdcAsnOctets(String s)
	{
		this(s.toCharArray());
	}

	/**
	 * コンストラクタ
	 *
	 * @param	s	オクテット列
	 * @since	UDC1.0
	 */
	public UdcAsnOctets(byte s[]) throws IllegalArgumentException
	{
		this(s, ASN_OCTET_STR);
	}

	/**
	 * コンストラクタ
	 *
	 * @param	s	オクテット列
	 * @param	t	ASN1識別子
	 * @since	UDC1.0
	 */
	public UdcAsnOctets(byte s[], byte t) throws IllegalArgumentException
	{
		value = s;
		type = t;
		if (value == null) {
			throw new IllegalArgumentException("Value is null");
		}
	}

	/**
	 * コンストラクタ
	 * 受信情報からデコードによるコンストラクタ。
	 *
	 * @param	in		デコード入力情報(受信メッセージ)
	 * @param	len		デコード長
	 * @since	UDC1.0
	 */
	public UdcAsnOctets(InputStream in, int len) throws IOException
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
		byte[] vals = new byte[value.length];
		for (int i=0; i<value.length; i++) {
			vals[i] = value[i];
		}
		return new UdcAsnOctets(vals,type);
	}

	/**
	 * OCTET値を文字列に変換したものを取得する。
	 *
	 * @return	OCTET値を文字列に変換したもの
	 * @since	UDC1.0
	 */
	public String getValue()
	{
		return toString();
	}

	/**
	 * OCTET値を取得する。
	 *
	 * @return	OCTET値
	 * @since	UDC1.0
	 */
	public byte[] getBytes()
	{
		return value;
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
			if (len == in.read(value,0,len)) {
				String str = "";
				str = new String(value);
			} else {
				throw new IOException("UdcAsnOctets(): Not enough data");
			}
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
		String str = "";

		if (type == IPADDRESS) {
			str = toIpAddress();
		} else if (type == OPAQUE) {
			str = HEX_PREFIX + toHex();
		} else {
			int length = value.length;
			int b = ' '; // the first printable char in the ASCII table
			int e = '~'; // the last printable char in the ASCII table
			boolean isPrintable = true;
			int i=0;
			while (i<length && isPrintable) {
				if (i == 0) {
					isPrintable = ((value[i] >= b && value[i] <= e) || Character.isWhitespace((char)value[i]));
				} else {
					isPrintable = ((value[i] >= b && value[i] <= e) || Character.isWhitespace((char)value[i]) || value[i] == 0);
				}
				i++;
			}

			if (isPrintable) {
				str = new String(value);
			} else {
				str = HEX_PREFIX + toHex();
			}
		}
		return str;
	}

	/**
	 * 自身がIPADDRESS型であった場合のIPアドレス文字列を取得する。
	 * フォーマットは、[xx.xx.xx.xx]
	 *
	 * @return	IPアドレス文字列
	 * @since	UDC1.0
	 */
	public String toIpAddress()
	{
		String str = "";
		int length, val;
		length = value.length;
		if (length > 0) {
			for (int i=0; i<length-1; i++) {
				val = (int) value[i];
				if (val <0) {
					val += 256;
				}
				str = str + String.valueOf(val) + ".";
			}
			val = (int) value[length-1];
			if (val <0) {
				val += 256;
			}
			str = str + String.valueOf(val);
		}
		return str;
	}

	/**
	 * 自身がOPAQUE型であった場合のヘキサ文字列を取得する。
	 * フォーマットは、[0xXX:0xXX:0xXX: ...]
	 *
	 * @return	ヘキサ文字列
	 * @since	UDC1.0
	 */
	public String toHex()
	{
		String str = "";
		int length = value.length;
		if (length > 0) {
			for (int i=0; i<length-1; i++) {
				str = str + UdcCoder.byte2HexString(value[i]) + ":";
			}
			str = str + UdcCoder.byte2HexString(value[length-1]);
		}
		return str;
	}

	/**
	 * オクテット値を表示文字列とする。
	 *
	 * @return	表示文字列
	 * @since	UDC1.0
	 */
	public String toDisplayString()
	{
		String str = "";
		int length = value.length;
		if (length > 0) {
			str = new String(value);
		}
		return str;
	}

	/**
	 * オクテット値変換時のヘキサプレフィックスを設定する。
	 *
	 * @param	newPrefix	オクテット値変換時のヘキサプレフィックス。
	 * @since	UDC1.0
	 */
	public static void setHexPrefix(String newPrefix)
	{
		HEX_PREFIX = newPrefix;
	}

}
