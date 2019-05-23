/* *********************************************************************
 * @(#)UdcPoint.java 1.2, 30 Jun 2007
 *
 * Copyright 2007 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.math;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.awt.geom.*;


/**
 * UDC座標クラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 30 Jun 2007
 * @since   UDC1.2
 */
public class UdcPoint
{
	/** 直交座標表現のX座標 */
	double x_;

	/** 直交座標表現のY座標 */
	double y_;

	/** 直交座標表現の定点X座標 */
	double ox_;

	/** 直交座標表現の定点Y座標 */
	double oy_;

	/** 極座標表現のr成分 */
	double r_;

	/** 極座標表現のtheta成分 */
	double theta_;

	/**
	 * コンストラクタ
	 * @since 1.2
	 */
	public UdcPoint()
	{
		x_ = y_ = ox_ = oy_ = r_ = theta_ = 0.0;
	}

	/**
	 * コンストラクタ<br>
	 * 極座標は定点座標を(0,0)とする
	 * @param	x	直交座標表現のx座標
	 * @param	y	直交座標表現のY座標
	 * @since 1.2
	 */
	public UdcPoint(double x, double y)
	{
		setRectangularCoordinates(x, y, 0, 0);
	}

	/**
	 * コンストラクタ<br>
	 * @param	x	直交座標表現のx座標
	 * @param	y	直交座標表現のY座標
	 * @param	ox	直交座標表現の定点x座標
	 * @param	oy	直交座標表現の定点Y座標
	 * @since 1.2
	 */
	public UdcPoint(double x, double y, double ox, double oy)
	{
		setRectangularCoordinates(x, y, ox, oy);
	}

	/**
	 * 直交座標を設定する。<br>
	 * @param	x	直交座標表現のx座標
	 * @param	y	直交座標表現のY座標
	 * @param	ox	直交座標表現の定点x座標
	 * @param	oy	直交座標表現の定点Y座標
	 * @since 1.2
	 */
	public void setRectangularCoordinates(double x, double y, double ox, double oy)
	{
		ox_ = ox;
		oy_ = oy;
		x_ = x;
		y_ = y;
		double dx = x_ - ox_;
		double dy = y_ - oy_;
		theta_ = Math.atan2(dy, dx);	
		r_ = Math.sqrt( dx*dx + dy*dy);
	}

	/**
	 * 極座標を設定する。<br>
	 * 極座標は定点座標を(0,0)とする
	 * @param	r		極座標表現のr成分
	 * @param	theta	極座標表現のtheta成分
	 * @since 1.2
	 */
	public void setPolarCoordinates(double r, double theta)
	{
		setPolarCoordinates(r, theta, 0, 0);
	}

	/**
	 * 極座標を設定する。<br>
	 * 極座標は定点座標を(0,0)とする
	 * @param	r		極座標表現のr成分
	 * @param	theta	極座標表現のtheta成分
	 * @param	ox	直交座標表現の定点x座標
	 * @param	oy	直交座標表現の定点Y座標
	 * @since 1.2
	 */
	public void setPolarCoordinates(double r, double theta, double ox, double oy)
	{
		r_ = r;
		theta_ = theta;
		ox_ = ox;
		oy_ = oy;
		x_ = r_ * Math.cos(theta_) + ox_;
		y_ = r_ * Math.sin(theta_) + oy_;
	}

	/**
	 * 直交座標表現の定点x座標を取得する。
	 * @return	直交座標表現の定点x座標
	 * @since 1.2
	 */
	public double getOX() { return ox_; }

	/**
	 * 直交座標表現の定点y座標を取得する。
	 * @return	直交座標表現の定点y座標
	 * @since 1.2
	 */
	public double getOY() { return oy_; }

	/**
	 * 直交座標表現のx座標を取得する。
	 * @return	直交座標表現のx座標
	 * @since 1.2
	 */
	public double getX() { return x_; }

	/**
	 * 直交座標表現のy座標を取得する。
	 * @return	直交座標表現のy座標
	 * @since 1.2
	 */
	public double getY() { return y_; }

	/**
	 * 極座標表現のr成分を取得する。
	 * @return 極座標表現のr成分
	 * @since 1.2
	 */
	public double getR() { return r_; }

	/**
	 * 極座標表現のtheta成分を取得する。
	 * @return 極座標表現のtheta成分
	 * @since 1.2
	 */
	public double getTheta() { return theta_; }

