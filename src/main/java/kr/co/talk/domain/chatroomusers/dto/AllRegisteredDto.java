package kr.co.talk.domain.chatroomusers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllRegisteredDto {
    private boolean allRegistered;
    private int questionNumber;
}
