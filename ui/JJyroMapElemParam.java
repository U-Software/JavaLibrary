/* *********************************************************************
 * @(#)JJyroMapElemParam.java 1.0, 1 Mar 2008
 *
 * Copyright 2008 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.jyro;

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


/**
 * ジャイロマップを構成する１区画の色コンポーネントのパラメータクラス。<br>
 *
 * @author  Takayuki Uchida
 * @version 1.0, 1 Mar 2008
 * @since   UDC1.3
 */
public class JJyroMapElemParam implements Comparable
{
	/**
	 * 表示色。
 	 * @since   UDC1.3
	 */
	protected Color		__color;

	/**
	 * 本値より大きい値の場合にcolorが表示される値
 	 * @since   UDC1.3
	 */
	protected double	__gtValue;
	
	/**
	 * コンストラクタ。
	 *
	 * @param color	表示色
	 * @param gtVal	本値より大きい値の場合にcolorが表示される値
 	 * @since   UDC1.3
	 */
	public JJyroMapElemParam(Color color, double gtVal)
	{
		__color = color;
		__gtValue = gtVal;
	}

	/**
	 * 表示色を取得する。
	 *
	 * @return 表示色
 	 * @since   UDC1.3
	 */
	public Color getColor() { return __color; }

	/**
	 * 本値より大きい値の場合にcolorが表示される値を取得する。
	 *
	 * return 本値より大きい値の場合にcolorが表示される値
 	 * @since   UDC1.3
	 */
	public double getGreaterThanValue() { return __gtValue; }

	/**
	 * Comparableインタフェースの比較関数。<br>
	 * __gtValueを比較対象とする。
	 *
	 * @return 比較結果
	 * @param	obj	比較対象
 	 * @since   UDC1.3
	 */
	public int compareTo(Object obj)  
	{
		JJyroMapElemParam elm = (JJyroMapElemParam)obj;
		if (elm.__gtValue == __gtValue) {
			return 0;
		}
		if (__gtValue > elm.__gtValue) {
			return 1;
		}
		return -1;
	}
}

