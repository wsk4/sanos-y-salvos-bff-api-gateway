package com.sanosysalvos.bff_gateway.controller;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;
import com.sanosysalvos.bff_gateway.service.OrquestadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bff/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") 
public class BffController {

    private final OrquestadorService orquestadorService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<MascotaConsolidadaDTO>> cargarDashboardPrincipal() {
        List<MascotaConsolidadaDTO> consolidado = orquestadorService.obtenerResumenDashboard();
        return ResponseEntity.ok(consolidado);
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

    @PutMapping(value = "/mascotas/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> actualizarMascota(
            @PathVariable Integer id,
            @RequestPart("mascota") String mascotaJson,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(orquestadorService.actualizarMascota(id, mascotaJson, archivo));
    }

    @DeleteMapping("/mascotas/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Integer id) {
        orquestadorService.eliminarMascotaCompleta(id);
        return ResponseEntity.noContent().build();
    }
}