package cl.usm.tallerhdd.services;

import cl.usm.tallerhdd.entities.RegistroPesaje;

import java.time.LocalDateTime;
import java.util.List;

public interface PesajeService {

    RegistroPesaje registrarPesaje(String idBalanza, String idPaquete, Double pesoEnKg);

    RegistroPesaje actualizarEstado(String id, String nuevoEstado);

    List<RegistroPesaje> obtenerHistorial(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
