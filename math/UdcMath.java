/* *********************************************************************
 * @(#)UdcMath.java 1.0, 20 Dec 2004
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.math;

import java.lang.*;
import java.util.*;
import java.io.*;


/**
 * UDC数学クラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 20 Dec 2004
 * @since   UDC1.2
 */
public class UdcMath
{
	/**
	 * 一様乱数発生インスタンス
	 */
	public static Random Rand = null;

	/**
	 * 一様乱数の初期化を行ないます。
	 * @param	seed 初期シード
	 * @since	UDC1.2
	 */
	final public static void init(Long seed)
	{
		if (seed == null) {
			Rand = new Random();
		} else {
			Rand = new Random(seed.longValue());
		}
	}
	
	/**
	 * 一様乱数を取得します。
	 * @return 一様乱数
	 */
	final public static double random()
	{
		if (Rand == null) {
			Rand = new Random();
		}
		return Rand.nextDouble();
	}

	/**
	 * 二項分布乱数を発生させます。
	 *
	 * @return	0.0 以上で、1.0 より小さい正の符号の付いた double値
	 * @param	probability		発生確率(0.0以上1.0以下の数値)
	 * @param	num				二項発生回数(1以上100以下の整数値)
	 * @since	UDC1.2
	 */
	public static double random_binominal(double probability, int num)
	{
		int i;
		double rd, res = 0.0, base = 1.0 / num;

		for (i=0; i<num; i++) {
			rd = random();
			if (rd < probability) {
				res += base;
			}
		}
		while (true) {
			rd = random();
			if (rd > (res-base) && rd < (res+base)) {
				return rd;
			}
		}
	}

	/**
	 * 正規分布乱数を発生させます。
	 *
	 * @return 正規分布乱数値(値範囲は、平均と標準偏差による)	
	 * @param	avr		平均
	 * @param	div		標準偏差
	 * @since	UDC1.2
	 */
	public static double random_reg(double avr, double div)
	{
		double a,b;
		while (true) {
			if ((a=random()) != 0.0f) { break; }
		}
		b = random();
		
		/* regx randam 
		 *		Math.sqrt(-2*Math.log(a)) * Math.sin(2*Math.PI*b) * div + avr;
		 * 				or 
		 *		Math.sqrt(-2*Math.log(b)) * Math.cos(2*Math.PI*a) * div + avr;
		 */
		return (Math.sqrt(-2*Math.log(a)) * Math.sin(2*Math.PI*b) * div + avr);
	}


	/**
	 * ポアソン分布 psn(x:λ)(x=0,1,2,..)の乱数を発生させます。
	 *
	 * @return ポアソン分布乱数値(値範囲は、λ(平均)による)	
	 * @param	lambda	λ
	 * @since	UDC1.2
	 */
	public static double random_poisson(double lambda)
	{
		double rnd = 0, v = 1.0;
		double limit = Math.pow(Math.E, (lambda*-1.0));

		while (true) {
			v *= random();
			if (v < limit) { break; }
			rnd += 1.0;
		}
		return rnd; 
	}

	/**
	 * 指数分布乱数を発生させます。
	 *
	 * @return 指数分布乱数値
	 * @param	lambda		平均間隔 1/λのλ値
	 * @since	UDC1.2
	 */
	public static double random_exponent(double lambda)
	{
		double a;
		while (true) {
			if ((a=random()) != 1.0f) { break; }
		}
		return (-1 * (1.0f/lambda) * Math.log(1.0f - a));
	}

	/**
	 * パレート分布乱数を発生させます。
	 * scale の値には <b>scale = {avg * (shape - 1)/shape} | {avg * (shape - 1)}</b>といったアプローチがあるため、本関数では引数はscaleとしている。
	 *
	 * @return パレート分布乱数値
	 * @param	scale	scaleパラメータ
	 * @param	shape 	shapeパラメータ(shapeは1.0より大きいこと)
	 * @since	UDC1.2
	 */
	public static double random_pareto(double scale, double shape)
	{
		// avr -> scale には幾つかのアプローチがあるので乱数計算ではこの処理
		// はサポートせず、ユーザにて実現する。
		//double scale = avg * ((shape - 1.0)/shape);
		return (scale * (1.0 / Math.pow(random(), 1.0/shape)));
	}

	/**
	 * ワイブル分布乱数を発生させます。
	 *
	 * @return ワイブル分布乱数値
	 * @param	shape 	shapeパラメータ
	 * @param	scale	scaleパラメータ
	 * @since	UDC1.2
	 */
	public static double random_rweibull(double shape, double scale)
	{
		double a;
		while (true) {
			if ((a=random()) != 0.0f) { break; }
		}
		return (scale * Math.pow(Math.log(a)*-1.0, 1.0/shape));
	}

