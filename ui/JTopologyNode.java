/* *********************************************************************
 * @(#)JTopologyNode.java 1.2, 10 Mar 2006
 *
 * Copyright 2005 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.ui.topology;

import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;


/**
 * ネットワーク・トポロジーのノード情報を表すコンポーネント。ネットワーク・トポロジーを構成する
 * ノード情報は本クラスあるいは本クラスを継承したクラスでなければなりません。<br>
 *
 * @author  Takayuki Uchida
 * @version 1.2, 10 Mar 2006
 * @since   UDC1.2
 */
public class JTopologyNode extends JComponent
{
	/**
	 * 関連親ノード／子ノードの設定を行っている場合、子ノードは、親ノードとの位置関係を保って移動します。<br>
	 * この時の親ノードの移動量を算出するための、移動前のX座標。
 	 * @since   UDC1.21
	 */
	protected double __oldx;

	/**
	 * 関連親ノード／子ノードの設定を行っている場合、子ノードは、親ノードとの位置関係を保って移動します。<br>
	 * この時の親ノードの移動量を算出するための、移動前のY座標。
 	 * @since   UDC1.21
	 */
	protected double __oldy;

	/**
	 * ノードの表示種別：四角図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Rectangle	= 1;

	/**
	 * ノードの表示種別：丸図形
 	 * @since   UDC1.2
	 */
	public final static int	Type_Arc		= 2;

	/**
	 * ノードの表示種別：Image
 	 * @since   UDC1.2
	 */
	public final static int	Type_Image		= 10;

	/**
	 * ノードの表示種別：JComponent
 	 * @since   UDC1.2
	 */
	public final static int	Type_JComponent	= 11;

	/**
	 * ノードの表示位置種別：表示なし
 	 * @since   UDC1.21
	 */
	public final static int	DispType_No		= 0;

	/**
	 * ノードの表示位置種別：画像の上
 	 * @since   UDC1.21
	 */
	public final static int	DispType_Up		= 1;

	/**
	 * ノードの表示位置種別：画像の中央
 	 * @since   UDC1.21
	 */
	public final static int	DispType_Middle	= 2;

	/**
	 * ノードの表示位置種別：画像の下
 	 * @since   UDC1.21
	 */
	public final static int	DispType_Down	= 3;

	/**
	 * ノード名
 	 * @since   UDC1.2
	 */
	String	__nodeName = "";

	/**
	 * ノード表示名
 	 * @since   UDC1.21
	 */
	String	__nodeDispName = "";

	/**
	 * ノードの表示種別
 	 * @since   UDC1.2
	 */
	int __nodeType = Type_Rectangle;

	/**
	 * ノード図形のWidth
 	 * @since   UDC1.2
	 */
	int	__imageWidth = 0;

	/**
	 * ノード図形のHeight
 	 * @since   UDC1.2
	 */
	int	__imageHeight = 0;

	/**
	 * ノード図形のイメージアイコン
 	 * @since   UDC1.2
	 */
	ImageIcon __nodeImage = null;

	/**
	 * ノード図形のイメージアイコン
 	 * @since   UDC1.2
	 */
	JComponent __nodeComponent = null;

	/**
	 * ノード名の表示位置
 	 * @since   UDC1.21
	 */
	int	__nameDisplay = DispType_No;

	/**
	 * 親パネルのサイズ
 	 * @since   UDC1.2
	 */
	protected Dimension	__parSize = null;
	
	/**
	 * 関連ノードリスト
 	 * @since   UDC1.21
	 */
	protected ArrayList	__relationNodeList = new ArrayList();

	/**
	 * 関連親ノード
 	 * @since   UDC1.21
	 */
	protected JTopologyNode __relationParent = null;	

	/**
	 * ノード位置をTopologyPanelのサイズ変更に併せて移動するか否か
 	 * @since   UDC1.23
	 */
	protected boolean __moveForParChange = true;

