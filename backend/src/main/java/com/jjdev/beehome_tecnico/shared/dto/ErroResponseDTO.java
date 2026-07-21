package com.jjdev.beehome_tecnico.shared.dto;

import java.util.List;

public record ErroResponseDTO(String message, List<String> errors) {

    public ErroResponseDTO(String message) {
        this(message, null);
    }

}
