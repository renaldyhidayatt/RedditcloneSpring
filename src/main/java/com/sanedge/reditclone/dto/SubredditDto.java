package com.sanedge.reditclone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubredditDto {
    private Long id;
    private String name;
    private String description;
}
