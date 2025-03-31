package dereck.angeles.controller;

import dereck.angeles.dto.TopicDto;
import dereck.angeles.model.Topic;
import dereck.angeles.repository.TopicRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;

@Path("/topics")
public class TopicController {

	@Inject
	TopicRepository topicRepository;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public List<TopicDto> getAllTopics() {
		List<Topic> topics = topicRepository.listAll();
		return topics.stream()
								 .map(this::toDto)
								 .collect(Collectors.toList());
	}

	private TopicDto toDto(Topic topic) {
		return new TopicDto(
					topic.getId(),
					topic.getName(),
					topic.getDescription(),
					topic.getCreatedAt(),
					topic.getUpdatedAt()
		);
	}
}