package com.example.demo.controller;

import com.example.demo.dto.CartItemResponse;
import com.example.demo.dto.CartResponse;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.model.entity.Cart;
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

import java.util.stream.Collectors;

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
        Cart cart = cartService.getCartByUser(user);

        System.out.println("Cart ID: " + cart.getId()); // 調試輸出購物車ID

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId(cart.getId());  // 設置購物車 ID
        cartResponse.setItems(cart.getItems().stream()
                .map(cartItem -> {
                    System.out.println("CartItem ID: " + cartItem.getId()); // 調試輸出購物車項目ID
                    System.out.println("Product ID: " + cartItem.getProduct().getId()); // 調試輸出產品ID


                    CartItemResponse itemDTO = new CartItemResponse();
                    itemDTO.setCartItemId(cartItem.getId());  // 設置購物車項目的 ID
                    itemDTO.setProductId(cartItem.getProduct().getId());  // 設置產品 ID
                    itemDTO.setProductName(cartItem.getProduct().getName());  // 設置產品名稱
                    itemDTO.setPrice(cartItem.getPrice());  // 設置價格
                    itemDTO.setQuantity(cartItem.getQuantity());  // 設置數量
                    itemDTO.setSize(cartItem.getSize());  // 設置尺寸
                    itemDTO.setTotalPrice(cartItem.getTotalPrice());  // 設置總價
                    return itemDTO;
                })
                .collect(Collectors.toList()));

        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addProductToCart(@RequestParam Long productId,
                                                         @RequestParam int quantity,
                                                         @RequestParam String size) {
        // 紀錄進入方法的日誌
        System.out.println("Adding product to cart. Product ID: " + productId + ", Quantity: " + quantity + ", Size: " + size);

        String username = getCurrentUsername();
        User user = userService.findByEmail(username);

        // 確認 User 對象
        System.out.println("User found: " + user.getId() + ", Username: " + user.getUsername());

        ProductDTO product = productService.getProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        Cart updatedCart = cartService.addProductToCart(user, product, quantity, size);

        // 確認更新後的 Cart 對象
        System.out.println("Cart updated with ID: " + updatedCart.getId());

        // 確認 Cart 內容
        updatedCart.getItems().forEach(item ->
                System.out.println("CartItem ID: " + item.getId() + ", Product ID: " + item.getProduct().getId())
        );

        // 轉換成 DTO 並返回
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartId(updatedCart.getId());
        cartResponse.setItems(updatedCart.getItems().stream()
                .map(cartItem -> {
                    CartItemResponse itemDTO = new CartItemResponse();
                    itemDTO.setCartItemId(cartItem.getId());
                    itemDTO.setProductId(cartItem.getProduct().getId());
                    itemDTO.setProductName(cartItem.getProduct().getName());
                    itemDTO.setQuantity(cartItem.getQuantity());
                    itemDTO.setPrice(cartItem.getPrice());
                    itemDTO.setSize(cartItem.getSize());
                    itemDTO.setTotalPrice(cartItem.getTotalPrice());
                    return itemDTO;
                }).collect(Collectors.toList()));

        return ResponseEntity.ok(cartResponse);
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateCartItem(@RequestParam Long cartItemId, @RequestParam int quantity) {
        String username = getCurrentUsername();
        User user = userService.findByEmail(username);

        Cart updatedCart = cartService.updateCartItem(user, cartItemId, quantity);
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