	/**
	 * ノードに対するMouseListener
 	 * @since   UDC1.21
	 */
	protected MouseListener	__userMouseListener = null;	

	/**
	 * ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	protected Object	__userObject = null;	

	// JTopologyPanel上での高速表示のための保持情報
	protected boolean paintStore = false;
	protected double cx = -1.0;
	protected double cy = -1.0;
	protected Rectangle2D	dispNameRect = null;

	/**
	 * コンストラクタ
	 */
	public JTopologyNode()
	{
	}

	/**
	 * コンストラクタ
	 *
	 * @param 	name	ノード名
	 * @param 	type	ノードの表示種別
	 * @param	width	ノード図形のWidth
	 * @param	height	ノード図形のHeight
 	 * @since   UDC1.2
	 */
	public JTopologyNode(String name, int type, int width, int height)
	{
		super();
		if (name != null) {
			__nameDisplay = DispType_Up;
			__nodeName = name;
			__nodeDispName = name;
		}
		__nodeType = type;
		__imageWidth = width;
		__imageHeight = height;
	}

	/**
	 * コンストラクタ
	 *
	 * @param 	name	ノード名
	 * @param	image	ノード図形のイメージアイコン
 	 * @since   UDC1.2
	 */
	public JTopologyNode(String name, ImageIcon image)
	{
		super();
		if (name != null) {
			__nameDisplay = DispType_Up;
			__nodeName = name;
			__nodeDispName = name;
		}
		setImage(image);
	}

	/**
	 * コンストラクタ
	 *
	 * @param 	name		ノード名
	 * @param	imagefile	ノード図形のイメージファイル
 	 * @since   UDC1.2
	 */
	public JTopologyNode(String name, String imagefile)
	{
		super();
		if (name != null) {
			__nameDisplay = DispType_Up;
			__nodeName = name;
			__nodeDispName = name;
		}
		setImage(imagefile);
	}

	/**
	 * コンストラクタ
	 *
	 * @param 	name	ノード名
	 * @param	component	ノード図形のJComponent
 	 * @since   UDC1.2
	 */
	public JTopologyNode(String name, JComponent component)
	{
		super();
		if (name != null) {
			__nameDisplay = DispType_Up;
			__nodeName = name;
			__nodeDispName = name;
		}
		setComponent(component);
	}

	/**
	 * ノード名を取得する
	 *
	 * @return	ノード名
 	 * @since   UDC1.2
	 */
	public String 	getNodeName() { return __nodeName; }

	/**
	 * ノード名を設定する
	 *
	 * @param	name	ノード名
 	 * @since   UDC1.2
	 */
	public void 	setNodeName(String name) { __nodeName = new String(name); }

	/**
	 * ノード表示名を取得する
	 *
	 * @return	ノード名
 	 * @since   UDC1.2
	 */
	public String 	getNodeDispName() { return __nodeDispName; }

	/**
	 * ノード表示名を設定する
	 *
	 * @param	name	ノード名
 	 * @since   UDC1.2
	 */
	public void 	setNodeDispName(String name)
	{
		dispNameRect = null;
		__nodeDispName = new String(name);
	}

	/**
	 * ノード図形でイメージを使用している場合のイメージ図形を取得する
	 *
	 * @return	ノード図形のイメージアイコン
	 * @since   UDC1.2
	 */
	public ImageIcon	getImage() { return __nodeImage; }

	/**
	 * ノード図形でイメージを使用するためにイメージ図形を設定する
	 *
	 * @param	image	ノード図形のイメージアイコン
	 * @since   UDC1.2
	 */
	public void setImage(ImageIcon image)
	{
		__nodeImage = image;
		__imageWidth = image.getImage().getWidth(this);
		__imageHeight = image.getImage().getHeight(this);
		__nodeType = Type_Image;
	}

