package com.ritchi.control_precios.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ritchi.control_precios.model.dto.ColorPrecioDTO;
import com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PdfExportService {

    private final ClienteProductoService clienteProductoService;

    public PdfExportService(ClienteProductoService clienteProductoService) {
        this.clienteProductoService = clienteProductoService;
    }

    private byte[] cargarLogo() {
        java.io.File logoFile = new java.io.File(
            "C:\\Users\\sistemasti\\Documents\\costos\\control_precios\\src\\main\\resources\\images\\logo_ritchi.jpg");
        if (logoFile.exists()) {
            try { return java.nio.file.Files.readAllBytes(logoFile.toPath()); } catch (Exception ignored) {}
        }
        java.io.File fallback = new java.io.File(System.getProperty("user.dir"),
            "src/main/resources/images/logo_ritchi.jpg");
        if (fallback.exists()) {
            try { return java.nio.file.Files.readAllBytes(fallback.toPath()); } catch (Exception ignored) {}
        }
        return null;
    }

    public byte[] exportarClienteProductosPdf(String nombreCliente, Integer idCliente,
                                               List<ProductoAgrupadoDTO> productos,
                                               Date filtroFecha,
                                               String estadoFiltro) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font infoFont   = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.BOLD, Color.WHITE);
            Font dataFont   = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // ── Cabecera: logo + datos del informe ──────────────────────────────
            PdfPTable headerTable = new PdfPTable(new float[]{3, 4});
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(10);

            byte[] logoBytes = cargarLogo();
            PdfPCell logoCell;
            if (logoBytes != null) {
                Image logo = Image.getInstance(logoBytes);
                logo.scaleToFit(180, 70);
                logoCell = new PdfPCell(logo, false);
            } else {
                logoCell = new PdfPCell(new Phrase("RITCHI", titleFont));
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);

            StringBuilder fechaInfo = new StringBuilder("Fecha de emisión: ").append(sdf.format(new Date()));
            if (filtroFecha != null) {
                fechaInfo.append("   |   Fecha filtro: ").append(sdf.format(filtroFecha));
            }
            Paragraph infoParagraph = new Paragraph();
            infoParagraph.add(new Phrase("RITCHI\n", titleFont));
            infoParagraph.add(new Phrase("Cliente: " + nombreCliente + "\n", infoFont));
            infoParagraph.add(new Phrase(fechaInfo.toString(), infoFont));
            PdfPCell infoCell = new PdfPCell();
            infoCell.addElement(infoParagraph);
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(infoCell);

            document.add(headerTable);

            // ── Tabla de productos ───────────────────────────────────────────────
            float[] colWidths = {10, 12, 12, 28, 14, 18, 10};
            PdfPTable table = new PdfPTable(colWidths);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            Color darkBlue = new Color(0, 0, 128);
            String[] headers = {"Cód. Estilo", "Imagen 1", "Imagen 2", "Descripción",
                                 "Estilo Cliente", "Color(es)", "Precio"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(darkBlue);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(4);
                table.addCell(cell);
            }

            boolean esNegociado = "NEGOCIANDO".equals(estadoFiltro);

            boolean hasData = false;

            for (ProductoAgrupadoDTO prod : productos) {
                List<ColorPrecioDTO> colores;
                try {
                    colores = clienteProductoService.obtenerColoresConPrecios(
                        prod.getIdProducto(), idCliente, prod.getIdClienteProducto());
                } catch (Exception e) {
                    colores = new ArrayList<>();
                }
                if (colores == null || colores.isEmpty()) continue;
                hasData = true;

                int numRows = colores.size();
                float imgHeight = 60f;

                boolean tieneImagen  = prod.getImagenProducto()  != null && !prod.getImagenProducto().isEmpty();
                boolean tieneImagen2 = prod.getImagenProducto2() != null && !prod.getImagenProducto2().isEmpty();

                // Columnas fusionadas por producto
                PdfPCell codCell = new PdfPCell(new Phrase(
                    prod.getCodigoEstilo() != null ? prod.getCodigoEstilo() : "", dataFont));
                codCell.setRowspan(numRows);
                codCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                codCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                codCell.setPadding(3);
                table.addCell(codCell);

                PdfPCell img1Cell = new PdfPCell();
                img1Cell.setRowspan(numRows);
                img1Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                img1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (tieneImagen) {
                    img1Cell.setFixedHeight(imgHeight);
                    try {
                        String b64 = prod.getImagenProducto();
                        String base64Data = b64.startsWith("data:") ? b64.substring(b64.indexOf(',') + 1) : b64;
                        byte[] imgBytes = Base64.getDecoder().decode(base64Data.trim());
                        Image img = Image.getInstance(imgBytes);
                        img.scaleToFit(80, imgHeight - 4);
                        img1Cell.addElement(img);
                    } catch (Exception ignored) {}
                }
                table.addCell(img1Cell);

                PdfPCell img2Cell = new PdfPCell();
                img2Cell.setRowspan(numRows);
                img2Cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                img2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (tieneImagen2) {
                    img2Cell.setFixedHeight(imgHeight);
                    try {
                        String b64 = prod.getImagenProducto2();
                        String base64Data = b64.startsWith("data:") ? b64.substring(b64.indexOf(',') + 1) : b64;
                        byte[] imgBytes = Base64.getDecoder().decode(base64Data.trim());
                        Image img = Image.getInstance(imgBytes);
                        img.scaleToFit(80, imgHeight - 4);
                        img2Cell.addElement(img);
                    } catch (Exception ignored) {}
                }
                table.addCell(img2Cell);

                PdfPCell descCell = new PdfPCell(new Phrase(
                    prod.getDescripcionProducto() != null ? prod.getDescripcionProducto() : "", dataFont));
                descCell.setRowspan(numRows);
                descCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                descCell.setPadding(3);
                table.addCell(descCell);

                PdfPCell estiloCell = new PdfPCell(new Phrase(
                    prod.getEstiloCliente() != null ? prod.getEstiloCliente() : "", dataFont));
                estiloCell.setRowspan(numRows);
                estiloCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                estiloCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                estiloCell.setPadding(3);
                table.addCell(estiloCell);

                // Una fila por color
                for (ColorPrecioDTO c : colores) {
                    PdfPCell colorCell = new PdfPCell(new Phrase(
                        c.getNombreColor() != null ? c.getNombreColor() : "", dataFont));
                    colorCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    colorCell.setPadding(3);
                    table.addCell(colorCell);

                    java.math.BigDecimal precio = esNegociado ? c.getValorNegociado() : c.getValorInicial();
                    String precioTexto = precio != null
                        ? String.format("$ %,.0f", precio.doubleValue())
                        : "-";
                    PdfPCell precioCell = new PdfPCell(new Phrase(precioTexto, dataFont));
                    precioCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    precioCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    precioCell.setPadding(3);
                    table.addCell(precioCell);
                }
            }

            if (!hasData) {
                PdfPCell noDataCell = new PdfPCell(new Phrase("No hay colores para este cliente.", dataFont));
                noDataCell.setColspan(7);
                noDataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                noDataCell.setPadding(10);
                table.addCell(noDataCell);
            }

            document.add(table);

        } finally {
            if (document.isOpen()) document.close();
        }

        return out.toByteArray();
    }
}