	/**
	 * 定点を基点とした２点間の直線を回転させた座標を取得する
	 * @return 回転座標
	 * @param	roundtheta	回転角度（ラジアン）
	 * @since 1.2
	 */
	public UdcPoint round(double roundtheta)
	{
		double rx = x_ - ox_;
		double ry = y_ - oy_;
		double rrx = rx*Math.cos(roundtheta) - ry*Math.sin(roundtheta) + ox_;
		double rry = rx*Math.sin(roundtheta) + ry*Math.cos(roundtheta) + oy_;
		return (new UdcPoint(rrx, rry, ox_, oy_));
	}

	/**
	 * 定点を基点とした２点間の直線を回転させ、定点を中心とする半径ｒの座標を取得する
	 * @return 回転座標
	 * @param	roundtheta	回転角度（ラジアン）
	 * @param	r	回転半径	
	 * @since 1.2
	 */
	public UdcPoint round(double roundtheta, double r)
	{
		double rx = r * Math.cos(theta_);
		double ry = r * Math.sin(theta_);
		double rrx = rx*Math.cos(roundtheta) - ry*Math.sin(roundtheta) + ox_;
		double rry = rx*Math.sin(roundtheta) + ry*Math.cos(roundtheta) + oy_;
		return (new UdcPoint(rrx, rry, ox_, oy_));
	}

	/**
	 * 自インスタンスと交差する直線の交点を取得する。
	 * @return 交点座標(UdcPointで返却し、定点は0,0とする)
	 * @param	line 直線情報
	 * @since 1.23
	 */
	public UdcPoint lineCrossPoint(UdcPoint line)
	{
		// 傾きが同じ場合は平行とみなす
		if (getTheta() == line.getTheta()) {
			return null;
		}

		// 直線の方程式
		//	直線1　始点(x1s, y1s) 終点(x1e,y1e)
		//	直線2　始点(x2s, y2s) 終点(y2e,y2e)
		//	・直線1の方程式	(x1e - x1s)・y - (y1e - y1s)・x = x1e・y1s - y1e・x1s
		//  ・直線2の方程式  (x2e - x2s)・y - (y2e - y2s)・x = x2e・y2s - y2e・x2s
		//	(*)行列計算による連立方程式の解は X = A^-1・B (A^-1はAの逆行列)
		double[][] a = new double[2][2];
		a[0][0] = (getOY() - getY()); 				a[0][1] = (getX() - getOX());				// -(y1e - y1s)	, (x1e - x1s)
		a[1][0] = (line.getOY() - line.getY());		a[1][1]	= (line.getX() - line.getOX());		// -(y2e - y2s) , (x2e - x2s)
		double[][] b = new double[2][1];
		b[0][0] = (getX()*getOY() - getY()*getOX());						// x1e・y1s - y1e・x1s
		b[1][0] = (line.getX()*line.getOY() - line.getY()*line.getOX());	// x2e・y2s - y2e・x2s

		double[][] rev_a = UdcMath.matrixReverse(a);
		if (rev_a == null) { return null; }
		double[][] cross = UdcMath.matrixMultiple(rev_a, b);
		return (new UdcPoint(cross[0][0], cross[1][0]));
	}

	/**
	 * 定点を円1の中心、X,Y座標を円2の中心とした場合の円と円の交点座標を取得する。
	 * @return 交点座標リスト
	 * @param	r1	円1の半径
	 * @param	r2	円2の半径
	 * @since 1.23
	 */
	public Vector circleCrossPoints(double r1, double r2)
	{
		// 円の交点座標公式
		// 	x1 = ox_ + r1 * cos(theta_ + a) ,  x2 = ox_ + r1 * cos(theta_ - a)
		// 	y1 = ox_ + r1 * sin(theta_ + a) ,  y2 = ox_ + r1 * sin(theta_ - a)
		// 	a値は余弦定理より 
		// 		cos(a) = (r_^2 + r1^2 - r2^2) / (2 * r_ * r1)
		// 		a = acos( cos(a) );
			// 未接続
		Vector points = new Vector();
		if (r_ > (r1 + r2) || r1 > (r_ + r2) || r2 > (r_ + r1)) {
			return points;
		}
		double a, cx, cy;
		a = Math.acos( ((Math.pow(r_,2)+Math.pow(r1,2)-Math.pow(r2,2)) / (2*r_*r1)) );
		cx = ox_ + r1 * Math.cos(theta_ + a);
		cy = oy_ + r1 * Math.sin(theta_ + a);
		points.add(new UdcPoint(cx, cy, ox_, oy_));
			// 1点接続
		if (r_ == (r1 + r2) || r1 == (r_ + r2) || r2 == (r_ + r1) || a == 0) {
			return points;
		}
			// 2点接続
		cx = ox_ + r1 * Math.cos(theta_ - a);
		cy = oy_ + r1 * Math.sin(theta_ - a);
		points.add(new UdcPoint(cx, cy, ox_, oy_));
		return points;
	}
}

