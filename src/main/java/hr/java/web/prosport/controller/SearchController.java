package hr.java.web.prosport.controller;

import hr.java.web.prosport.service.CategoryService;
import hr.java.web.prosport.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        if (q == null || q.trim().isEmpty()) {
            return "redirect:/products";
        }

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", productService.searchProducts(q.trim()));
        model.addAttribute("searchQuery", q.trim());

        return "search-results";
    }
}