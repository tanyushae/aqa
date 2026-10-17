package nbankApi.iteration2.middle.assertions;

import io.restassured.response.ValidatableResponse;
import nbankApi.iteration2.middle.models.profile.CustomerResponseDto;
import nbankApi.iteration2.middle.models.profile.UpdateCustomerProfileResponseDto;
import nbankApi.iteration2.middle.requests.profile.GetCustomerProfileRequest;
import nbankApi.iteration2.middle.specs.RequestSpecs;
import nbankApi.iteration2.middle.specs.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;

public class ProfileAssertions {

    public static void assertUserProfile(SoftAssertions softly, String userToken,
                                         int userId, String expectedName) {

        CustomerResponseDto customer = new GetCustomerProfileRequest(
                RequestSpecs.authSpec(userToken),
                ResponseSpecs.successOk()
        )
                .get()
                .extract()
                .as(CustomerResponseDto.class);

        softly.assertThat(customer).isNotNull();
        if (customer != null) {
            softly.assertThat(customer.getId())
                    .as("Check customer ID")
                    .isEqualTo(userId);

            softly.assertThat(customer.getName())
                    .as("Check customer name")
                    .isEqualTo(expectedName);
        }
    }

    public static void assertProfileUpdateResponse(SoftAssertions softly, ValidatableResponse response,
                                                   int expectedUserId, String expectedName) {

        UpdateCustomerProfileResponseDto responseDto = response.extract()
                .as(UpdateCustomerProfileResponseDto.class);

        softly.assertThat(responseDto).isNotNull();

        if (responseDto != null) {
            softly.assertThat(responseDto.getMessage())
                    .as("Check response message")
                    .isEqualTo("Profile updated successfully");

            softly.assertThat(responseDto.getCustomer().getId())
                    .as("Check customer ID")
                    .isEqualTo(expectedUserId);

            softly.assertThat(responseDto.getCustomer().getName())
                    .as("Check customer name")
                    .isEqualTo(expectedName);
        }
    }
}

