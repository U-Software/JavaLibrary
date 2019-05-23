/* *********************************************************************
 * @(#)UdcListElem.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;

/**
 * 分岐木リスト要素クラス
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcBranchElem implements Comparable
{
	/**
	 * 分岐木リストの親リンク。
	 *
	 * @since	UDC1.0
	 */
	protected UdcBranchElem	trunk;

	/**
	 * 分岐木リストの分岐木リンク群。
	 *
	 * @since	UDC1.0
	 */
	protected LinkedList	branch;

	/**
	 * 要素名。
	 * @since	UDC1.0
	 */
	String 	tagName;

	/**
	 * 要素値情報。
	 * @since	UDC1.0
	 */
	Object 	tagValue;

	/**
	 * 属性情報リスト。
	 * @since	UDC1.2
	 */
	LinkedList	attrList;

	/**
	 * コンストラクタ
	 *
	 * @since	UDC1.0
	 */
	public UdcBranchElem()
	{
		trunk= null;
		branch = new LinkedList();
		attrList = new LinkedList();
	}

	/**
	 * コンストラクタ
	 *
	 * @param	tag	要素名
	 * @param	val	要素値情報
	 * @since	UDC1.0
	 */
	public UdcBranchElem(String tag, Object val)
	{
		this();
		tagName = tag;
		tagValue = val;
	}

	/**
	 * 要素名を取得する。
	 *
	 * @return	要素名
	 * @since	UDC1.0
	 */
	public String getTagName() { return tagName; }

	/**
	 * 要素名を設定する。
	 *
	 * @param	name	要素名
	 * @since	UDC1.0
	 */
	public void setTagName(String name) { tagName = name; }

	/**
	 * 要素値情報を取得する。
	 *
	 * @return	要素値情報
	 * @since	UDC1.0
	 */
	public Object getTagValue() { return tagValue; }

	/**
	 * 要素値情報を設定する。
	 *
	 * @param	val	要素値情報
	 * @since	UDC1.0
	 */
	public void setTagValue(Object val) { tagValue = val; }

	/**
	 * 指定された名前の属性情報を取得する。
	 *
	 * @return 指定された名前の属性情報
	 * @param	name 属性名
	 * @since	UDC1.2
	 */
	public UdcBranchElem getAttr(String name)
	{
		if (attrList == null) { return null; }
		UdcBranchElem elm;
		for (int i=0; i<attrList.size(); i++) {
			elm = (UdcBranchElem)attrList.get(i);
			if (name.equals(elm.getTagName())) {
				return elm;
			}
		}
		return null;
	}

	/**
	 * 指定された名前の属性情報を追加する。
	 *
	 * @return	追加した属性情報（同一名の情報が存在する場合はnull）
	 * @param	attr 属性情報
	 * @since	UDC1.2
	 */
	public UdcBranchElem addAttr(UdcBranchElem attr)
	{
		if (getAttr(attr.getTagName()) != null) { return null; }
		if (attrList == null) { attrList = new LinkedList(); }
		attrList.add(attr);
		return attr;
	}

	/**
	 * 指定された名前の属性情報を削除する。
	 *
	 * @return 指定された名前の属性情報（同一名の情報が未存在の場合はnull）
	 * @param	name 属性名
	 * @since	UDC1.2
	 */
	public UdcBranchElem removeAttr(String name)
	{
		UdcBranchElem attr;
		if ((attr=getAttr(name)) == null) { return null; }
		attrList.remove(attr);
		return attr;
	}

	/**
	 * 属性情報リストを取得する。
	 *
	 * @return 属性情報リスト
	 * @since	UDC1.2
	 */
	public LinkedList getAttrList() { return attrList; }

	/**
	 * 属性情報リストを設定する。
	 *
	 * @param	attrlist 属性情報リスト
	 * @since	UDC1.2
	 */
	public void setAttrList(LinkedList attrlist) { attrList = attrlist; }

	/**
	 * 分岐木リストの親リンクを取得します。
	 *
	 * @return 	分岐木リストの親リンク
	 * @since	UDC1.0
	 */
	public UdcBranchElem getTrunk() { return trunk; }

	/**
	 * 分岐木リストの分岐木数を取得します。
	 *
	 * @return	分岐木リストの分岐木数
	 * @since	UDC1.0
	 */
	public int sizeBranch() { return branch.size(); }

	/**
	 * 自身配下の分岐木リスト要素数を取得します。
	 *
	 * @return	自身配下の分岐木リスト要素数
	 * @since	UDC1.0
	 */
	public int sizeBranchToLeaf()
	{
		int cnt = 0;
		UdcBranchElem elm;
		for (int i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			cnt ++;
			cnt += elm.sizeBranchToLeaf();
		}
		return cnt;
	}

	/**
	 * 指定した分岐木リスト要素が自身の１階層配下の分岐木リストに含まれるか否か
	 *
	 * @return	自身の１階層配下の分岐木リスト要素としてbrが含まれるか否か
	 * @param	br	検索対象の分岐木リスト要素
	 * @since	UDC1.0
	 */
	public boolean containsBranch(UdcBranchElem br)
	{
		if (br == this) {
			return true;
		}
		return branch.contains(br);
	}

	/**
	 * 指定した分岐木リスト要素が自身配下の分岐木リストに含まれるか否か
	 *
	 * @return	自身配下の分岐木リスト要素としてbrが含まれるか否か
	 * @param	br	検索対象の分岐木リスト要素
	 * @since	UDC1.0
	 */
	public boolean containsBranchToLeaf(UdcBranchElem br)
	{
		if (br == this) {
			return true;
		}

		UdcBranchElem elm;
		for (int i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			if (elm == br) {
				return true;
			}
			if ( elm.containsBranchToLeaf(br) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 自身１階層配下の分岐木リストの先頭からの格納位置を取得する。
	 *
	 * @return	自身１階層配下の分岐木リストの先頭からの格納位置
	 * @param	br	検索対象の分岐木リスト要素
	 * @since	UDC1.0
	 */
	public int indexOfBranch(UdcBranchElem br)
	{
		if (branch.size() <= 0) {
			return -1;
		}
		return branch.indexOf(br);
	}

	/**
	 * 自身１階層配下の分岐木リスト要素を先頭からの位置指定で取得する。
	 *
	 * @return	指定した格納位置の分岐木リスト要素
	 * @param	offset	自身の１階層配下の分岐木リスト要素の格納位置
	 * @since	UDC1.0
	 */
	public UdcBranchElem get(int offset)
	{
		if (branch.size() <= offset) {
			return null;
		}
		return (UdcBranchElem)branch.get(offset);
	}

	/**
	 * 自身１階層配下の分岐木リストの先頭要素を取得する。
	 *
	 * @return	自身１階層配下の分岐木リストの先頭要素
	 * @since	UDC1.0
	 */
	public UdcBranchElem getFirstBranch()
	{
		if (branch.size() <= 0) {
			return null;
		}
		return (UdcBranchElem)branch.getFirst();
	}

	/**
	 * 自身１階層配下の分岐木リストの最終要素を取得する。
	 *
	 * @return	自身１階層配下の分岐木リストの最終要素
	 * @since	UDC1.0
	 */
	public UdcBranchElem getLastBranch()
	{
		if (branch.size() <= 0) {
			return null;
		}
		return (UdcBranchElem)branch.getLast();
	}

	/**
	 * 自身１階層配下の分岐木リストに指定した要素を追加する。
	 *
	 * @return	正常(true)/異常(false) 異常となるケースは、同一要素を重複登録した場合のみです。
	 * @param	br		自身１階層配下に追加する分岐木リスト要素
	 * @since	UDC1.0
	 */
	public boolean addBranch(UdcBranchElem br)
	{
		br.trunk = this;
		return branch.add(br);
	}

	/**
	 * 自身１階層配下の分岐木リストに指定した要素を追加する。
	 *
	 * @param	index	分岐リストの追加位置
	 * @param	br		自身１階層配下に追加する分岐木リスト要素
	 * @since	UDC1.0
	 */
	public void addBranch(int index, UdcBranchElem br)
	{
		br.trunk = this;
		branch.add(index,br);
	}

	/**
	 * 自身１階層配下の分岐木リストから指定した要素をbrを外す。
	 *
	 * @return	正常(true)/異常(false) 	異常となるケースは、elmのリスト未登録の場合です。
	 * @param	br		自身１階層配下から外す分岐木リスト要素
	 * @since	UDC1.0
	 */
	public boolean removeBranch(UdcBranchElem br)
	{
		br.trunk = null;
		return branch.remove(br);
	}

	/**
	 * 自身配下の最初の分岐木の末端要素を取得します。
	 *
	 * @return	自身配下の最初の分岐木の末端要素
	 * @since	UDC1.0
	 */
	public UdcBranchElem getFirstLeaf()
	{
		UdcBranchElem elm = getFirstBranch();
		if (elm != null) {
			return elm.getFirstLeaf();
		}
		return this;
	}

	/**
	 * 自身配下の最初の分岐木の末端要素を取得します。もし、末端要素が存在しない場合は、
	 * 親要素から見た自身の次要素の最初の分岐木の末端要素を取得します。
	 *
	 * @return	自身配下の最初の分岐木の末端要素か、それが存在しない場合は 親要素から見た自身の次要素の最初の分岐木の末端要素を取得します。
	 * @since	UDC1.0
	 */
	public UdcBranchElem getNextLeaf()
	{
		UdcBranchElem elm = getFirstBranch();
		if (elm != null) {
			return elm.getFirstLeaf();
		}

		UdcBranchElem parent = trunk;
		UdcBranchElem child  = this;
		int pos = parent.indexOfBranch(child);
		if (pos < 0) {
			return null;
		}
		while (parent != null) {
			for (int i=pos+1; i<parent.sizeBranch(); i++) {
				elm = parent.get(i);
				if ((elm=elm.getFirstLeaf()) != null) {
					return elm;
				}
			}
			child = parent;
			parent = parent.trunk;
			pos = (parent != null) ? parent.indexOfBranch(child) : 0;
			if (pos < 0) {
				break;
			}
		}
		return null;
	}

	/**
	 * 自身配下の分岐木要素からタグ名たtagに一致した最初の要素を取得する。
	 *
	 * @return	自身配下の分岐木要素からタグ名たtagに一致した要素
	 * @param	tag	検索タグ名
	 * @since	UDC1.0
	 */
	public UdcBranchElem searchFirstForTag(String tag)
	{
		if (tagName != null) {
			if (tagName.equals(tag)) {
				return this;
			}
		}
		UdcBranchElem res,elm;
		for (int i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			if ((res=elm.searchFirstForTag(tag)) != null) {
				return res;
			}
		}
		return null;
	}

	/**
	 * 自身配下の分岐木要素からタグ名たtagに一致した要素を取得する。
	 *
	 * @return	自身配下の分岐木要素からタグ名たtagに一致した要素
	 * @param	tag	検索タグ名
	 * @since	UDC1.0
	 */
	public UdcBranchElem[] searchForTag(String tag)
	{
		LinkedList list = new LinkedList();
		if (tagName != null && tagName.equals(tag)) {
			list.add(this);
		}

		int i,j;
		UdcBranchElem elm;
		for (i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			UdcBranchElem[] res = elm.searchForTag(tag);
			if (res != null && res.length > 0) {
				for (j=0; j<res.length; j++) {
					list.add(res[j]);
				}
			}
		}
		if (list.size() <= 0) {
			return null;
		}

		UdcBranchElem[] d = new UdcBranchElem[list.size()];
		for (i=0; i<list.size(); i++) {
			d[i] = (UdcBranchElem)list.get(i);
		}
		return d;
	}

	/**
	 * 分岐リストを構成するLeaf要素中(分岐木要素で子要素をもたない要素)でタグ名が全て
	 * ユニークか否かをチェックする。
	 *
	 * @return タグ名が全ての要素でユニークか否か
	 * @since	UDC1.0
	 */
	public boolean isUniqueLeafTagName()
	{
		int i,j;
		String key;
		UdcBranchElem elm,celm;
		for (i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			if ((key=elm.getTagName()) == null) {
				return false;
			}
			for (j=0; j<elm.sizeBranch(); j++) {
				celm = (UdcBranchElem)elm.get(i);
				if (celm.getTagName() == null || key.equals(celm.getTagName()) ) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 自身の一階層下をtabNameでソートする。ソート方法は Collections.sort を参照のこと。
	 *
	 * @since	UDC1.0
	 */
	public void sortBranchTag()
	{
		Collections.sort(branch);			
	}

	/**
	 * 自身の配下全てをソートする。ソート方法は Collections.sort を参照のこと。
	 *
	 * @since	UDC1.0
	 */
	public void sortTag()
	{
		Collections.sort(branch);			

		UdcBranchElem elm;
		for (int i=0; i<branch.size(); i++) {
			elm = (UdcBranchElem)branch.get(i);
			elm.sortTag();
		}
	}

	/**
	 * Comparable.compareToの実装。
	 *
	 * @return	このオブジェクトが指定されたオブジェクトより小さい場合は負の整数、等しい場合はゼロ、大きい場合は正の整数
	 * @param	obj	比較対象オブジェクト
	 * @since	UDC1.0
	 */
	public int compareTo(Object obj)
	{
		UdcBranchElem elm = (UdcBranchElem)obj;
		return tagName.compareTo(elm.getTagName());
	}
}

