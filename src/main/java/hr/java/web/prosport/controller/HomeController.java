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

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", productService.findAvailable());
        return "home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute("categories", categoryService.findAll());

        if (categoryId != null) {
            model.addAttribute("products", productService.findByCategoryId(categoryId));
            model.addAttribute("selectedCategory", categoryService.findById(categoryId).orElse(null));
        } else {
            model.addAttribute("products", productService.findAvailable());
        }

        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        var product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);
        return "product-details";
    }
}