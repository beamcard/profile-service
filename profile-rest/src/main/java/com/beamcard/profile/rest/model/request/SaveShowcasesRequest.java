package com.beamcard.profile.rest.model.request;

import jakarta.validation.Valid;
import java.util.List;

public record SaveShowcasesRequest(@Valid List<ShowcaseRequest> showcases) {}
