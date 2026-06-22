package com.qadeer.uptimedesk.check;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-results")
public class CheckResultController {

    private final CheckResultRepository checkResultRepository;

    public CheckResultController(CheckResultRepository checkResultRepository) {
        this.checkResultRepository = checkResultRepository;
    }

    @GetMapping("/{id}")
    CheckResultResponse getCheckResult(@PathVariable Long id) {
        return checkResultRepository.findById(id)
                .map(CheckResultResponse::from)
                .orElseThrow(() -> new CheckResultNotFoundException(id));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class CheckResultNotFoundException extends RuntimeException {
        CheckResultNotFoundException(Long id) {
            super("Check result not found: " + id);
        }
    }
}
