package com.opinai.mapper;

import com.opinai.controller.dto.RegisterRequest;
import com.opinai.controller.dto.UserSummaryDto;
import com.opinai.model.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

    UserSummaryDto toUserSummaryDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Se encripta y setea en el Service
    @Mapping(target = "role", ignore = true)         // Se asigna por defecto en el Service
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest registerRequest);
}
