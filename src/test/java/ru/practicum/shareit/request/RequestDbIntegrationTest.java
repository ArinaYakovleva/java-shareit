package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.common.CustomPageRequest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestDbIntegrationTest {
    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;
    private User secondUser;
    private Request request;
    private Request secondRequest;

    @BeforeEach
    void init() {
        User user = new User(1L, "test user", "test@mail.ru");
        secondUser = new User(2L, "second test user", "test1@mail.ru");

        userRepository.save(user);
        userRepository.save(secondUser);

        request = new Request(1L, "нужна дрель", secondUser,
                LocalDateTime.of(2022, 11, 11, 12, 0));

        secondRequest = new Request(2L, "отвертка", user,
                LocalDateTime.of(2022, 11, 11, 12, 0));
        requestRepository.save(request);
        requestRepository.save(secondRequest);

        Item item = new Item(1L, "дрель", "test дрель", user, true, request);
        itemRepository.save(item);
    }

    @Test
    void addRequest() {
        Request newRequest = new Request(3L, "нужна дрель", secondUser,
                LocalDateTime.of(2022, 11, 11, 12, 0));
        Request createdRequest = requestRepository.saveAndFlush(newRequest);

        List<Request> requestList = requestRepository.findAll();

        assertEquals(newRequest, createdRequest);
        assertEquals(3, requestList.size());
    }

    @Test
    void getOwnersRequests() {
        Sort sort = Sort.by("created").descending();
        List<Request> requests = requestRepository.findAllByRequestor_Id(1L, sort);

        assertEquals(1, requests.size());
        assertEquals(secondRequest, requests.get(0));
    }

    @Test
    void getRequestsByOtherUser() {
        CustomPageRequest pageRequest = new CustomPageRequest(0, 10, Sort.by("created").descending());
        List<Request> requests = requestRepository.findAllByRequestor_IdNot(1L, pageRequest);

        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));
    }

    @Test
    void getRequest() {
        Request foundRequest = requestRepository.findById(1L).get();
        Optional<Request> notFoundRequest = requestRepository.findById(4L);

        assertEquals(request, foundRequest);
        assertFalse(notFoundRequest.isPresent());
    }
}
