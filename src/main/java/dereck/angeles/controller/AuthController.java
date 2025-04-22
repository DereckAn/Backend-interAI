package dereck.angeles.controller;

import dereck.angeles.dto.LoginDto;
import dereck.angeles.dto.RegisterDto;
import dereck.angeles.model.User;
import dereck.angeles.service.AuthService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

	@Inject
	AuthService authService;

	@Inject
	SecurityIdentity securityIdentity;

	@POST
	@Path("/register")
	public Response register(RegisterDto registerDto) {
		try {
			User user = authService.register(registerDto);
			return Response.status(Response.Status.CREATED)
										 .entity(user)
										 .build();
		} catch (Exception e) {
			return Response.status(Response.Status.BAD_REQUEST)
										 .entity(new ErrorResponse(e.getMessage()))
										 .build();
		}
	}

	@POST
	@Path("/login")
	public Response login(LoginDto loginDto) {
		try {
			AuthService.LoginResponse loginResponse = authService.login(loginDto);
			NewCookie cookie = new NewCookie(
						"jwt", loginResponse.getToken(),
						"/", null,
						"Authentication token",
						24 * 60 * 60, // 24 hours
						true,  // Secure: true for HTTPS
						true   // HttpOnly: true
			);
			return Response.ok(new SuccessResponse("Login successful",
																						 loginResponse.getUserId()))
										 .cookie(cookie)
										 .build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED)
										 .entity(new ErrorResponse(e.getMessage()))
										 .build();
		}
	}

	@GET
	@Path("/verify")
	@Authenticated
	public Response verifyToken() {
		try {
			// If the token is valid, SecurityIdentity will be populated
			if (securityIdentity.isAnonymous()) {
				return Response.status(Response.Status.UNAUTHORIZED)
											 .entity(new ErrorResponse(
														 "No token provided or invalid token"))
											 .build();
			}
			return Response.ok(new SuccessResponse("Token is valid")).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED)
										 .entity(new ErrorResponse("Invalid token"))
										 .build();
		}
	}

	@POST
	@Path("/logout")
	public Response logout() {
		NewCookie cookie = new NewCookie(
					"jwt", null,
					"/", null,
					"Authentication token",
					0,
					false,
					true
		);
		return Response.ok(new SuccessResponse("Logout successful"))
									 .cookie(cookie)
									 .build();
	}

	@GET
	@Path("/me")
	@Authenticated
	public Response getCurrentUser() {
		try {
			// Check if the user is authenticated
			if (securityIdentity.isAnonymous()) {
				return Response.status(Response.Status.UNAUTHORIZED)
											 .entity(new ErrorResponse(
														 "No token provided or invalid token"))
											 .build();
			}

			// Extract userId from the JWT's subject
			String userId = securityIdentity.getPrincipal()
																			.getName(); // The subject claim
			if (userId == null) {
				return Response.status(Response.Status.UNAUTHORIZED)
											 .entity(new ErrorResponse(
														 "Invalid token: userId not found"))
											 .build();
			}

			// Fetch the user from the database
			User user = authService.getUserById(UUID.fromString(userId));
			if (user == null) {
				return Response.status(Response.Status.UNAUTHORIZED)
											 .entity(new ErrorResponse("User not found"))
											 .build();
			}

			// Return user data
			return Response
						.ok(new UserResponse(user.getName(), user.getRole().toString()))
						.build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
										 .entity(new ErrorResponse(
													 "An error occurred: " + e.getMessage()))
										 .build();
		}
	}

	// Helper classes for response
	@Setter
	@Getter
	static class ErrorResponse {
		private String message;

		public ErrorResponse(String message) {
			this.message = message;
		}

	}

	@Setter
	@Getter
	static class SuccessResponse {
		private String message;
		private String userId;
//		private String token;

		public SuccessResponse(String message) {
			this.message = message;
		}

		public SuccessResponse(String message, String userId) {
			this.message = message;
			this.userId = userId;
//			this.token = token;
		}

	}

	@Setter
	@Getter
	static class UserResponse {
		private String name;
		private String role;

		public UserResponse(String name, String role) {
			this.name = name;
			this.role = role;
		}

	}
}