package autotesting.nbankapi.iteration2.senior.models.profile;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerProfileResponseDto extends BaseModel {
    private GetCustomerProfileResponseDto customer;
    private String message;
}
