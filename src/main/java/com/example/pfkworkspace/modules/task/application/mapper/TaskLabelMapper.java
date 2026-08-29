package com.example.pfkworkspace.modules.task.application.mapper;

import com.example.pfkworkspace.modules.label.api.exception.LabelNotFoundException;
import com.example.pfkworkspace.modules.label.domain.Label;
import com.example.pfkworkspace.modules.label.infrastructure.repo.LabelRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskLabelMapper {

    private final LabelRepository labelRepository;

    public Set<Label> toEntities(Set<UUID> labelIds, UUID projectId) {
        if (labelIds == null || labelIds.isEmpty()) {
            return Set.of();
        }
        List<Label> labels = labelRepository.findAllByIdInAndProjectId(labelIds, projectId);
        if (labels.size() != labelIds.size()) {
            throw new LabelNotFoundException("One or more labels were not found in this project");
        }

        return Set.copyOf(labels);
    }
}
