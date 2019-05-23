/* *********************************************************************
 * @(#)JLineChart.java 1.0, 31 Dec 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.chart;

import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * 折れ線グラフを表示するためのパネル・コンポーネント。<br>
 * 本クラスは、LayoutマネージャなしのJPanelを継承し、paintComponentをオーバライドしてグラフ情報を表示します。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 31 Dec 2006
 * @since   UDC1.2
 */
public class JLineChart extends JPanel
{
	/**
	 * グラフ種別：折れ線グラフ。
	 * @since   UDC1.21
	 */
	final public static int	ChartType_Line 	= 1;

	/**
	 * グラフ種別：棒グラフ。
	 * @since   UDC1.21
	 */
	final public static int	ChartType_Bar 	= 2;

	/**
	 * グラフ種別：分布グラフ。
	 * @since   UDC1.21
	 */
	final public static int	ChartType_Distribution 	= 3;

	/**
	 * グラフのプロット種別。なし。
	 * @since   UDC1.21
	 */
	final public static int	ProtType_None 	= 0;

	/**
	 * グラフのプロット種別。●型のプロット。
	 * @since   UDC1.21
	 */
	final public static int	ProtType_Arc 	= 1;

	/**
	 * グラフのプロット種別。■型のプロット。
	 * @since   UDC1.21
	 */
	final public static int	ProtType_Rect 	= 2;

	/**
	 * グリッドの種別。通常。
	 * @since   UDC1.21
	 */
	final public static int Grid_Normal	= 1;
	
	/**
	 * グリッドの種別。対数(logE)。
	 * @since   UDC1.21
	 */
	final public static int Grid_LogE	= 2;

	/**
	 * グリッドの種別。対数(log10)。
	 * @since   UDC1.21
	 */
	final public static int Grid_Log10	= 3;

	/**
	 * グリッド線の種別。グリッド線(点線)。
	 * @since   UDC1.21
	 */
	final public static int GridLine_Dotted	= 1;

	/**
	 * グリッド線の種別。グリッド線(実線)。
	 * @since   UDC1.21
	 */
	final public static int GridLine_Solid	= 2;

	/**
	 * グリッド線の種別。目盛りのみ。
	 * @since   UDC1.21
	 */
	final public static int GridLine_Scale	= 3;


	/**
	 * 閾値プロット線の種別。Y軸と垂直な任意のX軸のプロット線。
	 * @since   UDC1.21
	 */
	final public static int ThresholdType_X	= 1;

	/**
	 * 閾値プロット線の種別。X軸と水平な任意のY軸のプロット線。
	 * @since   UDC1.21
	 */
	final public static int ThresholdType_Y	= 2;

	/**
	 * X軸の軸名表示文字列。
	 * @since   UDC1.21
	 */
	String		xCredit = null;

	/**
	 * Y軸の軸名表示文字列。
	 * @since   UDC1.21
	 */
	String		yCredit = null;

	/**
	 * 軸名表示の色。
	 * @since   UDC1.21
	 */
	Color		creditColor = null;

	/**
	 * グリッド線の色。
	 * @since   UDC1.21
	 */
	Color		axisColor = null;

	/**
	 * X軸のグリッド線の幅種別。
	 * @since   UDC1.21
	 */
	int		gridTypeX = Grid_Normal;

	/**
	 * Y軸のグリッド線の幅種別。
	 * @since   UDC1.21
	 */
	int		gridTypeY = Grid_Normal;

	/**
	 * X軸のグリッド線の単位幅。
	 * @since   UDC1.21
	 */
	double		gridUnitX = 0.0f;

	/**
	 * Y軸のグリッド線の単位幅。
	 * @since   UDC1.21
	 */
	double		gridUnitY = 0.0f;

	/**
	 * X軸のグリッド線の種別。
	 * @since   UDC1.21
	 */
	int 		gridLineTypeX = GridLine_Dotted;

	/**
	 * Y軸のグリッド線の種別。
	 * @since   UDC1.21
	 */
	int 		gridLineTypeY = GridLine_Dotted;

	/**
	 * X軸のメモリ文字フォーマット。
	 * @since   UDC1.22
	 */
	Format 		xFormat = null;

	/**
	 * Y軸のメモリ文字フォーマット。
	 * @since   UDC1.22
	 */
	Format 		yFormat = null;


	/**
	 * 凡例表示フラグ
	 * @since   UDC1.22
	 */
	 boolean explanatory	= true;

	/**
	 * 目盛りストローク
	 * @since   UDC1.22
	 */
	BasicStroke	bs = new BasicStroke(0.1f);

	/**
	 * グラフ枠ストローク
	 * @since   UDC1.22
	 */
	BasicStroke	fbs = new BasicStroke(2.0f);

	/**
	 * グラフストローク
	 * @since   UDC1.22
	 */
	BasicStroke	dbs = new BasicStroke(1.0f);

	/**
	 * Y軸と垂直なX軸、あるいはX軸と水平なY軸の閾値プロットのリスト。
	 * @since   UDC1.22
	 */
	Vector		thresholdList = new Vector();	
	
