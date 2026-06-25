package cl.usm.tallerhdd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecificacionBalanzaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String brand;

    private Double maxCapacity;

    private Double precision;

    private Double lastCalibrationOffset;
}
