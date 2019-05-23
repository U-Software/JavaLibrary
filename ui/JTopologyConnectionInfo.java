/* *********************************************************************
 * @(#)JTopologyConnectionInfo.java 1.2, 10 Mar 2006
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
 * ネットワーク・トポロジーのノード間のライン情報。本コンポーネントはSwingコンポーネントではなく
 * JTopologyPanel上のライン描画のための情報です。本クラスはJTopologyPanelクラスの内部情報クラスと
 * して使用されるため、ユーザは使用すべきではありません。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JTopologyConnectionInfo extends JComponent
{
	/**
	 * ライン操作種別。なし。 
 	 * @since   UDC1.2
	 */
	public final static int		ConnOpeType_None = 0;

	/**
	 * ライン操作種別。デフォルト。 
 	 * @since   UDC1.2
	 */
	public final static int		ConnOpeType_Normal = 1;

	/**
	 * ライン操作種別。片方向矢印。 !Z!V
 	 * @since   UDC1.2
	 */
	public final static int		ConnOpeType_UniDirArrow = 2;

	/**
	 * ライン操作種別。 双方向矢印。
 	 * @since   UDC1.2
	 */
	public final static int		ConnOpeType_BiDirArrow = 3;

	/**
	 * ラインの始点ノード情報
 	 * @since   UDC1.2
	 */
	protected JTopologyNode 	connectNodeFrom;

	/**
	 * ラインの終点ノード情報
 	 * @since   UDC1.2
	 */
	protected JTopologyNode 	connectNodeTo;

	/**
	 * ラインの種別。ライン種別は大きい値のものがより前面に表示される。
 	 * @since   UDC1.2
	 */
	protected int	connectType;

	/**
	 * ラインのストローク
 	 * @since   UDC1.2
	 */
	protected Stroke	connStroke;
	
	/**
	 * ラインの色
 	 * @since   UDC1.2
	 */
	protected Color	connColor;

	/**
	 * ラインの点滅色
 	 * @since   UDC1.2
	 */
	protected Color	connBlinkColor = null;

	/**
	 * ライン表示のアニメーション有無
 	 * @since   UDC1.2
	 */
	protected boolean connAnimationStep = false;

	/**
	 * ライン表示のアニメーション色
 	 * @since   UDC1.2
	 */
	protected Color connAnimationColor = null;

	/**
	 * ライン表示のアニメーション・図形サイズ
 	 * @since   UDC1.2
	 */
	protected double connAnimationSize = 0.0;

	/**
	 * ライン表示をノードより上にするか否か
 	 * @since   UDC1.2
	 */
	protected boolean	topLayer = false;

	/**
	 * ラインのずらし幅（pixel値）
 	 * @since   UDC1.2
	 */
	protected int		connPhase = 0;

	/**
	 * ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
 	 * @since   UDC1.2
	 */
	protected int	opeType	= ConnOpeType_None;

	/**
	 * 操作種別のサイズ（pixel）
 	 * @since   UDC1.2
	 */
	protected int	opeSize	= 0;

	/**
	 * ラインの操作種別のShape。（ラインの中央に表示され、マウス操作を可能とする印種別）
 	 * @since   UDC1.2
	 */
	protected Polygon[]	opeShape = new Polygon[2];

	/**
	 * ラインの表示名
 	 * @since   UDC1.2
	 */
	protected String	connName = null;

	/**
	 * ActionListenerリスト
	 */
	ArrayList actionListenerList = new ArrayList();

	/**
	 * MouseListenerリスト
	 */
	ArrayList mouseListenerList = new ArrayList();

	/**
	 * ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	protected Object	__userObject = null;	

	// JTopologyPanel上での高速表示のための保持情報
	protected double theta = -1.0;
	protected double cosTheta = -100.0;
	protected double sinTheta = -100.0;
	protected double arrowLen = -1.0;
	protected int arrowtype = -1;
	protected double animationLen = -1.0;
	protected Polygon[]	animationShape = null;
	protected int phase = -1;
	protected double sx = -1;
	protected double sy = -1;
	protected double ex = -1;
	protected double ey = -1;
	protected double tsx = -1;
	protected double tsy = -1;
	protected double tex = -1;
	protected double tey = -1;
	protected Rectangle2D	connNameRect = null;

	/**
	 * コンストラクタ
	 *
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	usrtype		ライン種別
	 * @param	stroke		ラインのストローク
	 * @param	color		ライン色
	 * @since	UDC1.2
	 */
	public JTopologyConnectionInfo(JTopologyNode nodea, JTopologyNode nodeb, int usrtype, Stroke stroke, Color color)
	{
		connectNodeFrom = nodea;
		connectNodeTo = nodeb;
		connectType = usrtype;
		connStroke = stroke;
		connColor = color;
		opeShape[0] = new Polygon(new int[4], new int[4], 4);
		opeShape[1] = new Polygon(new int[4], new int[4], 4);
	}

	/**
	 * コンストラクタ
	 *
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	usrtype		ライン種別
	 * @param	stroke		ラインのストローク
	 * @param	color		ライン色
	 * @param	phase		ラインのずらし幅
	 * @since	UDC1.2
	 */
	public JTopologyConnectionInfo(JTopologyNode nodea, JTopologyNode nodeb, int usrtype, Stroke stroke, Color color, int phase)
	{
		this(nodea,nodeb,usrtype,stroke,color);
		connPhase = phase;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	usrtype		ライン種別
	 * @param	stroke		ラインのストローク
	 * @param	color		ライン色
	 * @param	phase		ラインのずらし幅
	 * @param	toplayer	ライン表示をノードより上にするか否か	
	 * @since	UDC1.2
	 */
	public JTopologyConnectionInfo(JTopologyNode nodea, JTopologyNode nodeb, int usrtype, Stroke stroke, Color color, int phase, boolean toplayer)
	{
		this(nodea,nodeb,usrtype,stroke,color,phase);
		topLayer = toplayer;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	usrtype		ライン種別
	 * @param	stroke		ラインのストローク
	 * @param	color		ライン色
	 * @param	phase		ラインのずらし幅
	 * @param	toplayer	ライン表示をノードより上にするか否か	
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @since	UDC1.2
	 */
	public JTopologyConnectionInfo(JTopologyNode nodea, JTopologyNode nodeb,
						int usrtype, Stroke stroke, Color color, int phase, boolean toplayer, int opetype)
	{
		this(nodea,nodeb,usrtype,stroke,color,phase,toplayer);
		opeType = opetype;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	usrtype		ライン種別
	 * @param	stroke		ラインのストローク
	 * @param	color		ライン色
	 * @param	phase		ラインのずらし幅
	 * @param	toplayer	ライン表示をノードより上にするか否か	
	 * @param	opetype		ラインの操作種別。（ラインの中央に表示され、マウス操作を可能とする印種別）
	 * @param	dispname	ラインの表示名(線名非表示の場合はnullを指定)
	 * @since	UDC1.2
	 */
	public JTopologyConnectionInfo(JTopologyNode nodea, JTopologyNode nodeb,
						int usrtype, Stroke stroke, Color color, int phase, boolean toplayer, int opetype, String dispname)
	{
		this(nodea,nodeb,usrtype,stroke,color,phase,toplayer,opetype);
		setConnName(dispname);
	}


	/**
	 * ラインの始点ノード情報を取得する
	 *
	 * @return ラインの始点ノード情報
 	 * @since   UDC1.2
	 */
	public JTopologyNode getConnectNodeFrom() { return connectNodeFrom; }

	/**
	 * ラインの終点ノード情報を取得する
	 *
	 * @return ラインの終点ノード情報
 	 * @since   UDC1.2
	 */
	public JTopologyNode getConnectNodeTo() { return connectNodeTo; }

	/**
	 * ラインの種別を取得する。ライン種別は大きい値のものがより前面に表示される。
	 * @return ラインの種別。ライン種別は大きい値のものがより前面に表示される。
 	 * @since   UDC1.2
	 */
	public int	getConnectType() { return connectType; }

	/**
	 * ラインのストロークを取得する
	 * @return ラインのストローク
 	 * @since   UDC1.2
	 */
	public Stroke getConnStroke() { return connStroke; }
	
	/**
	 * ラインの色を取得する
	 * @return ラインの色
 	 * @since   UDC1.2
	 */
	public Color getConnColor() { return connColor; }

	/**
	 * ラインの点滅色
 	 * @since   UDC1.2
	 */
	public Color getConnBlinkColor() { return connBlinkColor; }

	/**
	 * ライン表示をノードより上にするか否かを取得する
	 * @return ライン表示をノードより上にするか否か
 	 * @since   UDC1.2
	 */
	public boolean	getTopLayer() { return topLayer; }

	/**
	 * ラインのずらし幅（pixel値）を取得する
	 * @return ラインのずらし幅（pixel値）
 	 * @since   UDC1.2
	 */
	public int	getConnPhase() { return connPhase; }

	/**
	 * ラインの操作種別（ラインの中央に表示され、マウス操作を可能とする印種別）を取得する
	 * @return ラインの操作種別（ラインの中央に表示され、マウス操作を可能とする印種別）
 	 * @since   UDC1.2
	 */
	public int	getOpeType() { return opeType; }

	/**
	 * 操作種別のサイズ（pixel）を取得する
	 * @return 操作種別のサイズ（pixel）
 	 * @since   UDC1.2
	 */
	public int	getOpeSize() { return opeSize; }

	/**
	 * ラインの操作種別のShapeを取得する。
	 * @return ラインの操作種別のShape
 	 * @since   UDC1.2
	 */
	public Polygon[] getOpeShape() { return opeShape; }

	/**
	 * ラインの表示名を取得する
	 * @return 	ラインの表示名
 	 * @since   UDC1.2
	 */
	public String getConnName() { return connName; }

	/**
	 * ラインの表示名を設定する
	 * @param name	ラインの表示名
 	 * @since   UDC1.2
	 */
	protected void setConnName(String name)
	{ 
		connNameRect = null;
		connName = name;
	}

	/**
	 * コネクション表示名を表示する場合の表示サイズを取得する。<br>
	 * 表示サイズは、Graphicsに依存するため、入力されたGraphicsにマッチしたサイズを取得する。
	 *
	 * @param	g	グラフィックス
	 * @return 	コネクション表示名文字列のサイズ
	 * @since   UDC1.2
	 */
	protected Rectangle2D getConnNameLength(Graphics2D g)
	{
		if (connNameRect == null) {
			if (connName != null) {
				char[] chars = connName.toCharArray();
				connNameRect = getFont().getStringBounds(chars, 0, chars.length, g.getFontRenderContext());
			}
		}
		return connNameRect;
	}

	/**
	 * ライン表示のアニメーション色を取得する
	 * @return ライン表示のアニメーション色
 	 * @since   UDC1.2
	 */
	public Color getConnAnimationColor() { return connAnimationColor; }

	/**
	 * ライン表示のアニメーション・図形サイズを取得する
	 * @return ライン表示のアニメーション・図形サイズ
 	 * @since   UDC1.2
	 */
	public double getConnAnimationSize() { return connAnimationSize; }

	/**
	 * 点滅表示中か否かを取得する。
	 *
	 * @return 点滅表示中か否か
	 */
	public boolean isBlink() { return (connBlinkColor != null) ? true : false; }

	/**
	 * アニメーション表示中か否かを取得する。
	 *
	 * @return アニメーション表示中か否か
	 */
	public boolean isAnimation() { return connAnimationStep; }

	/**
	 * 指定した区間のライン情報と自インスタンスを比較する
	 *
	 * @return	真：一致／偽：不一致
	 * @param	nodea		始点ノード
	 * @param	nodeb		終点ノード
	 * @param	type		ライン種別
 	 * @since   UDC1.2
	 */
	public boolean isSame(String nodea, String nodeb, int type)
	{
		if (connectType != type) {
			return false;
		}
		if (connectNodeFrom.getNodeName().equals(nodea)) {
			if (connectNodeTo.getNodeName().equals(nodeb)) {
				return true;
			}
		//} else if (connectNodeTo.getNodeName().equals(nodea)) {
		//	if (connectNodeFrom.getNodeName().equals(nodeb)) {
		//		return true;
		//	}
		}
		return false;
	}

	/**
	 * 指定座標がパネル上のコネクション情報か判定する。
	 *
	 * @return	コネクション情報が指定された座標に存在するか否か
	 * @param	x	X座標
	 * @param	y	Y座標
 	 * @since   UDC1.2
	 */
	public boolean contain(double x, double y)
	{	
		switch (opeType) {
		case ConnOpeType_BiDirArrow:
			if (opeShape[0].contains(x, y)) { return true; }
			if (opeShape[1].contains(x, y)) { return true; }
			break;
		case ConnOpeType_UniDirArrow:
			if (opeShape[0].contains(x, y)) { return true; }
			break;
		case ConnOpeType_Normal:
			if (opeShape[0].contains(x, y)) { return true; }
			break;
		}
		return false;
	}

	/**
	 *  コネクションに登録されているActionListenerのリストを取得する。 
	 *
	 * @return 	コネクションに登録されているActionListenerのリスト
 	 * @since   UDC1.2
	 */
	public ArrayList getActionListenerList () { return actionListenerList; }

	/**
	 *  コネクションにActionListener を追加します。
	 *
	 * @param	listener	追加されるActionListener
 	 * @since   UDC1.2
	 */
	public void addActionListener(ActionListener listener)
	{
		actionListenerList.add(listener);
	}

	/**
	 *  コネクションからActionListener を削除します。
	 *
	 * @param	listener	削除するActionListener
 	 * @since   UDC1.2
	 */
	public void removeActionListener(ActionListener listener)
	{
		actionListenerList.remove(listener);
	}

	/**
	 *  コネクションに登録されているMouseListenerのリストを取得する。 
	 *
	 * @return 	コネクションに登録されているMouseListenerのリスト
 	 * @since   UDC1.2
	 */
	public ArrayList getMouseListenerList () { return mouseListenerList; }

	/**
	 *  コネクションにMouseListener を追加します。
	 *
	 * @param	listener	追加されるMouseListener
 	 * @since   UDC1.2
	 */
	public void addMouseListener(MouseListener listener)
	{
		mouseListenerList.add(listener);
	}

	/**
	 *  コネクションからMouseListener を削除します。
	 *
	 * @param	listener	削除するMouseListener
 	 * @since   UDC1.2
	 */
	public void removeMouseListener(MouseListener listener)
	{
		mouseListenerList.remove(listener);
	}

	/**
	 * ユーザオブジェクトを取得する
	 * 
	 * @return ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	public Object getUserObject() { return __userObject; }

	/**
	 * ユーザオブジェクトを設定する
	 * 
	 * @param obj	ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	public void setUserObject(Object obj) { __userObject = obj; }

}

