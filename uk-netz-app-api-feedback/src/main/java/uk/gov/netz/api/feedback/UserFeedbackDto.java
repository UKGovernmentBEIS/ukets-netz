package uk.gov.netz.api.feedback;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.common.validation.SpELExpression;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SpELExpression(
    expression = "{(#satisfactionRate ne 'NOT_APPLICABLE_NOT_USED_YET') "
    + "&& (#userRegistrationRate ne 'NOT_APPLICABLE_NOT_USED_YET')}",
    message = "feedback.rating.invalid")
public class UserFeedbackDto {

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating satisfactionRate;

    @Size(max = 1200)
    private String satisfactionRateReason;

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating userRegistrationRate;

    @Size(max = 1200)
    private String userRegistrationRateReason;

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating onlineGuidanceRate;

    @Size(max = 1200)
    private String onlineGuidanceRateReason;

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating creatingAccountRate;

    @Size(max = 1200)
    private String creatingAccountRateReason;

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating onBoardingRate;

    @Size(max = 1200)
    private String onBoardingRateReason;

    @NotNull(message = "{feedback.rating.notempty}")
    private FeedbackRating tasksRate;

    @Size(max = 1200)
    private String tasksRateReason;

    @Size(max = 1200)
    private String improvementSuggestion;

}
