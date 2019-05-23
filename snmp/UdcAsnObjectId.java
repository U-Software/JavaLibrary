/* *********************************************************************
 * @(#)UdcAsnObjectId.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;

/**
 * ASN1 ObjectId型情報クラス。<br>
 * <br>
 * if (abcd != 0)          output 1.000abcd 1.efghijk 1.lmnopqr 1.stuvwxy 0.zABCDEF<br>
 * else if (efghijk != 0)  output 1.efghijk 1.lmnopqr 1.stuvwxy 0.zABCDEF<br>
 * else if (lmnopqr != 0)  output 1.lmnopqr 1.stuvwxy 0.zABCDEF<br>
 * else if (stuvwxy != 0)  output 1.stuvwxy 0.zABCDEF<br>
 * else                    output 0.zABCDEF<br>
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcAsnObjectId extends UdcAsnObject
{
	/**
	 * OID値。
	 * @since	UDC1.0
	 */
	private long value[] = {1,3,6,1,2,1,1,0};

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param	vals	OID値
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId(long[] vals)
	{
		int i;
		value = new long[vals.length];
		for (i=0; i<vals.length; i++) {
			value[i] = vals[i];
		}
	}

	/**
	 * コンストラクタ
	 *
	 * @param	s	OID文字列
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId(String s) throws IllegalArgumentException
	{
		read(s);
	}

	/**
	 * コンストラクタ
	 * 受信情報からデコードによるコンストラクタ。
	 *
	 * @param	in		デコード入力情報(受信メッセージ)
	 * @param	len		デコード長
	 * @since	UDC1.0
	 */
	UdcAsnObjectId(InputStream in, int len) throws IOException
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
		return new UdcAsnObjectId(value);
	}


	/**
	 * OIDが等しいかチェックする。
	 *
	 * @return	等しいか否か
	 * @param	oid OID
	 * @since	UDC1.0
	 */
	public boolean isSame(UdcAsnObjectId oid)
	{
		long tv[] = oid.getValues();	
		if (value.length != tv.length) { return false; }
		for (int i=0; i<value.length; i++) {
			if (value[i] != tv[i]) { return false; }
		}
		return true;
	}

	/**
	 * OID文字列を取得する。
	 *
	 * @return	OID文字列
	 * @since	UDC1.0
	 */
	public String getValue()
	{
		return toString();
	}

	/**
	 * 指定された位置のOID部分値を取得する。
	 *
	 * @return	OID部分値
	 * @param	index 取得するOID値の配列インデックス
	 * @since	UDC1.0
	 */
	public long getValue(int index)
	{
		if (index >= value.length) {
			return -1;
		}
		return value[index];
	}

	/**
	 * OID値を取得する。
	 *
	 * @return	OID値
	 * @since	UDC1.0
	 */
	protected long[] getValues()
	{
		return value;
	}

	/**
	 * 値のASN1エンコード時のバイト長を取得する。
	 *
	 * @return	値のASN1エンコード時のバイト長
	 * @since	UDC1.0
	 */
	public int getSize()
	{
		return value.length;
	}

	/**
	 * OIDからインデックスOIDのみを取得する。
	 *
	 * @return	インデックスOID値
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId trimOid(int indexLen)
	{
		int trimlen = value.length - indexLen;
		if (trimlen <= 0) {
			return null;
		}
		long[] tmpval = new long[indexLen];
		for (int i=0; i<indexLen; i++) {
			tmpval[i] = value[trimlen+i];
		}
		return new UdcAsnObjectId( tmpval );
	}

	/**
	 * OIDからインデックスOIDを取り除いたOIDを取得する。
	 *
	 * @return	インデックスOIDを取り除いたOID
	 * @param	indexLen	取り除くインデックスOID長
	 * @since	UDC1.0
	 */
	public UdcAsnObjectId trimIndex(int indexLen)
	{
		int trimlen = value.length - indexLen;
		if (trimlen <= 0) {
			return null;
		}
		long[] tmpval = new long[indexLen];
		for (int i=0; i<trimlen; i++) {
			tmpval[i] = value[i];
		}
		return new UdcAsnObjectId( tmpval );
	}

	/**
	 * 指定したOIDが自身が含まれているか判定する。
	 *
	 * @return	true:含まれる／false:含まれない
	 * @param	oid		比較するOID
	 * @since	UDC1.1
 	 */
	public boolean contain(UdcAsnObjectId oid)
	{
		if (oid.value.length > value.length) {
			return false;
		}
		for (int i=0; i<oid.value.length; i++) {
			if (this.value[i] != oid.value[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * OID同士の比較を行う。
	 *
	 * @return	0:一致／1:自身が大なり／-1:自身が小なり
	 * @param	oid		比較するOID
	 * @since	UDC1.0
	 */
	public int compareTo(UdcAsnObjectId oid)
	{
		int minLen = value.length;
		if (value.length > oid.value.length) {
			minLen = oid.value.length;
		}
		for (int i=0; i<minLen; i++) {
			if (this.value[i] > oid.value[i]) {
				return 1;
			} else if (this.value[i] < oid.value[i]) {
				return -1;
			}
		}
		if (value.length == oid.value.length) {
			return 0;
		} else if (value.length > oid.value.length) {
			return 1;
		}
		return -1;
	}

	/**
	 * OID同士の比較を行う。
	 *
	 * @return	0:一致／1:自身が大なり／-1:自身が小なり
	 * @param	oid		比較するOID
	 * @param	len		比較対象とするOID長
	 * @since	UDC1.0
	 */
	public int compareTo(UdcAsnObjectId oid, int len)
	{
		int minLen = value.length;
		if (value.length > oid.value.length) {
			minLen = oid.value.length;
		}
		if (minLen > len) {
			minLen = len;
		}
		for (int i=0; i<minLen; i++) {
			if (this.value[i] > oid.value[i]) {
				return 1;
			} else if (this.value[i] < oid.value[i]) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * 現在のOIDに追加する。
	 *
	 * @param	val	追加するOID値
	 * @since	UDC1.0
	 */
	public void addOid(long val)
	{
		int len = value.length + 1;
		long[] newValue = new long[len];
		for (int i=0; i<value.length; i++) {
			newValue[i] = value[i];
		}
		newValue[value.length] = val;
		value = newValue;
	}

	/**
	 * 現在のOIDに追加する。
	 *
	 * @param	vals	追加するOID
	 * @since	UDC1.0
	 */
	public void addOid(long[] vals)
	{
		int i;
		int len = value.length + vals.length;
		long[] newValue = new long[len];
		for (i=0; i<value.length; i++) {
			newValue[i] = value[i];
		}
		for (i=0; i<vals.length; i++) {
			newValue[value.length+i] = vals[i];
		}
		value = newValue;
	}

	/**
	 * 現在のOIDに追加する。
	 *
	 * @param	oid	追加するOID
	 * @since	UDC1.0
	 */
	public void addOid(String oid)
				throws IllegalArgumentException
	{
		long[] val = readValues(oid);
		addOid(val);
	}

	/**
	 * 現在のOIDに追加する。
	 *
	 * @param	oid	追加するOID
	 * @since	UDC1.0
	 */
	public void addOid(UdcAsnObjectId oid)
	{
		addOid(oid.getValue());
	}

	/**
	 * 指定したindex位置のOID一桁を取得する。
	 *
	 * @return	指定したindex位置のOID一桁
	 * @param	index	取得対象位置
	 * @since	UDC1.0
	 */
	public synchronized long getElementAt(int index) throws ArrayIndexOutOfBoundsException
	{
		if (index >= value.length) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + value.length);
		}
		try {
			return value[index];
		} catch (ArrayIndexOutOfBoundsException exc) {
			throw new ArrayIndexOutOfBoundsException(index + " < 0");
		}
	}

	/**
	 * 値のASN1エンコード時のOID一桁のバイト長を取得する。
	 *
	 * @return	値のASN1エンコード時のバイト長
	 * @since	UDC1.0
	 */
	private int getSIDLen(long value)
	{
		int count;
		for (count=1; (value>>=7)!=0; count++) {
			;
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
		int val, idx, len;
		len = getSIDLen(value[0]*40 + value[1]);	/* First entry = OID[0]*40 + OID[1] */
		for(idx=2; idx<value.length; idx++) {
			len += getSIDLen(value[idx]);
		}
		return len;
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
		int idx;
			/* Asn1Type , Asn1Length */
		AsnBuildHeader(out, ASN_OBJECT_ID, size());
			/* Asn1Value
			 * 	(First entry = OID[0]*40 + OID[1])
			 */
		encodeSID(out, value[0]*40 + value[1]);
		for(idx=2; idx<value.length; idx++) {
			encodeSID(out, value[idx]);
		}
	}

	/**
	 * OID一桁分のASN1エンコードを行う。
	 *
	 * @param	out		エンコード情報の出力先ストリーム
	 * @param	value	エンコード対象OID一桁
	 * @since	UDC1.0
	 */
	private void encodeSID(OutputStream out, long value) throws IOException
	{
		byte mask = (byte)0x0F;
		int  count = 0;

			// Upper mask is 4 bits
		mask = 0xF;
			// Loop while value and mask is zero
		for(count=28; count>0; count-=7) {
			if (((value >> count) & mask) != 0) {
				break;
			}
			mask = 0x7f;
		}
			// While count, output value. If this isn't the last byte, output
			// 0x80 | value.
		for(; count>=0; count-=7) {
			out.write( (byte)(((value >> count) & mask) | (count>0 ? 0x80 : 0x00)) );
			mask = 0x7f;
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
		/*
		 * @param in The input stream from which the value should be read
		 * @param len The length of the AsnInteger
		 */

		byte data[] = new byte[len];
		if (len != in.read(data,0,len)) {
			throw new IOException("UdcAsnObjectId(): Not enough data");
		}
			/* now decide how many SID we will need
			 * count the bytes with 0 in the top bit - then add 1
			 */
		int sids = 1;    // first byte has 2 sids in it
		for (int off=0 ;off<len;off++) {
			if ( data[off] >= 0) {
				sids++;
			}
		}
			/* so allocate some space for the sids */
		value = new long[sids];
			/* decode the first two */
		value[0] = data[0] / 40;
		value[1] = data[0] % 40;
			/* now decode the rest */
		int off = 1;
		for (int idx=2; idx<value.length; idx++) {
			long tval = 0;
			do {
				tval = tval << 7;
				tval |= (data[off] & 0x7f);
			} while ( data[off++] < 0);
			value[idx] = tval;
		}
	}

	/**
	 * ASN1デコードを行う。
	 *
	 * @param	s	OID文字列
	 * @since	UDC1.0
	 */
	void read(String s) throws IllegalArgumentException
	{
		value = readValues(s);
	}

	/**
	 * OID文字列をOID値(数値列)に変換する。
	 *
	 * @return	OID値(数値列)
	 * @param	s	OID文字列
	 * @since	UDC1.0
	 */
	long[] readValues(String s) throws IllegalArgumentException
	{
		/* format a[.b] */
			/* Get number of dots */
		int count = readCount(s);
			/* make values */
		long[] v = new long[count];
		int pos,opos;
		try {
			opos = 0;
			for (int n=0; n < count; n++) {
				String num;
				pos = s.indexOf('.',opos);
				if (pos > 0) {
					num = s.substring(opos,pos);
				} else {
					num = s.substring(opos);
				}
				Long val = Long.valueOf(num);
				v[n] = val.longValue();
				opos = pos + 1;
			}
		} catch (java.lang.NumberFormatException exc) {
			throw new IllegalArgumentException("UdcAsnObjectId(): Bad OID " + s + " " + exc.getMessage());
		}
		return v;
	}

	/**
	 * OID文字列からOID数値列数を取得する。
	 *
	 * @return	OID数値列数
	 * @param	s	OID文字列
	 * @since	UDC1.0
	 */
	int readCount(String s)
	{
		int count = 1;
		for (int pos=s.indexOf('.'); pos>=0; count++) {
			pos = s.indexOf('.',pos+1);
		}
		return count;
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		String result = new String("");
		for (int n=0; n < value.length-1; n++) {
			result = result + value[n] + ".";
		}
		result = result + value[value.length-1];
		return result;
	}

}
