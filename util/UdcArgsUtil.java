/* *********************************************************************
 * @(#)UdcArgsUtil.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.io.*;

/**
 * パラメータ解析基本ユーティリティ
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcArgsUtil
{
	/**
	 * コマンドパラメータにoptionで指定したオプション文字列が存在するか
	 * を判定する。
	 *
	 * @param	args コマンドパラメータ列
	 * @param	option 指定オプション文字列
	 * @return	optionで指定したオプションがargs内に存在すれば(true)
	 * @since	UDC1.0
	 */
	public static boolean analyzeArgsOption(String[] args,String option)
	{
		return analyzeArgsOption(args,option,false);
	}

	/**
	 * コマンドパラメータにoptionで指定したオプション文字列が存在するか
	 * を判定する。
	 *
	 * @param	args コマンドパラメータ列
	 * @param	option 指定オプション文字列
	 * @param	argdel オプション一致パラメータを""に設定するかいなかのフラグ
	 * @return	optionで指定したオプションがargs内に存在すれば(true)
	 * @since	UDC1.0
	 */
	public static boolean analyzeArgsOption(String[] args,String option,boolean argdel)
	{
		int argc = args.length;
		for (int i=0; i<argc; i++) {
			if (args[i].equals(option)) {
				if (argdel == true) {
					args[i] = "";
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * コマンドパラメータにoptionで指定したオプション文字列が存在するか
	 * を判定し、存在した場合、その次の文字列を返却する。
	 *
	 * @param	args コマンドパラメータ列
	 * @param	option 指定オプション文字列
	 * @return	optionで指定したオプションの次のパラメータ文字列
	 * @since	UDC1.0
	 */
	public static String analyzeArgsOptionParam(String[] args,String option)
	{
		return analyzeArgsOptionParam(args,option,false);
	}

	/**
	 * コマンドパラメータにoptionで指定したオプション文字列が存在するか
	 * を判定し、存在した場合、その次の文字列を返却する。
	 *
	 * @param	args コマンドパラメータ列
	 * @param	option 指定オプション文字列
	 * @param	argdel オプション一致パラメータを""に設定するかいなかのフラグ
	 * @return	optionで指定したオプションの次のパラメータ文字列
	 * @since	UDC1.0
	 */
	public static String analyzeArgsOptionParam(String[] args,String option, boolean argdel)
	{
		String param = null;
		int argc = args.length;
		for (int i=0; i<argc; i++) {
			if (args[i].equals(option)) {
				if ((i+1) < argc) {
					param = new String(args[i+1]);
					if (argdel == true) {
						args[i] = "";
						args[i+1] = "";
					}
					break;
				}
			}
		}
		return param;
	}

	/**
	 * envfileで指定したパラメータファイルから、envnameで指定された
	 * パラメータ識別子のパラメータを取得する。
	 *  例) 以下の様な設定が任意のファイルにある場合、envname="ENV_DATA_1"
	 *	    delimiter=":" と指定し、{/tmp/trace.log }を取得する。
	 *		ENV_DATA1 : /tmp/trace.log
	 *  注意) envfile では行の先頭に "#" を記述した場合、コメントと見なされます。
	 *
	 * @param	envfile パラメータファイル
	 * @param	envname パラメータ識別子
	 * @param	delimiter パラメータ識別子とパラメータのデリミタ
	 * @return	パラメータ識別子に対応したパラメータ文字列
	 * @since	UDC1.0
	 */
	public static String analyzeEnvFile(File envfile,String envname, String delimiter)
	{
		if (! envfile.exists()) {
			return null;
		}
		int minlen = envname.length() + delimiter.length();
		FileInputStream file = null;
		try {
			file = new FileInputStream(envfile);
			BufferedReader filein = new BufferedReader( new InputStreamReader(file) );
			String envdata;
			while ((envdata=filein.readLine()) != null) {
				if (envdata.length() <= minlen) { continue; }
				if (envdata.charAt(0) == '#') { continue; }
				envdata = envdata.trim();
				if (! envdata.startsWith(envname)) { continue; }
				envdata = envdata.substring(envname.length());
				envdata = envdata.trim();
				if (! envdata.startsWith(delimiter)) { continue; }
				envdata = envdata.substring(delimiter.length());
				envdata = envdata.trim();
				if (envdata.length() <= 0) { continue; }
				file.close();
				return envdata;
			}
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}

		try {
			if (file != null) { file.close(); }
		} catch (IOException e) {}

		return null;
	}
}

