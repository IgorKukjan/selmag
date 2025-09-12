package ag.selm.feedback.controller;

import ag.selm.feedback.controller.payload.NewProductReviewPayload;
import ag.selm.feedback.entity.ProductReview;
import ag.selm.feedback.service.ProductReviewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@RestController
@RequestMapping("feedback-api/product-reviews")
@RequiredArgsConstructor
@Slf4j
public class ProductReviewsRestController {

    private final ProductReviewsService productReviewsService;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @GetMapping("by-product-id/{productId:\\d+}")
    public Flux<ProductReview> findProductReviewsByProductId(@PathVariable("productId") int productId,
                                                             Mono<Principal> principalMono,
                                                             Mono<JwtAuthenticationToken> jwtAuthenticationTokenMono) {
        return this.productReviewsService.findProductReviewsByProduct(productId);

//        return principalMono.flatMapMany(principal -> {
//            log.info("Principal: {}", principal);
//            return this.reactiveMongoTemplate
//                    .find(query(where("productId").is(productId)) , ProductReview.class);
//        });

//        return this.reactiveMongoTemplate
//                .find(query(where("productId").is(productId)) , ProductReview.class);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductReview>> createProductReview(
            Mono<JwtAuthenticationToken> authenticationTokenMono,
            @Valid @RequestBody Mono<NewProductReviewPayload> payloadMono,
            UriComponentsBuilder uriComponentsBuilder) {
        return authenticationTokenMono.flatMap(token -> payloadMono
                .flatMap(payload -> this.productReviewsService.createProductReview(
                        payload.productId(),
                        payload.rating(),
                        payload.review(),
                        token.getToken().getSubject())))
                .map(productReview -> ResponseEntity
                        .created(uriComponentsBuilder.replacePath("/feedback-api/product-reviews/{id}")
                                .build(productReview.getId()))
                        .body(productReview)
                );
    }
}
