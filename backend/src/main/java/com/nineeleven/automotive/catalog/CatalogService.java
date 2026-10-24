package com.nineeleven.automotive.catalog;

import com.nineeleven.automotive.offer.*;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CatalogService {
    private final CategoryRepository categories; private final CatalogServiceRepository services; private final ServicePriceRepository prices; private final OfferRepository offers; private final CatalogMapper mapper;
    public CatalogService(CategoryRepository categories,CatalogServiceRepository services,ServicePriceRepository prices,OfferRepository offers,CatalogMapper mapper){this.categories=categories;this.services=services;this.prices=prices;this.offers=offers;this.mapper=mapper;}
    public List<CatalogModels.CategoryResponse> publicCategories(){return categories.findAllByActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream().map(mapper::category).toList();}
    public Page<CatalogModels.ServiceResponse> publicServices(String category,String sizeClass,Pageable page){return services.findPublic(category,page).map(s->response(s,sizeClass));}
    public CatalogModels.ServiceResponse publicService(String slug,String sizeClass){return response(services.findPublicBySlug(slug).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Service not found.")),sizeClass);}
    private CatalogModels.ServiceResponse response(ServiceEntity entity,String sizeClass){var base=mapper.service(entity);var configured=prices.findAllByServiceId(entity.getId());var price=sizeClass==null?(configured.size()==1?configured.getFirst().getPrice():null):prices.findByServiceIdAndVehicleSizeClass(entity.getId(),sizeClass.toUpperCase(Locale.ROOT)).map(ServicePriceEntity::getPrice).orElse(null);return new CatalogModels.ServiceResponse(base.id(),base.categoryId(),base.categoryName(),base.categorySlug(),base.name(),base.slug(),base.description(),base.shortBenefit(),base.imageUrl(),base.baseDurationHours(),price,base.active());}
    public List<OfferModels.OfferResponse> publicOffers(){return offers.findPublic(LocalDate.now()).stream().map(mapper::offer).toList();}
    public List<CatalogModels.CategoryResponse> adminCategories(){return categories.findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc().stream().map(mapper::category).toList();}
    public CatalogModels.CategoryResponse saveCategory(UUID id,CatalogModels.CategoryRequest request){var existing=categories.findBySlugIgnoreCaseAndDeletedAtIsNull(request.slug());if(existing.isPresent()&&(id==null||!existing.get().getId().equals(id)))throw new IllegalArgumentException("Category slug is already in use.");ServiceCategoryEntity entity=id==null?new ServiceCategoryEntity():categories.findById(id).orElseThrow(()->notFound("Category"));mapper.update(request,entity);return mapper.category(categories.save(entity));}
    public void hideCategory(UUID id){var e=categories.findById(id).orElseThrow(()->notFound("Category"));e.setActive(false);categories.save(e);}
    public CatalogModels.ServiceResponse saveService(UUID id,CatalogModels.ServiceRequest request){var category=categories.findById(request.categoryId()).filter(c->c.getDeletedAt()==null).orElseThrow(()->notFound("Category"));if(services.existsBySlugIgnoreCaseAndDeletedAtIsNull(request.slug())&&(id==null||!services.findById(id).map(e->e.getSlug().equalsIgnoreCase(request.slug())).orElse(false)))throw new IllegalArgumentException("Service slug is already in use.");ServiceEntity entity=id==null?new ServiceEntity():services.findById(id).orElseThrow(()->notFound("Service"));mapper.update(request,entity);entity.setCategory(category);return response(services.save(entity),null);}
    public void hideService(UUID id){var e=services.findById(id).orElseThrow(()->notFound("Service"));e.setActive(false);services.save(e);}
    public List<CatalogModels.ServiceResponse> adminServices(){return services.findAll().stream().filter(s->s.getDeletedAt()==null).map(s->response(s,null)).toList();}
    public ServicePriceEntity setPrice(UUID serviceId,CatalogModels.PriceRequest request){var service=services.findActiveById(serviceId).orElseThrow(()->notFound("Service"));var price=prices.findByServiceIdAndVehicleSizeClass(serviceId,request.vehicleSizeClass().toUpperCase(Locale.ROOT)).orElseGet(ServicePriceEntity::new);price.setService(service);price.setVehicleSizeClass(request.vehicleSizeClass().toUpperCase(Locale.ROOT));price.setPrice(request.price());return prices.save(price);}
    public List<CatalogModels.PriceResponse> servicePrices(UUID serviceId){services.findById(serviceId).orElseThrow(()->notFound("Service"));return prices.findAllByServiceId(serviceId).stream().map(p->new CatalogModels.PriceResponse(p.getId(),serviceId,p.getVehicleSizeClass(),p.getPrice())).toList();}
    public void deletePrice(UUID serviceId,String sizeClass){prices.deleteByServiceIdAndVehicleSizeClass(serviceId,sizeClass.toUpperCase(Locale.ROOT));}
    public OfferModels.OfferResponse saveOffer(UUID id,OfferModels.OfferRequest request){validateOffer(request);OfferEntity e=id==null?new OfferEntity():offers.findDetailed(id).orElseThrow(()->notFound("Offer"));mapper.update(request,e);Set<ServiceEntity> selected=new LinkedHashSet<>();if(request.serviceIds()!=null)for(UUID sid:request.serviceIds())selected.add(services.findActiveById(sid).orElseThrow(()->notFound("Service")));if(!request.appliesToAll()&&selected.isEmpty())throw new IllegalArgumentException("Select at least one service or set appliesToAll to true.");if(request.discountType()==DiscountType.FLAT){var applicable=prices.findAll().stream().filter(p->request.appliesToAll()||selected.stream().anyMatch(s->s.getId().equals(p.getService().getId())));if(applicable.anyMatch(p->request.discountValue().compareTo(p.getPrice())>0))throw new IllegalArgumentException("Flat discount cannot exceed an applicable service price.");}e.setServices(selected);return mapper.offer(offers.save(e));}
    public List<OfferModels.OfferResponse> adminOffers(){return offers.findAll().stream().filter(o->o.getDeletedAt()==null).map(mapper::offer).toList();}
    public void hideOffer(UUID id){var e=offers.findById(id).orElseThrow(()->notFound("Offer"));e.setActive(false);offers.save(e);}
    private void validateOffer(OfferModels.OfferRequest r){if(r.validFrom()!=null&&r.validTo()!=null&&r.validFrom().isAfter(r.validTo()))throw new IllegalArgumentException("validFrom must be on or before validTo.");if(r.discountType()==DiscountType.PERCENT&&r.discountValue()!=null&&(r.discountValue().compareTo(BigDecimal.ONE)<0||r.discountValue().compareTo(BigDecimal.valueOf(100))>0))throw new IllegalArgumentException("Percent discount must be between 1 and 100.");if(r.discountType()==DiscountType.FLAT&&r.discountValue()!=null&&r.discountValue().signum()<0)throw new IllegalArgumentException("Flat discount cannot be negative.");}
    private ResponseStatusException notFound(String name){return new ResponseStatusException(HttpStatus.NOT_FOUND,name+" not found.");}
}
