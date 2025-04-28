package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private Item item;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("testUser");
        user.setEmail("testUser@example.com");
        user = userRepository.save(user);

        item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        commentRepository.save(new Comment(null, "Отличная дрель!", item, user, LocalDateTime.now()));
        commentRepository.save(new Comment(null, "Хороший товар", item, user, LocalDateTime.now().minusDays(1)));
        commentRepository.save(new Comment(null, "Не очень", item, user, LocalDateTime.now().minusDays(2)));
    }

    @Test
    @DisplayName("Метод findByItem_Id должен возвращать все комментарии для указанного itemId с учетом сортировки")
    void findByItem_Id_shouldReturnSortedComments() {
        Sort sort = Sort.by(Sort.Order.desc("created"));

        List<Comment> comments = commentRepository.findByItem_Id(item.getId(), sort);

        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getText()).isEqualTo("Отличная дрель!");
        assertThat(comments.get(1).getText()).isEqualTo("Хороший товар");
        assertThat(comments.get(2).getText()).isEqualTo("Не очень");
    }

    @Test
    @DisplayName("Метод findByItem_Id должен возвращать пустой список, если нет комментариев для указанного itemId")
    void findByItem_Id_shouldReturnEmptyListIfNoComments() {
        Item otherItem = new Item();
        otherItem.setName("Молоток");
        otherItem.setDescription("Сильный молоток");
        otherItem.setAvailable(true);
        otherItem.setOwner(user);
        otherItem = itemRepository.save(otherItem);

        List<Comment> comments = commentRepository.findByItem_Id(otherItem.getId(), Sort.unsorted());

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("Метод findByItem_Id должен корректно сортировать комментарии по дате")
    void findByItem_Id_shouldSortCommentsByDate() {
        Sort sort = Sort.by(Sort.Order.asc("created"));

        List<Comment> comments = commentRepository.findByItem_Id(item.getId(), sort);

        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getText()).isEqualTo("Не очень");
        assertThat(comments.get(1).getText()).isEqualTo("Хороший товар");
        assertThat(comments.get(2).getText()).isEqualTo("Отличная дрель!");
    }
}