	/**
	 * ノード図形でイメージを使用するためにイメージ図形を設定する
	 *
	 * @param	imagefile	ノード図形のイメージファイル
	 * @since   UDC1.2
	 */
	public void setImage(String imagefile)
	{
		//__nodeImage = new ImageIcon(getClass().getResource(imagefile));
		__nodeImage = new ImageIcon(imagefile);
		__imageWidth = __nodeImage.getImage().getWidth(this);
		__imageHeight = __nodeImage.getImage().getHeight(this);
		__nodeType = Type_Image;
	}

	/**
	 * ノード図形でComponentを使用するためにComponentを設定する
	 *
	 * @param	component	ノード図形のComponent
	 * @since   UDC1.2
	 */
	public void setComponent(JComponent component)
	{
		__nodeComponent = component;
		add(__nodeComponent);
		Dimension dm = component.getSize(new Dimension());
		__imageWidth = (int)dm.getWidth();
		__imageHeight = (int)dm.getHeight();
		__nodeType = Type_JComponent;
	}

	/**
	 * ノード名を表示するか否かを取得する
	 *
	 * @return	ノード名を表示する位置種別
	 * @since   UDC1.2
	 */
	public int 	getNameDisplay() { return __nameDisplay; }

	/**
	 * ノード名を表示するか否かを設定する
	 *
	 * @param	display	ノード名を表示する位置種別
	 * @since   UDC1.2
	 */
	public void	setNameDisplay(int display) { __nameDisplay = display; }

	/**
	 * ノードの表示種別を取得する
	 *
	 * @return ノードの表示種別
 	 * @since   UDC1.21
	 */
	public int	getNodeType() { return __nodeType; }

	/**
	 * ノードの表示種別を設定する
	 *
	 * @param	type ノードの表示種別
 	 * @since   UDC1.21
	 */
	public void	setNodeType(int type) { __nodeType = type; }

	/**
	 * ノード図形のWidthを取得する
	 *
	 * @return ノード図形のWidth
 	 * @since   UDC1.21
	 */
	public int	getImageWidth() { return __imageWidth; }

	/**
	 * ノード図形のWidthを設定する
	 *
	 * @param	width ノード図形のWidth
 	 * @since   UDC1.21
	 */
	public void	setImageWidth(int width) { __imageWidth = width; }

	/**
	 * ノード図形のHeightを取得する 
	 *
	 * @return ノード図形のHeight
 	 * @since   UDC1.21
	 */
	public int	getImageHeight() { return __imageHeight; }

	/**
	 * ノード図形のHeightを設定する 
	 *
	 * @param 	height ノード図形のHeight
 	 * @since   UDC1.21
	 */
	public void	setImageHeight(int height) { __imageHeight = height; }

	/**
	 * 関連ノードリストを取得する
	 *
	 * @return 関連ノードリスト
 	 * @since   UDC1.21
	 */
	public ArrayList	getRelationNodeList() { return __relationNodeList; }

	/**
	 * 関連ノードリストに追加する。
	 *
	 * @return 関連ノード(null時は既に追加済)
 	 * @since   UDC1.21
	 */
	public JTopologyNode addRelationNodeList(JTopologyNode node)
	{
		if (__relationNodeList.contains(node)) { return null; }
		__relationNodeList.add(node);
		node.setRelationParentNode(this);
		return node;
	}

	/**
	 * 関連親ノードを取得する。
	 *
	 * @return 関連親ノード
 	 * @since   UDC1.2
	 */
	public JTopologyNode getRelationParentNode() { return __relationParent;	}

	/**
	 * 関連親ノードを設定する。<br>
	 * 関連親ノードを設定した場合、親ノードの移動やパネル拡大時には、親との表示位置の関係を保って表示されます。<br>
	 * 関連親ノードとして設定した親ノードが、JTopologyPanelに登録されていない場合、子ノードはパネル拡大等では拡大／縮小前の位置を保ってしまいます。
	 *
	 * @param	node  関連親ノード
 	 * @since   UDC1.2
	 */
	public void setRelationParentNode(JTopologyNode node) { __relationParent = node; }

