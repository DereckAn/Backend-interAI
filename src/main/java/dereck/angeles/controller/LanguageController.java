package dereck.angeles.controller;

import dereck.angeles.dto.LanguageDto;
import dereck.angeles.service.LanguageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * The LanguageController class handles HTTP requests related to Language entities.
 * <p>
 * It delegates business logic to the LanguageService and returns LanguageDto objects in the
 * API responses.
 */
@Path("/languages")
public class LanguageController {

	private final LanguageService languageService;

	@Inject
	public LanguageController(LanguageService languageService) {
		this.languageService = languageService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<LanguageDto> getLanguages() {
		return languageService.getLanguages();
	}
}