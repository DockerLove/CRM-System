package com.example.util;

import com.example.model.Client;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    public static boolean exportClientsToExcel(List<Client> clients, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Клиенты");

            // Создаем стиль заголовка
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Стиль для данных
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Заголовки колонок
            String[] columns = {"ID", "Название", "Телефон", "Email", "Адрес", "Дата добавления"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Заполняем данными
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (Client client : clients) {
                Row row = sheet.createRow(rowNum);

                row.createCell(0).setCellValue(client.getId());
                row.createCell(1).setCellValue(client.getName());
                row.createCell(2).setCellValue(client.getPhone() != null ? client.getPhone() : "");
                row.createCell(3).setCellValue(client.getEmail() != null ? client.getEmail() : "");
                row.createCell(4).setCellValue(client.getAddress() != null ? client.getAddress() : "");
                row.createCell(5).setCellValue(client.getCreatedDate() != null ?
                        client.getCreatedDate().format(dateFormatter) : "");

                // Применяем стиль ко всем ячейкам строки
                for (int i = 0; i < columns.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }

                rowNum++;
            }

            // Авто-подбор ширины колонок
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // Ограничиваем максимальную ширину
                int width = sheet.getColumnWidth(i);
                if (width > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            // Сохраняем файл
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDefaultFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "clients_report_" + LocalDateTime.now().format(formatter) + ".xlsx";
    }
}