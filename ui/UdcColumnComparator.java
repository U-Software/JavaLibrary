/* *********************************************************************
 * @(#)UdcColumnComparator.java 1.21, 31 July 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui;

import java.lang.*;
import java.util.*;
import java.io.*;

/**
 * JTableのソートクラス。<br>
 * UGBViewによるJTableでは、DefaultTableModelが適用されます。ソートではTableModelを継承して実装されることがSunのチュートリアルにもありますが、これを簡易に実装するために本クラスを使用して、以下の実装を行うことで、ソートの実現が可能となります。<br>
 * 以下の例は、テーブルヘッダをクリックした時にソートする例です。<br><br>
 *  JTable.getTableHeader().addMouseListener(this);<br>
 *	:<br>
 *	public void mouseClicked(MouseEvent e) {<br>
 *		if (e.getSource() instanceof JTableHeader) {<br>
 *			DefaultTableModel dt = (DefaultTableModel)JTable.getModel();<br>
 *			TableColumnModel cm = ((JTableHeader)e.getSource()).getColumnModel();<br>
 *			int col = cm.getColumn(cm.getColumnIndexAtX(e.getX())).getModelIndex();<br>
 *			Collections.sort(dt.getDataVector(), new <b>UdcColumnComparator(col, true)</b>);<br>
 *			dt.fireTableDataChanged();<br>
 *			return;<br>
 *		}<br>
 *	:<br>
 *	}<br>
 *
 * @author  Takayuki Uchida
 * @version 1.21, 31 July 2006
 * @since   UDC1.2
 */
public class UdcColumnComparator implements Comparator
{
	/**
	 * テーブルカラムのインデックス
	 */
	final protected int index;

	/**
	 * ソート種別（昇順/降順）
	 */
	final protected boolean ascending;

	/**
     * コンストラクタ
     *
     * @param   index カラムID
     * @param   ascending ソート種別（昇順/降順）
     * @since   UDC1.21
     */
	public UdcColumnComparator(int index, boolean ascending)
	{
		this.index = index;
		this.ascending = ascending;
	}

	/**
     * カラム要素の比較関数
     *
     * @param   one	比較要素1
     * @param   two	比較要素2
 	 * @return	比較結果
     * @since   UDC1.21
     */
	public int compare(Object one, Object two)
	{
		if (one instanceof Vector && two instanceof Vector) {
			Object oOne = ((Vector)one).elementAt(index);
			Object oTwo = ((Vector)two).elementAt(index);
			if (oOne == null && oTwo == null) {
				return 0;
			} else if(oOne==null) {
				return ascending ? -1 :  1;
			} else if(oTwo==null) {
				return ascending ?  1 : -1;
			} else if(oOne instanceof Comparable && oTwo instanceof Comparable) {
				Comparable cOne = (Comparable)oOne;
				Comparable cTwo = (Comparable)oTwo;
				return ascending ? cOne.compareTo(cTwo) : cTwo.compareTo(cOne);
			}
		}
		return 1;
	}

	/**
     * カラム要素の比較関数
     *
     * @param   o1	比較要素1
     * @param   o2	比較要素2
 	 * @return	比較結果
     * @since   UDC1.21
     */
	public int compare(Number o1, Number o2)
	{
		double n1 = o1.doubleValue();
		double n2 = o2.doubleValue();
		if(n1 < n2) 	{ return -1; }
		else if(n1 > n2){ return 1; }
		else			{ return 0; }
	}
}

