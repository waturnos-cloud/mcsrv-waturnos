package com.waturnos.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Provider;
import com.waturnos.entity.ProviderOrganization;
import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.ProviderService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.UserProcess;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {


}
