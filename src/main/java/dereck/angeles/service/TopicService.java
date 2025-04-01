package dereck.angeles.service;

import dereck.angeles.dto.TopicDto;
import dereck.angeles.model.Topic;
import dereck.angeles.repository.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The TopicService class handles business logic related to Language entities.
 * <p>
 * It communicates with the LanguageRepository to fetch Language entities and
 * converts them to LanguageDto objects for use in the API. This service is used
 * by LanguageController to retrieve language data while encapsulating the
 * entity-to-DTO mapping logic.
 * <p>
 * Example usage:
 * <ul><li>topicService.getTopics() to fetch all languages as DTOs.</li></ul>
 */
@ApplicationScoped
public class TopicService {
	private final TopicRepository topicRepository;

	@Inject
	public TopicService(TopicRepository topicRepository) {
		this.topicRepository = topicRepository;
	}

	@Transactional
	public List<TopicDto> getTopics() {
		List<Topic> topics = topicRepository.listAll();
		return topics.stream()
								 .map(this::mapToDto)
								 .toList();
	}

	private TopicDto mapToDto(Topic topic) {
		return new TopicDto(
					topic.getId(),
					topic.getName(),
					topic.getDescription(),
					topic.getCreatedAt(),
					topic.getUpdatedAt()
		);
	}
}
