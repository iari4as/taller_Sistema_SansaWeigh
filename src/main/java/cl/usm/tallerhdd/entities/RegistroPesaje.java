package cl.usm.tallerhdd.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "registros_pesaje")
public class RegistroPesaje implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String idBalanza;

    private String idPaquete;

    private Double pesoSansas;

    private String categoria; // "LIVIANO", "MEDIANO", "PESADO"

    private String estado; // "INGRESADO", "PESADO", "APROBADO", "RECHAZADO", "DESPACHADO"

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
