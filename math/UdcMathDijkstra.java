/* *********************************************************************
 * @(#)UdcMathDijkstra.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.math;

import java.lang.*;
import java.util.*;
import java.io.*;

//for speed debug
//import udc.util.*;


/**
 * ダイクストラ法演算クラス。
 *
 * @author  Takayuki Uchida
 * @version 1.1, 18 Jan 2004
 * @see 	UdcMathCost
 * @since   UDC1.1
 */
public class UdcMathDijkstra
{
	/** 全経路検索時のフィルタモード：順不同 */
	final public static int FilterMode_NoOrder = 0;
	/** 全経路検索時のフィルタモード：順序通り */
	final public static int FilterMode_Order = 1;

	/** 全経路探索による探索開始時の通過条件のコスト変更比率 */
	public static int ftrChangeCost = 1000;

	/** 全経路検索時のフィルタモード */
	private int ftrMode = FilterMode_NoOrder;
	/** 隣接行列の行列数。*/
	private int	mtrxMax = 0;
	/** 隣接テーブル(順方向) */
	private ArrayList[] mtrxTbl_ = null;
	/** 隣接テーブル(逆方向) */
	private ArrayList[] r_mtrxTbl_ = null;

	/** ノードのリスト */
	private Integer[]	nodeList_ = null;

	/** ダイクストラ法での経路の通過状態リスト。(本情報は探索演算中の一時的な情報) */
	private boolean[] unuseFlag_ = null;
	/** ダイクストラ法での経路の未探索ノードリスト。(本情報は探索演算中の一時的な情報) */
	private int[] unuseNode_ = null; 
	/** ダイクストラ法での始点からのコスト表。(本情報は探索演算中の一時的な情報) */
	private int[] routeVal_ = null;
	/** ダイクストラ法での始点からの最短隣接コスト表。(本情報は探索演算中の一時的な情報) */
	private UdcMathCost[] prevCost_ = null;

	/** ダイクストラ法での経路の通過状態リスト。(本情報は逆探索演算中の一時的な情報) */
	private boolean[] r_unuseFlag_ = null;
	/** ダイクストラ法での経路の未探索ノードリスト。(本情報は逆探索演算中の一時的な情報) */
	private int[] r_unuseNode_ = null;
	/** ダイクストラ法での終点へのコスト表。(本情報は逆探索演算中の一時的な情報) */
	private int[] r_routeVal_ = null;
	/** ダイクストラ法での終点への最短隣接コスト表。(本情報は逆探索演算中の一時的な情報) */
	private UdcMathCost[] r_prevCost_ = null;

	/** ダイクストラ法での経路の通過状態リスト。(本情報は探索演算中の一時的な情報) */
	private int[] reachFlag_ = null;
	/** 簡易探索による経路の未探索ノードリスト。(本情報は探索演算中の一時的な情報) */
	private int[] reachNode_ = null;
	/** 簡易探索による経路の未探索ノードリスト。(本情報は探索演算中の一時的な情報) */
	private int[] r_reachNode_ = null;

	/** 幾何学パターンチェックでの代替経路格納エリア(本情報は探索演算中の一時的な情報) */
	private ArrayList 	subRoute_ = new ArrayList();

	/** 全経路探索による最短経路探索時の最少コスト値。(本情報は演算中の一時的な情報) */
	private int allRouteCost_;


	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.1
	 */
	public UdcMathDijkstra()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param matrixNum	コストマトリックス数
	 * @since	UDC1.3
	 */
	public UdcMathDijkstra(int matrixNum)
	{
		ArrayList matrix[] = new ArrayList[matrixNum];
		for (int i=0; i<matrixNum; i++) {
			matrix[i] = new ArrayList();
		}
		setCost(matrix);
	}

	/**
	 * コンストラクタ
	 *
	 * @param	matrix	コストマトリックス
	 * @since	UDC1.1
	 */
	public UdcMathDijkstra(ArrayList[] matrix)
	{
		setCost(matrix);
	}

	/**
	 * 全経路検索時のフィルタモードを取得する。
	 *
	 * @return 全経路検索時のフィルタモード
	 * @since	UDC1.2
	 */
	public int getFilterMode() { return ftrMode; }

	/**
	 * 全経路検索時のフィルタモードを設定する。
	 *
	 * @param mode 全経路検索時のフィルタモード
	 * @since	UDC1.2
	 */
	public void setFilterMode(int mode) { ftrMode = mode; }

	/**
	 * コストマトリックス数を取得する。
	 *
	 * @return	コストマトリックス数
	 * @since	UDC1.1
	 */
	public int getMatrixMax() { return mtrxMax; }

	/**
	 * コストマトリックスのコピーを取得する。<br>
	 *
	 * @return	コストマトリックスの複製
	 * @since	UDC1.3
	 */
	public ArrayList[] getCopyCost() throws CloneNotSupportedException
	{
		ArrayList mc[] = new ArrayList[mtrxMax];
		for (int i=0; i<mtrxMax; i++) {
			mc[i] = new ArrayList();
			for (int j=0; j<mtrxTbl_[i].size(); j++) {
				mc[i].add( ((UdcMathCost)mtrxTbl_[i].get(j)).clone() );	
			}
		}
		return mc;
	}

	/**
	 * コストを取得する。内部情報を直に返却するため値等の変更を行う場合には本メンバ関数でコスト情報を取得して更新してください。
	 *
	 * @return	コスト値
	 * @param	x		コストポジション 
	 * @param	y		コストポジション
	 * @since	UDC1.1
	 */
	public UdcMathCost getCost(int x, int y)
	{
		UdcMathCost cos;
		ArrayList tbl = mtrxTbl_[x];
		for (int i=tbl.size()-1; i>=0; i--) {
			cos = (UdcMathCost)tbl.get(i);
			if (cos.remote == y) { return cos; }
		}
		return null;
	}

	/**
	 * コストマトリックスを全更新する
	 *
	 * @param	mc	コストマトリックス
	 * @since	UDC1.1
	 */
	private void setCost(ArrayList[] mc)
	{
		int i,j;
		mtrxMax = mc.length;
		mtrxTbl_ = mc;
		r_mtrxTbl_ = new ArrayList[mtrxMax];
		for (i=0; i<mtrxMax; i++) { r_mtrxTbl_[i] = new ArrayList(); }
		nodeList_ = new Integer[mtrxMax];
		unuseFlag_ = new boolean[mtrxMax];
		unuseNode_ = new int[mtrxMax];
		routeVal_ = new int[mtrxMax];
		prevCost_ = new UdcMathCost[mtrxMax];
		r_unuseFlag_ = new boolean[mtrxMax];
		r_unuseNode_ = new int[mtrxMax];
		r_routeVal_ = new int[mtrxMax];
		r_prevCost_ = new UdcMathCost[mtrxMax];
		reachFlag_ = new int[mtrxMax];
		reachNode_ = new int[mtrxMax];
		r_reachNode_ = new int[mtrxMax];
		for (i=0; i<mtrxMax; i++) {
			nodeList_[i] = new Integer(i);
			if (mtrxTbl_[i] == null) { mtrxTbl_[i] = new ArrayList(); }
			if (mtrxTbl_[i].size() > 0) {
				for (j=0; j<mtrxTbl_[i].size(); j++) {
					UdcMathCost cos = (UdcMathCost)mtrxTbl_[i].get(j);
					r_mtrxTbl_[cos.remote].add(cos);
				}
				Collections.sort(mtrxTbl_[i]);
			}
		}
		for (i=0; i<mtrxMax; i++) {
			Collections.sort(r_mtrxTbl_[i]);
		}
	}

	/**
	 * コストを設定する。内部情報に直に設定する。
	 *
	 * @param	x		コストポジション
	 * @param	y		コストポジション
	 * @param	cost	コスト値
	 * @since	UDC1.1
	 */
	public void setCost(int x, int y, UdcMathCost cost)
	{
		ArrayList ct;
		UdcMathCost cos = getCost(x,y);
		if (cos != null) {
			mtrxTbl_[x].remove(cos);
			r_mtrxTbl_[y].remove(cos);
		}
		if (cost != null) {
			cost.local = x;
			cost.remote = y; 
			mtrxTbl_[x].add(cost);
			Collections.sort(mtrxTbl_[x]);
			r_mtrxTbl_[y].add(cost);
			Collections.sort(r_mtrxTbl_[y]);
		}
	}

	/**
	 * コストマトリックスに任意のノード接続を追加する。<br>
	 * コストマトリックスそのものが末尾に追加されることに注意してください。
	 *
	 * @since	UDC1.3
	 */
	public void addCost() throws CloneNotSupportedException
	{
		ArrayList[] mc = new ArrayList[mtrxMax+1];
		System.arraycopy(mtrxTbl_, 0, mc, 0, mtrxMax);
		mc[mtrxMax] = new ArrayList();
		setCost(mc);
	}

	/**
	 * コストマトリックスから任意のノード接続を削除する。<br>
	 * 指定したノードのコストマトリックスそのものが削除されることに注意してください。
	 *
	 * @param	node	当該ノードのコストマトリックスのインデックス
	 * @since	UDC1.3
	 */
	public void delCost(int node) throws CloneNotSupportedException
	{
		if (node < 0 || node >= mtrxMax || mtrxMax <= 0) {
			return;
		}
		int i, j, cnt;
		UdcMathCost cos;
		ArrayList[] mc = new ArrayList[mtrxMax-1];
		for (i=0,cnt=0; i<mtrxMax; i++) {
			if (node == i) { continue; }
			mc[cnt] = mtrxTbl_[i];
			for (j=0; j<mc[cnt].size(); j++) {
				cos = (UdcMathCost)mc[cnt].get(j);
				if (cos.remote == node) {
					mc[cnt].remove(cos);
					j--;
				}
			}
			cnt++;
		}
		setCost(mc);
	}

	/**
	 * コストマトリックスの一時的不通過フラグをクリアする。<br>
	 *
	 * @since	UDC1.3
	 */
	public void resetDisableCost()
	{
		int i, j, sz;
		UdcMathCost cos;
		for (i=0; i<mtrxMax; i++) {
			sz = mtrxTbl_[i].size();
			for (j=0; j<sz; j++) {
				cos = (UdcMathCost)mtrxTbl_[i].get(j);
				cos.setDisabled(false);
				cos.tmpdisabled = 0;
			}
		}
	}

	/**
	 * コストマトリックスの一時的不通過フラグをノード単位に設定する。<br>
	 *
	 * @param node	ノードインデックス
	 * @param val	一時的不通過フラグ
	 * @since	UDC1.3
	 */
	public void setDisableCost(int node, boolean val)
	{
		int i, sz;
		sz = mtrxTbl_[node].size();
		for (i=0; i<sz; i++) {
			((UdcMathCost)mtrxTbl_[node].get(i)).setDisabled(val);
		}
		sz = r_mtrxTbl_[node].size();
		for (i=0; i<sz; i++) {
			((UdcMathCost)r_mtrxTbl_[node].get(i)).setDisabled(val);
		}
	}

	/**
	 * ダイクストラ法によって最短経路探索を行い、最短経路を返却する。<br>
	 * 返却される最短経路は、UdcMathCostの線形リストです。
	 * (*)処理の高速化のため、入力パラメータのチェックは行いません。そのため
	 *	入力パラメータの正常性はユーザ側で保証して下さい。また、返却するCostリストは高速化のため複製ではありません。
	 *
	 * @return	最短経路コストリスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @since	UDC1.1
	 */
	public LinkedList searchShortestRoute(int start, int end)
	{
		// コストのソート(同じ条件なら同じ結果にするため)
		for (int i=0; i<mtrxMax; i++) {
			Collections.sort(mtrxTbl_[i]);
			Collections.sort(r_mtrxTbl_[i]);
		}
		// startを発とした最短経路ツリーを作成
		makeShortestRouteTable(start);
		// 最短経路リスト作成
		return setRoute(start, end, new LinkedList());
	}

//for speed debug
//int speed, costspeed, costAspeed, costBspeed, costCspeed, ftr1speed, ftr1mspeed, tmpAspeed, tmpBspeed, tmpCspeed, tmpDspeed, tmpt3speed, tmpt2speed, tmpt1speed;
	/**
	 * 通過条件によってフィルタした任意の区間の最短経路を返却する。。<br>
	 * 通過条件リストでは、通過しなければならない区間情報を設定します。<br>
	 * 通過区間を指定する場合は、UdcMathCostを、通過ノードをしている場合はノード識別子をIntegerで設定してください。
	 * また、通過条件が FilterMode_Order の場合には、ftrListに設定された順序通りに通過する経路のみとなります。
	 * 返却される経路リストは、UdcMathCostの線形リストで一つの経路を表し、この経路のリストを返却します。
	 * (*)処理の高速化のため、入力パラメータのチェックは行いません。そのため入力パラメータの正常性はユーザ側で保証して下さい。
	 *
	 * @return	最短経路コストリスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @since	UDC1.3
	 */
	public LinkedList searchFtrShortestRoute(int start, int end, ArrayList ftrList)
	{
//for speed debug
//speed = costspeed = costAspeed = costBspeed = costCspeed = ftr1speed = ftr1mspeed = tmpAspeed = tmpBspeed = tmpCspeed = tmpDspeed = tmpt3speed = tmpt2speed = tmpt1speed = 0;
//UdcTrace.trace(100, "searchFtrShortestRoute start[" + start + "] end[" + end + "]");
		int i, rn, vstart=start, vend=end;
		LinkedList routeList = new LinkedList();
		// 通過条件の補正
		ArrayList flist=null, firstFtrList=null, lastFtrList=null;
		if (ftrList != null && ftrList.size() > 0) {
			firstFtrList = new ArrayList();
			lastFtrList = new ArrayList();
			if ((flist=matchFilterFirst(start, end, ftrList, firstFtrList, lastFtrList)) == null) { return routeList; }
			// 最初の通過条件が受を含む区間指定であれば、その区間指定のremoteノードからしか探索しない。
			if (firstFtrList.size() > 0) {
				if ((vstart=((UdcMathCost)firstFtrList.get(firstFtrList.size()-1)).remote) == end) {
					routeList.addAll(firstFtrList);
					return routeList;
				}
			}
			// 最後の通過条件が受を含む区間指定であれば、その区間指定のlocalノードまでしか探索しない。
			if (lastFtrList.size() > 0) { vend = ((UdcMathCost)lastFtrList.get(0)).local; }
		}
		int fsz = (flist!=null) ? flist.size() : 0;
		// 発/受/通過条件に反する経路は一時抑止
		ArrayList newflist = new ArrayList();
		ArrayList preInvalid = new ArrayList();
		if (disableCostForFtrSearch(vstart, vend, flist, newflist, true, preInvalid) != 0) {
			for (i=preInvalid.size()-1; i>=0; i--) { ((UdcMathCost)preInvalid.get(i)).tmpdisabled --; }	// 一時抑止回復
			return routeList;
		}
		if (firstFtrList != null && firstFtrList.size() > 0) { disableCostFtrList(firstFtrList, preInvalid); }
		if (lastFtrList != null && lastFtrList.size() > 0) { disableCostFtrList(lastFtrList, preInvalid); }
		// 経路探索(通過条件がないなら通常のダイクストラ探索)
		if (fsz <= 0) {
			LinkedList route = searchShortestRoute(vstart, vend);
			if (route.size() > 0) {
				if (firstFtrList != null && firstFtrList.size() > 0) { route.addAll(0, firstFtrList); }
				if (lastFtrList != null && lastFtrList.size() > 0) { route.addAll(lastFtrList); }
			}
			for (i=preInvalid.size()-1; i>=0; i--) { ((UdcMathCost)preInvalid.get(i)).tmpdisabled --; }	// 一時抑止回復
			return route;
		}
		// 通過条件経路のコストを一時良化
		int nfsz = newflist.size();
		if (nfsz > 0) {
			for (i=0; i<nfsz; i++) { flist.add(new UdcMathDijkstraFtr(((Integer)newflist.get(i)).intValue())); }
		}
		ArrayList ftrMinCosList = getChangeMinCostForFtrList(vstart, vend, flist);
		ArrayList preChangeCost = changeCostForFtrList(vstart, vend, ftrMinCosList, flist);
		if (nfsz > 0) {
			for (i=0; i<nfsz; i++) { flist.remove(fsz); }
		}
		// 探索高速化のため探索開始前に受への最短コスト値を算出しておく
		UdcMathDijkstraFtr ftr;
		makeShortestRouteTableReverse(vend);
		boolean need = (r_prevCost_[vstart] == null) ? false : true;
		ArrayList chgftr = new ArrayList(mtrxMax-1);
		UdcInhNodeInfo inhInfo = new UdcInhNodeInfo(nodeList_);
		// 通過条件の到達性チェック
		if (need) {
			makeShortestRouteTable(vstart);
			for (i=0; i<fsz; i++) {
				ftr = (UdcMathDijkstraFtr)flist.get(i);
				rn = ftr.getFtrTpNode();
				// 発->通過条件への到達性チェック
				if (prevCost_[rn] == null) { need = false; break; }
				// 通過条件->受への到達性チェック
				if (r_prevCost_[rn] == null) { need = false; break; }
				ftr.fpostcost = r_routeVal_[rn];
			}
		}
		// 不要な探索路を排除するための指標情報を作っておく
		if (need) {
			for (i=0; i<fsz; i++) {
				ftr = (UdcMathDijkstraFtr)flist.get(i);
				makeShortestRouteTable(ftr.getFtrTpNode());
				ftr.routeVal_ = Arrays.copyOf(routeVal_, routeVal_.length);
				makeShortestRouteTableReverse(ftr.getFtrTpNode());
				ftr.r_routeVal_ = Arrays.copyOf(r_routeVal_, r_routeVal_.length);
			}
			//
			// Farther study
			//
		}
		// 全経路探索による最短経路探索
		if (need) {
			allRouteCost_ = Integer.MAX_VALUE;
			LinkedList route = new LinkedList();
			searchAllRouteSub(vstart, vend, flist, inhInfo, route, 0, (mtrxMax-1), routeList, true, chgftr);
		}
		// 一時抑止経路回復
		for (i=preInvalid.size()-1; i>=0; i--) { ((UdcMathCost)preInvalid.get(i)).tmpdisabled --; }
		// 通過条件経路のコストを一時良化回復
		UdcMathCost cos;
		for (i=preChangeCost.size()-1; i>=0; i--) {
			cos = (UdcMathCost)preChangeCost.get(i);
			cos.setCalcCost( cos.svcost );
			Collections.sort(mtrxTbl_[cos.local]);	// コスト順ソートを回復
		}
		// 経路返却
//for speed debug
//UdcTrace.trace(100, "--- call-cnt: " + speed + " --- cut: " + (speed - costspeed)
//					+ "(" + costAspeed + "," + costBspeed +  "," + costCspeed + ":" + tmpt1speed + "/" + tmpt2speed + "/" + tmpt3speed + ")"
//					+ "  ftr1cnt: " + ftr1mspeed + "/" + ftr1speed
//					+ "  dsearch: " + tmpAspeed + "/" + tmpBspeed + "/" + tmpCspeed + "/" + tmpDspeed
//					+ "  cost : " + allRouteCost_);
		if (routeList.size() > 0) {
			LinkedList route = (LinkedList)routeList.get(0); 
			if (firstFtrList != null && firstFtrList.size() > 0) { route.addAll(0, firstFtrList); }
			if (lastFtrList != null && lastFtrList.size() > 0) { route.addAll(lastFtrList); }
			return route;
		}
		return routeList;
	}

