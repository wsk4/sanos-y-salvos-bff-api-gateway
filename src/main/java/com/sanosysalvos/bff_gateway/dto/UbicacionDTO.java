package com.sanosysalvos.bff_gateway.dto;

import lombok.Data;

@Data
public class UbicacionDTO {
    private Integer id;
    private Integer mascotaId;
    private Double latitud;
    private Double longitud;
    private String direccion;
}