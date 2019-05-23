/* *********************************************************************
 * @(#)UdcOpeThreadComReceiverFace.java 1.0, 30 Jun 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;


/**
 * スレッド間通信の応答ハンドリングインタフェースクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 30 Jun 2006
 * @see UdcOpeThreadComReceiver
 * @since   UDC1.2
 */
public interface UdcOpeThreadComReceiverFace
{
    /**
     *  スレッド間通信の応答情報を処理するインタフェース関数。
     *  @param  response   スレッド間通信応答情報
     */
	public void operatePerformed(UdcThreadChannelDtr response);
}