	/**
	 * 任意の区間の経路パタン全てを算出する。<br>
	 * 返却される経路リストは、UdcMathCostの線形リストで一つの経路を表し、この経路のリストを返却します。
	 * (*)処理の高速化のため、入力パラメータのチェックは行いません。そのため
	 *	入力パラメータの正常性はユーザ側で保証して下さい。
	 *
	 * @return	最短経路コストリスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @since	UDC1.1
	 */
	public LinkedList searchAllRoute(int start, int end)
	{
		return searchFtrAllRoute(start, end, null, Integer.MAX_VALUE, (mtrxMax-1));
	}

	/**
	 * 通過条件によってフィルタした任意の区間の経路パタン全てを算出する。<br>
	 * 通過条件リストでは、通過しなければならない区間情報を設定します。<br>
	 * 通過区間を指定する場合は、UdcMathCostを、通過ノードをしている場合はノード識別子をIntegerで設定してください。
	 * また、通過条件が FilterMode_Order の場合には、ftrListに設定された順序通りに通過する経路のみとなります。
	 * 返却される経路リストは、UdcMathCostの線形リストで一つの経路を表し、この経路のリストを返却します。
	 * (*)処理の高速化のため、入力パラメータのチェックは行いません。そのため入力パラメータの正常性はユーザ側で保証して下さい。
	 *
	 * @return	最短経路コストリスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @param	costMax		通過経路の総コストの上限
	 * @param	hopMax		ホップ数の上限
	 * @since	UDC1.1
	 */
	public LinkedList searchFtrAllRoute(int start, int end, ArrayList ftrList, int costMax, int hopMax)
	{
		allRouteCost_ = (costMax < 0) ? Integer.MAX_VALUE : costMax;
		if (hopMax < 0) { hopMax = mtrxMax - 1; }

		int i,vstart=start,vend=end;
		LinkedList routeList = new LinkedList();
		// 通過条件の補正
		ArrayList flist=null, firstFtrList=null, lastFtrList=null;
		if (ftrList != null && ftrList.size() > 0) {
			firstFtrList = new ArrayList();
			lastFtrList = new ArrayList();
			if ((flist=matchFilterFirst(start, end, ftrList, firstFtrList, lastFtrList)) == null) { return routeList; }
			// 最初の通過条件が受を含む区間指定であれば、その区間指定のremoteノードからしか探索しない。
			if (firstFtrList.size() > 0) {
				if ((vstart=((UdcMathCost)firstFtrList.get(firstFtrList.size()-1)).remote) == end) {
					routeList.addAll(firstFtrList);
					return routeList;
				}
			}
			// 最後の通過条件が受を含む区間指定であれば、その区間指定のlocalノードまでしか探索しない。
			if (lastFtrList.size() > 0) { vend = ((UdcMathCost)lastFtrList.get(0)).local; }
		}
		int rn, fsz = (flist!=null) ? flist.size() : 0;
		// 通過条件に反する経路は一時抑止
		ArrayList newflist = (fsz > 0) ? new ArrayList() : null;
		ArrayList preInvalidCost = new ArrayList();
		if (disableCostForFtrSearch(vstart, vend, flist, newflist, false, preInvalidCost) != 0) {
			for (i=preInvalidCost.size()-1; i>=0; i--) { ((UdcMathCost)preInvalidCost.get(i)).tmpdisabled --; }	// 一時抑止回復
			return routeList;
		}
		if (firstFtrList != null && firstFtrList.size() > 0) { disableCostFtrList(firstFtrList, preInvalidCost); }
		if (lastFtrList != null && lastFtrList.size() > 0) { disableCostFtrList(lastFtrList, preInvalidCost); }
		// 探索高速化のため探索開始前に受への最短コスト値を算出しておく
		UdcMathDijkstraFtr ftr;
		makeShortestRouteTableReverse(vend);
		boolean need = (r_prevCost_[vstart] == null) ? false : true;
		UdcInhNodeInfo inhInfo = new UdcInhNodeInfo(nodeList_);
		// 通過条件の到達性チェック
		if (need) {
			makeShortestRouteTable(vstart);
			for (i=0; i<fsz; i++) {
				ftr = (UdcMathDijkstraFtr)flist.get(i);
				rn = ftr.getFtrTpNode();
				// 発->通過条件への到達性チェック
				if (prevCost_[rn] == null) { need = false; break; }
				// 通過条件->受への到達性チェック
				if (r_prevCost_[rn] == null) { need = false; break; }
				ftr.fpostcost = r_routeVal_[rn]; 
			}
		}
		// 不要な探索路を排除するための指標情報を作っておく
		if (need) {
			for (i=0; i<fsz; i++) {
				ftr = (UdcMathDijkstraFtr)flist.get(i);
				makeShortestRouteTable(ftr.getFtrTpNode());
				ftr.routeVal_ = Arrays.copyOf(routeVal_, routeVal_.length);
				makeShortestRouteTableReverse(ftr.getFtrTpNode());
				ftr.r_routeVal_ = Arrays.copyOf(r_routeVal_, r_routeVal_.length);
			}
			//
			// Farther study
			//
		}
		// 経路探索
		LinkedList route = new LinkedList();
		searchAllRouteSub(vstart, vend, flist, inhInfo, route, 0, hopMax, routeList, false, null);
		// 一時抑止経路回復
		for (i=preInvalidCost.size()-1; i>=0; i--) { ((UdcMathCost)preInvalidCost.get(i)).tmpdisabled --; }
		// 経路返却
		if ((firstFtrList != null && firstFtrList.size() > 0) || (lastFtrList != null && lastFtrList.size() > 0)) {	
			for (i=0; i<routeList.size(); i++) {
				route = (LinkedList)routeList.get(i);
				if (firstFtrList != null && firstFtrList.size() > 0) { route.addAll(0, firstFtrList); }
				if (lastFtrList != null && lastFtrList.size() > 0) { route.addAll(lastFtrList); }
			}
		}
		return routeList;
	}

	/**
	 * 任意の区間の経路パタン全てを算出するリカーシブファンクション。<br>
	 * 通過条件リストでは、通過しなければならない区間情報を設定します。<br>
	 * 通過区間を指定する場合は、UdcMathCostを、通過ノードをしている場合はノード識別子をIntegerで設定してください。
	 * また、通過条件が FilterMode_Order の場合には、ftrListに設定された順序通りに通過する経路のみとなります。
	 * 
	 * @return 	探索有無チェック結果(deterrentOrCompleteRoute関数の結果)
	 * @param	start		コストマトリックス上の経路始点位置
	 * @param	end			コストマトリックス上の経路終点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @param	inhInfo	前経路までの到達性チェックでコスト超過となるノードリスト
	 * @param	route		作成中経路
	 * @param	nowCost		検索中の経路コスト
	 * @param	hopMax		ホップ数の上限
	 * @param	routeList	経路格納リスト
	 * @param	shortest	最短経路探索のみ
	 * @param	chgftr	前区間で通過条件が変化したか否か(最短経路探索のみ場合のみ有効)
	 * @since	UDC1.1
	 */
	private int searchAllRouteSub(int start, int end, ArrayList ftrList, UdcInhNodeInfo inhInfo,
					LinkedList route, int nowCost, int hopMax, LinkedList routeList, boolean shortest, ArrayList chgftr)
	{
		int i, j, rn, cnt,ret;
		int oinsz=inhInfo.inhSz;
		ArrayList tbl = mtrxTbl_[start];
		int nsz = tbl.size();
		boolean bcheck = true; // bcheck = inhInfo.bcheck; 今はまだ入れない（ボトルネックチェック要否に(route.size%2)といった処理をいれているためjumpでbcheck被害が拡大する可能性があるため）

//for speed debug
//speed ++;
		// 当該区間が使用可能かあるいは最短経路探索時の経路確定か否かをチェックする
		// (*)通過条件が一つあるいはなしなら以下で経路が確定する可能性があるため実施
		//ArrayList jumpRouteList = new ArrayList(); 今はまだ入れない（ボトルネックチェック要否に(route.size%2)といった処理をいれているためjumpでbcheck被害が拡大する可能性があるため）
		ArrayList jumpRouteList = null;
		int detret = deterrentOrCompleteRoute(start, end, ftrList, bcheck, route, nowCost, hopMax, routeList, inhInfo, shortest, jumpRouteList);
//for speed debug
/*
int remain = allRouteCost_ - nowCost;
if (route.size() > 0) { remain = allRouteCost_ - ( ((UdcMathCost)route.getLast()).tmpval + nowCost - ((UdcMathCost)route.getLast()).getCalcCost()); }
StringBuffer buf = new StringBuffer();
for (int y=0; y<route.size(); y++) {
	UdcMathCost tcos = (UdcMathCost)route.get(y);
	String data[] = tcos.getName().split("->");
	if (y == 0) { buf.append(tcos.getName()); }
	else		{ buf.append("/" + data[1]); }
}
UdcTrace.trace(100, buf.toString() + " (" + nowCost + ") " + speed + " : " + tmpAspeed + "/" + tmpBspeed +  "/" + tmpCspeed
			+ " fsz:" + ((ftrList!=null) ? ftrList.size() : 0) + " inh:" + inhInfo.inhSz + " ret:" + detret + " vrem:" + remain);
*/

		if (detret > 1) {
			inhInfo.bcheck = bcheck;
			for (i=inhInfo.inhSz; i>oinsz; i--) { enableNodeInOut(inhInfo.relLastInh()); }
			for (i=0; i<nsz; i++) { Arrays.fill(((UdcMathCost)tbl.get(i)).tmpenabled, true); }
			return detret;
		}
//for speed debug
//costspeed ++;

		// 経路Jumpがあれば、一気に特定経路をjumpする
		if (jumpRouteList != null && jumpRouteList.size() > 0) {
			UdcJumpRoute jumpRoute = (UdcJumpRoute)jumpRouteList.get(0);
			searchAllRouteJumpSub(start, end, ftrList, jumpRoute, inhInfo, route, nowCost, hopMax, routeList, shortest, chgftr);
			return detret;
		}

		// 動作中は mtrxTbl_[xx]の探索順序は絶対に昇順からでなければならない。
		// また、mtrxTbl_[xx]の格納順は変更してはならない。これはisNeed....でこの順序を意識して
		// 既に探索済み区間か否かを判定しているためである。
		UdcMathDijkstraFtr ftr;
		int ftrSz = (ftrList!=null) ? ftrList.size() : 0;
		int curCostMax = allRouteCost_ - nowCost;
		UdcMathCost cos, pcos;

		// 幾何学チェックのボトルネックチェック要否をここで保持（以下が不要）
		//     (受までの代替経路なし/受へのボトルネックでない)
		int csz = (chgftr != null) ? chgftr.size() : 0;
		int topobtCheck[] = new int[csz];
		int hcosv=0;
		if (csz > 0) {
			int total;
			for (j=csz-1; j>=0; j--) {
				if (((Boolean)chgftr.get(j)).booleanValue()) { break; }
				topobtCheck[j] = 0;
				pcos = (UdcMathCost)route.get(j);	
				hcosv += pcos.getCalcCost();
				if (r_prevCost_[pcos.local] == null) { continue; }
				for (i=0; i<nsz; i++) {
					if (!(cos=(UdcMathCost)tbl.get(i)).tmpenabled[0]) { continue; }
					if ((total=isContainRouteReverse(pcos.local, end, cos.remote)) > 0) {
						if ((hcosv+cos.getCalcCost()) >= total) { topobtCheck[j] |= (0x00000001 << (i%32)); }
					}
				}
			}
		}

		// 経路探索
		boolean need;
		int allRouteCostSv=allRouteCost_, changepos = -1;
		// 自->通過条件->受までの算出コスト値を保存
		int fmax=0;
		UdcFtrSvInfo svfinfo = null;
		if (ftrSz > 0) {
			svfinfo = new UdcFtrSvInfo(ftrSz);
			for (j=0; j<ftrSz; j++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(j);
				svfinfo.fprecost[j] = ftr.fprecost;
				svfinfo.fpostcost[j] = ftr.fpostcost;
				if (fmax < (ftr.fprecost + ftr.fpostcost)) { fmax = ftr.fprecost + ftr.fpostcost; }
			}
		}
		// 次HOP探索
		int in, out;
		UdcMatchFtr delFtr = new UdcMatchFtr(null);
		for (i=0; i<nsz; i++) {
			if (!(cos=(UdcMathCost)tbl.get(i)).tmpenabled[0] || cos.isUnActive()) { continue; }
			rn = cos.remote;
			// 再度コスト上限チェックを行う（最短経路が本ループ中で見つかった場合allRouteCost_が小さくなっているため）
			if (changepos >= 0 && cos.tmpval >= curCostMax) {
//for speed debug
//costCspeed ++;
				cos.tmpenabled[0] = false;
				continue;
			}
			// 目的地に到達
			if (rn == end) {
				if (shortest) {
					allRouteCost_ = nowCost + cos.getCalcCost();
					routeList.clear();
//for speed debug
//UdcTrace.trace(100, "** (" + speed + " cut: " + (speed - costspeed) + ") route - complete3  " + allRouteCost_);
				}
				LinkedList proute = (LinkedList)route.clone();
				proute.add(cos);
				routeList.add(proute);
				continue;
			}
			// 通過条件の更新
			need = false;
			delFtr.ftr = null;
			allRouteCostSv = allRouteCost_;
			if (matchFilterDelete(cos, ftrList, delFtr)) {
				// start地点からのone|two-hop合流による幾何学パターン(n角形)の探索除外
				//   三角形を例にすると a-->(cur)-->b / a-->b の経路が存在する場合、
				//   「通過条件が未変化」かつ「cost(a->cur)+cost(cur->b) >= cost(a->b)」であればcur->b 経路の探索は不要。
				//   またa->b をone-hopだけでなくtwo-hopでも同一である。
				need = true;
				if (shortest) {
					// 幾何学パターン(n角形)の探索除外
					if (delFtr.ftr == null || delFtr.ftr.getFtrNode() != null) {
						if ((out=getOnlyOneHopRoute(rn,start)) != -1 && (in=getOnlyOneHopRouteReverse(rn,start)) != -1) {
							if (!(out >= 0 && in >= 0 && in == out)) {
								boolean tcheck;
								hcosv = cos.getCalcCost();
								for (j=csz-1; j>=0; j--) {
									if (((Boolean)chgftr.get(j)).booleanValue()) { break; }
									pcos = (UdcMathCost)route.get(j);	
									hcosv += pcos.getCalcCost();
									tcheck = ((topobtCheck[j] & (0x00000001 << (i%32))) != 0) ? true : false;
									if ( !(need=isNeedSearchTopology(end, cos, pcos, hcosv, tcheck)) ) { break; }
								}
							}
						}
					}
				}
				if (need) {
					// 出力先の入経路を一時抑止
					disableNodeIn(rn);
					// 経路探索
					if (shortest) { chgftr.add( (delFtr.ftr == null) ? Boolean.FALSE : Boolean.TRUE ); }
					route.add(cos);
					ret = searchAllRouteSub(rn, end, ftrList, inhInfo,
								route, (nowCost+cos.getCalcCost()), hopMax, routeList, shortest, chgftr);
					route.removeLast();
					// 通過条件から受までの総コストを自身の値に戻す
					if (shortest) {
						chgftr.remove(csz);
						// 下流で最短経路が探索された/されない
						if (allRouteCostSv != allRouteCost_) {
							changepos = i;
							curCostMax = allRouteCost_ - nowCost;
						} else {
							cos.tmpenabled[0] = false;
						}
					}
					// 出力先の入経路を一時抑止解除
					enableNodeIn(rn);
				}
			}
			// 通過条件の復旧
			if (delFtr.ftr != null) { ftrList.add(delFtr.pos, delFtr.ftr); }
			// 通過条件の現コストを復旧する
			if (need) {
				for (j=0; j<ftrSz; j++) {
					ftr = (UdcMathDijkstraFtr)ftrList.get(j);
					ftr.fprecost = svfinfo.fprecost[j];
					ftr.fpostcost = svfinfo.fpostcost[j];
				}
				// 最短コストが変化した場合には、通過条件のコスト到達性をチェックする
				if (allRouteCostSv != allRouteCost_) {
					if (fmax >= allRouteCost_) {
//for speed debug
//costCspeed ++;
						break;
					}
				}
			}
		}
		// 後処理
		delFtr = null;
		svfinfo = null;
		for (i=0; i<nsz; i++) { Arrays.fill(((UdcMathCost)tbl.get(i)).tmpenabled, true); }

		// 下流経路探索で到達性不能要因となった情報リストを探索前の状態に戻す
		if (detret == 0) {
			for (i=inhInfo.inhSz; i>oinsz; i--) { enableNodeInOut(inhInfo.relLastInh()); }
		}
		inhInfo.bcheck = bcheck;

		return detret;
	}

