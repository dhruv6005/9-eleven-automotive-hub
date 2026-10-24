package com.nineeleven.automotive.catalog;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface CategoryRepository extends JpaRepository<ServiceCategoryEntity, UUID> {
    List<ServiceCategoryEntity> findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    List<ServiceCategoryEntity> findAllByActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    Optional<ServiceCategoryEntity> findBySlugIgnoreCaseAndDeletedAtIsNull(String slug);
}
interface CatalogServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    boolean existsBySlugIgnoreCaseAndDeletedAtIsNull(String slug);
    @Query("select s from ServiceEntity s join fetch s.category where s.deletedAt is null and s.active=true and s.category.active=true and s.category.deletedAt is null and (:category is null or s.category.slug=:category)")
    Page<ServiceEntity> findPublic(@Param("category") String category, Pageable pageable);
    @Query("select s from ServiceEntity s join fetch s.category where s.deletedAt is null and s.active=true and s.category.active=true and s.category.deletedAt is null order by s.name")
    List<ServiceEntity> findAllPublic();
    @Query("select s from ServiceEntity s join fetch s.category where s.slug=:slug and s.deletedAt is null and s.active=true and s.category.active=true and s.category.deletedAt is null")
    Optional<ServiceEntity> findPublicBySlug(@Param("slug") String slug);
    @Query("select s from ServiceEntity s join fetch s.category where s.id=:id and s.deletedAt is null") Optional<ServiceEntity> findActiveById(@Param("id") UUID id);
}
