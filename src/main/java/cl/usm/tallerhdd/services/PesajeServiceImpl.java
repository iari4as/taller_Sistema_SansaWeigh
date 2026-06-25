package cl.usm.tallerhdd.services;

import cl.usm.tallerhdd.entities.RegistroPesaje;
import cl.usm.tallerhdd.controllers.GlobalExceptionHandler.IllegalWeighingStateException;
import cl.usm.tallerhdd.controllers.GlobalExceptionHandler.ResourceNotFoundException;
import cl.usm.tallerhdd.repositories.RegistroPesajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PesajeServiceImpl implements PesajeService {

    private final RegistroPesajeRepository repository;

    private static final double SANSA_EQUIVALENCE_KG = 1.337;

    @Override
    public RegistroPesaje registrarPesaje(String idBalanza, String idPaquete, Double pesoEnKg) {
        if (idBalanza == null || idBalanza.isBlank()) {
            throw new IllegalArgumentException("El ID de la balanza no puede estar vacío.");
        }
        if (idPaquete == null || idPaquete.isBlank()) {
            throw new IllegalArgumentException("El ID del paquete no puede estar vacío.");
        }
        if (pesoEnKg == null || pesoEnKg <= 0) {
            throw new IllegalArgumentException("El peso en kilogramos debe ser mayor a cero.");
        }

        // 1. Conversión de Unidades: Kg a Sansas
        double pesoSansas = pesoEnKg / SANSA_EQUIVALENCE_KG;

        // 2. Clasificación de Peso
        String categoria = clasificarPeso(pesoSansas);

        // 3. Restricciones Críticas si el paquete es PESADO
        validarRestriccionesPesado(idBalanza, categoria);

        // 4. Inicialización en estado INGRESADO
        RegistroPesaje nuevoRegistro = RegistroPesaje.builder()
                .idBalanza(idBalanza)
                .idPaquete(idPaquete)
                .pesoSansas(pesoSansas)
                .categoria(categoria)
                .estado("INGRESADO")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return repository.save(nuevoRegistro);
    }

    @Override
    public RegistroPesaje actualizarEstado(String id, String nuevoEstado) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del registro no puede estar vacío.");
        }
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío.");
        }

        RegistroPesaje registro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el registro de pesaje con ID: " + id));

        String estadoActual = registro.getEstado();
        
        // 1. Validar la Máquina de Estados
        validarTransicion(estadoActual, nuevoEstado);

        // 2. Validar Restricciones Críticas si pasa a estado que procesa o confirma un paquete PESADO
        validarRestriccionesPesado(registro.getIdBalanza(), registro.getCategoria());

        // 3. Actualizar estado y fecha de modificación
        registro.setEstado(nuevoEstado);
        registro.setUpdatedAt(LocalDateTime.now());

        return repository.save(registro);
    }

    @Override
    public List<RegistroPesaje> obtenerHistorial(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias para obtener el historial.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return repository.findByCreatedAtBetween(fechaInicio, fechaFin);
    }

    /**
     * Clasifica el peso en Sansas en las categorías correspondientes.
     */
    private String clasificarPeso(double pesoSansas) {
        if (pesoSansas <= 10.0) {
            return "LIVIANO";
        } else if (pesoSansas <= 50.0) {
            return "MEDIANO";
        } else {
            return "PESADO";
        }
    }

    /**
     * Aplica las restricciones críticas para paquetes clasificados como PESADOS.
     */
    private void validarRestriccionesPesado(String idBalanza, String categoria) {
        if ("PESADO".equals(categoria)) {
            // Restricción Horaria: Bloquear si está entre las 20:00 y las 06:00 horas
            LocalTime horaActual = LocalTime.now();
            int hora = horaActual.getHour();
            if (hora >= 20 || hora < 6) {
                throw new IllegalWeighingStateException(
                        String.format("Restricción Horaria: Bloqueado el procesamiento de paquetes PESADOS entre las 20:00 y las 06:00 horas (Hora actual: %s).", horaActual)
                );
            }

            // Regla de Balanza Prima en días impares del mes
            try {
                int idNum = Integer.parseInt(idBalanza);
                if (isPrime(idNum)) {
                    int diaDelMes = LocalDate.now().getDayOfMonth();
                    if (diaDelMes % 2 != 0) {
                        throw new IllegalWeighingStateException(
                                String.format("Restricción de Balanza Prima: La balanza con ID %d (número primo) no puede registrar paquetes PESADOS en días calendario impares (Día actual: %d).", idNum, diaDelMes)
                        );
                    }
                }
            } catch (NumberFormatException e) {
                // Si el ID de la balanza no es puramente numérico, no se aplica la restricción de primalidad.
            }
        }
    }

    /**
     * Controla estrictamente la transición del ciclo de vida del paquete:
     * INGRESADO -> PESADO -> APROBADO/RECHAZADO -> DESPACHADO
     */
    private void validarTransicion(String estadoActual, String nuevoEstado) {
        if (estadoActual.equals(nuevoEstado)) {
            return; // No hay cambio de estado
        }

        boolean transicionValida = false;

        switch (estadoActual) {
            case "INGRESADO":
                if ("PESADO".equals(nuevoEstado)) {
                    transicionValida = true;
                }
                break;
            case "PESADO":
                if ("APROBADO".equals(nuevoEstado) || "RECHAZADO".equals(nuevoEstado)) {
                    transicionValida = true;
                }
                break;
            case "APROBADO":
            case "RECHAZADO":
                if ("DESPACHADO".equals(nuevoEstado)) {
                    transicionValida = true;
                }
                break;
            case "DESPACHADO":
                // Estado final, no se permiten más transiciones
                break;
        }

        if (!transicionValida) {
            throw new IllegalWeighingStateException(
                    String.format("Violación de Ciclo de Vida: No se permite transicionar el estado de %s a %s. El flujo permitido es: INGRESADO -> PESADO -> APROBADO/RECHAZADO -> DESPACHADO.", estadoActual, nuevoEstado)
            );
        }
    }

    /**
     * Algoritmo de primalidad básico optimizado (O(sqrt(N))).
     */
    private boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
