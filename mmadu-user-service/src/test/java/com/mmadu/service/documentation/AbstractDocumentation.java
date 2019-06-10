package com.mmadu.service.documentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mmadu.service.config.MongoInitializationConfig;
import com.mmadu.service.entities.AppDomain;
import com.mmadu.service.entities.AppUser;
import com.mmadu.service.providers.UniqueUserIdGenerator;
import com.mmadu.service.repositories.AppDomainRepository;
import com.mmadu.service.repositories.AppUserRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@RunWith(SpringRunner.class)
@Import({
        MongoInitializationConfig.class,
        AbstractDocumentation.SerializationConfig.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractDocumentation {
    public static final String ROOT_DOC_FOLDER = "../docs/apis/snippets";
    public static final String DOCUMENTATION_NAME = "{class-name}/{method-name}/{step}/";
    public static final String TEST_USER_ID = "12345ABCDEF";
    public static final String USER_DOMAIN_ID = "test-app";
    public static final String USER_PASSWORD = "my-password";
    public static final String USERNAME = "test-user";
    public static final String TEST_AUTHORITY = "admin";
    public static final String TEST_ROLE = "admin-role";
    private static final String DOMAIN_NAME = "test";
    public static final String DOMAIN_TOKEN = "1234";
    public static final String USER_EXTERNAL_ID = "123453432";
    protected final String ADMIN_TOKEN = "2222";

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(ROOT_DOC_FOLDER);

    @Autowired
    private WebApplicationContext context;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected AppUserRepository appUserRepository;
    @Autowired
    protected AppDomainRepository appDomainRepository;

    protected MockMvc mockMvc;

    @Before
    public void initializeTest() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document(DOCUMENTATION_NAME,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()))).build();
    }

    protected final String objectToString(Object object) throws JsonProcessingException {
        return objectMapper
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                        object
                );
    }

    protected AppUser createAppUserWithConstantId() {
        AppUser user = new AppUser();
        user.setDomainId(USER_DOMAIN_ID);
        user.setPassword(USER_PASSWORD);
        user.setUsername(USERNAME);
        user.setId(TEST_USER_ID);
        user.addAuthorities(TEST_AUTHORITY);
        user.addRoles(TEST_ROLE);
        user.setExternalId(USER_EXTERNAL_ID);
        user.set("country", "Nigeria");
        user.set("favourite-colour", "blue");
        return user;
    }

    protected AppUser createAppUserWithIndex(int index) {
        AppUser user = new AppUser();
        user.setDomainId(USER_DOMAIN_ID);
        user.setPassword(USER_PASSWORD + index);
        user.setUsername(USERNAME + index);
        user.setId(TEST_USER_ID + index);
        user.addAuthorities(TEST_AUTHORITY);
        user.addRoles(TEST_ROLE);
        user.setExternalId(USER_EXTERNAL_ID);
        user.set("country", "Nigeria");
        user.set("favourite-color", "blue");
        return user;
    }

    protected List<AppUser> createMultipleUsers(int size) {
        List<AppUser> appUsers = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            appUsers.add(createAppUserWithIndex(i));
        }
        return appUsers;
    }

    protected AppDomain createDomain() {
        AppDomain domain = new AppDomain();
        domain.setName(DOMAIN_NAME);
        domain.setId(USER_DOMAIN_ID);
        return domain;
    }

    @Configuration
    public static class SerializationConfig{
        @Bean
        public ObjectMapper objectMapper(){
            return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        }
    }
}