	/**
	 * 任意の区間の経路パタン探索中にjumpRouteで指定された一定区間をjumpしてsearchAllRouteSubを実行する。<br>
	 * 
	 * @param	start		コストマトリックス上の経路始点位置
	 * @param	end			コストマトリックス上の経路終点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @param	jumpRoute	jump経路情報
	 * @param	inhInfo	前経路までの到達性チェックでコスト超過となるノードリスト
	 * @param	route		作成中経路
	 * @param	nowCost		検索中の経路コスト
	 * @param	hopMax		ホップ数の上限
	 * @param	routeList	経路格納リスト
	 * @param	shortest	最短経路探索のみ
	 * @param	chgftr	前区間で通過条件が変化したか否か(最短経路探索のみ場合のみ有効)
	 */
	private void searchAllRouteJumpSub(int start, int end, ArrayList ftrList, UdcJumpRoute jumpRoute, UdcInhNodeInfo inhInfo,
					LinkedList route, int nowCost, int hopMax, LinkedList routeList, boolean shortest, ArrayList chgftr)
	{
		int i, dsz=0;
		int svfprecost[]=null, svfpostcost[]=null;
		UdcMathCost cos;
		UdcMathDijkstraFtr ftr;
		UdcMatchFtr delFtr = null;

		int curCostMax = allRouteCost_ - nowCost;
		int jrsz = jumpRoute.route.size();
		if (jumpRoute.cost == 0) {
			for (i=0; i<jrsz; i++) {
				cos = (UdcMathCost)jumpRoute.route.get(i);
				if (cos.isUnActive()) { jumpRoute.cost = 0; return; }
				jumpRoute.cost += cos.getCalcCost();
			}
		}
//UdcTrace.trace(100, " jump : " + speed + " cost:" + jumpRoute.cost + " route:" + ((UdcMathCost)jumpRoute.route.get(0)).getName() + " ... " + ((UdcMathCost)jumpRoute.route.get(jrsz-1)).getName());
		if (curCostMax <= jumpRoute.cost) { return; }

		int ftrSz = (ftrList!=null) ? ftrList.size() : 0;
		ArrayList delFtrList = null;
		if (jumpRoute.delFtrList != null && jumpRoute.delFtrList.size() > 0) {
			dsz = jumpRoute.delFtrList.size();
			delFtrList = new ArrayList();
		}

		// jump前処理
		int pos;
		boolean bcheck = inhInfo.bcheck;
		svfprecost = new int[ftrSz];
		svfpostcost = new int[ftrSz];
		for (i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			svfprecost[i] = ftr.fprecost;
			svfpostcost[i] = ftr.fpostcost;
		}
		for (i=0; i<dsz; i++) {
			ftr = (UdcMathDijkstraFtr)jumpRoute.delFtrList.get(i);
			if ((pos=ftrList.indexOf(ftr)) < 0) { continue; }
			delFtr = new UdcMatchFtr(ftr);
			delFtr.pos = pos;
			delFtrList.add(delFtr);
			ftrList.remove(delFtr.pos);
		}
		for (i=jrsz-1; i>=0; i--) { disableNodeIn(((UdcMathCost)jumpRoute.route.get(i)).remote); }
		// jump経路探索
		if (shortest) { chgftr.addAll( jumpRoute.chgList ); }
		route.addAll(jumpRoute.route);
		int ret = searchAllRouteSub(((UdcMathCost)jumpRoute.route.get(jrsz-1)).remote, end, ftrList, inhInfo,
								route, (nowCost+jumpRoute.cost), hopMax, routeList, shortest, chgftr);
		for (i=0; i<jrsz; i++) { route.removeLast(); }
		if (shortest) { for (i=0; i<jrsz; i++) { chgftr.remove(jrsz); } }
		// jump後処理
		for (i=jrsz-1; i>=0; i--) { enableNodeIn(((UdcMathCost)jumpRoute.route.get(i)).remote); }
		if (delFtrList != null) {
			for (i=delFtrList.size()-1; i>=0; i--) {
				delFtr = (UdcMatchFtr)delFtrList.get(i);
				ftrList.add(delFtr.pos, delFtr.ftr);
			}
		}
		for (i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			ftr.fprecost = svfprecost[i];
			ftr.fpostcost = svfpostcost[i];
		}
		svfprecost = null;
		svfpostcost = null;
		delFtrList = null;
		inhInfo.bcheck = bcheck;
		return;
	}

