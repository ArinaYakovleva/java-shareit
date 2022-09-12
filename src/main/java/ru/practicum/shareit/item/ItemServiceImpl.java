package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage storage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage storage, UserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        validateIfNoUser(ownerId);

        log.info(String.format("Добавление вещи: %s", itemDto));
        return storage.addItem(itemDto, ownerId);
    }

    @Override
    public ItemDto editItem(Long id, ItemDto item, Long ownerId) {
        if (storage.getItem(id) == null) {
            String errorMessage = String.format("Вещь с id=%d не найдена", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        validateIfNoUser(ownerId);

        ItemDto updatedItem = storage.editItem(id, item, ownerId);

        log.info(String.format("Изменение вещи с id=%d", id));
        return updatedItem;
    }

    @Override
    public ItemDto getItem(Long id) {
        ItemDto item = storage.getItem(id);
        if (item == null) {
            String errorMessage = String.format("Вещь с id=%d не найдена", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return item;
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = storage.deleteItem(id, ownerId);

        if (item == null) {
            String errorMessage = String.format("Вещь с id=%d и ownerId=%d не найдена", id, ownerId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        log.info(String.format("Удааление вещи с id=%d", id));
    }

    @Override
    public List<ItemDto> getAllItems(Long ownerId) {
        return storage.getAllItems(ownerId);
    }

    @Override
    public List<ItemDto> searchItems(String searchStr) {
        return storage.searchItems(searchStr);
    }

    private void validateIfNoUser(Long id) {
        boolean isAnyUser = userStorage.getAllUsers().stream()
                .anyMatch(userDto -> userDto.getId().equals(id));
        System.out.println("IS ANY USER " + isAnyUser);
        if (!isAnyUser) {
            String errorMessage = String.format("Пользователь с id=%d не существует", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

}
