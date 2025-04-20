package dereck.angeles.repository;

import dereck.angeles.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class UserRepository {

	@PersistenceContext
	EntityManager entityManager;

	public User findByEmail(String email) {
		return entityManager
					.createQuery("SELECT u FROM User u WHERE u.email = :email",
											 User.class)
					.setParameter("email", email)
					.getResultStream()
					.findFirst()
					.orElse(null);
	}

	public User findByUsername(String username) {
		return entityManager
					.createQuery("SELECT u FROM User u WHERE u.username = :username",
											 User.class)
					.setParameter("username", username)
					.getResultStream()
					.findFirst()
					.orElse(null);
	}

	public void persist(User user) {
		entityManager.persist(user);
	}
}