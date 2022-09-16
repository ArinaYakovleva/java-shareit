package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.exceptions.ForbiddenException;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.BaseItemService;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemStorageImpl implements BaseItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 0L;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        idCounter++;
        ItemDto itemToCreate = new ItemDto(idCounter,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable());
        Item item = ItemDTOMapper.fromItemDto(itemToCreate, ownerId);
        items.put(item.getId(), item);
        return itemToCreate;
    }

    @Override
    public ItemDto editItem(Long id, ItemDto item, Long ownerId) {
        Item existingItem = items.get(id);

        validateAccess(existingItem, ownerId);

        ItemDto updatedItemDto = new ItemDto(
                id,
                item.getName() == null ? existingItem.getName() : item.getName(),
                item.getDescription() == null ? existingItem.getDescription() : item.getDescription(),
                item.getAvailable() == null ? existingItem.getAvailable() : item.getAvailable()
        );
        items.put(id, ItemDTOMapper.fromItemDto(updatedItemDto, ownerId));
        return updatedItemDto;
    }

    @Override
    public ItemDto getItem(Long id) {
        Item item = items.get(id);
        return ItemDTOMapper.toItemDto(item);
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = items.get(id);

        validateAccess(item, ownerId);
        if (item == null) {
            String errorMessage = String.format("Вещь с id=%d, ownerId=%d не найдена", id, ownerId);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        items.remove(id);
    }

    @Override
    public List<ItemDto> getAllItems(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(ownerId))
                .map(ItemDTOMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String searchStr) {
        List<ItemDto> itemsList = new ArrayList<>();

        for (Item item : items.values()) {
            boolean isSearchSuccess = searchStr != null && !searchStr.isEmpty()
                    && item.getDescription().toLowerCase().contains(searchStr.toLowerCase())
                    && item.getAvailable();
            if (isSearchSuccess) {
                itemsList.add(ItemDTOMapper.toItemDto(item));
            }
        }
        return itemsList;
    }

    private void validateAccess(Item item, Long ownerId) {
        if (item != null && !Objects.equals(item.getOwnerId(), ownerId)) {
            String errorMessage = String.format("У пользователя с id=%d нет доступа к записи c id=%d",
                    ownerId, item.getId());
            log.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }
    }
}
