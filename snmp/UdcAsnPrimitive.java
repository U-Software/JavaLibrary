/* *********************************************************************
 * @(#)UdcAsnPrimitive.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.io.*;
import java.util.*;

import udc.util.*;

/**
 * ASN1 PRIMITIVE型情報クラス。
 *
 * This class represents the Exception values for SNMP v2c, v3:
 * SNMP_VAR_NOSUCHOBJECT, SNMP_VAR_NOSUCHINSTANCE, SNMP_VAR_ENDOFMIBVIEW
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public class UdcAsnPrimitive extends UdcAsnObject
{
	/**
	 * コンストラクタ
	 *
	 * @param	t	ASN1識別子
	 * @since	UDC1.0
	 */
	public UdcAsnPrimitive(byte t)
	{
		type = t;
	}

	/**
	 * 本インスタンスの複製を生成する。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public Object clone() throws CloneNotSupportedException
	{
		return new UdcAsnPrimitive(type);
	}

	/**
	 * インスタンス情報文字列を取得する。
	 *
	 * @return	インスタンス情報文字列
	 * @since	UDC1.0
	 */
	public String toString()
	{
		String str = "UdcAsnPrimitive ";
		if (type == SNMP_VAR_NOSUCHOBJECT) {
			str = "No such object";
		} else if (type == SNMP_VAR_NOSUCHINSTANCE) {
			str = "No such instance";
		} else if (type == SNMP_VAR_ENDOFMIBVIEW) {
			str = "End of MIB view";
		}
		return str;
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
		AsnBuildHeader(out, type, 0);
	}

}
