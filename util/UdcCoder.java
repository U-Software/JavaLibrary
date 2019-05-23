/* *********************************************************************
 * @(#)UdcCoder.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.io.*;
import java.util.*;

/**
 * エンコード・デコードユーティリティ。<br>
 * ver1.1 よりBase64-encode/decode機能を追加。
 *
 * @author  Takayuki Uchida
 * @version 1.23, 31 Mar 2008
 * @since   UDC1.23
 */
public class UdcCoder
{
	/** HEX文字変換表 */
	final static char[] HEX = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/** BASE64 用変換表 */
	final static char[] ascii = {
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
		'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'};

	/**
	 * バイトを16進数文字列情報に変換する。
	 *
	 * @return	バイトを String型 に変換したもの
	 * @param	val		バイト情報
	 * @since	UDC1.0
	 */
	public static String byte2HexString(byte val)
	{
		int val1 = (val >> 4) & 0x0F;
		int val2 = (val & 0x0F);
		return ("" + HEX[val1] + HEX[val2]);
	}

	/**
	 * バイト列を16進数文字列情報に変換する。
	 *
	 * @return	バイト列を String型 に変換したもの
	 * @param	vals	バイト列
	 * @since	UDC1.0
	 */
	public static String bytes2HexString(byte[] vals)
	{
		String str = "";
		if (vals != null) {
			int len = vals.length;
			for (int i=0; i<len; i++) { str += byte2HexString(vals[i]); }
		}
		return str;
	}

	/**
	 * 16進数文字列をバイト列情報に変換する。
	 *
	 * @return	バイト列情報
	 * @param	str	16進数文字列
	 * @since	UDC1.0
	 */
	public static byte[] hexString2Bytes(String str)
		throws IOException
	{
		if (str.length() == 0) {
			return null;
		}
		byte data[];
		if ((str.length() % 2) == 0){ data = new byte[str.length() / 2]; }
		else 						{ data = new byte[(str.length() / 2) + 1]; }
		int i,j,v1,v2;
		char c1,c2;
		for (i=0; i<str.length(); i+=2) {
			c1 = str.charAt(i);
			if ((str.length() - i) >= 2){ c2 = str.charAt(i+1); }
			else 						{ c2 = '0'; }
			v1 = v2 = -1;
			for (j=0; j<HEX.length; j++) { if (c1 == HEX[j]) { v1 = j; } }
			for (j=0; j<HEX.length; j++) { if (c2 == HEX[j]) { v2 = j; } }
			if (v1 < 0 || v2 < 0) {
				throw new IOException("UdcCoder.hexString2Bytes - illegal data value.");
			}
			data[i/2] = (byte)((v1 << 4) | v2);
		}
		return data;
	}

	/**
	 * バイト列をshort型情報に変換する。
	 *
	 * @return	バイト列を short型 に変換したもの
	 * @param	data	バイト列
	 * @param	signed 	signed型で変換するか否か
	 * @since	UDC1.0
	 */
	public static short bytes2Short(byte[] data,boolean signed)
		throws IOException
	{
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
		int size = data.length;
		if (size > 2) { size = 2; }
		int val = 0;
		if (signed == true) { val = din.readByte(); }
		else 				{ val = din.readUnsignedByte(); }
		for (int i=1; i<size; i++) {
			val = (val << 8) + din.readUnsignedByte();
		}
		return (short)val;
	}

	/**
	 * バイト列をint型情報に変換する。
	 *
	 * @return	バイト列を int型 に変換したもの
	 * @param	data	バイト列
	 * @param	signed 	signed型で変換するか否か
	 * @since	UDC1.0
	 */
	public static int bytes2Int(byte[] data,boolean signed)
		throws IOException
	{
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
		int size = data.length;
		if (size > 4) { size = 4; }
			/* signed型の実装のために先頭バイトはsignedで取得 */
		int val = 0;
		if (signed == true) { val = din.readByte(); }
		else 				{ val = din.readUnsignedByte();	}
		for (int i=1; i<size; i++) {
			val = (val << 8) + din.readUnsignedByte();
		}
		return val;
	}

