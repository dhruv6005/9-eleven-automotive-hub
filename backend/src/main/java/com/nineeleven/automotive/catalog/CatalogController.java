package com.nineeleven.automotive.catalog;

import com.nineeleven.automotive.offer.OfferModels;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import com.nineeleven.automotive.vehicle.VehicleSizeClass;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name="Catalog and offers")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog){this.catalog=catalog;}
    @GetMapping("/api/v1/public/categories") @Operation(summary="List active service categories") public ResponseEntity<?> categories(){return cached(catalog.publicCategories());}
    @GetMapping("/api/v1/public/vehicle-size-classes") @Operation(summary="List supported vehicle size classes") public ResponseEntity<?> vehicleSizeClasses(){return cached(catalog.publicVehicleSizeClasses());}
    @GetMapping("/api/v1/public/services") @Operation(summary="Browse active services") public ResponseEntity<?> services(@RequestParam(required=false) String category,@RequestParam(required=false) VehicleSizeClass sizeClass,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return cached(catalog.publicServices(category,sizeClass==null?null:sizeClass.name(),PageRequest.of(page,Math.min(size,100),Sort.by("category.displayOrder").ascending().and(Sort.by("name")))));}
    @GetMapping("/api/v1/public/services/{slug}") @Operation(summary="Read an active service by slug") public ResponseEntity<?> service(@PathVariable String slug,@RequestParam(required=false) VehicleSizeClass sizeClass){return cached(catalog.publicService(slug,sizeClass==null?null:sizeClass.name()));}
    @GetMapping("/api/v1/public/offers") @Operation(summary="List current active offers") public ResponseEntity<?> offers(){return cached(catalog.publicOffers());}
    @GetMapping("/api/v1/admin/categories") @PreAuthorize("hasRole('ADMIN')") public List<CatalogModels.CategoryResponse> adminCategories(){return catalog.adminCategories();}
    @PostMapping("/api/v1/admin/categories") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<CatalogModels.CategoryResponse> createCategory(@Valid @RequestBody CatalogModels.CategoryRequest body){return ResponseEntity.status(201).body(catalog.saveCategory(null,body));}
    @PutMapping("/api/v1/admin/categories/{id}") @PreAuthorize("hasRole('ADMIN')") public CatalogModels.CategoryResponse updateCategory(@PathVariable UUID id,@Valid @RequestBody CatalogModels.CategoryRequest body){return catalog.saveCategory(id,body);}
    @DeleteMapping("/api/v1/admin/categories/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> hideCategory(@PathVariable UUID id){catalog.hideCategory(id);return ResponseEntity.noContent().build();}
    @GetMapping("/api/v1/admin/services") @PreAuthorize("hasRole('ADMIN')") public List<CatalogModels.ServiceResponse> adminServices(){return catalog.adminServices();}
    @PostMapping("/api/v1/admin/services") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<CatalogModels.ServiceResponse> createService(@Valid @RequestBody CatalogModels.ServiceRequest body){return ResponseEntity.status(201).body(catalog.saveService(null,body));}
    @PutMapping("/api/v1/admin/services/{id}") @PreAuthorize("hasRole('ADMIN')") public CatalogModels.ServiceResponse updateService(@PathVariable UUID id,@Valid @RequestBody CatalogModels.ServiceRequest body){return catalog.saveService(id,body);}
    @DeleteMapping("/api/v1/admin/services/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> hideService(@PathVariable UUID id){catalog.hideService(id);return ResponseEntity.noContent().build();}
    @PutMapping("/api/v1/admin/services/{id}/prices/{sizeClass}") @PreAuthorize("hasRole('ADMIN')") public CatalogModels.PriceResponse setPrice(@PathVariable UUID id,@PathVariable VehicleSizeClass sizeClass,@Valid @RequestBody PriceValue body){var price=catalog.setPrice(id,new CatalogModels.PriceRequest(sizeClass,body.price()));return new CatalogModels.PriceResponse(price.getId(),id,price.getVehicleSizeClass(),price.getPrice());}
    @GetMapping("/api/v1/admin/services/{id}/prices") @PreAuthorize("hasRole('ADMIN')") public List<CatalogModels.PriceResponse> servicePrices(@PathVariable UUID id){return catalog.servicePrices(id);}
    @DeleteMapping("/api/v1/admin/services/{id}/prices/{sizeClass}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> deletePrice(@PathVariable UUID id,@PathVariable VehicleSizeClass sizeClass){catalog.deletePrice(id,sizeClass.name());return ResponseEntity.noContent().build();}
    public record PriceValue(@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") @jakarta.validation.constraints.Digits(integer=10,fraction=2) BigDecimal price){}
    @GetMapping("/api/v1/admin/offers") @PreAuthorize("hasRole('ADMIN')") public List<OfferModels.OfferResponse> adminOffers(){return catalog.adminOffers();}
    @PostMapping("/api/v1/admin/offers") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<OfferModels.OfferResponse> createOffer(@Valid @RequestBody OfferModels.OfferRequest body){return ResponseEntity.status(201).body(catalog.saveOffer(null,body));}
    @PutMapping("/api/v1/admin/offers/{id}") @PreAuthorize("hasRole('ADMIN')") public OfferModels.OfferResponse updateOffer(@PathVariable UUID id,@Valid @RequestBody OfferModels.OfferRequest body){return catalog.saveOffer(id,body);}
    @DeleteMapping("/api/v1/admin/offers/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> hideOffer(@PathVariable UUID id){catalog.hideOffer(id);return ResponseEntity.noContent().build();}
    private <T> ResponseEntity<T> cached(T body){return ResponseEntity.ok().cacheControl(CacheControl.maxAge(java.time.Duration.ofSeconds(60)).cachePublic()).body(body);}
}
