/* *********************************************************************
 * @(#)JMediaPanel.java 1.2, 10 Mar 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.jmf;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import javax.swing.*;

import javax.media.*;
import javax.media.util.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.bean.playerbean.*;

import com.sun.media.util.JMFI18N;
import com.sun.media.rtp.RTPSessionMgr;
import com.sun.media.ui.TabControl;
import com.sun.media.ui.AudioFormatChooser;
import com.sun.media.ui.VideoFormatChooser;


/**
 * JMFを利用したメディア表示のためのパネル・コンポーネント。本クラスでは、JMFによる
 * メディア表示からRTP送受信によるメディア表示までをサポートする。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @see		JMediaRtpSndCtlListener
 * @see		JMediaRtpRecCtlListener
 * @see		JMediaCaptureCtlListener
 * @see		JMediaFormatDialog
 * @since   UDC1.2
 */
public class JMediaPanel extends JPanel implements ReceiveStreamListener
{
	/**
	 * メディア処理状態：空状態
 	 * @since   UDC1.2
	 */
	public static int ST_Empty		 	= 0;

	/**
	 * メディア処理状態：メディア表示状態
 	 * @since   UDC1.2
	 */
	public static int ST_MediaPlayer 	= 1;

	/**
	 * メディア処理状態：RTP受信によるメディア表示状態
 	 * @since   UDC1.2
	 */
	public static int ST_RtpReceiver 	= 2;

	/**
	 * メディア処理状態：RTP送信によるメディア表示状態
 	 * @since   UDC1.2
	 */
	public static int ST_RtpSender 		= 3;

	/**
	 * メディア処理状態
 	 * @since   UDC1.2
	 */
	int 					status = ST_Empty;

	/**
	 * Audio関連のパネル。Audioパネルは、本パネルのBorderLayout.SOUTHに配置される。
 	 * @since   UDC1.2
	 */
	JPanel					audioPanel;

	/**
	 * Video関連のパネル。Videoパネルは、本パネルのBorderLayout.CENTERに配置される。
 	 * @since   UDC1.2
	 */
	JPanel					videoPanel;

	/**
	 * audio用のRTPセッション。
 	 * @since   UDC1.2
	 */
	RTPSessionMgr			audioRtpSession = null;

	/**
	 * audio用のメディアプレイヤー。
 	 * @since   UDC1.2
	 */
	MediaPlayer				audioMediaPlayer;

	/**
	 * audio用のRTP受信によるメディアプレイヤーのイベントハンドラー。
 	 * @since   UDC1.2
	 */
	JMediaRtpRecCtlListener	audioListener;

	/**
	 * video用のRTPセッション。
 	 * @since   UDC1.2
	 */
	RTPSessionMgr			videoRtpSession = null;

	/**
	 * video用のメディアプレイヤー。
 	 * @since   UDC1.2
	 */
	MediaPlayer				videoMediaPlayer;

	/**
	 * video用のRTP受信によるメディアプレイヤーのイベントハンドラー。
 	 * @since   UDC1.2
	 */
	JMediaRtpRecCtlListener	videoListener;
	
	/**
	 * RTP送信時の送信情報のメディアプレイヤーのイベントハンドラー。
 	 * @since   UDC1.2
	 */
	JMediaRtpSndCtlListener	sndListener;

	/**
	 * RTP送信時の送信情報のメディアプレイヤー。
 	 * @since   UDC1.2
	 */
	MediaPlayer				sndMediaPlayer;

	/**
	 * メディアプレイ時のメディアプレイヤーのイベントハンドラー。
 	 * @since   UDC1.2
	 */
	JMediaCaptureCtlListener	capListener;

	/**
	 * メディアプレイ時のメディアプレイヤー。
 	 * @since   UDC1.2
	 */
	MediaPlayer					capMediaPlayer;


	/**
	 * コンストラクタ
	 *
 	 * @since   UDC1.2
	 */
	public JMediaPanel()
	{
		super(new BorderLayout());
		
		videoPanel = new JPanel(new BorderLayout());
		audioPanel = new JPanel(new BorderLayout());
		add(videoPanel, BorderLayout.CENTER);
		add(audioPanel, BorderLayout.SOUTH);
	}