	/**
	 * バイト列をlong型情報に変換する。
	 *
	 * @return	バイト列を long型 に変換したもの
	 * @param	data	バイト列
	 * @param	signed 	signed型で変換するか否か
	 * @since	UDC1.0
	 */
	public static long bytes2Long(byte[] data,boolean signed)
		throws IOException
	{
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
		int size = data.length;
		if (size > 8) { size = 8; }
		long val = 0;
		if (signed == true) { val = din.readByte(); }
		else 				{ val = din.readUnsignedByte(); }
		for (int i=1; i<size; i++) {
			val = (val << 8) + din.readUnsignedByte();
		}
		return val;
	}

	/**
	 * バイト列をfloat型情報に変換する。
	 *
	 * @return	バイト列を float型 に変換したもの
	 * @param	data	バイト列
	 * @since	UDC1.0
	 */
	public static float bytes2Float(byte[] data)
		throws IOException
	{
		DataInputStream din = new DataInputStream(new ByteArrayInputStream(data));
		float val = din.readFloat();
		return val;
	}

	/**
	 * バイト列をString型情報に変換する。
	 *
	 * @return	バイト列を String型 に変換したもの
	 * @param	data	バイト列
	 * @since	UDC1.0
	 */
	public static String bytes2String(byte[] data)
		throws IOException
	{
		String val = new String(data);
		return val;
	}

	/**
	 * short型情報をバイト列に変換する。
	 *
	 * @return	short型情報を変換したバイト列
	 * @param	data		short型情報
	 * @param	encodeLen	エンコード長
	 * @since	UDC1.0
	 */
	public static byte[] short2Bytes(short data, int encodeLen)
	{
		byte[] val = new byte[encodeLen];
		short2Bytes(data,encodeLen,val,0);
		return val;
	}

	/**
	 * short型情報をバイト列に変換する。
	 *
	 * @param	data		short型情報
	 * @param	encodeLen	エンコード長
	 * @param 	val			short型情報を変換するバイト列格納エリア
	 * @param	pos			valの格納開始位置
	 * @since	UDC1.0
	 */
	public static void short2Bytes(short data, int encodeLen, byte[] val, int pos)
	{
		for (int i=(encodeLen-1); i>=0; i--) {
			val[encodeLen-i-1+pos] = (byte)((data >> (i*8)) & 0x00FF);
		}
	}

	/**
	 * int型情報をバイト列に変換する。
	 *
	 * @return	int型情報を変換したバイト列
	 * @param	data		int型情報
	 * @param	encodeLen	エンコード長
	 * @since	UDC1.0
	 */
	public static byte[] int2Bytes(int data, int encodeLen)
	{
		byte[] val = new byte[encodeLen];
		int2Bytes(data,encodeLen,val,0);
		return val;
	}

	/**
	 * int型情報をバイト列に変換する。
	 *
	 * @param	data		int型情報
	 * @param	encodeLen	エンコード長
	 * @param 	val			int型情報を変換するバイト列格納エリア
	 * @param	pos			valの格納開始位置
	 * @since	UDC1.0
	 */
	public static void int2Bytes(int data, int encodeLen, byte[] val, int pos)
	{
		for (int i=(encodeLen-1); i>=0; i--) {
			val[encodeLen-i-1+pos] = (byte)((data >> (i*8)) & 0x000000FF);
		}
	}

	/**
	 * long型情報をバイト列に変換する。
	 *
	 * @return	long型情報を変換したバイト列
	 * @param	data		long型情報
	 * @param	encodeLen	エンコード長
	 * @since	UDC1.0
	 */
	public static byte[] long2Bytes(long data, int encodeLen)
	{
		byte[] val = new byte[encodeLen];
		long2Bytes(data,encodeLen,val,0);
		return val;
	}

	/**
	 * long型情報をバイト列に変換する。
	 *
	 * @param	data		long型情報
	 * @param	encodeLen	エンコード長
	 * @param 	val			long型情報を変換するバイト列格納エリア
	 * @param	pos			valの格納開始位置
	 * @since	UDC1.0
	 */
	public static void long2Bytes(long data, int encodeLen, byte[] val, int pos)
	{
		for (int i=(encodeLen-1); i>=0; i--) {
			val[encodeLen-i-1+pos] = (byte)((data >> (i*8)) & 0x00000000000000FF);
		}
	}

