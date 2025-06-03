package hr.java.web.prosport.controller;

import hr.java.web.prosport.service.CategoryService;
import hr.java.web.prosport.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final String CATEGORIES_ATTR = "categories";
    private static final String PRODUCTS_ATTR = "products";
    private static final String SELECTED_CATEGORY_ATTR = "selectedCategory";
    private static final String PRODUCT_ATTR = "product";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute(CATEGORIES_ATTR, categoryService.findAll());
        model.addAttribute(PRODUCTS_ATTR, productService.findAvailable());
        return "home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute(CATEGORIES_ATTR, categoryService.findAll());

        if (categoryId != null) {
            model.addAttribute(PRODUCTS_ATTR, productService.findByCategoryId(categoryId));
            model.addAttribute(SELECTED_CATEGORY_ATTR, categoryService.findById(categoryId).orElse(null));
        } else {
            model.addAttribute(PRODUCTS_ATTR, productService.findAvailable());
        }

        return PRODUCTS_ATTR;
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        var product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));

        model.addAttribute(PRODUCT_ATTR, product);
        return "product-details";
    }
}