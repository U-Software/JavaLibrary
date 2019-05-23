/* *********************************************************************
 * @(#)UdcTrapPduv1.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;

import java.util.*;
import java.io.*;
import java.net.*;

import udc.util.*;

/**
 * SNMPv1 TRAP-PDU型クラス。
 * TRAPを送信する場合、本インスタンスを生成・設定し、
 * UdcSnmpContext/UdcSnmpAgentContextに引き渡す。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @see		UdcPdu
 * @since	UDC1.0
 */
public class UdcTrapPduv1 extends UdcPdu
{
	/**
	 * SNMP-TrapV1 - enterpise
	 * @since	UDC1.0
	 */
	private String	enterprise;

	/**
	 * SNMP-TrapV1 - IP-Address
	 * @since	UDC1.0
	 */
	private byte[]	IpAddress;

	/**
	 * SNMP-TrapV1 - generic-trap
	 * @since	UDC1.0
	 */
	private int		generic_trap;

	/**
	 * SNMP-TrapV1 - specific-trap
	 * @since	UDC1.0
	 */
	private int		specific_trap;

	/**
	 * SNMP-TrapV1 - time tics
	 * @since	UDC1.0
	 */
	private long	timeTicks;

	/**
	 * SNMP-TrapV1 - generic-trapの文字列表現リスト
	 * @since	UDC1.0
	 */
	public final static String [] genericTrapStrings = {
		"Cold Start",
		"Warm Start",
		"Link Down",
		"Link Up",
		"Authentication Failure",
		"EGP Neighbor Loss",
		"Enterprise Specific"
	};

	/**
	 * コンストラクタ。
	 *
	 * @since	UDC1.0
	 */
	public UdcTrapPduv1()
	{
		super();
		setMsgTypeTRAPV1();
		generic_trap = UdcAsnObject.SNMP_TRAP_WARMSTART;
	}

	/**
	 * SNMP-TrapV1 - enterpiseを取得する。
	 *
	 * @return SNMP-TrapV1 - enterpise
	 * @since	UDC1.0
	 */
	public String getEnterprise() { return enterprise; }

	/**
	 * SNMP-TrapV1 - enterpiseを設定する。
	 *
	 * @param	var	SNMP-TrapV1 - enterpise
	 * @since	UDC1.0
	 */
	public void setEnterprise(String var) { enterprise = var; }

	/**
	 * SNMP-TrapV1 - IP-Addressを取得する。(バイト列)
	 *
	 * @return 	SNMP-TrapV1 - IP-Address(バイト列)
	 * @since	UDC1.0
	 */
	public byte[] getIpAddress() { return IpAddress; }

	/**
	 * SNMP-TrapV1 - IP-Addressを設定する。
	 *
	 * @param	var 	SNMP-TrapV1 - IP-Address(バイト列)
	 * @since	UDC1.0
	 */
	public void setIpAddress(byte[] var) { IpAddress = var; }

	/**
	 * SNMP-TrapV1 - generic-trapを取得する。
	 *
	 * @return	SNMP-TrapV1 - generic-trap
	 * @since	UDC1.0
	 */
	public int getGenericTrap() { return generic_trap; }

	/**
	 * SNMP-TrapV1 - generic-trapを設定する。
	 *
	 * @param	var	SNMP-TrapV1 - generic-trap
	 * @since	UDC1.0
	 */
	public void setGenericTrap(int var) { generic_trap = var; }

	/**
	 * SNMP-TrapV1 - generic-trap文字列表現を取得する。
	 *
	 * @return	SNMP-TrapV1 - generic-trap文字列表現
	 * @since	UDC1.0
	 */
	public String getGenericTrapString()
	{
		String trapStr = "";
		if (generic_trap > -1 && generic_trap < genericTrapStrings.length) {
			trapStr = genericTrapStrings[generic_trap];
		}
		return trapStr;
	}

	/**
	 * SNMP-TrapV1 - specific-trapを取得する。
	 *
	 * @return	SNMP-TrapV1 - specific-trap
	 * @since	UDC1.0
	 */
	public int getSpecificTrap() { return specific_trap; }

	/**
	 * SNMP-TrapV1 - specific-trapを設定する。
	 *
	 * @param	var 	SNMP-TrapV1 - specific-trap
	 * @since	UDC1.0
	 */
	public void setSpecificTrap(int var) { specific_trap = var; }

	/**
	 * SNMP-TrapV1 - time ticsを取得する。
	 *
	 * @return	SNMP-TrapV1 - time tics
	 * @since	UDC1.0
	 */
	public long getTimeTicks() { return timeTicks; }

