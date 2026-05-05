package com.sanosysalvos.bff_gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;
import com.sanosysalvos.bff_gateway.dto.MascotaDTO;
import com.sanosysalvos.bff_gateway.dto.UbicacionDTO;

@Service
public class OrquestadorService {

    private static final Logger log = LoggerFactory.getLogger(OrquestadorService.class);

    @Value("${microservicio.mascotas.url}")
    private String mascotasUrl;

    @Value("${microservicio.geolocalizacion.url}")
    private String geolocalizacionUrl;

    private final RestClient restClient;

    public OrquestadorService() {
        this.restClient = RestClient.create();
    }

    public List<MascotaConsolidadaDTO> obtenerResumenDashboard() {
        List<MascotaDTO> mascotas;
        List<UbicacionDTO> geolocalizaciones = new ArrayList<>();

        try {
            mascotas = restClient.get()
                    .uri(mascotasUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MascotaDTO>>() {
                    });
        } catch (Exception e) {
            log.error("⚠️ ERROR CRÍTICO: Falló conexión con MS Mascotas. Motivo: {}", e.getMessage());
            return new ArrayList<>();
        }

        try {
            List<UbicacionDTO> response = restClient.get()
                    .uri(geolocalizacionUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UbicacionDTO>>() {
                    });
            if (response != null) {
                geolocalizaciones = response;
            }
        } catch (Exception e) {
            log.warn("⚠️ ADVERTENCIA: No se pudo obtener geolocalización. El dashboard se mostrará sin coordenadas.");
        }

        Map<Integer, UbicacionDTO> ubicacionMap = geolocalizaciones.stream()
                .collect(Collectors.toMap(UbicacionDTO::getMascotaId, u -> u, (u1, u2) -> u1));

        if (mascotas == null) {
            return new ArrayList<>();
        }

        return mascotas.stream()
                .map(mascota -> {
                    UbicacionDTO ubicacion = ubicacionMap.get(mascota.getId());
                    return MascotaConsolidadaDTO.builder()
                            .idMascota(mascota.getId())
                            .nombre(mascota.getNombre())
                            .raza(mascota.getRaza())
                            .estado(mascota.getEstado())
                            .latitud(ubicacion != null ? ubicacion.getLatitud() : null)
                            .longitud(ubicacion != null ? ubicacion.getLongitud() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
