package telran.java45;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import telran.java45.accounting.dao.UserAccountRepository;
import telran.java45.accounting.dto.exceptions.UserExistsException;
import telran.java45.accounting.model.UserAccount;

@SpringBootTest
class UserAccountServiceImplTest {

	@MockBean
	UserAccountRepository repository;
	@MockBean
	ModelMapper modelMapper;
	@SpyBean
	PasswordEncoder passwordEncoder;
	@MockBean
	private UserAccount userAccount;
	private List<UserAccount> users;

	private List<UserAccount> createUsers() {
		UserAccount first = new UserAccount("john11", passwordEncoder.encode("1234"), "John", "Brzenk");
		UserAccount second = new UserAccount("mary12", passwordEncoder.encode("1234"), "Mary", "Vatson");
		UserAccount third = new UserAccount("peter13", passwordEncoder.encode("1234"), "Peter", "Brok");
		UserAccount fourth = new UserAccount("patricia14", passwordEncoder.encode("1234"), "Patricia", "Vatson");
		return new ArrayList<>(List.of(first, second, third, fourth));
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		users = createUsers();
	}

	@Test()
	void testAddUser() {
		userAccount = new UserAccount("alex01", passwordEncoder.encode("1234"), "Alex", "Kurd");
		when(repository.save(any(UserAccount.class))).thenThrow(new UserExistsException(userAccount.getLogin()));
		when(repository.save(any(UserAccount.class))).thenReturn(userAccount);
		when(passwordEncoder.matches("1234", userAccount.getPassword())).thenReturn(true);
		when(repository.save(userAccount)).thenReturn(userAccount);
		assertThat(userAccount.getFirstName()).isEqualTo("Alex");
		assertThat(userAccount.getLastName()).isEqualTo("Kurd");
		assertThat(userAccount.getLogin()).isEqualTo("alex01");
		UserAccount userAccountCopy = new UserAccount("alex01", passwordEncoder.encode("1234"), "Alex", "Kurd");
		assertThat(userAccountCopy).isEqualTo(userAccount);
		when(repository.save(null)).thenThrow(NullPointerException.class);

	}

	@Test
	void testGetUser() {
		Optional<UserAccount> user = Optional.of(users.get(0));
		String login = user.get().getLogin();
		when(repository.findById(login)).thenReturn(user);
	}

	@Test
	void testRemoveUser() {
		String id = users.get(0).getLogin();
		when(repository.findById(id)).thenReturn(Optional.of(users.get(0)));
		doNothing().when(repository).delete(any(UserAccount.class));
		repository.delete(userAccount);
		verify(repository, times(1)).delete(userAccount);
	}

	@Test
	void testEditUser() {
		UserAccount user = users.get(1);
		when(repository.findById(user.getLogin())).thenReturn(Optional.of(user));
		when(repository.save(any(UserAccount.class))).thenReturn(user);
		user.setFirstName("Maria");
		UserAccount changeUser = repository.save(user);
		assertThat(changeUser).isNotNull();
		assertThat("Maria").isEqualTo(changeUser.getFirstName());

	}

	@Test
	void testChangeRolesList_AddRole() {
		UserAccount user = users.get(3);
		when(repository.findById(user.getLogin())).thenReturn(Optional.of(user));
		assertThat(user).isNotNull();
		assertThat(user.getRoles().contains("USER")).isTrue();
		user.addRole("MODERATOR");
		when(repository.save(any(UserAccount.class))).thenReturn(user);
		UserAccount changeUser = repository.save(user);
		assertThat(changeUser).isNotNull();
		assertThat(changeUser.getRoles()).contains("MODERATOR");
	}

	@Test
	void testChangeRolesList_RemoveRole() {
		UserAccount user = users.get(3);
		when(repository.findById(user.getLogin())).thenReturn(Optional.of(user));
		assertThat(user).isNotNull();
		user.addRole("MODERATOR");
		when(repository.save(any(UserAccount.class))).thenReturn(user);
		UserAccount changeUser = repository.save(user);
		assertThat(changeUser).isNotNull();
		assertThat(changeUser.getRoles()).contains("MODERATOR");
		changeUser.removeRole("MODERATOR");
		repository.save(changeUser);
		assertThat(changeUser.getRoles()).doesNotContain("MODERATOR");
	}

	@Test
	void testChangePassword() {
		UserAccount user = users.get(3);
		when(repository.findById(user.getLogin())).thenReturn(Optional.of(user));
		assertThat(passwordEncoder.matches("1234", user.getPassword())).isTrue();
		when(repository.save(any(UserAccount.class))).thenReturn(user);
		user.setPassword(passwordEncoder.encode("1234qwer"));
		assertThat(passwordEncoder.matches("1234qwer", user.getPassword())).isTrue();
	}

}
