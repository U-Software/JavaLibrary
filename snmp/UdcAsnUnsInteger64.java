/* *********************************************************************
 * @(#)UdcAsnUnsInteger64.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;


/**
 * ASN1 UnsignedInteger(64bit)型情報クラス。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcAsnUnsInteger64 extends UdcAsnObject
{
	/**
	 * unsigned int(64bit)値。
	 * @since	UDC1.0
	 */
	protected long value;

	/**
	 * コンストラクタ
	 *
	 * @param	v	unsigned int(64bit)値
	 * @since	UDC1.0
	 */
	public UdcAsnUnsInteger64(long v)
	{
		this.value = v;
	}

	/**
	 * コンストラクタ
	 * 受信情報からデコードによるコンストラクタ。
	 *
	 * @param	in		デコード入力情報(受信メッセージ)
	 * @param	len		デコード長
	 * @since	UDC1.0
	 */
	public UdcAsnUnsInteger64(InputStream in, int len) throws IOException
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
		return new UdcAsnUnsInteger64(value);
	}

	/**
	 * unsigned int(64bit)値を取得する。
	 *
	 * @return	値
	 * @since	UDC1.0
	 */
	public long getValue()
	{
		return value;
	}

	/**
	 * 値のASN1エンコード時のビット長を取得する。
	 *
	 * @return	値のASN1エンコード時のビット長
	 * @since	UDC1.0
	 */
	int bitsize()
	{
		int empty = 0x00, sign = 0x00;
		if (value < 0) {
			empty = 0xFF;
			sign  = 0x80;
		}
		int  count;
		for(count=56; count>0; count-=8) {
			if ( ((value >> count) & 0xFF) != empty) {
				break;
			}
		}
		if (((value >> count) & 0x80) != sign) {
			count += 8;
		}
		return count;
	}

	/**
	 * 値のASN1エンコード時のバイト長を取得する。
	 *
	 * @return	値のASN1エンコード時のバイト長
	 * @since	UDC1.0
	 */
	int size()
	{
		int  count = bitsize();
		return (count>>3)+1;
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
		int count = bitsize();
		AsnBuildHeader(out, COUNTER64, (count>>3)+1);
			/* Asn1Value */
		for(; count>=0; count-=8) {
			out.write((byte)((value >> count) & 0xFF));
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
		byte data[] = new byte[len];
		if (len != in.read(data,0,len)) {
			throw new IOException("UdcSnmpAsnInteger.UdcSnmpAsnInteger: Illegal Asn1-Value");
		}
		value = UdcCoder.bytes2Long(data,false);
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		return (String.valueOf(value));
	}

}
