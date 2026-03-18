package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.dto.ColorPrecioDTO;
import com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO;
import com.ritchi.control_precios.model.dto.ProductoClienteDTO;
import com.ritchi.control_precios.model.entity.ClienteProducto;
import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.repository.ClienteProductoColorRepository;
import com.ritchi.control_precios.repository.ClienteProductoRepository;
import com.ritchi.control_precios.repository.ProductoColorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteProductoService {

    private final ClienteProductoRepository clienteProductoRepository;
    private final ProductoColorRepository productoColorRepository;
    private final ClienteProductoColorRepository clienteProductoColorRepository;

    public ClienteProductoService(ClienteProductoRepository clienteProductoRepository,
                                  ProductoColorRepository productoColorRepository,
                                  ClienteProductoColorRepository clienteProductoColorRepository) {
        this.clienteProductoRepository = clienteProductoRepository;
        this.productoColorRepository = productoColorRepository;
        this.clienteProductoColorRepository = clienteProductoColorRepository;
    }

    public List<ClienteProducto> obtenerProductosDelCliente(Integer idCliente) {
        return clienteProductoRepository.findByCliente_IdClienteAndEstado(idCliente, "ACTIVO");
    }

    public List<ClienteProducto> obtenerTodosProductosDelCliente(Integer idCliente) {
        return clienteProductoRepository.findByCliente_IdCliente(idCliente);
    }

    public List<ClienteProducto> buscarPorEstiloCliente(Integer idCliente, String estiloCliente) {
        return clienteProductoRepository.findByCliente_IdClienteAndEstiloClienteContainingIgnoreCase(idCliente, estiloCliente);
    }

    public List<ClienteProducto> buscarPorCodigoOEstilo(Integer idCliente, String codigo) {
        return clienteProductoRepository.buscarPorCodigoOEstilo(idCliente, codigo);
    }

    public List<ClienteProducto> obtenerProductosOrdenadosPorFecha(Integer idCliente) {
        return clienteProductoRepository.findByCliente_IdClienteOrderByFechaCotizacionDesc(idCliente);
    }

    public ClienteProducto crearClienteProducto(ClienteProducto clienteProducto) {
        clienteProducto.setCreadoEn(new Date());
        clienteProducto.setActualizadoEn(new Date());
        if (clienteProducto.getEstado() == null || clienteProducto.getEstado().isEmpty()) {
            clienteProducto.setEstado("ACTIVO");
        }
        return clienteProductoRepository.save(clienteProducto);
    }

    public ClienteProducto actualizarClienteProducto(ClienteProducto clienteProducto) {
        clienteProducto.setActualizadoEn(new Date());
        return clienteProductoRepository.save(clienteProducto);
    }

    public void eliminarClienteProducto(Integer idClienteProducto) {
        eliminarClienteProducto(idClienteProducto, null);
    }

    public void eliminarClienteProducto(Integer idClienteProducto, String usuarioEliminador) {
        Optional<ClienteProducto> clienteProductoOpt = clienteProductoRepository.findById(idClienteProducto);

        if (clienteProductoOpt.isPresent()) {
            clienteProductoColorRepository.deleteByClienteProducto_IdClienteProducto(idClienteProducto);
            clienteProductoRepository.deleteById(idClienteProducto);
        } else {
            throw new RuntimeException("ClienteProducto no encontrado con ID: " + idClienteProducto);
        }
    }

    public Optional<ClienteProducto> obtenerPorClienteYProducto(Integer idCliente, Integer idProducto) {
        return clienteProductoRepository.findByCliente_IdClienteAndProducto_IdProducto(idCliente, idProducto);
    }

    public boolean existeProductoEnCliente(Integer idCliente, Integer idProducto) {
        return clienteProductoRepository.findByCliente_IdClienteAndProducto_IdProducto(idCliente, idProducto).isPresent();
    }

    public ClienteProducto obtenerPorId(Integer idClienteProducto) {
        return clienteProductoRepository.findById(idClienteProducto)
                .orElseThrow(() -> new RuntimeException("ClienteProducto no encontrado con ID: " + idClienteProducto));
    }

    public List<ProductoClienteDTO> obtenerVistaCompletaProductosCliente(Integer idCliente) {
        return clienteProductoRepository.obtenerVistaCompletaProductosCliente(idCliente);
    }

    public List<ProductoAgrupadoDTO> obtenerProductosAgrupados(Integer idCliente) {
        return clienteProductoRepository.obtenerProductosAgrupadosCliente(idCliente);
    }

    public List<ColorPrecioDTO> obtenerColoresConPrecios(Integer idProducto, Integer idCliente, Integer idClienteProducto) {
        if (idClienteProducto != null) {
            List<ClienteProductoColor> coloresCliente = clienteProductoColorRepository
                    .findByClienteProducto_IdClienteProductoAndActivoTrue(idClienteProducto);

            if (coloresCliente.isEmpty()) {
                sincronizarColoresDesdeProducto(idClienteProducto, idProducto);
                coloresCliente = clienteProductoColorRepository
                        .findByClienteProducto_IdClienteProductoAndActivoTrue(idClienteProducto);
            }

            if (!coloresCliente.isEmpty()) {
                return productoColorRepository
                        .obtenerColoresConPreciosPorClienteProducto(idCliente, idClienteProducto);
            }
        }

        return productoColorRepository.obtenerColoresConPreciosDelProducto(idProducto, idCliente, idClienteProducto);
    }

    @Transactional
    public void sincronizarColoresDesdeProducto(Integer idClienteProducto, Integer idProducto) {
        List<ClienteProductoColor> coloresCliente = clienteProductoColorRepository
                .findByClienteProducto_IdClienteProductoAndActivoTrue(idClienteProducto);
        List<ProductoColor> coloresProducto = productoColorRepository.findByProducto_IdProductoAndActivoTrue(idProducto);

        if (coloresProducto.isEmpty()) return;

        ClienteProducto clienteProducto = clienteProductoRepository.findById(idClienteProducto).orElse(null);
        if (clienteProducto == null) return;

        Date ahora = new Date();
        for (ProductoColor pc : coloresProducto) {
            boolean existe = coloresCliente.stream()
                    .anyMatch(cpc -> cpc.getProductoColor().getIdProductoColor().equals(pc.getIdProductoColor()));
            if (!existe) {
                ClienteProductoColor nuevoCpc = new ClienteProductoColor();
                nuevoCpc.setClienteProducto(clienteProducto);
                nuevoCpc.setProductoColor(pc);
                nuevoCpc.setActivo(true);
                nuevoCpc.setCreadoEn(ahora);
                clienteProductoColorRepository.save(nuevoCpc);
            }
        }
    }

    public ClienteProductoColor agregarColorAClienteProducto(Integer idClienteProducto, Integer idProductoColor) {
        ClienteProducto clienteProducto = clienteProductoRepository.findById(idClienteProducto)
                .orElseThrow(() -> new RuntimeException("ClienteProducto no encontrado"));

        ProductoColor productoColor = productoColorRepository.findById(idProductoColor)
                .orElseThrow(() -> new RuntimeException("ProductoColor no encontrado"));

        ClienteProductoColor cpc = new ClienteProductoColor();
        cpc.setClienteProducto(clienteProducto);
        cpc.setProductoColor(productoColor);
        cpc.setActivo(true);
        cpc.setCreadoEn(new Date());

        return clienteProductoColorRepository.save(cpc);
    }

    public void guardarColoresClienteProducto(Integer idClienteProducto, List<Integer> idsProductoColor) {
        ClienteProducto clienteProducto = clienteProductoRepository.findById(idClienteProducto)
                .orElseThrow(() -> new RuntimeException("ClienteProducto no encontrado"));

        Date ahora = new Date();

        for (Integer idProductoColor : idsProductoColor) {
            Optional<ClienteProductoColor> existente = clienteProductoColorRepository
                    .findByClienteProducto_IdClienteProductoAndProductoColor_IdProductoColor(idClienteProducto, idProductoColor);

            if (existente.isEmpty()) {
                ProductoColor productoColor = productoColorRepository.findById(idProductoColor)
                        .orElseThrow(() -> new RuntimeException("ProductoColor no encontrado"));

                ClienteProductoColor cpc = new ClienteProductoColor();
                cpc.setClienteProducto(clienteProducto);
                cpc.setProductoColor(productoColor);
                cpc.setActivo(true);
                cpc.setCreadoEn(ahora);

                clienteProductoColorRepository.save(cpc);
            }
        }
    }

    public List<Integer> obtenerIdsColoresActivosClienteProducto(Integer idClienteProducto) {
        return clienteProductoColorRepository.findIdsColoresActivosPorClienteProducto(idClienteProducto);
    }

    public String calcularEstadoGeneral(Integer idProducto, Integer idCliente, Integer idClienteProducto) {
        return "SIN PRECIO";
    }
}
