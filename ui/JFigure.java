/* *********************************************************************
 * @(#)JFigure.java 1.21, 31 Jul 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui;

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * 幾何学図形を現すコンポーネント。
 *
 * @author  Takayuki Uchida
 * @version 1.21, 31 Jul 2006
 * @since   UDC1.2
 */
public class JFigure extends JComponent
{
	/**
	 * 線
 	 * @since   UDC1.2
	 */
	public final static int	Type_Line = 1;

	/**
	 * 四角図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Rectangle = 2;

	/**
	 * 丸いコーナーつき四角図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_RoundRectangle	= 3;

	/**
	 * 丸図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Arc = 4;

	/**
	 * 楕円図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Oval = 5;

	/**
	 * 連続的につながった線
 	 * @since   UDC1.2
	 */
	public final static int	Type_Polyline = 6;

	/**
	 * 多角形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Polygon = 7;


	/**
	 * 図形種別
 	 * @since   UDC1.2
	 */
	protected int __type = Type_Line;

	/**
	 * 塗りつぶし有無
 	 * @since   UDC1.2
	 */
	protected boolean __fill = false;

	/**
	 * Graphics2DコンテキストのStroke
 	 * @since   UDC1.2
	 */
	protected Stroke __stroke = null;

	/**
	 * X軸座標情報
 	 * @since   UDC1.2
	 */
	protected int[]	xPoints = null;

	/**
	 * Y軸座標情報
 	 * @since   UDC1.2
	 */
	protected int[]	yPoints = null;

	/**
	 * 座標情報数
 	 * @since   UDC1.2
	 */
	protected int	pointNum = 0;


	/**
	 * コンストラクタ
 	 * @since   UDC1.2
	 */
	public JFigure()
	{
		super();
	}

	/**
	 * 指定した図形種別を取得する
	 * @return 	図形種別
 	 * @since   UDC1.2
	 */
	public int getType() { return __type; }

	/**
	 * 指定した図形を塗りつぶすか否かを取得する
	 * @return 	塗りつぶすか否か
 	 * @since   UDC1.2
	 */
	public boolean getFill() { return __fill; }

	/**
	 * 指定した図形を塗りつぶすか否かを設定する
	 * @param 	fill	塗りつぶすか否か
 	 * @since   UDC1.2
	 */
	public void setFill(boolean fill) { __fill = fill; }

	/**
	 * Graphics2DコンテキストのStrokeを取得する
	 * @return 	Graphics2DコンテキストのStroke
	 */
	public Stroke getStroke() { return __stroke; }

	/**
	 * Graphics2DコンテキストのStrokeを設定する
	 * @param	s	Graphics2DコンテキストのStroke
	 */
	public void setStroke(Stroke s) { __stroke = s; }


