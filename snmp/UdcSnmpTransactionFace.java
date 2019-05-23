/* *********************************************************************
 * @(#)UdcSnmpTransaction.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.snmp;


/**
 * ユーザトランザクションインタフェース。
 * Agent処理では、１要求に対して本インタフェースが生成され、ユーザに提供
 * される。ユーザではこのトランザクションを使用する場合は、本インタフェースを
 * 継承し、ユーザ独自のトランザクションを実装することができる。
 *
 * @author	Takayuki Uchida
 * @version	1.0, 18 Jan 2003
 * @since	UDC1.0
 */
public abstract class UdcSnmpTransactionFace implements Cloneable
{
	/**
	 * 本インスタンスの複製を生成する。
	 * 1要求に対してトランザクションを生成する時にコールされる。
	 *
	 * @return	本インスタンスの複製
	 * @since	UDC1.0
	 */
	public abstract Object clone() throws CloneNotSupportedException;

	/**
	 * UDC内部で、MIB処理を行う前に先立って１回コールされる。
	 * ユーザがトランザクションの開始処理等を行うために提供される。
	 *
	 * @return	結果(0:正常/非0:異常)
	 * @since	UDC1.0
	 */
	public abstract int prepare(UdcPdu request);

	/**
	 * UDC内部で、MIB処理を行った後に１回コールされる。
	 * ユーザがトランザクションの終了処理等を行うために提供される。
	 *
	 * @since	UDC1.0
	 */
	public abstract void commit(UdcPdu reply, UdcPdu request);
}
