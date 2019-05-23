/* *********************************************************************
 * @(#)JCalendar.java 1.2, 10 Mar 2006
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
 * カレンダを表示するためのコンポーネント。本クラスはBorderLayoutのJPanel上にカレンダ
 * を表示するための、Label/Button等を配した複合パネルです。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JCalendar extends JPanel 
{
	/**
	 * カレンダ表示言語：日本語。
 	 * @since   UDC1.2
	 */
	final public static int		Type_Japanese	= 0;

	/**
	 * カレンダ表示言語：英語。
 	 * @since   UDC1.2
	 */
	final public static int		Type_English	= 1;

	/**
	 * 月の日数テーブル。
 	 * @since   UDC1.2
	 */
	final static int 	__monthDays[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	/**
	 * 年の表示文字テーブル。
 	 * @since   UDC1.2
	 */
	final static String __yearLb[] 	= { "年", "."};	

	/**
	 * 月の表示文字テーブル。
 	 * @since   UDC1.2
	 */
	final static String __monthLb[][] = {
						{"1 月", "2 月", "3 月", "4 月", "5 月", "6 月", "7 月", "8 月", "9 月", "10 月", "11 月", "12 月" }, 
						{"Jan.", "Feb.", "Mar.", "Apl.", "May.", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.",  "Nov.",  "Dec"}};	

	/**
	 * 曜日の表示文字テーブル。
 	 * @since   UDC1.2
	 */
	final static String __dayLb[][] = {
						{"日" , "月" , "火" , "水" , "木" , "金" , "土"},
						{"Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat"}};

	/**
	 * カレンダ表示言語種別。
 	 * @since   UDC1.2
	 */
	int 				__strType = Type_Japanese;

	/**
	 * 表示カレンダ情報。
 	 * @since   UDC1.2
	 */
	GregorianCalendar	__cal;

	/**
	 * タイトルパネル。
 	 * @since   UDC1.2
	 */
	JPanel 	__titlePanel;

	/**
	 * 年ラベル。
 	 * @since   UDC1.2
	 */
	JLabel	__yearLabel;

	/**
	 * 月ラベル。
 	 * @since   UDC1.2
	 */
	JLabel	__monthLabel;

	/**
	 * 日ラベル。
 	 * @since   UDC1.2
	 */
	JPanel 	__dayPanel;

	/**
	 * 曜日のボタンテーブル。
 	 * @since   UDC1.2
	 */
	JButton	__dayTitle[];

	/**
	 * 日のボタンテーブル。
 	 * @since   UDC1.2
	 */
	JButton	__day[];

	/**
	 * 非表示ボタンテーブル。
 	 * @since   UDC1.2
	 */
	LinkedList __disableDay = new LinkedList();


	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.2
	 */
	public JCalendar()
	{
		super(new BorderLayout());

		__cal = new GregorianCalendar();
		__cal.set(__cal.get(Calendar.YEAR), __cal.get(Calendar.MONTH), 1);

		initTitlePanel(__cal);
		initDayPanel(__cal);

		set_defaultForeground();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	year	表示年
	 * @param	month	表示月
	 * @since	UDC1.2
	 */
	public JCalendar(int year, int month)
	{
		super(new BorderLayout());

		__cal = new GregorianCalendar();
		__cal.set(year, month, 1);

		initTitlePanel(__cal);
		initDayPanel(__cal);

		set_defaultForeground();
	}

	/**
	 * 表示年月を変更する
	 *
	 * @param	year	表示年
	 * @param	month	表示月
	 * @since	UDC1.2
	 */
	public void changeMonth(int year, int month)
	{
		removeAll();

		__cal = new GregorianCalendar();
		__cal.set(year, month, 1);

		initTitlePanel(__cal);
		initDayPanel(__cal);

		set_defaultForeground();
	}

	/**
	 * 表示言語を設定する
	 *
	 * @param	type	表示言語種別
	 * @since	UDC1.2
	 */
	public void set_stringType(int type)
	{
		__strType = Type_Japanese;
		if (type == Type_English) {
			__strType = Type_English;
		}

		__yearLabel.setText("" + __cal.get(Calendar.YEAR) + " " + __yearLb[__strType]);
		__monthLabel.setText("" + __monthLb[__strType][__cal.get(Calendar.MONTH)]);
		for (int i=0; i<__dayTitle.length; i++) {
			__dayTitle[i].setText(__dayLb[__strType][i]);
		}
	}

	/**
	 * 表示月情報を取得する
	 *
	 * @return	表示カレンダ情報
	 * @since	UDC1.2
	 */
	public GregorianCalendar	getCalendarMonth() { return __cal; }

	/**
	 * 年ラベルを取得する
	 *
	 * @return	年ラベル
	 * @since	UDC1.2
	 */
	public JLabel		getYearLabel()		{ return __yearLabel; }

	/**
	 * 月ラベルを取得する
	 *
	 * @return	月ラベル
	 * @since	UDC1.2
	 */
	public JLabel		getMonthLabel()		{ return __monthLabel; }

	/**
	 * 曜日のボタンテーブルを取得する
	 *
	 * @return	曜日のボタンテーブル
	 * @since	UDC1.2
	 */
	public JButton[]	getDayTitles()		{ return __dayTitle; }

	/**
	 * 日のボタンテーブルを取得する
	 *
	 * @return	日のボタンテーブル
	 * @since	UDC1.2
	 */
	public JButton[] 	getDateButtons() 		{ return __day; }

	/**
	 * 指定日のボタンを取得する
	 *
	 * @return	指定日のボタン
	 * @param	day		指定日
	 * @since	UDC1.2
	 */
	public JButton 		getDateButton(int day) 	{ return __day[day-1]; }

	/**
	 * 日のボタンのActionListenerを登録する
	 *
	 * @param	l	日のボタンのActionListener
	 * @since	UDC1.2
	 */
	public void addActionListener(ActionListener l)
	{
		if (__day != null) {
			for (int i=0; i<__day.length; i++) {
				__day[i].addActionListener(l);
			}
		}
	}

	/**
	 * 日のボタンのActionListenerを解除する
	 *
	 * @param	l	日のボタンのActionListener
	 * @since	UDC1.2
	 */
	public void removeActionListener(ActionListener l)
	{
		if (__day != null) {
			for (int i=0; i<__day.length; i++) {
				__day[i].removeActionListener(l);
			}
		}
	}

	/**
	 * 画面Foregroundをデフォルト色に設定する。<br>
	 * デフォルト色は、日曜日を赤、土曜日を青、その他をデフォルト色とします。
	 *
	 * @since	UDC1.2
	 */
	public void set_defaultForeground()
	{
		setSundayForeground(Color.red);
		setSaturdayForeground(Color.blue);
		setWeekdayForeground(getForeground());
	}

	/**
	 * 年/月ラベルのForeground色を取得する
	 *
	 * @return 	年/月ラベルのForeground色
	 * @since	UDC1.2
	 */
	public Color getTitleForeground()
	{
		return ((__yearLabel != null) ? __yearLabel.getForeground() : null);
	}

	/**
	 * 年/月ラベルのForeground色を設定する
	 *
	 * @param	color 	年/月ラベルのForeground色
	 * @since	UDC1.2
	 */
	public void setTitleForeground(Color color)
	{
		if (__titlePanel != null) { __titlePanel.setForeground(color); }
		if (__yearLabel != null)  { __yearLabel.setForeground(color); }
		if (__monthLabel != null) { __monthLabel.setForeground(color); }
	}

	/**
	 * 日曜日のForeground色を取得する
	 *
	 * @return	日曜日のForeground色
	 * @since	UDC1.2
	 */
	public Color getSundayForeground()
	{
		return ((__dayTitle != null) ? __dayTitle[0].getForeground() : null);
	}

	/**
	 * 日曜日のForeground色を設定する
	 *
	 * @param	sun_color	日曜日のForeground色
	 * @since	UDC1.2
	 */
	public void setSundayForeground(Color sun_color)
	{
		if (__dayTitle != null) {
			__dayTitle[0].setForeground(sun_color);
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (int i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos == 0) { __day[i].setForeground(sun_color); }
			}
		}
	}

	/**
	 * 土曜日のForeground色を取得する
	 *
	 * @return	土曜日のForeground色
	 * @since	UDC1.2
	 */
	public Color getSaturdayForeground()
	{
		return ((__dayTitle != null) ? __dayTitle[6].getForeground() : null);
	}

	/**
	 * 土曜日のForeground色を設定する
	 *
	 * @param	sat_color	土曜日のForeground色
	 * @since	UDC1.2
	 */
	public void setSaturdayForeground(Color sat_color)
	{
		if (__dayTitle != null) {
			__dayTitle[6].setForeground(sat_color);
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (int i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos == 6) { __day[i].setForeground(sat_color); }
			}
		}
	}

	/**
	 * 平日のForeground色を取得する
	 *
	 * @return	平日のForeground色
	 * @since	UDC1.2
	 */
	public Color getWeekdayForeground()
	{
		return ((__dayTitle != null) ? __dayTitle[1].getForeground() : null);
	}

	/**
	 * 平日のForeground色を設定する
	 *
	 * @param	week_color	平日のForeground色
	 * @since	UDC1.2
	 */
	public void setWeekdayForeground(Color week_color)
	{
		int i;
		if (__dayTitle != null) {
			for (i=1; i<6; i++) { __dayTitle[i].setForeground(week_color); }
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos != 0 && pos != 6) { __day[i].setForeground(week_color); }
			}
		}
	}

	/**
	 * 画面Backgroundをデフォルト色に設定する。<br>
	 * 非表示日エリアを指定の色に設定する。
	 *
	 * @param	color	Background色
	 * @since	UDC1.2
	 */
	public void setBackground(Color color)
	{
		super.setBackground(color);

		if (__disableDay != null) {
			JButton bt;
			for (int i=0; i<__disableDay.size(); i++) {
				bt = (JButton)__disableDay.get(i);
				bt.setBackground(color);
			}
		}
	}

	/**
	 * 年/月ラベルのBackground色を取得する
	 *
	 * @return 	年/月ラベルのBackground色
	 * @since	UDC1.2
	 */
	public Color getTitleBackground()
	{
		return ((__yearLabel != null) ? __yearLabel.getBackground() : null);
	}

	/**
	 * 年/月ラベルのBackground色を設定する
	 *
	 * @param	color 	年/月ラベルのBackground色
	 * @since	UDC1.2
	 */
	public void setTitleBackground(Color color)
	{
		if (__titlePanel != null) { __titlePanel.setBackground(color); }
		if (__yearLabel != null)  { __yearLabel.setBackground(color); }
		if (__monthLabel != null) { __monthLabel.setBackground(color); }
	}

	/**
	 * 日曜日のBackground色を取得する
	 *
	 * @return	日曜日のBackground色
	 * @since	UDC1.2
	 */
	public Color getSundayBackground()
	{
		return ((__dayTitle != null) ? __dayTitle[0].getBackground() : null);
	}
	
	/**
	 * 日曜日のBackground色を設定する
	 *
	 * @param	sun_color	日曜日のBackground色
	 * @since	UDC1.2
	 */
	public void setSundayBackground(Color sun_color)
	{
		if (__dayTitle != null) {
			__dayTitle[0].setBackground(sun_color);
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (int i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos == 0) { __day[i].setBackground(sun_color); }
			}
		}
	}

	/**
	 * 土曜日のBackground色を取得する
	 *
	 * @return	土曜日のBackground色
	 * @since	UDC1.2
	 */
	public Color getSaturdayBackground()
	{
		return ((__dayTitle != null) ? __dayTitle[6].getBackground() : null);
	}

	/**
	 * 土曜日のBackground色を設定する
	 *
	 * @param	sat_color	土曜日のBackground色
	 * @since	UDC1.2
	 */
	public void setSaturdayBackground(Color sat_color)
	{
		if (__dayTitle != null) {
			__dayTitle[6].setBackground(sat_color);
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (int i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos == 6) { __day[i].setBackground(sat_color); }
			}
		}
	}

	/**
	 * 平日のBackground色を取得する
	 *
	 * @return	土曜日のBackground色
	 * @since	UDC1.2
	 */
	public Color getWeekdayBackground()
	{
		return ((__dayTitle != null) ? __dayTitle[1].getBackground() : null);
	}

	/**
	 * 平日のBackground色を設定する
	 *
	 * @param	week_color	平日のBackground色
	 * @since	UDC1.2
	 */
	public void setWeekdayBackground(Color week_color)
	{
		int i;
		if (__dayTitle != null) {
			for (i=1; i<6; i++) { __dayTitle[i].setBackground(week_color); }
		}
		if (__day != null) {
			int pos, spos = __cal.get(Calendar.DAY_OF_WEEK) - 1;
			for (i=0; i<__day.length; i++) {
				pos = (spos + i) % 7;
				if (pos != 0 && pos != 6) { __day[i].setBackground(week_color); }
			}
		}
	}

	/**
	 * 年/月ラベルのフォントを取得する
	 *
	 * @return 	年/月ラベルのフォント
	 * @since	UDC1.2
	 */
	public Font getTitleFont()
	{
		return ((__yearLabel != null) ? __yearLabel.getFont() : null);
	}

	/**
	 * 年/月ラベルのフォントを設定する
	 *
	 * @param	font 	年/月ラベルのフォント
	 * @since	UDC1.2
	 */
	public void setTitleFont(Font font)
	{
		if (__titlePanel != null) { __titlePanel.setFont(font); }
		if (__yearLabel != null)  { __yearLabel.setFont(font); }
		if (__monthLabel != null) { __monthLabel.setFont(font); }
	}

	/**
	 *  曜日/日付のフォントを取得する
	 *
	 * @return 	曜日/日付のフォント
	 * @since	UDC1.2
	 */
	public Font getCalendarFont()
	{
		return ((__dayTitle != null) ? __dayTitle[0].getFont() : null);
	}

	/**
	 *  曜日/日付のフォントを設定する
	 *
	 * @param	font 	曜日/日付のフォント
	 * @since	UDC1.2
	 */
	public void setCalendarFont(Font font)
	{
		int i;
		if (__dayTitle != null) {
			for (i=0; i<__dayTitle.length; i++) { __dayTitle[i].setFont(font); }
		}
		if (__day != null) {
			for (i=0; i<__day.length; i++) { __day[i].setFont(font); }
		}
	}

	/**
	 *  背景色を有効にするか否かを設定します。本設定は、Componentを構成する全てに設定されます。<br>
	 *  メンバ関数内部ではrepaintを行なっていません。
	 *
	 * @param	opaque	背景色を有効にするか否か
	 * @since	UDC1.2
	 */
	public void setOpaque(boolean opaque)
	{
		super.setOpaque(opaque);

		int i;
		if (__titlePanel != null) { __titlePanel.setOpaque(opaque); }

		if (__yearLabel != null)  { __yearLabel.setOpaque(opaque); }
		if (__monthLabel != null) { __monthLabel.setOpaque(opaque); }

		if (__dayTitle != null) {
			for (i=0; i<__dayTitle.length; i++) { __dayTitle[i].setOpaque(opaque); }
		}
		if (__day != null) {
			for (i=0; i<__day.length; i++) { __day[i].setOpaque(opaque); }
		}
		if (__disableDay != null) {
			for (i=0; i<__disableDay.size(); i++) { ((JComponent)__disableDay.get(i)).setOpaque(opaque); }
		}
	}

	/**
	 *  指定月の日数を取得する
	 *
	 * @param	month 	指定月(0-11)
	 * @since	UDC1.2
	 */
	int get_monthDays(int month)
	{
		int year = __cal.get(Calendar.YEAR);
		int monthday = __monthDays[month];		
		
		if ((month+1) == 2) {
			if ((year%4) == 0) {
				monthday ++;
				if ((year%100) == 0 && (year%400) != 0) {
					monthday --;
				}
			}
		}
		return monthday;				
	}

	/**
	 *  タイトル部のComponentを生成・初期化する
	 *
	 * @param	cal		表示月の１日指定のカレンダ
	 * @since	UDC1.2
	 */
	void initTitlePanel(GregorianCalendar cal)
	{
		int i;

		__titlePanel = new JPanel(new BorderLayout());

		JPanel yearmonthPanel = new JPanel();
		yearmonthPanel.setOpaque(false);
		__yearLabel = new JLabel("" + cal.get(Calendar.YEAR) + " " + __yearLb[__strType]);
		yearmonthPanel.add(__yearLabel);
		__monthLabel = new JLabel("" + __monthLb[__strType][cal.get(Calendar.MONTH)]);
		yearmonthPanel.add(__monthLabel);

		__titlePanel.add(yearmonthPanel, BorderLayout.CENTER);

		add(__titlePanel, BorderLayout.NORTH);
	}

	/**
	 *  カレンダ部(曜日/日付)のComponentを生成・初期化する
	 *
	 * @param	cal		表示月の１日指定のカレンダ
	 * @since	UDC1.2
	 */
	void initDayPanel(GregorianCalendar cal)
	{
		int i;
		int month = cal.get(Calendar.MONTH) + 1;	
		int monthday = get_monthDays(month-1);
		int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int row = (monthday + day) / 7;
		if (((monthday + day) % 7) != 0) { row ++; }

		__disableDay.clear();

		__dayPanel = new JPanel(new BorderLayout());
		__dayPanel.setOpaque(false);

		/* day title */
		JPanel caltitlePanel = new JPanel(new GridLayout(1, 7));
		caltitlePanel.setOpaque(false);
		__dayTitle = new JButton[7];
		for (i=0; i<7; i++) {
			__dayTitle[i] = new JButton(__dayLb[__strType][i]);
			__dayTitle[i].setMargin(new Insets(1,1,1,1));
			caltitlePanel.add(__dayTitle[i]);
		}
		__dayPanel.add(caltitlePanel, BorderLayout.NORTH);

		/* date(s) */
		JPanel calPanel = new JPanel(new GridLayout(row, 7));
		calPanel.setOpaque(false);
		JButton disablebt;
		initDayElem(month);
		for (i=0; i<day; i++) {
			disablebt = getDisableDayButton();
			calPanel.add(disablebt);
			__disableDay.add(disablebt);
		}
		for (i=day; i<(day+monthday); i++) {
			calPanel.add(__day[i-day]);
		}
		for (i=day+monthday; i<(row*7); i++) {
			disablebt = getDisableDayButton();
			calPanel.add(disablebt);
			__disableDay.add(disablebt);
		}
		__dayPanel.add(calPanel, BorderLayout.CENTER);
		
		//add(__dayPanel, BorderLayout.CENTER);

		JPanel __tmpPanel = new JPanel(new BorderLayout());
		__tmpPanel.add(__dayPanel, BorderLayout.CENTER);
		add(__tmpPanel, BorderLayout.CENTER);
	}

	/**
	 *  カレンダ部の日付のComponentのみを生成・初期化する
	 *
	 * @param	month	表示月
	 * @since	UDC1.2
	 */
	void initDayElem(int month)
	{
		int monthday = get_monthDays(month-1);
		__day = new JButton[monthday];
		for (int i=0; i<monthday; i++) {
			__day[i] = new JButton("" + (i+1));
			__day[i].setMargin(new Insets(1,1,1,1));
			__day[i].setVerticalAlignment(SwingConstants.TOP);
			__day[i].setVerticalTextPosition(SwingConstants.TOP);
			__day[i].setHorizontalTextPosition(SwingConstants.CENTER);
		}
	}

	/**
	 *  カレンダ部はN行M列のテーブル構成です。この時、テーブル中には日付がないエリアも存在し、
	 *  これを埋めるためのComponent情報として、Disable状態のJButtonを生成・設定しています。この
	 *  ボタンを取得します。
	 *
	 * @return	非表示状態の日付エリアのボタン
	 * @since	UDC1.2
	 */
	JButton getDisableDayButton()
	{
		JButton disableDay = new JButton("");
		disableDay.setEnabled(false);
		return disableDay;
	}

	/**
	 * paintChildrenのオーバライド関数。<br>
	 *
	 * @param	gr		グラフィックス
 	 * @since   UDC1.2
	 */
	protected void paintChildren(Graphics gr)
	{
		super.paintChildren(gr);

		Dimension __sz = __dayPanel.getSize(new Dimension());
		double __st = (__sz.getWidth() % 7.00) / 2.00;
		Point __np = new Point();
		__np.setLocation(__st, 0.00);
		__dayPanel.setLocation(__np);
	}
}
