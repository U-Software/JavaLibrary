/* *********************************************************************
 * @(#)JChartTarget.java 1.0, 31 Dec 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.chart;

import java.util.*;
import java.text.*;
import java.awt.*;

/**
 *  チャート情報。本情報はJChartクラス（プロット情報）を集合体として管理し、本インスタンスで一つの折れ線情報を構成します。 
 *
 * @author  Takayuki Uchida
 * @version 1.2, 31 Dec 2006
 * @since   UDC1.2
 */
public class JChartTarget
{
	/**
	 * グラフの情報名。
	 * @since   UDC1.21
	 */
	String		targetName;

	/**
	 * グラフ種別。
	 * @since   UDC1.21
	 */
	int			chartType = JLineChart.ChartType_Line;

	/**
	 * 折れ線グラフのプロット種別。
	 * @since   UDC1.21
	 */
	int			protType = JLineChart.ProtType_None;

	/**
	 * 棒グラフの棒幅。
	 * @since   UDC1.21
	 */
	int			barWidth = 5;

	/**
	 * グラフの線色。 
	 * @since   UDC1.21
	 */
	Color		graphColor = null;

	/**
	 * グラフの線の太さ。
	 * @since   UDC1.21
	 */
	Stroke		graphStroke = null;

	/**
	 * グラフのプロット座標のフォント。
	 * @since   UDC1.21
	 */
	Font		protFont = null;

	/**
	 * グラフのデータリスト。JChartDataのリスト。
	 * @since   UDC1.21
	 */
	Vector		dataList;

	/**
	 * X軸の最大値。
	 * @since   UDC1.21
	 */
	double		maxValX = -100000.00;

	/**
	 * X軸の最小値。
	 * @since   UDC1.21
	 */
	double		minValX = 100000.00;

	/**
	 * Y軸の最大値。
	 * @since   UDC1.21
	 */
	double		maxValY = -100000.00;

	/**
	 * X軸の最小値。
	 * @since   UDC1.21
	 */
	double		minValY = 100000.00;

	/**
	 * 棒グラフの基点値。
	 * @since   UDC1.21
	 */
	double		barBaseVal = 0.00;

	/**
	 * X座標値の文字フォーマット。
	 * @since   UDC1.22
	 */
	Format 		xFormat = null;

	/**
	 * Y座標値の文字フォーマット。
	 * @since   UDC1.22
	 */
	Format 		yFormat = null;

	/**
	 * コンストラクタ
	 *
	 * @param	name	グラフの情報名
	 * @param	type 	グラフ種別
 	 * @since   UDC1.2
	 */
	public JChartTarget(String name, int type)
	{
		targetName = name;
		setChartType(type);
		dataList = new Vector();
	}

	/**
	 * グラフの情報名を取得する。
	 * @return グラフの情報名
	 * @since   UDC1.21
	 */
	public String 	getTargetName() { return targetName; }

	/**
	 * グラフ種別を取得する。
	 * @since   UDC1.21
	 */
	public int		getChartType() { return chartType; }

	/**
	 * グラフ種別を設定する。
	 * @param	type グラフ種別(ChartType_Line|ChartType_Bar|ChartType_Distribution)
	 * @see JLineChart
	 * @since   UDC1.21
	 */
	public void		setChartType(int type) { chartType = type; }

	/**
	 * グラフのプロット種別を取得する。
	 * @return グラフのプロット種別
	 * @see JLineChart
	 * @since   UDC1.21
	 */
	public int		getProtType() { return protType; }

	/**
	 * グラフのプロット種別を設定する。
	 * @param	type グラフのプロット種別(ProtType_None|ProtType_Arc|ProtType_Rect)
	 * @since   UDC1.21
	 */
	public void		setProtType(int type) { protType = type; }

	/**
	 * 棒グラフの棒幅を取得する。
	 * @return 棒グラフの棒幅
	 * @since   UDC1.21
	 */
	public int	getBarWidth() { return barWidth; }

	/**
	 * 棒グラフの棒幅を設定する。
	 * @param wd 棒グラフの棒幅
	 * @since   UDC1.21
	 */
	public void	setBarWidth(int wd) { barWidth = wd; }

	/**
	 * グラフの線色を取得する。
	 * @return グラフの線色
	 * @since   UDC1.21
	 */
	public Color	getGraphColor() { return graphColor; }

	/**
	 * グラフの線色を設定する。
	 * @param	color グラフの線色
	 * @since   UDC1.21
	 */
	public void		setGraphColor(Color color) { graphColor = color; }

	/**
	 * グラフの線のStrokeを取得する。
	 * @return グラフの線のStroke
	 * @since   UDC1.21
	 */
	public Stroke	getGraphStroke() { return graphStroke; }

