package com.sanosysalvos.bff_gateway.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;
import com.sanosysalvos.bff_gateway.service.OrquestadorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bff/v1/consolidado")
@RequiredArgsConstructor
public class BffController {

    private final OrquestadorService orquestadorService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<MascotaConsolidadaDTO>> cargarDashboardPrincipal() {
        return ResponseEntity.ok(orquestadorService.obtenerResumenDashboard());
    }

    @GetMapping("/mascotas/{id}")
    public ResponseEntity<MascotaConsolidadaDTO> obtenerDetalleMascota(@PathVariable Integer id) {
        return ResponseEntity.ok(orquestadorService.obtenerDetalleMascota(id));
    }

    @PostMapping(value = "/mascotas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> registrarMascotaCompleta(
            @RequestPart("mascota") String mascotaJson,
            @RequestPart("direccion") String direccion,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) {

        Object resultado = orquestadorService.registrarMascotaConUbicacion(mascotaJson, direccion, archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @DeleteMapping("/mascotas/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Integer id) {
        // Se mantiene porque elimina el registro en ambos microservicios
        orquestadorService.eliminarMascotaCompleta(id);
        return ResponseEntity.noContent().build();
    }
}