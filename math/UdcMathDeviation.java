/* *********************************************************************
 * @(#)UdcMathDeviation.java 1.0, 31 May 2008
 *
 * Copyright 2008 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.math;

import java.lang.*;
import java.util.*;
import java.io.*;


/**
 * UDC標準偏差計算クラス<br>
 *
 * @author  Takayuki Uchida
 * @version 1.0, 31 May 2008
 * @since   UDC1.23
 */
public class UdcMathDeviation
{
	/** 演算定数:偏差値の平均(50) */
	double avrDeviation = 50;

	/** 演算定数:偏差値の標準偏差(10) */
	double devDeviation = 10;

	/** 平均値(μ) */	
	double average;	

	/** 分散(σ2) */
	double dispersion;

	/** 標準偏差(σ) */
	double deviation;

	/** データ一覧(Vector&gtDouble&lt) 下位互換のためVector定義 */
	Vector datas;

	/**
	 * コンストラクタ
	 * @since 	UDC1.23
	 */
	public UdcMathDeviation()
	{
		average = 0;
		dispersion = 0;
		deviation = 0;
		datas = new Vector();
	}

	/**
	 * コンストラクタ
	 * @param	data	データ一覧
	 * @since 	UDC1.23
	 */
	public UdcMathDeviation(Vector data)
	{
		average = 0;
		dispersion = 0;
		deviation = 0;
		datas = (data!=null) ? data : new Vector();
	}

	/**
	 * 演算定数:偏差値の平均を取得する。
	 * @return 演算定数:偏差値の平均
	 * @since 	UDC1.23
	 */
	public double getAvrDeviation() { return avrDeviation; }

	/**
	 * 演算定数:偏差値の平均を設定する。設定しなければ50となる。
	 * @param	avrDev 演算定数:偏差値の平均
	 * @since 	UDC1.23
	 */
	public void setAvrDeviation(double avrDev) { avrDeviation = avrDev; }

	/**
	 * 演算定数:偏差値の標準偏差を取得する。
	 * @return 演算定数:偏差値の標準偏差
	 * @since 	UDC1.23
	 */
	public double getDevDeviation() { return devDeviation; }

	/**
	 * 演算定数:偏差値の標準偏差を設定する。設定しなければ10となる。
	 * @param	devDev	演算定数:偏差値の標準偏差
	 * @since 	UDC1.23
	 */
	public void setDevDeviation(double devDev) { devDeviation = devDev; }

	/** 
	 * 演算結果:平均値(μ)を取得する。
	 * 本値はcalcメンバ実行で値が設定される。
	 * @return 平均値
	 * @since 	UDC1.23
	 */	
	public double getAverage() { return average; }

	/** 
	 * 演算結果:分散を取得する。
	 * 本値はcalcメンバ実行で値が設定される。
	 * @return 分散
	 * @since 	UDC1.23
	 */	
	public double getDispersion() { return dispersion; }

	/** 
	 * 演算結果:標準偏差を取得する。
	 * 本値はcalcメンバ実行で値が設定される。
	 * @return 標準偏差
	 * @since 	UDC1.23
	 */	
	public double getDeviation() { return deviation; }

	/**
	 * 演算元データを取得する。
	 * @return データ一覧(Vector&gtDouble&lt)
	 * @since 	UDC1.23
	 */
	public Vector getDatas() { return datas; }


	/**
	 * 指定データの偏差値を取得する。
	 * @param	id	データの格納Index
	 */
	public double getStandardScore(int id)
	{
		if (deviation == 0) { return  avrDeviation; }
		// 偏差値(T) = 偏差値平均 + 偏差値の標準偏差 * (素点 - 平均(μ)) / 標準偏差(σ)
		double val = ((Double)datas.get(id)).doubleValue();
		return (avrDeviation + (devDeviation * ((val - average) / deviation)));
	}

	/**
	 * 平均/分散/標準偏差を計算する
	 * @return	演算結果(true:正常/false:異常)
	 * @since 	UDC1.23
	 */
	public boolean calc()
	{
		if (datas == null || datas.size() <= 0) { return false; }
		int i;
		double val, total;
		// 平均値の算出 μ = Σデータ / データ数
		total = 0;
		for (i=0; i<datas.size(); i++) {
			total += ((Double)datas.get(i)).doubleValue();
		}
		average = total / datas.size();
		// 分散の算出 σ^2 = Σ偏差^2 / データ数
		total = 0;
		for (i=0; i<datas.size(); i++) {
			val = ((Double)datas.get(i)).doubleValue() - average;
			total += (val * val);
		}
		dispersion = total / datas.size();
		// 標準偏差の算出 σ = √(σ^2)
		deviation = (dispersion != 0) ? Math.sqrt(dispersion) : 0;

		return true;
	}
}