	/**
	 * グラフ情報のリスト。
	 * @since   UDC1.21
	 */
	Vector		targetList = null;

	/**
	 * パネル上のグラフの開始X座標。
	 */
	int gstartx = 60;

	/**
	 * パネル上のグラフの開始Y座標。
	 */
	int gstarty = 40;

	/**
	 * パネル上のグラフの余白。
	 */
	int gedge = 30;

	/**
	 * 凡例を表示する際の幅。
	 */
	int gexedge = 30;

	/**
	 * グラフ描画のためのDimension
	 */
	Dimension bsz = new Dimension();

	/**
	 * グラフ上の文字描画のための高さ取得Rectangle
	 */
	Rectangle2D dispRect = null;

	/**
	 * コンストラクタ
	 *
 	 * @since   UDC1.2
	 */
	public JLineChart()
	{	
		super(null);
		setTargetList(new Vector());	
	}

	/**
	 * コンストラクタ
	 *
	 * @param	xcredit	X軸の軸名表示文字列
	 * @param	ycredit	Y軸の軸名表示文字列
 	 * @since   UDC1.2
	 */
	public JLineChart(String xcredit, String ycredit)
	{
		super(null);
		xCredit = xcredit;
		yCredit = ycredit;
		setTargetList(new Vector());	
	}

	/**
	 * コンストラクタ
	 *
	 * @param	targetlist	グラフ情報のリスト
	 * @param	xcredit	X軸の軸名表示文字列
	 * @param	ycredit	Y軸の軸名表示文字列
 	 * @since   UDC1.2
	 */
	public JLineChart(Vector targetlist, String xcredit, String ycredit)
	{
		super(null);
		xCredit = xcredit;
		yCredit = ycredit;
		setTargetList(targetlist);	
	}

	/**
	 * パネル上のグラフの開始X座標(グラフの左端上の座標)を取得する。デフォルトは60に設定されています。
	 * @return パネル上のグラフの開始X座標
 	 * @since   UDC1.22
	 */
	public synchronized int getGraphPositionX() { return gstartx; }

	/**
	 * パネル上のグラフの開始X座標(グラフの左端上の座標)を設定する。デフォルトは60に設定されています。
	 * @param	x	パネル上のグラフの開始X座標
 	 * @since   UDC1.22
	 */
	public synchronized void setGraphPositionX(int x) { gstartx = x; }

	/**
	 * パネル上のグラフの開始X座標(グラフの左端上の座標)を取得する。デフォルトは40に設定されています。
	 * @return パネル上のグラフの開始X座標
 	 * @since   UDC1.22
	 */
	public synchronized int getGraphPositionY() { return gstarty; }

	/**
	 * パネル上のグラフの開始X座標(グラフの左端上の座標)を設定する。デフォルトは40に設定されています。
	 * @param	y	パネル上のグラフの開始Y座標
 	 * @since   UDC1.22
	 */
	public synchronized void setGraphPositionY(int y) { gstarty = y; }

	/**
	 * パネル上のグラフの余白を取得する。デフォルトは30に設定されています。
	 * @return パネル上のグラフの余白
 	 * @since   UDC1.22
	 */
	public synchronized int getMargin() { return gedge; }

	/**
	 * パネル上のグラフの余白を設定する。デフォルトは30に設定されています。
	 * @param	m	 パネル上のグラフの余白
 	 * @since   UDC1.22
	 */
	public synchronized void setMargin(int m) { gedge = m; }

	/**
	 * 凡例を表示する際のY軸の高さを取得する。デフォルトは30に設定されています。
	 * @return 凡例を表示する際のY軸の高さ
	 */
	public int getExplanatoryHeight() { return gexedge; }

	/**
	 * 凡例を表示する際のY軸の高さを設定する。デフォルトは30に設定されています。
	 * @param	ht 凡例を表示する際のY軸の高さ
	 */
	public synchronized void setExplanatoryHeight(int ht) { gexedge = ht; }

	/**
	 * グリッド線の色を取得する。
	 * @return	グリッド線の色
	 * @since   UDC1.21
	 */
	public synchronized Color	getAxisColor() { return axisColor; }

	/**
	 * グリッド線の色を設定する。
	 * @param	color	グリッド線の色
	 * @since   UDC1.21
	 */
	public synchronized void		setAxisColor(Color color) { axisColor = color; }

	/**
	 * X軸の軸名表示文字列を取得する。
	 * @return X軸の軸名表示文字列
	 * @since   UDC1.21
	 */
	public synchronized String	getXCredit() { return xCredit; }

	/**
	 * X軸の軸名表示文字列を設定する。
	 * @param	credit X軸の軸名表示文字列
	 * @since   UDC1.21
	 */
	public synchronized void	setXCredit(String credit) { xCredit = credit; }

	/**
	 * Y軸の軸名表示文字列を取得する。
	 * @return Y軸の軸名表示文字列
	 * @since   UDC1.21
	 */
	public synchronized String	getYCredit() { return yCredit; }