	/**
	 * 関連ノードリストから削除する
	 *
	 * @return 関連ノード(null時は既に削除済)
 	 * @since   UDC1.21
	 */
	public JTopologyNode delRelationNodeList(JTopologyNode node)
	{
		if (!__relationNodeList.contains(node)) { return null; }
		__relationNodeList.remove(node);
		return node;
	}

	/**
	 * ノード位置をTopologyPanelのサイズ変更に併せて移動するか否かを取得する
	 *
	 * @return ノード位置をTopologyPanelのサイズ変更に併せて移動するか否か
 	 * @since   UDC1.23
	 */
	public boolean getMoveForParChange() { return __moveForParChange; }

	/**
	 * ノード位置をTopologyPanelのサイズ変更に併せて移動するか否かを設定する
	 *
	 * @param move ノード位置をTopologyPanelのサイズ変更に併せて移動するか否か
 	 * @since   UDC1.23
	 */
	public void getMoveForParChange(boolean move) { __moveForParChange = move; }

	/**
	 * ノードに対するユーザ指定のMouseListenerを取得する
	 *
	 * @return ノードに対するユーザ指定のMouseListener
 	 * @since   UDC1.21
	 */
	public MouseListener	getUserMouseListener() { return	__userMouseListener; }

	/**
	 * ノードに対するユーザ指定のMouseListenerを設定する
	 *
	 * @param ml	ノードに対するユーザ指定のMouseListener
 	 * @since   UDC1.21
	 */
	public void	setUserMouseListener(MouseListener ml) { __userMouseListener = ml; }

	/**
	/**
	 * ユーザオブジェクトを取得する
	 * 
	 * @return ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	public Object getUserObject() { return __userObject; }

	/**
	 * ユーザオブジェクトを設定する
	 * 
	 * @param obj	ユーザオブジェクト
 	 * @since   UDC1.21
	 */
	public void setUserObject(Object obj) { __userObject = obj; }

	/**
	 * 本コンポーネントの初期サイズを設定する。Graphicsなしでのサイズ設定であるため、仮演算してサイズを算出する。
	 *
	 * @since   UDC1.2
	 */
	protected void setSize()
	{
		int width = __imageWidth, height = __imageHeight;
		if (__nameDisplay != DispType_No) {
			int fontSize = getFont().getSize();
			int strlen = getFont().getSize() * ((__nodeDispName.getBytes().length/2) + 2);
			if (strlen > __imageWidth) {
				width = strlen;
			}
			if (__nameDisplay != DispType_Middle) {
				height += fontSize + 2;
			}
		}
		setSize(width + 1, height + 1);
	}

	/**
	 * 本コンポーネントの中心のX座標を取得する
	 *
	 * @param	g	表示していGraphics
	 * @return	本コンポーネントの中心のX座標
	 * @since   UDC1.2
	 */
	public double getCenterX(Graphics2D g)
	{	
		if (__nameDisplay == DispType_No) {
			return (getX() + (__imageWidth / 2));
		}
		Rectangle2D rt = getNameDispLength(g);
		if (rt == null) {
			return (getX() + (__imageWidth / 2));
		}
		double strlen = rt.getWidth();
		if (strlen > __imageWidth) {
			return (getX() + (strlen / 2));
		}
		return (getX() + (__imageWidth / 2));
	}

	/**
	 * 本コンポーネントの中心のY座標を取得する
	 *
	 * @param	g	表示していGraphics
	 * @return	本コンポーネントの中心のY座標
	 * @since   UDC1.2
	 */
	public double getCenterY(Graphics2D g)
	{
		if (__nameDisplay != DispType_Up && __nameDisplay != DispType_Down) {
			return (getY() + (__imageHeight / 2));
		}
		Rectangle2D rt = getNameDispLength(g);
		if (rt == null) {
			return (getY() + (__imageHeight / 2));
		}
		double strheight = rt.getHeight();		
		// DispType_Up
		if (__nameDisplay == DispType_Up) {
			return (getY() + strheight + 2 + (__imageHeight / 2));
		}
		// DispType_Down
		return (getY() + (__imageHeight / 2));
	}

