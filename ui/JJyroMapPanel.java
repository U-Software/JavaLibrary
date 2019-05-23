/* *********************************************************************
 * @(#)JJyroMapPanel.java 1.0, 1 Mar 2008
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
 * ジャイロマップを表示するためのコンポーネント。<br>
 * 本クラスはGridLayoutのJPanel上各項目を一定の大きさで任意の値に対応する色付けをして表示する。<br>
 * 各項目の間隔等については、本クラスのLayoutManager(GridLayout)を取得してユーザ側で設定してください。
 * また各要素の配置上の空きはGridLayoutを考慮してき空きと目視できるJJyroMapElemをパディングすることで実現してください。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 1 Mar 2008
 * @since   UDC1.3
 */
public class JJyroMapPanel extends JPanel 
{
	/**
	 * 各項目要素のハッシュリスト
 	 * @since UDC1.3
	 */
	Hashtable __elmList;


	/**
	 * コンストラクタ
	 *
	 * @since UDC1.3
	 */
	public JJyroMapPanel(int rows, int cols)
	{
		super(new GridLayout(rows, cols));
		__elmList = new Hashtable();
	}

	/**
	 * 項目要素を取得する。
	 *
	 * @return 項目要素
	 * @param id	項目要素ID
	 * @since UDC1.3
	 */
	public JJyroMapElem getJyroMapElm(String id)
	{
		return (JJyroMapElem)__elmList.get(id);
	}

	/**
	 * 項目要素リストを取得する。
	 *
	 * @return 項目要素リスト
	 * @since UDC1.3
	 */
	public Vector getJyroMapElmList()
	{
		Vector list = new Vector();
		Enumeration elist = __elmList.elements();
		while (elist.hasMoreElements()) {
			list.add(elist.nextElement());
		}
		return list;
	}

	/**
	 * 項目要素を追加する。マップ上の追加位置はGridLayoutに従います。<br>
	 * 意図した位置に配置する場合、本関数にて登録順をユーザにて制御する必要があります。
	 *
	 * @return 追加した項目要素
	 * @param elm	項目要素
	 * @since UDC1.3
	 */
	public JJyroMapElem addJyroMapElm(JJyroMapElem elm)
	{
		if (__elmList.get(elm.__id) != null) {
			return null;
		}
		__elmList.put(elm.__id, elm);
		elm.setBackground(getBackground());
		elm.setForeground(getForeground());
		add(elm);
		return elm;	
	}

	/**
	 * 項目要素を削除する。項目要素を削除するとマップ上の要素配置がGridLayoutに従って変更されます。<br>
	 *
	 * @return 削除した項目要素
	 * @param id	項目要素ID
	 * @since UDC1.3
	 */
	public JJyroMapElem removeJyroMapElm(String id)
	{
		JJyroMapElem elm = (JJyroMapElem)__elmList.get(id);
		if (elm != null) {
			__elmList.remove(elm);
		}
		remove(elm);
		return elm;
	}

	/**
	 * 項目要素を全て削除する。
	 *
	 * @since UDC1.3
	 */
	public void resetJyroMapElm()
	{
		__elmList.clear();
		removeAll();
	}
}

