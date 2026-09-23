package mb.demo.applications.petstore.analyzer.base.cucumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mb.demo.applications.petstore.analyzer.webapi.model.AvailabilityRatioResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.HasAvailableResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.TotalResponse;

@NoArgsConstructor
@Getter
@Setter
public class TestDataHolder {

    private HasAvailableResponse hasAvailableResponse;
    private AvailabilityRatioResponse availabilityRatioResponse;
    private TotalResponse totalDogsResponse;
    private TotalResponse totalCatsResponse;
    private TotalResponse totalPetsWithTagResponse;
    private TotalResponse totalAvailablePetsResponse;
    private Exception exception;
}