	/**
	 * 幾何学パターン(n角形)の出現を検出し、探索経路(cos)を探索すべきか否かを判定する
	 *
	 * @return	探索経路(cos)を探索すべきか否か
	 * @param end	コストマトリックス上の経路終点位置
	 * @param cos	判定探索経路
	 * @param pcos	探索始点コスト
	 * @param hcosv 探索始点コストからcosまでの総コスト
	 * @param bcheck ボトルネックチェックをするか否か
	 * @since	UDC1.3
	 */
	private boolean isNeedSearchTopology(int end, UdcMathCost cos, UdcMathCost pcos, int hcosv, boolean bcheck)
	{
		int i, cnt, nsz, dst=cos.remote, src=pcos.local;
		UdcMathCost scos;

		ArrayList tbl = mtrxTbl_[src];
		nsz = tbl.size();
		// one-hop合流による探索要否判定
		boolean dreach = false;
		int already = nsz-1;
		for (cnt=0,i=0; i<nsz; i++) {
			scos = (UdcMathCost)tbl.get(i);
			scos.tmpenabled[1] = false;
			if (scos == pcos) { already = i; continue; }
			if (scos.isUnActive()) { continue; }
			// one-hop合流による探索要否判定
			if (dst == scos.remote) {
				// ONE-HOP優先
				//if (hcosv > scos.getCalcCost() || (i < already && hcosv == scos.getCalcCost())) {
				if (hcosv >= scos.getCalcCost()) {
//for debug
//tmpt1speed++;
					return false;
				}
				dreach = true;
				continue;
			}
			if (hcosv > scos.getCalcCost()) {
				cnt++;
				scos.tmpenabled[1] = true;
			}
		}
		if (cnt == 0) { return true; }
		// srcから分岐なしの擬似OneHop経路でのTwo-Hop/Tree-HOP/N-HOP経路への合流なら探索不要
		int nres; 
		ArrayList sroute = subRoute_;
		for (cnt=0,i=0; i<nsz; i++) {
			scos = (UdcMathCost)tbl.get(i);
			if (!scos.tmpenabled[1]) { continue; }
			sroute.clear();
			sroute.add(scos);
			if ((nres=isNeedSearchTopologyHop(sroute, dst, src, hcosv, scos.getCalcCost(), ((i<already)?true:false))) == 0) {
//for debug
//tmpt2speed++;
				return false;
			}
			if (nres == 2) { dreach = true; }
			if (scos.tmpenabled[1]) { cnt++; }
		}
		if (cnt == 0) { return true; }

		// N-hop合流による探索要否判定(合流点がボトルネックになっているケース)
		// (*)処理負荷が重いため、上位でチェック有無を指定させている
		if (bcheck) {
			boolean ret;
			// 目的ノードを非通過にして受ノードまでの到達性チェック
			disableNodeIn(dst);
			for (i=0; i<nsz; i++) { if (!(scos=(UdcMathCost)tbl.get(i)).tmpenabled[0]) { scos.tmpdisabled ++; } }
			int edcnt=0;
			if (cos.tmpNexthop >= 0 && cos.tmpNexthop != end && cos.tmpNexthop != dst) {
				r_reachNode_[0] = cos.tmpNexthop;
				edcnt = 1;
			}
			ret = isReachable(src, end, 0, edcnt);
			for (i=0; i<nsz; i++) { if (!(scos=(UdcMathCost)tbl.get(i)).tmpenabled[0]) { scos.tmpdisabled --; } }
			enableNodeIn(dst);
			if (ret) { return true; }
			// 目的ノードへ到達できないのでは代替経路は存在しない
			if (!dreach) {
				disableNodeOut(dst);
				ret = isReachable(src, dst, 0, 0);
				enableNodeOut(dst);
				if (!ret) { return true; }
			}
			// 目的ノードがボトルネックなら目的ノードへの最短経路が現探索経路より小さいなら不要
			// (*) 目的ノードを非通過にして受ノードまでの到達性チェックでは、ボトルネックエラーとなった経路は使用不可として
			//     無条件に探索要となってしまうことを制限しているが、下記は目的ノードがボトルネックであるため、過去のボトルネック
			//     判定の結果は反映させずに、純粋に代替経路と現経路のコスト比較をする。
//for debug
//tmpDspeed ++;
			disableNodeOut(dst);
			makeShortestRouteTable(src);
			enableNodeOut(dst);
			if (prevCost_[dst] != null) {
				scos = null;
				for (int pnode=dst; pnode != src && prevCost_[pnode] != null; pnode=scos.local) { scos = prevCost_[pnode]; }
				for (i=0; i<nsz; i++) {
					if (tbl.get(i) == scos) { break; }
				}	
				if (hcosv > routeVal_[dst] || (i < already && hcosv == routeVal_[dst])) {
//for debug
//tmpt3speed++;
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 分岐ノードから再合流ノードまでに代替経路が存在する場合の探索要否を判定する
	 *
	 * @return	探索経路を探索すべきか否か(0:探索不要/1:探索要/2:探索要(合流ノードまでの経路有)
	 * @param sroute 代替経路
	 * @param cpos	再合流ノード
	 * @param dpos	分岐ノード
	 * @param hcosv 分岐ノードから再合流ノードまでの総コスト
	 * @param nhcosv 分岐ノードから再合流ノードまでの代替経路の総コスト
	 * @param already 代替経路が既に探索済みか否か
	 * @since	UDC1.3
	 */
	private int isNeedSearchTopologyHop(ArrayList sroute, int cpos, int dpos, int hcosv, int nhcosv, boolean already)
	{
		int i, j, rn, cnt, ret, srsz=sroute.size()-1;
		UdcMathCost scos = (UdcMathCost)sroute.get(srsz);
		UdcMathCost sscos, tcos=null, ttcos=null;
		ArrayList tbl = mtrxTbl_[scos.remote];	
		int ssz = tbl.size();
		boolean ml = false;
		for (cnt=0, i=0; i<ssz && cnt < 2; i++) {
			sscos = (UdcMathCost)tbl.get(i);
			if (sscos.isUnActive()) { continue; }
			if (sscos.remote == dpos) { continue; }
			for (j=srsz; j>0; j--) {
				if (sscos.remote == ((UdcMathCost)sroute.get(j)).local) { break; }
			}
			if (j > 0) { continue; }
			if (sscos.remote == cpos) { tcos = sscos; continue; }
			// 行き止りの枝はカウントしない(本当はここで受&&cposに到達できない経路はcontinueしたいが遅くなってしまうのでここではしない)
			if ((ret=getOnlyOneHopRoute(sscos.remote, sscos.local)) == -1) { continue; }
			if (ret == -2) { ml = true; }
			ttcos = sscos;
			cnt ++;
		}
		if (tcos != null) {
			if (cnt == 0) {
				int total = nhcosv + tcos.getCalcCost();
				if (hcosv > total || (already && hcosv == total)) { return 0; }
				((UdcMathCost)sroute.get(0)).tmpenabled[1] = false;
			}
			return 2;
		}
		if (cnt == 1 && !ml && hcosv > (nhcosv+ttcos.getCalcCost())) {
			sroute.add(ttcos);
			return isNeedSearchTopologyHop(sroute, cpos, dpos, hcosv, (nhcosv+ttcos.getCalcCost()), already);
		}
		return 1;
	}

	/**
	 * 当該区間が使用可能かあるいは最短経路探索時の経路確定か否かをチェックする。<br>
	 * 
	 * @return 当該区間が使用可能かあるいは最短経路探索時の経路確定か否か(経路使用可:0/経路使用可(現状態取得なし):1/経路確定:2/経路使用不可:3以上)
	 * @param	start		コストマトリックス上の経路始点位置
	 * @param	end			コストマトリックス上の経路始点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @param	route		作成中経路
	 * @param	bcheck		ボトルネックチェック有無
	 * @param	nowCost		検索中の経路コスト
	 * @param	hopMax		ホップ数の上限
	 * @param	routeList	経路格納リスト
	 * @param	inhInfo	前経路までの到達性チェックでコスト超過となるノードリスト
	 * @param	shortest	最短経路のみか否か
	 * @param	jumpRouteList	任意区間のjump経路リスト
	 * @since	UDC1.3
	 */
	private int deterrentOrCompleteRoute(int start, int end, ArrayList ftrList, boolean bcheck,
						LinkedList route, int nowCost, int hopMax, LinkedList routeList,
						UdcInhNodeInfo inhInfo, boolean shortest, ArrayList jumpRouteList)
	{
		// ホップ数の上限は超えない
		if (route.size() >= hopMax) { return 2; }

		int i, j, rn=0, nsz, ret=0, k, tmax, pn, bn;
		int ftrSz = (ftrList!=null) ? ftrList.size() : 0;
		int curCostMax = allRouteCost_ - nowCost;
		// 最短経路探索の場合、通過条件を満たしているなら現検索経路が最短経路になる。
		if (ftrSz <= 0) {
			// 最短経路探索
			makeShortestRouteTable(start);
			if (prevCost_[end] == null || curCostMax <= routeVal_[end]) { return 3; }
			// 経路確定
			if (shortest) {
				allRouteCost_ = nowCost + routeVal_[end];
				routeList.clear();
				routeList.add( setRoute(start, end, (LinkedList)route.clone()) );
//for speed debug
//UdcTrace.trace(100, "** (" + speed + " cut: " + (speed - costspeed) + ") route - complete2  " + allRouteCost_);
				return 2;
			}
			return 0;
		}

		// 本チェック処理は高負荷のため、通過条件がある場合、前HOP/次HOPの状況を考慮してチェック処理を実施する
		// か否かを決定する。　但し、通過条件がない場合、現地点からの最短経路で経路を特定できる。
		UdcMathCost cos, pcos;
		ArrayList tbl;
		UdcMathDijkstraFtr ftr;
		// 1) startからの経路が1方路もないなら何もせずに終了
		int cnt = 0;
		tbl = mtrxTbl_[start];
		nsz = tbl.size();
		for (i=0; i<nsz; i++) {
			if (((UdcMathCost)tbl.get(i)).isActive()) { cnt ++; }
		}
		if (cnt == 0) { return 3; }
		inhInfo.bcheck = (cnt == 1) ? false : true;
		// 2) 通過条件の簡易ボトルネックチェック(通過条件の利用可能な入出力が一つしかない場合)
		// 	  通過条件の入出力が1つもない場合はPrevHop等で本関数の次の処理でチェックされているはずのためそのチェックしない
		// 	  また、通過ノードは入力経路しか止めていないため、通過条件の隣接ノード=startだとしても下記のチェックではNGとならない
		int in, out;
		for (i=0; i<ftrSz; i++) {
			rn = ((UdcMathDijkstraFtr)ftrList.get(i)).getFtrTpNode();
			if ((out=getOnlyOneHopRoute(rn, start)) >= 0 && (in=getOnlyOneHopRouteReverse(rn, -1)) >= 0 && in == out) { return 7; }
		}

		// 3-1) 受/通過条件ノードへの到達性チェック
		makeShortestRouteTable(start);
		if (prevCost_[end] == null || curCostMax <= routeVal_[end]) { return 4; }
		// 通過ノードへの到達性チェック
		// 　+
		// 通過条件ノードから受ノードまでの最短コストチェック
		// (*)以降の受ノードまでのダイクストラ実施して現状況での最短コストチェックをする前にPrevHopでの
		//	通過条件から受までの最短経路コスト使ってチェックすることで、無駄なダイクストラ探索を削減させる
		//	（PrevHopの通過条件から受までの最短コストは現時点の探索コストより絶対に大きくないコストである。）
		int ftrMinPos=0, ftrMinCost=Integer.MAX_VALUE;
		for (i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			rn = ftr.getFtrTpNode();
			if (prevCost_[rn] == null || curCostMax <= (routeVal_[rn] + ftr.fpostcost)) { return 5; }
			ftr.fprecost = nowCost + routeVal_[rn];
			if (ftrMode == FilterMode_NoOrder) {
				if (ftrMinCost > routeVal_[rn]) {
					ftrMinCost = routeVal_[rn];
					ftrMinPos = i;
				}
			}
		}

		// 3-2)通過条件ノードから受ノードまでの到達性チェック
		makeShortestRouteTableReverse(end);
		for (i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			rn = ftr.getFtrTpNode();
			if (r_prevCost_[rn] == null || allRouteCost_ <= (ftr.fprecost + r_routeVal_[rn])) { return 6; }
			ftr.fpostcost = r_routeVal_[rn];
		}

		// 3-3) 残通過条件による特殊処理
		if (shortest) {
			// 通過条件が一つの場合、最短経路を繋げればそれが最短経路
			// (*) over-head もあるが、遅い探索は遅いままでも、早い探索が増加するために実装
			if (ftrSz == 1) {
//for speed debug
//ftr1speed ++;
				// ftrSz==1ならrnには通過条件ノードの設定が残っているはず
				ftr = (UdcMathDijkstraFtr)ftrList.get(0);
				if (getBottoleneckNode(start, end, ftr.getFtrTpNode()) < 0) {
					allRouteCost_ = ftr.fprecost + ftr.fpostcost;
					LinkedList proute = (LinkedList)route.clone();
					setRoute(start, rn, proute);
					setRouteReverse(rn, end, proute);
					routeList.clear();
					routeList.add(proute);
//for speed debug
//ftr1mspeed ++;
//UdcTrace.trace(100, "** " + speed + " cut: " + (speed - costspeed)
//					+ " (" + costAspeed + "," + costBspeed +  "," + costCspeed + ":" + tmpt1speed + "/" + tmpt2speed + "/" + tmpt3speed + ") "
//					+ " ftr1cnt: " + ftr1mspeed + "/" + ftr1speed + " route - complete1  " + allRouteCost_);
					return 2;
				}
			} 
		}

		// 4) startからの出力経路の探索要不要チェック
		tbl = mtrxTbl_[start];
		nsz = tbl.size();
		for (cnt=0, i=0; i<nsz; i++) {
			cos = (UdcMathCost)tbl.get(i);
			cos.tmpenabled[0] = false;
			// コストの正常性チェック／コスト上限のチェック
			if (cos.isUnActive()) { continue; }
			rn = cos.remote;
			cos.tmpval = cos.getCalcCost();
			if (rn == end) {
				//if (ftrList!=null && !matchFilterLast(cos, ftrList)) { continue; }
				if (ftrSz > 0) { continue; }
				if (cos.tmpval >= curCostMax) { continue; }
				cos.tmpenabled[0] = true;
				cnt ++;
				continue;
			}
			// 探索前に算出した通過条件へのコスト表を利用して現探索位置からの到達性チェックを行う
			//  ftr.r_routeVal_[rn]             : 探索前の(rn)から通過条件までの最短コスト
			//  r_routeVal_[ftr.getFtrTpNode()] : 現状態での通過条件から受までの最短コスト
			if (r_prevCost_[rn] == null) { continue; }
			int tmpCurCostMax = curCostMax - cos.tmpval;
			int max = r_routeVal_[rn];
			if (max < tmpCurCostMax) {
				// 通過条件が複数ある場合には任意の二つの通過条件を通過する場合の総コストを算出する。
				int m, frn, nrn, arn;
				if (ftrMode == FilterMode_NoOrder) {
					UdcMathDijkstraFtr nftr, aftr;
					for (j=0; j<ftrSz && max<tmpCurCostMax; j++) {
						ftr = (UdcMathDijkstraFtr)ftrList.get(j);
						if ((frn=ftr.getFtrTpNode()) == rn) { continue; } 
						tmax = ftr.r_routeVal_[rn]+r_routeVal_[frn];
						for (k=j+1; k<ftrSz && tmax<tmpCurCostMax; k++) {
							nftr = (UdcMathDijkstraFtr)ftrList.get(k);
							if ((nrn=nftr.getFtrTpNode()) == rn) { continue; } 
							pn = ftr.r_routeVal_[rn] + ftr.routeVal_[nrn] + r_routeVal_[nrn]; 							// start->rn -> ftr -> nftr -> end
							if ((bn=nftr.r_routeVal_[rn] + nftr.routeVal_[frn] + r_routeVal_[frn]) < pn) { pn = bn; }	// start->rn -> nftr -> ftr -> end
							if (tmax < pn) { tmax = pn; }
							for (m=k+1; m<ftrSz && tmax<tmpCurCostMax; m++) {
								aftr = (UdcMathDijkstraFtr)ftrList.get(m);
								if ((arn=aftr.getFtrTpNode()) == rn) { continue; } 
								pn = ftr.r_routeVal_[rn] + ftr.routeVal_[nrn] + nftr.routeVal_[arn] + r_routeVal_[arn];							// start->rn -> ftr -> nftr -> aftr -> end
								if ((bn=ftr.r_routeVal_[rn] + ftr.routeVal_[arn] + aftr.routeVal_[nrn] + r_routeVal_[nrn]) < pn) { pn = bn; }	// start->rn -> ftr -> aftr -> nftr -> end
								if ((bn=nftr.r_routeVal_[rn] + nftr.routeVal_[frn] + ftr.routeVal_[arn] + r_routeVal_[arn]) < pn) { pn = bn; }	// start->rn -> nftr -> ftr -> aftr -> end
								if ((bn=nftr.r_routeVal_[rn] + nftr.routeVal_[arn] + aftr.routeVal_[frn] + r_routeVal_[frn]) < pn) { pn = bn; }	// start->rn -> nftr -> aftr -> ftr -> end
								if ((bn=aftr.r_routeVal_[rn] + aftr.routeVal_[frn] + ftr.routeVal_[nrn] + r_routeVal_[nrn]) < pn) { pn = bn; }	// start->rn -> aftr -> ftr -> nftr -> end
								if ((bn=aftr.r_routeVal_[rn] + aftr.routeVal_[nrn] + nftr.routeVal_[frn] + r_routeVal_[frn]) < pn) { pn = bn; }	// start->rn -> aftr -> nftr -> ftr -> end
								if (tmax < pn) { tmax = pn; }
							}
						}
						if (tmax > max) { max = tmax; }
					}
				} else {
					tmax = 0;
					for (j=0,nrn=rn; j<ftrSz && tmax<tmpCurCostMax; j++) {
						ftr = (UdcMathDijkstraFtr)ftrList.get(j);
						tmax += ftr.r_routeVal_[nrn];
						nrn = ftr.getFtrTpNode();
					}
					tmax += r_routeVal_[nrn];
					if (tmax > max) { max = tmax; }
				}
			}
			if (max >= tmpCurCostMax) {
//for speed debug
//costBspeed ++;
				continue;
			}
			cos.tmpval += max;
			cos.tmpNexthop = r_prevCost_[rn].remote;
			cos.tmpenabled[0] = true;
			cnt ++;
		}
		if (cnt == 0) { return 8; }

		// 5) コスト超過ノードを一時抑止
		//  	ftr.routeVal_[i]    : 探索前の通過条件から(i)までの最短コスト
		//  	ftr.r_routeVal_[i]  : 探索前の(i)から通過条件までの最短コスト
		// 	(*)探索前の最短コストは、現状態からするとかなり小さな値となっているはずであるが、
		// 	   それでも下記のチェックにおいて十分なコスト超過ノードを検出できる。 ftrからの最短
		// 	   コストを再算出するのは、処理負荷が重すぎてできないため、探索前に1回だけしかやらない
		// 	(*)本処理は、通過条件を2つだけ抽出することによって飛躍的に不要ノードを増加することが可能で
		//     あるが、通過条件指定が多いと、この処理が処理負荷のボトルネックになってしまうことがあるため、
		//     今後何らかの改善が必要である。
		int inhcnt=0;
		if (allRouteCost_ != Integer.MAX_VALUE) {
			UdcMathDijkstraFtr nftr;
			int nrn, tmp;
			for (int m=inhInfo.needSz-1; m>=0; m--) {
				i = inhInfo.needList[m];
				if (prevCost_[i] == null || r_prevCost_[i] == null) { continue; }
				tmax = routeVal_[i] + r_routeVal_[i];
				for (j=0; j<ftrSz && tmax<curCostMax; j++) {
					ftr = (UdcMathDijkstraFtr)ftrList.get(j);
					if ((rn=ftr.getFtrTpNode()) == i) { continue; }
					// 通過条件を1つだけ抽出して経由最少コストを算出
					pn = routeVal_[i] + ftr.r_routeVal_[i] + r_routeVal_[rn];	// start -> i -> 通過条件 -> 受 のコスト算出
					bn = routeVal_[rn] + ftr.routeVal_[i] + r_routeVal_[i]; 	// start - > 通過条件 -> i -> 受 のコスト算出
					tmp = (pn < bn) ? pn : bn;
					// 通過条件を2つだけ抽出して最少コストを算出
					for (k=j+1; k<ftrSz && tmp<curCostMax; k++) {
						nftr = (UdcMathDijkstraFtr)ftrList.get(k);
						if ((nrn=nftr.getFtrTpNode()) == i) { continue; }
						pn = routeVal_[i] + ftr.r_routeVal_[i] + ftr.routeVal_[nrn] + r_routeVal_[nrn];							// start -> i -> ftr -> nftr -> end
						if ((bn=routeVal_[rn] + ftr.routeVal_[i] + nftr.r_routeVal_[i] + r_routeVal_[nrn]) < pn) { pn = bn; }	// start -> ftr -> i -> nftr -> end
						if ((bn=routeVal_[rn] + ftr.routeVal_[nrn] + nftr.routeVal_[i] + r_routeVal_[i]) < pn) { pn = bn; }		// start -> ftr -> nftr -> i -> end
						if (ftrMode == FilterMode_NoOrder) {
							if ((bn=routeVal_[i] + nftr.r_routeVal_[i] + nftr.routeVal_[rn] + r_routeVal_[rn]) < pn) { pn = bn; }	// start -> i -> nftr -> ftr -> end
							if ((bn=routeVal_[nrn] + nftr.routeVal_[i] + ftr.r_routeVal_[i] + r_routeVal_[rn]) < pn) { pn = bn; }	// start -> nftr -> i -> ftr -> end
							if ((bn=routeVal_[nrn] + nftr.routeVal_[rn] + ftr.routeVal_[i] + r_routeVal_[i]) < pn) { pn = bn; }		// start -> nftr -> ftr -> i -> end
						}
						if (tmp < pn) { tmp = pn; }
					}
					// 残通過条件の経由において経由最大コストを取得する
					if (tmax < tmp) { tmax = tmp; }
				}
				// 本来は FilterMode_NoOrder は以下であるが、これだと ftr.routaVal_/r_routeVal_ 等探索前に取得したコスト値
				// での計算がほとんどになってしまうため、それだとあまり効果がでないため、NoOrderと同じ処理をさせる
				//if (ftrMode == FilterMode_NoOrder) {
				//	tmp = tmax;
				//	if (ftrSz > 0) {
				//		for (j=0; j<=ftrSz; j++) {
				//			nftr = (UdcMathDijkstraFtr)ftrList.get(0);
				//			pn = ((j == 0) ? (routeVal_[i] + nftr.r_routeVal_[i]) : (routeVal_[nftr.getFtrTpNode()]));
				//			for (k=1; k<ftrSz; k++) {
				//				ftr = (UdcMathDijkstraFtr)ftrList.get(k-1);
				//				nftr = (UdcMathDijkstraFtr)ftrList.get(k);
				//				pn += ((j != k) ? (nftr.r_routeVal_[ftr.getFtrTpNode()]) : (ftr.routeVal_[i] + nftr.r_routeVal_[i]));
				//			}
				//			pn += ((j == ftrSz) ? (nftr.routeVal_[i] + r_routeVal_[i]) : (r_routeVal_[nftr.getFtrTpNode()]));
				//			if (pn < tmp) { tmp = pn; }
				//		}
				//	}
				//	if (tmp > tmax) { tmax = tmp; }
				//}
				if (tmax >= curCostMax) {
					disableNodeInOut(i);
					inhInfo.regInh(m);
					inhcnt ++;
					// 通過条件が一時抑止対象となってしまったら、当該探索は不要
					for (j=0; j<ftrSz; j++) {
						if (i == ((UdcMathDijkstraFtr)ftrList.get(j)).getFtrTpNode()) { return 9; }
					}

				}
			}
		}

		// 6) ボトルネックチェック
		//  当該通過点への発/受経路が重複している場合、ボトルネックノードを一時抑止して到達できるか確認
		//	(現地点→通過/通過→受までの到達性があっても重複した経路使用しかできなければ到達できない)
		if (bcheck) {
			boolean unreach = false;
			int m, rsz, cm=3;
			int stcnt, strcnt, edrcnt, st_reachNode=0, st_r_reachNode=0, ed_r_reachNode=0;
			// ボトルネックを除外した到達性チェック
			for (i=0; i<ftrSz; i++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				rn = ftr.getFtrTpNode();
				// ボトルネックノードの検出
				if ((cnt=getBottoleneckNode(start, end, ftr)) <= 0) { continue; }
				// 通過条件の別のNbrから受けまでの経路を使用しても重複しているノードだけに絞込む(ボトルネックを通過しないなら削除)
				tbl = mtrxTbl_[rn];
				for (k=tbl.size()-1; k>=0; k--) {
					cos = (UdcMathCost)tbl.get(k);
					if (cos == r_prevCost_[rn]) { continue; }
					pn = cos.remote;
					if (cos.isUnActive() || r_prevCost_[pn] == null || r_prevCost_[pn].remote == rn) { continue; }
					removeNotNeedBottoleneckNodeReverse(ftr, pn, end);
				}
				if ((rsz=ftr.bnode.size()) <= 0) { continue; }
				// 発から通過条件の別のNbrまでの経路を使用しても重複しているノードだけに絞込む(ボトルネックを通過しないなら削除)
				tbl = r_mtrxTbl_[rn];
				for (k=tbl.size()-1; k>=0; k--) {
					cos = (UdcMathCost)tbl.get(k);
					if (cos == prevCost_[rn]) { continue; }
					pn = cos.local;
					if (cos.isUnActive() || prevCost_[pn] == null || prevCost_[pn].local == rn) { continue; }
					removeNotNeedBottoleneckNode(ftr, start, pn);
				}
				if ((rsz=ftr.bnode.size()) <= 0) { continue; }

				// ボトルネック存在有無チェックでは isReachableを使用するが、このチェックはstart/end側からの並行幅探索
				// であるため、start/end間のHop数が多い場合探索負荷が重くなる。これを改善するため、予め到達性が分かっている
				// ノードをisReachable中で使用する到達性データに設定して探索するようにする。
				stcnt = strcnt = edrcnt = 0;
				bn = ((Integer)ftr.bnode.get(rsz-1)).intValue();
				if (prevCost_[bn].local != start) {
					st_reachNode = prevCost_[bn].local;
					stcnt = 1;
				}
				if (r_prevCost_[bn].remote != end) {
					ed_r_reachNode = r_prevCost_[bn].remote;
					edrcnt = 1;
				}
				bn = ((Integer)ftr.bnode.get(0)).intValue();
				for (pn=r_prevCost_[rn].remote; pn!=bn; pn=r_prevCost_[pn].remote) {
					st_r_reachNode = pn;
					strcnt = 1;
				}
				// ボトルネック存在有無チェック:(cm=3)ノードずつチェック(ボトルネック未通過で到達性があるかチェック)
				for (k=0; k<rsz; k+=cm) {
					cnt = k + cm;
					unreach = false;
					for (j=k; j<cnt && j<rsz; j++) { disableNodeIn(((Integer)ftr.bnode.get(j)).intValue()); }
					if (stcnt > 0) { reachNode_[0] = st_reachNode; }
					if (strcnt > 0) { r_reachNode_[0] = st_r_reachNode; }
					if (!isReachable(start, rn, stcnt, strcnt)) {
						if (strcnt > 0) { reachNode_[0] = st_r_reachNode; }
						if (edrcnt > 0) { r_reachNode_[0] = ed_r_reachNode; }
						if (!isReachable(rn, end, strcnt, edrcnt)) { unreach = true; }
					}
					for (j=k; j<cnt && j<rsz; j++) { enableNodeIn(((Integer)ftr.bnode.get(j)).intValue()); }
					if (unreach) {
						if ((k+1) < rsz) {
							unreach = false;
							for (j=k; j<cnt && j<rsz; j++) {
								pn = ((Integer)ftr.bnode.get(j)).intValue();
								disableNodeIn(pn);
								if (stcnt > 0) { reachNode_[0] = st_reachNode; }
								if (strcnt > 0) { r_reachNode_[0] = st_r_reachNode; }
								if (!isReachable(start, rn, stcnt, strcnt)) {
									if (strcnt > 0) { reachNode_[0] = st_r_reachNode; }
									if (edrcnt > 0) { r_reachNode_[0] = ed_r_reachNode; }
									if (!isReachable(rn, end, strcnt, edrcnt)) { unreach = true; }
								}
								enableNodeIn(pn);
								if (unreach) { break; }
							}
						}
						if (unreach) {
//for debug
//costAspeed++;
							return 7;
						}
					}
				}
			}
		}

		// 7) 次HOP以降の探索を効率的に実施するための施策
		if (shortest) {
			// 1-1) 探索順序を通過条件に近い順にするために一番近い通過条件へのNbrを探索順序の最初に並び替え
			//	 これをしないと意味不明に全経路を探索してしまう(早めに最短経路が探索できればそれだけコスト抑止で
			//	 探索数を抑えることができる)
			ftr = (UdcMathDijkstraFtr)ftrList.get(ftrMinPos);
			int pnode;
			for (pnode=ftr.getFtrTpNode(), cos=null; pnode != start && prevCost_[pnode] != null; pnode=prevCost_[pnode].local) {
				cos = prevCost_[pnode];
			}
			if (cos != null && mtrxTbl_[start].remove(cos)) { mtrxTbl_[start].add(0, cos); }
			// 1-2) 一定区間をJump可能なら jumpRouteにjump区間を設定する
			if (jumpRouteList != null && (cnt=ftr.brouteList.size()) > 0) {
				int jrsz;
				UdcJumpRoute jumpRoute;
				for (i=0; i<cnt; i++) {
					jumpRoute = (UdcJumpRoute)ftr.brouteList.get(i);
					if (jumpRoute.src == start) {
						jrsz = jumpRoute.route.size();
						jumpRouteList.add(jumpRoute);
						break;
					}
				}
				//// NextHopが1つしかない場合もjumpさせてしまう
				//if (jumpRouteList.size() <= 0) {
				//	rn = start;
				//	in = (route.size() > 0) ? ((UdcMathCost)route.getLast()).remote : -1;
				//	jumpRoute = null;
				//	while ((out=getOnlyOneHopRoute(rn, in)) >= 0) {
				//		if (jumpRoute == null) {
				//			jumpRoute = new UdcJumpRoute(start, new ArrayList());
				//			jumpRoute.chgList = new ArrayList();
				//			jumpRoute.delFtrList = new ArrayList();
				//		}
				//		jumpRoute.route.add(getCost(rn, out));
				//		for (i=0; i<ftrSz; i++) {
				//			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				//			if (ftr.getFtrTpNode() == out) {
				//				if (ftrMode == FilterMode_Order && i != jumpRoute.delFtrList.size()) { return 5; }
				//				jumpRoute.chgList.add(Boolean.TRUE);	
				//				jumpRoute.delFtrList.add(ftr);	
				//				break;
				//			}
				//		}
				//		if (i >= ftrSz) { jumpRoute.chgList.add(Boolean.FALSE); }
				//		in = rn;
				//		rn = out;
				//	}
				//	if (jumpRoute != null && jumpRoute.route.size() >= 2) { jumpRouteList.add(jumpRoute); }
				//}
			}
			// (1-3)
			// 		Farther study
			//
		}

		return ret;
	}

	/**
	 * 全経路探索における経路探索前の通過条件の正規化をする。
	 *
	 * @return 通過経路に対応するフィルタ条件を削除した新規のフィルタ情報
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 * @param	firstFtrList	発からの連続した通過条件リスト格納エリア
	 * @param	lastFtrList		受への連続した通過条件リスト格納エリア
	 */
	private ArrayList matchFilterFirst(int start, int end, ArrayList ftrList, ArrayList firstFtrList, ArrayList lastFtrList)
	{
		if (ftrList == null) { return null; }

		int i,j,rn;
		Object ftr,tftr;
		UdcMathCost cos,tcos;
		ArrayList flist = new ArrayList();
		// 通過条件:順序指定
		if (ftrMode == FilterMode_Order) {
			for (i=0; i<ftrList.size(); i++) {
				ftr = ftrList.get(i);
				if (ftr instanceof Integer) {
					int node = ((Integer)ftr).intValue();
					if (i == 0 && node == start) { continue; }
					if (i == (ftrList.size()-1) && node == end) { continue; }
					if (node == start || node == end) { return null; }
					for (j=i+1; j<ftrList.size(); j++) {
						tftr = ftrList.get(j);
						if (tftr instanceof Integer) {		
							if (node == ((Integer)tftr).intValue()) { return null; }
						} else if (tftr instanceof UdcMathCost) {
							cos = (UdcMathCost)tftr;
							if (j != (i+1) && node == cos.local) { return null; }
							if (node == cos.remote) { return null; }
						}
					}
				} else if (ftr instanceof UdcMathCost) {
					cos = (UdcMathCost)ftr;		
					for (j=0; j<ftrList.size(); j++) {
						if (i == j) { continue; }
						tftr = ftrList.get(j);
						if (tftr instanceof UdcMathCost) {
							tcos = (UdcMathCost)tftr;		
							if (cos.local == tcos.local) { return null; }
							if (cos.remote == tcos.remote) { return null; }
							if (j > i && cos.local == tcos.remote) { return null; }
						}
					}
				} else {
					return null;	// Filter異常
				}
				flist.add(ftr);
			}
			// 発からのの連続した通過条件もFilter条件から削除する場合
			if (firstFtrList != null) {
				rn = start;
				for (i=0; i<flist.size(); i++) {
					ftr = flist.get(i);
					if (!(ftr instanceof UdcMathCost)) { break; }
					cos = (UdcMathCost)ftr;	
					if (cos.local != rn) { break; }
					flist.remove(i);
					firstFtrList.add(cos);
					rn = cos.remote;
					i --;
				}
				if (rn == end && flist.size() > 0) { return null; }
			}
			// 受までの連続した通過条件もFilter条件から削除する場合
			if (lastFtrList != null && flist.size() > 0) {
				rn = end;
				for (i=flist.size()-1; i>=0; i--) {
					ftr = flist.get(i);
					if (!(ftr instanceof UdcMathCost)) { break; }
					cos = (UdcMathCost)ftr;		
					if (cos.remote != rn) { break; }
					flist.remove(i);
					lastFtrList.add(0,cos);
					rn = cos.local;
				}
			}

		// 通過条件:順序未指定
		} else {
			for (i=0; i<ftrList.size(); i++) {
				ftr = ftrList.get(i);
				if (ftr instanceof Integer) {
					int node = ((Integer)ftr).intValue();
					if (node == start || node == end) { continue; }
					boolean need = true;
					for (j=0; j<ftrList.size(); j++) {
						if (i == j) { continue; }
						tftr = ftrList.get(j);
						if (tftr instanceof Integer) {		
							if (node == ((Integer)tftr).intValue()) { need = false; }
						} else if (tftr instanceof UdcMathCost) {
							cos = (UdcMathCost)tftr;
							if (node == cos.local || node == cos.remote) { need = false; }
						}
						if (!need) { break; }
					}
					if (!need) { continue; }
				} else if (ftr instanceof UdcMathCost) {
					cos = (UdcMathCost)ftr;		
					for (j=0; j<ftrList.size(); j++) {
						if (i == j) { continue; }
						tftr = ftrList.get(j);
						if (tftr instanceof UdcMathCost) {
							tcos = (UdcMathCost)tftr;		
							if (cos.local == tcos.local) { return null; }
							if (cos.remote == tcos.remote) { return null; }
						}
					}
				} else {
					return null;	// Filter異常
				}
				flist.add(ftr);
			}
			// 発からのの連続した通過条件もFilter条件から削除する場合
			if (firstFtrList != null) {
				rn = start;
				for (i=0; i<flist.size(); i++) {
					ftr = flist.get(i);
					if (!(ftr instanceof UdcMathCost)) { continue; }
					cos = (UdcMathCost)ftr;	
					if (cos.local == rn) {
						flist.remove(i);
						firstFtrList.add(cos);
						rn = cos.remote;
						i = -1;
					}
				}
				if (rn == end && flist.size() > 0) { return null; }
			}
			// 受までの連続した通過条件もFilter条件から削除する場合
			if (lastFtrList != null && flist.size() > 0) {
				rn = end;
				for (i=flist.size()-1; i>=0; i--) {
					ftr = flist.get(i);
					if (!(ftr instanceof UdcMathCost)) { continue; }
					cos = (UdcMathCost)ftr;	
					if (cos.remote == rn) {
						flist.remove(i);
						lastFtrList.add(0, cos);
						rn = cos.local;
						i = flist.size();
					}
				}
			}
		}

		// 正規化された通過条件を探索用の通過条件リストに変換
		ArrayList filterList = new ArrayList();
		for (i=0; i<flist.size(); i++) {
			ftr = flist.get(i);
			if (ftr instanceof Integer) {
				filterList.add(new UdcMathDijkstraFtr(((Integer)ftr).intValue()));	
			} else {
				filterList.add(new UdcMathDijkstraFtr((UdcMathCost)ftr));	
			}
		}
		return filterList;
	}

	/**
	 * 全経路探索における中間区間での通過条件判定をし、通過経路の通過条件を削除する。
	 *
	 * @return 	通過可能か否か
	 * @param	cos	当該区間のコスト
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @param	delFtr	通過経路に対応するftrListから削除したフィルタ条件
	 */
	private boolean matchFilterDelete(UdcMathCost cos, ArrayList ftrList, UdcMatchFtr delFtr)
	{
		int i, ftrSz;
		if (ftrList == null || (ftrSz=ftrList.size()) <= 0) { return true; }
		UdcMathDijkstraFtr ftr;
		// 高速化のため処理を分ける
		if (ftrMode == FilterMode_NoOrder) {
			for (i=0; i<ftrSz; i++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				// 通過チェック
				if (ftr.getFtrNode() != null) {
					if (ftr.getFtrTpNode() == cos.remote) {
						ftrList.remove(i);
						delFtr.ftr = ftr;
						delFtr.pos = i;
						break;	// 重複するフィルタ条件はmatchFilterFirstで全て削除しているため
					}
				} else {
					if (cos == ftr.getFtrCost()) {
						ftrList.remove(i);
						delFtr.ftr = ftr;
						delFtr.pos = i;
						break;	// 重複するフィルタ条件はmatchFilterFirstで全て削除しているため
					}
				}
			}
		} else {
			boolean match = false;
			boolean orderMisMatch = false;
			for (i=0; i<ftrSz; i++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				// 通過チェック
				match = false;
				if (ftr.getFtrNode() != null) {
					if (ftr.getFtrTpNode() == cos.remote) { match = true; }
				} else {
					if (cos == ftr.getFtrCost()) { match = true; }
				}
				if (match) {
					if (orderMisMatch) { return false; }
					ftrList.remove(i);
					delFtr.ftr = ftr;
					delFtr.pos = i;
					i --;	ftrSz --;
				} else {
					orderMisMatch = true;
				}
			}
		}
		return true;
	}

	//現在は探索前に受ノード直前の通過条件がある場合、その通過条件のlocal側を仮想受として
	//全件探索をするため、ここでは通過条件が1つでもあればエラーとなる。
	/**
	 * 全経路探索における最終区間での通過条件判定をする。
	 *
	 * @return	通過条件に符号するか否か
	 * @param	cos	当該区間のコスト
	 * @param	ftrList		通過条件リスト（通過義務のある通過条件）
	 *
	private boolean matchFilterLast(UdcMathCost cos, ArrayList ftrList)
	{
		if (ftrList == null) { return true; }
		int i, ftrSz=ftrList.size();
		UdcMathDijkstraFtr ftr;
		boolean match = true;
		boolean last = false;
		for (i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			match = false;
			if (ftrMode == FilterMode_NoOrder) {
				if (ftr.getFtrNode() != null) {
					if (ftr.getFtrTpNode() == cos.remote) { match = true; }
				} else {
					if (cos == ftr.getFtrCost()) { match = true; }
				}
			} else if (ftrMode == FilterMode_Order) {
				if (ftr.getFtrNode() != null) {
					if (ftr.getFtrTpNode() == cos.remote) {
						last = true;
						match = true;
					}
				} else if (!last) {
					if (cos == ftr.getFtrCost()) { match = true; }
				}
			}
			if (!match) { break; }
		}
		return match;
	}
	**/

	/**
	 * 任意の区間の経路が存在するか否かをstartから探索して取得する。
	 *
	 * @return	経路が存在するか否か
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置(負値を指定すると戻りはfalseとなるが、unsedNode_[]に全てのノードへの到達性が設定される)
	 * @since	UDC1.1
	 */
	final public boolean isReachable(int start, int end)
	{
		return isReachable(start, end, 0, 0);
	}

	/**
	 * 任意の区間の経路が存在するか否かをstartから探索して取得する。
	 *  (*) isReachableは内部探索での利用頻度が高いため、高速化のためstcnt/edcntを負荷した高速探索を実現する。
	 *
	 * @return	経路が存在するか否か
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置(負値を指定すると戻りはfalseとなるが、unsedNode_[]に全てのノードへの到達性が設定される)
	 * @param	stcnt	start側からの検索で既に到達性のあるノード数(reachNode_に予め指定数分のノードを設定しておく必要があります。)
	 * @param	edcnt 	end側からの検索で既に到達性のあるノード数(r_reachNode_に予め指定数分のノードを設定しておく必要があります。)
	 * @since	UDC1.1
	 */
	final private boolean isReachable(int start, int end, int stcnt, int edcnt)
	{
//for debug
//tmpCspeed ++;
		int i, rn, cnt=stcnt, rcnt=edcnt, st=0, rst=0, nsz;
		UdcMathCost cos;

		// 初期化
		// 	(*)クラス変数へのアクセスは速度低下を招くため、クラス変数をローカル変数に移して処理する。
		// 	   本メソッドだけは特別に高速しなければならないため
		int[] reachFlag = reachFlag_;
		int[] reachNode = reachNode_;
		int[] r_reachNode = r_reachNode_;
		Arrays.fill(reachFlag, 0);

		// 到達性評価(幅探索によりstart/end側から並行探索)
		//  (*) 両側から並行探索するのは、とhop数に対してノード分布が一定の場合
		//        両側からだと 2r^2
		//        片側からだと (2r)^2=4r^2
		//      となり倍の面積を探索することになってしまうため。
		//  (*)stcnt/edcntで中間のノードを指定しておくことで、到達性がある場合には
		//     早く到達性を判別することができる。
		reachFlag[start] = 1;
		ArrayList tbl = mtrxTbl_[start];
		if (stcnt > 0) {
			for (i=0; i<stcnt; i++) { reachFlag[reachNode[i]] = 1; }
			st = 1;
			tbl = mtrxTbl_[reachNode[0]];
			reachNode[cnt] = start;
			cnt ++;
		}
		reachFlag[end] = 2;
		ArrayList rtbl = r_mtrxTbl_[end];
		if (edcnt > 0) {
			for (i=0; i<edcnt; i++) { reachFlag[r_reachNode[i]] = 2; }
			rst = 1;
			rtbl = r_mtrxTbl_[r_reachNode[0]];
			r_reachNode[rcnt] = end;
			rcnt ++;
		}

		while (true) {
			// 隣接ノードの到達性チェック
			nsz = tbl.size();
			for (i=0; i<nsz; i++) {
				cos = (UdcMathCost)tbl.get(i);
				if (cos.isUnActive()) { continue; }
				rn = cos.remote;
				if (reachFlag[rn] == 0) {
					//if (rn == end) { return true; }
					reachFlag[rn] = 1;
					reachNode[cnt] = rn;
					cnt ++;
				} else if (reachFlag[rn] == 2) {
					return true;
				}
			}
			if (st == cnt) { break; }
			tbl = mtrxTbl_[reachNode[st]];
			st ++;
			// endからの隣接ノードの到達性チェック
			nsz = rtbl.size();
			for (i=0; i<nsz; i++) {
				cos = (UdcMathCost)rtbl.get(i);
				if (cos.isUnActive()) { continue; }
				rn = cos.local;
				if (reachFlag[rn] == 0) {
					//if (rn == start) { return true; }
					reachFlag[rn] = 2;
					r_reachNode[rcnt] = rn;
					rcnt ++;
				} else if (reachFlag[rn] == 1) {
					return true;
				}
			}
			if (rst == rcnt) { break; }
			rtbl = r_mtrxTbl_[r_reachNode[rst]];
			rst ++;
		}
		return false;
	}

	/**
	 * 始点からのルートテーブルを作成する。
	 *
	 * @param	start	コストマトリックス上の経路始点位置
	 * @since	UDC1.2
	 */
	final private void makeShortestRouteTable(int start)
	{
//for debug
//tmpAspeed ++;
		int i, rn, ac, mincost=0, nsz, cnt=0, pos;
		UdcMathCost cos;
		// 初期化
		// 	(*)クラス変数へのアクセスは速度低下を招くため、クラス変数をローカル変数に移して処理する。
		// 	   本メソッドだけは特別に高速しなければならないため
		boolean[] unuseFlag = unuseFlag_;
		int[] routeVal = routeVal_;
		int[] unuseNode = unuseNode_;
		UdcMathCost[] prevCost = prevCost_;
		Arrays.fill(unuseFlag, true);
		Arrays.fill(routeVal, Integer.MAX_VALUE);
		Arrays.fill(prevCost, null);
		// コスト評価
		routeVal[start] = 0;
		unuseFlag[start] = false;
		ArrayList tbl = mtrxTbl_[start];
		while (true) {
			// 隣接ノードのコスト更新
			nsz = tbl.size();
			for (i=0; i<nsz; i++) {
				cos = (UdcMathCost)tbl.get(i);
				if (cos.isUnActive()) { continue; }
				rn = cos.remote;
				if ((ac=(mincost+cos.getCalcCost())) < routeVal[rn]) {
					routeVal[rn] = ac;
					prevCost[rn] = cos;
					if (unuseFlag[rn]) {
						unuseFlag[rn] = false;
						unuseNode[cnt] = rn;
						cnt ++;
					}
				}
			}
			if (cnt <= 0) { break; }
			// 未使用の最少コストを決定(高速化のため処理を改善)
			// (*)通常はunuseNodeをリストにしてコスト更新時にコスト順に格納しておき本処理コストを 0 にする
			//    が、リストのadd/remove処理コストがjavaではとても遅くまた、本処理の方が倍程度の速度を有する
			//    ことから本処理としている。
			pos = 0;
			mincost = routeVal[unuseNode[0]];
			for (i=1; i<cnt; i++) {
				rn = unuseNode[i];
				if (mincost > routeVal[rn]) {
					mincost = routeVal[rn];
					pos = i;
				}
			}
			tbl = mtrxTbl_[unuseNode[pos]];
			cnt --;
			if (cnt > 0 && pos != cnt) {
				unuseNode[pos] = unuseNode[cnt]; 	// System.arraycopy(unuseNode, pos+1, unuseNode, pos, cnt-pos);
			}
		}
	}

	/**
	 * 終点からのルートテーブルを作成する。
	 *
	 * @param	end	コストマトリックス上の経路終点位置
	 * @since	UDC1.2
	 */
	final private void makeShortestRouteTableReverse(int end)
	{
//for debug
//tmpBspeed ++;
		int i, rn, ac, mincost=0, nsz, cnt=0, pos;
		UdcMathCost cos;

		// 初期化
		// 	(*)クラス変数へのアクセスは速度低下を招くため、クラス変数をローカル変数に移して処理する。
		// 	   本メソッドだけは特別に高速しなければならないため
		boolean[] r_unuseFlag = r_unuseFlag_;
		int[] r_routeVal = r_routeVal_;
		int[] r_unuseNode = r_unuseNode_;
		UdcMathCost[] r_prevCost = r_prevCost_;
		Arrays.fill(r_unuseFlag, true);
		Arrays.fill(r_routeVal, Integer.MAX_VALUE);
		Arrays.fill(r_prevCost, null);
		// コスト評価
		r_routeVal[end] = 0;
		r_unuseFlag[end] = false;
		ArrayList tbl = r_mtrxTbl_[end];
		while (true) {
			// 隣接ノードのコスト更新
			nsz = tbl.size();
			for (i=0; i<nsz; i++) {
				cos = (UdcMathCost)tbl.get(i);
				if (cos.isUnActive()) { continue; }
				rn = cos.local;
				if ((ac=(mincost+cos.getCalcCost())) < r_routeVal[rn]) {
					r_routeVal[rn] = ac;
					r_prevCost[rn] = cos;
					if (r_unuseFlag[rn]) {
						r_unuseFlag[rn] = false;
						r_unuseNode[cnt] = rn;
						cnt ++;
					}
				}
			}
			if (cnt <= 0) { break; }
			// 未使用の最少コストを決定(高速化のため処理を改善)
			// (*)通常はunuseNodeをリストにしてコスト更新時にコスト順に格納しておき本処理コストを 0 にする
			//    が、リストのadd/remove処理コストがjavaではとても遅くまた、本処理の方が倍程度の速度を有する
			//    ことから本処理としている。
			pos = 0;
			mincost = r_routeVal[r_unuseNode[0]];
			for (i=1; i<cnt; i++) {
				rn = r_unuseNode[i];
				if (mincost > r_routeVal[rn]) {
					mincost = r_routeVal[rn];
					pos = i;
				}
			}
			tbl = r_mtrxTbl_[r_unuseNode[pos]];
			cnt --;
			if (cnt > 0 && pos != cnt) {
				r_unuseNode[pos] = r_unuseNode[cnt]; 	// System.arraycopy(r_unuseNode, pos+1, r_unuseNode, pos, cnt-pos); 
			}
		}
	}

	/**
	 * 順方向/逆方向検索による交点(ボトルネック)ノードを取得する
	 *
	 * @return 交点(ボトルネックノード)ノード
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @param	mid		コストマトリックス上の経路中間点位置
	 * @since	UDC1.3
	 */
	private int getBottoleneckNode(int start, int end, int mid)
	{
		int pn, sn;
		UdcMathCost scos, ecos;
		for (pn=mid; pn != start && prevCost_[pn] != null; pn = ecos.local) {
			ecos = prevCost_[pn];
			for (sn=mid; sn != end && r_prevCost_[sn] != null; sn=scos.remote) {
				scos = r_prevCost_[sn];
				if (ecos.local == scos.remote) { return ecos.local; }
			}
		}
		return -1;
	}

	/**
	 * 順方向/逆方向検索による交点(ボトルネック)ノードを取得する
	 *
	 * @return 交点(ボトルネックノード)ノード数
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @param	ftr		通過条件
	 * @since	UDC1.3
	 */
	private int getBottoleneckNode(int start, int end, UdcMathDijkstraFtr ftr)
	{
		ftr.bnode.clear();

		int pn, sn, en, cnt=0, mid=ftr.getFtrTpNode();
		UdcMathCost scos, ecos, cecos=null;
		for (pn=mid; pn != start && prevCost_[pn] != null; pn=ecos.local) {
			ecos = prevCost_[pn];
			en = getOnlyOneHopRouteReverse(ecos.local, ecos.remote);
			sn = (cecos != null) ? cecos.local : mid;
			for (; sn != end && r_prevCost_[sn] != null; sn=scos.remote) {
				scos = r_prevCost_[sn];
				if (ecos.local == scos.remote) {
					if (cecos == null || en < 0 || en != getOnlyOneHopRoute(scos.remote, scos.local)) {
						ftr.bnode.add(nodeList_[ecos.local]);
					}
					cecos = ecos;
					break;
				}
			}
		}
		return ftr.bnode.size();
	}

	/**
	 * getBottoleneckNode(int start, int end, UdcMathDijkstraFtr ftr)で取得したボトルネックノードで
	 * 不要なノードを削除する。
	 *
	 * @param	ftr		通過条件
	 * @param	node	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @since	UDC1.3
	 */
	private void removeNotNeedBottoleneckNode(UdcMathDijkstraFtr ftr, int start, int end)
	{
		int j, bn, pn;
		boolean unreach;
		for (j=ftr.bnode.size()-1; j>=0; j--) {
			bn = ((Integer)ftr.bnode.get(j)).intValue();
			unreach = true;
			for (pn=end; pn!=start && prevCost_[pn]!=null; pn=prevCost_[pn].local) {
				if (prevCost_[pn].local == bn) { unreach = false; break; }
			}
			if (unreach) { ftr.bnode.remove(j); }
		}
	}

	/**
	 * getBottoleneckNode(int start, int end, UdcMathDijkstraFtr ftr)で取得したボトルネックノードで
	 * 不要なノードを削除する。
	 *
	 * @param	ftr		通過条件
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @since	UDC1.3
	 */
	private void removeNotNeedBottoleneckNodeReverse(UdcMathDijkstraFtr ftr, int start, int end)
	{
		int j, bn, pn;
		boolean unreach;
		for (j=ftr.bnode.size()-1; j>=0; j--) {
			bn = ((Integer)ftr.bnode.get(j)).intValue();
			unreach = true;
			for (pn=start; pn!=end && r_prevCost_[pn]!=null; pn=r_prevCost_[pn].remote) {
				if (r_prevCost_[pn].remote == bn) { unreach = false; break; }
			}
			if (unreach) { ftr.bnode.remove(j); }
		}
	}

	/**
	 * 探索後の最短隣接コスト表から探索経路を設定する
	 *
	 * @return 探索経路(routeを返却)
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @param	route	経路格納領域
	 * @since	UDC1.3
	 */
	private LinkedList setRoute(int start, int end, LinkedList route)
	{
		int pos = route.size();
		for (int pn=end; pn != start && prevCost_[pn] != null; pn=prevCost_[pn].local) {
			route.add(pos, prevCost_[pn]);
		}
		return route;
	}

	/**
	 * 逆探索後の最短隣接コスト表から探索経路を設定する
	 *
	 * @return 探索経路(routeを返却)
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路終点位置
	 * @param	route	経路格納領域
	 * @since	UDC1.3
	 */
	private LinkedList setRouteReverse(int start, int end, LinkedList route)
	{
		for (int pn=start; pn != end && r_prevCost_[pn] != null; pn=r_prevCost_[pn].remote) {
			route.add(r_prevCost_[pn]);
		}
		return route;
	}

	/**
	 * 探索後の最短隣接コスト表から任意の経路が指定ノードを通過しているか否かを判定する。
	 *
	 * @return 任意の経路が指定ノードを通過している場合はstartからnodeまでのコスト（非通過時は-1）
	 * @param node 指定ノード
	 */
	private int isContainRoute(int start, int end, int node)
	{
		if (start == node) { return 0; }
		int total = 0;
		for (int pnode=end; pnode != start && prevCost_[pnode] != null; pnode=prevCost_[pnode].local) {
			total += prevCost_[pnode].getCalcCost();
			if (prevCost_[pnode].local == node) { return (routeVal_[end] - total); }
		}
		if (end == node) { return total; }
		return -1;
	}

	/**
	 * 逆探索後の最短隣接コスト表から任意の経路が指定ノードを通過しているか否かを判定する。
	 *
	 * @return 任意の経路が指定ノードを通過している場合はstartからnodeまでのコスト（非通過時は-1）
	 * @param node 指定ノード
	 */
	private int isContainRouteReverse(int start, int end, int node)
	{
		if (start == node) { return 0; }
		int pnode=start, total=0;
		for (; pnode != end && r_prevCost_[pnode] != null; pnode=r_prevCost_[pnode].remote) {
			total += r_prevCost_[pnode].getCalcCost();
			if (r_prevCost_[pnode].remote == node) { return total; }
		}
		if (end == node) { return total; }
		return -1; 
	}

	/**
	 * 指定ノードの入力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @since	UDC1.3
	 */
	private void enableNodeIn(int node)
	{
		ArrayList tbl = r_mtrxTbl_[node];
		for (int i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled --; }
	}

	/**
	 * 指定ノードの入力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @since	UDC1.3
	 */
	private void disableNodeIn(int node)
	{
		ArrayList tbl = r_mtrxTbl_[node];
		for (int i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled ++; }
	}

	/**
	 * 指定ノードの出力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @since	UDC1.3
	 */
	private void enableNodeOut(int node)
	{
		ArrayList tbl = mtrxTbl_[node];
		for (int i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled --; }
	}

	/**
	 * 指定ノードの出力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @since	UDC1.3
	 */
	private void disableNodeOut(int node)
	{
		ArrayList tbl = mtrxTbl_[node];
		for (int i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled ++; }
	}

	/**
	 * 指定ノードの入出力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @since	UDC1.3
	 */
	private void enableNodeInOut(int node)
	{
		int i;
		ArrayList tbl = r_mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled --; }
		tbl = mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled --; }
	}

	/**
	 * 指定ノードの入出力コスト抑止カウンタを更新する。
	 *
	 * @param	node	コストマトリックス上のノード位置
	 * @param	up		抑止カウンタを加算(true)するか減算(false)するか
	 * @since	UDC1.3
	 */
	private void disableNodeInOut(int node)
	{
		int i;
		ArrayList tbl = r_mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled ++; }
		tbl = mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) { ((UdcMathCost)tbl.get(i)).tmpdisabled ++; }
	}

	/**
	 * 指定コストを一時抑止し、抑止リストに追加する。
	 * @param	cos 一時抑止するコスト
	 * @param	disableList 抑止リスト
	 */
	private void disableAddList(UdcMathCost cos, ArrayList disableList)
	{
		cos.tmpdisabled ++;
		disableList.add(cos);
// debug
//UdcTrace.trace(100, " search-Inh(forFilter) : " + cos.getName());
	}

	/**
	 * 指定区間のコストを一時抑止し、抑止リストに追加する。
	 * @param	一時抑止したか否か
	 * @param	tp1	指定区間の端ノード
	 * @param	tp2	指定区間の端ノード
	 * @param	preInvalidCost 抑止リスト
	 */
	private boolean disableAddList(int tp1, int tp2, ArrayList preInvalidCost)
	{
		UdcMathCost cos = getCost(tp1, tp2);
		if (cos != null && cos.isActive()) {
			if (!preInvalidCost.contains(cos)) { disableAddList(cos, preInvalidCost); }
			return true;
		}
		return false;
	}

	/**
	 * 任意の通過ノードに対してボトルネックノードが2ノードの場合の一時抑止し、抑止リストに追加する。
	 * @param nbr1		ボトルネックノード1
	 * @param nbr1src	ボトルネックノード1の接続元ノード
	 * @param nbr2		ボトルネックノード2
	 * @param nbr2src	ボトルネックノード2の接続元ノード
	 * @param preInvalidCost 抑止リスト
	 * @param newftrList	通過条件による絶対通過要な通過条件ノードの格納エリア
	 * @param ftr		通過条件にボトルネックノードを設定したい場合には当該通過条件を指定（不要時はnull）
	 */
	private void disableFtrBottleneck(int nbr1, int nbr1src, int nbr2, int nbr2src, ArrayList preInvalidCost, ArrayList newftrList, UdcMathDijkstraFtr ftr)
	{
		int i, j, nsz, n1, n2;
		ArrayList tbl, route=new ArrayList();
		UdcMathCost cos, pcos;

//UdcTrace.trace(100, "## disableFtrBottleneck " + nbr1 + " - " + nbr2);
		tbl = mtrxTbl_[nbr1];
		nsz = tbl.size();
		for (i=0; i<nsz; i++) {
			cos = (UdcMathCost)tbl.get(i);
			if (cos.remote == nbr1src || cos.isUnActive()) { continue; }
			if (cos.remote != nbr2) {
				route.clear();
				if (getOnlyRoute(nbr1, cos.remote, nbr2, route) < 0) { continue; }
				for (j=route.size()-1; j>=0; j--) { 
					pcos = (UdcMathCost)route.get(j);
//UdcTrace.trace(100, " ** f-bt(route)-inh (" + nbr1 + "-" + nbr2 + ") : " + pcos.getName());
					disableAddList(pcos, preInvalidCost);
				}
			}
//UdcTrace.trace(100, " ** f-bt-inh (" + nbr1 + "-" + nbr2 + ") : " + cos.getName());
			disableAddList(cos, preInvalidCost);
		}

		tbl = mtrxTbl_[nbr2];
		nsz = tbl.size();
		for (i=0; i<nsz; i++) {
			cos = (UdcMathCost)tbl.get(i);
			if (cos.remote == nbr2src || cos.isUnActive()) { continue; }
			if (cos.remote != nbr1) {
				route.clear();
				if (getOnlyRoute(nbr2, cos.remote, nbr1, route) < 0) { continue; }
				for (j=route.size()-1; j>=0; j--) {
					pcos = (UdcMathCost)route.get(j);
//UdcTrace.trace(100, " ** f-bt(r)(route)-inh (" + nbr2 + "-" + nbr1 + ") : " + pcos.getName());
					disableAddList(pcos, preInvalidCost);
				}
			}
//UdcTrace.trace(100, " ** f-bt(r)-inh (" + nbr2 + "-" + nbr1 + ") : " + cos.getName());
			disableAddList(cos, preInvalidCost);
		}

		// 通過条件通過時のJump経路の生成
		cos = getCost(nbr1, nbr1src);
		pcos = getCost(nbr2src, nbr2);
		UdcJumpRoute broute = (UdcJumpRoute)ftr.brouteList.get(ftr.brouteList.size()-1);
		if (broute.route.size() <= 0 || broute.route.get(0) != cos) { broute.route.add(0, cos); }
		if (broute.route.size() <= 0 || broute.route.get(broute.route.size()-1) != pcos) { broute.route.add(pcos); }

		// 更に上流のボトルネックの一時抑止
		UdcJumpRoute b1 = null;
		if ((n1=getOnlyOneHopRouteReverse(nbr1, nbr1src)) >= 0) {
			if (newftrList != null && !newftrList.contains(nodeList_[n1])) { newftrList.add(nodeList_[n1]); }
			ftr.brouteList.add(0, (b1=new UdcJumpRoute(n1, nbr2, (ArrayList)broute.route.clone())));
			disableFtrBottleneck(n1, nbr1, nbr2, nbr2src, preInvalidCost, newftrList, ftr);
		}
		UdcJumpRoute b2 = null;
		if ((n2=getOnlyOneHopRoute(nbr2, nbr2src)) >= 0) {
			if (newftrList != null && !newftrList.contains(nodeList_[n2])) { newftrList.add(nodeList_[n2]); }
			ftr.brouteList.add(0, (b2=new UdcJumpRoute(nbr1, n2, (ArrayList)broute.route.clone())));
			disableFtrBottleneck(nbr1, nbr1src, n2, nbr2, preInvalidCost, newftrList, ftr);
		}
		if ((n1=getOnlyOneHopRoute(nbr1, nbr1src)) >= 0 && (n2=getOnlyOneHopRoute(nbr2, nbr2src)) >= 0) {
			if (b1 != null) { ftr.brouteList.remove(b1); }
			if (b2 != null) { ftr.brouteList.remove(b2); }
			broute.src = n1;
			broute.dst = n2;
			disableFtrBottleneck(n1, nbr1, n2, nbr2, preInvalidCost, newftrList, ftr);
		}
	}

	/**
	 * 経路探索前に発/受/通過条件による不要なコストを一時抑止する
	 *
	 * @return 	通過条件異常(非0)か正常か(0)
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @param	newftrList	通過条件による絶対通過要な通過条件ノードの格納エリア
	 * @param	preInvalidCost 一時抑止コストリスト
	 * @param	shortest 最短経路のみか否か
	 * @since	UDC1.3
	 */
	private int disableCostForFtrSearch(int start, int end, ArrayList ftrList, ArrayList newftrList, boolean shortest, ArrayList preInvalidCost)
	{
		int i, j, k;
		UdcMathCost cos, pcos;
		// 発への入経路は一時抑止
		ArrayList tbl = r_mtrxTbl_[start];
		int nsz = tbl.size();
		for(i=0; i<nsz; i++) { disableAddList((UdcMathCost)tbl.get(i), preInvalidCost); }

		// 受からの出経路は一時抑止
		tbl = mtrxTbl_[end];
		nsz = tbl.size();
		for(i=0; i<nsz; i++) { disableAddList((UdcMathCost)tbl.get(i), preInvalidCost); }

		// 通過条件の不要なコストを一時抑止
		disableCostFtrList(ftrList, preInvalidCost);

		// 通過条件のボトルネックリンクの通過を抑止する
		int frn, rn, orn, ret, insz, onsz, brsz, rsz, fsz=(ftrList != null) ? ftrList.size() : 0;
		UdcMathDijkstraFtr ftr;
		ArrayList innbrList = new ArrayList();
		ArrayList outnbrList = new ArrayList();
		for (i=0; i<fsz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);	
			frn = ftr.getFtrTpNode();
			// 通過条件の入力側隣接ノードを取得
			innbrList.clear();
			tbl = r_mtrxTbl_[frn];
			nsz = tbl.size();
			for (j=0; j<nsz; j++) {
				cos = (UdcMathCost)tbl.get(j);
				if (cos.isActive()) { innbrList.add(nodeList_[cos.local]); }
			}
			for (j=innbrList.size()-1; j>=0; j--) {
				rn = ((Integer)innbrList.get(j)).intValue();
				ret = getOnlyOneHopRouteReverse(rn, frn);	
				// 入方路が他の隣接ノードしかない場合、隣接ノードから削除
				if (ret >= 0 && innbrList.contains(nodeList_[ret])) {
					innbrList.remove(nodeList_[rn]);
					// 通過条件に本ノードが未含有かつ、本ノード経由コストより他の隣接リンクのコストの方が最短routeVal[rn]であれば
					// このコストは一時停止してしまう。
					cos = getCost(ret, rn);
					pcos = getCost(rn, frn);
					if (!containsFilter(rn, ftrList) && getCost(ret,frn).getCalcCost() < (cos.getCalcCost()+pcos.getCalcCost())) {
						disableAddList(cos, preInvalidCost);
//UdcTrace.trace(100, " frn[" + frn + "]  in-notneed-inh - " + cos.getName());
						disableAddList(pcos, preInvalidCost);
//UdcTrace.trace(100, " frn[" + frn + "]  in-notneed-inh - " + pcos.getName());
						
					}
				}
			}
			if ((insz=innbrList.size()) <= 0) { return -1; }
			// 通過条件の出力側隣接ノードを取得
			outnbrList.clear();
			tbl = mtrxTbl_[frn];
			nsz = tbl.size();
			for (j=0; j<nsz; j++) {
				cos = (UdcMathCost)tbl.get(j);
				if (cos.isActive()) { outnbrList.add(nodeList_[cos.remote]); }
			}
			for (j=outnbrList.size()-1; j>=0; j--) {
				rn = ((Integer)outnbrList.get(j)).intValue();
				ret = getOnlyOneHopRoute(rn, frn);	
				// 出方路が他の隣接ノードしかない場合、隣接ノードから削除
				if (ret >= 0 && outnbrList.contains(nodeList_[ret])) {
					outnbrList.remove(nodeList_[rn]);
					// 通過条件に本ノードが未含有かつ、本ノード経由コストより他の隣接リンクのコストの方が最短であれば
					// このコストは一時停止してしまう。
					cos = getCost(frn, rn);
					pcos = getCost(rn, ret);
					if (!containsFilter(rn, ftrList) && getCost(frn,ret).getCalcCost() < (cos.getCalcCost()+pcos.getCalcCost())) {
						disableAddList(cos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  out-notneed-inh - " + cos.getName());
						disableAddList(pcos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  out-notneed-inh - " + pcos.getName());
					}
				}
			}
			if ((onsz=outnbrList.size()) <= 0) { return -1; }

			// 入力側、出力側の隣接が1ノードで同一なら通過条件異常でエラー
			if (insz == 1 && onsz == 1 && innbrList.get(0) == outnbrList.get(0)) {
				return -1;
			}

			// 隣接ノード間がボトルネック関係にある場合、隣接ノード間のリンクを一時抑止
			int bn,nn;
				// 入力側隣接ノードが1ノードのみなら通過条件以外への出力リンクを一時抑止
			if (insz == 1) {
				tbl = mtrxTbl_[((Integer)innbrList.get(0)).intValue()];
				nsz = tbl.size();
				for (j=0; j<nsz; j++) {
					cos = (UdcMathCost)tbl.get(j);
					if (cos.isActive() && cos.remote != frn) {
						disableAddList(cos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  in-notout-inh - " + cos.getName());
						bn = cos.remote;
						rn = cos.local;
						while ((nn=getOnlyOneHopRoute(bn, rn)) >= 0) {
							pcos = getCost(bn ,nn);
							disableAddList(pcos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  in-notout-inh - " + pcos.getName());
							bn = nn;
							rn = bn;
						}
					}
				}
			}
				// 出力側隣接ノードが1ノードのみなら通過条件以外からの入力リンクを一時抑止
			if (onsz == 1) {
				tbl = r_mtrxTbl_[((Integer)outnbrList.get(0)).intValue()];
				nsz = tbl.size();
				for (j=0; j<nsz; j++) {
					cos = (UdcMathCost)tbl.get(j);
					if (cos.isActive() && cos.local != frn) {
						disableAddList(cos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  out-notin-inh - " + cos.getName());
						bn = cos.local;
						rn = cos.remote;
						while ((nn=getOnlyOneHopRouteReverse(bn, rn)) >= 0) {
							pcos = getCost(nn ,bn);
							disableAddList(pcos, preInvalidCost);	
//UdcTrace.trace(100, " frn[" + frn + "]  out-notin-inh - " + pcos.getName());
							bn = nn;
							rn = bn;
						}
					}
				}
			}

			// 入力側隣接ノードに対して出力側隣接ノードが1つなら、隣接ノード間のリンクは一時抑止
			for (j=0; j<insz; j++) {
				rn = ((Integer)innbrList.get(j)).intValue();
				for (orn=-1,k=0; k<onsz; k++) {
					if (rn == ((Integer)outnbrList.get(k)).intValue()) { continue; }
					if (orn >= 0) { orn = -1; break; }
					orn = ((Integer)outnbrList.get(k)).intValue();
				}
				if (orn >= 0) {
					ftr.brouteList.add(new UdcJumpRoute(rn, orn, new ArrayList()));
					disableFtrBottleneck(rn, frn, orn, frn, preInvalidCost, newftrList, ftr);
				}
			}
			// もしJump経路がある場合には他の通過条件との整合性チェック
			if ((brsz=ftr.brouteList.size()) > 0) {
				int m, jumpcost;
				UdcMathDijkstraFtr dftr;
				UdcJumpRoute jump;
				boolean already=false, addflag;
				for (j=0; j<brsz; j++) {
					jump = (UdcJumpRoute)ftr.brouteList.get(j);
					jump.chgList = new ArrayList();
					jump.delFtrList = new ArrayList();
					rsz = jump.route.size();
					for (k=0; k<rsz; k++) {
						cos = (UdcMathCost)jump.route.get(k);
						addflag = false;
						if (ftrMode == FilterMode_Order) {
							for (m=0; m<fsz; m++) {
								dftr = (UdcMathDijkstraFtr)ftrList.get(m);
								if (dftr.getFtrTpNode() == cos.remote) {
									if (already) {
										if (i > m) { return -1; }	// 通過条件順序異常
									} else {
										if (i < m) { return -1; }	// 通過条件順序異常
									}
									jump.delFtrList.add(dftr);
									addflag = true;
									break;
								}
							}
						} else {
							for (m=0; m<fsz; m++) {
								dftr = (UdcMathDijkstraFtr)ftrList.get(m);
								if (dftr.getFtrTpNode() == cos.remote) { jump.delFtrList.add(dftr); addflag=true; break; }
							}
						}
						jump.chgList.add((addflag) ? Boolean.TRUE : Boolean.FALSE);
						if (cos.remote == frn) { already = true; }
					}
//StringBuffer buf = new StringBuffer();
//for (int y=0; y<jump.route.size(); y++) {
//	if (y != 0) { buf.append(" -- "); }
//	buf.append(((UdcMathCost)jump.route.get(y)).getName());
//}
//UdcTrace.trace(100, " frn[" + frn + "] num(" + brsz + ") jump-route (" + jump.node +  ") delFtrNum:" + jump.delFtrList.size() + " route: " + buf);
				}
			}
		}

		// 最短経路探索において無駄な経路は削除する
		if (shortest) { disableCostNoRelation(start, end, ftrList, preInvalidCost); }

		return 0;
	}

	/**
	 * 最短経路探索において無駄な経路を一時抑止する
	 *
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @param	preInvalidCost 	一時抑止したコストリスト
	 * @since	UDC1.3
	 */
	private void disableCostNoRelation(int start, int end, ArrayList ftrList, ArrayList preInvalidCost)
	{
		boolean change;
		int i, j, k, nsz, rsz, cnt=0;
		ArrayList tbl, rtbl;
		UdcMathCost cos, pcos, bcos, ocos, ncos;

		while ( true ) { 	// for (int c=0; c<mtrxMax; c++)
			change = false;
			for (i=0; i<mtrxMax; i++) {
				// 発/受/通過条件ノードは除外する
				if (isRelativeNode(i, start, end, ftrList)) { continue; }
				// 隣接ノードが２ノードしかないノードで隣接ノード間にリンクが存在する場合、そのノードは通過不要
				tbl = mtrxTbl_[i];
				rtbl = r_mtrxTbl_[i];
				nsz = tbl.size();
				rsz = rtbl.size();
				for (k=0; k<nsz; k++) {
					cos = (UdcMathCost)tbl.get(k);
					if (cos.isUnActive()) { continue; }
					// 出に対して入が１本しかないかチェック
					ocos = null;
					cnt = 0;
					for (j=0; j<rsz; j++) {
						pcos = (UdcMathCost)rtbl.get(j);
						if (pcos.isUnActive()) { continue; }
						if (pcos.local == cos.remote) { continue; }
						if (ocos != null) { ocos = null; break; }
						ocos = pcos;
						cnt ++;
					}
					// 入が1本もないなら当該ノードへの出力は抑止
					if (cnt == 0) {
						disableAddList(cos, preInvalidCost);
						// 当該ノードへの入を参照し、入に対して出が１本もないなら入力を抑止(入はこの時点ではcosの逆方向以外にはないはず)
						for (j=0; j<nsz; j++) {
							bcos = (UdcMathCost)tbl.get(j);
							if (bcos.isUnActive()) { continue; }
							if (bcos.local == cos.remote) { continue; }
							cnt ++;
						}
						if (cnt == 0) {
							if ((pcos=getCost(cos.remote, cos.local)) != null) { disableAddList(pcos, preInvalidCost); }
						}
						change = true;
						continue;
					}
					if (ocos == null) { continue; }
					// 入出力対に対して関連しない出が更にあるかチェック	
					for (j=0; j<nsz; j++) {
						if (k == j) { continue; }
						pcos = (UdcMathCost)tbl.get(j);
						if (ocos.local == pcos.remote) { continue; }
						if (pcos.isActive()) { break; }
					}	
					if (j < nsz) { continue; }
	 				// 入出力が２方路しかない場合の、不要区間の経路を抑止する。
					if (disableCostNoRelationSub(ocos, cos, start, end, ftrList, preInvalidCost)) { change = true; }
				}
			}
			if (!change) { break; }
		}
	}

	/**
	 * 入出力が２方路しかない場合の、不要区間の経路を抑止する。
	 *
	 * @return 	抑止したか否か
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @param	preInvalidCost 一時抑止したコスト格納リスト
	 * @since	UDC1.3
	 */
	private boolean disableCostNoRelationSub(UdcMathCost incos, UdcMathCost outcos, int start, int end, ArrayList ftrList, ArrayList preInvalidCost)
	{
		if (isRelativeNode(outcos.remote, start, end, ftrList) || isRelativeNode(incos.local, start, end, ftrList)) { return false; }

		int i, j, rn, nsz, rsz, cnt=0;
		int searchcos, targetcos = incos.getCalcCost() + outcos.getCalcCost();
		UdcMathCost pcos, cos;
		ArrayList tbl, route = new ArrayList();
		boolean change = false;

//String local = incos.getName().split("->")[1];
//UdcTrace.trace(100, "***** " + local + " (" + incos.getName() + ")(" + outcos.getName() + ")");

		tbl = mtrxTbl_[incos.local];
		nsz = tbl.size();
		for (i=0; i<nsz; i++) {
			pcos = (UdcMathCost)tbl.get(i);
			if (pcos.isUnActive()) { continue; }
			if (pcos == incos) { continue; }
			cnt ++;
			// One-Hop経路がある場合
			if (pcos.remote == outcos.remote) {
				// 当該区間の直結リンクが存在し、かつコストが直結区間より大きければこの経路は不必要
				if (pcos.getCalcCost() <= targetcos) {
					disableAddList(incos, preInvalidCost);
					disableAddList(outcos, preInvalidCost);
//UdcTrace.trace(100, " ** inh - " + incos.getName() + " = " + outcos.getName());
				// 直結区間のコストが大きければ、当該ノードは入出力が直結区間のエッジしか存在しないため、直結区間を抑止
				} else {
					disableAddList(pcos, preInvalidCost);
//UdcTrace.trace(100, " ** inh - " + pcos.getName());
				}
				change = true;
				continue;
			}
			// 通過条件に関連する抑止はできない
			if (isRelativeNode(pcos.remote, start, end, ftrList)) { continue; }
			// N-Hop経路がある場合
			route.clear();
			rn = getOnlyRoute(pcos.local, pcos.remote, outcos.remote, route);
			if (rn < 0) { continue; }
			rsz = route.size();
			searchcos = pcos.getCalcCost();
			for (j=0; j<rsz; j++) {
				cos = (UdcMathCost)route.get(j);
				if (isRelativeNode(cos.remote, start, end, ftrList)) { break; }
				searchcos += cos.getCalcCost();
			}
				// 通過経路が通過条件に関連している場合は、抑止はできない
			if (j < rsz) { continue; }
				// 当該区間の直結リンクが存在し、かつコストが直結区間より大きければこの経路は不必要
			if (searchcos < targetcos) {
				disableAddList(incos, preInvalidCost);
				disableAddList(outcos, preInvalidCost);
//UdcTrace.trace(100, " *= inh - " + incos.getName() + " = " + outcos.getName());
				// 直結区間のコストが大きければ、当該ノードは入出力が直結区間のエッジしか存在しないため、直結区間を抑止
			} else {
				for (j=0; j<rsz; j++) {
					disableAddList((UdcMathCost)route.get(j), preInvalidCost);
//UdcTrace.trace(100, " *= inh - " + ((UdcMathCost)route.get(j)).getName());
				}
				disableAddList(pcos, preInvalidCost);
//UdcTrace.trace(100, " *= inh - " + pcos.getName());
			}
			change = true;
		}

		// 直結リンクが存在しない場合でも、隣接ノードの入出力が１方路しかないならその方路のノードと隣接ノードの直結リンク
		// が存在し、かつコストが直結区間より大きければこの経路は不必要
		if (cnt > 0 && !change) {
			if ((rn=getOnlyOneHopRoute(outcos.remote, outcos.local)) < 0) { return false; }
			pcos = getCost(outcos.remote, rn);
			if (disableCostNoRelationSub(incos, pcos, start, end, ftrList, preInvalidCost)) {
				if (preInvalidCost.contains(incos)) {
					disableAddList(outcos, preInvalidCost);
//UdcTrace.trace(100, " ## inh - " + outcos.getName());
				}
				change = true;
			}
		}

		return change;
	}

	/**
	 * 発/受/通過条件に関連するノードか否かを判定する
	 *
	 * @return 	発/受/通過条件に関連するノードか否か
	 * @param	node	判定ノード
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 */
	private boolean isRelativeNode(int node, int start, int end, ArrayList ftrList)
	{
		if (node == start || node == end) { return true; }	
		if (ftrList == null) { return false; }
		int j, fsz = ftrList.size();
		UdcMathDijkstraFtr ftr;
		UdcMathCost cos;
		for (j=0; j<fsz; j++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(j);	
			if (ftr.getFtrTpNode() == node || ((cos=ftr.getFtrCost()) != null && cos.local == node)) { return true; }
		}
		return false;
	}

	/**
	 * 任意のノードsrcの隣接ノード(node)が唯一の出方路しか持たない場合の出方路ノードをを取得する。
	 * @return 0以上:ノード/-1:経路なし/-2:複数経路有り
	 * @param 	node	隣接ノード
	 * @param	src		基点ノード
	 */
	private int getOnlyOneHopRoute(int node, int src)
	{
		int i, rn=-1;
		UdcMathCost cos;
		ArrayList tbl = mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) {
			cos = (UdcMathCost)tbl.get(i);
			if (cos.remote != src && cos.isActive()) {
				if (rn != -1) { return -2; }
				rn = cos.remote;
			}
		}
		return rn;
	}

	/**
	 * 任意のノードsrcの隣接ノード(node)が唯一の入方路しか持たない場合の入方路ノードをを取得する。
	 * @return 0以上:ノード/-1:経路なし/-2:複数経路有り
	 * @param 	node	隣接ノード
	 * @param	src		基点ノード
	 */
	private int getOnlyOneHopRouteReverse(int node, int src)
	{
		int i, rn=-1;
		UdcMathCost cos;
		ArrayList tbl = r_mtrxTbl_[node];
		for (i=tbl.size()-1; i>=0; i--) {
			cos = (UdcMathCost)tbl.get(i);
			if (cos.local != src && cos.isActive()) {
				if (rn != -1) { return -2; }
				rn = cos.local;
			}
		}
		return rn;
	}

	/**
	 * 任意のノードsrcからdstまでの分岐なし経路を取得する。
	 * @return 0:経路有り/-1:経路なし/-2:複数経路有り
	 * @param	src		送信元ノード
	 * @param	out		送信元ノードの隣接ノード
	 * @param	dst		送信先ノード
	 * @param	route	
	 */
	private int getOnlyRoute(int src, int out, int dst, ArrayList route)
	{
		int rn = getOnlyOneHopRoute(out, src);
		if (rn < 0) { return rn; }

		route.add(getCost(out, rn));
		if (rn == dst) { return 0; }

		return getOnlyRoute(out, rn, dst, route);
	}

	/**
	 * 通過条件中に指定ノードが含まれているか否かを取得する
	 * @return 通過条件中に指定ノードが含まれているか否か
	 * @param	node 指定ノード
	 * @param	ftrList	通過条件リスト
	 */
	private boolean containsFilter(int node, ArrayList ftrList)
	{
		UdcMathDijkstraFtr ftr;
		int ftrSz = ftrList.size();
		for (int i=0; i<ftrSz; i++) {
			ftr = (UdcMathDijkstraFtr)ftrList.get(i);
			if (ftr.getFtrTpNode() == node) { return true; }
			if (ftr.getFtrCost() != null && ftr.getFtrCost().local == node) { return true; }
		}
		return false;
	}

	/**
	 * 経路探索前に通過条件の不要なコストを一時抑止する
	 *
	 * @return 	一時抑止したコストリスト
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @param 	preInvalidCost 一時抑止したコストリスト
	 * @since	UDC1.3
	 */
	private ArrayList disableCostFtrList(ArrayList ftrList, ArrayList preInvalidCost)
	{
		if (ftrList == null || ftrList.size() <= 0) { return preInvalidCost; }

		int i, j, nsz, fsz=ftrList.size();;
		ArrayList tbl;
		Object obj;
		UdcMathCost cos,pcos;
		// 通過条件に反する経路は一時抑止
		for (i=0; i<fsz; i++) {
			obj = ftrList.get(i);
			cos = null;
			if (obj instanceof UdcMathDijkstraFtr) { cos = ((UdcMathDijkstraFtr)ftrList.get(i)).getFtrCost(); }
			else if (obj instanceof UdcMathCost)   { cos = (UdcMathCost)obj; }
			if (cos == null) { continue; }
			// 当該経路のcos.localの出力を指定区間のみとする
			tbl = mtrxTbl_[cos.local];
			nsz = tbl.size();
			for(j=0; j<nsz; j++) {
				pcos = (UdcMathCost)tbl.get(j);
				if (cos.remote != pcos.remote) { disableAddList(pcos, preInvalidCost); }
			}
			// 当該経路のcos.remoteへの入力を指定区間のみとする
			tbl = r_mtrxTbl_[cos.remote];
			nsz = tbl.size();
			for(j=0; j<nsz; j++) {
				pcos = (UdcMathCost)tbl.get(j);
				if (cos.local != pcos.local) { disableAddList(pcos, preInvalidCost); }
			}
			// 当該経路のcos.remoteからcos.localへの出力を制限する
			tbl = mtrxTbl_[cos.remote];
			nsz = tbl.size();
			for(j=0; j<nsz; j++) {
				pcos = (UdcMathCost)tbl.get(j);
				if (pcos.remote == cos.local) { disableAddList(pcos, preInvalidCost); }
			}
		}
		return preInvalidCost;
	}

	/**
	 * 経路探索前に通過条件コストを変更するため、通過条件の最少コストを求める
	 *
	 * @return 	通過条件の最少コスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @since	UDC1.3
	 */
	private ArrayList getChangeMinCostForFtrList(int start, int end, ArrayList ftrList)
	{
		int i, j, k, nsz;
		UdcMathCost cos, pcos;
		UdcFtrMinCost minCost,tmpCost;
		ArrayList tbl, minList = new ArrayList();
		// 通過条件の縮小対象コストの中で最少値を求める
		if (ftrList != null && ftrList.size() > 0) {
			UdcMathDijkstraFtr ftr;
			for (i=0; i<ftrList.size(); i++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				minCost = new UdcFtrMinCost();
				if (ftr.getFtrNode() != null) {
					minCost.node = ftr.getFtrNode().intValue();
					tbl = r_mtrxTbl_[minCost.node];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						if (pcos.isActive() && minCost.minVal > pcos.getCalcCost()) { minCost.minVal = pcos.getCalcCost(); }
					}
					tbl = mtrxTbl_[minCost.node];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						if (pcos.isActive() && minCost.minVal > pcos.getCalcCost()) { minCost.minVal = pcos.getCalcCost(); }
					}
				} else {
					cos = ftr.getFtrCost();
					if (cos.isActive()) {
						minCost.node = cos.local;
						tbl = r_mtrxTbl_[minCost.node];
						nsz = tbl.size();
						for (j=0; j<nsz; j++) {
							pcos = (UdcMathCost)tbl.get(j);
							if (pcos.isUnActive()) { continue; }
							if (pcos.isActive() && minCost.minVal > pcos.getCalcCost()) { minCost.minVal = pcos.getCalcCost(); }
						}
						minCost.rnode = cos.remote;
						tbl = mtrxTbl_[minCost.rnode];
						nsz = tbl.size();
						for (j=0; j<nsz; j++) {
							pcos = (UdcMathCost)tbl.get(j);
							if (pcos.isUnActive()) { continue; }
							if (pcos.isActive() && minCost.rminVal > pcos.getCalcCost()) { minCost.rminVal = pcos.getCalcCost(); }
						}
					}
				}
				minList.add(minCost);
			}
		}
		// 受ノードも通過条件の一つとみなせるので縮小対象コストとする。
		minCost = new UdcFtrMinCost();
		minCost.node = end;
		tbl = r_mtrxTbl_[end];
		nsz = tbl.size();
		for (j=0; j<nsz; j++) {
			pcos = (UdcMathCost)tbl.get(j);
			if (pcos.isActive() && minCost.minVal > pcos.getCalcCost()) { minCost.minVal = pcos.getCalcCost(); }
		}
		minList.add(minCost);

		// 通過条件が隣接する場合、隣接ノードとの間での最少値に更新する
		// 但し、隣接するがためにコスト最少値があまりに小さくなってしまう場合、そのノードの縮小はしないようにする。
		// (ベルマン・フォード法のような要領で最少値の整合性をとる)
		int msz = minList.size();
		for (int y=0; y<msz; y++) {
			boolean mod = false;
			for (i=0; i<msz; i++) {
				minCost = (UdcFtrMinCost)minList.get(i);
				// ノード指定/区間指定.local
				if (minCost.isActive) {
					tbl = mtrxTbl_[minCost.node];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						for (k=0; k<msz; k++) {
							if (k == i) { continue; }
							tmpCost = (UdcFtrMinCost)minList.get(k);
							if (tmpCost.isActive) {
								if (pcos.remote == tmpCost.node && minCost.minVal > tmpCost.minVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActive = false; break; }
									mod = true;
									minCost.minVal = tmpCost.minVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
							if (tmpCost.rnode < 0) { continue; }
							if (tmpCost.isActiveR) {
								if (pcos.remote == tmpCost.rnode && minCost.minVal > tmpCost.rminVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActiveR = false; break; }
									mod = true;
									minCost.minVal = tmpCost.rminVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
						}
					}
					tbl = r_mtrxTbl_[minCost.node];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						for (k=0; k<msz; k++) {
							if (k == i) { continue; }
							tmpCost = (UdcFtrMinCost)minList.get(k);
							if (tmpCost.isActive) {
								if (pcos.local == tmpCost.node && minCost.minVal > tmpCost.minVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActive = false; break; }
									mod = true;
									minCost.minVal = tmpCost.minVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
							if (tmpCost.rnode < 0) { continue; }
							if (tmpCost.isActiveR) {
								if (pcos.local == tmpCost.rnode && minCost.minVal > tmpCost.rminVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActiveR = false; break; }
									mod = true;
									minCost.minVal = tmpCost.rminVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
						}
					}
				}
				// 区間指定.remote
				if (minCost.rnode >= 0) {
					tbl = mtrxTbl_[minCost.rnode];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						for (k=0; k<msz; k++) {
							if (k == i) { continue; }
							tmpCost = (UdcFtrMinCost)minList.get(k);
							if (tmpCost.isActive) {
								if (pcos.remote == tmpCost.node && minCost.rminVal > tmpCost.minVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActive = false; break; }
									mod = true;
									minCost.rminVal = tmpCost.minVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
							if (tmpCost.rnode < 0) { continue; }
							if (tmpCost.isActiveR) {
								if (pcos.remote == tmpCost.rnode && minCost.rminVal > tmpCost.rminVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActive = false; break; }
									mod = true;
									minCost.rminVal = tmpCost.rminVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
						}
					}
					tbl = r_mtrxTbl_[minCost.rnode];
					nsz = tbl.size();
					for (j=0; j<nsz; j++) {
						pcos = (UdcMathCost)tbl.get(j);
						if (pcos.isUnActive()) { continue; }
						for (k=0; k<msz; k++) {
							if (k == i) { continue; }
							tmpCost = (UdcFtrMinCost)minList.get(k);
							if (tmpCost.isActive) {
								if (pcos.local == tmpCost.node && minCost.rminVal > tmpCost.minVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActiveR = false; break; }
									mod = true;
									minCost.rminVal = tmpCost.minVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
							if (tmpCost.rnode < 0) { continue; }
							if (tmpCost.isActiveR) {
								if (pcos.local == tmpCost.rnode && minCost.rminVal > tmpCost.rminVal) {
									if ((minCost.minVal/2) > tmpCost.minVal) { tmpCost.isActiveR = false; break; }
									mod = true;
									minCost.rminVal = tmpCost.rminVal;
									break;	// 重複する通過条件はここでは探索の最初に削除されているため
								}
							}
						}
					}
				}
			}
			if (!mod) { break; }
		}
		return minList; 
	}

	/**
	 * 経路探索前に通過条件コストを変更する
	 *
	 * @return 	コスト変更したコストリスト
	 * @param	start	コストマトリックス上の経路始点位置
	 * @param	end		コストマトリックス上の経路始点位置
	 * @param	ftrMinCosList	通過条件の最少コスト値
	 * @param	ftrList	通過条件リスト（通過義務のある通過条件）
	 * @since	UDC1.3
	 */
	private ArrayList changeCostForFtrList(int start, int end, ArrayList ftrMinCosList, ArrayList ftrList)
	{
		int i, j, nsz, rn;
		int min, tmp;
		UdcFtrMinCost minCost;
		UdcMathCost cos, pcos, scos;
		ArrayList tbl, preChangeCost = new ArrayList();
		// 通過条件経路のコストを一時的に良くする
		if (ftrList != null && ftrList.size() > 0) {
			UdcMathDijkstraFtr ftr;
			for (i=0; i<ftrList.size(); i++) {
				ftr = (UdcMathDijkstraFtr)ftrList.get(i);
				minCost = (UdcFtrMinCost)ftrMinCosList.get(i);
				min = minCost.minVal;
				tmp = min / ftrChangeCost;
				if (ftr.getFtrNode() != null) {
					if (minCost.isActive) {
						rn = ftr.getFtrNode().intValue();
						tbl = mtrxTbl_[rn];
						nsz = tbl.size();
						for (j=0; j<nsz; j++) {
							pcos = (UdcMathCost)tbl.get(j);
							if (pcos.isActive() && !preChangeCost.contains(pcos)) {
								pcos.svcost = pcos.getCalcCost();
								pcos.setCalcCost( (pcos.getCalcCost() - min) + tmp );
//UdcTrace.trace(100, "ftr-cost change : " + pcos.getName() + " before:" + pcos.svcost + "  after:" + pcos.getCalcCost());
								preChangeCost.add(pcos);
							}
						}
						tbl = r_mtrxTbl_[rn];
						nsz = tbl.size();
						for (j=0; j<nsz; j++) {
							pcos = (UdcMathCost)tbl.get(j);
							if (pcos.isActive() && !preChangeCost.contains(pcos)) {
								pcos.svcost = pcos.getCalcCost();
								pcos.setCalcCost( (pcos.getCalcCost() - min) + tmp );
//UdcTrace.trace(100, "ftr-cost change : " + pcos.getName() + " before:" + pcos.svcost + "  after:" + pcos.getCalcCost());
								preChangeCost.add(pcos);
							}
						}
					}
				} else {
					cos = ftr.getFtrCost();
					if (cos.isActive()) {
						if (!preChangeCost.contains(cos)) {
							cos.svcost = cos.getCalcCost();
							cos.setCalcCost( cos.getCalcCost() / ftrChangeCost );
//UdcTrace.trace(100, "ftr-cost change : " + cos.getName() + " before:" + cos.svcost + "  after:" + cos.getCalcCost());
							preChangeCost.add(cos);
						}
						if (minCost.isActive) {
							tbl = r_mtrxTbl_[cos.local];
							nsz = tbl.size();
							for (j=0; j<nsz; j++) {
								pcos = (UdcMathCost)tbl.get(j);
								if (pcos.local == cos.remote) { continue; }
								if (pcos.isActive() && !preChangeCost.contains(pcos)) {
									pcos.svcost = pcos.getCalcCost();
									pcos.setCalcCost( (pcos.getCalcCost() - min) + tmp );
//UdcTrace.trace(100, "ftr-cost change : " + pcos.getName() + " before:" + pcos.svcost + "  after:" + pcos.getCalcCost());
									preChangeCost.add(pcos);
								}
							}
						}
						if (minCost.isActiveR) {
							min = minCost.rminVal;
							tmp = min / ftrChangeCost;
							tbl = mtrxTbl_[cos.remote];
							nsz = tbl.size();
							for (j=0; j<nsz; j++) {
								pcos = (UdcMathCost)tbl.get(j);
								if (pcos.remote == cos.local) { continue; }
								if (pcos.isActive() && !preChangeCost.contains(pcos)) {
									pcos.svcost = pcos.getCalcCost();
									pcos.setCalcCost( (pcos.getCalcCost() - min) + tmp );
//UdcTrace.trace(100, "ftr-cost change : " + pcos.getName() + " before:" + pcos.svcost + "  after:" + pcos.getCalcCost());
									preChangeCost.add(pcos);
								}
							}
						}
					}
				}
			}
		}
		// 受ノードも通過条件の一つとみなせるのでコストを一時的に良くする。
		// (この効果は受への入力コストが大きい場合などには既探索路コスト値が小さくなるため、コストチェックではじかれる経路が多くなる)
		minCost = (UdcFtrMinCost)ftrMinCosList.get(ftrMinCosList.size()-1);
		min = minCost.minVal;
		tmp = min / ftrChangeCost;
		tbl = r_mtrxTbl_[end];
		nsz = tbl.size();
		for (j=0; j<nsz; j++) {
			pcos = (UdcMathCost)tbl.get(j);
			if (pcos.isActive() && !preChangeCost.contains(pcos)) {
				pcos.svcost = pcos.getCalcCost();
				pcos.setCalcCost( (pcos.getCalcCost() - min) + tmp );
				preChangeCost.add(pcos);
			}
		}

		// コスト順に探索させるため
		for (i=0; i<mtrxMax; i++) {
			Collections.sort(mtrxTbl_[i]);	
			Collections.sort(r_mtrxTbl_[i]);
		}
		return preChangeCost;
	}


	/**
	 * 通過条件要素
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Aug 2008
	 * @since   UDC1.3
	 */
	private class UdcMathDijkstraFtr
	{
		/** 通過条件ノード */
		private Integer	node = null;
		/** 通過条件コスト */
		private UdcMathCost	cost = null;
		/** 終端ノード */
		private int tpnode;
		/** ボトルネックによるjump経路情報 */
		private ArrayList brouteList = new ArrayList();

		/** 現探索位置から通過条件までのコスト。(本情報は演算中の一時的な情報) */
		private int fprecost;
		/** 通過条件から受までのコスト。(本情報は演算中の一時的な情報) */
		private int fpostcost;
		/** 全経路探索による通過条件の高速判定のためのコスト表。(本情報は演算中の一時的な情報) */
		private int[] routeVal_ = null;
		/** 全経路探索による通過条件の高速判定のためのコスト表。(本情報は演算中の一時的な情報) */
		private int[] r_routeVal_ = null;

		/** ボトルネックチェック時のボトルネックノードリスト。(本情報は演算中の一時的な情報) */
		private ArrayList bnode = new ArrayList();

		/**
		 * コンストラクタ
		 * @param ftrNode 通過条件ノード
		 */
		private UdcMathDijkstraFtr(int ftrNode)
		{
			node = nodeList_[ftrNode];
			tpnode = node.intValue();
		}

		/**
		 * コンストラクタ
		 * @param ftrCost 通過条件コスト
		 */
		private UdcMathDijkstraFtr(UdcMathCost ftrCost)
		{
			cost = ftrCost;
			tpnode = cost.remote;
		}

		/**
		 * 通過条件ノードを取得する
		 * @return 通過条件ノード
		 */
		private Integer	getFtrNode() { return node; }

		/**
		 * 通過条件コストを取得する
		 * @return 通過条件コスト
		 */
		private UdcMathCost	getFtrCost() { return cost; }

		/**
		 * 通過条件の終端ノードを取得する
		 * @raturn 通過条件終端ノード
		 */
		private int getFtrTpNode() { return tpnode; }
	}

	/**
	 * 通過時のJumpルート情報
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Jul 2008
	 * @since   UDC1.3
	 */
	private class UdcJumpRoute
	{
		/** 入力ノード */
		private int src = -1;
		/** 入力ノード */
		private int dst = -1;
		/** Jumpルート */
		private ArrayList route = null;
		/** Jumpルートのコスト */
		private int cost = 0;
		/** Jumpルートの通過条件使用状態リスト */
		private ArrayList chgList = null;
		/** Jumpルートの通過条件リスト */
		private ArrayList delFtrList = null;

		/**
		 * コンストラクタ
		 */
		private UdcJumpRoute() {} 

		/**
		 * コンストラクタ
		 * @param	inNode	入力ノード	
		 * @param	jumpRoute 通過経路
		 */
		private UdcJumpRoute(int inNode, int outNode, ArrayList jumpRoute)
		{
			src = inNode;
			dst = outNode;
			route = jumpRoute;
		}

		/**
		 * 指定されたインスタンスを自身に参照コピーする。
		 * @param	inst	コピー元インスタンス
		 */
		private void copyOf(UdcJumpRoute inst)
		{
			src = inst.src;
			dst = inst.dst;
			route = inst.route;
			cost = inst.cost;
			chgList = inst.chgList;
			delFtrList = inst.delFtrList;
		}
	}

	/**
	 * ノード・フィルタ対情報リスト要素
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Jul 2008
	 * @since   UDC1.3
	 */
	private class UdcMatchFtr
	{
		/** フィルタ情報 */
		private UdcMathDijkstraFtr	ftr;
		/** 通過条件リスト格納位置 */
		private int	pos = -1;

		/**
		 * コンストラクタ
		 * @param	ftr	フィルタ情報
		 */
		private UdcMatchFtr(UdcMathDijkstraFtr ftr) { this.ftr = ftr; }
	}

	/**
	 * 通過条件付き経路探索時の通過条件関連縮小コスト要素
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Jul 2008
	 * @since   UDC1.3
	 */
	private class UdcFtrMinCost
	{
		/** 通過条件ノード */
		private int node = -1;
		/** 区間指定時のremote通過条件ノード */
		private int rnode = -1;
		/** 縮小コスト有効無効フラグ */
		private boolean isActive = true;
		/** 縮小コスト有効無効フラグ */
		private boolean isActiveR = true;
		/** 通過条件ノードに隣接する最少コスト値 */
		private int minVal = Integer.MAX_VALUE;
		/** 区間指定時のremote通過条件ノードに隣接する最少コスト値 */
		private int rminVal = Integer.MAX_VALUE;
	}

	/**
	 * 通過条件付き経路探索時の通過条件関連縮小コスト要素
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Jul 2008
	 * @since   UDC1.3
	 */
	private class UdcFtrSvInfo
	{
		/** 現探索位置から通過条件までのコスト。(本情報は演算中の一時的な情報) */
		private int fprecost[] = null;
		/** 通過条件から受までのコスト。(本情報は演算中の一時的な情報) */
		private int fpostcost[] = null;

		/**
		 * コンストラクタ
		 * @param	ftrSz	フィルタ数
		 */
		private UdcFtrSvInfo(int ftrSz)
		{
			fprecost = new int[ftrSz];
			fpostcost = new int[ftrSz];
		}
	}

	/**
	 * 要不要ノード管理情報
	 *
	 * @author  Takayuki Uchida
	 * @version 1.3, 1 Jul 2008
	 * @since   UDC1.3
	 */
	private class UdcInhNodeInfo
	{
		/** 必要ノードリスト数 */
		int needSz;
		/** 必要ノードリスト */
		private int needList[];
		/** 不要ノードリスト数 */
		int inhSz;
		/** 不要ノードリスト */
		private int inhList[];
		/** 親ノードの分岐数が非1か否か */
		private boolean bcheck = true;

		/**
		 * コンストラクタ
	 	 * @param matrixNum	コストマトリックス数
		 */
		private UdcInhNodeInfo(Integer nodeList[])
		{
			needSz = nodeList.length;
			needList = new int[nodeList.length];
			for (int i=0; i<nodeList.length; i++) { needList[i] = nodeList[i].intValue(); }
			inhSz = 0;
			inhList = new int[nodeList.length];	
		}

		/**
		 * 指定ノードを不要ノードとして登録します。
		 * @param node	不要ノード
		 */
		private void regInh(int needIndex)
		{
			inhList[inhSz] = needList[needIndex];
			inhSz ++;
			needSz --;
			if (needSz != needIndex) { needList[needIndex] = needList[needSz]; }
		}

		/**
		 * 不要ノードリストの最後の要素を必要ノードとして登録します。
		 * @return 不要ノードリストの最後のノード
		 */
		private int relLastInh()
		{
			inhSz --;
			needList[needSz] = inhList[inhSz]; 
			needSz ++;
			return inhList[inhSz];
		}
	}
}

