/* *********************************************************************
 * @(#)UdcAsnObject.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;

/**
 * ASN1情報のベースクラス。全てASN1情報は本クラスを継承する。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public abstract class UdcAsnObject implements UdcSnmpConstants
{
	/**
	 * ASN1型識別子。
	 * @see		UdcSnmpConstants
	 * @since	UDC1.0
	 */
	protected byte type;

	/**
	 * ASN1エンコードバイト列で自身の値が格納される位置
	 * @see		UdcSnmpConstants
	 * @since	UDC1.0
	 */
	protected int startPos = 0;

	/**
	 * ASN1ヘッダ長(単位：バイト)
	 * @since	UDC1.0
	 */
	protected int headerLength = 0;

	/**
	 * ASN1情報長のデータ長(単位：バイト)
	 * @since	UDC1.0
	 */
	protected int contentsLength = 0;

	/**
	 * ASN1型情報の正常性。
	 * @since	UDC1.0
	 */
	protected boolean isCorrect = true;

	/**
	 * 本インスタンスの複製を生成する。
	 * 本メンバ関数は abstract定義です。ASN1型情報の各クラスにて定義しなければならない。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public abstract Object clone() throws CloneNotSupportedException;

	/**
	 * ASN1型識別子を取得する。
	 *
	 * @return 	ASN1型識別子
	 * @since	UDC1.0
	 */
	public byte getType()
	{
		return type;
	}

	/**
	 * Sequence型ベースでの構成ASN1要素を設定する。
	 *
	 * @return	設定したASN1構成要素
	 * @param	child	設定するASN1構成要素
	 * @see		UdcAsnSequence
	 * @since	UDC1.0
	 */
	UdcAsnObject add(UdcAsnObject child)
	{
		return child;
	}

	/**
	 * 自身がPDUである場合、自身を返却する。
	 * エンコード・デコードした情報からPDU情報を検索するのに使用する。
	 * 本メンバ関数は、UdcAsnPduSequenceによってオーバライドされる。
	 *
	 * @return	PDU情報
	 * @see		UdcAsnPduSequence
	 * @since	UDC1.0
	 */
	UdcAsnObject findPdu()
	{
		return null;
	}

	/**
	 * 自身がTrap-PDUである場合、自身を返却する。
	 * エンコード・デコードした情報からTrap-PDU情報を検索するのに使用する。
	 * 本メンバ関数は、UdcAsnPduSequenceによってオーバライドされる。
	 *
	 * @return	PDU情報
	 * @see		UdcAsnPduSequence
	 * @since	UDC1.0
	 */
	UdcAsnObject findTrapPduv1()
	{
		return null;
	}

	/**
	 * エンコード時のバイト長を取得する。
	 * 本メンバ関数は、継承する全てのASN1情報クラスによってオーバライドされる。
	 *
	 * @return	ASN1エンコード長(単位：バイト)
	 * @since	UDC1.0
	 */
	int size()
	{
		return 0;
	}

	/**
	 * ASN1エンコードバイト列で自身の値が格納される位置を取得する。
	 *
	 * @return	値の先頭位置(単位：バイト)
	 * @since	UDC1.0
	 */
	int getContentsPos()
	{
		return startPos + headerLength;
	}

	/**
	 * ASN1情報長のデータ長を取得する。
	 *
	 * @return	ASN1情報長のデータ長を取得する。(単位：バイト)
	 * @since	UDC1.0
	 */
	int getContentsLength()
	{
		return contentsLength;
	}


	/**
	 * ASN1バイト列情報から自身のデータ長を取得する。
	 *
	 * @return	自身のデータ長
	 * @param	in	デコード情報格納ストリーム
	 * @since	UDC1.0
	 */
	int getLengthPacket(InputStream in) throws IOException
	{
		byte mask =(byte) 0x7f;
		byte len = (byte) in.read();

			/* short form */
		if ((0x80 & len) == 0) {
			return (int)(mask & len);
		}
			/* long form */
		int length = 0;
		int count = (mask & len);
		if (count < 4) {
			byte data[] = new byte[count];
			int n = in.read(data, 0, count);
			if (n !=  count) {
				throw new IOException("UdcAsnObject.getLengthPacket(): Not enough data");
			}
			length = UdcCoder.bytes2Int(data,false);
		}
		return length;
	}

	/**
	 * ASN1デコードを行う。
	 *
	 * @return	デコードしたASN情報
	 * @param	in	デコード情報格納ストリーム
	 * @since	UDC1.0
	 */
	UdcAsnObject AsnReadHeader(InputStream in) throws IOException
	{
		return AsnReadHeader(in, 0);
	}

	/**
	 * ASN1デコードを行う。
	 * 指定されたinのpos位置からデコードを行う。
	 *
	 * @return	デコードしたASN情報
	 * @param	in	デコード情報格納ストリーム
	 * @param	pos	デコード情報対象格納位置(単位：バイト)
	 * @since	UDC1.0
	 */
	UdcAsnObject AsnReadHeader(InputStream in, int pos) throws IOException
	{
		/*
		 * Asn1 syntax {type length value}
		 *	type: CCFTTTTT
		 *		CC 		tag {[00]universal,[01]application,[10]context-specific,[11]private}
		 *		F		structure flag {[0]primitive,[1]construed}
		 *		TTTTTT	value
		 *		(*)SNMPでは基本型のみしか使用しないため[type]は1バイト固定
		 *	length:
		 *		0LLLLLLLL 					{short form}	LLLLLLL = length
		 *		1NNNNNNNN LLLLLLLL LLLLLLLL {long  form}	NNNNNNN = length of len-length / LL...LL = length
		 */
		UdcAsnObject ret = null;
		int had = in.available();
			/* Asn1 Type */
		type = (byte) in.read();
		if (type == -1) {
			return ret;
		}
			/* Asn1 Length */
		int len = getLengthPacket(in);
		int off = in.available();
		int headLength = had - off;
			/* Asn1 Value */
		int got = 0;
		byte body[] = new byte[len];
		if (len > 0) {
			got = in.read(body, 0, len);
		}
		if (got > -1) {
			ByteArrayInputStream buf = new ByteArrayInputStream(body);
			ret = AsnMakeMe(buf, type, len, pos, headLength);
			if (got != len) {
				ret.isCorrect = false;
			}
		}
		return ret;
	}

	/**
	 * ASN1-Valueのデコードを行う。
	 *
	 * @return	デコードしたASN情報
	 * @param	in	デコード情報格納ストリーム
	 * @param	t	ASN1識別子
	 * @param	len	デコード対象のバイト長
	 * @param	pos	デコード情報対象格納位置(単位：バイト)
	 * @param	headLength	ASN1-Type/Lenbgthのバイト長
	 * @since	UDC1.0
	 */
	UdcAsnObject AsnMakeMe(InputStream in, byte t, int len, int pos, int headLength) throws IOException
	{
		UdcAsnObject me = this;
		type = t;
		if ((type & PDU_MSG_BASE) == PDU_MSG_BASE) {
			switch (type) {
			case GET_REQ_MSG :
			case GETNEXT_REQ_MSG :
			case GETBULK_REQ_MSG :
			case SET_REQ_MSG :
			case GET_RSP_MSG :
			case INFORM_REQ_MSG:
			case GET_RPRT_MSG :
			case TRPV2_REQ_MSG :
				me = new UdcAsnPduSequence(in, len, (pos+headLength));
				break;
			case TRP_REQ_MSG :
				me = new UdcAsnTrapPduv1Sequence(in, len, (pos+headLength));
				break;
			default:
				me = new UdcAsnNull(in, len);
				me.isCorrect = false;
				break;
			}
		} else {
			switch (type) {
			case CONS_SEQ :
				me = new UdcAsnSequence(in, len, (pos+headLength));
				break;
			case ASN_INTEGER :
				me = new UdcAsnInteger(in, len);
				break;
			case TIMETICKS :
			case COUNTER :
			case GAUGE :
				me = new UdcAsnUnsInteger(in, len);
				break;
			case UINTEGER32 :
				me = new UdcAsnUnsInteger(in, len);
				break;
			case COUNTER64 :
				me = new UdcAsnUnsInteger64(in, len);
				break;
			case ASN_OBJECT_ID :
				me = new UdcAsnObjectId(in, len);
				break;
			case IPADDRESS :
			case ASN_OCTET_STR :
			case OPAQUE :
				me = new UdcAsnOctets(in, len);
				break;
			case NSAP_ADDRESS :
				me = new UdcAsnOctets(in,len);
				break;
			case ASN_BIT_STR :
				me = new UdcAsnBitString(in, len);
				break;
			case ASN_NULL :
				me = new UdcAsnNull(in, len);
				break;
			case SNMP_VAR_NOSUCHOBJECT:
			case SNMP_VAR_NOSUCHINSTANCE:
			case SNMP_VAR_ENDOFMIBVIEW:
				me = new UdcAsnPrimitive(type);
				break;
			default :
				me = new UdcAsnNull(in, len);
				me.isCorrect = false;
				break;
			}
		}
		me.type = type;
		me.startPos = pos;
		me.headerLength = headLength;
		me.contentsLength = len;

		return me;
	}


	/**
	 * ASN1エンコードを行う。
	 * 本メンバ関数は、継承する全てのASN1情報クラスによってオーバライドされる。
	 *
	 * @param	out	エンコード情報の出力先ストリーム
	 * @param	pos	エンコード情報の出力位置(本クラスでは未使用)
	 * @since	UDC1.0
	 */
	abstract void write (OutputStream out, int pos) throws IOException;

	/**
	 * lengthで指定されたASN1情報長の情報長を取得する。
	 *
	 * @return 	lengthで指定された情報長のASN1データ長
	 * @param	length	ASN1情報長の情報長
	 * @since	UDC1.0
	 */
	int getLengthBytes(int length)
	{
			/* Short form.. 1 byte */
		if (length < 0x80) {
			return 1;
		}
			/* Long form.. prefix byte + length bytes */
		int count;
		int mask = 0xFF000000;
		for(count=4; (length&mask)==0; count--) {
			mask >>= 8;
		}
		return count + 1;
	}

	/**
	 * ASN1-Type/Lengthをエンコードする。
	 *
	 * @param	out		エンコード情報の出力先ストリーム
	 * @param	t		ASN1識別子
	 * @param	length	ASN1情報長の情報長
	 * @since	UDC1.0
	 */
	void AsnBuildHeader(OutputStream out, byte t, int length) throws IOException
	{
			/* Asn1 Type */
		type = t;
		out.write(type);
			/* Asn1 Length */
		int count = getLengthBytes(length);
		headerLength = count + 1;
		contentsLength = length;
				/* Write long form prefix byte */
		if (count > 1) {
			-- count;
			byte tmp = (byte)(0x80 | (byte)count);
			out.write(tmp);
		}
		while(count!=0) {
			out.write((byte)((length >> (--count << 3)) & 0xFF));
		}
	}

	/**
	 * インスタンス情報文字列を取得する。
	 * 本メンバ関数は、継承する全てのASN1情報クラスによってオーバライドされる。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public abstract String toString();

}


