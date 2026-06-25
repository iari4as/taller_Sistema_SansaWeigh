package cl.usm.tallerhdd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesajeRequestDTO {

    private String idBalanza;

    private String idPaquete;

    private Double pesoEnKg;
}
