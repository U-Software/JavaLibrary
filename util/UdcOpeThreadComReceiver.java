/* *********************************************************************
 * @(#)UdcOpeThreadComReceiver.java 1.0, 30 Jun 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;
import javax.swing.*;

/**
 * 内部スレッド間通信によってサーバシステムの要求の応答をハンドリングするクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 30 Jun 2006
 * @see UdcOpeThreadComReceiverFace
 * @since   UDC1.2
 */
public class UdcOpeThreadComReceiver implements Runnable
{
	/**
	 * UGBViewが管理するスレッドプール管理クラス
	 */
	public UdcThreadOperationPool	threadManager;

	/**
	 * スレッド
	 */
	Thread 	thread;

	/**
	 * スレッド間通信チャネル
	 */
	UdcThreadChannel				channel;

	/**
	 * スレッド間通信の要求InvokeID
	 */
	int								recInvokeId;

	/**
	 * スレッド間通信の要求メッセージ種別
	 */
	int								recMsgType;						

	/**
	 * スレッド間通信の応答タイムアウト時間
	 */
	long							recTimer;

	/**
	 * スレッド間通信の応答を処理するクラス
	 */
	UdcOpeThreadComReceiverFace		receiver;

	/**
	 * 要求先の負荷分散スレッド
	 */
	UdcThreadOperation 				ope;

	/**
	 * スレッド間通信の要求メッセージ
	 */
	UdcThreadChannelDtr 			req;


	/**
     * コンストラクタ
	 * @param	pool	UGBViewが管理するスレッドプール管理クラス
     * @param   rec		スレッド間通信の応答を処理するクラス
     */
	public UdcOpeThreadComReceiver(UdcThreadOperationPool pool, UdcOpeThreadComReceiverFace rec)
	{
		threadManager = pool;
		receiver = rec;
		ope = null;
	}

	/**
	 * UGBViewが管理する負荷分散スレッドから１つスレッドを確保し、そのスレッドに要求を送信する。
	 * @param recType スレッド間通信メッセージ種別
	 * @param ch スレッド間通信の応答メッセージを受信するためのスレッドチャネル
	 * @param act スレッド間通信で要求するアクションクラス
	 */
	public int request(int recType, UdcThreadChannel ch, UdcActionFace act) throws Exception
	{
		if (ope != null) {
			return -1;
		}

		/* 処理スレッド確保 */
		ope = (UdcThreadOperation)threadManager.allocateOperationThread(10*1000);
		if (ope == null) {
			return -1;
		}

		try {
			/* 要求基本情報設定 */
			channel = ch;
			recTimer = act.get_timeout();
			recMsgType = recType;
			recInvokeId = channel.newInvokeId();
			req = new UdcThreadChannelDtr(recInvokeId, recMsgType);
			req.set_data(act);
			req.set_replyChannel(channel);

			/* 処理スレッドへ要求 */
			ope.getChannel().push(req);
		} catch (Exception exp) {
			threadManager.releaseOperationThread(ope.getThreadIndex());
			throw exp;
		}

		/* 応答待ち設定 */
		thread = new Thread(this);
		thread.start();

		return 0;
	}

	/**
	 * スレッド処理を行う
	 */
	public void run()
	{
		String funcname = "UdcThreadComReceiver.run";

		/* 応答受信 */
		UdcThreadChannelDtr res = null;
		try {	
			UdcTrace.trace(UdcTrace.Level, funcname, " pull start, invId(" + recInvokeId + ") msg(" + recMsgType + ") timer(" + recTimer + ")");	
			res = channel.pull(recInvokeId, recMsgType, recTimer);
			req.set_replyChannel(null);
		} catch (Exception exp) {
			res = null;
			UdcTrace.trace(UdcTrace.Level, funcname, " run - start.");
		}

		/* 処理スレッド解放 */
		if (ope != null) {
			threadManager.releaseOperationThread(ope.getThreadIndex());
			ope = null;	
		}

		/* ユーザコールバック */
		// 直接の起動は、Swing同期がとれないためSwingUtilities.invokeLaterを使用して同期をとる
		// receiver.operatePerformed(res);
		SwingUtilities.invokeLater(new UdcOpeThreadComReceiverInvoke(receiver, res));
	}


	/**
	 * ユーザコールバック実行クラス
	 */
	class UdcOpeThreadComReceiverInvoke implements Runnable
	{
		/**
		 * スレッド間通信の応答を処理するクラス
		 */
		UdcOpeThreadComReceiverFace	receiver;

		/**
		 * スレッド間通信の応答情報
		 */
		UdcThreadChannelDtr result;

		/**
		 * コンストラクタ
     	 * @param   rec		スレッド間通信の応答を処理するクラス
     	 */
		public UdcOpeThreadComReceiverInvoke(UdcOpeThreadComReceiverFace rec, UdcThreadChannelDtr res)
		{
			receiver = rec;
			result = res;
		}

		/**
		 * スレッド間通信の応答処理Swing同期処理で実施する
		 */
		public void run()
		{
			/* ユーザコールバック */
			receiver.operatePerformed(result);
		}
	}
}
