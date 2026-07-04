package com.beamcard.profile.rest.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ShowcaseRequest(
        @Size(max = 120) String title, @Size(max = 500) String intro, @Valid List<ShowcaseStepRequest> steps) {}
