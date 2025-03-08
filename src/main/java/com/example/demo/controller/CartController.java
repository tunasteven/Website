package com.example.demo.controller;

import com.example.demo.dto.CartResponse;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public CartController(CartService cartService, UserService userService, ProductService productService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        String username = getCurrentUsername();
        User user = userService.findByEmail(username);
        CartResponse cartResponse = cartService.getCartByUser(user); // ✅ 直接讓 service 回傳 DTO

        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam String size) {

        System.out.println("Adding product to cart. Product ID: " + productId + ", Quantity: " + quantity + ", Size: " + size);

        String username = getCurrentUsername();
        User user = userService.findByEmail(username);

        ProductDTO product = productService.getProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        CartResponse updatedCart = cartService.addProductToCart(user, product, quantity, size); // ✅ 讓 service 回傳 DTO

        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestParam Long cartItemId,
            @RequestParam int quantity) {

        String username = getCurrentUsername();
        User user = userService.findByEmail(username);

        CartResponse updatedCart = cartService.updateCartItem(user, cartItemId, quantity); // ✅ 修改回傳 DTO
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        String username = getCurrentUsername();
        User user = userService.findByEmail(username);

        cartService.removeCartItem(user, cartItemId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        throw new IllegalStateException("User not authenticated");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/reset")
    public ResponseEntity<Void> resetCart() {
        cartService.clearAndResetCart();
        return ResponseEntity.ok().build();
    }

}
