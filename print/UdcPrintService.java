/* *********************************************************************
 * @(#)UdcPrintService.java 1.22, 31 Aug 2006
 *
 * Copyright 2006 U-Software, Inc. All rights reserved.
 * U-Soft PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * ********************************************************************/
package udc.print;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.event.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;


/**
 * プリンタ出力サービスクラス。
 *
 * @author  Takayuki Uchida
 * @version 1.2, 31 Aug 2006
 * @since   UDC1.22
 */
public class UdcPrintService
{
	/**
	 * プリンタ選択ダイアログを表示し、印刷プリンタを選択する。
	 * flavor指定時はServiceUI.printDialogによってプリンタを選択する。省略時はPrinterJob.printDialogによってプリンタを選択する。
	 * これは、ServiceUI.printDialogではprasでMediaPrintableAreaによりMarginを設定することができないことからの対応です。
	 * @param	flavor	印刷形式(省略時null)
	 * @param	pras	印刷属性(省略時null)
	 * @return 	PrintServiceインスタンス
	 * @see		DocFlavor
	 * @see		PrintService
	 */
	static public PrintService showPrinterDialog(DocFlavor flavor, PrintRequestAttributeSet pras)
	{
		if (pras == null) { pras = new HashPrintRequestAttributeSet(); }
		if (flavor != null) {
			PrintService ps[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
			if (ps == null || ps.length <= 0) { return null; }
			PrintService defps = PrintServiceLookup.lookupDefaultPrintService();
			return ServiceUI.printDialog(null, 200, 200, ps, defps, flavor, pras);
		}
		PrinterJob pj = PrinterJob.getPrinterJob();
		if (pj.printDialog(pras)) {
			return pj.getPrintService();
		}
		return null;
	}

	/**
	 * プリンタ出力(ファイル印刷)。<br>
	 * psで指定するPrintServiceとfilenameで指定した印刷形式は、DocFlavorが一致していなければなりません。 
	 * @param	ps 	PrintServiceインスタンス
	 * @param	flavor	印刷形式
	 * @param	pras	印刷属性
	 * @param	filename 	印刷対象ファイル名
	 * @param	listener 	PrintJobListener
	 * @return 	0:正常/非0:異常
	 */
	static public int printoutFile(PrintService ps, DocFlavor flavor, PrintRequestAttributeSet pras, String filename, PrintJobListener listener)
	{
		try {
			DocPrintJob job = ps.createPrintJob();
			FileInputStream fis = new FileInputStream(filename);
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(fis, flavor, das);
			if (listener != null) {
				job.addPrintJobListener(listener);
			}
			job.print(doc, pras);
		} catch(Exception exp) {
			return -1;
		}
		return 0;
	}

	/**
	 * プリンタ出力(バイト列)。<br>
	 * psで指定するPrintServiceとbytearrayで指定した印刷形式は、DocFlavorが一致していなければなりません。 
	 * @param	ps 	PrintServiceインスタンス
	 * @param	flavor	印刷形式
	 * @param	pras	印刷属性
	 * @param	byteArray 	印刷対象バイト列
	 * @param	listener 	PrintJobListener
	 * @return 	0:正常/非0:異常
	 */
	static public int printoutBytes(PrintService ps, DocFlavor flavor, PrintRequestAttributeSet pras, byte[] byteArray, PrintJobListener listener)
	{
		try {
			DocPrintJob job = ps.createPrintJob();
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(byteArray, flavor, das);
			if (listener != null) {
				job.addPrintJobListener(listener);
			}
			job.print(doc, pras);
		} catch(Exception exp) {
			return -1;
		}
		return 0;
	}

	/**
	 * プリンタ出力(Printableオブジェクト)。<br>
	 * psで指定するPrintServiceのDocFlavorは、DocFlavor.SERVICE_FORMATTED.PRINTABLEである必要があります。
	 * @param	ps 	PrintServiceインスタンス
	 * @param	pras	印刷属性
	 * @param	obj 	Printableオブジェクト
	 * @param	listener 	PrintJobListener
	 * @return 	0:正常/非0:異常
	 */
	static public int printoutPrintable(PrintService ps, PrintRequestAttributeSet pras, Printable obj, PrintJobListener listener)
	{
		try {
			DocPrintJob job = ps.createPrintJob();
			DocAttributeSet das = new HashDocAttributeSet();
			Doc doc = new SimpleDoc(obj, DocFlavor.SERVICE_FORMATTED.PRINTABLE, das);
			if (listener != null) {
				job.addPrintJobListener(listener);
			}
			job.print(doc, pras);
		} catch(Exception exp) {
			return -1;
		}
		return 0;
	}
}
