/* *********************************************************************
 * @(#)JMediaCaptureCtlListener.java 1.2, 10 Mar 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.jmf;

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;
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
 * JMFを利用したメディア再生のイベントハンドリングクラス。<br>
 * ユーザは、JMediaPanelのみを意識し、本クラスは意識しないはずである。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JMediaCaptureCtlListener implements ControllerListener
{
	/**
	 * 再生メディアを表示するためのメディアプレイヤーを配備するパネル
 	 * @since   UDC1.2
	 */
	JPanel			parPanel;

	/**
	 * 再生メディアを表示するためのメディアプレイヤー
 	 * @since   UDC1.2
	 */
	MediaPlayer		mediaPlayer;


	/**
	 * コンストラクタ
	 *
	 * @param	mp	再生メディアを表示するためのメディアプレイヤー
	 * @param	par	再生メディアを表示するためのメディアプレイヤーを配備するパネル
 	 * @since   UDC1.2
	 */
	public JMediaCaptureCtlListener(MediaPlayer mp, JPanel par)
	{
		mediaPlayer = mp;
		parPanel = par;
	}

	/**
	 * 再生リソースを全て停止する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void close()
	{
		if (parPanel != null) {
			parPanel.removeAll();
			parPanel.repaint();
		}
		if (mediaPlayer != null) {
			mediaPlayer.removeControllerListener(this);
			mediaPlayer.close();
		}
		parPanel = null;
		mediaPlayer = null;
	}

	/**
	 * RTP受信を行なってメディアを表示するためのメディアプレイヤーのイベントハンドリング関数。
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
	 * 最低を行なっているメディアプレイヤーのrealize完了イベント処理関数。
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
				JPanel parentPanel=null, currentPanel;
				Component compControl;
				Control controls[] = mediaPlayer.getControls();
				for (int i=0; i<controls.length; i++) {
					if (!(controls[i] instanceof MonitorControl)) { continue; }
					mc = (MonitorControl)controls[i];
					if ((compControl=mc.getControlComponent()) == null) {
						mc.setEnabled(true);
						continue;
					}
					currentPanel = new JPanel(new BorderLayout());
					currentPanel.add (compControl, BorderLayout.CENTER);
					if (parentPanel == null){
						parPanel.add(currentPanel, BorderLayout.CENTER);
					} else {
						parentPanel.add(currentPanel, BorderLayout.SOUTH);
					}
					mc.setEnabled(true);
					parentPanel = currentPanel;
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
	 * 再生を行なっているメディアプレイヤーのprefetch完了イベント処理関数。
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
	 * 再生を行なっているメディアプレイヤーのstart完了イベント処理関数。
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
	 * 再生を行なっているメディアプレイヤーのControllerError発生イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void controllerError(ControllerErrorEvent event)
	{
		close();
	}

	/**
	 * 再生を行なっているメディアプレイヤーのController終了イベント処理関数。
	 *
	 * @param	event	メディアプレイヤーの制御イベント
 	 * @since   UDC1.2
	 */
	void controllerClose(ControllerClosedEvent event)
	{
		close();
	}
}
