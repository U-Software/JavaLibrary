/* *********************************************************************
 * @(#)JJyroMapElem.java 1.0, 1 Mar 2008
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
 * ジャイロマップを構成する１区画の色コンポーネント。<br>
 *
 * @author  Takayuki Uchida
 * @version 1.0, 1 Mar 2008
 * @since   UDC1.3
 */
public class JJyroMapElem extends JLabel
{
	/**
	 * 項目要素ID
 	 * @since   UDC1.3
	 */
	protected String __id;

	/**
	 * 項目要素の値に対応したパラメータ情報リスト
 	 * @since   UDC1.3
	 */
	protected Vector __colorValueList;

	/**
	 * 要素にツールチップをつけるか否か
 	 * @since   UDC1.3
	 */
	protected boolean isToolTip=false;

	/**
	 * 項目要素の現在の値
 	 * @since   UDC1.3
	 */
	protected double __curValue;

	/**
	 * コンストラクタ
	 *
	 * @param	id 項目要素ID
 	 * @since   UDC1.3
	 */
	public JJyroMapElem(String id)
	{
		super();
		setOpaque(true);
		__id = new String(id);
		__colorValueList = new Vector();
		__curValue = 0;
	}

	/**
	 * 項目要素IDを取得する。
	 *
	 * @return 項目要素ID
 	 * @since   UDC1.3
	 */
	public String getId() { return __id; }

	/**
	 * 要素にツールチップをつけるか否かを取得する
	 * @return 要素にツールチップをつけるか否か
 	 * @since   UDC1.3
	 */
	public boolean getIsToolTip() { return isToolTip; }

	/**
	 * 要素にツールチップをつけるか否かを設定する
	 * @param flag 要素にツールチップをつけるか否か
 	 * @since   UDC1.3
	 */
	public void setIsToolTip(boolean flag) { isToolTip = flag; }

	/**
	 * 項目要素の現在値を取得する。
	 *
	 * @return 項目要素の現在値
 	 * @since   UDC1.3
	 */
	public double getCurrentValue() { return __curValue; }

	/**
	 * 項目要素の現在値を更新する。更新すると表示される項目要素の色が変化します。<br>
	 * JJyroMapのrepaintはユーザにて行う必要があります。
	 *
 	 * @since   UDC1.3
	 */
	public void setCurrentValue(double val)
	{
		__curValue = val;
		JJyroMapElemParam elm = getCurrentParam();
		if (elm != null) {
			setBackground(elm.__color);
			if (isToolTip) { setToolTipText(__id + " [" + __curValue + "]"); }
			validate();
		}
	}

	/**
	 * 現在の値に対応した項目要素のパラメータ情報を取得します。
	 *
	 * @return 現在の値に対応した項目要素のパラメータ情報
 	 * @since   UDC1.3
	 */
	public JJyroMapElemParam getCurrentParam()
	{
		JJyroMapElemParam elm = null;
		for (int i=__colorValueList.size()-1; i>=0; i--) {
			elm = (JJyroMapElemParam)__colorValueList.get(i);
			if (__curValue > elm.__gtValue) {
				return elm;			
			}	
		}
		return elm;
	}

	/**
	 * 項目要素の値に対応したパラメータ情報リスト数を取得する。
	 *
	 * @return 項目要素の値に対応したパラメータ情報リスト数
 	 * @since   UDC1.3
	 */
	public int getColorValueListSize() { return __colorValueList.size(); }

	/**
	 * 指定した順番の項目要素のパラメータ情報を取得する。
	 *
	 * @return 項目要素のパラメータ情報リスト数
	 * @param	index	項目要素のパラメータ情報リストの格納位置
 	 * @since   UDC1.3
	 */
	public JJyroMapElemParam getElemParam(int index)
	{
		if (index < 0 || __colorValueList.size() >= index) {
			return null;
		}
		return (JJyroMapElemParam)__colorValueList.get(index);
	}
	
	/**
	 * 項目要素のパラメータ情報を登録する。<br>
	 * 項目要素のパラメータ情報は、gtValueで指定した値を昇順にソートされます。
	 *
	 * @return	追加した項目要素パラメータ情報のリスト格納位置
	 * @param	gtValue	本値より大きい値の場合にcolorが表示される値
	 * @param	color	表示色
 	 * @since   UDC1.3
	 */
	public int addColorValueList(double gtValue, Color color)	
	{
		JJyroMapElemParam elm = new JJyroMapElemParam(color, gtValue);
		__colorValueList.add(elm);
		if (__colorValueList.size() > 1) {
			Collections.sort(__colorValueList);
		}
		return __colorValueList.indexOf(elm);
	}

	/**
	 * 項目要素のパラメータ情報を削除する。
	 *
	 * @param	gtValue	本値より大きい値の場合にcolorが表示される値
 	 * @since   UDC1.3
	 */
	public void removeColorValueList(double gtValue)	
	{
		JJyroMapElemParam elm;
		for (int i=0; i<__colorValueList.size(); i++) {
			elm = (JJyroMapElemParam)__colorValueList.get(i);
			if (elm.__gtValue == gtValue) {
				__colorValueList.remove(elm);
				i --;
			}
		}
		if (__colorValueList.size() > 1) {
			Collections.sort(__colorValueList);
		}
		return;
	}

	/**
	 * 項目要素のパラメータ情報を変更する。<br>
	 * 項目要素のパラメータ情報は、gtValueで指定した値を昇順にソートされます。
	 *
	 * @param	srcGtValue	現在登録してある本値より大きい値の場合にcolorが表示される値
	 * @param	gtValue	本値より大きい値の場合にcolorが表示される値
	 * @param	color	表示色
 	 * @since   UDC1.3
	 */
	public void modifyColorValutList(double srcGtValue, Color color, double gtValue)
	{
		JJyroMapElemParam elm;
		for (int i=0; i<__colorValueList.size(); i++) {
			elm = (JJyroMapElemParam)__colorValueList.get(i);
			if (elm.__gtValue == gtValue) {
				elm.__color = color;
				elm.__gtValue = gtValue;
			}
		}
		if (__colorValueList.size() > 1) {
			Collections.sort(__colorValueList);
		}
		setCurrentValue(__curValue);
		return;
	}
}