	/**
	 * 行列演算(和)を行う。<br>
	 * 高速化のため、a/bの行列数はチェックしません。そのため行列数が異なるとExceptionが発生します。
	 *
	 * @return 演算結果
	 * @param	a	演算元行列1
	 * @param	b	演算元行列2
	 * @since	UDC1.3
	 */
	public static double[][] matrixAdd(double[][] a, double[][] b)
	{
		// 演算結果の行列を作成
		int row = (a.length > b.length) ? a.length : b.length;
		int col = (a[0].length > b[0].length) ? a[0].length : b[0].length;
		double c[][] = new double[row][col];
		// 行列の演算
		int i,j;
		for (i=0; i<row; i++) {
			for (j=0; j<col; j++) {
				c[i][j] = a[i][j] + b[i][j];
			}
		}
		return c;
	}

	/**
	 * 行列演算(積)を行う
	 * 高速化のため、a/bの行列数はチェックしません。そのため行列数が異なるとExceptionが発生します。
	 *
	 * @return 演算結果
	 * @param	a	演算元行列1
	 * @param	b	演算元行列2
	 * @since	UDC1.3
	 */
	public static double[][] matrixMultiple(double[][] a, double[][] b)
	{
		// 演算結果の行列を作成
		int row = a.length;
		int col = b[0].length;
		double c[][] = new double[row][col];
		// 行列の演算
		int i,j,k;
		for (i=0; i<row; i++) {
			for (j=0; j<col; j++) { c[i][j] = 0; }
		}
		for (i=0; i<row; i++) {
			for (j=0; j<col; j++) {
				for (k=0; k<a[i].length; k++) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return c;
	}

	/**
	 * 行列演算(逆行列)を行う。逆行列演算にはガウス・ジョルダン法を用います。
	 * 高速化のため、a/bの行列数はチェックしません。そのため行列数が異なるとExceptionが発生します。
	 * また、多次元行列の演算においてdouble型の精度によって演算が正しく行われなく見えることがあります。
	 * double型は、約±1.79769313486231570e＋308の範囲(精度は15桁)ですので、3行3列を超える行列の場合には
	 * この精度を十分に意識する必要があります。
	 *
	 * @return 演算結果
	 * @param	a	演算元行列
	 * @since	UDC1.3
	 */
	public static double[][] matrixReverse(double[][] a)
	{
		if (a.length != a[0].length) {
			return null;
		}

		int i,j;
		// 逆行列演算のための演算行列を生成
		double c[][] = new double[a.length][a.length*2];
		for (i=0; i<c.length; i++) {
			for (j=0; j<c[0].length; j++) {
				if (i < a.length && j < a.length) {
					c[i][j] = a[i][j];
				} else {
					c[i][j] = ((j-a.length) == i) ? 1 : 0;
				}
			}
		}
		for (i=0; i<c.length; i++) {
	 		// ピボット操作
			pivot(i, c);
			// 逆行列未存在チェック
			if (c[i][i] == 0) { return null; }
	 		// スウィープ操作
			sweep(i, c);
		}
		// 逆行列部を結果として抽出
		double r[][] = new double[a.length][a.length];
		for (i=0; i<a.length; i++) {
			for (j=a.length; j<c[0].length; j++) {
				r[i][j-a.length] = c[i][j];	
			}
		}
		return r;
	}
	
	/**
	 * 逆行列演算のため、消去演算を行う前の枢軸要素の絶対値が最大となる行を探索し、行入れ替えを行う
	 *
	 * @param col	演算列
	 * @param a		ガウス・ジョルダン法のための演算行列
	 */
	static private void pivot(int col, double a[][])
	{
		// 当該列(col)の絶対値が最大の行を検索する。
			// 初期値は col とする
		int ic = col;
		double max = Math.abs(a[ic][ic]);
			// col+1行以降から最大値を検索
		for (int i=col+1; i<a.length; i++) {
			if (max < Math.abs(a[i][col])) {
				ic = i;
				max = Math.abs(a[i][col]);
			}
		}
		// 絶対値が最大の行(ic)が col と異なれば、行を入れ替え
		if (ic != col) {
			double sv;
			for(int j=0; j<2*a.length; j++){
				sv = a[ic][j];
				a[ic][j] = a[col][j];
				a[col][j] = sv; 
			}
		}
	}

	/**
	 * 逆行列演算のためのガウス・ジョルダン法による、消去演算を行う
	 *
	 * @param col	演算列
	 * @param a		ガウス・ジョルダン法のための演算行列
	 */
	static private void sweep(int col, double a[][])
	{
		int i,j;
		// col行の要素をすべて a[col][col] で除算 (a[col][col] = 1とするため)
		double p = a[col][col];
		for(i=0; i<2*a.length; i++) {
			a[col][i] /= p;
		}
		// 消去演算	
		double sv;
		for(i=0; i<a.length; i++){
			if (i == col) { continue; }
			// col列以降を消去演算する (a[col][col] = 1とし、それ以外のcol列要素は0となる)
			sv = a[i][col];
			for(j=col; j<2*a.length; j++) {
				a[i][j] -= sv*a[col][j];
			}
		}
	}
		
}

