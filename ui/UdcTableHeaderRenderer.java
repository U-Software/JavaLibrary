/* *********************************************************************
 * This source code is made by UGBView
 * 
 * Copyright 2004-2005 U-Software, Inc. All rights reserved.
 * U-Software PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.print.*;


/**
 * テーブルヘッダのCellRendererクラス。本Rendererクラスを使用するとヘッダ行を改行することができる。
 *
 * @author  Takayuki Uchida
 * @version 1.22, 31 Aug 2007
 * @since   UDC1.22
 */
public class UdcTableHeaderRenderer extends JTextArea implements TableCellRenderer
{
	private int headerRow = 0;

	/**
 	 * コンストラクタ
	 *	@param	row	ヘッダの表示行数
     */
	public UdcTableHeaderRenderer(int row)
	{
		super();
		headerRow = row;
		setRows(headerRow);
		setLineWrap(true);
		setEditable(false);
		// setPreferredSizeをしておかないとJTextArea側ではなくJTable側で自動的にrowに関係なくHeightを計算してしまうため、最低値で設定しておく
		setPreferredSize(new Dimension(1,1));
	}

	/**
 	 * getTableCellRendererComponentのオーバーライドインタフェース
	 *	@param	tbl	[in] テーブル
	 *	@param	val	[in] 設定値
	 *	@param	isSelected [in] 対象row/colの選択状態
	 *	@param	hasFocus [in] フォーカス
	 *	@param	row	[in] 行
	 *	@param	col	[in] カラム
	 *	@return 当該セルの情報
 	 */
	public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col)
	{
		if (tbl != null) {
			JTableHeader header = tbl.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}
		setText((val==null) ? "" : val.toString());
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		return this;
	}
}
