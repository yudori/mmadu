package com.mmadu.registration.providers;

import com.mmadu.registration.models.RegistrationFieldModifiedEvent;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DomainRegistrationFormFieldsManageTest {
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
    public static void setUpClass() {
        file = new File(USER_HOME + "/mmadu-test/templates/domain");
        file.mkdirs();
    }

    @BeforeEach
    void setUp() {
        formFieldsManager.setTemplatesDirectoryResource(new FileSystemResource(USER_HOME + "/mmadu-test/templates"));
        lenient().doReturn(asList("1", "2", "3")).when(domainService).getDomainIds();
        lenient().doReturn("fields-1").when(formFieldsGenerator).generateFormFieldsForDomain("1");
        lenient().doReturn("fields-2").when(formFieldsGenerator).generateFormFieldsForDomain("2");
        lenient().doReturn("fields-3").when(formFieldsGenerator).generateFormFieldsForDomain("3");
    }

    @Test
    void onStartUpManagerShouldGenerateAllFormFieldFragments() throws Exception {
        formFieldsManager.startMonitoring();
        assertThat(FileCopyUtils.copyToString(new FileReader(new File(file, "register-1.html"))),
                equalTo("fields-1"));
        assertThat(FileCopyUtils.copyToString(new FileReader(new File(file, "register-2.html"))),
                equalTo("fields-2"));
        assertThat(FileCopyUtils.copyToString(new FileReader(new File(file, "register-3.html"))),
                equalTo("fields-3"));
    }

    @Test
    void testFieldChangeListening() throws Exception {
        DomainRegistrationFormFieldsManager spyManager = spy(formFieldsManager);
        spyManager.subscribeToEvent();
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        Thread.sleep(100);
        verify(spyManager, times(1))
                .handleEvent(eq(new RegistrationFieldModifiedEvent("1")));
    }

    @Test
    void testDebounceOfEvents() throws Exception {
        DomainRegistrationFormFieldsManager spyManager = spy(formFieldsManager);
        spyManager.subscribeToEvent();
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("1"));
        spyManager.sendEventToProcessor(new RegistrationFieldModifiedEvent("2"));
        Thread.sleep(100);
        verify(spyManager, times(1))
                .handleEvent(eq(new RegistrationFieldModifiedEvent("1")));
        verify(spyManager, times(1))
                .handleEvent(eq(new RegistrationFieldModifiedEvent("2")));
    }

    @AfterAll
    public static void tearDown() {
        File file = new File(System.getProperty("user.home") + "/mmadu-test/domain");
        file.delete();
    }

}