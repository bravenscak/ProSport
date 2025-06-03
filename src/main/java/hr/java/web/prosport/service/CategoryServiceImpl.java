package hr.java.web.prosport.service;

import hr.java.web.prosport.dto.CategoryDto;
import hr.java.web.prosport.model.Category;
import hr.java.web.prosport.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public Optional<CategoryDto> findById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public Optional<CategoryDto> findByName(String name) {
        return categoryRepository.findByName(name)
                .map(this::mapToDTO);
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new RuntimeException("Kategorija s nazivom '" + categoryDto.getName() + "' već postoji");
        }

        Category category = mapToEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));

        Optional<Category> categoryWithSameName = categoryRepository.findByName(categoryDto.getName());
        if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getId().equals(id)) {
            throw new RuntimeException("Kategorija s nazivom '" + categoryDto.getName() + "' već postoji");
        }

        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati kategoriju koja sadrži proizvode");
        }

        categoryRepository.delete(category);
    }

    private Category mapToEntity(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    private CategoryDto mapToDTO(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}