package dereck.angeles.repository;

import dereck.angeles.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

	@Inject
	EntityManager entityManager;

	public User findByEmail(String email) {
		return entityManager
					.createQuery("SELECT u FROM User u WHERE u.email = :email",
											 User.class)
					.setParameter("email", email)
					.getSingleResult();
	}

	public User findByUsername(String username) {
		return entityManager
					.createQuery("SELECT u FROM User u WHERE u.username = :username",
											 User.class)
					.setParameter("username", username)
					.getSingleResult();
	}

	public User findById(UUID id) {
		return entityManager.find(User.class, id);
	}

	public void persist(User user) {
		entityManager.persist(user);
	}
}