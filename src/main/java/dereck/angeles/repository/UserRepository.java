package dereck.angeles.repository;

import dereck.angeles.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

	@Inject
	EntityManager entityManager;

	public User findByEmail(String email) {
		List<User> users = entityManager
					.createQuery("SELECT u FROM User u WHERE u.email = :email",
											 User.class)
					.setParameter("email", email)
					.getResultList();
		return users.isEmpty() ? null : users.get(0);
	}

	public User findById(UUID id) {
		return entityManager.find(User.class, id);
	}

	public void persist(User user) {
		entityManager.persist(user);
	}
}