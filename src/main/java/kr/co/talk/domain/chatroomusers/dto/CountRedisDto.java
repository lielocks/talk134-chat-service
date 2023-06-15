package kr.co.talk.domain.chatroomusers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountRedisDto {
    private long userId;
    private long roomId;
    private int count;
}
