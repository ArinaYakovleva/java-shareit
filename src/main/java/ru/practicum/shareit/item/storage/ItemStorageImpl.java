package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.exceptions.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDTOMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemStorageImpl implements ItemStorage {
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
    public Item deleteItem(Long id, Long ownerId) {
        Item item = items.get(id);

        validateAccess(item, ownerId);

        items.remove(id);
        return item;
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
        if (item != null && !item.getOwnerId().equals(ownerId)) {
            String errorMessage = String.format("У пользователя с id=%d нет доступа к записи c id=%d",
                    ownerId, item.getId());
            log.error(errorMessage);
            throw new ForbiddenException(errorMessage);
        }
    }
}
