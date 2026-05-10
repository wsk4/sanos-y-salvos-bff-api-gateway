package com.sanosysalvos.bff_gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.bff_gateway.dto.MascotaConsolidadaDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrquestadorService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Value("${microservicio.mascotas.url}")
    private String mascotasUrl;

    @Value("${microservicio.geolocalizacion.url}")
    private String geoUrl;

    public Object registrarMascotaConUbicacion(String mascotaJson, String direccion, MultipartFile archivo) {

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> mascotaPart = new HttpEntity<>(mascotaJson, jsonHeaders);

        MultiValueMap<String, Object> bodyMascota = new LinkedMultiValueMap<>();
        bodyMascota.add("mascota", mascotaPart);

        if (archivo != null) {
            bodyMascota.add("archivo", archivo.getResource());
        }

        String respuestaMascotaStr = restClient.post()
                .uri(mascotasUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyMascota)
                .retrieve()
                .body(String.class);

        try {
            JsonNode mascotaNode = objectMapper.readTree(respuestaMascotaStr);
            Integer mascotaId = mascotaNode.get("id").asInt();

            Map<String, Object> bodyGeo = new HashMap<>();
            bodyGeo.put("mascotaId", mascotaId);
            bodyGeo.put("direccion", direccion);
            bodyGeo.put("radioBusqueda", 5.0);

            Object respuestaGeo = restClient.post()
                    .uri(geoUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bodyGeo)
                    .retrieve()
                    .body(Object.class);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("mascota", mascotaNode);
            finalResponse.put("ubicacion", respuestaGeo);
            return finalResponse;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error procesando respuesta del microservicio", e);
        }
    }

    public List<MascotaConsolidadaDTO> obtenerResumenDashboard() {
        try {
            String respuestaMascotas = restClient.get()
                    .uri(mascotasUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode mascotas = objectMapper.readTree(respuestaMascotas);

            String respuestaGeo = restClient.get()
                    .uri(geoUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode ubicaciones = objectMapper.readTree(respuestaGeo);

            Map<Integer, JsonNode> ubiPorMascota = new HashMap<>();
            for (JsonNode u : ubicaciones) {
                ubiPorMascota.put(u.get("mascotaId").asInt(), u);
            }

            List<MascotaConsolidadaDTO> resultado = new java.util.ArrayList<>();
            for (JsonNode m : mascotas) {
                Integer id = m.get("id").asInt();
                JsonNode ubi = ubiPorMascota.get(id);

                MascotaConsolidadaDTO dto = MascotaConsolidadaDTO.builder()
                        .idMascota(id)
                        .nombre(m.has("nombre") ? m.get("nombre").asText() : null)
                        .raza(m.has("raza") ? m.get("raza").asText() : null)
                        .estado(m.has("estado") ? m.get("estado").asText() : null)
                        .latitud(ubi != null && ubi.has("latitud") ? ubi.get("latitud").asDouble() : null)
                        .longitud(ubi != null && ubi.has("longitud") ? ubi.get("longitud").asDouble() : null)
                        .build();

                resultado.add(dto);
            }

            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener resumen del dashboard", e);
        }
    }

    public MascotaConsolidadaDTO obtenerDetalleMascota(Integer id) {
        return MascotaConsolidadaDTO.builder().idMascota(id).build();
    }

    public Object actualizarMascota(Integer id, String mascotaJson, MultipartFile archivo) {

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> mascotaPart = new HttpEntity<>(mascotaJson, jsonHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("mascota", mascotaPart);

        if (archivo != null) {
            body.add("archivo", archivo.getResource());
        }

        return restClient.patch()
                .uri(mascotasUrl + "/" + id)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    public void eliminarMascotaCompleta(Integer id) {
        restClient.delete().uri(mascotasUrl + "/" + id).retrieve().toBodilessEntity();
        try {
            restClient.delete().uri(geoUrl + "/mascota/" + id).retrieve().toBodilessEntity();
        } catch (Exception ignored) {
        }
    }
}