	/**
	 * Y軸の軸名表示文字列を設定する。
	 * @param	credit y軸の軸名表示文字列
	 * @since   UDC1.21
	 */
	public synchronized void	setYCredit(String credit) { yCredit = credit; }

	/**
	 * X軸のメモリ文字のFormatを取得する。
	 * @return X軸のメモリ文字のFormat
	 * @since   UDC1.21
	 */
	public synchronized Format	getGridXFormat() { return xFormat; }

	/**
	 * X軸のメモリ文字のFormatを設定する。
	 * @param fmt X軸のメモリ文字のFormat
	 * @since   UDC1.21
	 */
	public synchronized void	setGridXFormat(Format fmt) { xFormat = fmt; }

	/**
	 * Y軸のメモリ文字のFormatを取得する。
	 * @return Y軸のメモリ文字のFormat
	 * @since   UDC1.21
	 */
	public synchronized Format	getGridYFormat() { return yFormat; }

	/**
	 * Y軸のメモリ文字のFormatを設定する。
	 * @param fmt Y軸のメモリ文字のFormat
	 * @since   UDC1.21
	 */
	public synchronized void	setGridYFormat(Format fmt) { yFormat = fmt; }

	/**
	 * X軸のグリッドの種別を取得する。
	 * @return X軸のグリッドの種別
	 * @since   UDC1.21
	 */
	public synchronized int	getGridTypeX() { return gridTypeX; }

	/**
	 * X軸のグリッドの種別を設定する。
	 * @param type	X軸のグリッドの種別(Grid_Normal|Grid_LogE|Grid_Log10)
	 * @since   UDC1.21
	 */
	public synchronized void	setGridTypeX(int type) { gridTypeX = type; }

	/**
	 * Y軸のグリッドの種別を取得する。
	 * @return Y軸のグリッドの種別
	 * @since   UDC1.21
	 */
	public synchronized int	getGridTypeY() { return gridTypeY; }

	/**
	 * Y軸のグリッドの種別を設定する。
	 * @param type Y軸のグリッドの種別(Grid_Normal|Grid_LogE|Grid_Log10)
	 * @since   UDC1.21
	 */
	public synchronized void	setGridTypeY(int type) { gridTypeY = type; }

	/**
	 * X軸のグリッド線の単位幅を取得する。
	 * @return	X軸のグリッド線の単位幅
	 * @since   UDC1.21
	 */
	public synchronized double	getGridUnit_X() { return gridUnitX; }

	/**
	 * X軸のグリッド線の単位幅を設定する。
	 * @param	gridunit	X軸のグリッド線の単位幅
	 * @since   UDC1.21
	 */
	public synchronized void		setGridUnit_X(double gridunit) { gridUnitX = gridunit; }

	/**
	 * Y軸のグリッド線の単位幅を取得する。
	 * @return	Y軸のグリッド線の単位幅
	 * @since   UDC1.21
	 */
	public synchronized double	getGridUnit_Y() { return gridUnitY; }

	/**
	 * Y軸のグリッド線の単位幅を設定する。
	 * @param	gridunit	Y軸のグリッド線の単位幅
	 * @since   UDC1.21
	 */
	public synchronized void		setGridUnit_Y(double gridunit) { gridUnitY = gridunit; }

	/**
	 * X軸のグリッド線の種別を取得する。
	 * @return X軸のグリッド線の種別
	 * @since   UDC1.21
	 */
	public synchronized int 	getGridLineType_X() { return gridLineTypeX; }

	/**
	 * X軸のグリッド線の種別を設定する。
	 * @param type X軸のグリッド線の種別(GridLine_Dotted|GridLine_Solid|GridLine_Scale)
	 * @since   UDC1.21
	 */
	public synchronized void setGridLineType_X(int type) { gridLineTypeX = type; }

	/**
	 * Y軸のグリッド線の種別を取得する。
	 * @return Y軸のグリッド線の種別
	 * @since   UDC1.21
	 */
	public synchronized int 	getGridLineType_Y() { return gridLineTypeY; }

	/**
	 * Y軸のグリッド線の種別を設定する。
	 * @param type Y軸のグリッド線の種別(GridLine_Dotted|GridLine_Solid|GridLine_Scale)
	 * @since   UDC1.21
	 */
	public synchronized void setGridLineType_Y(int type) { gridLineTypeY = type; }





	/**
	 * 凡例表示有無を取得する。
	 */
	 public synchronized boolean getExplanatory() { return explanatory; }

	/**
	 * 凡例表示有無を設定する。
	 */
	 public synchronized void setExplanatory(boolean flag) { explanatory = flag; }

	/**
	 * グラフ情報のリストを取得する。
	 * @return グラフ情報のリスト
	 * @since   UDC1.21
	 */
	public synchronized Vector 	getTargetList() { return targetList; }

	/**
	 * グラフ情報のリストを設定する。
	 * @param	targetlist グラフ情報のリスト。
	 * @since   UDC1.21
	 */
	public synchronized void 	setTargetList(Vector targetlist) { targetList = targetlist; }

