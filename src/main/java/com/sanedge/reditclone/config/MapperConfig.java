package com.sanedge.reditclone.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.sanedge.reditclone.mapper.SubredditMapper;

@Configuration
public class MapperConfig {
    @Bean
    @Primary
    public SubredditMapper subredditMapper() {
        return Mappers.getMapper(SubredditMapper.class);
    }
}
