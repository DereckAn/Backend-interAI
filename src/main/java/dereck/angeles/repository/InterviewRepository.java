package dereck.angeles.repository;

import dereck.angeles.model.Interview;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InterviewRepository {

    @Inject
    EntityManager entityManager;

    public Interview findById(UUID id) {
        return entityManager.find(Interview.class, id);
    }

    public void persist(Interview interview) {
        entityManager.persist(interview);
    }

    public List<Interview> findByUserId(UUID userId) {
        return entityManager
                .createQuery("SELECT i FROM Interview i WHERE i.user.id = :userId", Interview.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Interview> findAll() {
        return entityManager
                .createQuery("SELECT i FROM Interview i", Interview.class)
                .getResultList();
    }
}