	/**
	 * 図形種別:線（Type_Line）のインスタンスを生成する。
	 * @return JFigureインスタンス
	 */
	public static JFigure makeLine(int x, int y, int x2, int y2)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Line;
		fig.xPoints = new int[2]; fig.xPoints[0] = x; fig.xPoints[1] = x2;
		fig.yPoints = new int[2]; fig.yPoints[0] = y; fig.yPoints[1] = y2;
		return fig;
	}

	/**
	 * 図形種別:四角形（Type_Rectangle）のインスタンスを生成する。
	 * @param	x		矩形のx座標
	 * @param	y		矩形のy座標
	 * @param	width	矩形の幅
	 * @param	height	矩形の高さ
	 * @param	fill	矩形を塗りつぶすか否か
	 * @return JFigureインスタンス
	 */
	public static JFigure makeRect(int x, int y, int width, int height, boolean fill)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Rectangle;
		fig.setFill(fill);
		fig.xPoints = new int[2]; fig.xPoints[0] = x; fig.xPoints[1] = width;
		fig.yPoints = new int[2]; fig.yPoints[0] = y; fig.yPoints[1] = height;
		return fig;
	}

	/**
	 * 図形種別:丸いコーナーつき四角形（Type_RoundRectangle）のインスタンスを生成する。
	 * @param	x		矩形のx座標
	 * @param	y		矩形のy座標
	 * @param	width	矩形の幅
	 * @param	height	矩形の高さ
	 * @param	arcWidth	4隅の弧の水平方向の直径
	 * @param	arcHeight	4隅の弧の垂直方向の直径
	 * @param	fill	矩形を塗りつぶすか否か
	 * @return JFigureインスタンス
	 */
	public static JFigure makeRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, boolean fill)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_RoundRectangle;
		fig.setFill(fill);
		fig.xPoints = new int[3]; fig.xPoints[0] = x; fig.xPoints[1] = width;	fig.xPoints[2] = arcWidth;
		fig.yPoints = new int[3]; fig.yPoints[0] = y; fig.yPoints[1] = height;	fig.yPoints[2] = arcHeight;
		return fig;
	}

	/**
	 * 図形種別:円（Type_Arc）のインスタンスを生成する。
	 * @param	x		矩形の左上隅のx座標
	 * @param	y		矩形の左上隅のy座標
	 * @param	width	矩形の幅
	 * @param	height	矩形の高さ
	 * @param	startAngle	開始角度
	 * @param	arcAngle	開始角度に対する弧の展開角度
	 * @param	fill	矩形を塗りつぶすか否か
	 * @return JFigureインスタンス
	 */
	public static JFigure makeArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean fill)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Arc;
		fig.setFill(fill);
		fig.xPoints = new int[3]; fig.xPoints[0] = x; fig.xPoints[1] = width;	fig.xPoints[2] = startAngle;
		fig.yPoints = new int[3]; fig.yPoints[0] = y; fig.yPoints[1] = height;	fig.yPoints[2] = arcAngle;
		return fig;
	}

	/**
	 * 図形種別:楕円（Type_Oval）のインスタンスを生成する。
	 * @param	x		矩形のx座標
	 * @param	y		矩形のy座標
	 * @param	width	矩形の幅
	 * @param	height	矩形の高さ
	 * @param	fill	矩形を塗りつぶすか否か
	 * @return JFigureインスタンス
	 */
	public static JFigure makeOval(int x, int y, int width, int height, boolean fill)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Oval;
		fig.setFill(fill);
		fig.xPoints = new int[2]; fig.xPoints[0] = x; fig.xPoints[1] = width;
		fig.yPoints = new int[2]; fig.yPoints[0] = y; fig.yPoints[1] = height;
		return fig;
	}

	/**
	 * 図形種別:連続的につながった線（Type_Polyline）のインスタンスを生成する。
	 * @param	x		矩形のx座標配列
	 * @param	y		矩形のy座標配列
	 * @param	n		点の総数
	 * @return JFigureインスタンス
	 */
	public static JFigure makePolyline(int[] x, int[] y, int n)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Polyline;
		fig.xPoints = x; 
		fig.yPoints = y;
		fig.pointNum = n;
		return fig;
	}

	/**
	 * 図形種別:多角形（Type_Polygon）のインスタンスを生成する。
	 * @param	x		矩形のx座標配列
	 * @param	y		矩形のy座標配列
	 * @param	n		点の総数
	 * @param	fill	矩形を塗りつぶすか否か
	 * @return JFigureインスタンス
	 */
	public static JFigure makePolygon(int[] x, int[] y, int n, boolean fill)
	{
		JFigure fig = new JFigure();
		fig.__type = Type_Polygon;
		fig.setFill(fill);
		fig.xPoints = x; 
		fig.yPoints = y;
		fig.pointNum = n;
		return fig;
	}


	/**
	 * paintComponent/paintChildrenメンバ関数のオーバライド関数からコールされる描画関数。本メンバ関数によって画面上の表示処理が実現されます。
	 * @param	gr	グラフィックス
	 * @since   UDC1.2
	 */
	protected void paintFigure(Graphics gr)
	{
		Graphics2D g = (Graphics2D)gr;

		Stroke src = g.getStroke();
		if (__stroke == null) { __stroke = src; }
		else 				  { g.setStroke(__stroke); }

		switch(__type) {
		case Type_Line: {
			g.drawLine(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
			} break;
		case Type_Rectangle: {
			if (__fill) { g.fillRect(xPoints[0], yPoints[0], xPoints[1], yPoints[1]); }
			else 		{ g.drawRect(xPoints[0], yPoints[0], xPoints[1], yPoints[1]); }
			} break;
		case Type_RoundRectangle: {
			if (__fill) { g.fillRoundRect(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]); }
			else 		{ g.drawRoundRect(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]); }
			} break;
		case Type_Arc: {
			if (__fill) { g.fillArc(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]); }
			else 		{ g.drawArc(xPoints[0], yPoints[0], xPoints[1], yPoints[1], xPoints[2], yPoints[2]); }
			} break;
		case Type_Oval: {
			if (__fill) { g.fillOval(xPoints[0], yPoints[0], xPoints[1], yPoints[1]); }
			else 		{ g.drawOval(xPoints[0], yPoints[0], xPoints[1], yPoints[1]); }
			} break;
		case Type_Polyline: {
			g.drawPolyline(xPoints, yPoints, pointNum);
			} break;
		case Type_Polygon: {
			if (__fill) { g.fillPolygon(xPoints, yPoints, pointNum); }
			else 		{ g.drawPolygon(xPoints, yPoints, pointNum); }
			} break;
		}

		g.setStroke(src);
	}

	/**
	 * paintComponentメンバ関数のオーバライド。本メンバ関数によって画面上の表示処理が実現されます。
	 * @param	gr	グラフィックス
	 * @since   UDC1.2
	 */
	protected void paintComponent(Graphics gr)
	{
		super.paintComponent(gr);
		paintFigure(gr);
	}
}

