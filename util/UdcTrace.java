/* *********************************************************************
 * @(#)UdcTrace.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * デバッグ用トレースユーティリティ。
 * 本クラスはデバッグ等で出力する情報を標準出力/標準エラー/ファイル
 * の任意の出力streamに出力する。
 * 出力streamにファイルを選択した場合、指定回数出力を行うと自動的に
 * 指定ファイル名.yyyyMMddHHmmssにファイル名を変更し、新たに指定ファイル
 * 名に保存を始めます。
 *
 * (注意)
 * 	UDC内部のトレース出力レベルは、[100]です。ユーザで使用する値はこれを考慮してください。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcTrace
{
	/**
	 * UDC内部トレースの出力レベル。
	 * @since	UDC1.0
	 */
	public static final int Level = 100;

	/**
	 * トレース文字列の日時情報のフォーマット。
	 * @since	UDC1.0
	 */
	private static SimpleDateFormat tracedf = new SimpleDateFormat("yy.MM/dd.HH:mm:ss.SSS");

	/**
	 * トレースファイルの拡張子フォーマット(日時情報フォーマット)。
	 * @since	UDC1.0
	 */
	private static SimpleDateFormat filedf = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * トレース出力先ストリーム。
	 * @since	UDC1.0
	 */
	private static PrintStream traceout = null;

	/**
	 * トレース出力先名。
	 * ファイル時　　　：ファイル名
	 * 標準出力時　　　："out" or "stdout"
	 * 標準エラー出力時："err" or "stderr"
	 * @since	UDC1.0
	 */
	private static String traceFile = "out";

	/**
	 * トレース出力先がファイルの場合のファイルストリーム。
	 * @since	UDC1.0
	 */
	private static FileOutputStream fileStream = null;

	/**
	 * トレース出力先に対して出力した回数
	 * @since	UDC1.0
	 */
	private static int traceCount = 0;

	/**
	 * トレース出力先(ファイル時)を出力した回数が本メンバ変数に達したら保存ファイルを自動的に変更する。
	 * @since	UDC1.0
	 */
	private static int traceCountMax = 10000;

	/**
	 * トレース出力許可レベル。UdcTrace.traceメンバで指定されたレベルが本メンバ変数
	 * より大きければトレース出力対象となります。
	 * @since	UDC1.0
	 */
	private static int traceLevel = 0;

	/**
	 * トレース出力をオープンする。
	 *
	 * @param	tracedestination	トレース出力ファイル名
	 * @param	level				トレース出力を行うレベル（指定したレベル以上なら出力を行う）
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public synchronized static int openTrace(String tracedestination, int level)
	{
		int ret = 0;
		traceCount = 0;
		if (tracedestination.equals("stdout") || tracedestination.equals("out")) {
			traceout = new PrintStream(System.out);
			traceFile = tracedestination;
		} else if (tracedestination.equals("stderr") || tracedestination.equals("err")) {
			traceout = new PrintStream(System.err);
			traceFile = tracedestination;
		} else {
			try {
				File file = new File(tracedestination);
				if (file.exists()) {
					if (file.isDirectory()) {
						return -1;
					}
					if (! file.canWrite()) {
						return -1;
					}
					file.renameTo(new File(file.getAbsolutePath() + "." + filedf.format(new Date())));
					file = new File(tracedestination);
				}
				traceFile = tracedestination;
				fileStream = new FileOutputStream(file);
				traceout = new PrintStream(fileStream);
			} catch (FileNotFoundException e) {
				ret = -1;
			}
		}
		if (ret == 0) {
			traceLevel = level;
		}
		return ret;
	}

	/**
	 * トレース出力を停止する。
	 *
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public synchronized static int closeTrace()
	{
		int ret = 0;
		if (fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException e) {
				ret = -1;
			}
		}
		traceCount = 0;
		traceFile = null;
		fileStream = null;
		traceout = null;
		return ret;
	}

	/**
	 * 以下のフォーマットでトレース出力を行う。
	 * YY.MM/DD.HH:mm:ss.SSS : トレース出力文字列
	 *  (注意)
	 *	  UdcTrace.openTraceで指定したトレース出力許可レベルより本メンバで指定されるレベル
	 *    が大きければトレース出力対象となります。
	 *
	 * @param	level		トレース出力のレベル
	 * @param	traceData	トレース出力文字列
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public static int trace(int level,String traceData)
	{
		return trace(level,null,traceData,null);
	}

	/**
	 * 以下のフォーマットでトレース出力を行う。
	 * YY.MM/DD.HH:mm:ss.SSS : [トレース出力文字列1] トレース出力文字列2
	 *  (注意)
	 *	  UdcTrace.openTraceで指定したトレース出力許可レベルより本メンバで指定されるレベル
	 *    が大きければトレース出力対象となります。
	 *
	 * @param	level		トレース出力のレベル
	 * @param	position	トレース出力文字列1
	 * @param	traceData	トレース出力文字列2
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public static int trace(int level,String position,String traceData)
	{
		return trace(level,position,traceData,null);
	}

	/**
	 * 以下のフォーマットでトレース出力を行う。
	 * YY.MM/DD.HH:mm:ss.SSS : [トレース出力文字列1] トレース出力文字列2
	 *     WWXXYYZZ WWXXYYZZ WWXXYYZZ WWXXYYZZ  WWXXYYZZ WWXXYYZZ WWXXYYZZ WWXXYYZZ
	 *     WWXXYYZZ WWXXYYZZ WWXXYYZZ WWXXYYZZ  WWXXYYZZ WWXXYYZZ WWXXYYZZ WWXXYYZZ
	 *  (注意)
	 *	  UdcTrace.openTraceで指定したトレース出力許可レベルより本メンバで指定されるレベル
	 *    が大きければトレース出力対象となります。
	 *
	 * @param	level		トレース出力のレベル
	 * @param	position	トレース出力文字列1
	 * @param	traceData	トレース出力文字列2
	 * param	binaryData	バイナリデータ列（nullを指定した場合何もしない）
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public synchronized static int trace(int level,String position,String traceData,byte[] binaryData)
	{
		if (traceout == null || level < traceLevel) {
			return -1;
		}
		if (position == null) {
			traceout.println( tracedf.format( new Date() ) + " : " + traceData);
		} else {
			traceout.println( tracedf.format( new Date() ) + " : [" + position + "]" + traceData);
		}
		if (binaryData != null) {
			String lineStr = "    ";
			int len = binaryData.length;
			for (int i=0; i<len; i++) {
				lineStr += UdcCoder.byte2HexString(binaryData[i]);
				if (((i+1)%4) == 0)  {
					if (((i+1)%16) == 0) {
						if (((i+1)%32) == 0) {
							traceout.println(lineStr);
							lineStr = "    ";
							continue;
						}
						lineStr += " ";
					}
					lineStr += " ";
				}
			}
			if ((len%32) != 0) { traceout.println(lineStr); }
		}

		traceCount ++;
		if (fileStream != null && traceCount >= traceCountMax) {
			traceCount = 0;
			try {
				fileStream.close();
				File file = new File(traceFile);
				file.renameTo( new File(traceFile + "." + filedf.format(new Date())) );
				file = new File(traceFile);
				fileStream = new FileOutputStream(file);
				traceout = new PrintStream(fileStream);
			} catch (IOException e) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * 以下のフォーマットでトレース出力を行う。
	 * YY.MM/DD.HH:mm:ss.SSS : <br>
	 * exp.printStackTrace()
	 *  (注意)
	 *	  UdcTrace.openTraceで指定したトレース出力許可レベルより本メンバで指定されるレベル
	 *    が大きければトレース出力対象となります。
	 *
	 * @param	level		トレース出力のレベル
	 * @param	exp			トレース出力Exception
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public synchronized static int trace(int level, Exception exp)
	{
		return trace(level, null, exp);
	}

	/**
	 * 以下のフォーマットでトレース出力を行う。
	 * YY.MM/DD.HH:mm:ss.SSS : <br>
	 * exp.printStackTrace()
	 *  (注意)
	 *	  UdcTrace.openTraceで指定したトレース出力許可レベルより本メンバで指定されるレベル
	 *    が大きければトレース出力対象となります。
	 *
	 * @param	level		トレース出力のレベル
	 * @param	position	トレース出力文字列1
	 * @param	exp			トレース出力Exception
	 * @return	[0]正常/[非0]異常
	 * @since	UDC1.0
	 */
	public synchronized static int trace(int level, String position, Exception exp)
	{
		if (traceout == null || level < traceLevel) {
			return -1;
		}
		if (position == null) {
			traceout.println( tracedf.format( new Date() ) + " : ");
		} else {
			traceout.println( tracedf.format( new Date() ) + " : [" + position + "]");
		}
		if (exp != null) {
			exp.printStackTrace(traceout);
		}
		return 0;
	}

	/**
	 * openTraceメンバ関数によってオープンされたファイルへの最大書き込み回数
	 * を設定する。デフォルトは10000に設定されている。
	 *
	 * @param	max		1ファイルに出力する最大回数
	 * @since	UDC1.0
	 */
	public synchronized static void setTraceCountMax(int max)
	{
		traceCountMax = max;
	}

	/**
	 * トレース出力レベルを設定する。traceメンバコール時に、本メンバで設定したlevel
	 * 以上ならトレース対象としてみなされます。
	 *
	 * @param	level				トレース出力を行うレベル（指定したレベル以上なら出力を行う）
	 * @since	UDC1.0
	 */
	public synchronized static void setTraceLevel(int level)
	{
		traceLevel = level;
	}
}

