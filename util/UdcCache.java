/* *********************************************************************
 * @(#)UdcCache.java 1.0, 30 Jun 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;

/**
 * 汎用キャッシュクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 30 Jun 2006
 * @see UdcThreadOperation
 * @since   UDC1.2
 */
public class UdcCache implements Comparable
{
	/**
	 * キャッシュに格納している属性ハッシュ
	 */	
	public Hashtable 	attrList = new Hashtable();

	/**
	 * キャッシュに格納している属性ハッシュで変化のあった属性ハッシュ
	 */	
	public Hashtable 	modList = new Hashtable();

	/**
	 * toString()メンバ関数出力時の属性キー
	 */
	public String		toStringKey = null;

	/**
	 * 二つのUdcCacheを辞書的に比較します。比較は、toStringkeyが設定されている場合にはその情報で比較を行い、設定されていない場合は無条件に１を返却します。
	 * また比較対象情報がComparableでない 場合にも無条件に１を返却します。
	 * @return 0:等しい/1以上:引数より大きい/0未満:引数より小さい
	 * @param an	比較対象
	 */
	public int compareTo(Object an)
	{
		if (toStringKey == null) { return 1; }

		Object c1 = getAttr(toStringKey);
		Object c2 = ((UdcCache)an).getAttr(toStringKey);
		if (c1 == null && c2 == null) {
			return 0;
		} else if (c1 == null) {
			return -1;
		} else if (c2 == null) {
			return 1;
		} else if(c1 instanceof Comparable && c2 instanceof Comparable) {
			Comparable cc1 = (Comparable)c1;
			Comparable cc2 = (Comparable)c2;
			return cc1.compareTo(cc2);
		}
		return 1;
	}

	/**
  	 *	ハッシュに属性設定があるか否かを取得する。
  	 *	@return ハッシュに属性設定があるか否か
	 */
	public boolean isAttrSet()
	{
		return ((attrList.size() > 0) ? true: false);
	}

	/**
  	 *	ハッシュに変更属性設定があるか否かを取得する。
  	 *	@return ハッシュに変更属性設定があるか否か
	 */
	public boolean isModAttrSet()
	{
		return ((modList.size() > 0) ? true: false);
	}

	/**
  	 *	任意の属性を取得する。
	 *	@return 属性名に対応するキャッシュ情報
	 *	@param	attrName	属性名
	 */
	public Object getAttr(String attrName) 
	{
		Object obj = getModAttr(attrName);
		if (obj != null) { return obj; }
		return attrList.get(attrName);
	}

	/**
  	 *	任意の変更属性を取得する。
	 *	@return 変更属性名に対応するキャッシュ情報
	 *	@param	attrName	属性名
	 */
	public Object getModAttr(String attrName) 
	{
		return modList.get(attrName);
	}

	/**
  	 *	任意の属性を設定する。本メンバ関数では、変更属性への設定は行なわない
	 *	@param	attrName	属性名
	 *	@param	attrVal 	属性名に対応するキャッシュ情報
	 */
	public void setAttr(String attrName, Object attrVal) 
	{
		setAttr(attrName, attrVal, false);
	}

	/**
  	 *	任意の属性を設定する。updateをtrueに指定しても属性がattrListに設定されていない場合には、modListへの反映は
	 *	行なわずにsetAttr(String, Object)メンバと同一の動作をします。また、attrListに既に設定があっても、equals()
	 *  による比較結果で同一の場合も同様にsetAttr(String, Object)と同様の動作をします。
	 *	@param	attrName	属性名
	 *	@param	attrVal 	属性名に対応するキャッシュ情報
	 *	@param	update	 	設定を変更属性に反映させるか否か
	 */
	public void setAttr(String attrName, Object attrVal, boolean update) 
	{
		if (update) {
			Object obj = getAttr(attrName);
			if (obj != null && !obj.equals(attrVal)) {
				if (getModAttr(attrName) != null) { modList.remove(attrName); }
				modList.put(attrName, attrVal);
			} else {
				attrList.put(attrName, attrVal);
			}
		} else {
			if (getAttr(attrName) != null) { attrList.remove(attrName); }
			attrList.put(attrName, attrVal);
		}
	}

	/**
  	 *	任意の属性をクリアする。
	 *	@param	attrName	属性名
	 */
	public void resetAttr(String attrName) 
	{
		if (getAttr(attrName) != null) {
			attrList.remove(attrName);
			if (getModAttr(attrName) != null) { modList.remove(attrName); }
		}
	}

	/**
  	 *	クリアする。
	 */
	public void clear() 
	{
		attrList.clear();
		modList.clear();
	}

	/**
	 * toString()メンバ関数出力時の属性キーを取得する。
  	 *	@return toString()メンバ関数出力時の属性キー
	 */
	public String getToStringKey()
	{
		return toStringKey;
	}

	/**
	 * toString()メンバ関数出力時の属性キーを設定する。
  	 *	@param key  toString()メンバ関数出力時の属性キー
	 */
	public void setToStringKey(String key)
	{
		toStringKey = key;
	}

	/**
	 * 本インスタンスの文字列情報を取得する。
	 *
	 * @return  本インスタンス情報
	 * @since   UDC1.0
	 */
	public synchronized String toString()
	{
		if (toStringKey != null) { return "" + getAttr(toStringKey); }
		return super.toString();
	}

	/**
	 * キャッシュリストから指定情報の配列格納位置を取得する。
	 *
	 * @return  検索条件にマッチしたキャッシュの配列格納位置(未検索時-1)
	 * @since   UDC1.0
	 */
	public static int indexOfCacheList(Vector list, String keyName, Object key)
	{
		Object obj;
		int sz = list.size();
		for (int i=0; i<sz; i++) {
			obj = ((UdcCache)list.get(i)).getAttr(keyName);
			if (obj != null && key.equals(obj)) { return i; }
		}
		return -1;
	}

	/**
	 * キャッシュリストから指定情報を検索する。
	 *
	 * @return  検索条件にマッチしたキャッシュ
	 * @since   UDC1.0
	 */
	public static UdcCache searchCacheList(Vector list, String keyName, Object key)
	{
		UdcCache ca;
		Object obj;
		int sz = list.size();
		for (int i=0; i<sz; i++) {
			ca = (UdcCache)list.get(i);
			obj = ca.getAttr(keyName);
			if (obj != null && key.equals(obj)) { return ca; }
		}
		return null;
	}

	/**
	 * キャッシュリストから指定情報を検索する。
	 *
	 * @return  検索条件にマッチしたキャッシュリスト(一致する要素がない場合にはnullを返却)
	 * @since   UDC1.0
	 */
	public static Vector searchCachesList(Vector list, String keyName, Object key)
	{
		UdcCache ca;
		Object obj;
		Vector mlist = new Vector();
		int sz = list.size();
		for (int i=0; i<sz; i++) {
			ca = (UdcCache)list.get(i);
			obj = ca.getAttr(keyName);
			if (obj != null && key.equals(obj)) { mlist.add(ca); }
		}
		if (mlist.size() < 0) { return null; }
		return mlist;
	}
}
