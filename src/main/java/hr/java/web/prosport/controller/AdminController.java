package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.CategoryDto;
import hr.java.web.prosport.dto.ProductDto;
import hr.java.web.prosport.service.CategoryService;
import hr.java.web.prosport.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public String adminDashboard() {
        return "redirect:/admin/products";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute CategoryDto categoryDto, RedirectAttributes attributes) {
        try {
            categoryService.createCategory(categoryDto);
            attributes.addFlashAttribute("success", "Kategorija je uspješno kreirana!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        CategoryDto category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return "admin/category-form";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CategoryDto categoryDto,
                                 RedirectAttributes attributes) {
        try {
            categoryService.updateCategory(id, categoryDto);
            attributes.addFlashAttribute("success", "Kategorija je uspješno ažurirana!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            categoryService.deleteCategory(id);
            attributes.addFlashAttribute("success", "Kategorija je uspješno obrisana!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // PROIZVODI
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDto());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute ProductDto productDto, RedirectAttributes attributes) {
        try {
            productService.createProduct(productDto);
            attributes.addFlashAttribute("success", "Proizvod je uspješno kreiran!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductDto product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDto productDto,
                                RedirectAttributes attributes) {
        try {
            productService.updateProduct(id, productDto);
            attributes.addFlashAttribute("success", "Proizvod je uspješno ažuriran!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            productService.deleteProduct(id);
            attributes.addFlashAttribute("success", "Proizvod je uspješno obrisan!");
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/categories/quick")
    @ResponseBody
    public CategoryDto createQuickCategory(@RequestParam String name, @RequestParam String description) {
        CategoryDto categoryDto = new CategoryDto(name, description);
        return categoryService.createCategory(categoryDto);
    }
}