	/**
	 * グラフ情報のリストをクリアする。
	 * @since   UDC1.22
	 */
	public synchronized void 	resetTargetList() { targetList = null; }

	/**
	 * Y軸と垂直なX軸、あるいはX軸と水平なY軸の閾値プロット線を追加する。
	 * @param	point	閾値プロットの座標
	 * @param	color	閾値プロット線色
	 * @since   UDC1.22
	 */
	public synchronized void addThreshold(int type, double point, Color color)
	{
		thresholdList.add(new JChartThresholdProt(type, point, color));
	}

	/**
	 * Y軸と垂直なX軸、あるいはX軸と水平なY軸の閾値プロット線を削除する。
	 * @param	point	閾値プロットの座標
	 * @since   UDC1.22
	 */
	public synchronized void removeThreshold(int type, double point)
	{
		JChartThresholdProt th = getThreshold(type, point);
		if (th != null) {
			thresholdList.remove(th);
		}
	}

	/**
	 * 閾値プロット線を全てクリアする。
	 * @since   UDC1.22
	 */
	public synchronized void clearThreshold()
	{
		thresholdList.clear();	
	}

	private synchronized JChartThresholdProt getThreshold(int type, double point)
	{
		JChartThresholdProt th;
		for (int i=0; i<thresholdList.size(); i++) {
			th = (JChartThresholdProt)thresholdList.get(i);
			if (th.type == type && th.point == point) {
				return th;
			}
		}
		return null;
	}

	/**
	 * グラフ情報を追加する。
	 * @param	target グラフ情報
	 * @return 	追加したグラフ情報(重複する場合nullを返却)
	 * @since   UDC1.21
	 */
	public synchronized JChartTarget	addTarget(JChartTarget target)
	{
		if (getTarget(target.getTargetName()) != null) {
			return null;
		}
		targetList.add(target);
		return target;
	}

	/**
	 * グラフ情報を削除する。
	 * @param	name グラフ名
	 * @since   UDC1.21
	 */
	public synchronized void removeTarget(String name)
	{
		JChartTarget target;
		if ((target=getTarget(name)) != null) {
			targetList.remove(target);
		}
	}

	/**
	 * グラフ情報を取得する。
	 * @param	name グラフ名
	 * @return	グラフ構成情報
	 * @since   UDC1.21
	 */
	public synchronized JChartTarget	getTarget(String name)
	{
		if (targetList == null) {
			return null;
		}
		String lname;
		JChartTarget target;
		for (int i=0; i<targetList.size(); i++) {
			target = (JChartTarget)targetList.get(i);
			if ((lname=target.getTargetName()) != null && name.equals(lname)) {
				return target;
			}
		}
		return null;
	}

	/**
	 * Y軸のグリッド線を描画する。
	 * @param	g2d	グラフィックス
	 * @param	minX	Y軸グリッドの始点X座標
	 * @param	maxX	Y軸グリッドの終点X座標
	 * @param	y		Y軸グリッドのY座標
	 * @since   UDC1.21
	 */
	void drawProtLineHorizontal(Graphics2D g2d, int minX, int maxX, int y)
	{
		for (int x=minX; x<maxX; x+=4) { g2d.drawLine(x, y, x+1, y); }
	}

	/**
	 * X軸のグリッド線を描画する。
	 * @param	g2d	グラフィックス
	 * @param	minY	X軸グリッドの始点Y座標
	 * @param	maxY	X軸グリッドの終点Y座標
	 * @param	x		X軸グリッドのY座標
	 * @since   UDC1.21
	 */
	void drawProtLineVertical(Graphics2D g2d, int minY, int maxY, int x)
	{
		for (int y=minY; y<maxY; y+=4) { g2d.drawLine(x, y, x, y+1); }
	}

	/**
	 * データのX座標を取得する。
	 * @param	x			X値
	 * @param	minusX		X軸の補正値
	 * @param	minValX		X軸の始点値
	 * @param	maxValX		X軸の終点値
	 * @param	startx		X軸の始点座標
	 * @param	dim			グラフ・パネルのサイズ
	 * @since   UDC1.21
	 */
	int getXPoint(double x, double minusX, double minValX, double maxValX, double startx, Dimension dim)
	{
		double lx = x;
		if (gridTypeX == Grid_LogE) {
			lx = Math.log(x);
		} else if (gridTypeX == Grid_Log10) {
			lx = Math.log10(x);
		}

		//return (int)((lx-minusX) * ((dim.getWidth()-120)/(maxValX-minValX)) + startx);
		return (int)((lx-minusX) * ((dim.getWidth()-(startx+gedge))/(maxValX-minValX)) + startx);
	}

