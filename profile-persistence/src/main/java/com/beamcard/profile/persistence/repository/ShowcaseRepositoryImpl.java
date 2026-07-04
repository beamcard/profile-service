package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.model.ShowcaseStep;
import com.beamcard.profile.domain.repository.ShowcaseRepository;
import com.beamcard.profile.persistence.model.ShowcaseJpa;
import com.beamcard.profile.persistence.model.ShowcaseStepJpa;
import com.beamcard.profile.persistence.repository.jpa.ShowcaseJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ShowcaseStepJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShowcaseRepositoryImpl implements ShowcaseRepository {

    private final ShowcaseJpaRepository showcaseRepository;
    private final ShowcaseStepJpaRepository stepRepository;

    @Override
    public List<Showcase> findByProfileId(UUID profileId) {
        return showcaseRepository.findByProfileIdOrderByPositionAsc(profileId).stream()
                .map(this::toShowcase)
                .toList();
    }

    @Override
    public void replace(UUID profileId, List<Showcase> showcases) {
        showcaseRepository.deleteByProfileId(profileId);
        int showcasePosition = 0;
        for (Showcase showcase : showcases) {
            ShowcaseJpa savedShowcase = showcaseRepository.save(ShowcaseJpa.builder()
                    .profileId(profileId)
                    .title(showcase.title())
                    .intro(showcase.intro())
                    .position(showcasePosition++)
                    .build());
            int stepPosition = 0;
            for (ShowcaseStep step : showcase.stepsOrEmpty()) {
                stepRepository.save(ShowcaseStepJpa.builder()
                        .showcaseId(savedShowcase.getId())
                        .imageKey(step.imageKey())
                        .description(step.description())
                        .position(stepPosition++)
                        .build());
            }
        }
    }

    private Showcase toShowcase(ShowcaseJpa showcase) {
        List<ShowcaseStep> steps = stepRepository.findByShowcaseIdOrderByPositionAsc(showcase.getId()).stream()
                .map(step -> new ShowcaseStep(step.getImageKey(), step.getDescription()))
                .toList();
        return new Showcase(showcase.getTitle(), showcase.getIntro(), steps);
    }
}
