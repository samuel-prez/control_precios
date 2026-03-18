package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.HistorialCambio;
import com.ritchi.control_precios.service.HistorialCambioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named(value = "historialController")
@ViewScoped
public class HistorialController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HistorialCambioService historialCambioService;

    private List<HistorialCambio> cambiosPrecios;
    private List<HistorialCambio> cambiosEstados;
    private List<HistorialCambio> todosLosCambios;

    @PostConstruct
    public void init() {
        cargarHistorial();
    }

    public void cargarHistorial() {
        cambiosPrecios = historialCambioService.obtenerCambiosPrecios();
        cambiosEstados = historialCambioService.obtenerCambiosEstados();
        todosLosCambios = historialCambioService.obtenerTodosLosCambios();
    }

    public List<HistorialCambio> getCambiosPrecios() {
        return cambiosPrecios;
    }

    public List<HistorialCambio> getCambiosEstados() {
        return cambiosEstados;
    }

    public List<HistorialCambio> getTodosLosCambios() {
        return todosLosCambios;
    }
}
