/* *********************************************************************
 * @(#)JTopologyPanel.java 1.2, 10 Mar 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.topology;

import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;


/**
 * ネットワーク・トポロジーを表示するためのパネル・コンポーネント。本クラスは、
 * LayoutマネージャなしのJPanelを継承し、paintComponentをオーバライドしてトポロジー
 * 情報を表示します。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JTopologyPanel extends JPanel implements MouseListener, MouseMotionListener
{
	/**
	 * 背景のイメージ
 	 * @since   UDC1.2
	 */
	Image __backImage;

	/**
	 * 背景のイメージサイズ
 	 * @since   UDC1.2
	 */
	Dimension __backImageSize;

	/**
	 * 本パネルの初期サイズ
 	 * @since   UDC1.2
	 */
	Dimension __baseSize = null;

	/**
	 * コネクションのリスト
 	 * @since   UDC1.2
	 */
	ArrayList	__connList;

	/**
	 * コネクション点滅色設定
 	 * @since   UDC1.22
	 */
	boolean connBlinkReverse = false;

	/**
	 * ライン表示のアニメーション・ステップ数
 	 * @since   UDC1.2
	 */
	int connAnimationStep = 0;

	/**
	 * タイマー
 	 * @since   UDC1.2
	 */
	java.util.Timer	__timer;	

	/**
	 * 点滅周期時間
 	 * @since   UDC1.2
	 */
	long __militimer = 800;

	/**
	 * 点滅処理タスク
 	 * @since   UDC1.2
	 */
	JTopologyPanel_TimerTask __timerTask = null; 

	/**
	 * ドラッグ中のノード情報
 	 * @since   UDC1.2
	 */
	protected JComponent draggedComponent = null;

	/**
	 * コネクション表示のための印長のデフォルト
 	 * @since   UDC1.2
	 */
	int	markLen = 8;

	/**
	 * コネクション表示のための数学計算値：30度のラジアン
 	 * @since   UDC1.2
	 */
	double th30 = Math.PI / 6;
	double cos30 = Math.cos(th30);
	double sin30 = Math.sin(th30);

	/**
	 * コネクション表示のための数学計算値：45度のラジアン
 	 * @since   UDC1.2
	 */
	double th45 = Math.PI / 4;
	double cos45 = Math.cos(th45);
	double sin45 = Math.sin(th45);

	/**
	 * コネクション表示のための数学計算値：90度のラジアン
 	 * @since   UDC1.2
	 */
	double th90 = Math.PI / 2;
	double cos90 = Math.cos(th90);
	double sin90 = Math.sin(th90);

	/**
	 * コネクション表示のための数学計算値：135度のラジアン
 	 * @since   UDC1.2
	 */
	double th135 = Math.PI*3 / 4;
	double cos135 = Math.cos(th135);
	double sin135 = Math.sin(th135);

	/**
	 * コネクション表示のための数学計算値：150度のラジアン
 	 * @since   UDC1.2
	 */
	double th150 = Math.PI*5 / 6;
	double cos150 = Math.cos(th150);
	double sin150 = Math.sin(th150);

	/**
	 * コネクション表示のための数学計算値：210度のラジアン
 	 * @since   UDC1.2
	 */
	double th210 = Math.PI*7 / 6;
	double cos210 = Math.cos(th210);
	double sin210 = Math.sin(th210);

	/**
	 * コネクション表示のための数学計算値：225度のラジアン
 	 * @since   UDC1.2
	 */
	double th225 = Math.PI*5 / 4;
	double cos225 = Math.cos(th225);
	double sin225 = Math.sin(th225);

	/**
	 * コネクション表示のための数学計算値：270度のラジアン
 	 * @since   UDC1.2
	 */
	double th270 = Math.PI*3 / 2;
	double cos270 = Math.cos(th270);
	double sin270 = Math.sin(th270);

	/**
	 * コネクション表示のための数学計算値：315度のラジアン
 	 * @since   UDC1.2
	 */
	double th315 = Math.PI*7 / 4;
	double cos315 = Math.cos(th315);
	double sin315 = Math.sin(th315);

	/**
	 * コネクション表示のための数学計算値：330度のラジアン
 	 * @since   UDC1.2
	 */
	double th330 = Math.PI*11 / 6;
	double cos330 = Math.cos(th330);
	double sin330 = Math.sin(th330);

	/**
	 * コネクション表示のための数学計算情報の一時格納エリア
 	 * @since   UDC1.2
	 */
	int ax[] = new int[6];

	/**
	 * コネクション表示のための数学計算情報の一時格納エリア
 	 * @since   UDC1.2
	 */
	int ay[] = new int[6];

	/**
	 * デフォルトのストローク 
 	 * @since   UDC1.2
	 */
	BasicStroke	basicstroke = new BasicStroke(1.0f);

	/**
	 * 背景画像等のためのDimesion
 	 * @since   UDC1.22
	 */
	Dimension bsz = new Dimension();

	///**
	// *  描画のための可視矩形 
 	// * @since   UDC1.22
	// */
	//Rectangle visibleRect = new Rectangle();

	/**
	 * カラー・マップ
 	 * @since   UDC1.22
	 */
	Hashtable colorMap = new Hashtable();

	/**
	 * レンダリング・マップ
 	 * @since   UDC1.22
	 */
	Map renderingMap = null;


	/**
	 * コンストラクタ
	 *
 	 * @since   UDC1.2
	 */
	public JTopologyPanel()
	{
		super(null, true);
		__connList = new ArrayList();
		__timer = new java.util.Timer();

		super.addMouseListener(this);
		super.addMouseMotionListener(this);
	}

	/**
	 * 本パネルの初期サイズを保存する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void setBaseSize()
	{
		__baseSize = getSize(new Dimension());

		int cnt = getComponentCount();
		JTopologyNode node;
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				node = (JTopologyNode)getComponent(i);
				node.__parSize = null;
			}
		}
	}

	/**
	 * 本パネルの初期サイズを保存する。
	 *
	 * @param	width	幅
	 * @param	height	高さ
 	 * @since   UDC1.2
	 */
	public synchronized void setBaseSize(int width, int height)
	{
		if (__baseSize == null) { __baseSize = new Dimension(); }
		__baseSize.width = width;
		__baseSize.height = height;

		int cnt = getComponentCount();
		JTopologyNode node;
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				node = (JTopologyNode)getComponent(i);
				node.__parSize = null;
			}
		}
	}

	/**
	 * 本パネルの初期サイズを取得する。
	 *
	 * @return	本パネルの初期サイズ
 	 * @since   UDC1.2
	 */
	public synchronized Dimension getBaseSize() { return __baseSize; }

	/**
	 * レンダリング・マップを設定する
 	 * @since   UDC1.22
	 */
	public void setRenderingMap(Map map)
	{
		renderingMap = map;
	}

	/**
	 * 点滅・アニメーション更新周期時間を取得する。
	 *
	 * @return	点滅周期時間
 	 * @since   UDC1.2
	 */
	public synchronized long	getBlinkTimer() { return __militimer; }

	/**
	 * 点滅・アニメーション更新周期時間を設定する。
	 *
	 * @param	militimer	点滅・アニメーション更新周期時間
 	 * @since   UDC1.2
	 */
	public synchronized void setBlinkTimer(long militimer)
	{
		__militimer = militimer;
		if (__timerTask != null) {
			__timerTask.cancel();	
			//__timer.purge();
			__timerTask = new JTopologyPanel_TimerTask(this);
			__timer.schedule(__timerTask, 0, __militimer);
		}
	}

	/**
	 * 背景のイメージを取得する
	 *
	 * @return	背景のイメージ
 	 * @since   UDC1.2
	 */
	public synchronized Image getImage() { return __backImage; }

	/**
	 * 背景のイメージを設定する
	 *
	 * @param	image	背景のイメージ
 	 * @since   UDC1.2
	 */
	public synchronized void setImage(Image image)
	{
		if (image == null) {
			__backImage = null;
			return;
		}
		__backImage = image;
		__backImageSize = new Dimension(__backImage.getWidth(this),__backImage.getHeight(this));
	}

	/**
	 * 背景のイメージを設定する
	 *
	 * @param	file	背景のイメージファイル
 	 * @since   UDC1.2
	 */
	public synchronized void setImage(String file)
	{
		if (file == null) {
			__backImage = null;
			return;
		}
		//__backImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource(file));
		__backImage = Toolkit.getDefaultToolkit().getImage(file);
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(__backImage, 0);
		tracker.checkAll(true);
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			__backImage=null;
		}
		__backImageSize = new Dimension(__backImage.getWidth(this),__backImage.getHeight(this));
	}

	/**
	 * パネル上のノード・コネクション全てを削除する
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void reset()
	{
		resetConnectionInfo();
		resetNode();
	}

	/**
	 * コネクション点滅色を反転させる
 	 * @since   UDC1.22
	 */
	public synchronized void updateConnectionBlink()
	{
		// 点滅更新
		connBlinkReverse = !connBlinkReverse;
		// アニメーション・カウントの更新 
		connAnimationStep ++;
		if (0 > connAnimationStep) { connAnimationStep = 1; }
		//execBlinkTimer();
	}

	/**
	 * ドラッグ中のノードが存在するか否かを取得する
 	 * @since   UDC1.2
	 */
	public synchronized boolean	isDraggedComponent()
	{
		return (draggedComponent == null) ? false : true;
	}

	/**
	 * パネル上のノード情報のリストを取得する
	 *
	 * @return	ノード情報のリスト
 	 * @since   UDC1.2
	 */
	public synchronized ArrayList getNodeList()
	{
		ArrayList neList = new ArrayList();
		int cnt = getComponentCount();
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				neList.add(getComponent(i));
			}
		}
		return neList;
	}

	/**
	 * パネル上のノード情報を取得する
	 *
	 * @return	ノード情報
	 * @param	name	取得するノード情報のノード名
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode getNode(String name)
	{
		int cnt = getComponentCount();
		JTopologyNode node;
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				node = (JTopologyNode)getComponent(i);
				if (node.getNodeName() != null && name.equals(node.getNodeName())) {
					return (JTopologyNode)getComponent(i);
				}
			}
		}
		return null;
	}

	/**
	 * ノード情報を登録する。同一ノード名の情報は登録できません。
	 *
	 * @return	ノード情報
	 * @param	name	ノード名
	 * @param	type	ノードの表示図形種別
	 * @param	width	ノードの表示図形幅
	 * @param	height	ノードの表示図形高さ
	 * @param	x		ノード情報の表示位置（X座標）
	 * @param	y		ノード情報の表示位置（Y座標）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode addNode(String name, int type, int width, int height, int x, int y)
	{
		if (getNode(name) != null) {
			return null;
		}
		JTopologyNode node = new JTopologyNode(new String(name), type, width, height);
		add(node);
		node.setSize();
		node.setLocation(x,y);
		return node;
	}

	/**
	 * ノード情報を登録する。同一ノード名の情報は登録できません。
	 *
	 * @return	ノード情報
	 * @param	name	ノード名
	 * @param	image	ノードのイメージ図形のイメージ
	 * @param	x		ノード情報の表示位置（X座標）
	 * @param	y		ノード情報の表示位置（Y座標）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode addNode(String name, ImageIcon image, int x, int y)
	{
		if (getNode(name) != null) {
			return null;
		}
		JTopologyNode node = new JTopologyNode(new String(name), image);
		add(node);
		node.setSize();
		node.setLocation(x,y);
		return node;
	}

	/**
	 * ノード情報を登録する。同一ノード名の情報は登録できません。
	 *
	 * @return	ノード情報
	 * @param	name		ノード名
	 * @param	imagefile	ノードのイメージ図形のイメージファイル
	 * @param	x			ノード情報の表示位置（X座標）
	 * @param	y			ノード情報の表示位置（Y座標）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode addNode(String name, String imagefile, int x, int y)
	{
		if (getNode(name) != null) {
			return null;
		}
		JTopologyNode node = new JTopologyNode(new String(name), imagefile);
		add(node);
		node.setSize();
		node.setLocation(x,y);
		return node;
	}

	/**
	 * ノード情報を登録する。同一ノード名の情報は登録できません。
	 *
	 * @return	ノード情報
	 * @param	node		ノード情報
	 * @param	x			ノード情報の表示位置（X座標）
	 * @param	y			ノード情報の表示位置（Y座標）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode addNode(JTopologyNode node, int x, int y)
	{
		if (getNode(node.getNodeName()) != null) {
			return null;
		}
		add(node);
		node.setSize();
		node.setLocation(x,y);
		return node;
	}

	/**
	 * ノード情報を削除する。
	 *
	 * @return	ノード情報
	 * @param	name		ノード名
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyNode delNode(String name)
	{
		JTopologyNode node;
		if ((node=getNode(name)) == null) {
			return null;
		}
		remove(node);

		int cnt = getComponentCount();
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode && name.equals(((JTopologyNode)getComponent(i)).getNodeName())) {
				JTopologyNode rn = (JTopologyNode)getComponent(i);		
				rn.delRelationNodeList(node);
			}
		}
		refreshConnectionInfo();
		return node;
	}

	/**
	 * パネル上のノード情報を全て削除する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void resetNode()
	{
		int cnt = getComponentCount();
		for (int i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				remove(i);
				i --;
				cnt --;
			}
		}
	}

	/**
	 * パネル上のコネクション情報を取得する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo getConnection(String nodeA, String nodeB, int usrtype)
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.isSame(nodeA, nodeB, usrtype)) {
				return conn;
			}
		}
		return null;
	}

	/**
	 * パネル上のコネクション情報を取得する。
	 *
	 * @return	コネクション情報
	 * @param	x	X座標
	 * @param	y	Y座標
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo getConnection(double x, double y)
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(x,y)) { return conn; }
		}
		return null;
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor)
	{
		return addConnection(nodeA,nodeB,usrtype,stroke,lineColor,null,0);
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor)
	{
		return addConnection(nodeA,nodeB,usrtype,stroke,lineColor,blinkColor,0);
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
	 * @param	phase		コネクション表示のずらし幅
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor, int phase)
	{
		return addConnection(nodeA,nodeB,usrtype,stroke,lineColor,blinkColor,phase,false);
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
	 * @param	phase		コネクション表示のずらし幅
	 * @param	toplayer	コネクションをノード情報より上に表示するか否か
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor,
													int phase, boolean toplayer)
	{
		return addConnection(nodeA,nodeB,usrtype,stroke,lineColor,blinkColor,phase,toplayer,JTopologyConnectionInfo.ConnOpeType_None,0);
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
	 * @param	phase		コネクション表示のずらし幅
	 * @param	toplayer	コネクションをノード情報より上に表示するか否か
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @param	opesize		操作種別の表示サイズ(pixel)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor,
													int phase, boolean toplayer, int opetype, int opesize)
	{
		return addConnection(nodeA,nodeB,usrtype,stroke,lineColor,blinkColor,phase,toplayer,opetype,opesize,null);
	}

	/**
	 * コネクション情報を登録する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA		始点ノード
	 * @param	nodeB		終点ノード
	 * @param	usrtype		コネクション種別
	 * @param	stroke		コネクション表示のライン・ストローク
	 * @param	lineColor	コネクション表示のライン色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
	 * @param	phase		コネクション表示のずらし幅
	 * @param	toplayer	コネクションをノード情報より上に表示するか否か
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @param	opesize		操作種別の表示サイズ(pixel)
	 * @param	dispname	コネクションの表示名(\nは改行を意味する。非表示の場合はnullを指定。)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo addConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor,
													int phase, boolean toplayer, int opetype, int opesize,String dispname)
	{
		JTopologyConnectionInfo conn,tmp;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) != null) { return null; } 
		JTopologyNode nodea,nodeb;
		if ((nodea=getNode(nodeA)) == null || (nodeb=getNode(nodeB)) == null) { return null; }
		conn = new JTopologyConnectionInfo(nodea, nodeb, usrtype, stroke, lineColor, phase, toplayer, opetype, dispname);
		conn.opeSize = opesize;
		conn.connBlinkColor = blinkColor;
		conn.setFont(getFont());

		boolean setflag = false;
		int csize = __connList.size();
		if (csize > 0) {
			if (((JTopologyConnectionInfo)__connList.get(csize-1)).connectType > usrtype) {
				for (int i=0; i<csize; i++) {
					tmp = (JTopologyConnectionInfo)__connList.get(i);
					if (tmp.connectType > usrtype) {
						__connList.add(i, conn);
						setflag = true;
						break;
					}
				}
			}
		}
		if (!setflag) { __connList.add(conn); }

		if (blinkColor != null) { execBlinkTimer(); }
		return conn;
	}

	/**
	 * パネル上のコネクション情報を削除する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo delConnection(String nodeA, String nodeB, int usrtype)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 
		__connList.remove(conn);

		if (conn.connBlinkColor != null || conn.connAnimationStep) { execBlinkTimer(); }
		return conn;
	}

	/**
	 * パネル上のコネクション情報(ライン色)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	lineColor	コネクションの表示色
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(String nodeA, String nodeB, int usrtype, Color lineColor)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		conn.connColor = lineColor;
		return conn;
	}

	/**
	 * パネル上のコネクション情報(点滅のためのライン色)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(String nodeA, String nodeB, int usrtype, Color lineColor, Color blinkColor)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		Color save = conn.connBlinkColor;
		conn.connColor = lineColor;
		conn.connBlinkColor = blinkColor;
		if ((save == null && blinkColor != null) || (save != null && blinkColor == null)) { execBlinkTimer(); }
		return conn;
	}

	/**
	 * パネル上のコネクション情報(点滅のためのライン色＋線Stroke)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	lineColor	コネクションの表示色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(
													String nodeA, String nodeB, int usrtype, 
													Stroke stroke, Color lineColor, Color blinkColor)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		Color save = conn.connBlinkColor;
		conn.connColor = lineColor;
		conn.connBlinkColor = blinkColor;
		conn.connStroke = stroke;
		if ((save == null && blinkColor != null) || (save != null && blinkColor == null)) { execBlinkTimer(); }
		return conn;
	}

	/**
	 * パネル上のコネクション情報(ラインの操作種別)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @param	opesize		操作種別の表示サイズ(pixel)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(
													String nodeA, String nodeB, int usrtype, int opetype, int opesize)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		conn.opeType = opetype;
		conn.opeSize = opesize;
		return conn;
	}

	/**
	 * パネル上のコネクション情報(表示名)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	dispname	コネクションの表示名(\nは改行を意味する。非表示の場合はnullを指定。)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(
													String nodeA, String nodeB, int usrtype, String dispname)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		conn.setConnName(dispname);
		return conn;
	}

	/**
	 * パネル上のコネクション情報(全て)を更新する。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	lineColor	コネクションの表示色
	 * @param	blinkColor	コネクション表示の点滅有の場合のライン色（null時は非点滅）
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @param	opesize		操作種別の表示サイズ(pixel)
	 * @param	dispname	コネクションの表示名(\nは改行を意味する。非表示の場合はnullを指定。)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo updateConnection(
													String nodeA, String nodeB, int usrtype,
													Stroke stroke, Color lineColor, Color blinkColor,
													int opetype, int opesize, String dispname)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 
		// 不要なため削除
		//if (getNode(nodeA) == null || getNode(nodeB) == null) {
		//	__connList.remove(conn);
		//	return null;
		//}
		conn.connStroke = stroke;
		conn.connColor = lineColor;
		conn.opeType = opetype;
		conn.opeSize = opesize;
		conn.setConnName(dispname);
		Color save = conn.connBlinkColor;
		conn.connBlinkColor = blinkColor;
		if ((save == null && blinkColor != null) || (save != null && blinkColor == null)) { execBlinkTimer(); }
		return conn;
	}

	/**
	 * パネル上のコネクション情報のアニメーション設定をする。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
	 * @param	animationColor	コネクションのアニメーション表示色
	 * @param	animationSize	コネクションのアニメーション表示幅(pixel)
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo setConnectionAnimation(
															String nodeA, String nodeB, int usrtype,
															Color animationColor, double animationSize)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		if (!conn.connAnimationStep) {
			conn.connAnimationStep = true;
		}
		conn.connAnimationColor = animationColor;
		conn.connAnimationSize = animationSize;
		execBlinkTimer();
		return conn;
	}

	/**
	 * パネル上のコネクション情報のアニメーション設定をする。
	 *
	 * @return	コネクション情報
	 * @param	nodeA	始点ノード
	 * @param	nodeB	終点ノード
	 * @param	usrtype	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized JTopologyConnectionInfo clearConnectionAnimation(String nodeA, String nodeB, int usrtype)
	{
		JTopologyConnectionInfo conn;
		if ((conn=getConnection(nodeA, nodeB, usrtype)) == null) { return null; } 

		conn.connAnimationStep = false;
		conn.connAnimationColor = null;
		conn.connAnimationSize = 0.0;
		conn.animationShape = null;
		execBlinkTimer();
		return conn;
	}


	/**
	 * パネル上のコネクション情報を全て取得する。
	 *
	 * @return コネクションリスト
 	 * @since   UDC1.2
	 */
	public synchronized ArrayList getConnectionList()
	{
		return __connList;
	}

	/**
	 * パネル上の指定コネクション種別より大きいコネクション情報を全て取得する。
	 *
	 * @return コネクションリスト
	 * @param	usrType	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized ArrayList getConnectionListGE(int usrType)
	{
		ArrayList clist = new ArrayList();
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connectType < usrType) { continue; }
			clist.add(conn);		
		}
		return clist;
	}

	/**
	 * パネル上の指定コネクション種別より小さいコネクション情報を全て取得する。
	 *
	 * @return コネクションリスト
	 * @param	usrType	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized ArrayList getConnectionListLE(int usrType)
	{
		ArrayList clist = new ArrayList();
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connectType > usrType) { continue; }
			clist.add(conn);		
		}
		return clist;
	}

	/**
	 * パネル上のコネクション情報を全て削除する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void resetConnectionInfo()
	{
		__connList.clear();
	}

	/**
	 * パネル上の指定コネクション種別のコネクション情報を全て削除する。
	 *
	 * @param	usrType	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized void resetConnectionInfo(int usrType)
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connectType != usrType) { continue; }
			__connList.remove(conn);
			i --;
			csize--;
		}
	}

	/**
	 * パネル上の指定コネクション種別より大きいコネクション情報を全て削除する。
	 *
	 * @param	usrType	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized void resetConnectionInfoGE(int usrType)
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connectType < usrType) { continue; }
			__connList.remove(conn);
			csize --;
			i --;
		}
	}

	/**
	 * パネル上の指定コネクション種別より小さいコネクション情報を全て削除する。
	 *
	 * @param	usrType	コネクション種別
 	 * @since   UDC1.2
	 */
	public synchronized void resetConnectionInfoLE(int usrType)
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connectType > usrType) { continue; }
			__connList.remove(conn);
			csize --;
			i --;
		}
	}

	/**
	 * 始点・終点ノードが存在しないコネクション情報を全て削除する。
	 *
 	 * @since   UDC1.2
	 */
	public synchronized void refreshConnectionInfo()
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.getConnectNodeFrom().getParent() == null || conn.getConnectNodeTo().getParent() == null) {
				__connList.remove(conn);
				csize --;
				i --;	
			}
		}
	}

	/**
	 * 点滅状態のコネクションの存在有無を取得する。
	 *
	 * @return	点滅状態のコネクションの存在有無
 	 * @since   UDC1.2
	 */
	private synchronized boolean isBlink()
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connBlinkColor != null) { return true; }
		}
		return false;	
	}

	/**
	 * アニメーション状態のコネクションの存在有無を取得する。
	 *
	 * @return	アニメーション状態のコネクションの存在有無
 	 * @since   UDC1.2
	 */
	private synchronized boolean isAnimation()
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connAnimationStep) { return true; }
		}
		return false;	
	}

	/**
	 * 点滅状態・アニメーション状態のコネクションの存在有無を取得する。
	 *
	 * @return	点滅状態・アニメーション状態のコネクションの存在有無
 	 * @since   UDC1.2
	 */
	private synchronized boolean isBlinkOrAnimation()
	{
		JTopologyConnectionInfo conn;
		int csize = __connList.size();
		for (int i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.connBlinkColor != null || conn.connAnimationStep) { return true; }
		}
		return false;	
	}

	/**
	 * 点滅状態・アニメーション状態のコネクションが存在すれば、点滅周期を再設定する。
	 * 点滅状態・アニメーション状態のコネクションが存在しなければ、点滅周期処理を停止する。
	 *
 	 * @since   UDC1.2
	 */
	private synchronized void execBlinkTimer()
	{
		if (isBlinkOrAnimation()) {
			if (__timerTask == null) {
				__timerTask = new JTopologyPanel_TimerTask(this);
				__timer.schedule(__timerTask, 0, __militimer);
			}
		} else {
			if (__timerTask != null) {
				__timerTask.cancel();
				//__timer.purge();
				__timerTask = null;
			}
			connBlinkReverse = false;
			connAnimationStep = 0;
		}
	}

	/**
	 * コネクション情報を描画する。toplayerで指定したコネクションのみを表示する。
	 *
	 * @param	gr			グラフィックス
	 * @param	toplayer	ノード情報より前面に表示するか否か	
 	 * @since   UDC1.2
	 */
	private synchronized void paintConnection(Graphics gr, boolean toplayer)
	{
		/* connection(s) is painted. */
		Graphics2D g = (Graphics2D)gr;
		AffineTransform srcform = g.getTransform();

		double sx,sy,ex,ey,dx,dy,xphase,yphase,alen;
		Stroke tstroke, baseStroke = g.getStroke();
		Color  tcolor, tdarker, tbrighter, baseColor = g.getColor();
		Color  backColor = g.getBackground();
		ArrayList reclr;

		//refreshConnectionInfo();
		//execBlinkTimer();

		int i,j,anum;
		boolean change;
		JTopologyNode nodea,nodeb;
		JTopologyConnectionInfo conn;
		int steptiming = connAnimationStep % 4;
		int csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.topLayer != toplayer) { continue; }

			// 前処理
			nodea = conn.getConnectNodeFrom();	
			if (!nodea.paintStore) {
				nodea.cx = (double)nodea.getCenterX(g);
				nodea.cy = (double)nodea.getCenterY(g);
				nodea.paintStore = true;
			}
			sx = nodea.cx;
			sy = nodea.cy;

			nodeb = conn.getConnectNodeTo();
			if (!nodeb.paintStore) {
				nodeb.cx = (double)nodeb.getCenterX(g);
				nodeb.cy = (double)nodeb.getCenterY(g);
				nodeb.paintStore = true;
			} 
			ex = nodeb.cx;
			ey = nodeb.cy;

			// 線情報に変化があったかチェック
			change = false;
			if (conn.sx != sx || conn.sy != sy || conn.ex != ex || conn.ey != ey) {
				change = true;
				conn.sx = sx;
				conn.sy = sy;
				conn.ex = ex;
				conn.ey = ey;
				// ２点間の極座標の算出
				dx = ex - sx;
				dy = ey - sy;
				conn.theta = Math.atan2(dy, dx);
				conn.cosTheta = Math.cos(conn.theta);
				conn.sinTheta = Math.sin(conn.theta);
			}

			// ずらし座標算出
			// (変化があった場合)
			if (change || conn.phase != conn.connPhase) {
				if (conn.connPhase != 0) {
					double rth, dt, sin_rth, cos_rth;
					// 回転前座標の算出
					xphase = (double)conn.connPhase * conn.cosTheta;	
					yphase = (double)conn.connPhase * conn.sinTheta;	
					// 90度(左右)回転
					dt = Math.min((Math.abs(conn.connPhase)/180.0), 0.5); // dt = Math.min((Math.abs(conn.connPhase)/90.0/2.0), 0.5);
					//rth = th00-90;
					rth = Math.PI * dt;		// 0-90(dt=0.0〜0.5)の範囲
					sin_rth = Math.sin(rth);
					cos_rth = Math.cos(rth);
					sx += xphase*cos_rth - yphase*sin_rth;
					sy += xphase*sin_rth + yphase*cos_rth;
					//rth = th90-180;
					rth = Math.PI * (1.0 - dt);	// 90-180(dt=0.5-1.0)の範囲
					sin_rth = Math.sin(rth);
					cos_rth = Math.cos(rth);
					ex += xphase*cos_rth - yphase*sin_rth;
					ey += xphase*sin_rth + yphase*cos_rth;
				}
				change = true;
				conn.phase = conn.connPhase;
				conn.tsx = sx;
				conn.tsy = sy;
				conn.tex = ex;
				conn.tey = ey;
			// (変化がない場合)
			} else {
				sx = conn.tsx;
				sy = conn.tsy;
				ex = conn.tex;
				ey = conn.tey;
			}

			// 点滅対応の blinkColorの設定
			tstroke = (conn.connStroke != null) ? conn.connStroke : baseStroke;
			tcolor = (conn.connColor != null) ? conn.connColor : baseColor;
			if (conn.connBlinkColor != null) {
				if (connBlinkReverse) { tcolor = conn.connBlinkColor; }
			}
			g.setStroke(tstroke);
			g.setColor(tcolor);
				// Color取得
			if ((reclr=(ArrayList)colorMap.get(tcolor)) == null) {
				reclr = new ArrayList();
				reclr.add(tcolor);
				reclr.add(tcolor.darker());
				reclr.add(tcolor.brighter());
				colorMap.put(tcolor, reclr);
			}

			// コネクションの線を表示
			// (ずらしがある場合)
			if (conn.connPhase != 0) {
				ax[0] = (int)conn.sx;	ay[0] = (int)conn.sy;
				ax[1] = (int)sx;		ay[1] = (int)sy;
				ax[2] = (int)ex;		ay[2] = (int)ey;
				ax[3] = (int)conn.ex;	ay[3] = (int)conn.ey;
				anum = 4;
			// (ずらしがない場合)
			} else {
				ax[0] = (int)sx;		ay[0] = (int)sy;
				ax[1] = (int)ex;		ay[1] = (int)ey;
				anum = 2;
			}
			g.drawPolyline(ax, ay, anum);

			// 矢印Shape算出(前回から変化があれば)
			if (change || conn.opeType != conn.arrowtype || conn.opeSize != conn.arrowLen) {
				conn.arrowtype = conn.opeType;
				conn.arrowLen = conn.opeSize;
				ax[0] = (int)(sx + (ex - sx) / 2.0);
				ay[0] = (int)(sy + (ey - sy) / 2.0);
				if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_UniDirArrow) {
					xphase = conn.opeSize * conn.cosTheta;
					yphase = conn.opeSize * conn.sinTheta;
					conn.opeShape[0].xpoints[0] = (int)(ax[0] + (xphase/2.0));
					conn.opeShape[0].ypoints[0] = (int)(ay[0] + (yphase/2.0));
					conn.opeShape[0].xpoints[1] = (int)(conn.opeShape[0].xpoints[0] + (xphase*cos150 - yphase*sin150));
					conn.opeShape[0].ypoints[1] = (int)(conn.opeShape[0].ypoints[0] + (xphase*sin150 + yphase*cos150));
					conn.opeShape[0].xpoints[2] = (int)(conn.opeShape[0].xpoints[0] + (xphase*cos210 - yphase*sin210));	
					conn.opeShape[0].ypoints[2] = (int)(conn.opeShape[0].ypoints[0] + (xphase*sin210 + yphase*cos210));
					conn.opeShape[0].npoints = 3;
					conn.opeShape[0].invalidate();
				} else if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_BiDirArrow) {
					xphase = conn.opeSize * conn.cosTheta;
					yphase = conn.opeSize * conn.sinTheta;
					conn.opeShape[0].xpoints[0] = (int)(ax[0] + (conn.opeSize+1)*conn.cosTheta);
					conn.opeShape[0].ypoints[0] = (int)(ay[0] + (conn.opeSize+1)*conn.sinTheta);
					conn.opeShape[0].xpoints[1] = (int)(conn.opeShape[0].xpoints[0] + (xphase*cos150 - yphase*sin150));
					conn.opeShape[0].ypoints[1] = (int)(conn.opeShape[0].ypoints[0] + (xphase*sin150 + yphase*cos150));
					conn.opeShape[0].xpoints[2] = (int)(conn.opeShape[0].xpoints[0] + (xphase*cos210 - yphase*sin210));	
					conn.opeShape[0].ypoints[2] = (int)(conn.opeShape[0].ypoints[0] + (xphase*sin210 + yphase*cos210));
					conn.opeShape[0].npoints = 3;
					conn.opeShape[0].invalidate();
					conn.opeShape[1].xpoints[0] = (int)(ax[0] - (conn.opeSize+1)*conn.cosTheta);
					conn.opeShape[1].ypoints[0] = (int)(ay[0] - (conn.opeSize+1)*conn.sinTheta);
					conn.opeShape[1].xpoints[1] = (int)(conn.opeShape[1].xpoints[0] + (xphase*cos30 - yphase*sin30));
					conn.opeShape[1].ypoints[1] = (int)(conn.opeShape[1].ypoints[0] + (xphase*sin30 + yphase*cos30));
					conn.opeShape[1].xpoints[2] = (int)(conn.opeShape[1].xpoints[0] + (xphase*cos330 - yphase*sin330));	
					conn.opeShape[1].ypoints[2] = (int)(conn.opeShape[1].ypoints[0] + (xphase*sin330 + yphase*cos330));
					conn.opeShape[1].npoints = 3;
					conn.opeShape[1].invalidate();
				} else if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_Normal) {
					xphase = conn.opeSize * conn.cosTheta;
					yphase = conn.opeSize * conn.sinTheta;
					conn.opeShape[0].xpoints[0] = (int)(ax[0] + (xphase*cos45 - yphase*sin45));
					conn.opeShape[0].ypoints[0] = (int)(ay[0] + (xphase*sin45 + yphase*cos45));
					conn.opeShape[0].xpoints[1] = (int)(ax[0] + (xphase*cos135 - yphase*sin135));
					conn.opeShape[0].ypoints[1] = (int)(ay[0] + (xphase*sin135 + yphase*cos135));
					conn.opeShape[0].xpoints[2] = (int)(ax[0] + (xphase*cos225 - yphase*sin225));
					conn.opeShape[0].ypoints[2] = (int)(ay[0] + (xphase*sin225 + yphase*cos225));
					conn.opeShape[0].xpoints[3] = (int)(ax[0] + (xphase*cos315 - yphase*sin315));
					conn.opeShape[0].ypoints[3] = (int)(ay[0] + (xphase*sin315 + yphase*cos315));
					conn.opeShape[0].npoints = 4;
					conn.opeShape[0].invalidate();
				}
			}

			// アニメーションは、線の上に表示
			if (conn.connAnimationStep) {
				alen = (conn.connAnimationSize > 0) ? conn.connAnimationSize : ((BasicStroke)tstroke).getLineWidth() * 3;
				//if (alen < markLen) { alen = markLen; }
				Color animationColor = (conn.connAnimationColor != null) ? conn.connAnimationColor : backColor;
				tdarker = (Color)reclr.get(1);
				g.setStroke(baseStroke);
				// (変化があった場合)
				if (change || conn.animationShape == null || alen != conn.animationLen) {
					conn.animationLen = alen;
					dx = ex - sx;
					dy = ey - sy;
					double asize = alen * 2.0;
					anum = (Math.abs(conn.sinTheta) > Math.abs(conn.cosTheta)) ?  (int)((dy/conn.sinTheta) / asize) : (int)((dx/conn.cosTheta) / asize);
					if (anum <= 0) { anum = 1; }
					xphase = alen * conn.cosTheta;
					yphase = alen * conn.sinTheta;
					double axdur = dx / anum;
					double aydur = dy / anum;
					double hoseix = sx, hoseiy = sy;
					if (anum >= 2) { 
						hoseix += xphase;
						hoseiy += yphase; 
						anum --;
					}
					double cossinU, sincosU, cossinD, sincosD, tmp2x, tmp2y, tmp3x, tmp3y, ax0, ay0;
					cossinU = xphase*cos150 - yphase*sin150;
					sincosU = xphase*sin150 + yphase*cos150;
					cossinD = xphase*cos210 - yphase*sin210;
					sincosD = xphase*sin210 + yphase*cos210;
					if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_UniDirArrow) { 
						tmp2x = cossinU - xphase;
						tmp2y = sincosU - yphase;
						tmp3x = cossinD - xphase;
						tmp3y = sincosD - yphase;
					} else {
						tmp2x = cossinU - xphase*1.5;
						tmp2y = sincosU - yphase*1.5;
						tmp3x = cossinD - xphase*1.5;
						tmp3y = sincosD - yphase*1.5;
					}
					int pnum;
					conn.animationShape = null;
					conn.animationShape = new Polygon[anum];
					for (j=0; j<anum; j++) {
						ax0 = hoseix + axdur * (j+1);
						ay0 = hoseiy + aydur * (j+1);
						if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_UniDirArrow) {
							ax[0] = (int)ax0;
							ay[0] = (int)ay0;
							ax[1] = (int)(ax0 + cossinU);
							ay[1] = (int)(ay0 + sincosU);
							ax[4] = (int)(ax0 + cossinD);	
							ay[4] = (int)(ay0 + sincosD);
							ax[2] = (int)(ax0 + tmp2x);
							ay[2] = (int)(ay0 + tmp2y);
							ax[3] = (int)(ax0 + tmp3x);
							ay[3] = (int)(ay0 + tmp3y);
							pnum = 5;
						} else {
							ax[0] = (int)(ax0 + cossinU);
							ay[0] = (int)(ay0 + sincosU);
							ax[3] = (int)(ax0 + cossinD);	
							ay[3] = (int)(ay0 + sincosD);
							ax[1] = (int)(ax0 + tmp2x);
							ay[1] = (int)(ay0 + tmp2y);
							ax[2] = (int)(ax0 + tmp3x);
							ay[2] = (int)(ay0 + tmp3y);
							pnum = 4;
						}
						conn.animationShape[j] = new Polygon(ax, ay, pnum);
						conn.animationShape[j].invalidate();
						g.setColor( ((steptiming == (j%4)) ? animationColor : tcolor) );
						g.fillPolygon(ax,ay,pnum);
						g.setColor(tdarker);
						g.drawPolygon(ax,ay,pnum);
					}
				// (変化がない場合)
				} else {
					for (j=0; j<conn.animationShape.length; j++) {
						g.setColor( ((steptiming == (j%4)) ? animationColor : tcolor) );
						g.fillPolygon(conn.animationShape[j].xpoints, conn.animationShape[j].ypoints, conn.animationShape[j].npoints);
						g.setColor(tdarker);
						g.drawPolygon(conn.animationShape[j].xpoints, conn.animationShape[j].ypoints, conn.animationShape[j].npoints);
					}
				}
			}

			// 操作Shape(矢印)は全ての上に表示するため、線の上に表示
			if (conn.opeType != JTopologyConnectionInfo.ConnOpeType_None) {
				tdarker = (Color)reclr.get(1);
				tbrighter = (Color)reclr.get(2);
				g.setStroke(basicstroke);
				if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_UniDirArrow) {
					g.setColor(tdarker);
					g.fillPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
					g.setColor(tbrighter);
					g.drawPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
				} else if (conn.opeType == JTopologyConnectionInfo.ConnOpeType_BiDirArrow) {
					g.setColor(tdarker);
					g.fillPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
					g.fillPolygon(conn.opeShape[1].xpoints, conn.opeShape[1].ypoints, conn.opeShape[1].npoints);
					g.setColor(tbrighter);
					g.drawPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
					g.drawPolygon(conn.opeShape[1].xpoints, conn.opeShape[1].ypoints, conn.opeShape[1].npoints);
				} else {
					g.setColor(tdarker);
					g.fillPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
					g.setColor(tbrighter);
					g.drawPolygon(conn.opeShape[0].xpoints, conn.opeShape[0].ypoints, conn.opeShape[0].npoints);
				}
			}
		}

		// コネクション名を表示(全ての上に表示)
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.topLayer != toplayer) { continue; }
			if (conn.connName == null || conn.connName.length() <= 0) { continue; }
			// カラー設定
			tstroke = (conn.connStroke != null) ? conn.connStroke : baseStroke;
			tcolor = (conn.connColor != null) ? conn.connColor : baseColor;
			if (conn.connBlinkColor != null) {
				if (connBlinkReverse) { tcolor = conn.connBlinkColor; }
			}
			// コネクション名処理
			Rectangle2D nameRect = conn.getConnNameLength(g);
			if (conn.opeType != JTopologyConnectionInfo.ConnOpeType_None) {
				alen = conn.arrowLen + nameRect.getHeight();
			} else {
				alen = ((BasicStroke)tstroke).getLineWidth() * 1.5 + nameRect.getHeight();
			}
			xphase = alen * conn.cosTheta;	
			yphase = alen * conn.sinTheta;	
			double xbase, ybase;
			if (conn.connPhase != 0) {
				xbase = conn.tsx + (conn.tex - conn.tsx) / 2.0;
				ybase = conn.tsy + (conn.tey - conn.tsy) / 2.0;
			} else {
				xbase = conn.tsx + (conn.ex - conn.sx) / 2.0;
				ybase = conn.tsy + (conn.ey - conn.sy) / 2.0;
			}
			ax[0] = (int)(xbase + (xphase*cos135 - yphase*sin135));
			ay[0] = (int)(ybase + (xphase*sin135 + yphase*cos135));
			g.transform(AffineTransform.getRotateInstance(conn.theta, ax[0], ay[0]));
			g.setColor(tcolor);
			// 複数行対応
			//g.drawString(conn.connName, ax[0], ay[0]);
			String cnames[] = conn.connName.split("\\n");
			for (j=0; j<cnames.length; j++) {
				if (j != 0) { ay[0] += nameRect.getHeight(); }
				g.drawString(cnames[j], ax[0], ay[0]);
			}
			//--
			g.setTransform(srcform);
		}
		g.setStroke(baseStroke);
	}

	/**
	 * paintChildrenのオーバライド関数。<br>
	 * ノード情報の上に表示するコネクション情報を描画する。
	 *
	 * @param	gr		グラフィックス
 	 * @since   UDC1.2
	 */
	protected synchronized void paintChildren(Graphics gr)
	{
		/* when here painted in super-class-paintChildren, node(s) is painted. */
		super.paintChildren(gr);

		/* connection(s) is painted. */
		paintConnection(gr, true);
	}

	/**
	 * paintComponentのオーバライド関数。<br>
	 * ノード情報および、ノード情報の下に表示するコネクション情報を描画する。
	 * また、それ以外のパネル上に登録してある子コンポーネントを描画する。
	 *
	 * @param	gr		グラフィックス
 	 * @since   UDC1.2
	 */
	protected synchronized void paintComponent(Graphics gr)
	{
		int i,j,cnt;
		Graphics2D g = (Graphics2D)gr;
		if (__baseSize == null) {
			setBaseSize();
		}
		/* 高速化のためレンダリング設定 */
		if (renderingMap != null) { g.setRenderingHints(renderingMap); }
		/* 可視矩形の設定 */
		//computeVisibleRect(visibleRect);	
		//g.setClip(visibleRect);

		/* ScrollPaneが親の場合、表示されていない領域のノードのpaintComponentがcallされない為、ノードは強制的に位置計算する */
		JTopologyNode node;
		cnt = getComponentCount();
		for (i=0; i<cnt; i++) {
			if (getComponent(i) instanceof JTopologyNode) {
				node = (JTopologyNode)getComponent(i);	
				node.paintComponent(gr);
				node.paintStore = false;
			}
		}

		/* when here painted in super-class-paintComponent. */
		super.paintComponent(gr);

		/* set background map */
		if (__backImage != null) {
			getSize(bsz);
			gr.drawImage(__backImage,0,0,bsz.width,bsz.height,this);
			
			//if (bsz.width > __backImageSize.width  || bsz.height > __backImageSize.height) {
			//	gr.drawImage(__backImage,0,0,bsz.width,bsz.height,this);
			//} else {
			//	int x = (bsz.width - __backImageSize.width) / 2;
			//	int y = (bsz.height - __backImageSize.height) / 2;
			//	gr.drawImage(__backImage, x, y, this);
			//}
		}

		/* connection(s) is painted. */
		paintConnection(gr, false);
	}

	/**
	 * MouseListener.mouseReleasedのイベントハンドリング関数。<br>
	 * ノード情報のドラッグ・ドロップのための処理として実装される。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseReleased(MouseEvent e)
	{
		if (draggedComponent != null) {
			draggedComponent = null;
			repaint();
		}

		Point pnt = e.getPoint();
		JComponent component = (JComponent)getComponentAt(pnt);
		if (component != null && component != this) {
			if (component instanceof MouseListener) {
				MouseEvent ev = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
											e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)component).mouseReleased(ev);
			}
			/* JTopologyNode */
			if (component instanceof JTopologyNode) {
				JTopologyNode node = (JTopologyNode)component;
				if (node.getUserMouseListener() != null) {
					MouseEvent ev = new MouseEvent(node, e.getID(), e.getWhen(), e.getModifiers(),
											e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
					node.getUserMouseListener().mouseReleased(ev);
				}
			}
		}

		/* コネクションのMouseListenerを検索 */
		JTopologyConnectionInfo conn;
		ArrayList mclist = new ArrayList();
		int i, j, asize, csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getMouseListenerList().size() <= 0) { continue; }
				mclist.add(conn);
			}
		}

		/* イベント転送(イベント処理中に__connListを変更されてしまう可能性があるため、上記で対象を確定して別のリストで処理要求を実施) */
		ArrayList alist;
		csize = mclist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)mclist.get(i);
			alist = conn.getMouseListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				MouseEvent ev = new MouseEvent(conn, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)alist.get(j)).mouseReleased(ev);
			}
		}
	}

	/**
	 * MouseListener.mouseClickedのイベントハンドリング関数。<br>
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseClicked(MouseEvent e)
	{
		int i,j;
		JTopologyConnectionInfo conn;

		Point pnt = e.getPoint();
		JComponent component = (JComponent)getComponentAt(pnt);
		if (component != null && component != this) {
			if (component instanceof MouseListener) {
				MouseEvent ev = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
											e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)component).mouseClicked(ev);
			}
			/* JTopologyNode */
			if (component instanceof JTopologyNode) {
				JTopologyNode node = (JTopologyNode)component;
				if (node.getUserMouseListener() != null) {
					MouseEvent ev = new MouseEvent(node, e.getID(), e.getWhen(), e.getModifiers(),
											e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
					node.getUserMouseListener().mouseClicked(ev);
				}
			}
		}

		/* コネクションのMouseListenerを検索 */
		ArrayList mclist = new ArrayList();
		int asize,csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getMouseListenerList().size() <= 0) { continue; }
				mclist.add(conn);
			}
		}

		/* コネクションのActionListenerを検索 */
		ArrayList aplist = new ArrayList();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getActionListenerList().size() <= 0) { continue; }
				aplist.add(conn);
			}
		}

		/* イベント転送(イベント処理中に__connListを変更されてしまう可能性があるため、上記で対象を確定して別のリストで処理要求を実施) */
		ArrayList alist;
		csize = mclist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)mclist.get(i);
			alist = conn.getMouseListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				MouseEvent ev = new MouseEvent(conn, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)alist.get(j)).mouseClicked(ev);
			}
		}
		csize = aplist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)aplist.get(i);
			alist = conn.getActionListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				ActionListener al = (ActionListener)alist.get(j);
				ActionEvent ae = new ActionEvent(conn, e.getID(), null);
				al.actionPerformed(ae);
			}
		}
	}

	/**
	 * MouseListener.mousePressedのイベントハンドリング関数。<br>
	 * 本イベントは何も処理しない。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mousePressed(MouseEvent e)
 	{
		Point pnt = e.getPoint();
		JComponent component = (JComponent)getComponentAt(pnt);
		if (component != null && component != this) {
			if (component instanceof MouseListener) {
				MouseEvent ev = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)component).mousePressed(ev);
			}
			/* JTopologyNode */
			if (component instanceof JTopologyNode) {
				JTopologyNode node = (JTopologyNode)component;
				if (node.getUserMouseListener() != null) {
					MouseEvent ev = new MouseEvent(node, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
					node.getUserMouseListener().mousePressed(ev);
				}
			}
		}

		/* コネクションのMouseListenerを検索 */
		JTopologyConnectionInfo conn;
		ArrayList mclist = new ArrayList();
		int i,j,asize,csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getMouseListenerList().size() <= 0) { continue; }
				mclist.add(conn);
			}
		}

		/* イベント転送(イベント処理中に__connListを変更されてしまう可能性があるため、上記で対象を確定して別のリストで処理要求を実施) */
		ArrayList alist;
		csize = mclist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)mclist.get(i);
			alist = conn.getMouseListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				MouseEvent ev = new MouseEvent(conn, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)alist.get(j)).mousePressed(ev);
			}
		}
	}

	/**
	 * MouseListener.mouseEnteredのイベントハンドリング関数。<br>
	 * 本イベントは何も処理しない。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseEntered(MouseEvent e)
	{
		Point pnt = e.getPoint();
		JComponent component = (JComponent)getComponentAt(pnt);
		if (component != null && component != this) {
			if (component instanceof MouseListener) {
				MouseEvent ev = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)component).mouseEntered(ev);
			}
			/* JTopologyNode */
			if (component instanceof JTopologyNode) {
				JTopologyNode node = (JTopologyNode)component;
				if (node.getUserMouseListener() != null) {
					MouseEvent ev = new MouseEvent(node, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
					node.getUserMouseListener().mouseEntered(ev);
				}
			}
		}

		/* コネクションのMouseListenerを検索 */
		JTopologyConnectionInfo conn;
		ArrayList mclist = new ArrayList();
		int i,j,asize,csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getMouseListenerList().size() <= 0) { continue; }
				mclist.add(conn);
			}
		}

		/* イベント転送(イベント処理中に__connListを変更されてしまう可能性があるため、上記で対象を確定して別のリストで処理要求を実施) */
		ArrayList alist;
		csize = mclist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)mclist.get(i);
			alist = conn.getMouseListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				MouseEvent ev = new MouseEvent(conn, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)alist.get(j)).mouseEntered(ev);
			}
		}
	}

	/**
	 * MouseListener.mouseExitedのイベントハンドリング関数。<br>
	 * 本イベントは何も処理しない。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseExited(MouseEvent e)
	{
		Point pnt = e.getPoint();
		JComponent component = (JComponent)getComponentAt(pnt);
		if (component != null && component != this) {
			if (component instanceof MouseListener) {
				MouseEvent ev = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)component).mouseExited(ev);
			}
			/* JTopologyNode */
			if (component instanceof JTopologyNode) {
				JTopologyNode node = (JTopologyNode)component;
				if (node.getUserMouseListener() != null) {
					MouseEvent ev = new MouseEvent(node, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
					node.getUserMouseListener().mouseExited(ev);
				}
			}
		}

		/* コネクションのMouseListenerを検索 */
		JTopologyConnectionInfo conn;
		ArrayList mclist = new ArrayList();
		int i, j, asize, csize = __connList.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)__connList.get(i);
			if (conn.contain(pnt.getX(), pnt.getY())) {
				if (conn.getMouseListenerList().size() <= 0) { continue; }
				mclist.add(conn);
			}
		}

		/* イベント転送(イベント処理中に__connListを変更されてしまう可能性があるため、上記で対象を確定して別のリストで処理要求を実施) */
		ArrayList alist;
		csize = mclist.size();
		for (i=0; i<csize; i++) {
			conn = (JTopologyConnectionInfo)mclist.get(i);
			alist = conn.getMouseListenerList();
			if ((asize=alist.size()) <= 0) { continue; }
			for (j=0; j<asize; j++) {
				MouseEvent ev = new MouseEvent(conn, e.getID(), e.getWhen(), e.getModifiers(),
										e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton()); 
				((MouseListener)alist.get(j)).mouseExited(ev);
			}
		}
	}

	/**
	 * MouseMoutionListener.mouseDraggedのイベントハンドリング関数。<br>
	 * ノード情報のドラッグ・ドロップのための処理として実装される。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseDragged(MouseEvent e) 	
	{
		JComponent component;
		Point pnt = e.getPoint();

		/* initial dragged component */
		if (draggedComponent == null) {
			if ((component=(JComponent)getComponentAt(pnt)) == null || !(component instanceof JTopologyNode)) {
				return;
			}
			draggedComponent = component;
			return;
		}
		/* move on dragged */
		draggedComponent.setLocation(pnt);
		JTopologyNode target = (JTopologyNode)draggedComponent;
		double xdif = pnt.getX() - target.__oldx;
		double ydif = pnt.getY() - target.__oldy;
		ArrayList rlist = target.getRelationNodeList();
		int rsize = rlist.size();
		for (int i=0; i<rsize; i++) {
			JTopologyNode rn = (JTopologyNode)rlist.get(i);
			Point rp = rn.getLocation();
			rp.setLocation(rp.getX()+xdif, rp.getY()+ydif);
			rn.setLocation(rp);
		}
		repaint();
	}

	/**
	 * MouseMoutionListener.mouseMovedのイベントハンドリング関数。<br>
	 * 本イベントは何も処理しない。
	 *
	 * @param	e	マウスイベント
 	 * @since   UDC1.2
	 */
	public void mouseMoved(MouseEvent e) {}	
}


