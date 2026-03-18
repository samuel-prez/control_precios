package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteProducto;
import com.ritchi.control_precios.model.entity.Color;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import com.ritchi.control_precios.repository.ClienteProductoColorRepository;
import com.ritchi.control_precios.repository.ClienteProductoRepository;
import com.ritchi.control_precios.repository.ClienteRepository;
import com.ritchi.control_precios.repository.ColorRepository;
import com.ritchi.control_precios.repository.ProductoColorRepository;
import com.ritchi.control_precios.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExcelImportService {

    private final ProductoRepository productoRepository;
    private final ColorRepository colorRepository;
    private final ProductoColorRepository productoColorRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteProductoRepository clienteProductoRepository;
    private final ClienteProductoColorRepository clienteProductoColorRepository;

    public ExcelImportService(ProductoRepository productoRepository,
                              ColorRepository colorRepository,
                              ProductoColorRepository productoColorRepository,
                              ClienteRepository clienteRepository,
                              ClienteProductoRepository clienteProductoRepository,
                              ClienteProductoColorRepository clienteProductoColorRepository) {
        this.productoRepository = productoRepository;
        this.colorRepository = colorRepository;
        this.productoColorRepository = productoColorRepository;
        this.clienteRepository = clienteRepository;
        this.clienteProductoRepository = clienteProductoRepository;
        this.clienteProductoColorRepository = clienteProductoColorRepository;
    }

    public byte[] generarPlantillaProductos() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Importar");

            CellStyle paso1Style = crearEstiloEncabezado(workbook, IndexedColors.DARK_BLUE);
            CellStyle paso2Style = crearEstiloEncabezado(workbook, IndexedColors.DARK_GREEN);
            CellStyle paso3Style = crearEstiloEncabezado(workbook, IndexedColors.ORANGE);
            CellStyle opcStyle   = crearEstiloEncabezado(workbook, IndexedColors.GREY_50_PERCENT);

            Row pasoRow = sheet.createRow(0);
            crearCeldaMerge(sheet, pasoRow, 0, 0, "PASO 1 - CLIENTE *", paso1Style);
            crearCeldaMerge(sheet, pasoRow, 1, 3, "PASO 2 - PRODUCTO *", paso2Style);
            crearCeldaMerge(sheet, pasoRow, 4, 4, "PASO 3 - COLORES *", paso3Style);
            crearCeldaMerge(sheet, pasoRow, 5, 6, "DATOS EXTRA (opcional)", opcStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 6));

            Row headerRow = sheet.createRow(1);
            String[] headers = {
                "Cliente (separar por ,)",   // 0 - paso 1
                "Cód. Estilo",               // 1 - paso 2
                "Cód. Prototipo",            // 2 - paso 2
                "Descripción",               // 3 - paso 2
                "Colores (separados por ,)", // 4 - paso 3
                "Tallas",                    // 5 - extra
                "Estilo Cliente"             // 6 - extra
            };
            CellStyle[] headerStyles = {
                paso1Style, paso2Style, paso2Style, paso2Style,
                paso3Style, opcStyle, opcStyle
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyles[i]);
            }

            int[] widths = {12000, 4000, 4500, 8000, 12000, 5000, 5000};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i]);
            }

            Sheet clientesSheet = workbook.createSheet("Clientes Disponibles");
            Row chRow = clientesSheet.createRow(0);
            Cell chCell = chRow.createCell(0);
            chCell.setCellValue("Nombre del Cliente");
            chCell.setCellStyle(paso1Style);
            List<Cliente> clientes = clienteRepository.findAll();
            int ci = 1;
            for (Cliente c : clientes) {
                clientesSheet.createRow(ci++).createCell(0).setCellValue(c.getNombre());
            }
            clientesSheet.setColumnWidth(0, 10000);

            Sheet colorsSheet = workbook.createSheet("Colores Disponibles");
            Row coRow = colorsSheet.createRow(0);
            Cell coCell = coRow.createCell(0);
            coCell.setCellValue("Nombre del Color");
            coCell.setCellStyle(paso3Style);
            List<Color> colores = colorRepository.findAllByOrderByNombreAsc();
            int coi = 1;
            for (Color col : colores) {
                colorsSheet.createRow(coi++).createCell(0).setCellValue(col.getNombre());
            }
            colorsSheet.setColumnWidth(0, 8000);

            Sheet instrSheet = workbook.createSheet("Instrucciones");
            instrSheet.setColumnWidth(0, 20000);
            String[] instr = {
                "INSTRUCCIONES - IMPORTACIÓN PASO A PASO",
                "",
                "REGLAS DE VALIDACIÓN (se aplican en orden):",
                "",
                "PASO 1 - CLIENTE (azul):",
                "  → El nombre del cliente es OBLIGATORIO en cada fila.",
                "  → Para asignar a VARIOS clientes, sepárelos con coma: CLIENTE1,CLIENTE2",
                "  → Si una fila tiene datos pero falta el cliente, la fila se rechaza.",
                "  → Consulte la hoja 'Clientes Disponibles' para los nombres exactos.",
                "",
                "PASO 2 - PRODUCTO (verde):",
                "  → Si llenó el cliente, debe llenar al menos Cód. Estilo O Cód. Prototipo.",
                "  → Si el cliente está pero no hay producto, la fila se rechaza.",
                "  → Si el producto ya existe en el sistema, se actualiza.",
                "",
                "PASO 3 - COLORES (naranja):",
                "  → Si llenó el producto, debe llenar al menos un color.",
                "  → Si no hay colores, la fila se rechaza.",
                "  → Separe múltiples colores con coma: NEGRO, ROJO, AZUL",
                "  → Consulte la hoja 'Colores Disponibles' para los nombres exactos.",
                "",
                "DATOS EXTRA (gris, OPCIONAL):",
                "  → Tallas: separadas por coma (S, M, L, XL)",
                "  → Estilo Cliente: código del estilo según el cliente",
                "",
                "NOTAS GENERALES:",
                "  → Elimine la fila de EJEMPLO (fila 3 en gris) antes de cargar.",
                "  → Las filas completamente vacías se ignoran.",
                "  → Al cargar verá un resumen con errores y advertencias por fila.",
                "  → Los colores inexistentes en el catálogo generan advertencia y se omiten."
            };
            for (int i = 0; i < instr.length; i++) {
                Row r = instrSheet.createRow(i);
                Cell c2 = r.createCell(0);
                c2.setCellValue(instr[i]);
                if (i == 0) {
                    CellStyle ts = workbook.createCellStyle();
                    Font tf = workbook.createFont();
                    tf.setBold(true);
                    tf.setFontHeightInPoints((short) 13);
                    ts.setFont(tf);
                    c2.setCellStyle(ts);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font f = workbook.createFont();
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setBold(true);
        style.setFont(f);
        return style;
    }

    private void crearCeldaMerge(Sheet sheet, Row row, int col, int colFin, String valor, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(valor);
        cell.setCellStyle(style);
        for (int c = col + 1; c <= colFin; c++) {
            Cell extra = row.createCell(c);
            extra.setCellStyle(style);
        }
    }

    public ImportResult importarProductos(InputStream inputStream) throws IOException {
        ImportResult result = new ImportResult();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                int rowNum = row.getRowNum();

                if (rowNum <= 1) continue;

                String clientesStr      = getCellValueAsString(row.getCell(0));
                String codigoEstilo     = getCellValueAsString(row.getCell(1));
                String codigoProto      = getCellValueAsString(row.getCell(2));
                String descripcion      = getCellValueAsString(row.getCell(3));
                String coloresStr       = getCellValueAsString(row.getCell(4));
                String tallas           = getCellValueAsString(row.getCell(5));
                String estiloCliente    = getCellValueAsString(row.getCell(6));

                if (estaVacia(clientesStr, codigoEstilo, codigoProto, coloresStr)) continue;

                int fila = rowNum + 1;

                if (estaVacio(clientesStr)) {
                    result.addError("Fila " + fila + " [PASO 1]: Falta el cliente. Si hay datos en la fila, el cliente es obligatorio.");
                    continue;
                }

                if (estaVacio(codigoEstilo) && estaVacio(codigoProto)) {
                    result.addError("Fila " + fila + " [PASO 2]: Tiene cliente pero falta el Cód. Estilo o Cód. Prototipo.");
                    continue;
                }

                if (estaVacio(coloresStr)) {
                    result.addError("Fila " + fila + " [PASO 3]: Tiene cliente y producto pero falta la columna Colores.");
                    continue;
                }

                Producto producto;
                try {
                    producto = resolverProducto(codigoEstilo, codigoProto, descripcion, tallas);
                    int coloresAgregados = procesarColores(producto, coloresStr, result, fila);
                    result.addColoresAgregados(coloresAgregados);
                } catch (Exception e) {
                    result.addError("Fila " + fila + " [PASO 2/3]: Error procesando producto — " + e.getMessage());
                    continue;
                }

                String[] nombresCliente = clientesStr.split(",");
                for (String nombreClienteRaw : nombresCliente) {
                    String nombreCliente = nombreClienteRaw.trim();
                    if (nombreCliente.isEmpty()) continue;

                    Cliente cliente = clienteRepository.findByNombre(nombreCliente).orElse(null);
                    if (cliente == null) {
                        result.addError("Fila " + fila + " [PASO 1]: Cliente '" + nombreCliente + "' no encontrado. Verifique la hoja 'Clientes Disponibles'.");
                        continue;
                    }

                    try {
                        asignarProductoAlCliente(cliente, producto, estiloCliente, result, fila);

                        result.addFila(fila, cliente.getNombre(), producto.getCodigoEstilo());

                    } catch (Exception e) {
                        result.addError("Fila " + fila + " [" + nombreCliente + "]: Error inesperado — " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            result.addError("Error al leer el archivo: " + e.getMessage());
        }

        return result;
    }

    private Producto resolverProducto(String codigoEstilo, String codigoProto,
                                       String descripcion, String tallas) {
        Producto producto = null;

        if (!estaVacio(codigoEstilo)) {
            producto = productoRepository.findByCodigoEstilo(codigoEstilo.trim()).orElse(null);
        }
        if (producto == null && !estaVacio(codigoProto)) {
            producto = productoRepository.findByCodigoPrototipo(codigoProto.trim()).orElse(null);
        }

        if (producto != null) {
            if (!estaVacio(codigoEstilo))  producto.setCodigoEstilo(codigoEstilo.trim());
            if (!estaVacio(codigoProto))   producto.setCodigoPrototipo(codigoProto.trim());
            if (!estaVacio(descripcion))   producto.setDescripcion(descripcion.trim());
            if (!estaVacio(tallas))        producto.setTallas(tallas.trim());
            producto = productoRepository.save(producto);
        } else {
            producto = new Producto();
            producto.setCodigoEstilo(!estaVacio(codigoEstilo) ? codigoEstilo.trim() : null);
            producto.setCodigoPrototipo(!estaVacio(codigoProto) ? codigoProto.trim() : null);
            producto.setDescripcion(!estaVacio(descripcion) ? descripcion.trim() : "");
            producto.setTallas(!estaVacio(tallas) ? tallas.trim() : null);
            producto.setCreadoEn(new Date());
            producto = productoRepository.save(producto);
        }
        return producto;
    }

    private ClienteProducto asignarProductoAlCliente(Cliente cliente, Producto producto,
                                                       String estiloCliente, ImportResult result, int fila) {
        ClienteProducto cp = clienteProductoRepository
            .findByCliente_IdClienteAndProducto_IdProducto(cliente.getIdCliente(), producto.getIdProducto())
            .orElse(null);

        if (cp == null) {
            cp = new ClienteProducto();
            cp.setCliente(cliente);
            cp.setProducto(producto);
            cp.setEstado("ACTIVO");
            cp.setCreadoEn(new Date());
            cp.setFechaCotizacion(new Date());
            if (!estaVacio(estiloCliente)) cp.setEstiloCliente(estiloCliente.trim());
            cp = clienteProductoRepository.save(cp);
            result.addAsignacion(fila, cliente.getNombre(), producto.getCodigoEstilo());
        } else if (!estaVacio(estiloCliente)) {
            cp.setEstiloCliente(estiloCliente.trim());
            cp = clienteProductoRepository.save(cp);
        }
        return cp;
    }

    private int procesarColores(Producto producto, String coloresStr, ImportResult result, int fila) {
        int agregados = 0;
        List<ProductoColor> existentes = productoColorRepository.findByProducto_IdProducto(producto.getIdProducto());

        for (String nombreColor : coloresStr.split(",")) {
            String nombre = nombreColor.trim();
            if (nombre.isEmpty()) continue;

            Color color = colorRepository.findByNombreIgnoreCase(nombre);
            if (color == null) {
                result.addAdvertencia("Fila " + fila + " [PASO 3]: Color '" + nombre + "' no existe en el catálogo y fue omitido.");
                continue;
            }

            boolean yaExiste = existentes.stream()
                .anyMatch(pc -> pc.getColor().getIdColor().equals(color.getIdColor()));
            if (yaExiste) continue;

            ProductoColor pc = new ProductoColor();
            pc.setProducto(producto);
            pc.setColor(color);
            pc.setCreadoEn(new Date());
            pc.setActivo(true);
            productoColorRepository.save(pc);
            existentes.add(pc);
            agregados++;
        }
        return agregados;
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private boolean estaVacia(String... valores) {
        for (String v : valores) {
            if (v != null && !v.trim().isEmpty()) return false;
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    public static class ImportResult {
        private final List<String> errores       = new ArrayList<>();
        private final List<String> advertencias  = new ArrayList<>();
        private final List<String> filasProcesadas = new ArrayList<>();
        private final List<String> asignaciones  = new ArrayList<>();
        private int coloresAgregados = 0;

        public void addError(String e)         { errores.add(e); }
        public void addAdvertencia(String a)   { advertencias.add(a); }
        public void addColoresAgregados(int n) { coloresAgregados += n; }
        public void addFila(int fila, String cliente, String estilo) {
            filasProcesadas.add("Fila " + fila + ": " + cliente + " → " + estilo);
        }
        public void addAsignacion(int fila, String cliente, String estilo) {
            asignaciones.add("Fila " + fila + ": " + cliente + " → " + estilo + " (nueva asignación)");
        }

        public List<String> getErrores()      { return errores; }
        public List<String> getAdvertencias() { return advertencias; }
        public List<String> getAsignaciones() { return asignaciones; }
        public boolean tieneErrores()         { return !errores.isEmpty(); }
        public boolean tieneAdvertencias()    { return !advertencias.isEmpty(); }

        public int getTotalCreados()          { return filasProcesadas.size(); }
        public int getTotalErrores()          { return errores.size(); }
        public int getTotalColoresAgregados() { return coloresAgregados; }

        public int getTotalActualizados()     { return 0; }
        public int getTotalPreciosCreados()   { return 0; }
        public List<String> getDetalleProductos() { return filasProcesadas; }

        public String getResumen() {
            StringBuilder sb = new StringBuilder();
            sb.append("Filas procesadas: ").append(filasProcesadas.size());
            sb.append(" | Colores agregados: ").append(coloresAgregados);
            if (!asignaciones.isEmpty()) sb.append(" | Nuevas asignaciones: ").append(asignaciones.size());
            if (!errores.isEmpty()) sb.append(" | ⚠ Errores: ").append(errores.size());
            if (!advertencias.isEmpty()) sb.append(" | Advertencias: ").append(advertencias.size());
            return sb.toString();
        }
    }
}
