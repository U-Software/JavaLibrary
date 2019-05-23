/* *********************************************************************
 * @(#)UdcXmlCoder.java 1.0, 18 Jan 2003
 *
 * Copyright 2003 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.util;

import java.util.*;
import java.lang.*;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.*;
import javax.xml.parsers.*;


/**
 * XMLエンコード・デコードユーティリティ。
 * <br>
 * 本パッケージは java1.4以上でのみ使用可能です。
 *
 * @author  Takayuki Uchida
 * @version 1.0, 18 Jan 2003
 * @since   UDC1.0
 */
public class UdcXmlCoder
{
	public static String 	ShounariStr = "@@lt@@";		// <
	public static String 	DainariStr 	= "@@gt@@";		// >
	public static String 	AndStr 		= "@@amp@@;";	// &


	/**
	 * XML文字列を解析し、UdcBranchElem分岐木情報に変換する。
	 * 本解析でサポートされるのは Node.ELEMENT_NODE のみで、末端のLeafのみ
	 * Node.TEXT_NODE をサポートします。
	 *
	 * @return	自身をルートとしたXML分岐木情報(UdcBranchElem)
	 * @param	xmlstr	XML文字列
	 * @since	UDC1.0
	 */
	public static UdcBranchElem parse(String xmlstr)
	{
		if (xmlstr == null) {
			return null;
		}

		Node root = null;
		try {
			//Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new StringBufferInputStream(xmlstr) );
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream(xmlstr.getBytes()) );
			root = (Node)doc.getDocumentElement();
		} catch (ParserConfigurationException pcexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, PerfConfigurationExeption : " + pcexp);
			root = null;
		} catch (SAXException saxexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, SAXExeption : " + saxexp);
			root = null;
		} catch (IOException ioexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, IOExeption : " + ioexp);
			root = null;
		}
		if (root == null) {
			return null;
		}

