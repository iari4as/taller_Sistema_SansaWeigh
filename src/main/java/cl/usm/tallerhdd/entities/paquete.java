package cl.usm.tallerhdd.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "paquetes")
public class paquete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;

    private Double weight;

    private String description;

    private String sender;

    private String destination;

    private String status; // "REGISTRADO", "PESADO", "EN_TRANSITO", "ENTREGADO"

    @CreatedDate
    private LocalDateTime createdAt;
}
