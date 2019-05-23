/* *********************************************************************
 * @(#)UdcSnmpConstants.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;


/**
 * SNMP-ASN1各種情報定義インタフェース。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public interface UdcSnmpConstants
{
	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_BOOLEAN     =(byte)(0x01);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_INTEGER     =(byte)(0x02);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_BIT_STR     =(byte)(0x03);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_OCTET_STR   =(byte)(0x04);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_NULL        =(byte)(0x05);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_OBJECT_ID   =(byte)(0x06);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_SEQUENCE    =(byte)(0x10);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_SET         =(byte)(0x11);

	public static final byte ASN_UNIVERSAL   =(byte)(0x00);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_APPLICATION =(byte)(0x40);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_CONTEXT     =(byte)(0x80);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_PRIVATE     =(byte)(0xC0);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_PRIMITIVE   =(byte)(0x00);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_CONSTRUCTOR =(byte)(0x20);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_LONG_LEN    =(byte)(0x80);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_EXTENSION_ID=(byte)(0x1F);

	/**
	 * ASN1型識別子。
	 */
	public static final byte ASN_BIT8        =(byte)(0x80);

	/**
	 * ASN1型識別子。
	 */
	public static final byte INTEGER         =(byte)ASN_INTEGER;

	/**
	 * ASN1型識別子。
	 */
	public static final byte STRING          =(byte)ASN_OCTET_STR;

	/**
	 * ASN1型識別子。
	 */
	public static final byte OBJID           =(byte)ASN_OBJECT_ID;

	/**
	 * ASN1型識別子。
	 */
	public static final byte NULLOBJ         =(byte)ASN_NULL;

	/**
	 * ASN1型識別子。
	 */
	public static final byte IPADDRESS       =(byte)(ASN_APPLICATION | 0);

	/**
	 * ASN1型識別子。
	 */
	public static final byte COUNTER         =(byte)(ASN_APPLICATION | 1);

	/**
	 * ASN1型識別子。
	 */
	public static final byte GAUGE           =(byte)(ASN_APPLICATION | 2);

	/**
	 * ASN1型識別子。
	 */
	public static final byte TIMETICKS       =(byte)(ASN_APPLICATION | 3);

	/**
	 * ASN1型識別子。
	 */
	public static final byte OPAQUE          =(byte)(ASN_APPLICATION | 4);

	/**
	 * ASN1型識別子。
	 */
	public static final byte COUNTER64       =(byte)(ASN_APPLICATION | 6);

	/**
	 * ASN1型識別子。
	 * see
	 * <a href="http://ietf.org/rfc/rfc1442.txt">RFC 1442</a> ,
	 * <a href="http://ietf.org/rfc/rfc1902.txt">RFC 1902</a>).
	 */
	public static final byte NSAP_ADDRESS    =(byte)(ASN_APPLICATION | 5);

	/**
	 * ASN1型識別子。
	 * see
	 * <a href="http://ietf.org/rfc/rfc1442.txt">RFC 1442</a>,
	 * <a href="http://ietf.org/rfc/rfc1902.txt">RFC 1902</a>).
	 */
	public static final byte UINTEGER32      =(byte)(ASN_APPLICATION | 7);


	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_COLDSTART         =(byte)(0x0);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_WARMSTART         =(byte)(0x1);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_LINKDOWN          =(byte)(0x2);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_LINKUP            =(byte)(0x3);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_AUTHFAIL          =(byte)(0x4);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_EGPNEIGHBORLOSS   =(byte)(0x5);

	/**
	 * generic-trap種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_TRAP_ENTERPRISESPECIFIC=(byte)(0x6);


	/**
	 * Indicated the agent does not implement the object referred to by
	 * this object identifier.
	 * 	[SNMPv2c, SNMPv3 (GET)]
	 */
	public static final byte SNMP_VAR_NOSUCHOBJECT =(byte)(ASN_CONTEXT | ASN_PRIMITIVE | 0x0);

	/**
	 * Indicated that this object does not exists for this operation.
	 * 	[SNMPv2c, SNMPv3 (GET)]
	 */
	public static final byte SNMP_VAR_NOSUCHINSTANCE =(byte)(ASN_CONTEXT | ASN_PRIMITIVE | 0x1);

	/**
	 * Indicated an attempt to reference an object identifier that is
	 * beyond the end of the MIB at the agent.
	 * 	[SNMPv2c, SNMPv3 (GET,GETBULK)]
	 */
	public static final byte SNMP_VAR_ENDOFMIBVIEW =(byte)(ASN_CONTEXT | ASN_PRIMITIVE | 0x2);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1, SNMPv2c, SNMPv3]
	 */
	public static final byte SNMP_ERR_NOERROR = (byte)(0x00);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1, SNMPv2c, SNMPv3]
	 */
	public static final byte SNMP_ERR_TOOBIG = (byte)(0x01);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1]
	 */
	public static final byte SNMP_ERR_NOSUCHNAME = (byte)(0x02);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1(SET)]
	 */
	public static final byte SNMP_ERR_BADVALUE = (byte)(0x03);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1(SET)]
	 */
	public static final byte SNMP_ERR_READONLY = (byte)(0x04);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv1, SNMPv2c, SNMPv3]
	 */
	public static final byte SNMP_ERR_GENERR = (byte)(0x05);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_NOACCESS = (byte)(0x06);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 *	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_WRONGTYPE = (byte)(0x07);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_WRONGLENGTH = (byte)(0x08);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_WRONGENCODING = (byte)(0x09);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_WRONGVALUE         =(byte)(0x0A);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_NOCREATION         =(byte)(0x0B);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_INCONSISTENTVALUE  =(byte)(0x0C);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_RESOURCEUNAVAILABLE =(byte)(0x0D);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_COMMITFAILED       =(byte)(0x0E);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_UNDOFAILED         =(byte)(0x0F);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3]
	 */
	public static final byte SNMP_ERR_AUTHORIZATIONERR   =(byte)(0x10);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_NOTWRITABLE        =(byte)(0x11);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 * 	[SNMPv2c, SNMPv3 (SET)]
	 */
	public static final byte SNMP_ERR_INCONSISTENTNAME   =(byte)(0x12);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_ERR_DECODING_EXC       =(byte)(0x13);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_ERR_DECODINGASN_EXC    =(byte)(0x14);

	/**
	 *  error-status種別。詳細はRFC定義参照のこと。
	 */
	public static final byte SNMP_ERR_DECODINGPKTLNGTH_EXC = (byte)(0x15);


	/**
	 * SNMP Version種別。 SNMPv1
	 */
	public static final byte SNMP_VERSION_1 =(byte)(0x0);

	/**
	 * SNMP Version種別。 SNMPv2c
	 */
	public static final byte SNMP_VERSION_2c =(byte)(0x1);

	/**
	 * SNMP Version種別。 SNMPv3 (本システムでは未提供)
	 */
	public static final byte SNMP_VERSION_3 =(byte)(0x3);


	/**
	 * ASN1-PDU種別。[ベース]
	 */
	 static final byte PDU_MSG_BASE = (byte)(ASN_CONTEXT | ASN_CONSTRUCTOR);

	/**
	 * ASN1-PDU種別。[GetRequest]
	 */
	static final byte GET_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x0);

	/**
	 * ASN1-PDU種別。[GetNextRequest]
	 */
	static final byte GETNEXT_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x1);

	/**
	 * ASN1-PDU種別。[Response]
	 */
	static final byte GET_RSP_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x2);

	/**
	 * ASN1-PDU種別。[SetRequest]
	 */
	static final byte SET_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x3);

	/**
	 * ASN1-PDU種別。[Trap SNMPv1]
	 */
	static final byte TRP_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x4);

	/**
	 * ASN1-PDU種別。[GetBulkRequest]
	 */
	static final byte GETBULK_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x5);

	/**
	 * ASN1-PDU種別。[InformRequest]
	 */
	static final byte INFORM_REQ_MSG  =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x6);

	/**
	 * ASN1-PDU種別。[Trap SNMPv2]
	 */
	static final byte TRPV2_REQ_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x7);

	/**
	 * ASN1-PDU種別。[Trap GetReport]
	 */
	static final byte GET_RPRT_MSG =(byte)(ASN_CONTEXT | ASN_CONSTRUCTOR | 0x8);

	/**
	 * ASN1-PDU種別。[ベース]
	 */
	static final byte CONS_SEQ  =(byte)(ASN_SEQUENCE | ASN_CONSTRUCTOR);



	/**
	 * PDU-Responseチェック結果: 正常
	 * 本値はUdcPdu.checkResponse/checkResponse_nextの戻り値です。
	 */
	static final int SnmpRespCheck_OK = 0;

	/**
	 * PDU-Responseチェック結果: タイムアウト
	 * 本値はUdcPdu.checkResponse/checkResponse_nextの戻り値です。
	 */
	static final int SnmpRespCheck_Timeout = 1;

	/**
	 * PDU-Responseチェック結果: 不明エラー
	 * 本値はUdcPdu.checkResponse/checkResponse_nextの戻り値です。
	 */
	static final int SnmpRespCheck_UnknonwErr = 2;

	/**
	 * PDU-Responseチェック結果: 応答エラー時
	 * 本値はUdcPdu.checkResponse/checkResponse_nextの戻り値です。
	 */
	static final int SnmpRespCheck_ResError = 3;

	/**
	 * PDU-Responseチェック結果: Primitiveエラー
	 * 本値はUdcPdu.checkResponse/checkResponse_nextの戻り値です。
	 */
	static final int SnmpRespCheck_PrimitiveError = 4;

}