	/**
	 * データのY座標を取得する。
	 * @param	y			Y値
	 * @param	minusX		Y軸の補正値
	 * @param	minValX		Y軸の始点値
	 * @param	maxValX		Y軸の終点値
	 * @param	startx		Y軸の始点座標
	 * @param	dim			グラフ・パネルのサイズ
	 * @since   UDC1.21
	 */
	int getYPoint(double y, double minusY, double minValY, double maxValY, double starty, Dimension dim)
	{
		double ly = y;
		if (gridTypeY == Grid_LogE) {
			ly = Math.log(y);
		} else if (gridTypeY == Grid_Log10) {
			ly = Math.log10(y);
		}
		int exheight = explanatory ? gexedge: 0;
		if (xCredit != null) { exheight += dispRect.getHeight() + 2; }
		if (gridTypeX == Grid_LogE || gridTypeX == Grid_Log10 || gridUnitX > 0.0f) { exheight += dispRect.getHeight() + 2; }
		double tmp = starty + gedge;
		return (int)((dim.getHeight()-tmp-exheight) - ((ly-minusY) * ((dim.getHeight()-tmp-exheight)/(maxValY-minValY))) + starty);
	}

	/**
	 * paintComponentメンバ関数のオーバライド。本メンバ関数によって画面上の表示処理が実現されます。
	 * @param	g	グラフィックス
	 * @since   UDC1.21
	 */
	protected synchronized void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (targetList == null || targetList.size() <= 0) {
			return;
		}

		int i, j, pos;
		double prot, mintmp, maxtmp;
		Vector dataList;
		JChartTarget target;
		Color defColorFg = getForeground();
		Font  defFont = getFont();
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform srcform = g2d.getTransform();

		// 文字の表示高さを取得
		char[] chars = "ABCD".toCharArray();
		dispRect = getFont().getStringBounds(chars, 0, chars.length, g2d.getFontRenderContext());

		// グラフの基礎情報取得
		int protType, x,y, px=-1, py=-1;
		double maxValX, maxValY, minValX, minValY;
		maxValX = maxValY = 0.00;
		minValX = minValY = 0.00;
		for (i=0; i<targetList.size(); i++) {
			target = (JChartTarget)targetList.get(i);
			if (i == 0) {
				maxValX = target.getMaxValX();
				maxValY = target.getMaxValY();
				minValX = target.getMinValX();
				minValY = target.getMinValY();
				continue;
			}
			if (maxValX < target.getMaxValX()) { maxValX = target.getMaxValX(); }	
			if (maxValY < target.getMaxValY()) { maxValY = target.getMaxValY(); }	
			if (minValX > target.getMinValX()) { minValX = target.getMinValX(); }	
			if (minValY > target.getMinValY()) { minValY = target.getMinValY(); }	
		}
		double minusX=0.00, minusY=0.00;
		if (minValX != 0.00) { minusX = minValX; }
		if (minValY != 0.00) { minusY = minValY; }
		if ((gridTypeY == Grid_LogE || gridTypeY == Grid_Log10) && minValY <= 0.0) { minValY = 0.00000001; }
		if ((gridTypeX == Grid_LogE || gridTypeX == Grid_Log10) && minValX <= 0.0) { minValX = 0.00000001; }

		// Log系グラフの場合、min/maxValをLog値換算で変換してしまうため、値範囲チェックを行うために保存
		double trueMaxValX = maxValX;
		double trueMinValX = minValX;
		double trueMaxValY = maxValY;
		double trueMinValY = minValY;

		// グラフの枠作成
		Dimension dim = getSize(bsz);
		JChartThresholdProt th;
		int startx, starty, endx, endy, exheight;
		startx = gstartx;
		starty = gstarty;
		endx = (int)(dim.getWidth() - gedge);
		exheight = explanatory ? gexedge: 0;
		if (xCredit != null) { exheight += dispRect.getHeight() + 2; }
		if (gridTypeX == Grid_LogE || gridTypeX == Grid_Log10 || gridUnitX > 0.0f) { exheight += dispRect.getHeight() + 2; }
		endy = (int)(dim.getHeight() - gedge - exheight);

		if (xCredit != null) {
			char[] crechars = xCredit.toCharArray();
			Rectangle2D creditRect = getFont().getStringBounds(crechars, 0, crechars.length, g2d.getFontRenderContext());
			int xpos = startx + ((endx - startx) / 2);
			xpos -= (int)(creditRect.getWidth() / 2);
			int ypos = (int)(endy + creditRect.getHeight()) + 2;
			if (gridTypeX == Grid_LogE || gridTypeX == Grid_Log10 || gridUnitX > 0.0f) {
				ypos += (int)creditRect.getHeight() + 2;
			}
			g2d.drawString(xCredit, xpos, ypos);
		}
		int yprotst = gedge;
		if (yCredit != null) {
			char[] crechars = yCredit.toCharArray();
			Rectangle2D creditRect = getFont().getStringBounds(crechars, 0, crechars.length, g2d.getFontRenderContext());
			int ypos = starty + ((endy - starty) / 2);
			ypos += (int)(creditRect.getWidth() / 2);
			int xpos = (int)(gedge + creditRect.getHeight());
			g2d.transform(AffineTransform.getRotateInstance((Math.PI/2.0)*3.0, xpos, ypos));	
			g2d.drawString(yCredit, xpos, ypos);
			g2d.setTransform(srcform);
			yprotst += creditRect.getHeight() + 4;
		}

