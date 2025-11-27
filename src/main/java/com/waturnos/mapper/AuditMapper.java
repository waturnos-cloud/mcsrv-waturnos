package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Context;

import com.waturnos.dto.response.AuditDTO;
import com.waturnos.entity.Audit;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mappings({
        @Mapping(target = "ip", source = "ipAddress"),
        @Mapping(target = "eventLabel", expression = "java(labelResolver.labelFor(source.getEvent()))")
    })
    AuditDTO toDto(Audit source, @Context AuditLabelResolver labelResolver);

    List<AuditDTO> toDtoList(List<Audit> entities, @Context AuditLabelResolver labelResolver);
}
