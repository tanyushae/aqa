package autotesting.nbankapi.iteration2.senior.comparison;

import autotesting.nbankapi.iteration2.senior.models.BaseModel;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ModelAssertions {

    private final SoftAssertions softly;
    private final BaseModel request;
    private final BaseModel response;

    private ModelAssertions(SoftAssertions softly, BaseModel request, BaseModel response) {
        this.softly = softly;
        this.request = request;
        this.response = response;
    }

    public static ModelAssertions assertThatModels(SoftAssertions softly, BaseModel request, BaseModel response) {
        return new ModelAssertions(softly, request, response);
    }

    public void match() {
        ModelComparisonConfigLoader.ComparisonRule rule =
                ModelComparisonConfigLoader
                        .getInstance()
                        .getRuleFor(request.getClass());

        assertThat(rule)
                .as("Comparison rule for %s", request.getClass().getSimpleName())
                .isNotNull();

        ModelComparator.ComparisonResult result =
                ModelComparator.compareFields(request, response, rule.getFieldMappings());

        softly.assertThat(result.isSuccess())
                .as("Model comparison: %s", result)
                .isTrue();
    }
}
