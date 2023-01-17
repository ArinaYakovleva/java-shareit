package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDTOMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemDbIntegrationTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    private ItemDto firstItem;
    private ItemDto secondItem;
    private User user;
    private User secondUser;

    @BeforeEach
    void init() {
        UserDto userDto = new UserDto(1L, "test user", "test@mail.ru");
        User user = userRepository.saveAndFlush(UserDTOMapper.fromUserDto(userDto));
        this.user = user;

        ItemDto firstItem = new ItemDto(1L, "отвертка", "хорошая отвертка", true, null);
        this.firstItem = firstItem;
        itemRepository.save(ItemDTOMapper.fromItemDto(firstItem, user, null));

        UserDto userDto1 = new UserDto(2L, "Ivan", "ivan@mail.ru");
        User user1 = userRepository.saveAndFlush(UserDTOMapper.fromUserDto(userDto1));
        this.secondUser = user1;

        ItemDto secondItem = new ItemDto(2L, "дрель", "отличная дрель", false, null);
        this.secondItem = secondItem;
        itemRepository.save(ItemDTOMapper.fromItemDto(secondItem, user1, null));

    }

    @Test
    void addItem() {
        ItemDto itemDto = new ItemDto(3L, "газонокосилка", "газонокосилка karcher", true, null);
        Item item = itemRepository.saveAndFlush(ItemDTOMapper.fromItemDto(itemDto, user, null));

        List<Item> items = itemRepository.findAll();

        assertEquals(itemDto, ItemDTOMapper.toItemDto(item));
        assertEquals(3, items.size());
    }

    @Test
    void editItem() {
        ItemDto itemDto = new ItemDto(1L, "updated", "updated", false, null);
        Item item = itemRepository.saveAndFlush(ItemDTOMapper.fromItemDto(itemDto, user, null));
        assertEquals(itemDto, ItemDTOMapper.toItemDto(item));
    }

    @Test
    void addComment() {
        LocalDateTime creationDate = LocalDateTime.of(2022, 10, 24, 13, 0);
        CommentDto commentDto = new CommentDto(1L, "отличная дрель", "Иван", creationDate);
        Comment comment = commentRepository.saveAndFlush(ItemDTOMapper.fromCommentDto(commentDto,
                ItemDTOMapper.fromItemDto(secondItem, secondUser, null), user, creationDate));
        List<Comment> comments = commentRepository.findAllByItem_Id(2L);
        assertEquals(1, comments.size());
        assertEquals(comment, comments.get(0));

    }

    @Test
    void getItem() {
        Item item = itemRepository.findById(1L).get();
        Optional<Item> notFoundItem = itemRepository.findById(6L);

        assertEquals(ItemDTOMapper.fromItemDto(firstItem, user, null), item);
        assertFalse(notFoundItem.isPresent());
    }

    @Test
    void getAllItems() {
        List<Item> items = itemRepository.findAll();

        assertEquals(2, items.size());
        assertEquals(ItemDTOMapper.fromItemDto(firstItem, user, null), items.get(0));
        assertEquals(ItemDTOMapper.fromItemDto(secondItem, secondUser, null), items.get(1));
    }

    @Test
    void searchItems() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 2);
        List<Item> items = itemRepository.search("отВерт", pageRequest);
        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId());
    }

    @Test
    void deleteItem() {
        itemRepository.deleteById(1L);
        List<Item> items = itemRepository.findAll();
        assertEquals(1, items.size());
        assertEquals(ItemDTOMapper.fromItemDto(secondItem, secondUser, null), items.get(0));
    }
}
