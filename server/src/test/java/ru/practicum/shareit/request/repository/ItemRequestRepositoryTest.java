package ru.practicum.shareit.request.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Поиск запросов пользователя по убыванию даты создания")
    void findByRequestorIdOrderByCreatedDesc_shouldReturnRequestsInCorrectOrder() {
        User user = new User();
        user.setName("User1");
        user.setEmail("user1@example.com");
        entityManager.persist(user);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Нужен молоток");
        request1.setRequestor(user);
        request1.setCreated(LocalDateTime.now().minusDays(1));
        entityManager.persist(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Нужна отвертка");
        request2.setRequestor(user);
        request2.setCreated(LocalDateTime.now());
        entityManager.persist(request2);

        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(user.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequest::getDescription)
                .containsExactly("Нужна отвертка", "Нужен молоток");
    }

    @Test
    @DisplayName("Поиск запросов других пользователей с пагинацией")
    void findAllExcludingUser_shouldReturnRequestsOfOtherUsers() {
        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@example.com");
        entityManager.persist(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@example.com");
        entityManager.persist(user2);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Запрос от user1");
        request1.setRequestor(user1);
        request1.setCreated(LocalDateTime.now());
        entityManager.persist(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Запрос от user2");
        request2.setRequestor(user2);
        request2.setCreated(LocalDateTime.now());
        entityManager.persist(request2);

        entityManager.flush();

        List<ItemRequest> result = itemRequestRepository.findAllExcludingUser(user1.getId(), PageRequest.of(0, 10));

        assertThat(result)
                .hasSize(1)
                .extracting(ItemRequest::getDescription)
                .containsExactly("Запрос от user2");
    }
}
