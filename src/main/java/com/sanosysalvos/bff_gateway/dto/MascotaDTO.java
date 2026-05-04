package com.sanosysalvos.bff_gateway.dto;

import lombok.Data;

@Data
public class MascotaDTO {
    private Integer id;
    private String nombre;
    private String raza;
    private String color;
    private String tamano;
    private String estado;
}