/* *********************************************************************
 * @(#)JMediaFormatDialog.java 1.2, 10 Mar 2006
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
 * JMFを利用したメディアのAudio/Videoフォーマット決定のためのダイアログクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JMediaFormatDialog extends JDialog implements ActionListener
{
	/**
	 * ダイアログの入力状態:未入力 
 	 * @since   UDC1.2
	 */
	final public static int ACTION_EMPTY	= 0;

	/**
	 * ダイアログの入力状態:入力完了
 	 * @since   UDC1.2
	 */
	final public static int ACTION_YES		= 1;

	//final public static int ACTION_NO		= 2;

	/**
	 * ダイアログの入力状態
 	 * @since   UDC1.2
	 */
	int		 	status = ACTION_EMPTY;

	/**
	 * メディアのディスクリプタ
 	 * @since   UDC1.2
	 */
	String 		contentDiscriptor;

	/**
	 * メディアを表示/送信するためのメディアプレイヤーを配備するパネル
 	 * @since   UDC1.2
	 */
	Component	parComponent;

	/**
	 * ダイアログの入力完了ボタン
 	 * @since   UDC1.2
	 */
	JButton		yesBt;

	//JButton		noBt;

	/**
	 * プロセッサー
	 */
	Processor	processor = null;

	/**
	 * Audio用のフォーマットChooserリスト
 	 * @since   UDC1.2
	 */
	Vector	audioChooserList = new Vector();

	/**
	 * Audio用のトラックコントロールリスト
 	 * @since   UDC1.2
	 */
	Vector	audioTrackList = new Vector();

	/**
	 * Video用のフォーマットChooserリスト
 	 * @since   UDC1.2
	 */
	Vector	videoChooserList = new Vector();

	/**
	 * Video用のトラックコントロールリスト
 	 * @since   UDC1.2
	 */
	Vector 	videoTrackList = new Vector();


	/**
	 * コンストラクタ
	 *
	 * @param	proc		プロセッサー
	 * @param	par			本ダイアログの親フレーム
	 * @param	contdiscr	メディアのディスクリプタ
 	 * @since   UDC1.2
	 */
	public JMediaFormatDialog(Processor proc,  Frame par, String contdiscr)
	{
		super(par, "Media Format", true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		contentDiscriptor = contdiscr;
		processor = proc;
		initDialog();
		changeContentType();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	proc		プロセッサー
	 * @param	par			本ダイアログの親ダイアログ
	 * @param	contdiscr	メディアのディスクリプタ
 	 * @since   UDC1.2
	 */
	public JMediaFormatDialog(Processor proc,  Dialog par, String contdiscr)
	{
		super(par, "Media Format", true);
		contentDiscriptor = contdiscr;
		processor = proc;
		initDialog();
		changeContentType();
	}

	/**
	 * ダイアログの入力状態を取得する
	 *
	 * @return	ダイアログの入力状態
 	 * @since   UDC1.2
	 */
	public int getAction()
	{
		return status;
	}

	/**
	 * 本ダイアログを生成する。
	 *
	 * @param	proc		プロセッサー
	 * @param	par			本ダイアログの親ダイアログ
	 * @param	contdiscr	メディアのディスクリプタ
 	 * @since   UDC1.2
	 */
	public static JMediaFormatDialog newInstance(Processor proc, Component par, String contdiscr)
	{
		Component tmp = par;
		while (tmp != null) {
			if (tmp instanceof Frame) {
				return new JMediaFormatDialog(proc, (Frame)tmp, contdiscr);
			} else if (tmp instanceof Dialog) {
				return new JMediaFormatDialog(proc, (Dialog)tmp, contdiscr);
			}
			tmp = tmp.getParent();
		}
		return null;
	}

	/**
	 * 本ダイアログの構成要素を初期化する。
	 *
 	 * @since   UDC1.2
	 */
	void initDialog()
	{
		AudioFormatChooser	chooserAudio;
		VideoFormatChooser	chooserVideo;
		TabControl tabControl = new TabControl(TabControl.ALIGN_TOP);

		resetFormatDialog();	
		TrackControl trackControls[] = processor.getTrackControls();

		for (int i=0; i<trackControls.length; i++) {
			Format format = trackControls[i].getFormat();
			/* Audio Format */
			if (format instanceof AudioFormat) {
				chooserAudio = new AudioFormatChooser(trackControls[i].getSupportedFormats(), (AudioFormat)format, true, this);
				chooserAudio.setTrackEnabled (trackControls[i].isEnabled());
				tabControl.addPage(chooserAudio, ("AudioFormat" + i), null);
				audioChooserList.add(chooserAudio);
				audioTrackList.add(trackControls[i]);
			/* Video Format */
			} else if (format instanceof VideoFormat) {
				chooserVideo = new VideoFormatChooser(trackControls[i].getSupportedFormats(), (VideoFormat)format, true, this);
				chooserVideo.setTrackEnabled (trackControls[i].isEnabled());
				tabControl.addPage(chooserVideo, ("VideoFormat" + i), null);
				videoChooserList.add(chooserVideo);
				videoTrackList.add(trackControls[i]);
			}
		}

		JPanel formatPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		yesBt = new JButton(" SET  ");
		yesBt.addActionListener(this);
		buttonPanel.add(yesBt);
		//noBt = new JButton("CANCEL");
		//noBt.addActionListener(this);
		//buttonPanel.add(noBt);
		formatPanel.add(tabControl, BorderLayout.CENTER);
		formatPanel.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(formatPanel);
		setSize(384,384);
	}

	/**
	 * プロセッサのディスクリプタを変更し、Audio/Videoフォーマットchooserに反映する。
	 *
 	 * @since   UDC1.2
	 */
	public void changeContentType()
	{
		int i;
		AudioFormatChooser	chooserAudio;
		VideoFormatChooser	chooserVideo;
		TrackControl        trackControl;

		processor.setContentDescriptor(new ContentDescriptor(contentDiscriptor));	

		for (i=0; i<audioChooserList.size(); i++) {
			chooserAudio = (AudioFormatChooser)audioChooserList.get(i);
			trackControl = (TrackControl)audioTrackList.get(i);	
			chooserAudio.setSupportedFormats(trackControl.getSupportedFormats(), (AudioFormat)trackControl.getFormat());
		}
		for (i=0; i<videoChooserList.size(); i++) {
			chooserVideo = (VideoFormatChooser)videoChooserList.get(i);
			trackControl = (TrackControl)videoTrackList.get(i);	
			chooserVideo.setSupportedFormats(trackControl.getSupportedFormats(), (VideoFormat)trackControl.getFormat());
		}
	}

	/**
	 * ActionListenerのハンドリング関数。
	 *
	 * @param	e	アクションイベント
 	 * @since   UDC1.2
	 */
	public void actionPerformed(ActionEvent e)	
	{
		if (e.getSource() == yesBt) {
			actionPerformed_setFormat(e);

		/**
		} else if (e.getSource() == noBt) {
			status = ACTION_NO;
			dispose();
			return;
		**/
		}
	}

	/**
	 * ダイアログの入力完了ボタンのハンドリング関数。
	 *
	 * @param	e	アクションイベント
 	 * @since   UDC1.2
	 */
	public void actionPerformed_setFormat(ActionEvent e)	
	{
		int 			i;
		Format 			rtpFormat;
		TrackControl 	trackControl;
		AudioFormatChooser	chooserAudio;
		VideoFormatChooser	chooserVideo;
		
		for (i=0; i<audioTrackList.size(); i++) {
			chooserAudio = (AudioFormatChooser)audioChooserList.get(i);
			trackControl = (TrackControl)audioTrackList.get(i);
			if ((rtpFormat=chooserAudio.getFormat()) != null) {
				trackControl.setEnabled(true);
				trackControl.setFormat(rtpFormat);
			} else {
				trackControl.setEnabled ( false );
			}
		}
		for (i=0; i<videoTrackList.size(); i++) {
			chooserVideo = (VideoFormatChooser)videoChooserList.get(i);
			trackControl = (TrackControl)videoTrackList.get(i);
			if ((rtpFormat=chooserVideo.getFormat()) != null) {
				trackControl.setEnabled(true);
				trackControl.setFormat(rtpFormat);
			} else {
				trackControl.setEnabled ( false );
			}
		}
		status = ACTION_YES;
		dispose();
		return;
	}

	/**
	 * ダイアログの入力フォーマットリソースを解放する。
	 *
 	 * @since   UDC1.2
	 */
	void resetFormatDialog()
	{
		audioChooserList.clear();
		audioTrackList.clear();
		videoChooserList.clear();
		videoTrackList.clear();
	}
}
