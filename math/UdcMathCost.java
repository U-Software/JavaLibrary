/* *********************************************************************
 * @(#)UdcMathCost.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.math;

import java.lang.*;
import java.util.*;
import java.io.*;


/**
 * ダイクストラ法等のコストクラス。<br>
 * ユーザは、必要に応じて本クラスを継承してコストクラスを実装して下さい。<br>
 * 一時的にコストを抑止/解除をする場合にはsetDisabledメソッドを使用してください。また、設定可能なコスト値は正値で内部では、通過条件探索時に通過条件のコストをcostとするため、それ以外のコストをcost*UdcMathDijkstra.ftrChangeCostとするため、これを考慮した値として下さい。<br>
 * <b>UGBView1.22まではコスト値にdouble型を使用していたが、計算精度が低いため正常なコスト判定がなされない。そのためコスト値はint型とする。</b>
 *
 * @author  Takayuki Uchida
 * @version 1.1, 18 Jan 2004
 * @see 	UdcMathDijkstra
 * @since   UDC1.1
 */
public class UdcMathCost implements Comparable
{
	static long index = 0;

	/** コスト識別子 */
	String costname;
	/** コスト値。デフォルトのInteger.MAX_VALUEは、非接続を表します。*/
	protected int cost = Integer.MAX_VALUE;
	/** コスト値を1000倍した値。コストを設定する場合には必ず本値も併せて変更される必要があります。 */
	protected int calcCost = Integer.MAX_VALUE;
	/** 一時的なコスト値保存 */
	protected int svcost = Integer.MAX_VALUE;
	/** 一時的な非通過フラグ */
	protected boolean disabled = false;
	/** ローカルノードの識別子 */
	protected int local;
	/** 隣接ノードの識別子 */
	protected int remote;
	/** 内部情報としての一時的な未使用フラグ */
	protected int tmpdisabled = 0;
	/** 内部情報としての一時的な使用フラグ([0]探索時の使用可否状態/[1]幾何学パターンチェック時のチェック要否状態 */
	protected boolean[] tmpenabled = new boolean[2];
	/** 内部情報としての一時的な保存値 */
	protected int tmpval;
	/** 内部情報としての一時的な保存値 */
	protected int tmpNexthop;


	/**
	 * コンストラクタ
	 *
	 * @param	name	コスト識別子
	 * @since	UDC1.1
	 */
	 public UdcMathCost(String name)
	 {
		if (name == null) { costname = "" + (index++); }
		else 			  { costname = name; }
		Arrays.fill(tmpenabled, true);
	 }

	/**
	 * コンストラクタ
	 *
	 * @param	inst	UdcMathCostインスタンス
	 * @since	UDC1.1
	 */
	 public UdcMathCost(UdcMathCost inst)
	 {
		costname = new String(inst.getName());
		setCost(inst.getCost());
		setDisabled(isDisabled());
		local = inst.local;
		remote = inst.remote;
		tmpdisabled = inst.tmpdisabled;
		svcost = inst.svcost;
		System.arraycopy(tmpenabled, 0, inst.tmpenabled, 0, inst.tmpenabled.length);
	 }

	/**
	 * コスト識別名を取得します。
	 *
	 * @return	コスト識別名（ユーザ側では通常ノードID等を使用するとよい）
	 * @since	UDC1.1
	 */
	public String getName() { return costname; }

	/**
	 * ローカルノードの識別子を取得します。
	 *
	 * @return ローカルノードの識別子
	 * @since	UDC1.2
	 */
	final public int	getLocalNode() { return local; }

	/**
	 * 隣接ノードの識別子を取得します。
	 *
	 * @return 隣接ノードの識別子 
	 * @since	UDC1.2
	 */
	final public int getRemoteNode() { return remote; }

	/**
	 * コスト値を返却します。
	 * 本処理速度は探索速度に直結します。継承クラスにて計算が必要である場合、探索前に事前に計算を
	 * 実施しておき、本メンバ関数では、計算値を返却するのみにしなければならない。
	 *
	 * @return	コスト値
	 * @since	UDC1.1
	 */
	final public int getCost() { return cost; }

	/**
	 * コスト値を設定します。
	 *
	 * @return コストを正常に設定したか否か
	 * @param	val 	コスト値
	 * @since	UDC1.1
	 */
	final public boolean setCost(int val)
	{
		if (val < 0) { return false; }
		double tval = (double)val * (double)UdcMathDijkstra.ftrChangeCost;
		if (tval >= Integer.MAX_VALUE) { return false; }

		cost = val;
		setCalcCost(cost * UdcMathDijkstra.ftrChangeCost);
		return true;
	}

	/**
	 * 経路演算用のコスト値を返却します。
	 * double型の演算精度が低いため、内部ではUdcMathDijkstra.ftrChangeCost倍のコスト値を演算コストとして使用します。
	 * これは通過条件を優先する場合に優先コストの一部をUdcMathDijkstra.ftrChangeCostで除算して演算するためです。
	 *
	 * @return	コスト値
	 * @since	UDC1.3
	 */
	final protected int getCalcCost() { return calcCost; }

	/**
	 * 経路演算用のコスト値を設定します。
	 * double型の演算精度が低いため、内部ではUdcMathDijkstra.ftrChangeCost倍のコスト値を演算コストとして使用します。
	 * これは通過条件を優先する場合に優先コストの一部をUdcMathDijkstra.ftrChangeCostで除算して演算するためです。
	 *
	 * @param	val 	コスト値
	 * @since	UDC1.1
	 */
	final protected void setCalcCost(int val) { calcCost = val; }

	/**
	 * 一時的な非通過フラグを取得する。
	 * @return 一時的な非通過フラグ
	 */
	final public boolean isDisabled() { return disabled; }

	/**
	 * 一時的な非通過フラグを設定する。
	 * @param val 一時的な非通過フラグ
	 */
	final public void setDisabled(boolean val)
	{
		if (disabled != val) {
			if (val) { tmpdisabled ++; }
			else	 { tmpdisabled --; }
			disabled = val;
		}
	}

	/**
	 * コストの有効性有無を取得する
	 *
	 * @return コストの有効性有無
	 */
	final public boolean isActive() { return (tmpdisabled <= 0); }

	/**
	 * コストの無効性有無を取得する
	 *
	 * @return コストの無効性有無
	 */
	final public boolean isUnActive() { return (tmpdisabled > 0); }

	/**
	 * 自身の複製を返却します。
	 *
	 * @return	自身の複製
	 * @since	UDC1.1
	 */
	public Object clone() throws CloneNotSupportedException { return (new UdcMathCost(this)); }

	/**
	 * CostのgetCost()値で比較します。
	 *
	 * @return 0:等しい/1以上:引数より大きい/0未満:引数より小さい
	 * @param an	比較対象
	 */
	public int compareTo(Object an)
	{
		UdcMathCost tcos = (UdcMathCost)an;
		if (isActive()) {
			if (tcos.isUnActive()) { return -1; }
		} else {
			if (tcos.isActive()) { return 1; }
		}
		if (calcCost > tcos.calcCost) 		{ return 1; }
		else if (calcCost < tcos.calcCost) { return -1; }

		if (remote > tcos.remote) { return 1; }
		else if (remote < tcos.remote) { return -1; }
		return 0;
	}
}

