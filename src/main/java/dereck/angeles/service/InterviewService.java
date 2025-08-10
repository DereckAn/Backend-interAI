package dereck.angeles.service;

import dereck.angeles.dto.InterviewDto;
import dereck.angeles.model.*;
import dereck.angeles.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class InterviewService {

    @Inject
    InterviewRepository interviewRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TopicRepository topicRepository;

    @Inject
    LanguageRepository languageRepository;

    @Inject
    DifficultyRepository difficultyRepository;

    @Transactional
    public Interview createInterview(InterviewDto interviewDto) {
        // Get referenced entities
        User user = userRepository.findById(UUID.fromString(interviewDto.userId()));
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Topic topic = topicRepository.findById(UUID.fromString(interviewDto.topicId()));
        if (topic == null) {
            throw new RuntimeException("Topic not found: " + interviewDto.topicId());
        }

        Language language = languageRepository.findById(UUID.fromString(interviewDto.languageId()));
        if (language == null) {
            throw new RuntimeException("Language not found: " + interviewDto.languageId());
        }

        Difficulty difficulty = difficultyRepository.findByLevel(interviewDto.difficultyLevel());
        if (difficulty == null) {
            throw new RuntimeException("Difficulty not found: " + interviewDto.difficultyLevel());
        }

        // Create interview entity
        Interview interview = new Interview();
        interview.setUser(user);
        interview.setTopic(topic);
        interview.setLanguage(language);
        interview.setDifficulty(difficulty);
        interview.setJobDescription(interviewDto.jobDescription());
        interview.setExperienceYears(interviewDto.experienceYears());
        interview.setStartTime(Instant.now());
        interview.setStatus("in_progress");

        interviewRepository.persist(interview);
        return interview;
    }

    public Interview findById(UUID id) {
        return interviewRepository.findById(id);
    }
}