	/**
	 * グラフの線のStrokeを設定する。
	 * @param	stroke グラフの線のStroke
	 * @since   UDC1.21
	 */
	public void	setGraphStroke(Stroke stroke) { graphStroke = stroke; }

	/**
	 * グラフのプロット座標のフォントを取得する。
	 * @return グラフのプロット座標のフォント
	 * @since   UDC1.21
	 */
	public Font		getProtFont() { return protFont; }

	/**
	 * グラフのプロット座標のフォントを設定する。
	 * @param	font グラフのプロット座標のフォント
	 * @since   UDC1.21
	 */
	public void		setProtFont(Font font) { protFont = font; }

	/**
	 * X座標値の文字のFormatを取得する。
	 * @return X座標値の文字のFormat
	 * @since   UDC1.22
	 */
	public Format	getXFormat() { return xFormat; }

	/**
	 * X座標値の文字のFormatを設定する。
	 * @param fmt X座標値の文字のFormat
	 * @since   UDC1.22
	 */
	public void	setXFormat(Format fmt) { xFormat = fmt; }

	/**
	 * Y座標値の文字のFormatを取得する。
	 * @return Y座標値の文字のFormat
	 * @since   UDC1.22
	 */
	public Format	getYFormat() { return yFormat; }

	/**
	 * Y座標値の文字のFormatを設定する。
	 * @param fmt Y座標値の文字のFormat
	 * @since   UDC1.22
	 */
	public void	setYFormat(Format fmt) { yFormat = fmt; }

	/**
	 * プロットデータの文字列を取得する。
	 * @return プロットデータの文字列
	 * @since   UDC1.22
	 */
	public String getProtValue(JChartData data)
	{
		StringBuffer buf = new StringBuffer();
		if (xFormat != null) {
			buf.append(xFormat.format(new Double(data.getX())));
		} else {
			buf.append("" + data.getX());
		}
		buf.append(",");
		if (yFormat != null) {
			buf.append(yFormat.format(new Double(data.getY())));
		} else {
			buf.append("" + data.getY());
		}
		return buf.toString();
	}


	/**
	 * グラフのデータリストを取得する。
	 * @return グラフのデータリスト
	 * @since   UDC1.21
	 */
	public Vector	getDataList() { return dataList; }

	/**
	 * グラフのデータリストにデータを追加する。
	 * @param	data グラフのデータ
	 * @since   UDC1.21
	 */
	public void 	addData(JChartData data)
	{
		if (maxValX < data.getX()) { maxValX = data.getX(); }
		if (minValX > data.getX()) { minValX = data.getX(); }
		if (maxValY < data.getY()) { maxValY = data.getY(); }
		if (minValY > data.getY()) { minValY = data.getY(); }

		dataList.add(data);
		Collections.sort(dataList);
	}

	/**
	 * グラフのデータリストをクリアする。
	 * @since   UDC1.21
	 */
	public void 	resetData()
	{
		dataList.clear();
	}

	/**
	 * X軸の最大値を取得する。
	 * @return X軸の最大値
	 * @since   UDC1.21
	 */
	public double getMaxValX() { return maxValX; }

	/**
	 * X軸の最大値を設定する。
	 * @param	val X軸の最大値
	 * @since   UDC1.21
	 */
	public void setMaxValX(double val) { maxValX = val; }

	/**
	 * X軸の最小値を取得する。
	 * @return X軸の最小値
	 * @since   UDC1.21
	 */
	public double getMinValX() { return minValX; }

	/**
	 * X軸の最小値を設定する。
	 * @param	val X軸の最小値
	 * @since   UDC1.21
	 */
	public void setMinValX(double val) { minValX = val; }

	/**
	 * Y軸の最大値を取得する。
	 * @return Y軸の最大値。
	 * @since   UDC1.21
	 */
	public double getMaxValY() { return maxValY; }

	/**
	 * Y軸の最大値を設定する。
	 * @param	val Y軸の最大値
	 * @since   UDC1.21
	 */
	public void setMaxValY(double val) { maxValY = val; }

	/**
	 * Y軸の最小値を取得する。
	 * @return Y軸の最小値
	 * @since   UDC1.21
	 */
	public double getMinValY() { return minValY; }

	/**
	 * Y軸の最小値を設定する。
	 * @param	val Y軸の最小値
	 * @since   UDC1.21
	 */
	public void setMinValY(double val) { minValY = val; }

	/**
	 * 棒グラフの基点値を取得する。
	 * @return 棒グラフの基点値。
	 * @since   UDC1.21
	 */
	public double getBarBaseVal() { return barBaseVal; }

	/**
	 * 棒グラフの基点値を取得する。
	 * @param val 棒グラフの基点値。
	 * @since   UDC1.21
	 */
	public void setBarBaseVal(double val) { barBaseVal = val; }
}

