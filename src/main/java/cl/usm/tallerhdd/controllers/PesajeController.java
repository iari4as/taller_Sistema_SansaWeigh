package cl.usm.tallerhdd.controllers;

import cl.usm.tallerhdd.dto.EstadoUpdateRequestDTO;
import cl.usm.tallerhdd.dto.PesajeRequestDTO;
import cl.usm.tallerhdd.entities.RegistroPesaje;
import cl.usm.tallerhdd.services.PesajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pesajes")
@RequiredArgsConstructor
public class PesajeController {

    private final PesajeService pesajeService;

    @PostMapping
    public ResponseEntity<RegistroPesaje> registrarPesaje(@RequestBody PesajeRequestDTO request) {
        RegistroPesaje nuevoPesaje = pesajeService.registrarPesaje(
                request.getIdBalanza(),
                request.getIdPaquete(),
                request.getPesoEnKg()
        );
        return new ResponseEntity<>(nuevoPesaje, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<RegistroPesaje> actualizarEstadoPut(
            @PathVariable String id,
            @RequestBody EstadoUpdateRequestDTO request) {
        RegistroPesaje actualizado = pesajeService.actualizarEstado(id, request.getEstado());
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RegistroPesaje> actualizarEstadoPatch(
            @PathVariable String id,
            @RequestBody EstadoUpdateRequestDTO request) {
        RegistroPesaje actualizado = pesajeService.actualizarEstado(id, request.getEstado());
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping
    public ResponseEntity<List<RegistroPesaje>> obtenerHistorial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<RegistroPesaje> historial = pesajeService.obtenerHistorial(fechaInicio, fechaFin);
        return ResponseEntity.ok(historial);
    }
}
