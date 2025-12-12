package com.example.vibecoing2.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Integer x;
    private Integer y;
    private String unit = "px";
    private String alignment;

    public Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.unit = "px";
    }
}
