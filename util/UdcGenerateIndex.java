/* *********************************************************************
 * @(#)UdcGenerateIndex.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;

/**
 * ユニークなIndex値の確保を可能とするクラス
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcGenerateIndex
{
	private	UdcMutex	usedGeneratedIdMutex = new UdcMutex();
	private int			usedGeneratedId = 0;
	private int			minIndex = 1;
	private int			maxIndex = (int)0x7FFFFFFF;

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcGenerateIndex()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param	min	インデックスの最小値
	 * @param	max	インデックスの最大値
	 * @since	UDC1.0
	 */
	public UdcGenerateIndex(int min, int max)
	{
		minIndex = min;
		maxIndex = max;
		usedGeneratedId = minIndex - 1;
	}

	/**
	 * 現在のインデックス確保値を初期化します。
	 *
	 * @return	正常(true)/異常(false) 異常となるケースは、currentIdが制限範囲内にない場合
	 * @param	currentId	初期化するインデックス値
	 * @since	UDC1.0
	 */
	public boolean initialize(int currentId)
	{
		if (currentId < minIndex || currentId > maxIndex) {
			return false;
		}
		usedGeneratedId = currentId;
		return true;
	}

	/**
	 * 新たにユニークなインデックスを取得します。
	 *
	 * @return	ユニークなインデックス値
	 * @since	UDC1.0
	 */
	public int allocateIndex() throws InterruptedException
	{
		usedGeneratedIdMutex.lock();
		usedGeneratedId ++;
		if (maxIndex <= 0) {
			if (usedGeneratedId < minIndex) {
				usedGeneratedId = minIndex;
			}
		} else {
			if (usedGeneratedId > maxIndex) {
				usedGeneratedId = minIndex;
			}
		}
		usedGeneratedIdMutex.unlock();
		return usedGeneratedId;
	}
}

