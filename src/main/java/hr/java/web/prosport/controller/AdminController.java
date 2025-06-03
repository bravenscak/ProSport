package hr.java.web.prosport.controller;

import hr.java.web.prosport.dto.CategoryDto;
import hr.java.web.prosport.dto.LoginHistoryDto;
import hr.java.web.prosport.dto.ProductDto;
import hr.java.web.prosport.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String REDIRECT_ADMIN_CATEGORIES = "redirect:/admin/categories";
    private static final String REDIRECT_ADMIN_PRODUCTS = "redirect:/admin/products";
    private static final String REDIRECT_ADMIN_PRODUCTS_NEW = "redirect:/admin/products/new";
    private static final String ADMIN_CATEGORY_FORM = "admin/category-form";
    private static final String ADMIN_PRODUCT_FORM = "admin/product-form";
    private static final String CATEGORIES_ATTR = "categories";
    private static final String SUCCESS_ATTR = "success";
    private static final String ERROR_ATTR = "error";
    private static final String EDIT_PATH = "/edit";
    private static final String CATEGORY_CREATED_MSG = "Kategorija je uspješno kreirana!";
    private static final String CATEGORY_UPDATED_MSG = "Kategorija je uspješno ažurirana!";
    private static final String CATEGORY_DELETED_MSG = "Kategorija je uspješno obrisana!";
    private static final String PRODUCT_CREATED_MSG = "Proizvod je uspješno kreiran!";
    private static final String PRODUCT_UPDATED_MSG = "Proizvod je uspješno ažuriran!";
    private static final String PRODUCT_DELETED_MSG = "Proizvod je uspješno obrisan!";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    private static final String INVALID_IMAGE_MSG = "Neispravna datoteka. Molimo uploadajte JPG, PNG, GIF ili WebP sliku manju od 10MB.";
    private static final String IMAGE_UPLOAD_ERROR_MSG = "Greška pri uploadu slike: ";

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ImageUploadService imageUploadService;
    private final LoginHistoryService loginHistoryService;
    private final OrderService orderService;

    @GetMapping
    public String adminRoot() {
        return REDIRECT_ADMIN_PRODUCTS;
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute(CATEGORIES_ATTR, categoryService.findAll());
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String saveCategory(@Valid @ModelAttribute CategoryDto categoryDto,
                               BindingResult result,
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return ADMIN_CATEGORY_FORM;
        }

        try {
            categoryService.createCategory(categoryDto);
            attributes.addFlashAttribute(SUCCESS_ATTR, CATEGORY_CREATED_MSG);
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
        }
        return REDIRECT_ADMIN_CATEGORIES;
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new CategoryDto());
        return ADMIN_CATEGORY_FORM;
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        CategoryDto category = categoryService.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        model.addAttribute("category", category);
        return ADMIN_CATEGORY_FORM;
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute CategoryDto categoryDto,
                                 BindingResult result,
                                 RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return ADMIN_CATEGORY_FORM;
        }
        try {
            categoryService.updateCategory(id, categoryDto);
            attributes.addFlashAttribute(SUCCESS_ATTR, CATEGORY_UPDATED_MSG);
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
        }
        return REDIRECT_ADMIN_CATEGORIES;
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            categoryService.deleteCategory(id);
            attributes.addFlashAttribute(SUCCESS_ATTR, CATEGORY_DELETED_MSG);
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
        }
        return REDIRECT_ADMIN_CATEGORIES;
    }

    @PostMapping("/categories/quick")
    @ResponseBody
    public CategoryDto createQuickCategory(@RequestParam String name, @RequestParam String description) {
        CategoryDto categoryDto = new CategoryDto(name, description);
        return categoryService.createCategory(categoryDto);
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDto());
        model.addAttribute(CATEGORIES_ATTR, categoryService.findAll());
        return ADMIN_PRODUCT_FORM;
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute ProductDto productDto,
                              @RequestParam(value = "image", required = false) MultipartFile imageFile,
                              RedirectAttributes attributes) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                if (!imageUploadService.isValidImage(imageFile)) {
                    attributes.addFlashAttribute(ERROR_ATTR, INVALID_IMAGE_MSG);
                    return REDIRECT_ADMIN_PRODUCTS_NEW;
                }

                String imageUrl = imageUploadService.uploadImage(imageFile);
                productDto.setImageUrl(imageUrl);
            }

            productService.createProduct(productDto);
            attributes.addFlashAttribute(SUCCESS_ATTR, PRODUCT_CREATED_MSG);
        } catch (IOException e) {
            attributes.addFlashAttribute(ERROR_ATTR, IMAGE_UPLOAD_ERROR_MSG + e.getMessage());
            return REDIRECT_ADMIN_PRODUCTS_NEW;
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
            return REDIRECT_ADMIN_PRODUCTS_NEW;
        }
        return REDIRECT_ADMIN_PRODUCTS;
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        ProductDto product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));
        model.addAttribute("product", product);
        model.addAttribute(CATEGORIES_ATTR, categoryService.findAll());
        return ADMIN_PRODUCT_FORM;
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDto productDto,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                RedirectAttributes attributes) {
        try {
            ProductDto existingProduct = productService.findById(id)
                    .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));

            if (imageFile != null && !imageFile.isEmpty()) {
                if (!imageUploadService.isValidImage(imageFile)) {
                    attributes.addFlashAttribute(ERROR_ATTR, INVALID_IMAGE_MSG);
                    return "redirect:/admin/products/" + id + EDIT_PATH;
                }

                if (existingProduct.getImageUrl() != null) {
                    imageUploadService.deleteImage(existingProduct.getImageUrl());
                }

                String imageUrl = imageUploadService.uploadImage(imageFile);
                productDto.setImageUrl(imageUrl);
            } else {
                productDto.setImageUrl(existingProduct.getImageUrl());
            }

            productService.updateProduct(id, productDto);
            attributes.addFlashAttribute(SUCCESS_ATTR, PRODUCT_UPDATED_MSG);
        } catch (IOException e) {
            attributes.addFlashAttribute(ERROR_ATTR, IMAGE_UPLOAD_ERROR_MSG + e.getMessage());
            return "redirect:/admin/products/" + id + EDIT_PATH;
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
            return "redirect:/admin/products/" + id + EDIT_PATH;
        }
        return REDIRECT_ADMIN_PRODUCTS;
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            ProductDto product = productService.findById(id)
                    .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND_MSG));

            if (product.getImageUrl() != null) {
                imageUploadService.deleteImage(product.getImageUrl());
            }

            productService.deleteProduct(id);
            attributes.addFlashAttribute(SUCCESS_ATTR, PRODUCT_DELETED_MSG);
        } catch (RuntimeException e) {
            attributes.addFlashAttribute(ERROR_ATTR, e.getMessage());
        }
        return REDIRECT_ADMIN_PRODUCTS;
    }

    @GetMapping("/login-history")
    public String loginHistory(@RequestParam(required = false) String username,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                               Model model) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<LoginHistoryDto> loginHistory = loginHistoryService.findByFilters(username, startDateTime, endDateTime);

        model.addAttribute("loginHistory", loginHistory);
        model.addAttribute("username", username);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/login-history";
    }
}