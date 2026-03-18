package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.dto.ColorPrecioDTO;
import com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExcelExportService {

    private static final int LOGO_ROW_END = 2;
    private static final int HEADER_ROWS  = 5;
    private static final int LAST_COL     = 6;

    private final ClienteProductoService clienteProductoService;
    public ExcelExportService(ClienteProductoService clienteProductoService) {
        this.clienteProductoService = clienteProductoService;
    }

    private byte[] cargarLogo() {
        try (java.io.InputStream is = getClass().getResourceAsStream("/images/logo_ritchi.jpg")) {
            if (is != null) return is.readAllBytes();
        } catch (Exception e) {}
        return null;
    }

    public byte[] exportarClienteProductos(String nombreCliente, Integer idCliente,
                                           List<ProductoAgrupadoDTO> productos,
                                           Date filtroFecha,
                                           String estadoFiltro) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            Font whiteFont = wb.createFont();
            whiteFont.setBold(true);
            whiteFont.setColor(IndexedColors.WHITE.getIndex());

            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font subtitleFont = wb.createFont();
            subtitleFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(whiteFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(headerStyle);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle subtitleStyle = wb.createCellStyle();
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.LEFT);
            subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);
            setBorder(dataStyle);

            CellStyle imageCellStyle = wb.createCellStyle();
            imageCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            imageCellStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(imageCellStyle);

            CellStyle precioStyle = wb.createCellStyle();
            precioStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            precioStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorder(precioStyle);
            org.apache.poi.ss.usermodel.DataFormat df = wb.createDataFormat();
            precioStyle.setDataFormat(df.getFormat("$ #,##0"));

            Sheet sheet = wb.createSheet("Productos");
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            int[] colWidths = {13, 18, 18, 30, 16, 26, 12};
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            for (int r = 0; r <= LOGO_ROW_END; r++) {
                Row row = sheet.getRow(r);
                if (row == null) row = sheet.createRow(r);
                row.setHeightInPoints(40);
                for (int c = 0; c <= 2; c++) {
                    if (row.getCell(c) == null) row.createCell(c);
                }
            }

            byte[] logoBytes = cargarLogo();
            if (logoBytes != null) {
                int logoIdx = wb.addPicture(logoBytes, Workbook.PICTURE_TYPE_JPEG);
                ClientAnchor logoAnchor = wb.getCreationHelper().createClientAnchor();
                logoAnchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                logoAnchor.setCol1(0); logoAnchor.setRow1(0);
                logoAnchor.setCol2(3); logoAnchor.setRow2(LOGO_ROW_END + 1);
                logoAnchor.setDx1(0); logoAnchor.setDy1(0);
                logoAnchor.setDx2(0); logoAnchor.setDy2(0);
                org.apache.poi.ss.usermodel.Picture logoPic = drawing.createPicture(logoAnchor, logoIdx);
                logoPic.resize(1.0);
            }

            Row row0 = sheet.getRow(0);
            Cell cellTitulo = row0.createCell(3);
            cellTitulo.setCellValue("RITCHI");
            cellTitulo.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, LAST_COL));

            Row row1 = sheet.getRow(1);
            if (row1 == null) row1 = sheet.createRow(1);
            Cell cellCliente = row1.createCell(3);
            cellCliente.setCellValue("Cliente: " + nombreCliente);
            cellCliente.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 3, LAST_COL));

            Row row2 = sheet.getRow(2);
            if (row2 == null) row2 = sheet.createRow(2);
            Cell cellFecha = row2.createCell(3);
            StringBuilder infoFecha = new StringBuilder("Fecha de emisión: ").append(sdf.format(new Date()));
            if (filtroFecha != null) {
                infoFecha.append("   |   Fecha filtro: ").append(sdf.format(filtroFecha));
            }
            cellFecha.setCellValue(infoFecha.toString());
            cellFecha.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, LAST_COL));

            Row row3 = sheet.createRow(3);
            row3.setHeightInPoints(18);

            boolean esNegociado = "NEGOCIANDO".equals(estadoFiltro);

            String[] headers = {
                "Cód. Estilo", "Imagen 1", "Imagen 2", "Descripción", "Estilo Cliente",
                "Color(es)", "Precio"
            };
            Row headerRow = sheet.createRow(4);
            headerRow.setHeightInPoints(18);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = HEADER_ROWS;

            for (ProductoAgrupadoDTO prod : productos) {
                List<ColorPrecioDTO> colores;
                try {
                    colores = clienteProductoService.obtenerColoresConPrecios(
                        prod.getIdProducto(), idCliente, prod.getIdClienteProducto());
                } catch (Exception e) {
                    colores = new ArrayList<>();
                }

                if (colores == null || colores.isEmpty()) continue;

                int startRow = rowNum;
                int endRow   = rowNum + colores.size() - 1;
                boolean tieneImagen = prod.getImagenProducto() != null
                    && !prod.getImagenProducto().isEmpty();
                boolean tieneImagen2 = prod.getImagenProducto2() != null
                    && !prod.getImagenProducto2().isEmpty();

                for (int i = 0; i < colores.size(); i++) {
                    ColorPrecioDTO c = colores.get(i);
                    Row dataRow = sheet.createRow(rowNum);
                    dataRow.setHeightInPoints(i == 0 && (tieneImagen || tieneImagen2) ? 75 : 18);

                    writeCell(dataRow, 0, prod.getCodigoEstilo(), dataStyle);
                    dataRow.createCell(1).setCellStyle(imageCellStyle);
                    dataRow.createCell(2).setCellStyle(imageCellStyle);
                    writeCell(dataRow, 3, prod.getDescripcionProducto(), dataStyle);
                    writeCell(dataRow, 4, prod.getEstiloCliente(), dataStyle);
                    writeCell(dataRow, 5, c.getNombreColor(), dataStyle);
                    java.math.BigDecimal precio = esNegociado ? c.getValorNegociado() : c.getValorInicial();
                    Cell precioCell = dataRow.createCell(6);
                    if (precio != null) {
                        precioCell.setCellValue(precio.doubleValue());
                        precioCell.setCellStyle(precioStyle);
                    } else {
                        precioCell.setCellValue("");
                        precioCell.setCellStyle(dataStyle);
                    }

                    rowNum++;
                }

                if (endRow > startRow) {
                    sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
                    sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1, 1));
                    sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 2, 2));
                    sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 3, 3));
                    sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 4, 4));
                }

                if (tieneImagen) {
                    try {
                        String imagenBase64 = prod.getImagenProducto();
                        int pictureType = Workbook.PICTURE_TYPE_JPEG;
                        String base64Data;
                        if (imagenBase64.startsWith("data:")) {
                            if (imagenBase64.contains("image/png")) pictureType = Workbook.PICTURE_TYPE_PNG;
                            int commaIdx = imagenBase64.indexOf(',');
                            base64Data = commaIdx >= 0 ? imagenBase64.substring(commaIdx + 1) : imagenBase64;
                        } else {
                            base64Data = imagenBase64;
                        }
                        byte[] imgBytes = Base64.getDecoder().decode(base64Data.trim());
                        int picIdx = wb.addPicture(imgBytes, pictureType);
                        ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        anchor.setCol1(1); anchor.setRow1(startRow);
                        anchor.setCol2(2); anchor.setRow2(endRow + 1);
                        drawing.createPicture(anchor, picIdx);
                    } catch (Exception ignored) { }
                }

                if (tieneImagen2) {
                    try {
                        String imagenBase64 = prod.getImagenProducto2();
                        int pictureType = Workbook.PICTURE_TYPE_JPEG;
                        String base64Data;
                        if (imagenBase64.startsWith("data:")) {
                            if (imagenBase64.contains("image/png")) pictureType = Workbook.PICTURE_TYPE_PNG;
                            int commaIdx = imagenBase64.indexOf(',');
                            base64Data = commaIdx >= 0 ? imagenBase64.substring(commaIdx + 1) : imagenBase64;
                        } else {
                            base64Data = imagenBase64;
                        }
                        byte[] imgBytes = Base64.getDecoder().decode(base64Data.trim());
                        int picIdx = wb.addPicture(imgBytes, pictureType);
                        ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        anchor.setCol1(2); anchor.setRow1(startRow);
                        anchor.setCol2(3); anchor.setRow2(endRow + 1);
                        drawing.createPicture(anchor, picIdx);
                    } catch (Exception ignored) { }
                }
            }

            if (rowNum == HEADER_ROWS) {
                Row row = sheet.createRow(rowNum);
                Cell cell = row.createCell(0);
                cell.setCellValue("No hay colores para este cliente.");
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, LAST_COL));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private void writeCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void setBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
