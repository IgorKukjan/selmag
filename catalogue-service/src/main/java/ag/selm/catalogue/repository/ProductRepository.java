package ag.selm.catalogue.repository;

import ag.selm.catalogue.entity.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends CrudRepository<Product, Integer> {

//    Iterable<Product> findAllByTitleLikeIgnoreCase(String filter);//(1)select * from catalogue.t_product where c_title ilike :filter

//    @Query(value = "select p from Product p where p.title ilike :filter")//(2)(JPQL)
//    Iterable<Product> findAllByTitleLikeIgnoreCase(@Param("filter") String filter);

//    @Query(value = "select * from catalogue.t_product where c_title ilike :filter", nativeQuery = true)//(3)(SQL)
//    Iterable<Product> findAllByTitleLikeIgnoreCase(@Param("filter") String filter);

    @Query(name = "Product.findAllByTitleLikeIgnoringCase", nativeQuery = true)//(4)(JPQL + NamedQuery (см Product) )
    Iterable<Product> findAllByTitleLikeIgnoreCase(@Param("filter") String filter);
}
