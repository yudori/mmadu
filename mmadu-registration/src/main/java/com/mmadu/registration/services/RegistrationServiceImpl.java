package com.mmadu.registration.services;

import com.mmadu.registration.entities.RegistrationProfile;
import com.mmadu.registration.exceptions.UserFormValidationException;
import com.mmadu.registration.models.UserForm;
import com.mmadu.registration.models.UserModel;
import com.mmadu.registration.providers.UserFormConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {
    private MmaduUserServiceClient userServiceClient;
    private RegistrationProfileService registrationProfileService;
    private UserFormConverter userFormConverter;

    @Autowired
    public void setRegistrationProfileService(RegistrationProfileService registrationProfileService) {
        this.registrationProfileService = registrationProfileService;
    }

    @Autowired
    public void setUserServiceClient(MmaduUserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Autowired
    public void setUserFormConverter(UserFormConverter userFormConverter) {
        this.userFormConverter = userFormConverter;
    }

    @Override
    public void registerUser(String domainId, UserForm userForm) {
        if (userForm == null) {
            throw new IllegalArgumentException("user form cannot be null");
        }
        if (StringUtils.isEmpty(userForm.get("username").orElse(""))) {
            throw new UserFormValidationException("username cannot be empty");
        }

        RegistrationProfile profile = registrationProfileService.getProfileForDomain(domainId);
        UserModel model = userFormConverter.convertToUserProperties(domainId, userForm);
        if (!model.get("roles").isPresent()) {
            model.set("roles", profile.getDefaultRoles());
        }
        if (!model.get("authorities").isPresent()) {
            model.set("authorities", profile.getDefaultAuthorities());
        }
        if (!model.get("password").isPresent()) {
            model.set("password", "");
        }
        userServiceClient.addUsers(domainId, model.getProperties());
    }
}