		UdcBranchElem branch;
		if ((branch=UdcXmlCoder.parse(root)) == null) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", " - illegal syntax.");
			return null;
		}
		return branch;
	}

	/**
	 * XML文字列を解析し、UdcBranchElem分岐木情報に変換する。
	 * 本解析でサポートされるのは Node.ELEMENT_NODE のみで、末端のLeafのみ
	 * Node.TEXT_NODE をサポートします。
	 *
	 * @return	自身をルートとしたXML分岐木情報(UdcBranchElem)
	 * @param	xmlbytes	XMLバイト列
	 * @since	UDC1.0
	 */
	public static UdcBranchElem parse(byte[] xmlbytes)
	{
		if (xmlbytes == null) {
			return null;
		}

		Node root = null;
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream(xmlbytes) );
			root = (Node)doc.getDocumentElement();
		} catch (ParserConfigurationException pcexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, PerfConfigurationExeption : " + pcexp);
			root = null;
		} catch (SAXException saxexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, SAXExeption : " + saxexp);
			root = null;
		} catch (IOException ioexp) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", "- illegal entry data, IOExeption : " + ioexp);
			root = null;
		}
		if (root == null) {
			return null;
		}

		UdcBranchElem branch;
		if ((branch=UdcXmlCoder.parse(root)) == null) {
			UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", " - illegal syntax.");
			return null;
		}
		return branch;
	}

	/**
	 * XML文字列を解析し、UdcBranchElem分岐木情報に変換する。
	 * 本解析でサポートされるのは Node.ELEMENT_NODE のみで、末端のLeafのみ
	 * Node.TEXT_NODE をサポートします。
	 *
	 * @return	自身をルートとしたXML分岐木情報(UdcBranchElem)
	 * @param	node	XMLノード
	 * @since	UDC1.0
	 */
	public static UdcBranchElem	parse(Node node)
	{
		String nodeName = node.getNodeName();

		Node child = node.getFirstChild();
		if (child == null || (child == node.getLastChild() && child.getNodeType() != Node.ELEMENT_NODE)) {
			return UdcXmlCoder.parseElem(node);
		}

		UdcBranchElem root = new UdcBranchElem(nodeName, null);
		if (node.hasAttributes()) {
			String name, val;
			NamedNodeMap attrmap = node.getAttributes();
			for (int i=0; i<attrmap.getLength(); i++) {
				Node attr = attrmap.item(i);
				if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
					name = attr.getNodeName();
					val = attr.getNodeValue();
					if (root.addAttr(new UdcBranchElem(name,val)) == null) {
						UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parse", " - duplicate attr name, tag[" + nodeName + "] attr[" + name + "]");
					}
				}
			}
		}

		short type;
		UdcBranchElem leaf;
		for (child=node.getFirstChild(); child!=null; child=child.getNextSibling()){
			type = child.getNodeType();
			nodeName = child.getNodeName();
			if (type != Node.ELEMENT_NODE) {
				continue;
			}
			if ((leaf=UdcXmlCoder.parse(child)) != null) {
				root.addBranch(leaf);
			}
		}
		return root;
	}

	/**
	 * XML文字列を解析し、UdcBranchElem分岐木情報に変換する。
	 * 本解析は、末端のLeafノードのみを対象とします。<br>
	 * 以下の文字は自動的に変換されてしまいますので注意して下さい。<br>
	 *	[@@lt@@] ---> [<]<br>
	 *	[@@gt@@] ---> [>]<br>
	 *	[@@amp@@] ---> [&]<br>
	 *
	 * @return	末端のLeafXML分岐木情報(UdcBranchElem)
	 * @param	node	XMLノード
	 * @since	UDC1.0
	 */
	public static UdcBranchElem parseElem(Node node)
	{
		String name, val;
		UdcBranchElem leaf = null;
		Node child = node.getFirstChild();
		if (child != null && child == node.getLastChild()) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				name = node.getNodeName();
				val = child.getNodeValue();
				if (val != null && val.length() > 0) {
					val = val.replaceAll(ShounariStr,"<");
					val = val.replaceAll(DainariStr,">");
					val = val.replaceAll(AndStr,"&");
				}
				leaf = new UdcBranchElem(name,val);
			}
		} else {
			name = node.getNodeName();
			leaf = new UdcBranchElem(name,"");
		}
		if (leaf != null && node.hasAttributes()) {
			NamedNodeMap attrmap = node.getAttributes();
			for (int i=0; i<attrmap.getLength(); i++) {
				Node attr = attrmap.item(i);
				if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
					name = attr.getNodeName();
					val = attr.getNodeValue();
					if (leaf.addAttr(new UdcBranchElem(name,val)) == null) {
						UdcTrace.trace(UdcTrace.Level, "UdcXmlCoder.parseElem", " - duplicate attr name, tag[" + leaf.getTagName() + "] attr[" + name + "]");
					}
				}
			}
		}
		return leaf;
	}


	/**
	 * 自身をルートとしたXML文字列を取得する。
	 * tagName/tagValueを以下のように <tagName>tagValue</tagName> にする。<br>
	 * 以下の文字は自動的に変換されてしまいますので注意して下さい。<br>
	 *	[<] ---> [@@lt@@]<br>
	 *	[>] ---> [@@gt@@]<br>
	 *	[&] ---> [@@amp@@]<br>
	 *
	 * @return	自身をルートとしたXML文字列
	 * @param	node	XML分岐木情報
	 * @since	UDC1.0
	 */
	public static String toXmlString(UdcBranchElem node)
	{
		if (node.getTagName() == null) {
			return null;
		}

		int i;
		UdcBranchElem elm;
		StringBuffer buffer = new StringBuffer();
		buffer.append("<" + node.getTagName());
		LinkedList attrlist = node.getAttrList();
		if (attrlist != null && attrlist.size() > 0) {
			for (i=0; i<attrlist.size(); i++) {
				elm = (UdcBranchElem)attrlist.get(i);
				if (elm.getTagValue() == null) {
					buffer.append(" " + elm.getTagName() + "=\"\"");
				} else {
					buffer.append(" " + elm.getTagName() + "=\"" + elm.getTagValue() + "\"");
				}
			}
		}
		buffer.append(">");

		String val;
		if (node.sizeBranch() > 0) {
			for (i=0; i<node.sizeBranch(); i++) {
				elm = node.get(i);
				if ((val=UdcXmlCoder.toXmlString(elm)) == null) {
					return null;
				}
				buffer.append(val);
			}
		} else {
			if ((val=(String)node.getTagValue()) != null) {
				if (val.length() > 0) {
					val = val.replaceAll("<",ShounariStr);
					val = val.replaceAll(">",DainariStr);
					val = val.replaceAll("&",AndStr);
				}
				buffer.append(val);
			}
		}
		buffer.append("</" + node.getTagName() + ">");
		return buffer.toString();
	}
}

