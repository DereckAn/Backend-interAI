package dereck.angeles.service;

import dereck.angeles.model.Difficulty;
import dereck.angeles.model.Language;
import dereck.angeles.model.Topic;
import dereck.angeles.repository.DifficultyRepository;
import dereck.angeles.repository.LanguageRepository;
import dereck.angeles.repository.TopicRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class DataInitializationService {

    @Inject
    TopicRepository topicRepository;

    @Inject
    LanguageRepository languageRepository;

    @Inject
    DifficultyRepository difficultyRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        initializeTopics();
        initializeLanguages();
        initializeDifficulties();
    }

    private void initializeTopics() {
        List<TopicData> topicsData = Arrays.asList(
            new TopicData("Fullstack", "Questions about developing applications involving both frontend and backend, using technologies like React, Angular, Vue.js, Node.js, Django, and more."),
            new TopicData("Backend", "Questions about server-side development, including databases, APIs, frameworks like Spring Boot, Express.js, and languages like Java, Python, and PHP."),
            new TopicData("Frontend", "Questions about user interface development, using technologies like HTML, CSS, JavaScript, React, Angular, and Vue.js."),
            new TopicData("DevOps", "Questions about continuous integration and deployment, infrastructure management, tools like Docker, Kubernetes, Jenkins, and DevOps practices."),
            new TopicData("Data Science", "Questions about data analysis, statistics, data visualization, using tools like Python, R, and data analysis platforms."),
            new TopicData("Mobile", "Questions about mobile application development, using technologies like Android, iOS, Flutter, and React Native."),
            new TopicData("Machine Learning", "Questions about machine learning algorithms, data modeling, using frameworks like TensorFlow, PyTorch, and Scikit-learn."),
            new TopicData("Algorithms", "Questions about algorithm design and analysis, data structures, and competitive programming problems."),
            new TopicData("System Design", "Questions about designing scalable systems, software architecture, distributed databases, and microservices design."),
            new TopicData("Testing", "Questions about software testing, types of testing (unit, integration, acceptance), testing tools, and testing strategies."),
            new TopicData("Cyber Security", "Questions about computer security, data protection, attack prevention, security tools, and security policies."),
            new TopicData("Cloud Computing", "Questions about cloud computing, AWS, Azure, Google Cloud services, and cloud deployment practices."),
            new TopicData("Blockchain", "Questions about blockchain technology, cryptocurrencies, smart contracts, and decentralized applications."),
            new TopicData("IoT", "Questions about Internet of Things, connected devices, device communication, and IoT platforms."),
            new TopicData("AR/VR", "Questions about augmented and virtual reality, AR/VR application development, and technologies like Unity and Unreal Engine."),
            new TopicData("Quantum Computing", "Questions about quantum computing, quantum algorithms, and quantum computing applications."),
            new TopicData("Game Development", "Questions about video game development, game engines like Unity and Unreal Engine, game design, and game programming.")
        );

        for (TopicData topicData : topicsData) {
            if (topicRepository.findByName(topicData.name) == null) {
                Topic topic = new Topic();
                topic.setName(topicData.name);
                topic.setDescription(topicData.description);
                topic.setCreatedAt(Instant.now());
                topic.setUpdatedAt(Instant.now());
                topicRepository.persist(topic);
                System.out.println("Created topic: " + topicData.name);
            }
        }
    }

    private void initializeLanguages() {
        List<String> languages = Arrays.asList(
            "Java", "Python", "JavaScript", "Ruby", "C#", "PHP", 
            "Go", "Rust", "Swift", "Kotlin", "TypeScript", "Scala"
        );

        for (String languageName : languages) {
            if (languageRepository.findByName(languageName) == null) {
                Language language = new Language();
                language.setName(languageName);
                language.setCreatedAt(Instant.now());
                language.setUpdatedAt(Instant.now());
                languageRepository.persist(language);
                System.out.println("Created language: " + languageName);
            }
        }
    }

    private void initializeDifficulties() {
        List<DifficultyData> difficultiesData = Arrays.asList(
            new DifficultyData(Difficulty.DifficultyLevel.Junior, "Basic and fundamental questions for beginner developers. Ideal for those starting in the field and needing to build a solid foundation."),
            new DifficultyData(Difficulty.DifficultyLevel.MidLevel, "Intermediate questions requiring deeper knowledge of concepts and tools. Ideal for developers with experience in practical projects."),
            new DifficultyData(Difficulty.DifficultyLevel.Senior, "Advanced and complex questions for field experts. Ideal for those with years of experience seeking more challenging technical problems.")
        );

        for (DifficultyData difficultyData : difficultiesData) {
            // Convert enum to string for the repository method
            String levelString = difficultyData.level == Difficulty.DifficultyLevel.MidLevel ? "Mid-Level" : difficultyData.level.name();
            if (difficultyRepository.findByLevel(levelString) == null) {
                Difficulty difficulty = new Difficulty();
                difficulty.setLevel(difficultyData.level);
                difficulty.setDescription(difficultyData.description);
                difficultyRepository.persist(difficulty);
                System.out.println("Created difficulty: " + levelString);
            }
        }
    }

    // Helper classes for data structure
    private static class TopicData {
        final String name;
        final String description;

        TopicData(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private static class DifficultyData {
        final Difficulty.DifficultyLevel level;
        final String description;

        DifficultyData(Difficulty.DifficultyLevel level, String description) {
            this.level = level;
            this.description = description;
        }
    }
}