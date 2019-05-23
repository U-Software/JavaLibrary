/* *********************************************************************
 * @(#)JLineChart.java 1.0, 31 Dec 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.chart;


/**
 *  チャートのプロット情報。本情報はJChartTargetクラスに集合体として管理され、一つの折れ線情報を構成されます。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 31 Dec 2006
 * @since   UDC1.2
 */
public class JChartData implements Comparable
{
	/**
	 * グラフのX軸の情報値。 
	 * @since   UDC1.21
	 */
	double 	x;

	/**
	 * グラフのY軸の情報値。 
	 * @since   UDC1.21
	 */
	double	y;

	/**
	 * グラフの情報要素に情報値を表示するか否か。 
	 * @since   UDC1.21
	 */
	boolean	displayProtValue;

	/**
	 * コンストラクタ
	 *
 	 * @since   UDC1.2
	 */
	public JChartData()
	{
		x = 0.00;
		y = 0.00;
		displayProtValue = false;
	}

	/**
	 * コンストラクタ
	 *
	 * @param	xdata	グラフのX軸の情報値。
	 * @param	ydata	グラフのY軸の情報値。
	 * @param	display	グラフの情報要素に情報値を表示するか否か
 	 * @since   UDC1.2
	 */
	public JChartData(double xdata, double ydata, boolean display)
	{
		x = xdata;
		y = ydata;
		displayProtValue = display;
	}

	/**
	 * グラフのX軸の情報値を取得する。 
	 * @return グラフのX軸の情報値
	 * @since   UDC1.21
	 */
	public double	getX() { return x; }

	/**
	 * グラフのX軸の情報値を設定する。
	 * @param data	グラフのX軸の情報値
	 * @since   UDC1.21
	 */
	public void		setX(double data) { x = data; }

	/**
	 * グラフのY軸の情報値を取得する。 
	 * @return グラフのY軸の情報値
	 * @since   UDC1.21
	 */
	public double	getY() { return y; }

	/**
	 * グラフのY軸の情報値を設定する。
	 * @param	data グラフのY軸の情報値
	 * @since   UDC1.21
	 */
	public void		setY(double data) { y = data; }

	/**
	 * グラフの情報要素に情報値を表示するか否かを取得する。
	 * @return グラフの情報要素に情報値を表示するか否か
	 * @since   UDC1.21
	 */
	public boolean	getDisplayProtValue() { return displayProtValue; }

	/**
	 * グラフの情報要素に情報値を表示するか否かを設定する。
	 * @param	display グラフの情報要素に情報値を表示するか否か
	 * @since   UDC1.21
	 */
	public void		setDisplayProtValue(boolean display) { displayProtValue = display; }

	/**
	 * グラフの情報要素に表示する情報値文字列を取得する。
	 * @return グラフの情報要素に表示する情報値文字列
	 * @since   UDC1.21
	 */
	public String 	getProtValue()
	{
		return "" + x + "," + y + "";
	}

	/**
	 * グラフのX軸の値を比較する。
	 * @return 0:一致/1:自インスタンスの方が大きい/-1:自インスタンスの方が小さい
	 * @since   UDC1.21
	 */
	public int compareTo(Object obj)
	{
		JChartData data = (JChartData)obj;
		if (x > data.getX()) {
			return 1;
		} else if (x < data.getX()) {
			return -1;
		}
		return 0;
	}
}