	/**
	 * 本コンポーネントのImageのX座標を取得する
	 *
	 * @param	g	表示していGraphics
	 * @return	本コンポーネントのImageのX座標
	 * @since   UDC1.2
	 */
	public int getImageX(Graphics2D g)
	{
		int imagex = 0;
		if (__nameDisplay != DispType_No) {
			Rectangle2D rt = getNameDispLength(g);
			int strlen = (int)rt.getWidth();
			if (strlen > __imageWidth) {
				imagex = (strlen - __imageWidth) / 2;
			}
		}
		return imagex;
	}

	/**
	 * 本コンポーネントのImageのY座標を取得する
	 *
	 * @param	g	表示していGraphics
	 * @return	本コンポーネントのImageのY座標
	 * @since   UDC1.2
	 */
	public int getImageY(Graphics2D g)
	{
		int imagey = 0;
		if (__nameDisplay != DispType_No) {
			Rectangle2D rt = getNameDispLength(g);
			int strheight = (int)rt.getHeight();
			if (__nameDisplay == DispType_Up) {
				imagey = strheight + 2;
			}
		}
		return imagey;
	}

	/**
	 * paintComponentメンバ関数のオーバライド。本メンバ関数によって画面上の表示処理が実現されます。
	 * 表示内容は、ノード名、ノード図形の二つの情報を縦方向に配置します。
	 *
	 * @param	gr	グラフィックス
	 * @since   UDC1.2
	 */
	protected void paintComponent(Graphics gr)
	{
		Graphics2D g = (Graphics2D)gr;
		super.paintComponent(g);

		Container tp = getParent();
		if (__parSize == null) { __parSize = tp.getSize(new Dimension()); }
		int width = __imageWidth, height = __imageHeight, imagex = 0, imagey = 0;
		int strx = 0, stry = 0;

		/* size/position check */
		if (__nameDisplay != DispType_No) {
			Rectangle2D rt = getNameDispLength(g);
			int strlen = (int)rt.getWidth();
			int strheight = (int)rt.getHeight();
			if (strlen > __imageWidth) {
				strx = 0;
				width = strlen;
				imagex = (strlen - __imageWidth) / 2;
			} else {
				strx = (__imageWidth-strlen) / 2;
				//imagex = 0;
			}
			if (__nameDisplay == DispType_Up) {
				stry = strheight;
				imagey = strheight + 2;
				height += strheight + 2;
			} else if (__nameDisplay == DispType_Down) {
				stry = __imageHeight + strheight;
				//imagey = 0;
				height += strheight + 2;
			} else if (__nameDisplay == DispType_Middle) {
				stry = (__imageHeight/2) + strheight/2;
				//imagey = 0;
			}
		}

		setSize(width + 1, height + 1);
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fill3DRect(0, 0, getWidth(), getHeight(), true);
		}

