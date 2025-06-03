package hr.java.web.prosport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @NotBlank(message = "Naziv kategorije je obavezan")
    @Size(min = 2, max = 50, message = "Naziv mora imati između 2 i 50 znakova")
    private String name;
    private String description;

    public CategoryDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
}