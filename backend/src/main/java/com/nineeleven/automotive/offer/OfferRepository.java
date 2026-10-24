package com.nineeleven.automotive.offer;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OfferRepository extends JpaRepository<OfferEntity, UUID> {
    @Query("select distinct o from OfferEntity o left join fetch o.services where o.deletedAt is null and o.active=true and o.validFrom<=:today and o.validTo>=:today")
    List<OfferEntity> findPublic(@Param("today") LocalDate today);
    @Query("select distinct o from OfferEntity o left join fetch o.services where o.id=:id and o.deletedAt is null") Optional<OfferEntity> findDetailed(@Param("id") UUID id);
}
