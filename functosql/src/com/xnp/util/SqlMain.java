package com.xnp.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author xnp
 *
 */
public class SqlMain {

	public final static String vs_dynstr1 = "vs_dynstr1";
	
	/**
	 * 选择选项
	 * @param sc
	 */
	public static void out(Scanner sc) {
		System.out.println("请选择操作：\n"
				+ "1、执行sql生成 \n"
				+ "2、退出 \n");
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		boolean quit = true;
		out(sc);
		while (quit) {
			int type = 0;
			try {
				type = sc.nextInt();
			} catch (Exception e) {
				System.err.println("请输入数字！");
				out(sc);
				sc.nextLine();
				continue;
			}
			if (type==1) {
				String result = sqlout(sc);
				System.out.println(result);
				out(sc);
			}else if (type==2) {
				System.out.println("已退出！");
				sc.close();
				return;
			}else {
				System.out.println("已退出！");
				sc.close();
				return;
			}
		}
	}
	
	/**
	 * 返回结果
	 * @param sc
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String sqlout(Scanner sc) {
		sc.nextLine();
		System.out.print("请您输入文件：");
		String inPath = sc.nextLine().trim();
		//输入文件名
		//输出文件路径
		//获取文件
		File file = new File(inPath);
		if (!file.exists()) {
			return "输入文件有误！";
		}
		
		//将文件放入缓存区
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return "文件找不到！";
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			return e.getMessage();
		}
		
		//获取所有中间表和结果表
		List<String> tableList = new ArrayList<String>();
		//获取所有insert语句
		List<String> insertList = new ArrayList<String>();
		//逐行读取
		String tmp;
		//插入语句标记
		boolean insertFlag = false;
		//单个插入语句
		String insertStr = "";
		//table名
		String tableName = "";
		//结果表
		String outTable = "";
		try {
			while((tmp=br.readLine())!=null){
				//全取小写
				tmp = tmp.toLowerCase();
				
				if (tmp.contains("vs_table_name") && tmp.contains(":=")
						&& tmp.indexOf("vs_table_name")< tmp.indexOf(":=")) {
					outTable = tmp.substring(tmp.indexOf("'")+1,tmp.lastIndexOf("'")).trim();
					//C:\Users\xnp\Desktop\sqlout\dw2.dw_time_limit_source_ms\p_dw_time_limit_source_ms.sql
				}
				
				if (tmp.contains(vs_dynstr1) && tmp.contains("insert")) {
					insertFlag = true;
				}
				if (insertFlag) {
					if (tmp.contains("insert")) {
						insertStr = insertStr + tmp.substring(tmp.indexOf("insert")-1).replaceAll("\t", "")  + "\n";
					}else {
						insertStr = insertStr + tmp.replaceAll("\t", "") + "\n";
					}
					//将每个插入语句都放入list
					if (tmp.contains("insert")) {
						if (tmp.contains("select")) {
							tableName = tmp.substring(tmp.indexOf("into")+5,tmp.indexOf("select")).trim();
						}else {
							tableName = tmp.substring(tmp.indexOf("into")+5).trim();
						}
					}
				}
				if (tmp.contains(";")) {
					insertFlag = false;
					if (!tableList.contains(tableName)) {
						tableList.add(tableName);
					}
					if (insertStr!=null && insertStr!="") {
						insertList.add(insertStr+"\n");
					}
					insertStr = "";
				}
			}
		}catch (Exception e) {
			return e.getMessage();
		}
		
		try {
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//文件输出
		String path="";
		try {
			path=outSql(tableList,insertList,file.getParent()+file.separator+outTable+".sql");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "success!输出sql："+path;
	}
	
	
	public static String outSql(List<String> tableList,List<String> insertList,String path) throws IOException {
		//中间主体sql文件
		File tmpFile = new File(path);
		Utils.createFile(path);
		
		BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(new FileOutputStream (tmpFile,false),"UTF-8"));
		//插入语句
		String insertAll = "";
		//建表语句
		String createAll ="";
		//drop语句
		String dropAll = "";
		//truncate语句
		String truncateAll = "";
		//合并sql
		for (int i = 0; i < insertList.size(); i++) {
			String insert = insertList.get(i);
//			System.err.println(insert);
			insertAll = insertAll +"--#############################################\n" + insert;
		}
		insertAll = insertAll.replaceAll("''", "'").replaceAll("';", ";");
		//替换sql
		for (int i = tableList.size()-1; i >0; i--) {
			String tableName = tableList.get(i);
			String newTableName = "";
			if (tableName.toLowerCase().contains("dw2")) {
				newTableName = tableName.toLowerCase().replace("dw2.dw", "tmp2.tmp");
			}else if (tableName.toLowerCase().contains("report2")) {
				newTableName = tableName.toLowerCase().replace("report2.report", "tmp2.tmp");
			}
			//替换
			insertAll = insertAll.replaceAll(tableName, newTableName);
//			System.err.println(i+"1:"+tableList.get(i));
//			System.err.println(i+"2:"+tableList.get(i).replace("dw2.dw", "tmp2.tmp"));
			//建表
			createAll = createAll + "create table "+newTableName+" (like "+tableName+" including indexes) WITH ( OIDS=FALSE,appendonly=true, compresslevel=5, orientation=column, compresstype=zlib) tablespace tbs_tmp2 ; \n" ;
			//drop语句
			dropAll = dropAll + "drop table "+newTableName + "; \n";
			//truncate语句
			truncateAll = truncateAll + "truncate "+newTableName + "; \n";
		}
		writer.write("--DropAll#############################################\n");
		writer.write(dropAll);
		writer.newLine();
		writer.write("--CreateAll#############################################\n");
		writer.write(createAll);
		writer.newLine();
		writer.write("--TruncateAll#############################################\n");
		writer.write(truncateAll);
		writer.newLine();
		writer.write(insertAll);
		writer.flush();
		writer.close();
		return path;
	}
	
}
