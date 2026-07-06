package com.ekwe_hub.zeeshopserver.shared.mapper;

import java.util.List;

/**
 * Contract for MapStruct mappers that convert between a domain Entity (E)
 * and its Data Transfer Object (D).
 *
 * Why keep entities and DTOs separate?
 * Entities belong to the persistence layer and carry JPA annotations, lazy-load
 * proxies, and audit fields the API caller should never see. DTOs are the
 * contract with the outside world — shaped for what the client needs, versioned
 * independently of the database schema.
 *
 * Usage: each domain (Product, Order, User…) creates a MapStruct @Mapper that
 * implements this interface. MapStruct generates the implementation at compile
 * time so there is zero reflection overhead at runtime.
 *
 * Example:
 *   @Mapper(componentModel = "spring")
 *   public interface ProductMapper extends EntityDtoMapper<ProductDto, ProductEntity> {}
 *
 * @param <D> the DTO type (what the API sends / receives)
 * @param <E> the Entity type (what JPA persists)
 */
public interface EntityDtoMapper<D, E> {
    E toEntity(D dto);
    D toDto(E entity);
    List<D> toDtoList(List<E> entities);
    List<E> toEntityList(List<D> dtos);
}
