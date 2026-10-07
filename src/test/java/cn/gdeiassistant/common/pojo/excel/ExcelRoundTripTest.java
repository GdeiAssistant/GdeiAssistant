package cn.gdeiassistant.common.pojo.excel;

import cn.gdeiassistant.common.annotation.ExcelField;
import org.junit.jupiter.api.Test;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Synthetic documents exercise the public import/export contract, with no user data. */
class ExcelRoundTripTest {
    public static class Record {
        @ExcelField(title="Label**Synthetic label",sort=1,groups={1}) private String label;
        @ExcelField(title="Count",sort=2,groups={1}) private Integer count;
        private Double amount;
        public Record() {}
        Record(String label,int count,double amount){this.label=label;this.count=count;this.amount=amount;}
        public String getLabel(){return label;}public void setLabel(String label){this.label=label;}
        public Integer getCount(){return count;}public void setCount(Integer count){this.count=count;}
        @ExcelField(title="Amount",sort=3,groups={1}) public Double getAmount(){return amount;}
        public void setAmount(Double amount){this.amount=amount;}
    }
    private byte[] export(String title,int type,int... groups) throws Exception {
        var exporter=new ExportExcel(title,Record.class,type,groups);
        try(var out=new ByteArrayOutputStream()){
            exporter.setDataList(List.of(new Record("synthetic-one",1,1.25),new Record("synthetic-two",2,2.5),new Record("synthetic-last",3,3.75))).write(out);
            return out.toByteArray();
        }finally{exporter.dispose();}
    }
    @Test void annotationExportAndImportKeepAllRowsWithAndWithoutTitle() throws Exception {
        for(String title:List.of("","Synthetic report")){
            byte[] data=export(title,1);int header=title.isEmpty()?0:1;
            var importer=new ImportExcel(new MockMultipartFile("file","synthetic.xlsx","application/octet-stream",data),header,0);
            var rows=importer.getDataList(Record.class);
            assertEquals(3,rows.size());assertEquals("synthetic-last",rows.get(2).getLabel());assertEquals(3,rows.get(2).getCount());assertEquals(3.75,rows.get(2).getAmount());
            assertEquals(header+1,importer.getDataRowNum());assertEquals(3,importer.getLastCellNum());
        }
        var grouped=new ImportExcel("synthetic.xlsx",new ByteArrayInputStream(export("Synthetic report",2,99,1)),1,0).getDataList(Record.class,99,1);
        assertEquals(3,grouped.size());assertEquals(1,grouped.get(0).getCount());
    }
    @Test void numericDateFormulaBooleanAndBlankCellsRetainTheirMeaning() throws Exception {
        for(boolean legacy:List.of(false,true)){
            byte[] bytes;
            try(Workbook book=legacy?new HSSFWorkbook():new XSSFWorkbook();var out=new ByteArrayOutputStream()){
                Sheet sheet=book.createSheet();Row header=sheet.createRow(0);header.createCell(0).setCellValue("Synthetic");Row row=sheet.createRow(1);
                row.createCell(0).setCellValue(123456.0);row.createCell(1).setCellValue(true);row.createCell(2).setCellFormula("1+2");row.createCell(3).setCellValue("text");
                var date=row.createCell(4);date.setCellValue(new GregorianCalendar(2026,0,2).getTime());var style=book.createCellStyle();style.setDataFormat(book.createDataFormat().getFormat("yyyy-MM-dd"));date.setCellStyle(style);
                row.createCell(5).setCellErrorValue(FormulaError.NA.getCode());book.getCreationHelper().createFormulaEvaluator().evaluateAll();book.write(out);bytes=out.toByteArray();
            }
            var reader=new ImportExcel(legacy?"synthetic.xls":"synthetic.xlsx",new ByteArrayInputStream(bytes),0,0);var row=reader.getRow(1);
            assertEquals("123456",reader.getCellValue(row,0));assertEquals(true,reader.getCellValue(row,1));assertEquals("3.0",reader.getCellValue(row,2));assertEquals("text",reader.getCellValue(row,3));assertEquals("2026-01-02",reader.getCellValue(row,4));assertEquals(FormulaError.NA.getCode(),reader.getCellValue(row,5));assertEquals("",reader.getCellValue(row,6));assertEquals("",reader.getCellValue(null,0));
        }
    }
    @Test void exportResponsePreservesContentAndRejectsUnsupportedImportTypes() throws Exception {
        var exporter=new ExportExcel("Synthetic report",new String[]{"Value**Notes","Type"});
        try{
            var row=exporter.addRow();List<Object> values=Arrays.asList("synthetic",1,2L,3.5,4.5f,new GregorianCalendar(2026,0,2).getTime(),null,new java.math.BigDecimal("5.25"));
            for(int i=0;i<values.size();i++)exporter.addCell(row,i,values.get(i),i%4,Class.class);
            var response=new MockHttpServletResponse();exporter.write(response,"合成.xlsx");assertTrue(response.getHeader("Content-Disposition").contains("%"));
            try(var book=new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))){assertEquals("synthetic",book.getSheetAt(0).getRow(2).getCell(0).getStringCellValue());assertEquals(3.5,book.getSheetAt(0).getRow(2).getCell(3).getNumericCellValue());assertEquals("5.25",book.getSheetAt(0).getRow(2).getCell(7).getStringCellValue());}
        }finally{exporter.dispose();}
        assertThrows(RuntimeException.class,()->new ImportExcel("",InputStream.nullInputStream(),0,0));assertThrows(RuntimeException.class,()->new ImportExcel("synthetic.txt",InputStream.nullInputStream(),0,0));
    }
}