	/**
	 * このコンポーネントのフォアグランドカラーを設定します。<br>
	 * 内部では、構成要素全てに対してsetForeground()関数を呼び出しているので、
	 * 詳細な制約はComponent.setForeground()のマニュアルを参照して下さい。
	 *
	 * @param	c	コンポーネントのフォアグランドカラー
 	 * @since   UDC1.2
	 */
	public void setForeground(Color c)
	{
		super.setForeground(c);
		if (videoPanel != null) {
			videoPanel.setForeground(c);
			audioPanel.setForeground(c);
		}
	}

	/**
	 * このコンポーネントのバックグランドカラーを設定します。<br>
	 * 内部では、構成要素全てに対してsetBackground()関数を呼び出しているので、
	 * 詳細な制約はComponent.setBackground()のマニュアルを参照して下さい。
	 *
	 * @param	c	コンポーネントのバックグランドカラー
 	 * @since   UDC1.2
	 */
	public void setBackground(Color c)
	{
		super.setBackground(c);
		if (videoPanel != null) {
			videoPanel.setBackground(c);
			audioPanel.setBackground(c);
		}
	}

	/**
	 * メディア処理状態を取得する。
	 *
	 * @return 	メディア処理状態
 	 * @since   UDC1.2
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * キャプチャ時のAudioデバイスリストを取得する。
	 *
	 * @return 	Audioデバイスリスト
 	 * @since   UDC1.2
	 */
	static public Vector getAudioDevices()
	{
		Format arrFormats[];
		Vector devices = (Vector)CaptureDeviceManager.getDeviceList(null).clone();
		Enumeration en = devices.elements();

		int i;
		Vector dvlist = new Vector();
		while (en.hasMoreElements()) {
			CaptureDeviceInfo cdi = (CaptureDeviceInfo)en.nextElement();
			arrFormats = cdi.getFormats();
			for (i=0; i<arrFormats.length; i++) {
				if (arrFormats[i] instanceof AudioFormat) {
					dvlist.add(cdi);
					break;
				}
			}
		}
		return dvlist;
	}

	/**
	 * キャプチャ時のVideoデバイスリストを取得する。
	 *
	 * @return 	Videoデバイスリスト
 	 * @since   UDC1.2
	 */
	static public Vector getVideoDevices()
	{
		Format arrFormats[];
		Vector devices = (Vector)CaptureDeviceManager.getDeviceList(null).clone();
		Enumeration en = devices.elements();

		int i;
		Vector dvlist = new Vector();
		while (en.hasMoreElements()) {
			CaptureDeviceInfo cdi = (CaptureDeviceInfo)en.nextElement();
			arrFormats = cdi.getFormats();
			for (i=0; i<arrFormats.length; i++) {
				if (arrFormats[i] instanceof VideoFormat) {
					dvlist.add(cdi);
					break;
				}
			}
		}
		return dvlist;
	}

	/**
	 * メディアファイルをRTP通信によって送信する。<br>
	 * 自パネルには送信しているメディア情報が表示される。送信できるメディアファイルについては、JMFマニュアルを参照のこと。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常／-3:processor異常／-4,-5:processorのデータソース異常／-6,-7:RTPセッション異常
	 * @param	url				メディアファイル
	 * @param	srcAddr			RTP通信ローカルアドレス
	 * @param	srcAudioPort	RTP通信Audio用ローカルポート
	 * @param	srcVideoPort	RTP通信Video用ローカルポート
	 * @param	dstAddr			RTP通信宛先アドレス
	 * @param	dstAudioPort	RTP通信Audio用宛先ポート
	 * @param	dstVideoPort	RTP通信Video用宛先ポート
	 * @param	audioTtl		RTP通信Audio用TTL
	 * @param	videoTtl		RTP通信Video用TTL
 	 * @since   UDC1.2
	 */
	public synchronized int openRtpSend(String url,
										String srcAddr, int srcAudioPort, int srcVideoPort,
										String dstAddr, int dstAudioPort, int dstVideoPort, int audioTtl, int videoTtl)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		/* create processor */
		DataSource urlSource = null;
		try {
			MediaLocator mediaSource = new MediaLocator(url);
			urlSource = Manager.createDataSource(mediaSource);
		} catch(Exception exp) {
			return -2;
		}
		Processor processor = null;
		try {
			processor = Manager.createProcessor(urlSource);
		} catch(Exception exp) {
			return -3;	
		}

