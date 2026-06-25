package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReorderLinksRequest(@NotEmpty List<@NotNull UUID> ids) {}