		/* for zoom */
		if (__moveForParChange && __relationParent == null && (tp.getWidth() != __parSize.width || tp.getHeight() != __parSize.height)) {
			Dimension psz = tp.getSize(new Dimension());
			Point pt = getLocation();
			double x = pt.getX();
			double y = pt.getY();
			if (tp.getWidth() != __parSize.width) {
				double h = psz.getWidth() / __parSize.getWidth();
				x *= h;
				if (psz.getWidth() > __parSize.getWidth())	{ x += (((width+1) * h) - (width+1)) / 2; }
				else 										{ x -= ((width+1) - ((width+1) * h)) / 2; }
			}
			if (tp.getHeight() != __parSize.height) {
				double h = psz.getHeight() / __parSize.getHeight();
				y *= h;
				if (psz.getHeight() > __parSize.getHeight()){ y += (((height+1) * h) - (height+1)) / 2; }
				else 										{ y -= ((height+1) - ((height+1) * h)) / 2; }
			}
			double difx = x - pt.getX();
			double dify = y - pt.getY();
			pt.setLocation(x,y);
			setLocation(pt);
			__parSize = psz;
			JTopologyNode child;
			Point cp;
			int nsize = __relationNodeList.size();
			for (int j=0; j<nsize; j++) {
				child = (JTopologyNode)__relationNodeList.get(j);
				if (child.__moveForParChange) {
					cp = child.getLocation();	
					cp.setLocation(cp.getX()+difx, cp.getY()+dify);
					child.setLocation(cp);
					child.repaint();
				}
			}
		}

		/* draw image */
		Color fgcolor = getForeground();
		Color bgcolor = getBackground();
		if (__nodeType == Type_Image) {
			g.drawImage(__nodeImage.getImage(), imagex, imagey, __imageWidth, __imageHeight, this);
		} else if (__nodeType == Type_JComponent) {
			__nodeComponent.setLocation(imagex, imagey);
		} else if (__nodeType == Type_Rectangle) {
			g.setColor(bgcolor);
			g.fill3DRect(imagex, imagey, __imageWidth, __imageHeight, true);
		} else if (__nodeType == Type_Arc) {
			g.setColor(bgcolor);
			g.fillArc(imagex, imagey, __imageWidth, __imageHeight, 0, 360);
			g.setColor(bgcolor.brighter());
			g.drawArc(imagex, imagey, __imageWidth-1, __imageHeight-1, 60, 180);
			g.setColor(bgcolor.darker());
			g.drawArc(imagex, imagey, __imageWidth-1, __imageHeight-1, 240, 180);
		}

		/* draw name string */
		g.setColor(fgcolor);
		if (__nameDisplay != DispType_No) { g.drawString(__nodeDispName, strx, stry); }
	}

	/**
	 * setLocationのオーバーライド関数。<br>
	 * 関連親ノード／子ノードの設定を行っている場合、子ノードは、親ノードとの位置関係を保って移動します。<br>
	 * これに伴い、親ノードが移動した時の移動サイズを保存しておくためにおーバライドしています。
	 *
	 * @param	x	表示位置X座標 
	 * @param	y	表示位置Y座標 
	 * @since   UDC1.2
	 */
	public void setLocation(int x, int y)
	{
		Point p = getLocation();			
		__oldx = p.getX();
		__oldy = p.getY();
		super.setLocation(x,y);
	}

	/**
	 * setLocationのオーバーライド関数。<br>
	 * 関連親ノード／子ノードの設定を行っている場合、子ノードは、親ノードとの位置関係を保って移動します。<br>
	 * これに伴い、親ノードが移動した時の移動サイズを保存しておくためにおーバライドしています。
	 *
	 * @param	np	表示位置
	 * @since   UDC1.2
	 */
	public void setLocation(Point np)
	{
		Point p = getLocation();			
		__oldx = p.getX();
		__oldy = p.getY();
		super.setLocation(np);
	}

	/**
	 * ノード表示名を表示する場合の表示サイズを取得する。<br>
	 * 表示サイズは、Graphicsに依存するため、入力されたGraphicsにマッチしたサイズを取得する。
	 *
	 * @param	g	グラフィックス
	 * @return 	ノード表示名文字列のサイズ
	 * @since   UDC1.2
	 */
	Rectangle2D getNameDispLength(Graphics2D g)
	{
		if (dispNameRect == null) {
			if (__nodeDispName != null) {
				char[] chars = __nodeDispName.toCharArray();
				dispNameRect = getFont().getStringBounds(chars, 0, chars.length, g.getFontRenderContext());
			}
		}
		return dispNameRect;
	}
}