		Color srcColor = g2d.getColor();
			// Y軸
		g2d.setStroke( fbs );
		if (axisColor != null) { g2d.setColor(axisColor); }
		g2d.drawLine(startx, starty, startx, endy);
				// prot line
		g2d.setStroke( bs );
		if (gridTypeY == Grid_LogE) {
			minValY = Math.log(minValY) - 1.0;
			maxValY = (double)((int)(Math.log(maxValY) + 1.0));
			minusY = (minValY < 0) ? minValY : 0.0;
			for (j=0, i=(int)minValY; i<=(int)maxValY; j++, i++) {
				prot = Math.pow(Math.E, (double)(i));
				if (getThreshold(ThresholdType_Y, prot) == null) {
					pos = getYPoint(prot, minusY, minValY, maxValY, starty, dim);
					if (yFormat != null) {
						g2d.drawString(yFormat.format(new Double(Math.pow(Math.E, (double)(j)))), yprotst, pos+5);
					} else {
						g2d.drawString("E^" + j , yprotst, pos+5);
					}
					if (j != 0) {
						if (gridLineTypeY == GridLine_Dotted) 		{ drawProtLineHorizontal(g2d, startx, endx, pos); }
						else if (gridLineTypeY == GridLine_Solid) 	{ g2d.drawLine(startx, pos, endx, pos); }
						else 										{ g2d.drawLine(startx, pos, startx+3, pos); }
					}
				}
			}
		} else if (gridTypeY == Grid_Log10) {
			minValY = Math.log10(minValY) - 1.0;
			maxValY = (double)((int)(Math.log10(maxValY) + 1.0));
			minusY = (minValY < 0) ? minValY : 0.0;
			for (j=0, i=(int)minValY; i<=(int)maxValY; j++, i++) {
				prot = Math.pow(10.0, (double)(i));
				if (getThreshold(ThresholdType_Y, prot) == null) {
					pos = getYPoint(prot, minusY, minValY, maxValY, starty, dim);
					if (yFormat != null) {
						g2d.drawString(yFormat.format(new Double(prot)), yprotst, pos+5);
					} else {
						g2d.drawString("" + prot, yprotst, pos+5);
					}
					if (j != 0) {
						if (gridLineTypeY == GridLine_Dotted) 		{ drawProtLineHorizontal(g2d, startx, endx, pos); }
						else if (gridLineTypeY == GridLine_Solid) 	{ g2d.drawLine(startx, pos, endx, pos); }
						else 										{ g2d.drawLine(startx, pos, startx+3, pos); }
					}
				}
			}
		} else {
			if (gridUnitY > 0.0f) {
				maxtmp = maxValY / gridUnitY;
				mintmp = minValY / gridUnitY;
				if ((gridUnitY*((int)maxtmp)) < maxValY) { maxValY = gridUnitY * ((int)maxtmp + 1); }
				if (minValY < 0.00) {
					if ((gridUnitY*((int)mintmp)) > minValY) {
						minValY = gridUnitY * ((int)mintmp - 1);
						minusY = minValY;
					}
				}
				int widthy = Math.abs((int)maxtmp) + Math.abs((int)mintmp);
				for (i=0; i<=widthy; i++) {
					prot = minValY + (gridUnitY * i);
					if (getThreshold(ThresholdType_Y, prot) == null) {
						pos =  getYPoint(prot, minusY, minValY, maxValY, starty, dim);
						if (yFormat != null) {
							g2d.drawString(yFormat.format(new Double(prot)), yprotst, pos+5);
						} else {
							g2d.drawString("" + prot, yprotst, pos+5);
						}
						if (i != 0) {
							if (prot == 0) 	{
								if (gridLineTypeY == GridLine_Scale) { g2d.drawLine(startx, pos, startx+3, pos); }
								else								 { g2d.drawLine(startx, pos, endx, pos); }
							} else {
								if (gridLineTypeY == GridLine_Dotted) 		{ drawProtLineHorizontal(g2d, startx, endx, pos); }
								else if (gridLineTypeY == GridLine_Solid) 	{ g2d.drawLine(startx, pos, endx, pos); }
								else 										{ g2d.drawLine(startx, pos, startx+3, pos); }
							}
						}
					}
				}
			}
		}
				// 閾値線
		for (i=0; i<thresholdList.size(); i++) {
			th = (JChartThresholdProt)thresholdList.get(i);
			if (th.type != ThresholdType_Y || th.point < minValY || th.point > maxValY) { continue; }
			g2d.setColor(th.color);
			pos =  getYPoint(th.point, minusY, minValY, maxValY, starty, dim);
			if (yFormat != null) {
				g2d.drawString(yFormat.format(new Double(th.point)), yprotst, pos+5);
			} else {
				g2d.drawString("" + th.point, yprotst, pos+5);
			}
			g2d.drawLine(startx, pos, endx, pos);
		}
		g2d.setColor(srcColor);

