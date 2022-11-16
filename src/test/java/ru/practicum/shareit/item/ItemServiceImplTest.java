package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    ItemService itemService;
    ItemDto itemDto = new ItemDto(
            null,
            "Отвертка",
            "лучшая отвертка",
            true,
            null
    );
    ItemDto itemDtoWithId = new ItemDto(
            1L,
            "Отвертка",
            "лучшая отвертка",
            true,
            null
    );

    ItemBookingDto itemBookingDto = new ItemBookingDto(
            1L,
            "Отвертка",
            "лучшая отвертка",
            true,
            null,
            null,
            new ArrayList<>());


    @Test
    void addItem() {
        Mockito.when(itemService.addItem(itemDto, 1L))
                .thenReturn(itemDtoWithId);
        ItemDto createdItem = itemService.addItem(itemDto, 1L);
        assertEquals(itemDtoWithId, createdItem);
        Mockito.verify(itemService, Mockito.times(1)).addItem(itemDto, 1L);
    }

    @Test
    void editItem() {
        Mockito.when(itemService.editItem(1L, itemDto, 1L))
                .thenReturn(itemDtoWithId);
        ItemDto editedItem = itemService.editItem(1L, itemDto, 1L);
        assertEquals(itemDtoWithId, editedItem);
        Mockito.verify(itemService, Mockito.times(1)).editItem(1L, itemDto, 1L);
    }

    @Test
    void getItem() {
        Mockito.when(itemService.getItem(1L, 1L))
                .thenReturn(itemBookingDto);
        ItemBookingDto bookingDto = itemService.getItem(1L, 1L);
        assertEquals(itemBookingDto, bookingDto);
        Mockito.verify(itemService, Mockito.times(1)).getItem(1L, 1L);
    }

    @Test
    void getAllItems() {
        Mockito.when(itemService.getAllItems(1L, 0, 1))
                .thenReturn(getItems());
        List<ItemBookingDto> itemBookingDtos = itemService.getAllItems(1L, 0, 1);
        assertEquals(getItems().get(0).getId(), itemBookingDtos.get(0).getId());
        assertEquals(getItems().size(), itemBookingDtos.size());

        Mockito.verify(itemService,
                        Mockito.times(1))
                .getAllItems(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void searchItems() {
        Mockito.when(itemService.searchItems("отВерт", 0, 1))
                .thenReturn(getItemDtos());
        List<ItemDto> itemDtos = itemService.searchItems("отВерт", 0, 1);
        assertEquals(getItemDtos().get(0).getId(), itemDtos.get(0).getId());
        assertEquals(getItemDtos().size(), itemDtos.size());

        Mockito.verify(itemService,
                        Mockito.times(1))
                .searchItems(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void deleteItem() {
        itemService.deleteItem(1L, 1L);
        Mockito.verify(itemService, Mockito.times(1))
                .deleteItem(Mockito.anyLong(), Mockito.anyLong());
    }

    private List<ItemBookingDto> getItems() {
        List<ItemBookingDto> items = new ArrayList<>();
        items.add(new ItemBookingDto(
                1L,
                "Отвертка",
                "лучшая отвертка",
                true,
                null,
                null,
                new ArrayList<>()));
        return items;
    }

    private List<ItemDto> getItemDtos() {
        List<ItemDto> itemDtos = new ArrayList<>();
        itemDtos.add(new ItemDto(
                null,
                "Отвертка",
                "лучшая отвертка",
                true,
                null
        ));
        return itemDtos;
    }
}
