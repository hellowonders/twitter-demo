package com.sas.demo.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerateExcelFile {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GenerateExcelFile.class);
	private static final String FILE_PATH = "C:/Users/Prateek/Desktop/";
	private static XSSFWorkbook workbook;
	private static XSSFSheet sheet;
	private static int rowNum;

	public static void initialize(String sheetName) {
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet(sheetName);
		rowNum = 0;
	}

	public static void insertRow(String data) {
		String[] splitCol = data.split("~");
		Row row = sheet.createRow(rowNum++);
		int colNum = 0;
		for (String field : splitCol) {
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(field);
		}
	}

	public static void insertData(int rowNumber,int columnNumber, String cellVal) {
		Row row = sheet.getRow(rowNumber);
		if (row == null)
			row = sheet.createRow(rowNumber);
		Cell cell = row.createCell(columnNumber);
		cell.setCellValue(cellVal);
	}
	
	public static void writeFile(String fileName) {
		try {
			LOG.info("Writing to excel sheet");
			FileOutputStream outputStream = new FileOutputStream(FILE_PATH + fileName);
			workbook.write(outputStream);
		} catch (FileNotFoundException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}
}