			// X軸
		g2d.setStroke( fbs );
		if (axisColor != null) { g2d.setColor(axisColor); }
		g2d.drawLine(startx, endy, endx, endy);
				// prot line
		g2d.setStroke( bs );
		if (gridTypeX == Grid_LogE) {
			minValX = Math.log(minValX) - 1.0;
			maxValX = (double)((int)(Math.log(maxValX) + 1.0));
			minusX = (minValX < 0) ? minValX : 0.0;
			for (j=0, i=(int)minValX; i<=(int)maxValX; j++, i++) {
				prot = Math.pow(Math.E, (double)(i));
				if (getThreshold(ThresholdType_X, prot) == null) {
					pos = getXPoint(prot, minusX, minValX, maxValX, startx, dim);
					if (xFormat != null) {
						g2d.drawString(xFormat.format(new Double(Math.pow(Math.E, (double)(j)))), pos-15, endy+15);
					} else {
						g2d.drawString("E^" + j , pos-15, endy+15);
					}
					if (j != 0) {
						if (gridLineTypeX == GridLine_Dotted) 		{ drawProtLineVertical(g2d, starty, endy, pos); }
						else if (gridLineTypeX == GridLine_Solid) 	{ g2d.drawLine(pos, starty, pos, endy); }
						else 										{ g2d.drawLine(pos, endy-3, pos, endy); }
					}
				}
			}
		} else if (gridTypeX == Grid_Log10) {
			minValX = Math.log10(minValX) - 1.0;
			maxValX = (double)((int)(Math.log10(maxValX) + 1.0));
			minusX = (minValX < 0) ? minValX : 0.0;
			for (j=0, i=(int)minValX; i<=(int)maxValX; j++, i++) {
				prot = Math.pow(10.0, (double)(i));
				if (getThreshold(ThresholdType_X, prot) == null) {
					pos = getXPoint(prot, minusX, minValX, maxValX, startx, dim);
					if (xFormat != null) {
						g2d.drawString(xFormat.format(new Double(prot)), pos-15, endy+15);
					} else {
						g2d.drawString("" + prot , pos-15, endy+15);
					}
					if (j != 0) {
						if (gridLineTypeX == GridLine_Dotted) 		{ drawProtLineVertical(g2d, starty, endy, pos); }
						else if (gridLineTypeX == GridLine_Solid) 	{ g2d.drawLine(pos, starty, pos, endy); }
						else 										{ g2d.drawLine(pos, endy-3, pos, endy); }
					}
				}
			}
		} else {
			if (gridUnitX > 0.0f) {
				maxtmp = maxValX / gridUnitX;
				mintmp = minValX / gridUnitX;
				if ((gridUnitX*((int)maxtmp)) < maxValX) { maxValX = gridUnitX * ((int)maxtmp + 1); }
				if (minValX < 0.00) {
					if ((gridUnitX*((int)mintmp)) > minValX) {
						minValX = gridUnitX * ((int)mintmp - 1);
						minusX = minValX;
					}
				}
				int widthx = Math.abs((int)maxtmp - (int)mintmp);
				for (i=0; i<=widthx; i++) {
					prot = minValX + (gridUnitX * i);
					if (getThreshold(ThresholdType_X, prot) == null) {
						pos = getXPoint(prot, minusX, minValX, maxValX, startx, dim);
						if (xFormat != null) {
							g2d.drawString(xFormat.format(new Double(prot)), (int)(pos-2), (int)(endy+dispRect.getHeight()+2));
						} else {
							g2d.drawString("" + prot , (int)(pos-2), (int)(endy+getHeight()+2));
						}
						if (i != 0) {
							if (prot == 0) 	{
								if (gridLineTypeX == GridLine_Scale) { g2d.drawLine(pos, endy-3, pos, endy); }
								else								 { g2d.drawLine(pos, starty, pos, endy); }
							} else {
								if (gridLineTypeX == GridLine_Dotted) 		{ drawProtLineVertical(g2d, starty, endy, pos); }
								else if (gridLineTypeX == GridLine_Solid) 	{ g2d.drawLine(pos, starty, pos, endy); }
								else 										{ g2d.drawLine(pos, endy-3, pos, endy); }
							}
						}
					}
				}
			}
		}
				// 閾値線
		for (i=0; i<thresholdList.size(); i++) {
			th = (JChartThresholdProt)thresholdList.get(i);
			if (th.type != ThresholdType_X || th.point < minValX || th.point > maxValX) { continue; }
			g2d.setColor(th.color);
			pos = getXPoint(th.point, minusX, minValX, maxValX, startx, dim);
			if (xFormat != null) {
				g2d.drawString(xFormat.format(new Double(th.point)), pos-15, endy+15);
			} else {
				g2d.drawString("" + th.point, pos-15, endy+15);
			}
			g2d.drawLine(pos, starty, pos, endy);
		}
		g2d.setColor(srcColor);

