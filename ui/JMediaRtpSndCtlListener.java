/* *********************************************************************
 * @(#)JMediaRtpSndCtlListener.java 1.2, 10 Mar 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.jmf;

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.bean.playerbean.*;

import com.sun.media.util.JMFI18N;
import com.sun.media.ui.TabControl;
import com.sun.media.ui.AudioFormatChooser;
import com.sun.media.ui.VideoFormatChooser;
import com.sun.media.rtp.RTPSessionMgr;

/**
 * JMFを利用したRTPによるメディア送信のためのイベントハンドリングクラス。<br>
 * ユーザは、JMediaPanelのみを意識し、本クラスは意識しないはずである。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JMediaRtpSndCtlListener implements ControllerListener
{
	/**
	 * 送信メディアのプロセッサー
 	 * @since   UDC1.2
	 */
	Processor	processor = null;	

	/**
	 * 送信メディアを表示するためのメディアプレイヤー
 	 * @since   UDC1.2
	 */
	MediaPlayer	mediaPlayer = null;

	/**
	 * 送信メディアを表示するためのメディアプレイヤーを配備するパネル
 	 * @since   UDC1.2
	 */
	JPanel		parPanel = null;
	
	/**
	 * メディアプレイヤーのconfigure/realizeの状態遷移のためのロックリソース
 	 * @since   UDC1.2
	 */
	Object 	stateLock = new Object();

	/**
	 * メディアプレイヤーのconfigure/realizeの状態遷移のための状態情報
 	 * @since   UDC1.2
	 */
	boolean stateFailed = false;


	/**
	 * コンストラクタ
	 *
	 * @param	proc	送信メディアのプロセッサー
 	 * @since   UDC1.2
	 */
	public JMediaRtpSndCtlListener(Processor proc)
	{
		processor = proc;
	}

	/**
	 * 送信メディアを表示するためのメディアプレイヤーとそれを配備するパネルの設定を行なう
	 *
	 * @param	mp	送信メディアを表示するためのメディアプレイヤー
	 * @param	par	送信メディアを表示するためのメディアプレイヤーを配備するパネル
 	 * @since   UDC1.2
	 */
	public void setMediaPlayer(MediaPlayer mp, JPanel par)
	{
		mediaPlayer = mp;
		parPanel = par;
	}

	/**
	 * RTP送信のためのプロセッサーの初期化を行なう。具体的には以下の処理を行う。<br>
	 * ①プロセッサーの configure()<br>
	 * ②送信するRTPのAudio/Videoフォーマットの入力（ダイアログによるユーザの入力）<br>
	 * ③プロセッサーの realize()<br>
	 *
	 * @return	0:正常／-1:configure異常／-2:フォーマット入力異常／realize異常
	 * @param	parComponent	フォーマット入力のためのダイアログを表示するためのモーダルの元コンポーネント
 	 * @since   UDC1.2
	 */
	public int openProcessor(Component parComponent)
	{
		int ret; 

		/* configure processor */
		if ((ret=operateProcessor(Processor.Configured)) != 0) {
			return -1;
		}
		//processor.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW_RTP));

		/* update format (create dialog for select format) */
		JMediaFormatDialog formatDialog = JMediaFormatDialog.newInstance(processor, parComponent, ContentDescriptor.RAW_RTP);
		if (formatDialog == null) {
			return -100;
		}
		formatDialog.show();
		ret = formatDialog.getAction();	
		if (ret != JMediaFormatDialog.ACTION_YES) {
			return -2;	
		}

		/* realize processor */
		if ((ret=operateProcessor(Processor.Realized)) != 0) {
			return -3;
		}

		return 0;
	}

	/**
	 * RTP送信リソースを全て停止する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void close()
	{
		if (parPanel != null) {
			parPanel.removeAll();
			parPanel.repaint();
		}
		if (processor != null) {
			processor.close ();
		}
		/*
		if (mediaPlayer != null) {
			mediaPlayer.removeControllerListener(this);
			mediaPlayer.close();
		}
		*/
		
		parPanel = null;
		processor = null;
		mediaPlayer = null;
	}

	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのイベントハンドリング関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
	 * @see ControllerListener
 	 * @since   UDC1.2
	 */
	public synchronized void controllerUpdate(ControllerEvent event)
	{
		if (event instanceof RealizeCompleteEvent) 		{ realizeComplete((RealizeCompleteEvent)event); }
		else if (event instanceof PrefetchCompleteEvent){ prefetchComplete((PrefetchCompleteEvent)event); }
		else if (event instanceof ControllerErrorEvent) { controllerError((ControllerErrorEvent)event); }
		else if (event instanceof ControllerClosedEvent){ controllerClose((ControllerClosedEvent)event); }
		else if (event instanceof StartEvent) 			{ startComplete((StartEvent)event); }
		else if (event instanceof DurationUpdateEvent) 	{ ; }
		else if (event instanceof CachingControlEvent)	{ ; }
		else if (event instanceof MediaTimeSetEvent) 	{ ; }
		else if (event instanceof TransitionEvent)		{ ; }
		else if (event instanceof RateChangeEvent)		{ ; }
		else if (event instanceof StopTimeChangeEvent)	{ ; }
		else if (event instanceof FormatChangeEvent)	{ ; }
		else if (event instanceof SizeChangeEvent)		{ ; }
		else if (event.getClass().getName().endsWith("ReplaceURLEvent")) { ; }
	}

	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのrealize完了イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void realizeComplete(RealizeCompleteEvent event)
	{
		Dimension dim = parPanel.getSize();

		if (mediaPlayer.getVisualComponent() == null) {
			MonitorControl mc = (MonitorControl)mediaPlayer.getControl( "javax.media.control.MonitorControl" );	
			if (mc != null) {
				JPanel mainPanel=null, parentPanel=null, currentPanel;
				Component compControl;
				Control controls[] = mediaPlayer.getControls();
				for (int i=(controls.length-1); i>=0; i--) {
					if (!(controls[i] instanceof MonitorControl)) { continue; }
					mc = (MonitorControl)controls[i];
					//mc.setEnabled(true);
					if ((compControl=mc.getControlComponent()) == null) { continue; }
					currentPanel = new JPanel(new BorderLayout());
					currentPanel.add (compControl, BorderLayout.CENTER);
					if (parentPanel == null){ mainPanel = currentPanel; }
					else 					{ parentPanel.add(currentPanel, BorderLayout.SOUTH); }
					parentPanel = currentPanel;
				}
				if (mainPanel != null) {
					parPanel.add(mainPanel, BorderLayout.CENTER);
				}
			}
		} else {
			parPanel.add(mediaPlayer, BorderLayout.CENTER);
		}
		Dimension pdim = parPanel.getPreferredSize();
		parPanel.setSize(pdim.width, pdim.height);
		parPanel.validate();
		parPanel.repaint();
		parPanel.setSize(dim.width, dim.height);
		parPanel.validate();
		parPanel.repaint();

		/* wait for visual to show up */
		Component compVis = mediaPlayer.getVisualComponent();
		if (compVis != null) {
			while (!compVis.isVisible()) {
				try { Thread.sleep(100); } catch(Exception exp) {}
			}
		}
		try { Thread.sleep(1000); } catch(Exception exp) {}

		/* prefetch */
		mediaPlayer.prefetch();
	}

	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのprefetch完了イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void prefetchComplete(PrefetchCompleteEvent event)
	{
		if (mediaPlayer.getTargetState() != Controller.Started ) {
			mediaPlayer.start();
		}
	}
	
	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのstart完了イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void startComplete(StartEvent event)
	{
		/*
		mediaPlayer.validate();
		mediaPlayer.repaint();
		parPanel.repaint();
		*/
	}

	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのControllerError発生イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void controllerError(ControllerErrorEvent event)
	{
		close();	
	}

	/**
	 * RTP送信を行なっているメディアを表示するためのメディアプレイヤーのController終了イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void controllerClose(ControllerClosedEvent event)
	{
		close();
	}

	/**
	 * RTP送信しているプロセッサのconfigure/realize実行関数。本関数で完了イベントが発生するまで中断します。
	 *
	 * @return	0:正常／非0:異常
	 * @param	state	configure/realize種別
 	 * @since   UDC1.2
	 */
	synchronized int operateProcessor(int state)
	{
		if (processor == null) {
			return -1;
		}

		/* configure */	
		stateFailed = false;
		StateListener sl = new StateListener();
		processor.addControllerListener(sl);

		if (state == Processor.Configured) {
			processor.configure();
		} else if (state == Processor.Realized) {
			processor.realize();
		}

		/* wait complete configure */
		while (processor.getState() < state && !stateFailed) {
			synchronized (stateLock) {
				try {
					stateLock.wait();
				} catch (InterruptedException ie) {
					return -1;
				}
			}
		}
		processor.removeControllerListener(sl);

		return 0;
	}

	/**
	 * プロセッサーのconfigure/realize完了イベント監視クラス。
	 *
	 * @author  Takayuki Uchida
	 * @version 1.2, 10 Mar 2006
	 * @since   UDC1.2
	 */
	class StateListener implements ControllerListener
	{
		/**
		 * コンストラクタ。
		 *
		 * @param	ce	プロセッサの制御イベント
	 	 * @since   UDC1.2
		 */
		public void controllerUpdate(ControllerEvent ce)
		{
			if (ce instanceof ControllerClosedEvent) {
				stateFailed = true;
			}
			if (ce instanceof ControllerEvent) {
				synchronized (stateLock) {
					stateLock.notifyAll();
				}
			}
		}
	}
}
