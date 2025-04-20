package dereck.angeles.controller;

import dereck.angeles.dto.LoginDto;
import dereck.angeles.dto.RegisterDto;
import dereck.angeles.model.User;
import dereck.angeles.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

	@Inject
	AuthService authService;

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
			String token = authService.login(loginDto);
			// Set JWT in an HTTP-only cookie
			NewCookie cookie = new NewCookie(
						"jwt", token,
						"/", null,
						"Authentication token",
						24 * 60 * 60, // 24 hours in seconds
						false, // Not secure (set to true in production with HTTPS)
						true   // HTTP-only
			);
			return Response.ok(new SuccessResponse("Login successful"))
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
	public Response verifyToken(@CookieParam("jwt") String token) {
		try {
			if (token == null) {
				return Response.status(Response.Status.UNAUTHORIZED)
											 .entity(new ErrorResponse("No token provided"))
											 .build();
			}
			// Quarkus automatically verifies the JWT token via mp.jwt.verify settings
			// If the token is valid, return a success response
			return Response.ok(new SuccessResponse("Token is valid")).build();
		} catch (Exception e) {
			return Response.status(Response.Status.UNAUTHORIZED)
										 .entity(new ErrorResponse("Invalid token"))
										 .build();
		}
	}

	// Helper classes for response
	static class ErrorResponse {
		private String message;

		public ErrorResponse(String message) {
			this.message = message;
		}

		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
	}

	static class SuccessResponse {
		private String message;

		public SuccessResponse(String message) {
			this.message = message;
		}

		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
	}
}