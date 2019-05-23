/* *********************************************************************
 * @(#)UdcUiUtil.java 1.0, 29 Feb 2008
 *
 * Copyright 2008 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui;

import java.awt.*;


/**
 *  Swingのユーティリティクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 29 Feb 2008
 * @since   UDC1.3
 */
public class UdcUiUtil
{
	/**
	 * イメージファイルをImageとして読み込む。
	 *
	 * @param	comp	イメージが最終的に描画される コンポーネント
	 * @param	file	イメージファイル
 	 * @since   UDC1.3
	 */
	public static Image getImage(Component comp, String file)
	{
		Image image = Toolkit.getDefaultToolkit().getImage(file);
		if (image == null) { return null; }
		return UdcUiUtil.getImage(comp, image);
	}

	/**
	 * イメージファイルをJARリソースからImageとして読み込む。
	 *
	 * @param	comp	イメージが最終的に描画される コンポーネント
	 * @param	file	JARに格納されるイメージファイル
 	 * @since   UDC1.3
	 */
	public static Image getImageFromResource(Component comp, String file)
	{
		Image image = Toolkit.getDefaultToolkit().getImage(comp.getClass().getResource(file));
		if (image == null) { return null; }
		return getImage(comp, image);
	}

	/**
	 * Image読み込み完了をMediaTrackerを使用して待つ
	 *
	 * @param	comp	イメージが最終的に描画される コンポーネント
	 * @param	image	読み込みを完了させるイメージ
 	 * @since   UDC1.3
	 */
	public static Image getImage(Component comp, Image image)
	{
		if (image == null) { return null; }
		MediaTracker tracker = new MediaTracker(comp);
		tracker.addImage(image, 0);
		tracker.checkAll(true);
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			return null;
		}
		return image;
	}
}