/**
 * コネクション情報の点滅/フロー処理を実現するための周期処理クラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
class JTopologyPanel_TimerTask extends TimerTask
{
	/**
	 * トポロジー・パネルクラス
	 * @since   UDC1.2
	 */
	JTopologyPanel topologypanel;

	/**
	 * 輻輳時の処理休止情報
	 * @since   UDC1.2
	 */
	static Boolean 	lockTarget = new Boolean(false);

	/**
	 * コンストラクタ
	 *
	 * @param	panel	トポロジー・パネル
	 * @since   UDC1.2
	 */
	public JTopologyPanel_TimerTask(JTopologyPanel panel)
	{
		topologypanel = panel;	
	}

	/**
	 * スレッド周期処理関数
	 *
	 * @since   UDC1.2
	 */
	public void run()
	{
		EventQueue evq = Toolkit.getDefaultToolkit().getSystemEventQueue();
		synchronized (lockTarget) {
			if (evq.peekEvent() == null) {
				EventQueue.invokeLater(new InvokeTimer(topologypanel));
			}
		}
	}

	/**
	 * コネクション情報の点滅/フロー処理を実現するための周期処理の同期起動クラス。
	 *
	 * @author  Takayuki Uchida
	 * @version 1.2, 10 Mar 2006
	 * @since   UDC1.2
	 */
	class InvokeTimer implements Runnable
	{
		/**
		 * トポロジー・パネルクラス
		 * @since   UDC1.2
		 */
		private JTopologyPanel topologypanel;

		/**
		 * コンストラクタ
		 * @param	type		処理種別
		 * @param	topology	トポロジーパネル
		 * @param	cnt			処理カウンタ
		 */
		public InvokeTimer(JTopologyPanel panel)
		{
			topologypanel = panel;
		}

		/**
		 * スレッド周期処理同期起動関数
		 *
		 * @since   UDC1.2
		 */
		public void  run()
		{
			// 点滅・アニメーション 
			topologypanel.updateConnectionBlink();
			topologypanel.repaint();
		}

	}	
}

