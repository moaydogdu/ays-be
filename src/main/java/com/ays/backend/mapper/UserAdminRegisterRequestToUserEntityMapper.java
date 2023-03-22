package com.ays.backend.mapper;

import com.ays.backend.user.controller.payload.request.AdminRegisterRequest;
import com.ays.backend.user.model.entities.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserAdminRegisterRequestToUserEntityMapper extends BaseMapper<AdminRegisterRequest, User> {

}