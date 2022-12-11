package com.bluebox.user;

import com.bluebox.api.registeration.RegistrationController.RegistrationReq;
import com.bluebox.service.mail.EmailException;
import com.bluebox.service.mail.MailService;
import com.bluebox.service.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bluebox.Constants.REGISTRATION_BASE;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("user")
@AutoConfigureMockMvc
class UserRegistrationTest {
    @Autowired
    private UserRepository repository;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MailService mailSender;
    ArgumentCaptor<String> lastMailContent = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> lastMailToAdd = ArgumentCaptor.forClass(String.class);


    @BeforeEach
    public void before() throws EmailException {
        repository.deleteAll();
        Mockito.doNothing().when(mailSender).sendEmail(lastMailToAdd.capture(), any(), any(), lastMailContent.capture());
    }


    @SneakyThrows
    @Test
    void nullFirstName_shouldReturnError() {
        var req = new RegistrationReq();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName", is("firstName is required")));
    }

    @SneakyThrows
    @Test
    void nullLastName_shouldReturnError() {
        var req = new RegistrationReq();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName", is("lastName is required")));
    }

    @SneakyThrows
    @Test
    void invalidEmail_shouldReturnError() {
        var req = new RegistrationReq();
        req.setEmail("invalid-email");
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is("valid email is required")));
    }

    @SneakyThrows
    @Test
    void invalidPhone_shouldReturnError() {
        var req = new RegistrationReq();
        req.setPhone("871673523");
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone", is("international phone is required")));
    }

    @SneakyThrows
    @Test
    void correctUser_shouldReceiveEmail() {
        var req = createUser();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isOk());
        assertEquals(req.getEmail(), lastMailToAdd.getValue());
    }

    @SneakyThrows
    @Test
    void correctUser_couldVerify() {
        var req = createUser();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isOk());
        var content = (lastMailContent.getValue());
        assertNotNull(content);
        Pattern pattern = Pattern.compile("^.*href=\"([^\"]*)\".*$");
        Matcher matcher = pattern.matcher(content);
        assertTrue(matcher.find());
        var url = matcher.group(1);
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }


    @SneakyThrows
    @Test
    void correctUser_shouldWork() {
        var req = createUser();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("harry-potter@hagwartz.com")))
                .andExpect(jsonPath("$.firstName", is("harry")))
                .andExpect(jsonPath("$.lastName", is("potter")))
                .andExpect(jsonPath("$.phone", is("+4912345678901")))
                .andExpect(jsonPath("$.uuid", Matchers.notNullValue()))
                .andExpect(jsonPath("$.enabled", is(false)))
                .andExpect(jsonPath("$.deleted", is(false)));
    }

    @SneakyThrows
    @Test
    void duplicateUser_shouldReturnError() {
        repository.save(new UserBuilder().email("harry-potter@hagwartz.com").firstName("harry").lastName("potter").build());
        var req = createUser();
        mockMvc.perform(post(REGISTRATION_BASE)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(toJson(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("User already exists")));
    }

    @SneakyThrows
    private String toJson(RegistrationReq req) {
        return new ObjectMapper().writeValueAsString(req);
    }

    @NotNull
    private static RegistrationReq createUser() {
        var req = new RegistrationReq();
        req.setEmail("harry-potter@hagwartz.com");
        req.setFirstName("harry");
        req.setLastName("potter");
        req.setPhone("+4912345678901");
        return req;
    }
}
