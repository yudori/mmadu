package com.mmadu.registration.providers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class DomainRegistrationFormFieldsManagerDirectoryInitializationTest {
    public static final String USER_HOME = System.getProperty("user.home");
    @Value("${mmadu.registration.templates}")
    private String templatesFolder;

    @Mock
    private FormFieldsGenerator formFieldsGenerator;
    @Mock
    private DomainService domainService;
    @InjectMocks
    private DomainRegistrationFormFieldsManager formFieldsManager = new DomainRegistrationFormFieldsManager();

    private static File file;

    @BeforeAll
    static void setUpClass() {
        file = new File(USER_HOME + "/mmadu-test/templates/domain");
    }

    @BeforeEach
    void setUp() {
        formFieldsManager.setTemplatesDirectoryResource(new FileSystemResource(USER_HOME + "/mmadu-test/templates"));
        doReturn(asList("1")).when(domainService).getDomainIds();
        doReturn("fields-1").when(formFieldsGenerator).generateFormFieldsForDomain("1");
    }

    @Test
    void onStartUpManagerShouldCreateNestedFolders() throws Exception {
        formFieldsManager.startMonitoring();
        assertThat(FileCopyUtils.copyToString(new FileReader(new File(file, "register-1.html"))),
                equalTo("fields-1"));
    }

    @AfterAll
    static void tearDown() {
        File file = new File(System.getProperty("user.home") + "/mmadu-test");
        file.delete();
    }

}