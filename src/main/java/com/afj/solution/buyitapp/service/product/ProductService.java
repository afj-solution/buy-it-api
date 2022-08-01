package com.afj.solution.buyitapp.service.product;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.afj.solution.buyitapp.model.Product;
import com.afj.solution.buyitapp.payload.request.CreateProductRequest;
import com.afj.solution.buyitapp.payload.request.UpdateCharacteristicRequest;
import com.afj.solution.buyitapp.payload.response.ProductResponse;

/**
 * @author Tomash Gombosh
 */
public interface ProductService {

    Page<ProductResponse> getProducts(Pageable pageable);

    Product save(CreateProductRequest createProductRequest);

    Product save(Product product);

    ProductResponse addImageToProduct(UUID id, MultipartFile file) throws IOException;

    ProductResponse updateCharacteristicToProduct(UUID id, UpdateCharacteristicRequest request);

    byte[] getImageByProductId(UUID id);

    Product findById(UUID id);

    List<Product> productsWithEmptyQuantity(List<UUID> productIds);

    Set<Product> getProductsById(List<UUID> productIds);

    void decreaseProductQuantity(UUID productId, int quantity);

    void increaseProductQuantity(UUID productId, int quantity);
}
