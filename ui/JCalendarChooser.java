/* *********************************************************************
 * This source code is made by Gui Builder
 * 
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.calendar;

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

/**
 * カレンダChooserを表示するためのコンポーネント。本クラスはBorderLayoutのJPanel上に
 * カレンダChooserを表示するための、Label、Button等を配備したパネルです。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JCalendarChooser extends JPanel implements ActionListener
{
	/**
	 * 日付選択のためのカレンダ・コンポーネント
	 * @since	UDC1.2
	 */
	JCalendar	__calendar = null;

	/**
	 * 前月、次月表示のための制御パネル
	 * @since	UDC1.2
	 */
	JPanel		__ctlPanel;

	/**
	 * 次月表示のためのボタン
	 * @since	UDC1.2
	 */
	JButton		__nextBt;

	/**
	 * 前月表示のためのボタン
	 * @since	UDC1.2
	 */
	JButton		__prevBt;

	/**
	 * 現在選択している日付の表示エリアのラベル
	 * @since	UDC1.2
	 */
	JLabel		__curLb;

	/**
	 * 現在選択している日付の表示エリア
	 * @since	UDC1.2
	 */
	JTextField	__curText;
	
	/**
	 * 選択した日付情報の格納エリア
	 * @since	UDC1.2
	 */
	GregorianCalendar	__curday = null;


	/**
	 * コンストラクタ。<br>
	 * 現在月が表示されます。
	 *
	 * @since	UDC1.2
	 */
	public JCalendarChooser()
	{
		super(new BorderLayout());

		refresh(0, 0);
	}

	/**
	 * コンストラクタ。<br>
	 * 指定月が表示されます。
	 *
	 * @param	year	表示する年
	 * @param	month	表示する月
	 * @since	UDC1.2
	 */
	public JCalendarChooser(int year, int month)
	{
		super(new BorderLayout());

		refresh(year, month);
	}

	/**
	 * 選択した日付情報を取得する。
	 *
	 * @return	選択した日付情報
	 * @since	UDC1.2
	 */
	public GregorianCalendar getDate() { return __curday; }

	/**
	 * 本Chooserの表示月を設定する。year/monthに０を指定した場合、現在月が表示されます。<br>
	 * 本メンバ関数を実行すると、選択した日付情報はクリアされます。
	 *
	 * @param	year	表示する年
	 * @param	year	表示する月
	 * @since	UDC1.2
	 */
	void refresh(int year, int month)
	{
		boolean flag = false;
		/* caneldar */
		if (__calendar != null) {
			remove(__calendar);
			flag = true;
		}
		if (year != 0) {
			__calendar = new JCalendar(year, month);
		} else {
			__calendar = new JCalendar();
		}
		add(__calendar, BorderLayout.CENTER);
		__calendar.addActionListener(this);

		if (!flag) {
			__ctlPanel = new JPanel();
			/* current day */
			__curLb = new JLabel("Date");
			__ctlPanel.add(__curLb);
			__curText = new JTextField(10);	
			__curText.setEditable(false);
			__ctlPanel.add(__curText);
			/* next / prev Button */
			__prevBt = new JButton("<<");
			__prevBt.setMargin(new Insets(1,1,1,1));
			__prevBt.addActionListener(this);	
			__ctlPanel.add(__prevBt);
			__nextBt = new JButton(">>");
			__nextBt.setMargin(new Insets(1,1,1,1));
			__nextBt.addActionListener(this);	
			__ctlPanel.add(__nextBt);

			add(__ctlPanel, BorderLayout.SOUTH);
		}
		__curText.setText("");
		__curday = null;
	}

	/**
	 * 現在表示されている月の翌月にカレンダChooserを初期化します。
	 * 初期化すると選択した日付はクリアされます。<br>
	 * 本メンバ関数は、次月表示ボタンを押下された場合に実行されます。
	 *
	 * @param	e 	アクションイベント
	 * @since	UDC1.2
	 */
	void actionPerformed_nextMonth(ActionEvent e)
	{
		GregorianCalendar d = __calendar.getCalendarMonth();	
		
		int year = d.get(Calendar.YEAR);
		int month = d.get(Calendar.MONTH) + 1;
		if (month >= 12) {
			year ++;
			month = 0;
		}
		refresh(year, month);
		validate();
		repaint();
	}

	/**
	 * 現在表示されている月の前月にカレンダChooserを初期化します。
	 * 初期化すると選択した日付はクリアされます。<br>
	 * 本メンバ関数は、前月表示ボタンを押下された場合に実行されます。
	 *
	 * @param	e 	アクションイベント
	 * @since	UDC1.2
	 */
	void actionPerformed_prevMonth(ActionEvent e)
	{
		GregorianCalendar d = __calendar.getCalendarMonth();	
		
		int year = d.get(Calendar.YEAR);
		int month = d.get(Calendar.MONTH) - 1;
		if (month < 0) {
			year --;
			month = 11;
		}
		refresh(year, month);
		validate();
		repaint();
	}

	/**
	 * ActionListener処理で、前月表示、次月表示、日付選択のボタンが押下された場合に
	 * 自動的に呼び出されます。
	 *
	 * @param	e 	アクションイベント
	 * @since	UDC1.2
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == __nextBt) {
			actionPerformed_nextMonth(e);

		} else if (e.getSource() == __prevBt) {
			actionPerformed_prevMonth(e);

		} else {
			JButton dayBt[] = __calendar.getDateButtons();
			for (int i=0; i<dayBt.length; i++) {
				if (e.getSource() == dayBt[i]) {
					GregorianCalendar tmp = __calendar.getCalendarMonth();
					__curday = new GregorianCalendar(tmp.get(Calendar.YEAR), tmp.get(Calendar.MONTH), (i+1), 0, 0, 0);
					__curText.setText("" + __curday.get(Calendar.YEAR) + " " + (__curday.get(Calendar.MONTH)+1) + "/" + __curday.get(Calendar.DATE));
					break;
				}
			}
		}
	}
}