		// データのプロット
		JChartData data;
		g2d.setStroke( dbs );
		int barbasey=0, barwidth=5;
		int extop = endy + exheight - gexedge + 4;
		int exstrtop = extop + (int)dispRect.getHeight();
		for (j=0; j<targetList.size(); j++) {
			px = py = -1;
			// グラフ属性の設定
			target = (JChartTarget)targetList.get(j);
			dataList = target.getDataList();	
			protType = target.getProtType();
			if (target.getChartType() == ChartType_Line) {
				g2d.setStroke( (target.getGraphStroke() == null) ? dbs : target.getGraphStroke());
			} else if (target.getChartType() == ChartType_Bar) {
				g2d.setStroke( (target.getGraphStroke() == null) ? dbs : target.getGraphStroke());
				barwidth = (target.getBarWidth() <= 0) ? 5 : target.getBarWidth();
				barbasey = getYPoint(target.getBarBaseVal(), minusY, minValY, maxValY, starty, dim);
			} else if (target.getChartType() == ChartType_Distribution) {
				;
			}
			if (target.getGraphColor() != null) { g2d.setColor(target.getGraphColor()); }
			// 凡例 
			if (explanatory && target.getTargetName() != null) {
				g2d.setFont(defFont);
				if (protType == ProtType_Arc) 	  { g2d.fillArc (startx+20+(j*100), extop-4,  8, 8, 0, 360); }
				else if (protType == ProtType_Rect) { g2d.fillRect(startx+20+(j*100), extop-4,  8, 8); }
				g2d.drawLine(startx+(j*100), extop, startx+50+(j*100),  extop);
				g2d.drawString(target.getTargetName(), startx+(j*100), exstrtop);	
			}
			if (target.getProtFont() != null) 	{ g2d.setFont(target.getProtFont()); }
			// データプロット
			for (i=0; i<dataList.size(); i++) {
				data = (JChartData)dataList.get(i);
				/* Log形式では、0.0以下は表現不能 */
				if ((gridTypeX == Grid_Log10 || gridTypeX == Grid_LogE) && data.getX() <= 0.0) { continue; }
				if ((gridTypeY == Grid_Log10 || gridTypeX == Grid_LogE) && data.getY() <= 0.0) { continue; }

				if (data.getX() < trueMinValX || data.getX() > trueMaxValX) { continue; }
				if (data.getY() < trueMinValY || data.getY() > trueMaxValY) { continue; }
				x = getXPoint(data.getX(), minusX, minValX, maxValX, startx, dim);
				y = getYPoint(data.getY(), minusY, minValY, maxValY, starty, dim);

				// 折れ線グラフ
				if (target.getChartType() == ChartType_Line) {	
					// データプロット
					if (protType == ProtType_Arc) 		{ g2d.fillArc(x-4, y-4,  8, 8, 0, 360); }
					else if (protType == ProtType_Rect) { g2d.fillRect(x-4, y-4, 8, 8); }
					// データプロットライン
					if (px >= 0) { g2d.drawLine(x,y, px,py); }	
					// データプロット位置文字列
					if (data.getDisplayProtValue()) { g2d.drawString(target.getProtValue(data), x-15, y+15); }
					px = x;
					py = y;
				// 棒グラフ
				} else if (target.getChartType() == ChartType_Bar) {
					if (data.getY() >= target.getBarBaseVal()) {
						// データプロットライン
						g2d.fillRect(x,y, barwidth, (barbasey-y));
						// データプロット位置文字列
						if (data.getDisplayProtValue()) { g2d.drawString(target.getProtValue(data), x-15, y-15); }
					} else {
						// データプロットライン
						g2d.fillRect(x,barbasey, barwidth, (y-barbasey));
						// データプロット位置文字列
						if (data.getDisplayProtValue()) { g2d.drawString(target.getProtValue(data), x-15, y+15); }
					}
				// 分布グラフ
				} else if (target.getChartType() == ChartType_Distribution) {
					// データプロット
					if (protType == ProtType_Arc) 		{ g2d.fillArc(x-2, y-2,  4, 4, 0, 360); }
					else if (protType == ProtType_Rect) { g2d.fillRect(x-2, y-2, 4, 4); }
					else								{ g2d.fillArc(x-1, y-1, 2, 2, 0, 360); }
					// データプロット位置文字列
					if (data.getDisplayProtValue()) { g2d.drawString(target.getProtValue(data), x-10, y+10); }
				}
			}
			g2d.setColor(defColorFg);
			g2d.setFont(defFont);
		}
	}

	

	/**
	 * 閾値プロット線情報(本情報は内部情報としてのみ使用される)。
	 *
	 * @author  Takayuki Uchida
	 * @version 1.2, 31 Jul 2007
	 * @since   UDC1.22
	 */
	class  JChartThresholdProt
	{
		public int 		type;
		public double	point;
		public Color	color;

		public JChartThresholdProt(int t, double pt, Color c)
		{
			type = t;
			point = pt;
			color = c;
		}
	}
}

