package dereck.angeles.service;

import dereck.angeles.dto.LanguageDto;
import dereck.angeles.model.Language;
import dereck.angeles.repository.LanguageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * The LanguageService class handles business logic related to Language entities.
 * <p>
 * It communicates with the LanguageRepository to fetch Language entities and converts them to
 * LanguageDto objects for use in the API. This service is used by LanguageController to retrieve
 * language data while encapsulating the entity-to-DTO mapping logic.
 * <p>
 * Example usage:
 * <ul><li>languageService.getLanguages() to fetch all languages as DTOs.</li></ul>
 */
@ApplicationScoped
public class LanguageService {

	private final LanguageRepository languageRepository;

	@Inject
	public LanguageService(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}

	@Transactional
	public List<LanguageDto> getLanguages() {
		List<Language> languages = languageRepository.listAll();
		return languages.stream()
										.map(this::mapToDto)
										.toList();
	}

	private LanguageDto mapToDto(Language language) {
		return new LanguageDto(
					language.getId(),
					language.getName(),
					language.getCreatedAt(),
					language.getUpdatedAt()
		);
	}
}