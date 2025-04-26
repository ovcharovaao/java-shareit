package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "requestorId", source = "request.requestor.id")
    @Mapping(target = "items", expression = "java(java.util.Collections.emptyList())")
    ItemRequestDto toDto(ItemRequest request);

    @Mapping(target = "requestorId", source = "request.requestor.id")
    @Mapping(target = "items", source = "items")
    ItemRequestDto toDtoWithItems(ItemRequest request, List<ItemShortDto> items);
}
