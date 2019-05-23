/* *********************************************************************
 * @(#)UdcJarLoader.java 1.0, 31 Mar 2008
 *
 * Copyright 2008 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.lang;

import java.util.*;
import java.util.jar.*;
import java.lang.*;
import java.lang.reflect.*;
import java.io.*;


/**
 * Jarデータのクラスロード
 *
 * @author  Takayuki Uchida
 * @version 1.0, 31 Mar 2008
 * @since   UDC2.3
 */
public class UdcJarLoader extends ClassLoader
{
	/** Jarのバイト列 */
	byte[] jdata;

	/**
	 * コンストラクタ
	 *
	 * @param data	Jarのバイト列
	 * @since UDC2.3
	 */
	public UdcJarLoader(byte[] data)
	{
		jdata = data;
	}

	/**
	 * ClassLoader.findClassメソッドのオーバーライド関数。
	 *
	 * @return	結果として得られる Class オブジェクト
	 * @param name	クラスのバイナリ名
	 * @since UDC2.3
	 */
	protected Class findClass(String name)
			throws ClassNotFoundException
	{
		String ename;
		String cname = name + ".class";
		JarEntry entry = null;
		JarInputStream jin = null;
		try {
			jin = new JarInputStream(new ByteArrayInputStream(jdata));	
			while ((entry=jin.getNextJarEntry()) != null) {
				if (entry.isDirectory()) { continue; }
				ename = entry.getName();
				if (ename.endsWith(".class")) { ename = ename.replaceAll("/", "."); }
				if (cname.equals(ename)) {
					int rlen;
					byte data[] = new byte[1024];
					ByteArrayOutputStream jout = new ByteArrayOutputStream();
					while ((rlen=jin.read(data, 0, 1024)) != -1) {
						jout.write(data,0,rlen);	
					}
					jin.closeEntry();
					return defineClass(name, jout.toByteArray(), 0, jout.size());
				}
				jin.closeEntry();
			}
		} catch(Exception exp) {
			throw new ClassNotFoundException();
		} finally {
			if (jin != null) { try { jin.close(); } catch(Exception jexp) {} }
		}
		throw new ClassNotFoundException();
	}

	/**
	 * ClassLoader.getResourceAsStreamメソッドのオーバーライド関数。
	 *
	 * @return	リソースを読み込むための入力ストリーム。 リソースが見つからなかった場合は null
	 * @param name	リソース名
	 * @since UDC2.3
	 */
	public InputStream getResourceAsStream(String name)
	{
		JarEntry entry = null;
		JarInputStream jin = null;
		try {
			jin = new JarInputStream(new ByteArrayInputStream(jdata));	
			while ((entry=jin.getNextJarEntry()) != null) {
				if (entry.isDirectory()) { continue; }
				if (entry.getName().equals(name)) {
					int rlen;
					byte data[] = new byte[1024];
					ByteArrayOutputStream jout = new ByteArrayOutputStream();
					while ((rlen=jin.read(data, 0, 1024)) != -1) {
						jout.write(data,0,rlen);	
					}
					jin.closeEntry();
					return new ByteArrayInputStream(jout.toByteArray());
				}
				jin.closeEntry();
			}
		} catch(Exception exp) {
			;
		} finally {
			if (jin != null) { try { jin.close(); } catch(Exception jexp) {} }
		}
		return super.getResourceAsStream(name);
	}
}