	/**
	 * Float型情報をバイト列に変換する。
	 *
	 * @return	float型情報を変換したバイト列
	 * @param	data		float型情報
	 * @since	UDC1.0
	 */
	public static byte[] float2Bytes(float data)
		throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bos);
		dout.writeFloat(data);
		return bos.toByteArray();
	}

	/**
	 * String型情報をバイト列に変換する。
	 *
	 * @return	String型情報を変換したバイト列
	 * @param	data	String型情報
	 * @since	UDC1.0
	*/
	public static byte[] string2Bytes(String data)
	{
		byte[] value = new byte[data.length()];
		string2Bytes(data,value,0);
		return value;
	}

	/**
	 * String型情報をバイト列に変換する。
	 *
	 * @param	data	String型情報
	 * @param	val		String型を変換したバイト列格納エリア
	 * @param	pos		valの格納位置のオフセット
	 * @since	UDC1.0
	 */
	public static void string2Bytes(String data, byte[] val, int pos)
	{
		char[] s = data.toCharArray();
		int len = s.length;
		for (int i=0; i<len; i++) {
			val[pos+i] = (byte)s[i];
		}
	}

	/**
	 * 暗号化されたbyte型情報を通常のString型情報に戻す。
	 *
	 * @return	変換元String型情報
	 * @param	str 	UDC暗号化したbyte型情報
	 * @since	UDC1.0
	 */
	public static String udcString2String(byte[] str)
	{
		int len = str.length;
		char data[] = new char[len];
		for (int i=0; i<len; i++) {
			data[i]  = UdcCoder.udcStringByte2Char(str[i]);
		}
		return new String(data);
	}

	/**
	 * 暗号化されたbyte型情報を通常のchar型情報に戻す。
	 *
	 * @return	char型情報
	 * @param	b 	UDC暗号化したbyte型情報
	 * @since	UDC1.0
	 */
	public static char udcStringByte2Char(byte b)
	{
		byte bd;
		bd  = (byte)((b >> 5) & 0x07);	// FGHA BCDE -> 0000 0FGH
		bd |= (byte)((b << 3) & 0xF8);	// FGHA BCDE -> ABCD E000
		return (char)bd;
	}

	/**
	 * String型情報をUDC暗号化する。
	 *
	 * @return	UDC暗号化したbyte列情報
	 * @param	str		変換元String型情報
	 * @since	UDC1.0
	 */
	public static byte[] string2UdcString(String str)
		throws IOException
	{
		char c[] = str.toCharArray();
		int len = c.length;
		byte data[] = new byte[len];
		for (int i=0; i<len; i++) {
			data[i] = UdcCoder.char2UdcStringByte(c[i]);
		}
		return data;
	}

	/**
	 * char型情報をUDC暗号化する。
	 *
	 * @return	UDC暗号化したbyte情報
	 * @param	c	変換元char型情報
	 * @since	UDC1.0
	 */
	public static byte char2UdcStringByte(char c)
	{
		byte cd = (byte)c;
		byte bd;
		bd  = (byte)((cd << 5) & 0xE0); 	// ABCD EFGH -> FGH0 0000
		bd |= (byte)((cd >> 3) & 0x1F); 	// ABCD EFGH -> 000A BCDE
		return bd;
	}

	/**
	 * 通常のStringをBlowfishアルゴリズムで暗号化する。
	 *
	 * @return 	Blowfishアルゴリズムで変換したString情報
	 * @param	str		変換元String型情報
	 * @since	UDC1.0
	 */
	public static String encrypt_blowfish(String key, String str)
	{
		String rstr = null;
		try {
			javax.crypto.spec.SecretKeySpec sksSpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "Blowfish");
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("Blowfish");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);
			byte[] enc = cipher.doFinal(str.getBytes());
			rstr = bytes2HexString(enc);
		} catch(Exception exp) {
			return null;
		}
		return rstr;
	}

	/**
	 * 暗号化された情報を通常のString型情報に戻す。
	 *
	 * @return	変換元String型情報
	 * @param	str 	Blowfishアルゴリズムで変換したString情報
	 * @since	UDC1.0
	 */
	public static String decrypt_blowfish(String key, String str)
	{
		String rstr = null;
		try {
			byte[] enc = hexString2Bytes(str);
			javax.crypto.spec.SecretKeySpec sksSpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "Blowfish");
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("Blowfish");
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, sksSpec);
			byte[] dec = cipher.doFinal(enc);
			rstr = new String(dec);
		} catch(Exception exp) {
			return null;
		}
		return rstr;
	}

	/**
	 * Base64コードにエンコードする。
	 *
	 * @return Base64文字列
	 * @param	data	元データ
	 * @param	cols	エンコード時の1行の長さ(0以下は未改行:4の倍数以外は非許容)
	 * @since	UDC1.23
	 */
	public static String base64Encode(byte[] data, int cols)
	{
		return base64Encode(data, 0, data.length, cols);
	}

	/**
	 * Base64コードにエンコードする。
	 *
	 * @return Base64文字列
	 * @param	data	元データ
	 * @param	offset	開始位置
	 * @param	length	サイズ
	 * @param	cols	エンコード時の1行の長さ(0以下は未改行:4の倍数以外は非許容)
	 * @since	UDC1.23
	 */
	public static String base64Encode(byte[] data, int offset, int length, int cols)
	{
		ByteArrayOutputStream out = base64EncodeToStream(data, offset, length, cols, null);
		if (out == null) { return null; }
		String ostr = null;
		try { ostr = new String(out.toByteArray(), "ASCII"); } catch(Exception exp) { ostr = null; }
		return ostr;
	}

	/**
	 * Base64コードにエンコードする。
	 * 
	 * @return Base64のbyte配列
	 * @param	data	元データ
	 * @param	offset	開始位置
	 * @param	length	サイズ
	 * @param	cols	エンコード時の1行の長さ(0以下は未改行:4の倍数以外は非許容)
	 * @since	UDC1.23
	 */
	public static byte[] base64EncodeToByte(byte[] data, int offset, int length, int cols)
	{
		ByteArrayOutputStream out = base64EncodeToStream(data, offset, length, cols, null);
		if (out == null) { return null; }
		return out.toByteArray();
	}

	/**
	 * Base64コードにエンコードする。
	 *
	 * @return Base64データ(ASCII文字)を格納したStream
	 * @param	data	元データ
	 * @param	offset	開始位置
	 * @param	length	サイズ
	 * @param	cols	エンコード時の1行の長さ(0以下は未改行:4の倍数以外は非許容)
	 * @param	os		Base64データ(ASCII文字)を格納するStream(nullの場合は内部で生成)
	 * @since	UDC1.23
	 */
	public static ByteArrayOutputStream base64EncodeToStream(byte[] data, int offset, int length, int cols, ByteArrayOutputStream os)
	{
		// 入力チェック
		if (data == null || offset < 0 || length <= 0 || data.length < (offset+length) || (cols > 0 && (cols%4) != 0)) {
			return null;
		}
		// encodeデータ格納OutputStreamを作成
		ByteArrayOutputStream out = os;
		if (out == null) {
			int encSize = (length + 2) / 3 * 4;
			if (cols > 0) { encSize += (encSize + cols - 1) / cols * 2; } // 改行設定がある場合は改行文字数(\r\n)を追加
			out = new ByteArrayOutputStream(encSize);
		}
		// encode
		int tmp=0, bit=0, col=0, max=offset+length;
		byte[] cr = new byte[2];
		cr[0] = '\r';
		cr[1] = '\n';
		for (int i=offset; i<max; i++) {
			tmp <<= 8;
			tmp |= data[i] & 0xff;
			bit += 8;
			do {
				bit -= 6;
				out.write( ascii[(tmp >> bit) & 0x3f] );
				col ++;
				if (col == cols) {
					out.write(cr, 0, cr.length);
					col = 0;
				}
			} while (bit >= 6);
		}
			// 元データ長が3の倍数でない場合のpadding{(2|4)の残ビット}
		if (bit > 0) {
			out.write( ascii[(tmp << (6 - bit)) & 0x3f] );
			bit += (8 - 6);
			do {
				out.write('=');
				if ((bit -= 6) < 0) { bit += 8; }
			} while (bit > 0);
		}
		if (cols > 0 && col > 0) {
			out.write(cr, 0, cr.length);
		}
		return out;
	}

	/**
	 * Base64コードをデコードする。
	 *
	 * @return デコードデータ
	 * @param data Base64データ(ASCII文字)
	 * @since	UDC1.23
	 */
	public static byte[] base64Decode(String data)
	{
		ByteArrayOutputStream out = base64DecodeStream(data, null);
		if (out == null) { return null; }
		return out.toByteArray();
	}

	/**
	 * Base64コードへデコードする。
	 *
	 * @return デコードデータを格納したStream
	 * @param	data	Base64データ(ASCII文字)
	 * @param	os		デコードデータを格納するStream(nullの場合は内部で生成)
	 * @since	UDC1.23
	 */
	public static ByteArrayOutputStream base64DecodeStream(String data, ByteArrayOutputStream os)
	{
		// 入力チェック
		int dlen = data.length();

		// decodeデータ格納OutputStreamを作成
		ByteArrayOutputStream out = os;
		if (out == null) {
			int decSize = data.length() / 4 * 3;
			out = new ByteArrayOutputStream(decSize);
		}

		// decode
		int b=0, c, i, o, p=0;
		byte[] dt = new byte[3];
		for (i=0,o=0; i<data.length(); i++) {
			c = data.charAt(i);
			if (c >= 'A' && c <='Z')		{ b <<= 6;	b |= c - 'A'; }
			else if (c >= 'a' && c <= 'z')	{ b <<= 6;	b |= c - 'a' + 0x1a; }
			else if (c >= '0' && c <= '9')	{ b <<= 6;	b |= c - '0' + 0x34; }
			else if (c == '+') 				{ b <<= 6;	b |= 0x3e; }
			else if (c == '/') 				{ b <<= 6;	b |= 0x3f; }
			else if (c == '=') 				{ b <<= 6; 	p++; }		// Padding文字のため shiftは実施
			else 							{ continue; } 			// 改行コード(\r\n)/異常文字は読み飛ばし
			o ++;
			if ((o%4) == 0) {
				if (p < 3) {
					if (p <= 2) { dt[0] = (byte)((b >> 16) & 0xff); }
					if (p <= 1) { dt[1] = (byte)((b >> 8) & 0xff); }
					if (p <= 0) { dt[2] = (byte)(b & 0xff); }
					out.write(dt, 0, dt.length - p);
				}
				b = p = 0;
			}
		}
		if ((o%4) != 0) { out = null; }
		return out;
	}

	/**
	 * charset1ファイルをcharset2ファイルに変換する。
	 *
	 * @return 	0:正常/非0:異常
	 * @param	srcpath		変換元ファイル名(charset1ファイル)
	 * @param	srccharset	変換元ファイルのcharset	
	 * @param	dstpath		変換先ファイル名(charset2ファイル)
	 * @param	dstcharset	変換先ファイルのcharset	
	 * @since	UDC1.2
	 */
	public static int charset1Tocharset2(String srcpath, String srccharset, String dstpath, String dstcharset)
	{
		File src = new File(srcpath);
		if (!src.exists()) { return -1;	}
		File dst = new File(dstpath);

		FileInputStream fin = null;
		FileOutputStream fout = null;
		InputStreamReader is = null;
		OutputStreamWriter os = null;
		try {
			dst.createNewFile();
			fin = new FileInputStream(src);
			fout = new FileOutputStream(dst);
			is = new InputStreamReader(fin, srccharset);
			os = new OutputStreamWriter(fout, dstcharset);
			int c;
			while ((c=is.read()) != -1) {
				os.write(c);
			}
			is.close();		is = null;
			fin.close();	fin = null;
			os.close();		os = null;
			fout.close();	fout = null;
		} catch (Exception exp) {
			try { if (is != null) { is.close(); } } catch(Exception dummy) {}
			try { if (fin != null) { fin.close(); } } catch(Exception dummy) {}
			try { if (os != null) { os.close(); } } catch(Exception dummy) {}
			try { if (fout != null) { fout.close(); } } catch(Exception dummy) {}
			return -1;
		}
		return 0;
	}

}

