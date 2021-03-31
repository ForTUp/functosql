package com.xnp.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;





public class Utils {

	/**
	  *  创建文件
	 * @param path
	 * @throws IOException
	 */
	public static void createFile(String path) throws IOException {
	    if (path!=null && path!="") {
	        File file = new File(path);
	        if (!file.getParentFile().exists()) {
	            file.getParentFile().mkdirs();
	        }
	        file.createNewFile();
	    }

	}
	
	/**
	 * 删除文件
	 * @param list
	 * @throws IOException
	 */
	public static void deleteFile(List<String > list) throws IOException  {
		for (String string : list) {
			if (string!=null && string!="") {
		        File file = new File(string);
		        if (file.exists() && !file.isDirectory()) {
		            file.delete();
		        }
		    }
		}
	}
	
	/**
	 * 删除目前及下面文件
	 * @param file
	 * @throws IOException
	 */
	public static void deleteFile(File file) throws IOException{
		if (file == null || !file.exists() || !file.isDirectory()){
		   throw new IOException("文件不存在或者不是目录！");
		}
		//取得这个目录下的所有子文件对象
		File[] files = file.listFiles();
		//遍历该目录下的文件对象
		for (File f: files){
		    //判断子目录是否存在子目录,如果是文件则删除
		    if (f.isDirectory()){
		        deleteFile(f);
		    }else {
		        f.delete();
		    }
		}
		//删除空文件夹  for循环已经把上一层节点的目录清空。
	    file.delete();
	}
	
	/**
	 * 合并文件
	 * @throws Exception
	 */
	public static void fileMerge(List<String > list,String outPath) throws Exception{
		//合并文件
		File tmpFile = new File(outPath);
		Utils.createFile(outPath);
		@SuppressWarnings("resource")
//		FileWriter writer= new FileWriter(tmpFile, false);
		BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(new FileOutputStream (tmpFile,false),"UTF-8"));
		for (int i = 0; i < list.size(); i++) {
			File file = new File(list.get(i));
			if (!file.exists() || file.isDirectory()) {
				System.out.println("合并文件异常!");
				return;
			}
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			BufferedReader br  = new BufferedReader(new InputStreamReader(is, "utf-8"));
			//读写文件行
			String tmp;
			while((tmp=br.readLine())!=null){
				writer.write(tmp+"\n");
			}
			
			br.close();
			is.close();
		}
		writer.flush();
		writer.close();
	}
	
	
	/**
	 * 获取字符串编码
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GB2312
				String s = encode;
				return s; //是的话，返回“GB2312“，以下代码同理
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是ISO-8859-1
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是UTF-8
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GBK
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
			
		}
		return "";
	}
	
	/**
	 * 转成utf8
	 * @param str
	 * @return
	 */
	public static String toChinese(String strvalue, String encodeFormat) {		
		String result = null;
		try {			
			if (strvalue == null) {				
				return "";			
			} else {				
				result = new String(strvalue.getBytes("GBK"), "UTF-8");					
				return result;			
			}		
		} catch (Exception e) {			
			return "";		
		}	
	}
	
}
