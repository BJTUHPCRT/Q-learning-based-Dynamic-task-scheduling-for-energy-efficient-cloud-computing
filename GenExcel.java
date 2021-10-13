package org.cloudbus.cloudsim.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenExcel {
	private XSSFSheet sheet;
	private XSSFWorkbook hwb;
	private XSSFRow row;

	private File path_file = new File("cloudsim-3.0s");
	private String path = path_file.getAbsolutePath();
	
	private static String New_List_File="Q.xlsx";	
	private static GenExcel instance = new GenExcel();
	Map<String, Map<Integer, Double>> QList= new HashMap<String, Map<Integer, Double>>();
	public static GenExcel getInstance() {
		if (instance == null)
			instance = new GenExcel();
		return instance;
	}
	
	public   Map<String, Map<Integer, Double>> init()
	{
		
		 try {
			File file = new File(path + "Q.xlsx");
			  InputStream inputStream = new FileInputStream(file); 
			  String fileName = file.getName(); 
			  Workbook wb = null; 
	
			 hwb = new XSSFWorkbook(inputStream);
			//excel sheet
			 sheet = hwb.getSheetAt(0);
   
			  int firstRowIndex = sheet.getFirstRowNum();
			  int lastRowIndex = sheet.getLastRowNum(); 
			  for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex ++){ 
			      Row row = sheet.getRow(rIndex); 
			      if(row != null){ 
			          int firstCellIndex = row.getFirstCellNum(); 
			          int lastCellIndex = row.getLastCellNum(); 
			          
			          Cell cells=row.getCell(firstCellIndex);
			          String values="";
			           if(cells!=null){
			        	  values=cells.toString();
			        	  QList.put(values, new HashMap<Integer, Double>());
			        	
			            }else{
			            	//sheet.shiftRows(rIndex, sheet.getLastRowNum()+1,-1);
			        	 QList.put("", new HashMap<Integer, Double>());
			            }
			          			     
			        	System.out.println(values+"state");	  
			         
			          for(int cIndex = firstCellIndex+1; cIndex < 400; cIndex ++){ 
			              Cell cell = row.getCell(cIndex); 
			              String value = ""; 
			                if(cell != null){ 
			                  value = cell.toString(); 
			                  QList.get(values).put(cIndex-1, Double.parseDouble(value));			                  
			                  System.out.print(value+"\t"+"action"); 
			              } else{
			            	  //sheet.shiftRows(rIndex+1, sheet.getLastRowNum()+1,-1);
			            	  QList.get(values).put(cIndex-1, 0.0);
			                }
			          } 
			          System.out.println(); 
		   	      } else{
			      }
			  }
			  
		} catch (FileNotFoundException e) {
			 hwb = new XSSFWorkbook();
			//excel sheet
			 sheet = hwb.createSheet();
			
		} catch(IOException e){
			e.printStackTrace();
		}
		return QList;
	}

	public void fillData(Map<String, Map<Integer, Double>> QList, String state_idx, int action_idx, double QValue)
	{
		File file = new File(path + "Q.xlsx");			
		int rownumber=sheet.getLastRowNum();
		 	
		 for (int i = 1; i <= rownumber; i++) {	               
			 Row row = sheet.getRow(i);
			 if(row!=null){
			 sheet.removeRow(row);
			 }
           }
		 		
		 int rows=0;
		 int s=0;
		 
		 for(Map.Entry <String, Map<Integer, Double>> me : QList.entrySet()) 
		 {
			 Row row = sheet.createRow(rows);
			 row.createCell(0).setCellValue(me.getKey());
			//record
			 for (int k = 0; k <= 40; k++) {
				 if(me.getValue().containsKey(k)){
					 row.createCell(k+1).setCellValue(me.getValue().get(k));
				 }
			 }	
			 rows ++;
		 }	 			
	}
	
	public void genExcel()
	{
		
		try {
			File file = new File(path + "Q.xlsx");
			hwb.write(new FileOutputStream(file,false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
