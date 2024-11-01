package mizdooni.controller;

import mizdooni.controllers.AuthenticationController;
import mizdooni.exceptions.DuplicatedUsernameEmail;
import mizdooni.exceptions.InvalidEmailFormat;
import mizdooni.exceptions.InvalidUsernameFormat;
import mizdooni.model.Address;
import mizdooni.model.User;
import mizdooni.response.Response;
import mizdooni.response.ResponseException;
import mizdooni.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import static mizdooni.controllers.ControllerUtils.PARAMS_BAD_TYPE;
import static mizdooni.controllers.ControllerUtils.PARAMS_MISSING;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestAuthenticationController {
    @Mock
    private UserService userService;
    @InjectMocks
    private AuthenticationController authenticationController;

    private Map<String, Object> authParams = new HashMap<>();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        authParams.put("username", "testUser");
        authParams.put("password", "testPassword");
        authParams.put("email", "test@example.com");
        authParams.put("role", "client");
        Map<String, String> address = new HashMap<>();
        address.put("country", "TestCountry");
        address.put("city", "TestCity");
        authParams.put("address", address);
    }

    @Test
    public void noUserExists_tryToSignupWithMissingAuthParam_failedToSignup() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        authParams.remove("email");
        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> authenticationController.signup(authParams),
                "Auth params are missed so you cannot signup and we expect an exception!"
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_MISSING, exception.getMessage());

        verify(userService, never()).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }


    @Test
    public void noUserExists_tryToSignupWithInvalidRole_failedToSignupDueToBadType() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        authParams.put("role", "RandomWrongRole");

        ResponseException exception = assertThrows(
                ResponseException.class,
                () -> authenticationController.signup(authParams),
                "Auth params are wrong it type, so you cannot signup and we expect an exception!"
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(PARAMS_BAD_TYPE, exception.getMessage());

        verify(userService, never()).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }

    @Test
    public void noUserExists_tryToSignupWithCorrectParams_signedUpSuccessfully() throws DuplicatedUsernameEmail, InvalidUsernameFormat, InvalidEmailFormat {
        // Expectations :
        User mockUser = new User("testUser", "testPassword", "test@example.com", new Address("TestCountry", "TestCity", null), User.Role.client);
        doNothing().when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        when(userService.login("testUser", "testPassword")).thenReturn(true);
        when(userService.getCurrentUser()).thenReturn(mockUser);
        // _____

        Response response = authenticationController.signup(authParams);

        assertEquals("signup successful", response.getMessage());
        assertEquals(mockUser, response.getData());
        verify(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
        verify(userService).login("testUser", "testPassword");

    }

    @Test
    public void userExists_tryToSignupWithSameEmailAddress_failed() throws Exception{
        doThrow(new DuplicatedUsernameEmail()).when(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));

        ResponseException exception = assertThrows(ResponseException.class, () -> authenticationController.signup(authParams));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userService).signup(anyString(), anyString(), anyString(), any(Address.class), any(User.Role.class));
    }


}