		/* open rtp to send */
		return openRtpSend(processor, srcAddr, srcAudioPort, srcVideoPort, dstAddr, dstAudioPort, dstVideoPort, audioTtl, videoTtl);
	}

	/**
	 * キャプチャ情報をRTP通信によって送信する。<br>
	 * 自パネルには送信しているメディア情報が表示される。送信できるメディアファイルについては、JMFマニュアルを参照のこと。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常／-3:processor異常／-4,-5:processorのデータソース異常／-6,-7:RTPセッション異常
	 * @param	audioDv			Audio用デバイス
	 * @param	videoDv			Video用デバイス
	 * @param	srcAddr			RTP通信ローカルアドレス
	 * @param	srcAudioPort	RTP通信Audio用ローカルポート
	 * @param	srcVideoPort	RTP通信Video用ローカルポート
	 * @param	dstAddr			RTP通信宛先アドレス
	 * @param	dstAudioPort	RTP通信Audio用宛先ポート
	 * @param	dstVideoPort	RTP通信Video用宛先ポート
	 * @param	audioTtl		RTP通信Audio用TTL
	 * @param	videoTtl		RTP通信Video用TTL
 	 * @since   UDC1.2
	 */
	public synchronized int openRtpSend(CaptureDeviceInfo audioDv, CaptureDeviceInfo videoDv,
										String srcAddr, int srcAudioPort, int srcVideoPort,
										String dstAddr, int dstAudioPort, int dstVideoPort, int audioTtl, int videoTtl)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		/* create processor */
		DataSource dvSource = getDeviceDataSource(audioDv, videoDv);
		if (dvSource == null) {
			return -2;
		}
		Processor processor = null;
		try {
			processor = Manager.createProcessor(dvSource);
		} catch(Exception exp) {
			return -3;	
		}

		/* open rtp to send */
		return openRtpSend(processor, srcAddr, srcAudioPort, srcVideoPort, dstAddr, dstAudioPort, dstVideoPort, audioTtl, videoTtl);
	}

	/**
	 * プロセッサーのデータソースをRTP通信によって送信する。<br>
	 * 自パネルには送信しているメディア情報が表示される。送信できるメディアファイルについては、JMFマニュアルを参照のこと。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常／-3:processor異常／-4,-5:processorのデータソース異常／-6,-7:RTPセッション異常
	 * @param	processor		JMF:プロセッサー
	 * @param	srcAddr			RTP通信ローカルアドレス
	 * @param	srcAudioPort	RTP通信Audio用ローカルポート
	 * @param	srcVideoPort	RTP通信Video用ローカルポート
	 * @param	dstAddr			RTP通信宛先アドレス
	 * @param	dstAudioPort	RTP通信Audio用宛先ポート
	 * @param	dstVideoPort	RTP通信Video用宛先ポート
	 * @param	audioTtl		RTP通信Audio用TTL
	 * @param	videoTtl		RTP通信Video用TTL
 	 * @since   UDC1.2
	 */
	public synchronized int openRtpSend(Processor processor,
										String srcAddr, int srcAudioPort, int srcVideoPort,
										String dstAddr, int dstAudioPort, int dstVideoPort, int audioTtl, int videoTtl)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		status = ST_RtpSender;
		sndListener = new JMediaRtpSndCtlListener(processor);

		/* configure processor (configure() +  update processor format(include format select dialog) + realize() */	
		sndListener.openProcessor(this);

		/* create session */
		DataSource os = processor.getDataOutput();
		if (os == null || !(os instanceof PushBufferDataSource)) {
			return -4;
		}
		PushBufferStream streams[] = ((PushBufferDataSource)os).getStreams();
		if (streams == null) {
			return -5;
		}
		RTPSessionMgr ses = null;
		for (int i=0; i<streams.length; i++) {
			Format format = streams[i].getFormat();		
			try {
				if (format instanceof VideoFormat) {
					videoRtpSession = createSessionManager(srcAddr, srcVideoPort, dstAddr, dstVideoPort, videoTtl);
					ses = videoRtpSession;
				} else if (format instanceof AudioFormat) {
					audioRtpSession = createSessionManager(srcAddr, srcAudioPort, dstAddr, dstAudioPort, audioTtl);
					ses = audioRtpSession;
				} else {
					continue;
				}
				if (ses == null) {
					closeRtpSend();
					return -6;
				}
				SendStream sendstream = ses.createSendStream(os,i);
				sendstream.start();
			} catch(Exception exp) {
				closeRtpSend();
				return -7;
			}
		}

		/* create media-player */
		sndMediaPlayer = new MediaPlayer();	
		sndMediaPlayer.setPlayer(processor);
		sndMediaPlayer.setFixedAspectRatio(true);
		sndMediaPlayer.setControlPanelVisible(false);
		sndMediaPlayer.setPopupActive (false);
		sndMediaPlayer.addControllerListener(sndListener);
		sndListener.setMediaPlayer(sndMediaPlayer, videoPanel);
		sndMediaPlayer.realize();

		return 0;
	}

	/**
	 * RTP送信を停止する。
	 *
	 * @return 	0:正常／-1:状態異常
 	 * @since   UDC1.2
	 */
	public synchronized int closeRtpSend()
	{
		/* check status */
		if (status != ST_RtpSender) {
			return -1;
		}

		/* close media-player */
		videoPanel.removeAll();
		audioPanel.removeAll();
		if (videoRtpSession != null) { videoRtpSession.dispose(); }
		if (audioRtpSession != null) { audioRtpSession.dispose(); }
		if (sndListener != null) {
			sndListener.close();
		//} else {
		//	if (sndMediaPlayer != null) { sndMediaPlayer.close(); }
		}

		/* set managed data */
		status = ST_Empty;

		audioRtpSession = null;
		videoRtpSession = null;
		sndMediaPlayer = null;
		sndListener = null;

		return 0;
	}

	/**
	 * RTP通信によって受信したデータソースを自パネルに表示する。<br>
	 * RTP通信によってデータを受信しない場合には、受信するまで待ちます。但し本関数は受信するまえにユーザに
	 * 制御権を返却します。<br>
	 * また、一度接続し、その後RTPセッションが切れた場合には、自動的に接続はしないので、closeRtpReceiveによって
	 * 切断した後、再度本関数で接続する必要があります。
	 *
	 * @return 	0:正常／-1:状態異常／-2:RTP受信データソース異常
	 * @param	srcAddr			RTP通信ローカルアドレス
	 * @param	srcAudioPort	RTP通信Audio用ローカルポート
	 * @param	srcVideoPort	RTP通信Video用ローカルポート
	 * @param	audioTtl		RTP通信Audio用TTL
	 * @param	videoTtl		RTP通信Video用TTL
 	 * @since   UDC1.2
	 */
	public synchronized int openRtpReceive(String srcAddr, int srcAudioPort, int srcVideoPort, int audioTtl, int videoTtl)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		/* create rtp-session for receive */
		status = ST_RtpReceiver;
			/* audio */
		if (srcAudioPort > 0) {
			audioRtpSession = createSessionManager(srcAddr, srcAudioPort, srcAddr, srcAudioPort, audioTtl);
			if (audioRtpSession == null) {
				closeRtpReceive();
				return -2;
			}
			audioRtpSession.addReceiveStreamListener(this);
		}
			/* video */
		if (srcVideoPort > 0) {
			videoRtpSession = createSessionManager(srcAddr, srcVideoPort, srcAddr, srcVideoPort, videoTtl);
			if (videoRtpSession == null) {
				closeRtpReceive();
				return -2;
			}
			videoRtpSession.addReceiveStreamListener(this);
		}

		return 0;
	}

	/**
	 * RTP受信を停止する。
	 *
	 * @return 	0:正常／-1:状態異常
 	 * @since   UDC1.2
	 */
	public synchronized int closeRtpReceive()
	{
		/* check status */
		if (status != ST_RtpReceiver) {
			return -1;
		}

		/* close media-player */
		videoPanel.removeAll();
		if (videoListener != null) {
			videoListener.close();
		} else {
			if (videoRtpSession != null) { videoRtpSession.dispose(); }
			if (videoMediaPlayer != null) { videoMediaPlayer.close(); }
		}

		audioPanel.removeAll();
		if (audioListener != null) {
			audioListener.close();
		} else {
			if (audioRtpSession != null) { audioRtpSession.dispose(); }
			if (audioMediaPlayer != null) { audioMediaPlayer.close(); }
		}

		/* set managed data */
		status = ST_Empty;

		audioRtpSession = null;
		audioMediaPlayer = null;
		audioListener = null;

		videoRtpSession = null;
		videoMediaPlayer = null;
		videoListener = null;

		return 0;
	}

	/**
	 * キャプチャ情報をメディアプレイヤーで表示する。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常
	 * @param	audioDv			Audio用デバイス
	 * @param	videoDv			Video用デバイス
 	 * @since   UDC1.2
	 */
	public synchronized int open(CaptureDeviceInfo audioDv, CaptureDeviceInfo videoDv)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		/* open media-palyer */
		DataSource dvSource = getDeviceDataSource(audioDv, videoDv);
		if (dvSource == null) {
			return -2;
		}

		return open(dvSource);
	}

	/**
	 * 指定メディアファイル情報をメディアプレイヤーで表示する。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常
	 * @param	url		メディアファイル
 	 * @since   UDC1.2
	 */
	public synchronized int open(String url)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		/* open media-palyer */
		DataSource urlSource = null;
		try {
			MediaLocator mediaSource = new MediaLocator(url);
			urlSource = Manager.createDataSource(mediaSource);
		} catch(Exception exp) {
			return -2;
		}

		return open(urlSource);
	}

	/**
	 * 指定データソースをメディアプレイヤーで表示する。
	 *
	 * @return 	0:正常／-1:状態異常／-2:データソース異常
	 * @param	source		データソース
 	 * @since   UDC1.2
	 */
	public synchronized int open(DataSource source)
	{
		/* check status */
		if (status != ST_Empty) {
			return -1;
		}

		status = ST_MediaPlayer;

		/* create media-palyer */
		capMediaPlayer = new MediaPlayer();
		capMediaPlayer.setDataSource(source);

		capListener = new JMediaCaptureCtlListener(capMediaPlayer, videoPanel);
		capMediaPlayer.addControllerListener(capListener);
		capMediaPlayer.setFixedAspectRatio(true);
		capMediaPlayer.setControlPanelVisible(false);
		capMediaPlayer.setPopupActive (false);
		capMediaPlayer.realize();	

		return 0;
	}

	/**
	 * メディアプレイヤーで表示を停止する。
	 *
	 * @return 	0:正常／-1:状態異常
 	 * @since   UDC1.2
	 */
	public synchronized int close()
	{
		/* check status */
		if (status != ST_MediaPlayer) {
			return -1;
		}

		/* close media-player */
		videoPanel.removeAll();
		if (capListener != null) {
			capListener.close();
		} else {
			if (capMediaPlayer != null) { capMediaPlayer.close(); }
		}

		/* set managed data */
		status = ST_Empty;

		capMediaPlayer = null;
		capListener = null;

		return 0;
	}

	/**
	 * ReceiveStreamListenerのイベントハンドリング関数。<br>
	 * RTPセッションでの受信監視を行ない、受信を検知したら、受信情報をメディアプレイヤーで
	 * 再生する。
	 *
	 * @param	event	ReceiveStreamイベント
	 * @see ReceiveStreamListener
 	 * @since   UDC1.2
	 */
	public synchronized void update(ReceiveStreamEvent event)	
	{
		if (event instanceof NewReceiveStreamEvent) {
			if (status != ST_RtpReceiver) {
				return;
			}
			RTPSessionMgr ses = (RTPSessionMgr)event.getSource();
			ReceiveStream stream =((NewReceiveStreamEvent)event).getReceiveStream();
			DataSource ds = stream.getDataSource();
			/* audio */
			if (ses == audioRtpSession) {
				if (audioMediaPlayer != null) {
					audioPanel.removeAll();
					audioMediaPlayer.removeControllerListener(audioListener);
					audioMediaPlayer.close();
				}
				audioMediaPlayer = new MediaPlayer();
				audioListener = new JMediaRtpRecCtlListener(audioMediaPlayer, audioPanel, ses);
				audioMediaPlayer.setDataSource(ds);
				audioMediaPlayer.setFixedAspectRatio(true);
				audioMediaPlayer.setControlPanelVisible(false);
        		audioMediaPlayer.setPopupActive ( false );
				audioMediaPlayer.addControllerListener(audioListener);
				audioMediaPlayer.realize();
			/* video */
			} else if (ses == videoRtpSession) {
				if (videoMediaPlayer != null) {
					videoPanel.removeAll();
					videoMediaPlayer.removeControllerListener(videoListener);
					videoMediaPlayer.close();
				}
				videoMediaPlayer = new MediaPlayer();
				videoListener = new JMediaRtpRecCtlListener(videoMediaPlayer, videoPanel, ses);
				videoMediaPlayer.setDataSource(ds);
				videoMediaPlayer.setFixedAspectRatio(true);
				videoMediaPlayer.setControlPanelVisible(false);
        		videoMediaPlayer.setPopupActive ( false );
				videoMediaPlayer.addControllerListener(videoListener);
				videoMediaPlayer.realize();
			} else {
				return;
			}
		}
	}

	/**
	 * RTPセッションを生成する。RTP受信のためのセッションでは dstAddr/dstPortは srcAddr/srcPortと同値
	 * を設定しておけば良い。
	 *
	 * @return	RTPセッション
	 * @param	srcAddr		ローカルアドレス
	 * @param	srcPort		ローカルポート
	 * @param	dstAddr		宛先アドレス
	 * @param	dstPort		宛先ポート
	 * @param	ttl			TTL
 	 * @since   UDC1.2
	 */
	RTPSessionMgr createSessionManager(String srcAddr, int srcPort, String dstAddr, int dstPort, int ttl)
	{
		RTPSessionMgr sesmgr = (RTPSessionMgr)RTPManager.newInstance();
		if (sesmgr == null) {
			return null;
		}
		try {
			InetAddress srcaddr = InetAddress.getByName(srcAddr);
			InetAddress dstaddr = InetAddress.getByName(dstAddr);
			//SessionAddress dstses = new SessionAddress(dstaddr, dstPort, dstaddr, dstPort+1);
			SessionAddress sesLocal, sesRemote;
			if (dstaddr.isMulticastAddress()) {
				sesLocal = new SessionAddress(dstaddr, dstPort, ttl);
				sesRemote = new SessionAddress(dstaddr, dstPort, ttl);
			} else {
				sesLocal = new SessionAddress(srcaddr, srcPort);	
				sesRemote = new SessionAddress(dstaddr, dstPort);	
			}
			sesmgr.initialize(sesLocal);
			sesmgr.addTarget(sesRemote);
		} catch(Exception exp) {
			return null;
		}
		return sesmgr;
	}

	/**
	 * 指定されたデバイスと接続し、データソースを取得する。<br>
	 * Audio/Videoの両デバイスを指定している場合には、二つのデバイスのデータソースをマージした
	 * データソースを返却する。
	 *
	 * @return 	データソース
	 * @param	audioDv			Audio用デバイス
	 * @param	videoDv			Video用デバイス
 	 * @since   UDC1.2
	 */
	DataSource getDeviceDataSource(CaptureDeviceInfo audioDv, CaptureDeviceInfo videoDv)
	{
		DataSource videoSource=null, audioSource=null, mergedSource=null;

		/* audio device */
		if (audioDv != null) {
			MediaLocator audioLocator = audioDv.getLocator();
			try {
				audioSource = javax.media.Manager.createDataSource(audioLocator);
				audioSource.connect();
				mergedSource = audioSource;
			} catch(Exception exp) {
				return null;
			}
		}

		/* video device */
		if (videoDv != null) {
			MediaLocator videoLocator = videoDv.getLocator();	
			try {
				videoSource  = javax.media.Manager.createDataSource(videoLocator);
				videoSource.connect();
				mergedSource = videoSource;
			} catch(Exception exp) {
				if (audioSource != null) {
					try { audioSource.disconnect(); } catch(Exception aexp) {}
				}
				return null;
			}
		}

		/* merged source */
		if (audioSource != null && videoSource != null) {
			DataSource datasource[] = { audioSource, videoSource };
			try {
				mergedSource = javax.media.Manager.createMergingDataSource(datasource);
			} catch(Exception exp) {
				try {
					audioSource.disconnect();
					videoSource.disconnect();
				} catch(Exception aexp) {}
				return null;
			}
		}

		return mergedSource;
	}
}