	/**
	 * SNMP-TrapV1 - time ticsを設定する。
	 *
	 * @param	var	SNMP-TrapV1 - time tics
	 * @since	UDC1.0
	 */
	public void setTimeTicks(long var) { timeTicks = var; }

	/**
	 * トラップPDUをエンコードする。
	 *
	 * @return	エンコードしたバイト列
	 * @since	UDC1.0
	 */
	public byte[] encode()
		throws java.io.IOException, UdcEncodingException
	{
			// create msg
		UdcAsnSequence	msg = new UdcAsnSequence();
				// version
		msg.add(new UdcAsnInteger(version));
				// community
		msg.add(new UdcAsnOctets(getCommunity()));
				// pdu
		UdcAsnObject pdu = new UdcAsnSequence(msgType);					// msgtype (choice)
		pdu.add(new UdcAsnObjectId(enterprise));						// enterprise
		pdu.add(new UdcAsnOctets(IpAddress, UdcAsnObject.IPADDRESS));	// agent-IPaddress
		pdu.add(new UdcAsnInteger(generic_trap)); 						// generic-trap
		pdu.add(new UdcAsnInteger(specific_trap));						// specific-trap
		pdu.add(new UdcAsnUnsInteger(timeTicks));						// time-stamp
						// varbindlist
		UdcAsnObject vbl = pdu.add(new UdcAsnSequence());
		UdcVarbind vb;
		for (int i=0; i<varbindlist.size(); i++) {
			vb = (UdcVarbind)varbindlist.get(i);
			UdcAsnObject vbObj = vbl.add(new UdcAsnSequence());
			vbObj.add(vb.getOid());
			vbObj.add(vb.getValue());
		}
		msg.add(pdu);
			// encoding
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		msg.write(out);
		return out.toByteArray();
	}

	/**
	 * デコードする。
	 *
	 * @param	in	デコード対象のストリーム
	 * @since	UDC1.0
	 */
	public void decode(InputStream in)
		throws java.io.IOException
	{
			// decoding
		UdcAsnSequence dummy = new UdcAsnSequence();
		UdcAsnSequence asnTopSeq = (UdcAsnSequence)dummy.AsnReadHeader(in);
		decode(asnTopSeq);
	}

	/**
	 * デコードする。
	 *
	 * @param	asnTopSeq	デコード対象のSEQUENCE情報
	 * @since	UDC1.0
	 */
	public void decode(UdcAsnSequence asnTopSeq)
		throws java.io.IOException
	{
			// version
		version = (byte)((UdcAsnInteger)asnTopSeq.getObj(0)).getValue();
			// community
		community = ((UdcAsnOctets)asnTopSeq.getObj(1)).getValue();
			// pdu
		UdcAsnTrapPduv1Sequence pdu = (UdcAsnTrapPduv1Sequence)asnTopSeq.findTrapPduv1();
		enterprise = ((UdcAsnObjectId)pdu.getObj(0)).getValue();	// enterprise
		IpAddress = ((UdcAsnOctets)pdu.getObj(1)).getBytes();		// agent-IPaddress
		generic_trap = ((UdcAsnInteger)pdu.getObj(2)).getValue();	// generic-trap
		specific_trap = ((UdcAsnInteger)pdu.getObj(3)).getValue();	// specific-trap
		timeTicks = ((UdcAsnUnsInteger)pdu.getObj(4)).getValue();	// time-stamp
				// varbindlist
		UdcAsnSequence varbindings = (UdcAsnSequence)pdu.getObj(5);
		int size = varbindings.getObjCount();
		for (int i=0; i<size; i++) {
			UdcAsnSequence vbObj = (UdcAsnSequence)varbindings.getObj(i);
			varbindlist.add( new UdcVarbind(vbObj) );
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
		String iPAddressStr = "null";
		if (IpAddress != null) {
			iPAddressStr = (new UdcAsnOctets(IpAddress)).toIpAddress();
		}
		String res = getClass().getName()
			+ "["
			+ ", msgType=0x" + UdcCoder.byte2HexString(msgType)
			+ ", enterprise=" + enterprise
			+ ", IpAddress=" + iPAddressStr
			+ ", generic_trap=" + getGenericTrapString()
			+ ", specific_trap=" + specific_trap
			+ ", timeTicks=" + timeTicks
			+ ", varbindlist=";

		if (varbindlist != null) {
			res += "[";
			UdcVarbind vb;
			for (int i=0; i<varbindlist.size(); i++) {
				vb = (UdcVarbind)varbindlist.get(i);
				res += vb.toString();
				if ((i+1) < varbindlist.size()) {
					res += ", ";
				}
			}
			res += "]";
		} else {
			res += "null";
		}
		return res;
	}
}
